/* 
 * polymap.org
 * Copyright (C) 2015-2016, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.ui.MetadataDescriptionProvider;
import org.polymap.core.catalog.ui.MetadataLabelProvider;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.ActionText;
import org.polymap.rhei.batik.toolkit.ClearTextAction;
import org.polymap.rhei.batik.toolkit.TextActionItem;
import org.polymap.rhei.batik.toolkit.TextActionItem.Type;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Panel;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogPanel
        extends P4Panel 
        implements IOpenListener {

    private static Log log = LogFactory.getLog( CatalogPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "catalog" );

    private MdListViewer                viewer;

    @Scope( P4Plugin.Scope )
    private Context<IResourceInfo>      selectedResource;
    
    @Scope( P4Plugin.Scope )
    private Context<IMetadata>          selectedMetadata;
    
    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectMapPanel )
                .map( parent -> {
                    site().title.set( "" );
                    site().tooltip.set( "Data catalog" );
                    site().icon.set( P4Plugin.images().svgImage( "book-open-page-variant.svg", P4Plugin.HEADER_ICON_CONFIG ) );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( "Catalog" );
        site().setSize( SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH );
        parent.setLayout( FormLayoutFactory.defaults().margins( 0, 5 ).create() );
        
        // tree/list viewer
        viewer = ((MdToolkit)getSite().toolkit()).createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION, SWT.SINGLE );
        viewer.setContentProvider( new P4MetadataContentProvider( P4Plugin.allResolver() ) );
        viewer.firstLineLabelProvider.set( new MetadataLabelProvider() );
        viewer.secondLineLabelProvider.set( new MetadataDescriptionProvider() );
        viewer.iconProvider.set( new MetadataIconProvider() );
        viewer.addOpenListener( this );
        viewer.setInput( P4Plugin.catalogs() );

        // search field
        ActionText search = tk().createActionText( parent, "" );
        new TextActionItem( search, Type.DEFAULT )
                .action.put( ev -> doSearch( search.getText().getText() ) )
                .text.put( "Search..." )
                .tooltip.put( "Fulltext search. Use * as wildcard.<br/>&lt;ENTER&gt; starts the search." )
                .icon.put( P4Plugin.images().svgImage( "magnify.svg", SvgImageRegistryHelper.DISABLED12 ) );
        new ClearTextAction( search );
        
        // layout
        search.getControl().setLayoutData( FormDataFactory.filled().noBottom().create() );
        // fill the entiry space as items are expandable; scrollbar would not adopted otherwise
        viewer.getTree().setLayoutData( FormDataFactory.filled().top( search.getControl() ).create() );
    }

    
    protected void doSearch( String query ) {
        log.info( "doSearch(): ..." );
        
        P4MetadataContentProvider cp = (P4MetadataContentProvider)viewer.getContentProvider();
        cp.catalogQuery.set( query );
        cp.flush();
        
        // otherwise preserveSelection() fails because of no getParent()
        viewer.setSelection( new StructuredSelection() );
        viewer.setInput( P4Plugin.catalogs() );
        viewer.expandToLevel( 2 );
    }

    
    @Override
    public void open( OpenEvent ev ) {
        SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
            if (elm instanceof IMetadata) {
                selectedMetadata.set( (IMetadata)elm );
                getContext().openPanel( getSite().getPath(), MetadataInfoPanel.ID );                        
            }
            else if (elm instanceof IResourceInfo) {
                selectedResource.set( (IResourceInfo)elm );
                getContext().openPanel( getSite().getPath(), ResourceInfoPanel.ID );                        
            }
            else {
                viewer.toggleItemExpand( elm );
            }
        });
    }

}
