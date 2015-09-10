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

enum ArchiveFormats {
    // @formatter:off
    ZIP("zip", "application/zip"), 
    TAR("tar", "tape archiver"), 
    TAR_GZ("tar.gz", "tar zipped with GNU zip"), 
    GZIP("gzip", "GNU zip");
    // @formatter:on

    private String fileExtension, description;


    ArchiveFormats( String fileExtension, String description ) {
        this.fileExtension = fileExtension;
        this.description = description;
    }


    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}