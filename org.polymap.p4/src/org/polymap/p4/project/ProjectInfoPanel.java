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

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.IMap;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectInfoPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( ProjectInfoPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "project" );
    
    public static final String          DASHBOARD_ID = "org.polymap.p4.project.project";
    
    @Scope(P4Plugin.Scope)
    private Context<IMap>               map;
    
    private Dashboard                   dashboard;

    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectMapPanel )
                .map( parent -> {
                    getSite().setTitle( "Settings" );
                    getSite().setPreferredWidth( 200 );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Settings: " + map.get().label.get() );
        getSite().setPreferredWidth( 300 );
        
        dashboard = new Dashboard( getSite(), DASHBOARD_ID );
        dashboard.addDashlet( new BasicInfoDashlet() );
        dashboard.createContents( parent );

        ContributionManager.instance().contributeFab( this );
    }

    
    /**
     * 
     */
    class BasicInfoDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( "Basics" );
            site.constraints.get().add( new PriorityConstraint( 100 ) );
            site.constraints.get().add( new MinWidthConstraint( 300, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            getSite().toolkit().createFlowText( parent, "..." );
        }        
    }
    
}
