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
package org.polymap.p4.imports;

import java.io.File;

// https://en.wikipedia.org/wiki/Shapefile
enum ShapeFileFormats {
    // @formatter:off
    AIH("aih", "an attribute index of the active fields in a table"), 
    AIN("ain", "an attribute index of the active fields in a table"), 
    ATX("atx","an attribute index for the .dbf file in the form of shapefile.columnname.atx (ArcGIS 8 and later)"), 
    CPG("cpg", "used to specify the code page (only for .dbf) for identifying the character encoding to be used"), 
    DBF("dbf", "attribute format; columnar attributes for each shape, in dBase IV format"), 
    FBN("fbn", "a spatial index of the features that are read-only"), 
    FBX("fbx", "a spatial index of the features that are read-only"), 
    IXS("ixs", "a geocoding index for read-write datasets"), 
    MXS("mxs", "a geocoding index for read-write datasets (ODB format)"), 
    PRJ("prj", "projection format; the coordinate system and projection information, a plain text file describing the projection using well-known text format"), 
    QIX("qix", "an alternative quadtree spatial index used by MapServer and GDAL/OGR software"), 
    SBN("sbn", "a spatial index of the features"), 
    SBX("sbx", "a spatial index of the features"), 
    SHP("shp", "shape format; the feature geometry itself"), 
    SHP_XML("shp.xml", "geospatial metadata in XML format, such as ISO 19115 or other XML schema"), 
    SHX("shx", "shape index format; a positional index of the feature geometry to allow seeking forwards and backwards quickly");
    // @formatter:on

    private String fileExtension, description;


    ShapeFileFormats( String fileExtension, String description ) {
        this.fileExtension = fileExtension;
        this.description = description;
    }


    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    public static ShapeFileFormats getFormat( File file ) {
        for (ShapeFileFormats value : values()) {
            if (ShapeFileValidator.getFileExtension( file.getName() ).equalsIgnoreCase( value.getFileExtension() )) {
                return value;
            }
        }
        return null;
    }
}