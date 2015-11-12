/*
 * polymap.org Copyright (C) @year@ individual contributors as indicated by
 * the @authors tag. All rights reserved.
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
package org.polymap.p4.data.imports.refine.excel;

import java.io.File;

/**
 * A container to transport selected excel sheet and file to the next importer.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 *
 */
public class Sheet {

    private final int    index;

    private final File   file;

    private final String name;


    public Sheet( File file, int index, String name ) {
        this.name = name;
        this.index = index;
        this.file = file;
    }


    public File file() {
        return file;
    }

    public int index() {
        return index;
    }


    public String name() {
        return name;
    }
}
