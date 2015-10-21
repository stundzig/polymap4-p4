/* 
 * polymap.org
 * Copyright (C) 2015, the @autors. All rights reserved.
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
package org.polymap.p4.data.imports.shapefile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ImporterFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShpImporterFactory
        implements ImporterFactory {

    private static Log log = LogFactory.getLog( ShpImporterFactory.class );
    
    @ContextIn
    protected List<File>            files;
    

    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (files != null) {
            Optional<File> shp = files.stream().filter( f -> f.getName().toLowerCase().endsWith( ".shp" ) ).findAny();
            if (shp.isPresent()) {
                String basename = FilenameUtils.getBaseName( shp.get().getName() );
                List<File> shpFiles = files.stream()
                        .filter( f -> FilenameUtils.getBaseName( f.getName() ).equals( basename ) )
                        .collect( Collectors.toList() );
                
                builder.newImporter( new ShpImporter(), shp.get(), shpFiles );
            }
        }
    }
    
}
