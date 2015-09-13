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
import java.util.Map;

import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.swt.graphics.Point;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.p4.P4Plugin;

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
    private static final long serialVersionUID = 5968111183538777162L;

//    private static final String CSS_DELETE = "deleteCell";

    private Map<String,Map<String,List<File>>> files;
    
    private UpdatableList updatableList;
    

    /**
     * 
     */
    public ShapeFileDeleteActionProvider( Map<String,Map<String,List<File>>> files, UpdatableList updatableList ) {
        this.files = files;
        this.updatableList = updatableList;
    }


    @Override
    public void update( ViewerCell cell ) {
        cell.setImage( P4Plugin.images().svgImage( "ic_delete_48px.svg", NORMAL24 ) );
        // TODO: doesn't work as cell.getItem() represents the hole row (= TreeItem), cell.getControl() doesn't work either
//        ((TreeItem) cell.getItem()).setData( RWT.CUSTOM_VARIANT, CSS_DELETE );
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
     */
    @Override
    public String getToolTipText( Object element ) {
        if(element instanceof File) {
            return "Delete " + ((File) element).getName();
        } else if(element instanceof String) {
            return "Delete all files of " + String.valueOf(element);
        } else {
            return super.getToolTipText( element );
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipShift(java.lang.Object)
     */
    public Point getToolTipShift(Object object) {
        return new Point(5,5);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipDisplayDelayTime(java.lang.Object)
     */
    public int getToolTipDisplayDelayTime(Object object) {
        return 2000;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerLabelProvider#getTooltipTimeDisplayed(java.lang.Object)
     */
    public int getToolTipTimeDisplayed(Object object) {
        return 5000;
    }    

    @Override
    public void perform( MdListViewer viewer, Object element ) {
        if (element instanceof String) {
            String elementString = (String)element;
            String elementString1 = null;
            String elementString2 = null;
            if(elementString.contains( "/" )) {
                elementString1 = elementString.substring( 0, elementString.indexOf( "/" ) ).trim();
                elementString2 = elementString.substring( elementString.indexOf( "/" )+1 ).trim();
            } else {
                elementString1 = elementString;
                elementString2 = elementString;
            }
            Map<String,List<File>> entry = files.get( elementString1 );
            if(entry != null) {
                List<File> entry2 = entry.get( elementString2 );
                if(entry2 != null) {
                    entry2.stream().forEach( f -> deleteFile(f) );
                } else {
                    // directly remove entry as only dummy used as node for error messages in UI
                    files.remove( elementString1 );
                    updatableList.refresh();
                }
            }
        } else if (element instanceof File) {
            File f = (File)element;
            deleteFile( f );
        }
    }


    @SuppressWarnings("unchecked")
    private void deleteFile( File f ) {
        ShapeDeleteOperation op = new ShapeDeleteOperation().file.put( f );
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
            if (ev.getResult().isOK()) {
                for (Map.Entry<String,Map<String,List<File>>> entry : files.entrySet()) {
                    Map<String,List<File>> map = entry.getValue();
                    for (Map.Entry<String,List<File>> entry2 : map.entrySet()) {
                        if (entry2.getValue().contains( f )) {
                            entry2.getValue().remove( f );
                            if (entry2.getValue().size() == 0) {
                                map.remove( entry2.getKey() );
                                if (map.size() == 0) {
                                    files.remove( entry.getKey() );
                                }
                            }
                            break;
                        }
                    }
                }
            }
            else {
                StatusDispatcher.handleError( "Couldn't delete file " + f.getName(), ev.getResult().getException() );
            }
            updatableList.updateListAndFAB(f, false);
        } ) );
    }
}
