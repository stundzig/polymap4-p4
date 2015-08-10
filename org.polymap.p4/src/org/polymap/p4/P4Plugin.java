/* 
 * polymap.org
 * Copyright (C) 2013-2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4;

import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.CorePlugin;
import org.polymap.core.project.IMap;
import org.polymap.core.ui.ImageRegistryHelper;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.contribution.ContributionManager;

import org.polymap.service.geoserver.GeoServerServlet;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.catalog.LocalResolver;
import org.polymap.p4.project.NewLayerContribution;
import org.polymap.p4.project.ProjectRepository;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4Plugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( P4Plugin.class );
    
    public static final String  ID = "org.polymap.p4"; //$NON-NLS-1$

    /** The globale {@link Context} scope for the {@link P4Plugin}. */
    public static final String  Scope = "org.polymap.p4"; //$NON-NLS-1$

    private static P4Plugin     instance;
	

    public static P4Plugin instance() {
        return instance;
    }

    
    // instance *******************************************

    private ImageRegistryHelper     images = new ImageRegistryHelper( this );
    
    public LocalCatalog             localCatalog;
    
    public LocalResolver            localResolver;    

    private ServiceTracker          httpServiceTracker;
    
    private Optional<HttpService>   httpService = Optional.empty();

    
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        log.info( "Bundle state: " + getStateLocation() );
        log.info( "Bundle data: " + CorePlugin.getDataLocation( instance() ) );
        
        localCatalog = new LocalCatalog();
        localResolver = new LocalResolver( localCatalog );
        
        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                httpService = Optional.ofNullable( (HttpService)super.addingService( reference ) );
                
                httpService.ifPresent( service -> {
                    // fake/test GeoServer
                    UnitOfWork uow = ProjectRepository.instance.get().newUnitOfWork();
                    IMap map = uow.entity( IMap.class, "root" );
                    try {
                        // @Jörg: map enthält an dieser stelle unsere projektstruktur; diese muss
                        // im GeoServerLoader als basis verwendet werden; mir ist nicht ganz klar wie ich
                        // map zum GeoServerLoader gebracht habe und ob das so richtig gut war
                        service.registerServlet( "/wms", new GeoServerServlet(), null, null );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                });

                return httpService.get();
            }
        };
        httpServiceTracker.open();
        
        ContributionManager.addStaticSupplier( () -> new NewLayerContribution() );
//        ContributionManager.addStaticSupplier( () -> new TestProjectContribution() );
    }

    
    public void stop( BundleContext context ) throws Exception {
        httpServiceTracker.close();
        localCatalog.close();
        
        instance = null;
        super.stop( context );
    }

    
    public HttpService httpService() {
        return httpService.orElseThrow( () -> new IllegalStateException( "No HTTP service!" ) );
    }
    
    
    public Image imageForDescriptor( ImageDescriptor descriptor, String key ) {
        return images.image( descriptor, key );
    }

    
    public Image imageForName( String resName ) {
        return images.image( resName );
    }
    
    
    public ImageDescriptor imageDescriptor( String path ) {
        return images.imageDescriptor( path );
    }    

}
