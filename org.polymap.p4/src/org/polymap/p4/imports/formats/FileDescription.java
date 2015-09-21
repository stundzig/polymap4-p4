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
package org.polymap.p4.imports.formats;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.Mandatory;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FileDescription extends Configurable {

    @Mandatory
    public Config2<FileDescription, String> name;
    
    public Config2<FileDescription, String> groupName;
    
    // in bytes
    public Config2<FileDescription, Long> size;
    
    public Config2<FileDescription, IFileFormat> format;

    public Config2<FileDescription, File> file;

    public Config2<FileDescription, FileDescription> parentFile;
    
    private List<FileDescription> containedFiles = new ArrayList<FileDescription>();

    
    public List<FileDescription> getContainedFiles() {
        return containedFiles;
    }
    
    public void addContainedFile(FileDescription fileDesc) {
        fileDesc.parentFile.set( this );
        containedFiles.add( fileDesc );
    }
}
