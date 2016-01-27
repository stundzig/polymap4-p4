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

import static org.polymap.core.runtime.UIThreadExecutor.asyncFast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.operations.DeleteLayerOperation;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerInfoPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LayerInfoPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layer" );
    
    public static final String          DASHBOARD_ID = "org.polymap.p4.project.layer";
    
    @Scope(P4Plugin.Scope)
    private Context<ILayer>             layer;
    
    private Dashboard                   dashboard;

    private MdToolkit                   tk;

    
    @Override
    public boolean wantsToBeShown() {
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Layer: " + layer.get().label.get() );
        getSite().setPreferredWidth( 350 );
        tk = (MdToolkit)site().toolkit();
        
        dashboard = new Dashboard( getSite(), DASHBOARD_ID );
        dashboard.addDashlet( new BasicInfoDashlet() );
        dashboard.addDashlet( new DeleteLayerDashlet() );
        dashboard.createContents( parent );

        ContributionManager.instance().contributeTo( this, this );
    }

    
    /**
     * 
     */
    class BasicInfoDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( layer.get().label.get() );
            site.constraints.get().add( new PriorityConstraint( 100 ) );
            site.constraints.get().add( new MinWidthConstraint( 350, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            getSite().toolkit().createFlowText( parent, "..." );
        }        
    }
    

    /**
     * 
     */
    class DeleteLayerDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( "Danger zone" );
            site.constraints.get().add( new PriorityConstraint( 0 ) );
            site.constraints.get().add( new MinWidthConstraint( 350, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            Button deleteBtn = tk.createButton( parent, "Delete this layer", SWT.PUSH );
            deleteBtn.setToolTipText( "Delete this layer." );
            deleteBtn.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent e ) {
//                    MdSnackbar snackbar = tk.createSnackbar();
//                    snackbar.showIssue( MessageType.WARNING, "We are going to delete the project." );
                    
                    DeleteLayerOperation op = new DeleteLayerOperation();
                    op.uow.set( ProjectRepository.unitOfWork().newUnitOfWork() );
                    op.layer.set( layer.get() );

                    OperationSupport.instance().execute2( op, true, false, ev2 -> asyncFast( () -> {
                        if (ev2.getResult().isOK()) {
                            PanelPath parentPath = site().path().removeLast( 1 );
                            BatikApplication.instance().getContext().closePanel( parentPath );

//                            // close panel and parent, assuming that projct map is root
//                            getContext().openPanel( PanelPath.ROOT, new PanelIdentifier( "start" ) );
                        }
                        else {
                            StatusDispatcher.handleError( "Unable to delete project.", ev2.getResult().getException() );
                        }
                    }));
                }
            });
        }        
    }
    
}
