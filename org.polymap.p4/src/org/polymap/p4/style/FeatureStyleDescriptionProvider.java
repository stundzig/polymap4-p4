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
package org.polymap.p4.style;

import static org.polymap.core.ui.UIUtils.sanitize;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.style.model.Style;

import org.polymap.rhei.batik.toolkit.md.MdToolkit;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class FeatureStyleDescriptionProvider
        extends CellLabelProvider {

    private static Log log = LogFactory.getLog( FeatureStyleDescriptionProvider.class );

    private MdToolkit           tk;

    
    public FeatureStyleDescriptionProvider( MdToolkit tk ) {
        this.tk = tk;
    }


    @Override
    public void update( ViewerCell cell ) {
        Style elm = (Style)cell.getElement();
        
        // default
        String description = ((Style)elm).description.get();
        description = StringUtils.abbreviate( description, 55 );
        description = StringUtils.replaceChars( description, '\n', ' ' );
        description = sanitize( description );
//        description = tk.markdown( description, cell.getItem() );
        cell.setText( description != null ? description : "[No description yet]" );
        
//        // Style images
//        if (elm instanceof StyleGroup) {
//            cell.setText( description != null ? description : "[No Description yet]" );
//        }
//        else if (elm instanceof PolygonStyle) {
//            cell.setText( description != null ? description : "Polygon" );
//        }
//        else if (elm instanceof PointStyle) {
//            cell.setText( description != null ? description : "Point/Mark" );
//        }
////        else if (cell.getElement() instanceof LineStyle) {
////            cell.setImage( P4Plugin.images().svgImage( "vector-line.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
////        }
    }
    
}
