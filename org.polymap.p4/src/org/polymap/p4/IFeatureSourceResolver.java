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
package org.polymap.p4;

import java.util.function.Consumer;

import org.geotools.data.FeatureSource;

import org.polymap.core.project.ILayer;

/**
 * Provides the connection between a {@link ILayer} and its data/content/service.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFeatureSourceResolver {

    /**
     * 
     *
     * @param fsuid The UID of the {@link FeatureSource} to resolve.
     * @param resultHandler
     * @param errorHandler
     */
    public void resolve( String fsuid, Consumer<FeatureSource> resultHandler, Consumer<Throwable> errorHandler );
    
}
