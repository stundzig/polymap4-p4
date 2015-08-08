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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.image.EncodedImageProducer;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.mapeditor.ILayerProvider;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.mapeditor.services.SimpleWmsServer;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.BlockingReference2;
import org.polymap.core.runtime.UIJob;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.LocalResolver;
import org.polymap.p4.data.P4PipelineIncubator;
import org.polymap.rap.openlayers.layer.Layer;
import org.polymap.rap.openlayers.layer.TileLayer;
import org.polymap.rap.openlayers.source.TileWMSSource;
import org.polymap.rap.openlayers.source.WMSRequestParams;

/**
 * Builds OpenLayers layer objects for the {@link MapViewer} of the {@link ProjectPanel}
 * out of {@link ILayer} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectLayerProvider
        implements ILayerProvider<ILayer>, AutoCloseable {

    private static Log log = LogFactory.getLog( ProjectLayerProvider.class );

    private String                                      alias;
    
    private Map<String,BlockingReference2<Pipeline>>    pipelines = new ConcurrentHashMap();
    
    
    public ProjectLayerProvider() {
        try {
            // register WMS servlet
            alias = "/mapviewer" + hashCode();
            P4Plugin.instance().httpService().registerServlet( alias, new SimpleWmsServer() {
                @Override
                protected String[] layerNames() {
                    throw new RuntimeException( "not yet implemented." );
                }
                @Override
                protected Pipeline createPipeline( String layerName ) {
                    return pipelines.get( layerName ).waitAndGet();
                }
            }, null, null );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    protected String createPipeline( ILayer layer ) {
        String layerName = layer.label.get();
        assert !pipelines.containsKey( layerName );
        BlockingReference2<Pipeline> emptyRef = new BlockingReference2();
        pipelines.put( layerName, emptyRef );

        new UIJob( layerName ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                // resolve service
                DataSourceDescription dsd = LocalResolver.instance().connectLayer( layer, monitor )
                        .orElseThrow( () -> new RuntimeException( "No data source for layer: " + layer ) );
                
                // create pipeline for it
                Pipeline pipeline = P4PipelineIncubator.forLayer( layer )
                        .newPipeline( EncodedImageProducer.class, dsd, null );
                emptyRef.set( pipeline );
            }
        }.schedule();
        
        return layerName;
    }


    @Override
    public Layer getLayer( ILayer elm ) {
        // start creating pipeline
        String layerName = createPipeline( elm );
        
        // make OpenLayers layer
        return new TileLayer()
                 .source.put( new TileWMSSource()
                         .url.put( ".." + alias )
                         .params.put( new WMSRequestParams()
                                 .version.put( "1.1.1" )  // send "SRS" param
                                 .layers.put( layerName )
                                 .format.put( "image/jpeg" ) ) );
    }

    
    
    @Override
    public int getPriority( ILayer elm ) {
        // XXX
        return 0;
    }


    @Override
    public void close() {
        if (alias != null) {
            P4Plugin.instance().httpService().unregister( alias );
            alias = null;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        close();
    }

}
