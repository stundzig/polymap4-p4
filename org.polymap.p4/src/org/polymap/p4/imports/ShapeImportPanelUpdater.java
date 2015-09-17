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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.polymap.core.ui.UIUtils;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;
import org.polymap.p4.imports.utils.IssueReporter;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.Snackbar.MessageType;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportPanelUpdater
        implements UpdatableList {

    private final List<FileDescription> files;

    private final MdListViewer          fileList;

    private final Button                importFab;

    private SelectionAdapter            selectionListener = null;

    private final IssueReporter         issueReporter;

    private final ShapeFileImporter     shapeFileImporter;


    /**
     * 
     */
    public ShapeImportPanelUpdater( List<FileDescription> files, MdListViewer fileList, Button importFab,
            IssueReporter issueReporter, ShapeFileImporter shapeFileImporter ) {
        this.files = files;
        this.issueReporter = issueReporter;
        this.fileList = fileList;
        this.importFab = importFab;
        this.shapeFileImporter = shapeFileImporter;

    }


    public void updateListAndFAB( Object root, boolean fromUpload ) {
        fileList.refresh();
        fileList.toggleItemExpand( root );

        boolean valid = new ShapeFileValidator().validateAll( files );
        if (valid) {
            List<FileDescription> shps = files.stream().flatMap( file -> file.getContainedFiles().stream() )
                    .filter( cf -> cf.name.get().toLowerCase().endsWith( "." + ShapeFileFormats.SHP ) )
                    .collect( Collectors.toList() );
            if (shps.size() > 0) {
                if (selectionListener != null) {
                    importFab.removeSelectionListener( selectionListener );
                }
                selectionListener = new SelectionAdapter() {

                    private static final long serialVersionUID = -1075952252353984655L;


                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        for (Object shp : shps) {
                            UIUtils.activateCallback( "importFiles" );
                            shapeFileImporter.importFiles( (File)shp );
                            UIUtils.deactivateCallback( "importFiles" );
                        }
                    }
                };

                importFab.addSelectionListener( selectionListener );
                importFab.setVisible( true );
                if (fromUpload) {
                    issueReporter.showIssue( MessageType.SUCCESS, root + " successfully uploaded." );
                }
            }
        }
        else {
            importFab.setVisible( false );
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.polymap.p4.imports.UpdatableList#refresh()
     */
    @Override
    public void refresh() {
        fileList.refresh();
    }
}
