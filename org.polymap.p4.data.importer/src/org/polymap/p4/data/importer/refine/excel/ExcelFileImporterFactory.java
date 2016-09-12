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
package org.polymap.p4.data.importer.refine.excel;

import java.util.List;
import java.util.Set;

import java.io.File;

import com.google.common.collect.Sets;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ImporterFactory;

/**
 * Importerfactory for Excel files.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class ExcelFileImporterFactory
        implements ImporterFactory {

    public final static Set<String> supportedTypes = Sets.newHashSet( ".xls", ".xlsx" );

    @ContextIn
    protected File                  file;

    @ContextIn
    protected List<File>            files;

    @ContextIn
    protected Sheet                 sheet;


    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (sheet != null) {
            builder.newImporter( new ExcelFileImporter(), sheet, sheet.file() );
        }
        else {
            if (isSupported( file )) {
                Sheet newSheet = new Sheet( file, -1, null );
                builder.newImporter( new ExcelFileImporter(), newSheet, newSheet.file() );
            }
            if (files != null) {
                for (File currentFile : files) {
                    if (isSupported( currentFile )) {
                        Sheet newSheet = new Sheet( currentFile, -1, null );
                        builder.newImporter( new ExcelFileImporter(), newSheet, newSheet.file() );
                    }
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
