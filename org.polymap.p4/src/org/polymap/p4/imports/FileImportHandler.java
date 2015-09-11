/*
 * polymap.org and individual contributors as indicated by the @authors tag.
 * Copyright (C) 2009-2015 
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

import org.eclipse.swt.widgets.Composite;

/**
 * To support different types of import files, these interface could be
 * implemented and the implementations could be provided via extension point.
 *
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public interface FileImportHandler {

	/**
	 * @param file
	 * @return true, if the handler could work with these type of file
	 */
	boolean canHandle(File file);

	/**
	 * shows a panel with additional file data, validations or more options
	 * @param file
	 * @param parent
	 */
	void handle(File file, Composite parent);
	
	/**
	 * @return true if the file could be imported sucessfully
	 */
	boolean isValid();
}
