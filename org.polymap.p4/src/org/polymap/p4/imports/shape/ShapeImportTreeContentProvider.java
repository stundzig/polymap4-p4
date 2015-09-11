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
package org.polymap.p4.imports.shape;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

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

    private Map<String,Map<String,List<File>>> files;


    /**
     * 
     */
    public ShapeImportTreeContentProvider( Map<String,Map<String,List<File>>> files ) {
        this.files = files;
    }


    @Override
    public Object[] getElements( Object elm ) {
        return files.keySet().stream().flatMap( key -> {
            if(files.get( key ).isEmpty()) {
                return Collections.singletonList( key ).stream();
            } else if(files.get( key ).containsKey( key )) { 
                return files.get( key ).keySet().stream(); 
            } else {
                return files.get( key ).entrySet().stream().map( e -> key + " / " + e.getKey() ).collect( Collectors.toList()).stream();
            }
        } ).toArray();
    }


    @Override
    public Object getParent( Object element ) {
        if (element instanceof File) {
            return files
                    .entrySet()
                    .stream()
                    .filter(
                            e -> e.getValue().entrySet().stream().filter( e2 -> e2.getValue().contains( element ) )
                                    .count() == 1 ).findFirst();
        }
        return null;
    }


    @Override
    public boolean hasChildren( Object element ) {
        return element instanceof String;
    }


    @Override
    public Object[] getChildren( Object parentElement ) {
        if (parentElement instanceof String) {
            String key = (String) parentElement;
            String key1 = null;
            String key2 = null;
            if(key.contains( "/" )) {
                key1 = key.substring( 0, key.indexOf( "/" )).trim();
                key2 = key.substring( key.indexOf( "/" )+1).trim();
            } else {
                key1 = key;
                key2 = key;
            }
            Map<String,List<File>> map = files.get( key1 );
            if(map != null) {
                if (map.containsKey( key2 )) {
                    return map.get( key2 ).toArray();
                }
                else {
                    return map.entrySet().stream().flatMap( e -> e.getValue().stream() ).collect( Collectors.toList() )
                            .toArray();
                }
            } else {
                return new Object[0];
            }
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
            new ShapeFileValidator().validateAll( (Map<String,Map<String,List<File>>>) newInput );
        }
    }
}
