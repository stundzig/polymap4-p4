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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class AbstractShapeImportCellLabelProvider extends CellLabelProvider {
    private static final long serialVersionUID = 3274272030290362203L;
    private static final String DEFAULT_IDENT = "     ";
    private int indent = 0;
    private String indentation = "";

    @Override
    public void update( ViewerCell cell ) {
        Object elem = cell.getElement();
        if (elem instanceof String) {
            indent = 0;
            indentation = "";
        }
        else if (elem instanceof File) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= (indent + 1); i++) {
                sb.append( DEFAULT_IDENT );
            }
            indentation = sb.toString();
        }
    }
    
    /**
     * @return the indentation
     */
    public String getIndentation() {
        return indentation;
    }
}
