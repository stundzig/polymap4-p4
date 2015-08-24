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
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.StatusDispatcher.Adapter2;
import org.polymap.core.ui.StatusDispatcher.Style;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class MessageCellLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    private static final long                     serialVersionUID = 1901185636012005022L;

    private Map<ViewerCell,StatusDispatcher.Adapter2> adapters         = new WeakHashMap<ViewerCell,StatusDispatcher.Adapter2>();

    private Map<String,Map<String,List<File>>>    files;


    /**
     * 
     */
    public MessageCellLabelProvider( Map<String,Map<String,List<File>>> files ) {
        this.files = files;
    }


    @Override
    public void update( ViewerCell cell ) {
        super.update( cell );
        Object element = cell.getElement();
        if(element != null) {
            Adapter2 adapter = adapters.get( cell );
            if (adapter == null) {
                adapter = ( Object src, IStatus status, Style... styles ) -> {
                    if (isEqual( element, src )) {
                        if (!status.isOK()) {
                            ViewerRow row = cell.getViewerRow();
                            if(row != null) {
                                cell.setText( getIndentation() + status.getMessage() );
                                int color = SWT.COLOR_BLACK;
                                if (status.getSeverity() == IStatus.ERROR) {
                                    color = SWT.COLOR_RED;
                                }
                                else if (status.getSeverity() == IStatus.WARNING) {
                                    color = SWT.COLOR_DARK_YELLOW;
                                }
                                cell.setForeground( Display.getCurrent().getSystemColor( color ) );
                            }
                        }
                    }
                };
                StatusDispatcher.registerAdapter( adapter );
                adapters.put( cell, adapter );
            }
            if (adapter != null) {
                new ShapeFileValidator().validate( files, cell.getElement() );
            }
        }
    }


    /**
     * @param element
     * @param src
     * @return
     */
    private boolean isEqual( Object element, Object src ) {
        if (element instanceof File && src instanceof File) {
            File elementFile = (File)element;
            File srcFile = (File)src;
            return elementFile.getName().equals( srcFile.getName() );
        }
        return element == src;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.CellLabelProvider#dispose(org.eclipse.jface
     * .viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    public void dispose( ColumnViewer viewer, ViewerColumn column ) {
        for (Adapter2 adapter : adapters.values()) {
            StatusDispatcher.unregisterAdapter( adapter );
        }
        adapters.clear();
        super.dispose( viewer, column );
    }
}
