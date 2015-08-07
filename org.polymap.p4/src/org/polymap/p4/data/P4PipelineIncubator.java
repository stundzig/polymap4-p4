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
package org.polymap.p4.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.pipeline.DefaultPipelineIncubator;
import org.polymap.core.data.pipeline.PipelineIncubator;
import org.polymap.core.data.pipeline.PipelineProcessor;
import org.polymap.core.data.wms.WmsRenderProcessor;
import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4PipelineIncubator
        extends DefaultPipelineIncubator
        implements PipelineIncubator {

    private static Log log = LogFactory.getLog( P4PipelineIncubator.class );

    /** Terminal and transformer processors. */
    private static Class<PipelineProcessor>[] procTypes = new Class[] {
//        ImageEncodeProcessor.class,
//        ImageDecodeProcessor.class,
//        FeatureRenderProcessor2.class,
//        DataSourceProcessor.class,
//        RasterRenderProcessor.class
        WmsRenderProcessor.class
    };

    
    public static P4PipelineIncubator forLayer( ILayer layer ) {
        return new P4PipelineIncubator();
    }
    
    
    // instance *******************************************
    
    public P4PipelineIncubator() {
        super( procTypes );
    }
    
}
