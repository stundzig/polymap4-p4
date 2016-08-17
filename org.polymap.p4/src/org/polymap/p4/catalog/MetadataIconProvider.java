/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.catalog;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.ui.MetadataContentProvider;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author Falko Bräutigam
 */
class MetadataIconProvider
        extends CellLabelProvider {

    @Override
    public void update( ViewerCell cell ) {
        if (cell.getElement() instanceof IMetadataCatalog) {
            cell.setImage( P4Plugin.images().svgImage( "book-open-variant.svg", NORMAL24 ) );
        }
        else if (cell.getElement() instanceof IMetadata) {
            cell.setImage( P4Plugin.images().svgImage( "buffer.svg", SvgImageRegistryHelper.NORMAL12 ) );
        }
        else if (cell.getElement() instanceof IResourceInfo) {
            cell.setImage( P4Plugin.images().svgImage( "layers.svg", SvgImageRegistryHelper.NORMAL12 ) );
        }
        else if (cell.getElement() == MetadataContentProvider.LOADING) {
            cell.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );
        }
        else {
            cell.setImage( null );
        }
    }
}