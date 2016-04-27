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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.mapeditor.MapViewer;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ILayer.LayerUserSettings;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Provides the content of an {@link IMap}.
 * <p/>
 * This also tracks the state of the layers and triggers {@link MapViewer#refresh()}
 * on {@link ProjectNodeCommittedEvent} and {@link PropertyChangeEvent}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectContentProvider
        implements IStructuredContentProvider {

    private static Log log = LogFactory.getLog( ProjectContentProvider.class );
    
    private IMap                map;

    private MapViewer           viewer;

    private CommitListener      commitListener;

    private PropertyListener    propertyListener;

    
    @Override
    public void inputChanged( @SuppressWarnings("hiding") Viewer viewer, Object oldInput, Object newInput ) {
        this.map = (IMap)newInput;
        this.viewer = (MapViewer)viewer;
        
        // commit listener
        EventManager.instance().subscribe( commitListener = new CommitListener(), ev -> 
                ev instanceof ProjectNodeCommittedEvent &&
                ev.getSource() instanceof IMap &&
                ((IMap)ev.getSource()).id() == map.id() );
                // XXX check if structural change or just label changed

        // listen to LayerUserSettings#visible
        EventManager.instance().subscribe( propertyListener = new PropertyListener(), ev -> {
            if (ev instanceof PropertyChangeEvent) {
                if (ev.getSource() instanceof LayerUserSettings) {
                    LayerUserSettings userSettings = (LayerUserSettings)ev.getSource();
                    ILayer eventLayer = userSettings.layer.get();
                    IMap eventMap = eventLayer.parentMap.get();
                    return eventMap.id().equals( map.id() );
                }
            }
            return false;
        });
    }


    /**
     * 
     */
    class CommitListener {
        @EventHandler( display=true, delay=100 )
        protected void onCommit( List<ProjectNodeCommittedEvent> evs ) {
            viewer.refresh();
        }
    }


    /**
     * 
     */
    class PropertyListener {
        @EventHandler( display=true, delay=100 )
        protected void onPropertyChange( List<PropertyChangeEvent> evs ) {
            // FIXME check if layer was just created and onCommit() did it already
            for (PropertyChangeEvent ev : evs) {
                LayerUserSettings userSettings = (LayerUserSettings)ev.getSource();
                ILayer eventLayer = userSettings.layer.get();
                ILayer mapLayer = map.belongsTo().entity( eventLayer );
                viewer.refresh( mapLayer );
            }
        }
    }

    
    @Override
    public Object[] getElements( Object inputElement ) {
        Object[] result = map.layers.stream().filter( l -> l.userSettings.get().visible.get() ).toArray();
        log.info( "Layers: " + Arrays.stream( result ).map( l -> ((ILayer)l).label.get() ).collect( Collectors.toList() ) );
        return result;
    }

    
    @Override
    public void dispose() {
        log.info( "..." );
        this.map = null;
        EventManager.instance().unsubscribe( commitListener );
        EventManager.instance().unsubscribe( propertyListener );
    }
    
}
