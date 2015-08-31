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
package org.polymap.p4.imports;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.polymap.core.catalog.MetadataQuery;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeFetchOperation
        extends AbstractFileDataAwareOperation
        implements IUndoableOperation {

    private List<File> files = new ArrayList<File>();

    public ShapeFetchOperation() {
        super( "Shapefile fetch" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            files = Arrays.asList(getDataDir().listFiles());
            MetadataQuery entries = P4Plugin.instance().localCatalog.query( "" );
            List<String> fileNames =  entries.execute().stream().map( e -> e.getTitle().replace( ".shp", "" )).collect( Collectors.toList() );
            files = files.stream().filter( file -> !fileNames.contains( FilenameUtils.getBaseName( file.getName() ))).collect( Collectors.toList() );
            return Status.OK_STATUS;
        }
        catch (Exception e) {
            throw new ExecutionException( e.getMessage(), e );
        }
    }
    
    /**
     * @return the fileNames
     */
    public List<File> getFiles() {
        return files;
    }

    @Override
    public IStatus redo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public IStatus undo( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean canUndo() {
        return false;
    }
    
    @Override
    public boolean canRedo() {
        return false;
    }

}
