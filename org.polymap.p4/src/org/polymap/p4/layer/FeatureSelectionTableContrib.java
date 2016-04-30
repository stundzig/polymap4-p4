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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.geotools.data.FeatureStore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.UIJob;
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
    

    @Override
    public void fillToolbar( @SuppressWarnings( "hiding" ) IContributionSite site, MdToolbar2 toolbar ) {
        if (site.panel() instanceof ProjectMapPanel 
                && site.tagsContain( ProjectMapPanel.BOTTOM_TOOLBAR_TAG )) {
            
            this.site = site;
            for (ILayer layer : map.get().layers) {
                createLayerItem( toolbar, layer );
            }
        }
    }


    protected void createLayerItem( ItemContainer group, ILayer layer ) {
        Memento memento = site.panel().site().memento();
        Optional<String> selectedLayerId = memento.getOrCreateChild( getClass().getName() ).optString( "selectedLayerId" );
        
        FeatureSelection.forLayer( layer ).waitForFs( 
                fs -> async( () -> {
                    RadioItem item = new RadioItem( group );
                    item.text.put( StringUtils.abbreviate( layer.label.get(), 15 ) );
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
            }.schedule();  // UI callback?
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
