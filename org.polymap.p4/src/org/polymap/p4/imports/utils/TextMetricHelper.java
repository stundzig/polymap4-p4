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
package org.polymap.p4.imports.utils;

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class TextMetricHelper {

    public Point getTextExtent(ViewerCell cell, String text) {
        GC gc = new GC(cell.getControl());
        Point point = gc.textExtent( text );
        gc.dispose();
        return point;
    }
    
    public FontMetrics getFontMetrics(ViewerCell cell) {
        GC gc = new GC(cell.getControl());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        return fontMetrics;
    }
}
