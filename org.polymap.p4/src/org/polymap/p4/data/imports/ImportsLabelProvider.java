/* 
 * Copyright (C) 2015, the @authors. All rights reserved.
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
package org.polymap.p4.data.imports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.BatikPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ImportsLabelProvider
        extends CellLabelProvider {

    private static Log log = LogFactory.getLog( ImportsLabelProvider.class );

    public enum Type {
        Summary, Description, Icon;
    }
    
    @Mandatory
    public Config2<ImportsLabelProvider,Type>   type;
    
    
    protected ImportsLabelProvider() {
        ConfigurationFactory.inject( this );
    }
    
    
    @Override
    public void update( ViewerCell cell ) {
        // loading        
        if (cell.getElement() == ImportsContentProvider.LOADING) {
            if (type.get() == Type.Icon) {
                cell.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );
            }
            else if (type.get() == Type.Summary) {
                cell.setText( "Crunching data..." );
            }
        }

        // ImporterContext
        else if (cell.getElement() instanceof ImporterContext) {
            ImporterContext context = (ImporterContext)cell.getElement();
            if (type.get() == Type.Summary) {
                cell.setText( context.site().summary.get() );
            }
            else if (type.get() == Type.Description) {
                cell.setText( context.site().description.get() );
            }
            else if (type.get() == Type.Icon) {
                cell.setImage( context.site().icon.get() );
            }
        }
        
        // ImportPrompt
        else if (cell.getElement() instanceof ImportPrompt) {
            ImportPrompt prompt = (ImportPrompt)cell.getElement();
            if (type.get() == Type.Summary) {
                cell.setText( prompt.summary.get() );
            }
            else if (type.get() == Type.Description) {
                cell.setText( prompt.description.get() );
            }
            else if (type.get() == Type.Icon) {
                cell.setImage( null );  // reset loading image
            }
        }
        else {
            throw new RuntimeException( "Unknown element type: " + cell.getElement().getClass().getName() );
        }
    }
    
}
