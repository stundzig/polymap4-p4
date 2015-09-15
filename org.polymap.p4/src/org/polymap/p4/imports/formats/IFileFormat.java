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

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IFileFormat {

    public String getFileExtension();

    public String getDescription();
    
    public String getImageName();
    
    public default String getImagePath() {
        return getImageName() + ".svg";
    }

    public static String getUnknownFileImagePath() {
        return "unknown_file.svg";
    }

    public static String getMultipleFileImagePath() {
        return "file-multiple.svg";
    }
    
    public static <T extends IFileFormat> T getFileFormat( File file, List<T> values ) {
        for (T value : values) {
            if (getFileExtension( file.getName() ).equalsIgnoreCase( value.getFileExtension() )) {
                return value;
            }
        }
        return null;
    }

    static String getFileExtension( String fileName ) {
        String fileExtension = FilenameUtils.getExtension( fileName );
        if ("xml".equalsIgnoreCase( fileExtension )) {
            int index = fileName.indexOf( "." + ShapeFileFormats.SHP_XML.getFileExtension() );
            if (index > 0) {
                fileExtension = ShapeFileFormats.SHP_XML.getFileExtension();
            }
        }
        return fileExtension;
    }
}
