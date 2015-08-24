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
import java.util.Map;

import org.eclipse.jface.viewers.ViewerCell;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportCellLabelProvider extends AbstractShapeImportCellLabelProvider {
    private static final long serialVersionUID = -4135548787249566637L;

    @Override
    public void update( ViewerCell cell ) {
        super.update( cell );
        Object elem = cell.getElement();
        if (elem instanceof String) {
            cell.setText( getIndentation() + (String)elem );
        }
        else if (elem instanceof Map.Entry<?,?>) {
            cell.setText( getIndentation() + ((Map.Entry<?,?>)elem).getKey() );
        }
        else if (elem instanceof File) {
            cell.setText( getIndentation() + ((File)elem).getName() );
        }
    }
}
