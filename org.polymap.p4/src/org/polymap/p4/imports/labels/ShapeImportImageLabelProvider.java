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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.IFileFormat;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class ShapeImportImageLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    @Override
    public void update( ViewerCell cell ) {
        handleBackgroundColor( cell );
        Object elem = cell.getElement();
        if (elem instanceof FileDescription) {
            FileDescription fileDesc = (FileDescription)elem;
            if (!fileDesc.parentFile.isPresent()) {
                Image image = null;
                if(fileDesc.name.isPresent()) {
                    Optional<ArchiveFormats> ext = Arrays.asList( ArchiveFormats.values() ).stream()
                            .filter( f -> fileDesc.name.get().endsWith( "." + f.getFileExtension() ) ).findFirst();
                    if (ext.isPresent()) {
                        image = P4Plugin.images().svgImage( ext.get().getImagePath(), NORMAL24 );
                    }
                }
                if(image == null) {
                    image = P4Plugin.images().svgImage( IFileFormat.getMultipleFileImagePath(), NORMAL24 ); 
                }
                cell.setImage( image );
            }
            else {
                Image image = null;
                if(fileDesc.format.isPresent()) {
                    IFileFormat fileFormat = fileDesc.format.get();
                    if (fileFormat != null) {
                        image = P4Plugin.images().svgImage( fileFormat.getImagePath(), NORMAL24 );
                    }
                }
                if(image == null) {
                    image = P4Plugin.images().svgImage( IFileFormat.getUnknownFileImagePath(), NORMAL24 ); 
                }
                cell.setImage( image );
            }
        }
    }
}
