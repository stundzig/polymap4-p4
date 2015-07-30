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
package org.polymap.p4;

import static org.polymap.rhei.batik.contribution.ContributionSiteFilters.panelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.IMap;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.DefaultContribution;
import org.polymap.rhei.batik.contribution.IContributionSite;

import org.polymap.p4.project.ProjectPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class TestProjectContribution
        extends DefaultContribution {

    private static Log log = LogFactory.getLog( TestProjectContribution.class );
    
    @Scope("org.polymap.p4")
    private Context<IMap>               map;
    
    
    public TestProjectContribution() {
        super( panelType( ProjectPanel.class ), Place.Toolbar, Place.FAB );
    }

    
    @Override
    protected void execute( IContributionSite site ) throws Exception {
        log.info( "..." );
    }

}
