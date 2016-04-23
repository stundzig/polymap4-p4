/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.ILayer;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.model.Style;
import org.polymap.core.style.model.StylePropertyValue;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;

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

    private static Log                  log = LogFactory.getLog( LayerStylePanel.class );

    public static final PanelIdentifier ID  = PanelIdentifier.parse( "layerStyle" );

    private FeatureStyle                featureStyle;


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
                    .orElseThrow( () -> new IllegalStateException( "Layer has no style." ) );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to get style of layer.", e );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Styling" ); // + ": " + layer.get().label.get() );
        getSite().setPreferredWidth( 350 );
        ContributionManager.instance().contributeTo( this, this );

        for (Style style : featureStyle.members()) {
            IPanelSection editorSection = tk().createPanelSection( parent,
                    PointStyle.class.isInstance( style ) ? "Point" : "Polygon" );
            createStyleEditor( editorSection.getBody(), style );
        }

        // fab
        Button fab = tk().createFab();
        fab.setToolTipText( "Save changes" );
        fab.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent ev ) {
                featureStyle.store();
                tk().createSnackbar( Appearance.FadeIn, "Saved" );
            }
        } );
    }


    protected void createStyleEditor( Composite parent, Style style ) {
        parent.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 0, 5 ).spacing( 5 ).create() );

        Collection<PropertyInfo<StylePropertyValue>> propInfos = style.info().getProperties();
        for (PropertyInfo<StylePropertyValue> propInfo : propInfos) {
            if (StylePropertyValue.class.isAssignableFrom( propInfo.getType() )) {
                StylePropertyField field = new StylePropertyField(
                        (Property<StylePropertyValue>)propInfo.get( style ), getContext(), getSite() );
                field.createContents( parent );
            }
        }
    }

}
