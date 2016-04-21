/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.style;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.batik.IPanelSite.PanelStatus;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelChangeEvent.EventType;
import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.contribution.IToolbarContribution;
import org.polymap.rhei.batik.toolkit.ToggleItem;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.layer.FeatureSelectionTable;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * Contributes a button that opens {@link LayerStylePanel} to the toolbar of
 * {@link FeatureSelectionTable}.
 *
 * @author Falko Bräutigam
 */
public class LayerStyleContrib
        implements IToolbarContribution {

    private static Log log = LogFactory.getLog( LayerStyleContrib.class );

    private ToggleItem                      item;
    
    private Optional<LayerStylePanel>       childPanel = Optional.empty();

    
    @Override
    public void fillToolbar( IContributionSite site, MdToolbar2 toolbar ) {
        if (site.panel() instanceof ProjectMapPanel 
                && site.tagsContain( FeatureSelectionTable.TOOLBAR_TAG )) {
            assert item == null;
            
            item = new ToggleItem( toolbar );
            item.text.set( "" );
            item.icon.set( P4Plugin.images().svgImage( "brush.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
            item.tooltip.set( "Edit geometry styling" );
            item.onSelected.set( ev -> {
                assert !childPanel.isPresent();
                childPanel = site.context().openPanel( site.panelSite().path(), LayerStylePanel.ID );
                
                // FIXME does not work
                site.context().addListener( LayerStyleContrib.this, ev2 -> 
                        ev2.getPanel() == childPanel.orElse( null ) && ev2.getType().isOnOf( EventType.LIFECYCLE ) );
            });
            item.onUnselected.set( ev -> {
                if (childPanel.isPresent() && !childPanel.get().isDisposed()) {
                    site.context().closePanel( childPanel.get().site().path() );
                    childPanel = Optional.empty();
                    site.context().removeListener( LayerStyleContrib.this );
                }
            });
        }
    }

    
    @EventHandler( display=true )
    protected void childPanelClosed( PanelChangeEvent ev ) {
        log.info( "Child panel lifecycle: " + ev.getPanel().site().panelStatus() );
        if (item != null /*&& !item.isDisposed()*/
                && ev.getPanel().site().panelStatus() == PanelStatus.CREATED) {
            item.selected.set( false );
        }
    }
    
}