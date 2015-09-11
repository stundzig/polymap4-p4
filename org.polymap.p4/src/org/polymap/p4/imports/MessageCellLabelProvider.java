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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerRow;

import org.eclipse.rap.rwt.RWT;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.p4.imports.shape.ShapeFileValidator;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class MessageCellLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    private Map<Object,StatusEventHandler>           statusEventHandlers = new HashMap<Object,StatusEventHandler>();

    private final Map<String,Map<String,List<File>>> files;


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
        if (element != null) {
            if (statusEventHandlers.get( element ) == null) {
                StatusEventHandler statusEventHandler = new StatusEventHandler( cell );
                EventManager.instance().subscribe( statusEventHandler,
                        ev -> ev instanceof ValidationEvent && isEqual( element, ev.getSource() ) );
                statusEventHandlers.put( element, statusEventHandler );
            }
            new ShapeFileValidator().validate( files, element );
        }
    }


    /**
     * 
     */
    private class StatusEventHandler {

        private final ViewerCell cell;


        public StatusEventHandler( ViewerCell cell ) {
            this.cell = cell;
        }


        @EventHandler(delay = 100, display = true)
        protected void onStatusChange( List<ValidationEvent> evs ) {
            evs.forEach( ev -> {
                if (cell.getItem().isDisposed()) {
                    if (statusEventHandlers.containsKey( cell.getElement() )) {
                        EventManager.instance().unsubscribe( this );
                        statusEventHandlers.remove( cell.getElement() );
                    }
                }
                else {
                    handleStatus( cell, ev.getSeverity(), ev.getMessage() );
                }
            } );
        }
    }


    public void handleStatus( ViewerCell cell, int severity, String message ) {
        if (severity != IStatus.OK) {
            ViewerRow row = cell.getViewerRow();
            if (row != null) {
                applyStyle( cell, severity );
                cell.setText( getIndentation() + message );
            }
        }
        else {
            cell.setText( "" );
        }
    }


    private void applyStyle( ViewerCell cell, int severity ) {
        // TODO: CSS doesn't work, as there seems to be no control identifying the
        // cell, where to call .setData
        // applyStyleViaCSS( cell, status );
        applyStyleViaSWT( cell, severity );
    }


    private void applyStyleViaCSS( ViewerCell cell, int severity ) {
        String cssCode = "none";
        if (severity == IStatus.ERROR) {
            cssCode = "error";
        }
        else if (severity == IStatus.WARNING) {
            cssCode = "warning";
        }
        ((TreeItem)cell.getItem()).setData( RWT.CUSTOM_VARIANT, cssCode );
    }


    private void applyStyleViaSWT( ViewerCell cell, int severity ) {
        int color = SWT.COLOR_BLACK;
        if (severity == IStatus.ERROR) {
            color = SWT.COLOR_RED;
        }
        else if (severity == IStatus.WARNING) {
            color = SWT.COLOR_DARK_YELLOW;
        }
        cell.setForeground( Display.getCurrent().getSystemColor( color ) );
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
        if (element instanceof String && src instanceof String) {
            String elementString = (String)element;
            if (elementString.contains( "/" )) {
                elementString = elementString.substring( elementString.indexOf( "/" ) + 1 ).trim();
            }
            return elementString.equals( src );

        }
        return element.equals( src );
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.CellLabelProvider#dispose(org.eclipse.jface
     * .viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
     */
    @Override
    public void dispose( ColumnViewer viewer, ViewerColumn column ) {
        statusEventHandlers.forEach( ( element, handler ) -> EventManager.instance().unsubscribe( handler ) );
        statusEventHandlers.clear();
        super.dispose( viewer, column );
    }
}
