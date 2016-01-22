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
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.rs.catalog.RServiceResolver;
import org.polymap.core.data.shapefile.catalog.ShapefileServiceResolver;
import org.polymap.core.data.wms.catalog.WmsServiceResolver;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

import org.polymap.p4.P4Plugin;

/**
 * Provides the connection between an {@link ILayer} -> {@link IMetadata} ->
 * {@link IServiceInfo} and back.
 * <p/>
 * Holds a static list of metadata {@link #resolvers} which are responsible of
 * creating a service/resource out of a metadata entry. A {@link ILayer} is connected
 * to an metadata entry via the {@link #resourceIdentifier(IResourceInfo)} which
 * consists of the metadata identifier and the resource name.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalResolver
        implements IMetadataResourceResolver {

    private static Log log = LogFactory.getLog( LocalResolver.class );

    public static final IMetadataResourceResolver[] resolvers = { 
            new WmsServiceResolver(), 
            new ShapefileServiceResolver(), 
            new RServiceResolver() };
    
    public static final String                      ID_DELIMITER = "|";
    
    public static LocalResolver instance() {
        return P4Plugin.localResolver();
    }
    
    
    // instance *******************************************
    
    private LocalCatalog                    localCatalog;
    
    /**
     * Cache {@link IResolvableInfo} instances in order to have just one underlying
     * service instances (WMS, Shape, RDataStore, etc.) per JVM.
     */
    private Cache<IMetadata,IResolvableInfo> resolved = CacheConfig.defaults().initSize( 128 ).createCache();
    
    
    public LocalResolver( LocalCatalog localCatalog ) {
        assert localCatalog != null;
        this.localCatalog = localCatalog;
    }


    public String resourceIdentifier( IResourceInfo res ) {
        IServiceInfo serviceInfo = res.getServiceInfo();
        IMetadata metadata = serviceInfo.getMetadata();
        return metadata.getIdentifier() + ID_DELIMITER + res.getName();
    }
    
    
    /**
     * {@link DataSourceDescription} for the given layer with potentially cached
     * service instance.
     *
     * @param layer
     * @param monitor
     * @return Newly created {@link DataSourceDescription} with potentially cached
     *         service instance.
     * @throws Exception
     */
    public Optional<DataSourceDescription> connectLayer( ILayer layer, IProgressMonitor monitor ) throws Exception {
        StringTokenizer tokens = new StringTokenizer( layer.resourceIdentifier.get(), ID_DELIMITER );
        String metadataId = tokens.nextToken();
        String resName = tokens.nextToken();
        
        IMetadata metadata = localCatalog.entry( metadataId ).get();
        
        if (metadata != null) {
            IServiceInfo serviceInfo = (IServiceInfo)resolve( metadata, monitor );
            Object service = serviceInfo.createService( monitor );
            
            DataSourceDescription result = new DataSourceDescription();
            result.service.set( service );
            result.resourceName.set( resName );
            return Optional.of( result );
        }
        else {
            return Optional.empty();
        }
    }
    
    
    // IMetadataResourceResolver **************************
    
    @Override
    public boolean canResolve( IMetadata metadata ) {
        if (resolved.get( metadata ) != null) {
            return true;
        }
        else {
            return Arrays.stream( resolvers )
                    .filter( resolver -> resolver.canResolve( metadata ) )
                    .findFirst().isPresent();
        }
    }

    
    /**
     * Resolves info for the given metadata and caches the result.
     * <p>
     * This usually <b>blocks</b> execution until backend service is available and/or connected.
     * 
     * @param metadata
     * @param monitor
     * @return Created or cached info instance.
     */
    @Override
    public IResolvableInfo resolve( IMetadata metadata, IProgressMonitor monitor ) throws Exception {
        return resolved.get( metadata, key -> {
            for (int i=0; i<resolvers.length; i++) {
                if (resolvers[i].canResolve( metadata ) ) {
                    return resolvers[i].resolve( metadata, monitor );
                }            
            }
            return (IResolvableInfo)null;
        });
    }

    @Override
    public Map<String,String> createParams( Object service ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
    
}
