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
package org.polymap.p4.data.imports.refine.excel;

import java.io.File;
import java.util.Set;

import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ImporterFactory;

import com.google.common.collect.Sets;

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
    //
    // @ContextIn
    // protected List<File> files;

    @ContextIn
    protected Sheet                 sheet;

    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (isSupported( file ) || sheet != null) {
            if (sheet == null) {
                sheet = new Sheet( file, -1, null );
            }
            builder.newImporter( new ExcelFileImporter(), sheet, sheet.file() );
        }
    }


    private boolean isSupported( File file ) {
        if (file == null) {
            return false;
        }
        for (String type : supportedTypes) {
            if (file.getName().toLowerCase().endsWith( type )) {
                return true;
            }
        }
        return false;
    }

}
