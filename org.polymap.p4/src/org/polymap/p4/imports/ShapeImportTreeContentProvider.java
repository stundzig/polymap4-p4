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
package org.polymap.p4.imports;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.polymap.p4.imports.formats.FileDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportTreeContentProvider
        implements ITreeContentProvider {

    /**
     * 
     */
    private static final long                  serialVersionUID = 5968111183538777162L;

    private List<? extends FileDescription> files;


    /**
     * 
     */
    public ShapeImportTreeContentProvider( List<? extends FileDescription> files ) {
        this.files = files;
    }


    @Override
    public Object[] getElements( Object elm ) {
        return files.toArray();
    }


    @Override
    public Object getParent( Object element ) {
        if (element instanceof FileDescription ) {
            return ((FileDescription) element).parentFile.get();
        }
        return null;
    }


    @Override
    public boolean hasChildren( Object element ) {
        if (element instanceof FileDescription ) {
            return ((FileDescription) element).getContainedFiles().size() > 0;
        }
        return false;
    }


    @Override
    public Object[] getChildren( Object parentElement ) {
        if (parentElement instanceof FileDescription ) {
            return ((FileDescription) parentElement).getContainedFiles().toArray();
        }
        else {
            return new Object[0];
        }
    }


    @Override
    public void dispose() {
    }


    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        if(newInput != null) {
            new ShapeFileValidator().validateAll( (List<FileDescription>) newInput );
        }
    }
}
