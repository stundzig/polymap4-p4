/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.layer;

import static org.polymap.core.runtime.event.TypeEventFilter.ifType;
import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.core.ui.SelectionAdapter.on;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.PropertyIsLike;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureCollectionContentProvider.FeatureTableElement;
import org.polymap.rhei.table.FeatureTableViewer;
import org.polymap.rhei.table.IFeatureTableElement;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionTable {

    private static Log log = LogFactory.getLog( FeatureSelectionTable.class );
    
    public static final String          TOOLBAR_TAG = "FeatureSelectionTable";

    private FeatureSelection            featureSelection;
    
    private FeatureStore                fs;
    
    private FeatureCollection           features;

    private IPanel                      panel;
    
    private FeatureTableViewer          viewer;
    
    private Text                        searchText;

    private Button                      searchBtn;

    private MdToolbar2                  toolbar;

    
    public FeatureSelectionTable( Composite parent, FeatureSelection featureSelection, IPanel panel ) {
        BatikApplication.instance().getContext().propagate( this );
        this.featureSelection = featureSelection;
        this.panel = panel;

        parent.setLayout( FormLayoutFactory.defaults().create() );

        try {
            this.fs = featureSelection.waitForFs().get();  // already loaded by LayerFeatureTableContribution
            this.features = fs.getFeatures( featureSelection.filter() );
        }
        catch (Exception e) {
            log.warn( "", e );
            tk().createLabel( parent, "<p>Unable to fetch features.</p>Reason: " + e.getLocalizedMessage() );
            return;
        }
        
        // topbar
        Composite topbar = on( tk().createComposite( parent ) ).fill().noBottom().height( 36 ).control();
        topbar.setLayout( FormLayoutFactory.defaults().spacing( 3 ).margins( 3 ).create() );
    
        // seach
        createTextSearch( topbar );
        on( searchBtn ).fill().noLeft().right( 100, -205 );
        on( searchText ).fill().right( searchBtn );
        
        // toolbar
        toolbar = tk().createToolbar( topbar, SWT.RIGHT, SWT.FLAT );
        on( toolbar.getControl() ).fill().left( 100, -200 ).width( 200 );
        ContributionManager.instance().contributeTo( toolbar, panel, TOOLBAR_TAG );

        // table viewer
        createTableViewer( parent );
        on( viewer.getTable() ).fill().top( topbar );
        
        // listen to commit events
        EventManager.instance().subscribe( this, ifType( FeatureEvent.class, ev -> 
                ev.getType() == Type.COMMIT &&
                ev.getFeatureSource() == fs ) );
        
        // listen to click events
        EventManager.instance().subscribe( this, ifType( FeatureClickEvent.class, ev -> 
                ev.getSource() == this.featureSelection &&
                ev.clicked.isPresent() ) );
    }
    
    
    protected MdToolkit tk() {
        return (MdToolkit)panel.site().toolkit();
    }
    
    
    protected void createTableViewer( Composite parent ) {
        viewer = new FeatureTableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER );
    
        // add columns
        DefaultFeatureTableColumn first = null;
        for (PropertyDescriptor prop : features.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                DefaultFeatureTableColumn column = new DefaultFeatureTableColumn( prop );
                viewer.addColumn( column );
                first = first != null ? first : column;
            }
        }
        
        // it is important to sort any column; otherwise preserving selection during refresh()
        // always selects a new element, which causes an event, which causes a refresh() ...
        first.sort( SWT.DOWN );
        
        //
        viewer.setContent( features );

        // selection -> FeaturePanel
        viewer.addSelectionChangedListener( ev -> {
            log.info( "" + ev.getSelection() );
            on( ev.getSelection() ).first( FeatureTableElement.class ).ifPresent( elm -> {
                log.info( "selection: " + elm );
                featureSelection.setClicked( elm.getFeature() );
            
                BatikApplication.instance().getContext().openPanel( panel.site().path(), FeaturePanel.ID );
            });
        });        
    }
    

    @EventHandler( display=true )
    protected void onFeatureClick( FeatureClickEvent ev ) {
        if (!viewer.getTable().isDisposed()) {
            IFeatureTableElement[] selected = viewer.getSelectedElements();
            String clickedFid = ev.clicked.get().getIdentifier().getID();
            if (selected.length != 1 || !selected[0].fid().equals( clickedFid )) {
                viewer.selectElement( clickedFid, true, false );
            }
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }


    @EventHandler( display=true )
    protected void onFeatureChange( FeatureEvent ev ) {
        if (!viewer.getTable().isDisposed()) {
            // XXX this tries to preserve selection; this is index based; it causes
            // a selection event; if sort has changed, another element ist selected!
            viewer.refresh();
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }
    
    
    protected void createTextSearch( Composite topbar ) {
        searchText = tk().createText( topbar, null, SWT.BORDER );
        searchText.setToolTipText( "Text to search for in all properties.<br/>" +
                "\"Tex\" finds: \"Text\", \"Texts\", etc.<br/>" +
                "Wildcard: *, ?");
        searchText.forceFocus();
        searchText.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                searchBtn.setEnabled( searchText.getText().length() > 1 );
            }
        });
        searchText.addKeyListener( new KeyAdapter() {
            @Override
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.Selection) {
                    search();
                }
            }
        });

        searchBtn = tk().createButton( topbar, null, SWT.PUSH );
        searchBtn.setToolTipText( "Start search" );
        searchBtn.setImage( P4Plugin.images().svgImage( "magnify.svg", SvgImageRegistryHelper.WHITE24 ) );
        searchBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                search();
            }
        });
    }
    
    
    protected void search() {
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( null );
        Filter filter = Filter.INCLUDE;
        for (PropertyDescriptor prop : features.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                PropertyIsLike isLike = ff.like( ff.property( prop.getName() ), searchText.getText() + "*", "*", "?", "\\" );
                filter = filter == Filter.INCLUDE ? isLike : ff.or( filter, isLike ); 
            }
        }
        log.info( "FILTER: "  + filter );
        FeatureCollection filtered = features.subCollection( filter );
        log.info( "RESULT: "  + filtered.size() );
        viewer.setContent( filtered );
    }
    
}
