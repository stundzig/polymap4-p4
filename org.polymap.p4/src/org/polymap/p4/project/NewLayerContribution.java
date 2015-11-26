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

import static org.polymap.core.runtime.UIThreadExecutor.asyncFast;
import static org.polymap.rhei.batik.contribution.ContributionSiteFilters.panelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.operations.NewLayerOperation;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.DefaultContribution;
import org.polymap.rhei.batik.contribution.IContributionSite;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.ResourceInfoPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewLayerContribution
        extends DefaultContribution {

    private static Log log = LogFactory.getLog( NewLayerContribution.class );
    
    @Mandatory
    @Scope(P4Plugin.Scope)
    private Context<IMap>               map;
    
    @Mandatory
    @Scope(P4Plugin.Scope)
    private Context<IResourceInfo>      res;

    
    public NewLayerContribution() {
        super( panelType( ResourceInfoPanel.class ), Place.FAB );
    }

    
    @Override
    protected void execute( IContributionSite site ) throws Exception {
        String resId = P4Plugin.localResolver().resourceIdentifier( res.get() );
        
        NewLayerOperation op = new NewLayerOperation()
                .uow.put( ProjectRepository.unitOfWork().newUnitOfWork() )
                .map.put( map.get() )
                .label.put( res.get().getName() )
                .resourceIdentifier.put( resId );

        OperationSupport.instance().execute2( op, true, false, ev2 -> asyncFast( () -> {
            if (ev2.getResult().isOK()) {
                PanelPath parentPath = site.panelSite().path().removeLast( 1 );
                BatikApplication.instance().getContext().closePanel( parentPath );

//                // close panel and parent, assuming that projct map is root
//                site.getContext().openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) );
            }
            else {
                StatusDispatcher.handleError( "Unable to create new layer.", ev2.getResult().getException() );
            }
        }));
    }

}
