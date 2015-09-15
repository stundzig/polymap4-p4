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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.p4.imports.ShapeImportPanelUpdater;
import org.polymap.p4.imports.ops.ShapeFetchOperation;
import org.polymap.rhei.batik.toolkit.md.Snackbar.MessageType;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class InitFileListHelper {

    private static Log                               log = LogFactory.getLog( InitFileListHelper.class );

    private final Map<String,Map<String,List<File>>> files;

    private final ShapeImportPanelUpdater            shapeImportPanelUpdater;

    private final IssueReporter                      issueReporter;


    public InitFileListHelper( Map<String,Map<String,List<File>>> files,
            ShapeImportPanelUpdater shapeImportPanelUpdater, IssueReporter issueReporter ) {
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
                        Map<String,List<File>> grouped = FileGroupHelper.groupFilesByName( fs );
                        for (Map.Entry<String,List<File>> entry : grouped.entrySet()) {
                            Map<String,List<File>> map = new HashMap<String,List<File>>();
                            map.put( entry.getKey(), entry.getValue() );
                            files.put( entry.getKey(), map );
                        }
                    }
                    else {
                        issueReporter.showIssue( MessageType.ERROR, "Couldn't read out data." );
                        log.error( "Couldn't read out data.", ev.getResult().getException() );
                    }
                    if (!files.isEmpty()) {
                        UIThreadExecutor.async( ( ) -> shapeImportPanelUpdater.updateListAndFAB( files.keySet()
                                .iterator().next(), false ), UIThreadExecutor.runtimeException() );
                    }
                } ) );
    }
}
