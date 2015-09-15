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
package org.polymap.p4.imports.labels;

import java.io.File;

import org.eclipse.jface.viewers.ViewerCell;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportCellLabelProvider extends AbstractShapeImportCellLabelProvider {

    @Override
    public void update( ViewerCell cell ) {
        handleBackgroundColor( cell );
        Object elem = cell.getElement();
        if (elem instanceof String) {
            cell.setText( String.valueOf( elem) );
        }
        else if (elem instanceof File) {
            cell.setText( ((File)elem).getName() );
        }
    }
}
