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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.IFileFormat;
import org.polymap.p4.imports.formats.ShapeFileFormats;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FileGroupHelper {

    public static Map<String,List<File>> groupFilesByName( List<File> fs ) {
        Map<String,List<File>> files = new HashMap<String,List<File>>();
        for (File f : fs) {
            int index = f.getName().lastIndexOf( "." + ShapeFileFormats.SHP_XML.getFileExtension() );
            if (index == -1) {
                index = f.getName().lastIndexOf( "." );
            }
            if (index > 0) {
                String fName = f.getName().substring( 0, index );
                List<File> gFs = files.get( fName );
                if (gFs == null) {
                    gFs = new ArrayList<File>();
                }
                gFs.add( f );
                files.put( fName, gFs );
            }
        }
        return files;
    }


    public static void fillFilesList( List<FileDescription> files, String rootFileName, long fileSize, List<File> read ) {
        Map<String,List<File>> grouped = FileGroupHelper.groupFilesByName( read );
        String localRootFileName = rootFileName;
        if (rootFileName != null) {
            if (files.stream().anyMatch( f -> rootFileName.equals( f.name.get() ) )) {
                localRootFileName = "_duplicated";
            }
        }
        for (Map.Entry<String,List<File>> entry : grouped.entrySet()) {
            FileDescription root = new FileDescription().groupName.put( entry.getKey() );
            if (localRootFileName != null) {
                root.name.put( localRootFileName ).format.put( IFileFormat.getKnownFileFormat( localRootFileName ) );
            }
            if (fileSize != -1) {
                root.size.set( fileSize );
            }
            for (File file : entry.getValue()) {
                FileDescription fileDesc = new FileDescription().name.put( file.getName() ).file.put( file ).size
                        .put( file.length() ).format.put( IFileFormat.getKnownFileFormat( file.getName() ) );
                root.addContainedFile( fileDesc );
            }
            files.add( root );
        }
    }
}
