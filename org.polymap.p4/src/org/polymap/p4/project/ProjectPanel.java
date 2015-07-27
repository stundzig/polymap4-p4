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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.tx.TxProvider;
import org.polymap.rhei.batik.tx.TxProvider.Propagation;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.Messages;
import org.polymap.p4.P4AppDesign;

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

    @Scope("org.polymap.p4")
    private Context<ProjectUowProvider> uowProvider;
    
    private TxProvider<UnitOfWork>.Tx   uow;
    
    @Scope("org.polymap.p4")
    protected Context<IMap>             map;

    private MapViewer<ILayer>           mapViewer;
    
    
    @Override
    public void init() {
        try {
            uowProvider.compareAndSet( null, new ProjectUowProvider() );
            uow = uowProvider.get().newTx( this ).start( Propagation.REQUIRES_NEW );
            map.compareAndSet( null, uow.get().entity( IMap.class, "root" ) );
        }
        catch (IOException e) {
            StatusDispatcher.handleError( "Unable to start application.", e );
        }
    }

    
    @Override
    public void createContents( Composite parent ) {
        // title and layout
        String title = map.get().label.get();
        getSite().setTitle( title );
        getSite().setPreferredWidth( 650 );

        ((P4AppDesign)BatikApplication.instance().getAppDesign()).setAppTitle( title );
        
        parent.setLayout( new FillLayout() /*FormLayoutFactory.defaults().margins( 0 ).create()*/ );

//        getSite().toolkit().createLabel( parent, "Karte... (" + hashCode() + ")" )
//                .setLayoutData( FormDataFactory.filled().width( 600 ).create() );

        // mapViewer
        mapViewer = new MapViewer( parent );
        mapViewer.contentProvider.set( new ProjectContentProvider() );
        mapViewer.layerProvider.set( new ProjectLayerProvider() );
        
//        // FAB
//        Button fab = ((MdToolkit)getSite().toolkit()).createFab();
//        fab.setToolTipText( "Create a new layer for this resource" );
//        fab.addSelectionListener( new SelectionAdapter() {
//            @Override
//            public void widgetSelected( SelectionEvent ev ) {
//                log.info( "..." );
//                //OperationSupport.instance().execute( );
//            }
//        });
    }

}
