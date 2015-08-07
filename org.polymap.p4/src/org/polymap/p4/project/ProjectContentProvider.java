/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.project.IMap;

/**
 * Provides the content of an {@link IMap}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectContentProvider
        implements IStructuredContentProvider {

    private static Log log = LogFactory.getLog( ProjectContentProvider.class );
    
    private IMap                map;

    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.map = (IMap)newInput;
    }

    @Override
    public Object[] getElements( Object inputElement ) {
        log.info( "Layers: " + map.layers );
        return map.layers.toArray();
    }

    @Override
    public void dispose() {
        this.map = null;
    }
    
}
