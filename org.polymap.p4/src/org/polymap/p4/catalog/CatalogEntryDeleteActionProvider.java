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
package org.polymap.p4.catalog;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.catalog.local.LocalMetadata;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.p4.P4Plugin;

import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class CatalogEntryDeleteActionProvider
        extends ActionProvider {
    
//    private Image image = P4Plugin.instance().imageDescriptor( "resources/icons/png/gray/16/ic_delete_48px.png" ).createImage();

    @Override
    public void update( ViewerCell cell ) {
        if(cell.getElement() instanceof LocalMetadata) {
            cell.setImage( P4Plugin.images().svgImage( "ic_delete_48px.svg", NORMAL24 ) );
//          ((TreeItem) cell.getItem()).setData( RWT.CUSTOM_VARIANT, CSS_DELETE );
        } else {
            cell.setImage( null );
        }
    }


    @Override
    public void perform( MdListViewer viewer, Object element ) {
        if(element instanceof LocalMetadata) {
            LocalMetadata localMetadata = (LocalMetadata) element;
            CatalogEntryDeleteOperation op = new CatalogEntryDeleteOperation().identifier.put( localMetadata.getIdentifier() );
            OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
                if (!ev.getResult().isOK()) {
                    StatusDispatcher.handleError( "Couldn't delete entry " + localMetadata.getIdentifier(), ev.getResult().getException() );
                } else {
                    viewer.remove( element );
                    // TODO: the row background produced by the hover effect isn't removed after element remove
                }
            } ) );
            
        }
    }
}
