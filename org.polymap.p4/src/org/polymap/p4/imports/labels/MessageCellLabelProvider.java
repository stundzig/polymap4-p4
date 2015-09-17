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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.p4.imports.ShapeFileValidator;
import org.polymap.p4.imports.ValidationEvent;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.IFileFormat;
import org.polymap.p4.imports.formats.ShapeFileFormats;
import org.polymap.p4.imports.utils.TextMetricHelper;

import com.google.common.base.Joiner;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 */
public class MessageCellLabelProvider
        extends AbstractShapeImportCellLabelProvider {

    /**
     * 
     */
    private static final String            LINE_BREAK          = "\n";

    private Map<Object,StatusEventHandler> statusEventHandlers = new HashMap<Object,StatusEventHandler>();

    private ShapeFileValidator             shapeFileValidator  = null;

    private TextMetricHelper               textMetricHelper    = null;


    @Override
    public void update( ViewerCell cell ) {
        handleBackgroundColor( cell );
        Object element = cell.getElement();
        if (element instanceof FileDescription) {
            if (statusEventHandlers.get( element ) == null) {
                StatusEventHandler statusEventHandler = createStatusEventHandler( cell );
                EventManager.instance().subscribe( statusEventHandler,
                        ev -> ev instanceof ValidationEvent && isEqual( element, ev.getSource() ) );
                statusEventHandlers.put( element, statusEventHandler );
            }
            getShapeFileValidator().validate( (FileDescription)element );
        }
    }


    private StatusEventHandler createStatusEventHandler( ViewerCell cell ) {
        return new StatusEventHandler( cell );
    }


    void setShapeFileValidator( ShapeFileValidator shapeFileValidator ) {
        this.shapeFileValidator = shapeFileValidator;
    }


    ShapeFileValidator getShapeFileValidator() {
        if (shapeFileValidator == null) {
            shapeFileValidator = new ShapeFileValidator();
        }
        return shapeFileValidator;
    }


    void setTextMetricHelper( TextMetricHelper textMetricHelper ) {
        this.textMetricHelper = textMetricHelper;
    }


    TextMetricHelper getTextMetricHelper() {
        if (textMetricHelper == null) {
            textMetricHelper = new TextMetricHelper();
        }
        return textMetricHelper;
    }


    @Override
    public String getToolTipText( Object element ) {
        if (element instanceof File) {
            return "Description of " + ((File)element).getName();
        }
        else if (element instanceof String) {
            return "Description of " + String.valueOf( element );
        }
        else {
            return super.getToolTipText( element );
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
                    handleStatus( cell, ev );
                }
            } );
        }
    }


    public void handleStatus( ViewerCell cell, ValidationEvent event ) {
        int severity = event.getSeverity();
        if (severity != IStatus.OK) {
            ViewerRow row = cell.getViewerRow();
            if (row != null) {
                applyStyle( cell, severity );
                String message;
                if (event.getSource() instanceof File && cell.getElement() instanceof String) {
                    message = "There are issues with one or more contained files.";
                }
                else {
                    message = event.getMessage();
                }
                setCellText( cell, message );
            }
        }
        else {
            setCellText( cell, getDescription( cell.getElement() ) );
        }
    }


    /**
     * @param cell
     * @param string
     */
    private void setCellText( ViewerCell cell, String text ) {
        Rectangle cellBounds = cell.getControl().getBounds();
        Point point = getTextExtent( cell, text );
        if (point.x > cellBounds.width - 10) {
            text = insertLineBreaksToFitCellWidth( cell, text, cellBounds );
        }
        cell.setText( text );
    }


    private Point getTextExtent( ViewerCell cell, String text ) {
        return textMetricHelper.getTextExtent( cell, text );
    }


    private String insertLineBreaksToFitCellWidth( ViewerCell cell, String text, Rectangle cellBounds ) {
        List<String> tokens = new ArrayList<String>();
        FontMetrics fontMetrics = getFontMetrics( cell );
        for (String token : text.split( LINE_BREAK )) {
            int currentWidth = fontMetrics.getAverageCharWidth() * token.toCharArray().length;
            if (currentWidth > cellBounds.width - 10) {
                int index = token.lastIndexOf( " " );
                if (index <= 0) {
                    index = Math.round( (token.length() * (cellBounds.width - 10)) / currentWidth );
                }
                StringBuilder sb = new StringBuilder( token );
                sb.insert( index, LINE_BREAK );
                tokens.add( sb.toString() );
            }
        }
        return Joiner.on( LINE_BREAK ).join( tokens );
    }


    private FontMetrics getFontMetrics( ViewerCell cell ) {
        return textMetricHelper.getFontMetrics( cell );
    }


    /**
     * @param element
     * @return
     */
    private String getDescription( Object element ) {
        if (element instanceof FileDescription) {
            FileDescription fd = (FileDescription) element;
            if(fd.parentFile.isPresent()) {
                return getDescriptionForShapeFile( fd );
            } else {
                return getDescriptionForFile( fd );
            }
        }
        else if (element instanceof String) {
        }
        return null;
    }


    private String getDescriptionForShapeFile( FileDescription fd ) {
        Optional<ArchiveFormats> ext = Arrays.asList( ArchiveFormats.values() ).stream()
                .filter( f -> fd.name.get().endsWith( "." + f.getFileExtension() ) ).findFirst();
        if (ext.isPresent()) {
            return "description: " + ext.get().getDescription();
        }
        else {
            return "description: Shapefile group";
        }
    }


    private String getDescriptionForFile( FileDescription fd ) {
        File file = fd.file.get();
        String sizeStr = getSizeStr( fd.size.get() );
        String lastChangedStr = getLastChangedStr( file );
        IFileFormat format = fd.format.get();
        String description = "description: " + ((format != null) ? format.getDescription() : "unsupported file format");
        return description + ", " + lastChangedStr + ", " + sizeStr;
    }


    /**
     * @param file
     * @return
     */
    private String getLastChangedStr( File file ) {
        DateFormat dateFormat = DateFormat.getDateInstance( DateFormat.MEDIUM );
        return "last modified: " + dateFormat.format( new Date( file.lastModified() ) );
    }


    private String getSizeStr( long bytes ) {
        String size = "size: ";
        if (bytes != -1) {
            if (bytes < 1024) {
                double kilobytes = (bytes / 1024);
                if (kilobytes < 1024) {
                    double megabytes = (kilobytes / 1024);
                    size += megabytes + " MB";
                }
                else {
                    size += kilobytes + " KB";
                }
            }
            else {
                size += bytes + " B";
            }
        }
        return size;
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
        if (element instanceof FileDescription && src instanceof FileDescription) {
            FileDescription elementFileDescription = (FileDescription) element;
            FileDescription srcFileDescription = (FileDescription) src;
            boolean equals = true;
            if(elementFileDescription.groupName.isPresent() && srcFileDescription.groupName.isPresent()) {
                equals = elementFileDescription.groupName.get().equals( srcFileDescription.groupName.get());
            }
            return equals && elementFileDescription.name.get().equals( srcFileDescription.name.get());
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
