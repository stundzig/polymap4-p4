/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.imports.formats;

import org.geotools.data.shapefile.shp.ShapeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.core.runtime.config.Config2;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFileDescription extends FileDescription {

    public static class ShapeFileRootDescription extends ShapeFileDescription {
        public Config2<ShapeFileRootDescription, Integer> featureCount;
        public Config2<ShapeFileRootDescription, FeatureType> featureType;
    }
    
    public static class ShpFileDescription extends ShapeFileDescription {
        public Config2<ShpFileDescription, ShapeType> geometryType;
    }
    
    public static class PrjFileDescription extends ShapeFileDescription {
        public Config2<PrjFileDescription, CoordinateReferenceSystem> crs;
    }

    public static class DbfFileDescription extends ShapeFileDescription {
        public Config2<DbfFileDescription, String> charset;
    }
}
