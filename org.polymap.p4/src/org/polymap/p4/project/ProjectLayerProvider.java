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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.mapeditor.ILayerProvider;
import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;

import org.polymap.rap.openlayers.layer.Layer;

/**
 * Builds OpenLayers layer objects for the {@link MapViewer} of the {@link ProjectPanel}
 * out of {@link ILayer} instances. Includes resolving...
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectLayerProvider
        implements ILayerProvider<ILayer> {

    private static Log log = LogFactory.getLog( ProjectLayerProvider.class );

    @Override
    public Layer getLayer( ILayer elm ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public int getPriority( ILayer elm ) {
        // XXX
        return 0;
    }

}
