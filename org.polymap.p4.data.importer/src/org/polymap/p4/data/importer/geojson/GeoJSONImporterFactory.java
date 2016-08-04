/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.data.importer.geojson;

import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FilenameUtils;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ImporterFactory;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class GeoJSONImporterFactory
        implements ImporterFactory {

    @ContextIn
    protected File       file;

    @ContextIn
    protected List<File> files;


    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (isSupported( file )) {
            builder.newImporter( new GeoJSONImporter(), file );
        }
        if (files != null) {
            for (File file : files) {
                if (isSupported( file )) {
                    builder.newImporter( new GeoJSONImporter(), file );
                }
            }
        }
    }


    private boolean isSupported( File file ) {
        if (file == null) {
            return false;
        }
        if ("geojson".equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) )) {
            return true;
        }
        try {
            // XXX brute force
            if ("json".equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) )) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader( new FileReader( file ) );
                    String line;
                    // search for the second {
                    boolean firstBracketFound = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains( "featurecollection" )) {
                            return true;
                        }
                        int index = line.indexOf( "{" );
                        if (index != -1 && !firstBracketFound) {
                            firstBracketFound = true;
                            index = line.indexOf( "{", index + 1 );
                        }
                        if (index != -1 && firstBracketFound) {
                            return false;
                        }
                    }
                    return false;
                }
                finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        }
        catch (Exception e) {
            // do nothing
        }
        return false;
    }
}