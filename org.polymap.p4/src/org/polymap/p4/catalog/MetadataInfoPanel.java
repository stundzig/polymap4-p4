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
package org.polymap.p4.catalog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerCell;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.ui.MetadataDescriptionProvider;
import org.polymap.core.catalog.ui.MetadataLabelProvider;
import org.polymap.core.project.IMap;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.contribution.ContributionManager;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.toolkit.MinHeightConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MetadataInfoPanel
        extends P4Panel {

    private static Log log = LogFactory.getLog( MetadataInfoPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "metadata" );
    
    public static final String          DASHBOARD_ID = "org.polymap.p4.catalog.metadata";
    
    public static final int             TEXTFIELD_HEIGHT = 72;
    
    /** Inbound: */
    @Scope( P4Plugin.Scope )
    private Context<IMap>               map;
    
    /** Inbound: */
    @Scope( P4Plugin.Scope )
    private Context<IMetadata>          md;

    /** Outbound: */
    @Scope( P4Plugin.Scope )
    private Context<IResourceInfo>      selectedResource;

    private Dashboard                   dashboard;

    

    @Override
    public void createContents( Composite parent ) {
        site().title.set( md.get().getTitle() );
        site().setSize( SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH*5/4, SIDE_PANEL_WIDTH*3/2 );
        ContributionManager.instance().contributeTo( this, this );
        
        dashboard = new Dashboard( getSite(), DASHBOARD_ID );
        dashboard.addDashlet( new BasicFormDashlet() );
        dashboard.addDashlet( new ResourcesDashlet() );
        dashboard.createContents( parent );
        ContributionManager.instance().contributeTo( dashboard, this );
    }


    /**
     * 
     */
    protected class ResourcesDashlet
            extends DefaultDashlet 
            implements IOpenListener {

        private MdListViewer            viewer;

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( "Data sets" );
            site.constraints.get().add( new PriorityConstraint( 0 ) );
            site.constraints.get().add( new MinWidthConstraint( 300, 1 ) );
            site.constraints.get().add( new MinHeightConstraint( 400, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            parent.setLayout( FormLayoutFactory.defaults().create() );
            viewer = tk().createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION, SWT.SINGLE );
            viewer.setContentProvider( new P4MetadataContentProvider( P4Plugin.localResolver() ) );
            viewer.firstLineLabelProvider.set( new MetadataLabelProvider() );
            viewer.secondLineLabelProvider.set( new MetadataDescriptionProvider() );
            viewer.iconProvider.set( new MetadataIconProvider() );
            viewer.firstSecondaryActionProvider.set( new CreateLayerAction() );
            viewer.addOpenListener( this );
            viewer.setAutoExpandLevel( 3 );
            viewer.setInput( md.get() );
            
            viewer.getTree().setLayoutData( FormDataFactory.filled().height( 400 ).create() );
            viewer.expandToLevel( 2 );
        }

        @Override
        public void open( OpenEvent ev ) {
            SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                if (elm instanceof IResourceInfo) {
                    selectedResource.set( (IResourceInfo)elm );
                    getContext().openPanel( site().path(), ResourceInfoPanel.ID );                        
                }
                else {
                    viewer.toggleItemExpand( elm );
                }
            });
        }
    }
    

    /**
     * 
     */
    protected static class CreateLayerAction
            extends ActionProvider {

        @Override
        public void update( ViewerCell cell ) {
            Object elm = cell.getElement();
            if (elm instanceof IResourceInfo) {
                cell.setImage( P4Plugin.images().svgImage( "plus-circle-outline.svg", SvgImageRegistryHelper.OK24 ) );
            }
        }

        @Override
        public void perform( MdListViewer viewer, Object elm ) {
            throw new RuntimeException( "working on it..." );
        }    
    }

    

    /**
     * Basic fields of the {@link IMetadata}. 
     */
    protected class BasicFormDashlet
            extends DefaultDashlet {

        @Override
        public void init( DashletSite site ) {
            super.init( site );
            site.title.set( md.get().getTitle() );
            site.constraints.get().add( new PriorityConstraint( 100 ) );
            site.constraints.get().add( new MinWidthConstraint( 300, 1 ) );
        }

        @Override
        public void createContents( Composite parent ) {
            parent.setLayout( new FillLayout() );
            BatikFormContainer form = new BatikFormContainer( new Form() );
            form.createContents( parent );
            form.setEnabled( false );
        }

        /**
         * 
         */
        class Form
                extends DefaultFormPage {
            
            @Override
            public void createFormContents( IFormPageSite site ) {
                super.createFormContents( site );
                
                Composite body = site.getPageBody();
                body.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).create() );
                
//                site.newFormField( new PlainValuePropertyAdapter( "title", md.get().getTitle() ) ).create();

                site.newFormField( new PlainValuePropertyAdapter( "description", md.get().getDescription() ) )
                        .field.put( new TextFormField() )
                        .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, TEXTFIELD_HEIGHT ) );
                
                site.newFormField( new PlainValuePropertyAdapter( "keywords", 
                        Joiner.on( ", " ).skipNulls().join( md.get().getKeywords() ) ) ).create();
                
                site.newFormField( new PlainValuePropertyAdapter( "modified", md.get().getModified() ) )
                        .field.put( new StringFormField() )
                        .validator.put( new DateValidator() )
                        .create();

                site.newFormField( new PlainValuePropertyAdapter( "publisher", md.get().getPublisher() ) )
                        .field.put( new TextFormField() )
                        .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, TEXTFIELD_HEIGHT ) );
        
                site.newFormField( new PlainValuePropertyAdapter( "rights", md.get().getRights() ) )
                        .field.put( new TextFormField() )
                        .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, TEXTFIELD_HEIGHT ) );
            }
        }
        
    }
    
}
