/* 
 * polymap.org
 * Copyright (C) 2009-2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.tx.TxProvider;
import org.polymap.rhei.batik.tx.TxProvider.Propagation;

import org.polymap.model2.runtime.UnitOfWork;

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

    private MapViewer                   mapViewer;
    
    
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
        String title = map.get().label.get();
        getSite().setTitle( title );
        getSite().setPreferredWidth( 650 );

        ((P4AppDesign)BatikApplication.instance().getAppDesign()).setAppTitle( title );
        
        parent.setLayout( FormLayoutFactory.defaults().margins( 0 ).create() );

        getSite().toolkit().createLabel( parent, "Karte... (" + hashCode() + ")" )
                .setLayoutData( FormDataFactory.filled().width( 600 ).create() );
        
        
//        Bounds bounds = new Bounds( 4500000, 5550000, 4700000, 5700000 );

//        map = new OlMap( parent, SWT.NONE, new View()
//            .projection.put( new Projection( "EPSG:3857", Units.m ) )
//            .center.put( new Coordinate( 1387648, 6688702 ) )
//            .zoom.put( 5 ) );
//
//        map.addLayer( new ImageLayer()
//            .source.put( new ImageWMSSource()
//                .url.put( "http://ows.terrestris.de/osm/service/" )
//                .params.put( new ImageWMSSource.RequestParams().layers.put( "OSM-WMS" ) ) )
//            .opacity.put( 0.5f ) );
//
//        VectorSource vectorSource = new VectorSource()
//            .format.put( new GeoJSONFormat() )
//            .attributions.put( Arrays.asList( new Attribution( "Mapzone" ) ) );
//
//        VectorLayer vectorLayer = new VectorLayer()
//            .style.put( new Style()
//                .fill.put( new FillStyle().color.put( new Color( 0, 0, 255, 0.1f ) ) )
//                .stroke.put( new StrokeStyle().color.put( new Color( "red" ) ).width.put( 1f ) ) )
//                .source.put( vectorSource );
//
//        map.addLayer( vectorLayer );
//
//        OlFeature feature = new OlFeature();
//        feature.name.set( "Test1!" );
//        feature.labelPoint.set( map.view.get().center.get() );
//        feature.geometry.set( new PointGeometry( map.view.get().center.get() ) );
//        feature.style.put( new Style()
//                .text.put( new TextStyle()
//                        .text.put( "MY MESSAGE" )
//                        .font.put( new Font()
//                        .family.put( Font.Family.CourierNew )
//                        .weight.put( Font.Weight.bold )
//                        .size.put( 24 ) )
//                        .stroke.put( new StrokeStyle()
//                        .color.put( new Color( "green" ) )
//                        .width.put( 2f ) ) )
//                        .image.put( new CircleStyle( 5.0f )
//                        .fill.put( new FillStyle()
//                        .color.put( new Color( "red" ) ) ) ) );
//        vectorSource.addFeature( feature );
//
//        map.setLayoutData( FormDataFactory.filled().height( 500 ).create() );
//        
//        //
////        map.addControl( new NavigationControl( true ) );
////        map.addControl( new PanZoomBarControl() );
////        map.addControl( new LayerSwitcherControl() );
////        map.addControl( new MousePositionControl() );
//        map.addControl( new ScaleLineControl( null, null ) );
//        map.addControl( new ZoomSliderControl() );
//        map.addControl( new ZoomControl() );
//
//        // map.addControl( new ScaleControl() );
//        // map.addControl( new LoadingPanelControl() );
//
//        // map.setRestrictedExtend( maxExtent );
////        map.zoomToExtent( bounds, true );
//        //map.zoomTo( 2 );
    }

}
