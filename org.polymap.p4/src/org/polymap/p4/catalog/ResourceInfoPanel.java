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
package org.polymap.p4.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.geotools.data.wms.WebMapServer;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.mapeditor.OlContentProvider;
import org.polymap.core.mapeditor.OlLayerProvider;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.UIThreadExecutor;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinHeightConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.rap.openlayers.base.OlFeature;
import org.polymap.rap.openlayers.control.MousePositionControl;
import org.polymap.rap.openlayers.control.ScaleLineControl;
import org.polymap.rap.openlayers.format.GeoJSONFormat;
import org.polymap.rap.openlayers.geom.PolygonGeometry;
import org.polymap.rap.openlayers.layer.ImageLayer;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.layer.VectorLayer;
import org.polymap.rap.openlayers.source.ImageSource;
import org.polymap.rap.openlayers.source.ImageWMSSource;
import org.polymap.rap.openlayers.source.VectorSource;
import org.polymap.rap.openlayers.style.FillStyle;
import org.polymap.rap.openlayers.style.StrokeStyle;
import org.polymap.rap.openlayers.style.Style;
import org.polymap.rap.openlayers.types.Attribution;
import org.polymap.rap.openlayers.types.Color;
import org.polymap.rap.openlayers.types.Coordinate;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ResourceInfoPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( ResourceInfoPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "resource" );
    
    public static final String          DASHBOARD_ID = "org.polymap.p4.catalog.resource";
    
    private Context<IResourceInfo>      res;

    private Dashboard                   dashboard;

    
    @Override
    public boolean wantsToBeShown() {
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Resource" );
        getSite().setPreferredWidth( 300 );
        
        dashboard = new Dashboard( getSite(), DASHBOARD_ID );
        dashboard.addDashlet( new BasicInfoDashlet() );
        dashboard.addDashlet( new MetadataDashlet() );
        dashboard.addDashlet( new MapDashlet() );
        dashboard.createContents( parent );
    }

    
    /**
     * 
     */
    class MapDashlet
            extends DefaultDashlet {

        private MapViewer           mapViewer;

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( "Resource Data" );
            site.constraints.get().add( new MinWidthConstraint( 500, 1 ) );
            site.constraints.get().add( new MinHeightConstraint( 500, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            new UIJob( "Create map") {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    Object service = res.get().getServiceInfo().createService( monitor );
                    UIThreadExecutor.async( 
                            () -> { createMap( parent, service); return null; }, 
                            UIThreadExecutor.logErrorMsg( "Unable to create map." ) );
                }
            }.schedule();
        }
        
        protected void createMap( Composite parent, Object service ) throws Exception {
            parent.setLayout( new FillLayout() );
            mapViewer = new MapViewer( parent );
            mapViewer.contentProvider.set( new OlContentProvider() );
            mapViewer.layerProvider.set( new OlLayerProvider() {
                @Override
                public int getPriority( Layer elm ) {
                    if (elm instanceof VectorLayer) {
                        return 10;
                    }
                    return 0;
                }
            });
            mapViewer.addMapControl( new MousePositionControl() );
            mapViewer.addMapControl( new ScaleLineControl() );
            
            // OSM layer
            Layer<ImageSource> background = new ImageLayer().source.put( new ImageWMSSource()
                    .url.put( "http://ows.terrestris.de/osm/service/" )
                    .params.put( new ImageWMSSource.RequestParams().layers.put( "OSM-WMS" ) ) );
    
            // data layer
            Layer data = null;
            ReferencedEnvelope bounds = null;
            if (service instanceof WebMapServer) {
                WebMapServer wms = (WebMapServer)service;
                String url = wms.getInfo().getSource().toString();
                String layerName = res.get().getName();
                data = new ImageLayer().source.put( new ImageWMSSource()
                        .url.put( url )
                        .params.put( new ImageWMSSource.RequestParams().layers.put( layerName ) ) );
                
                org.geotools.data.ows.Layer layer = wms.getCapabilities().getLayerList().stream()
                        .filter( l -> layerName.equals( l.getName() ) )
                        .findFirst().get();
                bounds = wms.getInfo( layer ).getBounds().transform( Geometries.crs( "EPSG:3857" ), false );
            }
            else {
                throw new RuntimeException( "Unhandled service type: " + service );
            }
            
            // fence geometry layer
            VectorSource vectorSource = new VectorSource()
                    .format.put( new GeoJSONFormat() )
                    .attributions.put( Arrays.asList( new Attribution( "Data extent" ) ) );

            VectorLayer vectorLayer = new VectorLayer()
                    .style.put( new Style()
                    .fill.put( new FillStyle().color.put( new Color( 0, 0, 255, 0.1f ) ) )
                    .stroke.put( new StrokeStyle().color.put( new Color( "red" ) ).width.put( 1f ) ) )
                    .source.put( vectorSource );

            List<Coordinate> coords = Arrays.stream( JTS.toGeometry( bounds ).getCoordinates() )
                    .map( c -> new Coordinate( c.x, c.y ) )
                    .collect( Collectors.toList() );

            OlFeature feature = new OlFeature();
            feature.name.set( "Fence" );
            feature.geometry.set( new PolygonGeometry( coords ) );
            vectorSource.addFeature( feature );

            mapViewer.maxExtent.set( bounds );
            mapViewer.setInput( new Object[] {/*background, data,*/ background, vectorLayer, data} );
            
            ReferencedEnvelope finalBounds = bounds;
            UIThreadExecutor.async( () -> mapViewer.zoomTo( finalBounds ), UIThreadExecutor.logErrorMsg( "" ) );
        }
    }
    
    
    /**
     * 
     */
    class BasicInfoDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( res.get().getTitle() );
            site.constraints.get().add( new PriorityConstraint( 100 ) );
            site.constraints.get().add( new MinWidthConstraint( 300, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            getSite().toolkit().createFlowText( parent, res.get().getDescription() );
        }        
    }
    
    
    /**
     * 
     */
    class MetadataDashlet
            extends DefaultDashlet {

        private IMetadata           metadata;

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            metadata = res.get().getServiceInfo().getMetadata();
            site.title.set( metadata.getTitle() );
            site.constraints.get().add( new PriorityConstraint( 10 ) );
            site.constraints.get().add( new MinWidthConstraint( 300, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            getSite().toolkit().createFlowText( parent, metadata.getDescription() );
        }
        
    }
    
}
