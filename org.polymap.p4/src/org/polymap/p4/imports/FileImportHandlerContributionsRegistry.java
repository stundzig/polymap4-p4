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

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.google.common.collect.Lists;

/**
 * Reads all registered FileImportHandler extensions.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class FileImportHandlerContributionsRegistry {

	private final String ID = "org.polymap.p4.imports.FileImportHandler";
	private List<FileImportHandler> handlers;

	private FileImportHandlerContributionsRegistry() {
		handlers = Lists.newArrayList();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(ID);
		if (point == null) {
			throw new IllegalStateException("extension point could not be found");
		}
		IConfigurationElement[] config = registry.getConfigurationElementsFor(ID);
		try {
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension");
				final Object o = e.createExecutableExtension("class");
				if (o instanceof FileImportHandler) {
					handlers.add((FileImportHandler) o);
				}
			}
			handlers = Collections.unmodifiableList(handlers);
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}
	}

	List<FileImportHandler> handlers() {
		return handlers;
	}

	private static FileImportHandlerContributionsRegistry instance;

	static synchronized FileImportHandlerContributionsRegistry INSTANCE() {
		if (instance == null) {
			instance = new FileImportHandlerContributionsRegistry();
		}
		return instance;
	}
}
