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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.io.File;
import java.util.List;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Point;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.ops.ShapeDeleteOperation;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class ShapeFileDeleteActionProvider
        extends ActionProvider {

    /**
     * 
     */
    private static final long     serialVersionUID = 5968111183538777162L;

    // private static final String CSS_DELETE = "deleteCell";

    private List<FileDescription> files;

    private UpdatableList         updatableList;


    /**
     * 
     */
    public ShapeFileDeleteActionProvider( List<FileDescription> files, UpdatableList updatableList ) {
        this.files = files;
        this.updatableList = updatableList;
    }


    @Override
    public void update( ViewerCell cell ) {
        cell.setImage( P4Plugin.images().svgImage( "ic_delete_48px.svg", NORMAL24 ) );
        // TODO: doesn't work as cell.getItem() represents the hole row (= TreeItem),
        // cell.getControl() doesn't work either
        // ((TreeItem) cell.getItem()).setData( RWT.CUSTOM_VARIANT, CSS_DELETE );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText( Object element ) {
        if (element instanceof File) {
            return "Delete " + ((File)element).getName();
        }
        else if (element instanceof String) {
            return "Delete all files of " + String.valueOf( element );
        }
        else {
            return super.getToolTipText( element );
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object
     * )
     */
    public Point getToolTipShift( Object object ) {
        return new Point( 5, 5 );
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java
     * .lang.Object)
     */
    public int getToolTipDisplayDelayTime( Object object ) {
        return 2000;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java
     * .lang.Object)
     */
    public int getToolTipTimeDisplayed( Object object ) {
        return 5000;
    }


    @Override
    public void perform( MdListViewer viewer, Object element ) {
        if (element instanceof FileDescription) {
            FileDescription fileDescription = (FileDescription)element;
            if(fileDescription.file.isPresent()) {
                deleteFile(fileDescription);
            } else {
                files.remove( fileDescription );
                updatableList.refresh();
            }
        }
    }


    @SuppressWarnings("unchecked")
    private void deleteFile( FileDescription fileDescription ) {
        ShapeDeleteOperation op = new ShapeDeleteOperation().file.put( fileDescription.file.get() );
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
            if (ev.getResult().isOK()) {
                if(fileDescription.parentFile.isPresent()) {
                    fileDescription.parentFile.get().getContainedFiles().remove( fileDescription );
                    // remove empty groups
                    if(fileDescription.parentFile.get().getContainedFiles().size() == 0) {
                        files.remove( fileDescription.parentFile.get() );
                    }
                }
            }
            else {
                StatusDispatcher.handleError( "Couldn't delete file " + fileDescription.name.get(), ev.getResult().getException() );
            }
            updatableList.updateListAndFAB( fileDescription, false );
        } ) );
    }
}
