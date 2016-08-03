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
package org.polymap.p4.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;

import org.polymap.model2.Entity;
import org.polymap.model2.Property;

/**
 * Reference between an {@link IMap} or {@link ILayer} and it <b>outbound</b> metadata.
 *
 * @author Falko Bräutigam
 */
public class MetadataReference
        extends Entity {

    private static Log log = LogFactory.getLog( MetadataReference.class );
    
    public static MetadataReference     TYPE;
    
    /**
     * 
     */
    public Property<String>         projectNodeId;

    /**
     * 
     */
    public Property<String>         metadataId;
    
}
