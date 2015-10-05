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
        if (type.get() == Type.Icon) {
            if (cell.getElement() == ImportsContentProvider.LOADING) {
                cell.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );
            }
            else if (cell.getElement() instanceof Importer) {
                importerIcon( cell, (Importer)cell.getElement() );
            }
            else {
                cell.setImage( null );
            }
        }
        else if (type.get() == Type.Summary) {
            if (cell.getElement() instanceof Importer) {
                importerSummary( cell, (Importer)cell.getElement() );
            }
            else if (cell.getElement() instanceof ImportPrompt) {
                promptSummary( cell, (ImportPrompt)cell.getElement() );
            }
        }
        else if (type.get() == Type.Description) {
            if (cell.getElement() instanceof Importer) {
                importerDescription( cell, (Importer)cell.getElement() );
            }
            else if (cell.getElement() instanceof ImportPrompt) {
                promptDescription( cell, (ImportPrompt)cell.getElement() );
            }
        }
    }
    
    
    protected void importerIcon( ViewerCell cell, Importer importer ) {
        cell.setImage( importer.site().icon.get() );
    }

    protected void importerSummary( ViewerCell cell, Importer importer ) {
        cell.setText( importer.site().summary.get() );
    }

    protected void importerDescription( ViewerCell cell, Importer importer ) {
        cell.setText( importer.site().description.get() );
    }

    protected void promptSummary( ViewerCell cell, ImportPrompt prompt ) {
        cell.setText( prompt.summary.get() );
    }

    protected void promptDescription( ViewerCell cell, ImportPrompt prompt ) {
        cell.setText( prompt.description.get() );
    }
    
}
