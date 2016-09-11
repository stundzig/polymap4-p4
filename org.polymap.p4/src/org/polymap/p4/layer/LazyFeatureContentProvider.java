/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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

import static org.polymap.rhei.table.FeatureTableViewer.LOADING_ELEMENT;

import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

import org.polymap.rhei.table.DefaultFeatureTableElement;
import org.polymap.rhei.table.FeatureTableViewer;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class LazyFeatureContentProvider
        implements ILazyContentProvider {

    private static final Log log = LogFactory.getLog( LazyFeatureContentProvider.class );
    
    private FeatureSource           fs;
    
    private Filter                  filter = Filter.INCLUDE;
    
    private FeatureTableViewer      viewer;
    
    /** Table index -> feature */
    private Cache<Integer,Feature>  cache = CacheConfig.defaults().concurrencyLevel( 4 ).createCache();
    
    
    public LazyFeatureContentProvider setFeatureFilter( Filter filter ) {
        this.filter = filter;
        cache.clear();
        viewer.refresh();
        return this;
    }
    
    
    @Override
    public void inputChanged( Viewer _viewer, Object oldInput, Object newInput ) {
        this.viewer = (FeatureTableViewer)_viewer;
        if (newInput == null) {
            return;
        }
        if (newInput instanceof FeatureSource) {
            this.fs = (FeatureSource)newInput;
        }
        
        cache.clear();
        viewer.setItemCount( 1 );
        viewer.replace( LOADING_ELEMENT, 0 );

        new UIJob( "Size" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                int size = fs.getFeatures( filter ).size();
                UIThreadExecutor.async( () -> {
                    if (!viewer.getControl().isDisposed()) {
                        viewer.setItemCount( size );
                    }
                });
            }
        }.schedule();
    }


    @Override
    public void dispose() {
    }

    
    @Override
    public void updateElement( int index ) {
        SimpleFeature result = (SimpleFeature)cache.get( index );
        if (result != null) {
            viewer.replace( new SimpleFeatureTableElement( result ), index );
        }
        else {
            log.info( "Index requested: " + index );
            viewer.replace( LOADING_ELEMENT, index );
            new ContentUpdater( index, 1 ).schedule();
        }
    }

    
    /**
     * 
     */
    class ContentUpdater
            extends UIJob {

        private int index;
        private int num;

        public ContentUpdater( int index, int num ) {
            super( "LazyFeatureContentProvider" );
            this.index = index;
            this.num = num;
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            Query query = new Query();
            query.setFilter( filter );
            query.setMaxFeatures( num );
            query.setStartIndex( index );
            FeatureCollection features = fs.getFeatures( query );
            UIThreadExecutor.async( () -> {
                try (
                    FeatureIterator it = features.features();
                ){
                    for (int i=0; i<num && it.hasNext(); i++) {
                        SimpleFeature feature = (SimpleFeature)it.next();
                        log.info( "Feature loaded: " + feature.getID() );
                        if (!viewer.getControl().isDisposed()) {
                            viewer.replace( new SimpleFeatureTableElement( feature ), index+i );
                        }
                    }
                }
            });
        }
    }

    public static class SimpleFeatureTableElement
            extends DefaultFeatureTableElement {

        private SimpleFeature       feature;

        public SimpleFeatureTableElement( SimpleFeature feature ) {
            this.feature = feature;
        }

        public String fid() {
            return feature.getID();
        }

        public Object getValue( String name ) {
            return feature.getAttribute( name );
        }

        public void setValue( String name, Object value ) {
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
}
