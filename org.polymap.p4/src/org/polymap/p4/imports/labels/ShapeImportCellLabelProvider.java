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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.polymap.p4.imports.formats.FileDescription;

import com.google.common.base.Joiner;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportCellLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText( Object elem ) {
        if (elem instanceof FileDescription) {
            return "TODO: ";
        }
        return null;
    }


    @Override
    public void update( ViewerCell cell ) {
        handleBackgroundColor( cell );
        Object elem = cell.getElement();
        if (elem instanceof FileDescription) {
            FileDescription fileDesc = (FileDescription)elem;
            if (!fileDesc.parentFile.isPresent()) {
                List<String> descriptionParts = new ArrayList<String>();
                if (fileDesc.name.isPresent()) {
                    descriptionParts.add( fileDesc.name.get() );
                }
                if (fileDesc.groupName.isPresent()) {
                    descriptionParts.add( fileDesc.groupName.get() );
                }
                cell.setText( "<b>Shapefile:</b> " + Joiner.on( " / " ).join( descriptionParts ) );
            }
            else {
                cell.setText( fileDesc.name.get() );
            }
        }
    }
}
