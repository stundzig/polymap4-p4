/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.geotools.data.FeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.Feature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.data.pipeline.PipelineIncubationException;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.FutureJobAdapter;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.p4.P4Panel;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.data.P4PipelineIncubator;

/**
 * Carries the {@link FeatureSource}, the {@link #filter()}ed features and the
 * {@link #clicked()} feature for a given {@link ILayer}. There is one instance per
 * layer per session, retrieved from {@link #forLayer(ILayer)}.
 * <p/>
 * The currently <b>active</b> layer and its selection is stored in
 * {@link P4Panel#featureSelection()}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelection {
    
    private static Log log = LogFactory.getLog( FeatureSelection.class );

    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );

    /**
     * The selection modes.
     */
    public enum Mode { 
        REPLACE, ADD, DIFFERENCE, INTERSECT 
    };

    
    /**
     * The one and only {@link FeatureSelection} instance for the given layer.
     * <p/>
     * If the layer is not connected to a feature source (in case of WMS or image) ... ???
     *
     * @param layer
     */
    public static FeatureSelection forLayer( ILayer layer ) {
        Map<String,FeatureSelection> sessionInstances = SessionHolder.instance( SessionHolder.class ).instances;
        return sessionInstances.computeIfAbsent( (String)layer.id(), key -> new FeatureSelection( layer ) );
    }
    
    private static class SessionHolder
            extends SessionSingleton {
    
        ConcurrentMap<String,FeatureSelection>    instances = new ConcurrentHashMap();
    }
    
    
    // instance *******************************************
    
    private ILayer                      layer;
    
    /** Lazily initialized. */
    private Lazy<PipelineFeatureSource> fs = new LockedLazyInit();
    
    private Filter                      filter = Filter.INCLUDE;
    
    private Optional<Feature>           clicked = Optional.empty();
    
    
    protected FeatureSelection( ILayer layer ) {
        this.layer = layer;
    }

    
    public void select( Filter selection, Mode mode ) {
        Filter old = filter;
        switch (mode) {
            case REPLACE: 
                filter = selection; break;
            case ADD: 
                filter = ff.or( filter, selection ); break;
            default: 
                throw new RuntimeException( "Unhandled mode: " + mode );
        }
        // event
        EventManager.instance().publish( new FeatureSelectionEvent( this, filter, old ) );
    }
    
    
    public ILayer layer() {
        return layer;
    }

    
    /**
     * Waits for the {@link FeatureStore} to become available. Executes the given
     * task when available.
     * <p/>
     * This should be called from display thread.
     *
     * @param task This is executed when the {@link FeatureStore} is available. This
     *        is called from within an {@link UIJob}. Use {@link UIThreadExecutor} to
     *        update the UI.
     * @param errorHandler This is executed if there was an error while retrieving
     *        teh FeatureStore.
     */
    public void waitForFs( Consumer<FeatureStore> task, Consumer<Exception> errorHandler ) {
        new UIJob( "Wait for FeatureStore" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                try {
                    FeatureStore result = waitForFs( monitor );
                    task.accept( result );
                }
                catch (Exception e) {
                    errorHandler.accept( e );
                }
            }
        }.schedule();
    }

    
    /**
     * Call this from display thread.
     * 
     * @param timeoutMillis
     * @return The {@link FeatureStore} of the {@link #layer}.
     */
    public Future<FeatureStore> waitForFs() {
        if (fs.isInitialized()) {
            return new Future<FeatureStore>() {
                @Override
                public boolean cancel( boolean mayInterruptIfRunning ) {
                    return false;
                }
                @Override
                public boolean isCancelled() {
                    return false;
                }
                @Override
                public boolean isDone() {
                    return true;
                }
                @Override
                public FeatureStore get() throws InterruptedException, ExecutionException {
                    return fs.get();
                }
                @Override
                public FeatureStore get( long timeout, TimeUnit unit )
                        throws InterruptedException, ExecutionException, TimeoutException {
                    return get();
                }
            };
        }
        else {
            UIJob job = new UIJob( "Connect layer" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    FeatureStore result = waitForFs( monitor );
                    setProperty( FutureJobAdapter.RESULT_VALUE_NAME, result );
                }
            };
            job.schedule();
            return new FutureJobAdapter( job );
        }
    }
    
    
    /**
     * Call this from within a {@link Job}.
     * 
     * @param timeoutMillis
     * @return The {@link FeatureStore} of the {@link #layer}.
     * @throws Exception 
     */
    public FeatureStore waitForFs( IProgressMonitor monitor ) throws PipelineIncubationException, Exception {
        assert monitor != null;
        try (
            ExceptionCollector<Exception> exc = Streams.exceptions()
        ){
            return fs.get( () -> exc.check( () -> doConnectLayer( monitor ) ) );
        }
    }
    
    
    protected PipelineFeatureSource doConnectLayer( IProgressMonitor monitor ) throws PipelineIncubationException, Exception {
        log.info( "doConnectLayer(): " + layer.label.get() );
        // resolve service
        DataSourceDescription dsd = AllResolver.instance().connectLayer( layer, monitor )
                .orElseThrow( () -> new RuntimeException( "No data source for layer: " + layer ) );

        // create pipeline for it
        Pipeline pipeline = P4PipelineIncubator.forLayer( layer )
                .newPipeline( FeaturesProducer.class, dsd, null );
        if (pipeline == null || pipeline.length() == 0) {
            throw new PipelineIncubationException( "Unable to build pipeline for: " + dsd );
        }
        return new PipelineFeatureSource( pipeline );
    }
    
    
    public Filter filter() {
        return filter;
    }
    
    
    /**
     * The one feature that was 'clicked' somewhere in the UI. Usually a feature can
     * be clicked in the map and a feature table.
     */
    public Optional<Feature> clicked() {
        return clicked;
    }
    
    
    /**
     * 
     *
     * @param clicked The newly {@link #clicked()} feature, or null if currently
     *        clicked feature should be un-clicked.
     */
    public void setClicked( Feature clicked ) {
        Optional<Feature> previous = this.clicked;
        this.clicked = Optional.ofNullable( clicked );
        EventManager.instance().publish( new FeatureClickEvent( this, this.clicked, previous ) );
    }

}
