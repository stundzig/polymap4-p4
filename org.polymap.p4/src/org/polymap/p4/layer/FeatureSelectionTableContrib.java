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
package org.polymap.p4.layer;

import static org.polymap.core.runtime.UIThreadExecutor.async;
import static org.polymap.core.runtime.event.TypeEventFilter.ifType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geotools.data.FeatureStore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.project.ProjectNode.ProjectNodeCommittedEvent;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.Memento;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.contribution.IToolbarContribution;
import org.polymap.rhei.batik.toolkit.ItemContainer;
import org.polymap.rhei.batik.toolkit.RadioItem;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionTableContrib
        implements IToolbarContribution {

    private static Log log = LogFactory.getLog( FeatureSelectionTableContrib.class );

    /** Inbound */
    @Mandatory
    @Scope( P4Plugin.Scope )
    private Context<IMap>               map;

    /** Outbound. See {@link P4Panel#featureSelection}. */
    @Scope( P4Plugin.Scope )
    private Context<FeatureSelection>   featureSelection;
    
    private IContributionSite           site;

    private MdToolbar2                  toolbar;
    
    private Map<String,RadioItem>       createdLayerIds = new HashMap();
    

    @Override
    @SuppressWarnings( "hiding" )
    public void fillToolbar( IContributionSite site, MdToolbar2 toolbar ) {
        if (site.panel() instanceof ProjectMapPanel 
                && site.tagsContain( ProjectMapPanel.BOTTOM_TOOLBAR_TAG )) {
            
            this.site = site;
            this.toolbar = toolbar;
            for (ILayer layer : map.get().layers) {
                createLayerItem( toolbar, layer );
            }
            
            EventManager.instance().subscribe( this, ifType( ProjectNodeCommittedEvent.class, 
                    ev -> ev.getSource() instanceof ILayer && map.get().containsLayer( (ILayer)ev.getSource() ) ) );
        }
    }

    
    /**
     * Handle layer create/remove/update events.
     * <p/>
     * XXX Correctness depends on the delay :( If to short then this handler is
     * called twice and creates two entries for the same layer. createdLayerIds does
     * not prevent this as it is updated asynchronously.
     *
     * @param evs
     */
    @EventHandler( display=true, delay=100 )
    protected void mapLayerChanged( List<ProjectNodeCommittedEvent> evs ) {
        if (toolbar.getControl().isDisposed()) {
            EventManager.instance().unsubscribe( FeatureSelectionTableContrib.this );
        }
        else {
            Set<String> handledLayerIds = new HashSet();
            for (ProjectNodeCommittedEvent ev : evs) {
                ILayer layer = (ILayer)ev.getSource();
                if (!handledLayerIds.contains( layer.id() )) {
                    handledLayerIds.add( layer.id() );
                    
                    // newly created
                    if (!createdLayerIds.containsKey( layer.id() )) {
                        createLayerItem( toolbar, layer );                    
                    }
                    // removed
                    else if (layer.status().equals( EntityStatus.REMOVED )) {
                        log.info( "XXX not yet implemented!" );
                        // RadioItem item = createdLayerIds.remove( layer.id() );
                        // item.dispose();
                    }
                    // modified
                    else if (createdLayerIds.containsKey( layer.id() )) {
                        RadioItem item = createdLayerIds.get( layer.id() );
                        item.text.set( label( layer ) );
                    }
                }
            }
        }
    }

    
    protected String label( ILayer layer ) {
        return StringUtils.abbreviate( layer.label.get(), 17 );
    }
    
    
    protected void createLayerItem( ItemContainer group, ILayer layer ) {
        Memento memento = site.panel().site().memento();
        Optional<String> selectedLayerId = memento.getOrCreateChild( getClass().getName() ).optString( "selectedLayerId" );
        
        FeatureSelection.forLayer( layer ).waitForFs( 
                fs -> async( () -> {
                    RadioItem item = new RadioItem( group );
                    item.text.put( label( layer ) );
                    item.tooltip.put( "Show contents of " + layer.label.get() );
                    item.icon.put( P4Plugin.images().svgImage( "layers.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
                    AtomicBoolean wasVisible = new AtomicBoolean();
                    item.onSelected.put( ev -> {
                        log.info( "fs=" + fs );
                        createTableView( layer, fs );
                        saveState( layer, true );

                        wasVisible.set( layer.userSettings.get().visible.get() );
                        layer.userSettings.get().visible.set( true );
                    });
                    item.onUnselected.put( ev -> {
                        ((ProjectMapPanel)site.panel()).closeButtomView();
                        layer.userSettings.get().visible.set( wasVisible.get() );
                        saveState( layer, false );
                    });
                    
                    createdLayerIds.put( layer.id(), item );
                    
                    // memento select
                    item.selected.set( selectedLayerId.orElse( "$%&" ).equals( layer.id() ) );
                }),
                e -> {
                    log.info( "No FeatureSelection for: " + layer.label.get() + " (" + e.getMessage() + ")" );
                });
    }
    
    
    protected void createTableView( ILayer layer, FeatureStore fs ) {
        // create bottom view
        ((ProjectMapPanel)site.panel()).updateButtomView( parent -> {
            
            site.toolkit().createFlowText( parent, " Loading " + layer.label.get() + "..." );
            parent.layout();
            
            new UIJob( "Loading data" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    async( () -> {
                        UIUtils.disposeChildren( parent );
                        FeatureSelection layerFeatureSelection = FeatureSelection.forLayer( layer );
                        featureSelection.set( layerFeatureSelection );
                        
                        new FeatureSelectionTable( parent, layerFeatureSelection, site.panel() );
                        parent.layout();
                    });        
                }
            }.scheduleWithUIUpdate();  // UI callback?
        });
    }
    
    
    protected void saveState( ILayer layer, boolean visible ) {
        IPanel panel = site.panel();
        Memento memento = panel.site().memento();
        Memento childMem = memento.getOrCreateChild( getClass().getName() );
        childMem.putString( "selectedLayerId", visible ? (String)layer.id() : "__unselected__" );
        memento.save();
    }

}
