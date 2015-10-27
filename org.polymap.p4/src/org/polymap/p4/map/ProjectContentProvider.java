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
package org.polymap.p4.map;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Provides the content of an {@link IMap}.
 * <p/>
 * Triggers {@link MapViewer#refresh()} on {@link ProjectNodeCommittedEvent}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectContentProvider
        implements IStructuredContentProvider {

    private static Log log = LogFactory.getLog( ProjectContentProvider.class );
    
    private IMap                map;

    private MapViewer           viewer;

    
    @Override
    public void inputChanged( @SuppressWarnings("hiding") Viewer viewer, Object oldInput, Object newInput ) {
        this.map = (IMap)newInput;
        this.viewer = (MapViewer)viewer;
        
        EventManager.instance().subscribe( this, ev -> 
                ev instanceof ProjectNodeCommittedEvent &&
                ev.getSource() instanceof IMap &&
                ((IMap)ev.getSource()).id() == map.id() );
                // XXX check if structural change or just label changed
    }

    
    @EventHandler( display=true, delay=100 )
    protected void mapLayersChanged( List<ProjectNodeCommittedEvent> evs ) {
        log.info( "mapLayersChanged: " + evs );
        viewer.refresh();
    }
    
    
    @Override
    public Object[] getElements( Object inputElement ) {
        log.info( "Layers: " + map.layers );
        return map.layers.toArray();
    }

    
    @Override
    public void dispose() {
        log.info( "..." );
        this.map = null;
        EventManager.instance().unsubscribe( this );
    }
    
}
