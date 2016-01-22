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
package org.polymap.p4.map;

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.function.Consumer;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.security.SecurityContext;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.Messages;
import org.polymap.p4.P4AppDesign;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.project.ProjectRepository;
import org.polymap.p4.ui.P4Panel;
import org.polymap.rap.openlayers.control.MousePositionControl;
import org.polymap.rap.openlayers.control.ScaleLineControl;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectMapPanel
        extends P4Panel {

    private static Log log = LogFactory.getLog( ProjectMapPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "start" );
    
    private static final IMessages      i18n = Messages.forPrefix( "ProjectPanel" );

    /**
     * The map of this P4 instance. This instance belongs to
     * {@link ProjectRepository#unitOfWork()}. Don't forget to load a local copy for
     * an nested {@link UnitOfWork} if you are going to modify anything.
     */
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;

    public MapViewer<ILayer>            mapViewer;

    private Composite                   tableParent;
    
    
    @Override
    public void init() {
        // the 'start' panel initializes context
        map.compareAndSet( null, ProjectRepository.unitOfWork().entity( IMap.class, "root" ) );
        
        // XXX fake user login; used by ProjectNodeUser for example
        SecurityContext sc = SecurityContext.instance();
        if (!sc.isLoggedIn()) {
            if (!sc.login( "admin", "admin" )) {
                throw new RuntimeException( "Default/fake login did not succeed." );
            }
        }
    }

    
    @Override
    public void dispose() {
    }


    @Override
    public void createContents( Composite parent ) {
        // title and layout
        String title = map.get().label.get();
        site().title.set( title );
        site().preferredWidth.set( 650 );
        
        ((P4AppDesign)BatikApplication.instance().getAppDesign()).setAppTitle( title );
        
        parent.setBackground( UIUtils.getColor( 0xff, 0xff, 0xff ) );
        parent.setLayout( FormLayoutFactory.defaults().margins( 0 ).spacing( 5 ).create() );
        
        // buttom toolbar
        MdToolbar2 tb = ((MdToolkit)site().toolkit()).createToolbar( parent );
        on( tb.getControl() ).fill().noTop();
        tb.getControl().moveAbove( null );
        
        ContributionManager.instance().contributeTo( tb, this );
        
        // table area
        tableParent = on( site().toolkit().createComposite( parent, SWT.NONE ) )
                .fill().bottom( tb.getControl() ).noTop().height( 5 ).control();
        
        // mapViewer
        try {
            mapViewer = new MapViewer( parent );
            // triggers {@link MapViewer#refresh()} on {@link ProjectNodeCommittedEvent} 
            mapViewer.contentProvider.set( new ProjectContentProvider() );
            mapViewer.layerProvider.set( new ProjectLayerProvider() );
            
            // FIXME
            CoordinateReferenceSystem epsg3857 = Geometries.crs( "EPSG:3857" );
            mapViewer.maxExtent.set( new ReferencedEnvelope( 1380000, 1390000, 6680000, 6690000, epsg3857 ) );
            
            mapViewer.addMapControl( new MousePositionControl() );
            mapViewer.addMapControl( new ScaleLineControl() );
            
            mapViewer.setInput( map.get() );
            on( mapViewer.getControl() ).fill().bottom( tableParent );
            mapViewer.getControl().moveBelow( null );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        ContributionManager.instance().contributeTo( this, this );
    }

    
    /**
     * Simple/experimental way to add bottom view to this panel.
     *
     * @param creator
     */
    public void addButtomView( Consumer<Composite> creator ) {
        on( tableParent ).height( 200 );
        
        UIUtils.disposeChildren( tableParent );
        creator.accept( tableParent );
        tableParent.getParent().layout();
    }
    
}
