/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.imports.shape;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.data.shapefile.catalog.ShapefileServiceResolver;
import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.imports.AbstractFileDataAwareOperation;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeImportOperation
        extends AbstractFileDataAwareOperation
        implements IUndoableOperation {

    @Mandatory
    public Config2<ShapeImportOperation,File>   shpFile;
    
    
    public ShapeImportOperation() {
        super( "Shapefile import" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        String shpBasename = FilenameUtils.getBaseName( shpFile.get().getAbsolutePath() );
        
        try (
            ExceptionCollector<?> excs = Streams.exceptions();
            Updater update = P4Plugin.instance().localCatalog.prepareUpdate();
        ){
            // filter basename, copy files to dataDir
            Arrays.stream( shpFile.get().getParentFile().listFiles() )
                    .filter( f -> f.getName().startsWith( shpBasename ) )
                    .forEach( f -> excs.check( () -> {
                        if(!getDataDir().getAbsolutePath().equals(f.getParentFile().getAbsolutePath())) {
                            FileUtils.moveFileToDirectory( f, getDataDir(), true );
                        }
                        return null;
                    }));
            
            // createcatalog entry
            String shpFileURL = new File( getDataDir(), shpFile.get().getName() ).toURI().toURL().toString();
            update.newEntry( metadata -> {
                metadata.setTitle( shpFile.get().getName() );
                metadata.setConnectionParams( ShapefileServiceResolver.createParams( shpFileURL ) );
            });
            update.commit();
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getMessage(), e );
        }
    }

}
