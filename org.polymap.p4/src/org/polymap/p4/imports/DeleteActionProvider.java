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

import org.eclipse.jface.viewers.ViewerCell;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class DeleteActionProvider
        extends ActionProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 5968111183538777162L;

    private Map<String,Map<String,List<File>>> files;
    
    private UpdatableList updatableList;


    /**
     * 
     */
    public DeleteActionProvider( Map<String,Map<String,List<File>>> files, UpdatableList updatableList ) {
        this.files = files;
        this.updatableList = updatableList;
    }


    @Override
    public void update( ViewerCell cell ) {
        Object elem = cell.getElement();
        if (elem instanceof File) {
            cell.setText( "Delete" );
        }
    }


    @Override
    public void perform( MdListViewer viewer, Object element ) {
        if (element instanceof File) {
            File f = (File)element;
            ShapeDeleteOperation op = new ShapeDeleteOperation().file.put( f );
            OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
                if (ev.getResult().isOK()) {
                    for (Map.Entry<String,Map<String,List<File>>> entry : files.entrySet()) {
                        Map<String,List<File>> map = entry.getValue();
                        for (Map.Entry<String,List<File>> entry2 : map.entrySet()) {
                            if (entry2.getValue().contains( f )) {
                                entry2.getValue().remove( f );
                                if (entry2.getValue().size() == 0) {
                                    map.remove( entry2.getKey() );
                                    if (map.size() == 0) {
                                        files.remove( entry.getKey() );
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                else {
                    StatusDispatcher.handleError( "Couldn't delete file " + f.getName(), ev.getResult().getException() );
                }
                updatableList.updateListAndFAB();
            } ) );
        }
    }
}
