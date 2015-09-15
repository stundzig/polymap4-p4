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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.p4.catalog.CatalogPanel;
import org.polymap.p4.imports.ops.ShapeImportOperation;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelPath;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeFileImporter {

    private static Log         log = LogFactory.getLog( ShapeFileImporter.class );

    private final DefaultPanel panel;


    public ShapeFileImporter( DefaultPanel panel ) {
        this.panel = panel;
    }


    @SuppressWarnings("unchecked")
    protected void importFiles( File file ) {
        ShapeImportOperation op = new ShapeImportOperation().shpFile.put( file );
        // XXX progress?
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
            if (ev.getResult().isOK()) {
                PanelPath panelPath = panel.getSite().getPath();
                panel.getContext().closePanel( panelPath/* .removeLast( 1 ) */);
                panel.getContext().openPanel( panel.getSite().getPath(), CatalogPanel.ID );
            }
            else {
                ShapeFileValidator.reportError( file, "Unable to import file." );
                log.error( "Unable to import file.", ev.getResult().getException() );
            }
        } ) );
    }
}
