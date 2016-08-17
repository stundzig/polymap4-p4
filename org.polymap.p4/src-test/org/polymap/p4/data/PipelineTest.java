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
package org.polymap.p4.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.geotools.data.wms.WebMapServer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.image.ImageProducer;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;

@RunWith(MockitoJUnitRunner.class)
public class PipelineTest {

    private static final Log log = LogFactory.getLog( PipelineTest.class );
    
    @Mock
    private WebMapServer webMapServer;
    
    @Test
    @Ignore
    public void checkWMS2Image() throws Exception {
        
        P4PipelineIncubator incubator = P4PipelineIncubator.forLayer( null );
        
        DataSourceDescription dsd = new DataSourceDescription();
        dsd.service.set( webMapServer );
        dsd.resourceName.set( "WMS" );
        
        Pipeline p = incubator.newPipeline( ImageProducer.class, dsd, null );
        assertNotNull( p );
        assertTrue( "no processors in pipeline", p.length() > 0);
    }
}
