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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportTreeContentProvider implements ITreeContentProvider {
    /**
     * 
     */
    private static final long serialVersionUID = 5968111183538777162L;

    private Map<String,Map<String,List<File>>> files;


    /**
     * 
     */
    public ShapeImportTreeContentProvider( Map<String,Map<String,List<File>>> files ) {
        this.files = files;
    }

    @Override
    public Object[] getElements( Object elm ) {
        return files.keySet().toArray();
    }


    @SuppressWarnings("rawtypes")
    @Override
    public Object getParent( Object element ) {
        if (element instanceof Map.Entry) {
            return ((Map.Entry)element).getKey();
        }
        return null;
    }


    @Override
    public boolean hasChildren( Object element ) {
        return (element instanceof String) || (element instanceof Map.Entry<?,?>);
    }

    @Override
    public Object[] getChildren( Object parentElement ) {
        if (parentElement instanceof String) {
            Map<String,List<File>> map = files.get( parentElement );
            if (map.containsKey( parentElement )) {
                return map.get( parentElement ).toArray();
            }
            else {
                return map.entrySet().toArray();
            }
        }
        else if (parentElement instanceof Map.Entry<?,?>) {
            Map.Entry<?,?> mapEntry = (Map.Entry<?,?>)parentElement;
            return ((List<?>)mapEntry.getValue()).toArray();
        }
        else {
            return new Object[0];
        }
    }


    @Override
    public void dispose() {
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
    }
}
