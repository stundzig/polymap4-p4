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

import static org.polymap.core.ui.FormDataFactory.on;
import static org.polymap.core.ui.SelectionAdapter.on;

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

import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.PanelSite;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.table.DefaultFeatureTableColumn;
import org.polymap.rhei.table.FeatureCollectionContentProvider.FeatureTableElement;
import org.polymap.rhei.table.FeatureTableViewer;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionTable {

    private static Log log = LogFactory.getLog( FeatureSelectionTable.class );

    private FeatureSelection            featureSelection;
    
    private FeatureCollection           features;

    private FeatureTableViewer          viewer;
    
    private PanelSite                   site;

    private Text                        searchText;

    private Button                      searchBtn;
    
    
    public FeatureSelectionTable( Composite parent, FeatureSelection featureSelection, PanelSite site ) {
        this.featureSelection = featureSelection;
        this.site = site;
        
        BatikApplication.instance().getContext().propagate( this );

        parent.setLayout( FormLayoutFactory.defaults().create() );

        try {
            this.features = featureSelection
                    .waitForFs().get()  // already loaded by LayerFeatureTableContribution
                    .getFeatures( featureSelection.filter() );
        }
        catch (Exception e) {
            log.warn( "", e );
            tk().createLabel( parent, "<p>Unable to featch features.</p>Reason: " + e.getLocalizedMessage() );
            return;
        }
        
        // topbar
        Composite topbar = on( tk().createComposite( parent ) ).fill().noBottom().height( 30 ).control();
        topbar.setLayout( FormLayoutFactory.defaults().spacing( 3 ).create() );
    
        // seach
        createTextSearch( topbar );
        on( searchBtn ).fill().noLeft().control();
        on( searchText ).fill().right( searchBtn );

        // table viewer
        createTableViewer( parent );
        on( viewer.getTable() ).fill().top( topbar );
    }
    
    
    protected MdToolkit tk() {
        return (MdToolkit)site.toolkit();
    }
    
    
    protected void createTableViewer( Composite parent ) {
        viewer = new FeatureTableViewer( parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER );
    
        // add columns
        for (PropertyDescriptor prop : features.getSchema().getDescriptors()) {
            if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                // skip Geometry
            }
            else {
                viewer.addColumn( new DefaultFeatureTableColumn( prop ) );
            }
        }
        //
        viewer.setContent( features );

        // selection -> FeaturePanel
        viewer.addSelectionChangedListener( (SelectionChangedEvent ev) -> {
            FeatureTableElement elm = (FeatureTableElement)on( ev.getSelection() ).first().get();
            log.info( "selection: " + elm );
            featureSelection.setClicked( elm.getFeature() );
            BatikApplication.instance().getContext().openPanel( site.path(), FeaturePanel.ID );
        });
    }
    
    
    protected void createTextSearch( Composite topbar ) {
        searchText = tk().createText( topbar, null, SWT.BORDER );
        searchText.setToolTipText( "Beginning of a text to search for. At least 2 characters." );
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
