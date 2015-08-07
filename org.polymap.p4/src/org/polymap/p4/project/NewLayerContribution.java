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

import static org.polymap.rhei.batik.contribution.ContributionSiteFilters.panelType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.project.operations.NewLayerOperation;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.DefaultContribution;
import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.tx.TxProvider;

import org.polymap.model2.runtime.UnitOfWork;
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
    private Context<ProjectUowProvider> uowProvider;

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
        String resId = P4Plugin.instance().localResolver.resourceIdentifier( res.get() );
        
        TxProvider<UnitOfWork>.Tx tx = uowProvider.get().newTx( site.getPanel() );
        
        NewLayerOperation op = new NewLayerOperation()
                .tx.put( tx )
                .map.put( map.get() )
                .label.put( res.get().getName() )
                .resourceIdentifier.put( resId );

        OperationSupport.instance().execute( op, true, false, ev2 -> UIThreadExecutor.async( () -> {
            if (ev2.getResult().isOK()) {
                PanelPath panelPath = site.getPanel().getSite().getPath();
                site.getContext().closePanel( panelPath.removeLast( 1 /*2*/ ) );
            }
            else {
                StatusDispatcher.handleError( "Unable to create new layer.", ev2.getResult().getException() );
            }
        }));
    }

}
