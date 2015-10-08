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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24_ALERT;
import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24_ERROR;
import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24_OK;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.ConfigurationFactory;
import org.polymap.core.runtime.config.Mandatory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class ImportsLabelProvider
        extends CellLabelProvider {

    private static Log log = LogFactory.getLog( ImportsLabelProvider.class );

    public enum Type {
        Summary, Description, Icon, StatusIcon, StatusText;
    }
    
    @Mandatory
    public Config2<ImportsLabelProvider,Type>   type;
    
    
    protected ImportsLabelProvider( Type type ) {
        ConfigurationFactory.inject( this );
        this.type.set( type );
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
            switch (type.get()) {
                case Summary:       cell.setText( context.site().summary.get() ); break;
                case Description:   cell.setText( context.site().description.get() ); break;
                case Icon:          cell.setImage( context.site().icon.get() ); break;
                case StatusIcon: {
                    switch (context.maxFailingPromptSeverity()) {
                        case INFO:      cell.setImage( P4Plugin.images().svgImage( "check.svg", NORMAL24_OK ) ); break;
                        case MANDATORY: cell.setImage( P4Plugin.images().svgImage( "alert-circle.svg", NORMAL24_ALERT ) );  break;
                        case VERIFY:    cell.setImage( P4Plugin.images().svgImage( "alert-circle.svg", NORMAL24_ERROR ) ); break;
                    }
                    break;
                }
                case StatusText : {
//                  cell.setText( "Click to make something great!" );
//                  cell.setForeground( Display.getCurrent().getSystemColor( SWT.COLOR_GREEN ) );
                }
            }
        }
        
        // ImportPrompt
        else if (cell.getElement() instanceof ImporterPrompt) {
            ImporterPrompt prompt = (ImporterPrompt)cell.getElement();
            switch (type.get()) {
                case Summary:       cell.setText( prompt.summary.get() ); break;
                case Description:   cell.setText( prompt.description.get() ); break;
                case Icon:          cell.setImage( P4Plugin.images().svgImage( "help.svg", SvgImageRegistryHelper.NORMAL12 ) ); break;
                case StatusIcon: {
                    if (prompt.severity.get() == Severity.INFO || prompt.ok.get()) {
                        cell.setImage( P4Plugin.images().svgImage( "check.svg", NORMAL24_OK ) ); break;
                    }
                    else if (prompt.severity.get() == Severity.VERIFY) {
                        cell.setImage( P4Plugin.images().svgImage( "alert-circle.svg", NORMAL24_ALERT ) ); break;
                    }
                    else if (prompt.severity.get() == Severity.MANDATORY) {
                        cell.setImage( P4Plugin.images().svgImage( "alert-circle.svg", NORMAL24_ERROR ) ); break;
                    }
                    break;
                }
                case StatusText: {
                    break;
                }
            }
        }
        else {
            throw new RuntimeException( "Unknown element type: " + cell.getElement().getClass().getName() );
        }
    }
    
}
