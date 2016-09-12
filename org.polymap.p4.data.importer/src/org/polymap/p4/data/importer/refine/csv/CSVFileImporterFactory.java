/*
 * polymap.org Copyright (C) @year@ individual contributors as indicated by
 * the @authors tag. All rights reserved.
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
package org.polymap.p4.data.importer.refine.csv;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ImporterFactory;

/**
 * Importerfactory for CSV files.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporterFactory
        implements ImporterFactory {

    private static Log              log            = LogFactory.getLog( CSVFileImporterFactory.class );

    public final static Set<String> supportedTypes = Sets.newHashSet( ".csv", ".tsv", ".txt" );

    @ContextIn
    protected File                  file;

    @ContextIn
    protected List<File>            files;


    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (isSupported( file )) {
            builder.newImporter( new CSVFileImporter(), file );
        }
        if (files != null) {
            for (File currentFile : files) {
                if (isSupported( currentFile )) {
                    builder.newImporter( new CSVFileImporter(), currentFile );
                }
            }
        }
    }


    private boolean isSupported( File f ) {
        if (f == null) {
            return false;
        }
        for (String type : supportedTypes) {
            if (f.getName().toLowerCase().endsWith( type )) {
                return true;
            }
        }
        return false;
    }

}
