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

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.p4.imports.FileImporter;
import org.polymap.p4.imports.ShapeFileValidator;
import org.polymap.p4.imports.ShapeImportPanelUpdater;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.UploadService;
import org.polymap.rhei.batik.toolkit.md.Snackbar.MessageType;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class UploadHelper
        implements IUploadHandler {

    private static Log                               log = LogFactory.getLog( UploadHelper.class );

    private final Map<String,Map<String,List<File>>> files;

    private final ShapeImportPanelUpdater            shapeImportPanelUpdater;

    private final IssueReporter                      issueReporter;


    public UploadHelper( Map<String,Map<String,List<File>>> files, ShapeImportPanelUpdater shapeImportPanelUpdater,
            IssueReporter issueReporter ) {
        this.files = files;
        this.shapeImportPanelUpdater = shapeImportPanelUpdater;
        this.issueReporter = issueReporter;
    }


    @SuppressWarnings("unchecked")
    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

        try {
            List<File> read = new FileImporter().run( clientFile.getName(), clientFile.getType(), in );
            if (read.isEmpty()) {
                ShapeFileValidator.reportError( clientFile.getName(), "There are no files contained." );
            }
            else {
                Map<String,List<File>> grouped = FileGroupHelper.groupFilesByName( read );
                if (!files.containsKey( clientFile.getName() )) {
                    files.put( clientFile.getName(), grouped );
                }
                else {
                    files.put( clientFile.getName() + "_duplicated", grouped );
                }
            }
            UIThreadExecutor.async( ( ) -> shapeImportPanelUpdater.updateListAndFAB( clientFile.getName(), true ),
                    UIThreadExecutor.runtimeException() );
        }
        catch (Exception e) {
            files.put( clientFile.getName(), Collections.EMPTY_MAP );
            log.error( "Unable to import file.", e );
            UIThreadExecutor.async( ( ) -> {
                shapeImportPanelUpdater.updateListAndFAB( clientFile.getName(), false );
                ShapeFileValidator.reportError( clientFile.getName(), "Unable to import file." );
                issueReporter.showIssue( MessageType.ERROR, "Unable to import file." );
            }, UIThreadExecutor.runtimeException() );
        }
    }
    
    public DropTargetAdapter createDropTargetAdapter() {
        DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {

            @Override
            public void drop( DropTargetEvent ev ) {
                ClientFile[] clientFiles = (ClientFile[])ev.data;
                Arrays.stream( clientFiles ).forEach( clientFile -> {
                    log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

                    String uploadUrl = UploadService.registerHandler( UploadHelper.this );

                    ClientFileUploader uploader = RWT.getClient().getService( ClientFileUploader.class );
                    uploader.submit( uploadUrl, new ClientFile[] { clientFile } );
                } );
            }
        };
        return dropTargetAdapter;
    }    
}
