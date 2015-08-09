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
package org.polymap.p4.project;

import java.util.List;

import java.beans.PropertyChangeEvent;
import java.io.IOException;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.tx.TxProvider;
import org.polymap.rhei.batik.tx.TxProvider.Propagation;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.Messages;
import org.polymap.p4.P4AppDesign;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( ProjectPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "start" );
    
    private static final IMessages      i18n = Messages.forPrefix( "ProjectPanel" );

    @Scope(P4Plugin.Scope)
    private Context<ProjectUowProvider> uowProvider;
    
    private TxProvider<UnitOfWork>.Tx   uow;
    
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;

    protected MapViewer<ILayer>         mapViewer;
    
    
    @Override
    public void init() {
        try {
            uowProvider.compareAndSet( null, new ProjectUowProvider() );
            uow = uowProvider.get().newTx( this ).start( Propagation.REQUIRES_NEW );
            map.compareAndSet( null, uow.get().entity( IMap.class, "root" ) );
            
            EventManager.instance().subscribe( this, ev -> 
                    ev instanceof PropertyChangeEvent && ev.getSource() == map.get() );
        }
        catch (IOException e) {
            StatusDispatcher.handleError( "Unable to start application.", e );
        }
    }

    
    @EventHandler(delay=100,display=true)
    protected void onEntityChange( List<PropertyChangeEvent> evs ) {
        log.info( "evs: " + evs.size() );
        evs.forEach( ev -> log.info( "    ev=" + ev ) );
        //mapViewer.refresh();
    }
    
    
    @Override
    public void createContents( Composite parent ) {
        // title and layout
        String title = map.get().label.get();
        getSite().setTitle( title );
        getSite().setPreferredWidth( 650 );

        ((P4AppDesign)BatikApplication.instance().getAppDesign()).setAppTitle( title );
        
        parent.setLayout( new FillLayout() /*FormLayoutFactory.defaults().margins( 0 ).create()*/ );

        // mapViewer
        try {
            mapViewer = new MapViewer( parent );
            mapViewer.contentProvider.set( new ProjectContentProvider() );
            mapViewer.layerProvider.set( new ProjectLayerProvider() );
            
            // FIXME
            CoordinateReferenceSystem epsg3857 = Geometries.crs( "EPSG:3857" );
            mapViewer.maxExtent.set( new ReferencedEnvelope( 1380000, 1390000, 6680000, 6690000, epsg3857 ) );
            
            mapViewer.setInput( map.get() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        ContributionManager.instance().contributeFab( this );
    }

}
