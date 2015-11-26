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

import org.geotools.feature.FeatureCollection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.feature.FeaturesProducer;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.Pipeline;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.IContributionFactory;
import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.toolkit.md.MdActionItem;
import org.polymap.rhei.batik.toolkit.md.MdItemContainer;
import org.polymap.rhei.batik.toolkit.md.MdToolbar2;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.P4PipelineIncubator;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayersFeatureTableContribution
        implements IContributionFactory {

    private static Log log = LogFactory.getLog( LayersFeatureTableContribution.class );

    @Mandatory
    @Scope( P4Plugin.Scope )
    private Context<IMap>               map;

    
    @Override
    public void fillToolbar( IContributionSite site, Object toolbar ) {
        if (site.panel() instanceof ProjectMapPanel) {
            MdToolbar2 tb = (MdToolbar2)toolbar;
//            MdGroupItem group = new MdGroupItem( tb, "layers" );
            
            for (ILayer layer : map.get().layers) {
                createLayerItem( tb, layer, site );
            }
        }
    }

    
    protected void createLayerItem( MdItemContainer group, ILayer layer, IContributionSite site ) {
        UIJob job = new UIJob( "Connect layer" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                // data source for layer
                DataSourceDescription dsd = P4Plugin.localResolver().connectLayer( layer, monitor )
                        .orElseThrow( () -> new Exception( "No service for layer: " + layer.label.get() ) );

                // create pipeline
                Pipeline pipeline = P4PipelineIncubator.forLayer( layer ).newPipeline( FeaturesProducer.class, dsd, null );

                // FeatureSource?
                if (pipeline != null && pipeline.length() > 0) {
                    log.info( "Feature pipeline created for: " + layer.label.get() );
                    MdActionItem item = new MdActionItem( group );
                    item.text.put( StringUtils.abbreviate( layer.label.get(), 10 ) );
                    item.tooltip.put( "Attributes table: " + layer.label.get() );
                    item.icon.put( P4Plugin.images().svgImage( "table.svg", P4Plugin.TOOLBAR_ICON_CONFIG ) );
                    item.action.put( ev -> {
                        createTableView( layer, pipeline, site );                        
                    });
                }
            }
        };
        job.schedule();  // UI callback?        
    }
    
    
    protected void createTableView( ILayer layer, Pipeline pipeline, IContributionSite site ) {
        // create bottom view
        ((ProjectMapPanel)site.panel()).addButtomView( parent -> {
            
            site.toolkit().createFlowText( parent, " Loading " + layer.label.get() + "..." );
            parent.layout();
            
            new UIJob( "Loading data" ) {
                @Override
                protected void runWithException( IProgressMonitor monitor ) throws Exception {
                    log.info( "Creating viewer for: " + layer.label.get() );
                    PipelineFeatureSource fs = new PipelineFeatureSource( pipeline );
                    FeatureCollection features = fs.getFeatures();
                    
                    UIThreadExecutor.async( () -> {
                        UIUtils.disposeChildren( parent );
                        new FeatureTablePanelPart( parent, features );
                        parent.layout();
                    });        
                }
            }.schedule();  // UI callback?
        });
    }

    
    @Override
    public void fillFab( IContributionSite site ) {
    }
    
}
