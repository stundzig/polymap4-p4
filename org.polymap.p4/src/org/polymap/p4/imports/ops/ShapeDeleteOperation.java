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
package org.polymap.p4.imports.ops;

import java.io.File;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeDeleteOperation
        extends AbstractFileDataAwareOperation
        implements IUndoableOperation {

    @Mandatory
    public Config2<ShapeDeleteOperation, File> file;

    public ShapeDeleteOperation() {
        super( "Shapefile deletion" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            if(file.isPresent()) {
                File realFile = file.get();
                if(realFile.getParentFile().getAbsolutePath().equals( getDataDir().getAbsolutePath())) {
                    if(realFile.exists() && realFile.isFile() && realFile.canWrite()) {
                        realFile.delete();
                        return Status.OK_STATUS;
                    }
                }
                return new Status(IStatus.ERROR, P4Plugin.ID, "Couldn't delete " + realFile.getName());
            }
            return new Status(IStatus.ERROR, P4Plugin.ID, "No file to delete given.");
        }
        catch (Exception e) {
            throw new ExecutionException( e.getMessage(), e );
        }
    }

}
