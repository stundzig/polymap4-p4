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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.p4.imports.ShapeImportPanelUpdater;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.ops.ShapeFetchOperation;
import org.polymap.rhei.batik.toolkit.md.AbstractFeedbackComponent;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class InitFileListHelper {

    private static Log                    log = LogFactory.getLog( InitFileListHelper.class );

    private final List<FileDescription> files;

    private final ShapeImportPanelUpdater shapeImportPanelUpdater;

    private final IssueReporter           issueReporter;


    public InitFileListHelper( List<FileDescription> files, ShapeImportPanelUpdater shapeImportPanelUpdater,
            IssueReporter issueReporter ) {
        this.files = files;
        this.shapeImportPanelUpdater = shapeImportPanelUpdater;
        this.issueReporter = issueReporter;
    }


    @SuppressWarnings("unchecked")
    public void initFileList() {
        ShapeFetchOperation shapeFetchOperation = new ShapeFetchOperation();
        OperationSupport.instance().execute2(
                shapeFetchOperation,
                true,
                false,
                ev -> UIThreadExecutor.asyncFast( ( ) -> {
                    if (ev.getResult().isOK()) {
                        List<File> fs = shapeFetchOperation.getFiles();
                        files.clear();
                        FileGroupHelper.fillFilesList(files, null, -1, fs);
                    }
                    else {
                        issueReporter.showIssue( AbstractFeedbackComponent.MessageType.ERROR, "Couldn't read out data." );
                        log.error( "Couldn't read out data.", ev.getResult().getException() );
                    }
                    if (!files.isEmpty()) {
                        UIThreadExecutor.async(
                                ( ) -> shapeImportPanelUpdater.updateListAndFAB( files.iterator().next(), false ),
                                UIThreadExecutor.runtimeException() );
                    }
                } ) );
    }
}
