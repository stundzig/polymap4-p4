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
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.viewers.ViewerCell;
import org.polymap.p4.P4Plugin;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportImageLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    @Override
    public void update( ViewerCell cell ) {
        handleBackgroundColor( cell );
        Object elem = cell.getElement();
        if (elem instanceof String) {
            String str = String.valueOf( elem );
            Optional<ArchiveFormats> ext = Arrays.asList( ArchiveFormats.values() ).stream()
                    .filter( f -> str.contains( "." + f.getFileExtension() ) ).findFirst();
            if (ext.isPresent()) {
                cell.setImage( P4Plugin.instance().imageForName( ext.get().getImagePath() ) );
            }
            else {
                cell.setImage( P4Plugin.instance().imageForName( IFileFormat.getMultipleFileImagePath() ) );
            }
        }
        else if (elem instanceof File) {
            ShapeFileFormats shapeFileFormat = ShapeFileFormats.getFileFormat( (File)elem );
            if (shapeFileFormat != null) {
                cell.setImage( P4Plugin.instance().imageForName( shapeFileFormat.getImagePath() ) );
            }
            else {
                cell.setImage( P4Plugin.instance().imageForName( IFileFormat.getUnknownFileImagePath() ) );
            }
        }
    }
}
