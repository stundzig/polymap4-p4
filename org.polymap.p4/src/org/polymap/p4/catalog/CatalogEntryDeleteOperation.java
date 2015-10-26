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
package org.polymap.p4.catalog;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
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
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CatalogEntryDeleteOperation extends AbstractOperation implements IUndoableOperation {

    @Mandatory
    public Config2<CatalogEntryDeleteOperation, String> identifier;

    public CatalogEntryDeleteOperation() {
        super( "Catalog entry deletion" );
        ConfigurationFactory.inject( this );
    }


    @Override
    public IStatus execute( IProgressMonitor monitor, IAdaptable info ) throws ExecutionException {
        try {
            if(identifier.isPresent()) {
                P4Plugin.localCatalog().deleteEntry( identifier.get() );
                return Status.OK_STATUS;
            } else {
                return new Status(IStatus.ERROR, P4Plugin.ID, "No entry to delete given.");
            }
        }
        catch (Exception e) {
            throw new ExecutionException( e.getMessage(), e );
        }
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
