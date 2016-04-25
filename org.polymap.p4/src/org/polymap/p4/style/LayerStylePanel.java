/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.style;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.polymap.core.runtime.UIThreadExecutor.async;
import static org.polymap.core.ui.FormDataFactory.on;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.project.ILayer;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.PolygonStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StyleGroup;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.style.ui.StylePropertyField;
import org.polymap.core.ui.ColumnDataFactory;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.ActionItem;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.model2.Property;
import org.polymap.model2.runtime.PropertyInfo;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.LayerInfoPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerStylePanel
        extends P4Panel {

    private static Log log = LogFactory.getLog( LayerStylePanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layerStyle" );
    
    public static final String          TOOLBAR = LayerStylePanel.class.getSimpleName();
    
    private FeatureStyle                featureStyle;

    private MdListViewer                list;

    private MdToolbar2                  toolbar;
    
    private IPanelSection               editorSection;
    
    
    @Override
    public boolean beforeInit() {
        IPanel parent = getContext().getPanel( getSite().getPath().removeLast( 1 ) );
        if (parent instanceof LayerInfoPanel) {
            getSite().setTitle( "" );
            getSite().setTooltip( "Edit styling" );
            getSite().setIcon( P4Plugin.images().svgImage( "brush.svg", P4Plugin.HEADER_ICON_CONFIG ) );
            return true;
        }
        return false;
    }


    @Override
    public void init() {
        try {
            ILayer layer = featureSelection.get().layer();
            featureStyle = P4Plugin.styleRepo().featureStyle( layer.styleIdentifier.get() )
                    .orElseThrow( () -> new IllegalStateException( "Layer has no style.") );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to get style of layer.", e );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Styling" );  // + ": " + layer.get().label.get() );
        getSite().setPreferredWidth( 350 );
        ContributionManager.instance().contributeTo( this, this );
        
        // toolbar
        toolbar = tk().createToolbar( parent, SWT.TOP );
        new AddPointItem( toolbar );
        new AddPolygonItem( toolbar );
        ContributionManager.instance().contributeTo( toolbar, this, TOOLBAR );
        
        // style list
        list = tk().createListViewer( parent, SWT.SINGLE, SWT.FULL_SELECTION );
        list.setContentProvider( new FeatureStyleContentProvider() );
        list.firstLineLabelProvider.set( new FeatureStyleLabelProvider() );
        list.iconProvider.set( new FeatureStyleLabelProvider() );
        list.setComparer( new StyleIdentityComparer() );
        list.addSelectionChangedListener( ev -> {
            log.info( "Event: " + ev );
            Object elm = org.polymap.core.ui.SelectionAdapter.on( ev.getSelection() ).first().get(); 
            if (elm instanceof StyleGroup) {
                // ...
            }
            else if (elm instanceof Style) {
                createStyleEditor( editorSection.getBody(), (Style)elm );   
            }
        });
        list.setInput( featureStyle );
        list.expandAll();
        if (!featureStyle.members().isEmpty()) {
            async( () -> list.setSelection( new StructuredSelection( featureStyle.members().iterator().next() ) ) );
        }
        
        //
        editorSection = tk().createPanelSection( parent, ""/*, SWT.BORDER*/ );
        
        // fab
        Button fab = tk().createFab();
        fab.setToolTipText( "Save changes" );
        fab.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                featureStyle.store();
                tk().createSnackbar( Appearance.FadeIn, "Saved" );
            }
        });
        
        // layout
        parent.setLayout( FormLayoutFactory.defaults().margins( 3, 8 ).spacing( 8 ).create() );
        on( toolbar.getControl() ).fill()/*.height( 32 )*/.noBottom();
        on( list.getControl() ).fill().top( toolbar.getControl() ).bottom( 23 );
        on( editorSection.getControl() ).fill().top( list.getControl() );
    }
    
    
    protected void createStyleEditor( Composite parent, Style style ) {
        editorSection.setTitle( defaultString( style.title.get(), "Style settings" ) );
        UIUtils.disposeChildren( parent );
        
        parent.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 5 ).spacing( 10 ).create() );
        
        Collection<PropertyInfo<StylePropertyValue>> propInfos = style.info().getProperties();
        for (PropertyInfo<StylePropertyValue> propInfo : propInfos) {
            if (StylePropertyValue.class.isAssignableFrom( propInfo.getType() )) {
                StylePropertyField field = new StylePropertyField( (Property<StylePropertyValue>)propInfo.get( style ) );
                Control control = field.createContents( parent );
                // the widthHint is a minimal width; without the fields expand the enclosing section
                control.setLayoutData( ColumnDataFactory.defaults().widthHint( 350 ).create() );
            }
        }
        parent.layout( true );
    }


    /**
     * 
     */
    class AddPointItem
            extends ActionItem {

        public AddPointItem( ItemContainer container ) {
            super( container );
            text.set( "" );
            icon.set( P4Plugin.images().svgImage( "map-marker.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Point/Marker render description" );
            action.set( ev -> {
                DefaultStyle.fillPointStyle( featureStyle.members().createElement( PointStyle.defaults ) );
                list.refresh( true );
            });
        }
    }

    
    /**
     * 
     */
    class AddPolygonItem
            extends ActionItem {

        public AddPolygonItem( ItemContainer container ) {
            super( container );
            text.set( "" );
            icon.set( P4Plugin.images().svgImage( "vector-polygon.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            tooltip.set( "Create a new Polygon render description" );
            action.set( ev -> {
                DefaultStyle.fillPolygonStyle( featureStyle.members().createElement( PolygonStyle.defaults ) );
                list.refresh( true );
            });
        }
    }

    
    /**
     * 
     */
    protected class StyleIdentityComparer
            implements IElementComparer {
    
        @Override
        public int hashCode( Object elm ) {
            return elm.hashCode();
        }
    
        @Override
        public boolean equals( Object a, Object b ) {
            return a == b;
        }
    }
    
}
