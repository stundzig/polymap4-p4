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

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.CorePlugin;
import org.polymap.core.ui.ImageRegistryHelper;

import org.polymap.p4.catalog.LocalCatalog;

/**
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4Plugin 
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( P4Plugin.class );
    
	public static final String ID = "org.polymap.p4"; //$NON-NLS-1$

	private static P4Plugin instance;
	

    public static P4Plugin instance() {
        return instance;
    }

    
    // instance *******************************************

    private ImageRegistryHelper     images = new ImageRegistryHelper( this );
    
    public LocalCatalog             localCatalog;
    
    
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        log.info( "Bundle state: " + getStateLocation() );
        log.info( "Bundle data: " + CorePlugin.getDataLocation( instance() ) );
        
        localCatalog = new LocalCatalog();
    }

    public void stop( BundleContext context ) throws Exception {
        localCatalog.close();
        
        instance = null;
        super.stop( context );
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
