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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.ui.MetadataContentProvider;
import org.polymap.core.catalog.ui.MetadataDescriptionProvider;
import org.polymap.core.catalog.ui.MetadataLabelProvider;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
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
        extends P4Panel {

    private static Log log = LogFactory.getLog( CatalogPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "catalog" );

    private MdListViewer                viewer;

    @Scope(P4Plugin.Scope)
    private Context<IResourceInfo>      res;
    
    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectMapPanel )
                .map( parent -> {
                    site().title.set( "" );
                    site().tooltip.set( "Data catalog" );
                    site().icon.set( P4Plugin.images().svgImage( "book-open.svg", P4Plugin.HEADER_ICON_CONFIG ) );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        site().title.set( "Catalog" );
        site().setSize( SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH, SIDE_PANEL_WIDTH );
        parent.setLayout( FormLayoutFactory.defaults().create() );
        
        viewer = ((MdToolkit)getSite().toolkit()).createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION, SWT.SINGLE );
        viewer.setContentProvider( new MetadataContentProvider( P4Plugin.localResolver() ) );
        viewer.firstLineLabelProvider.set( new MetadataLabelProvider() );
        viewer.secondLineLabelProvider.set( new MetadataDescriptionProvider() );
        viewer.iconProvider.set( new CellLabelProvider() {
            @Override
            public void update( ViewerCell cell ) {
                if (cell.getElement() instanceof IMetadata) {
                    cell.setImage( P4Plugin.images().svgImage( "layers.svg", NORMAL24 ) );
                }
                else if (cell.getElement() == MetadataContentProvider.LOADING) {
                    cell.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );
                }
                else {
                    cell.setImage( null );
                }
            }
        });
        viewer.addOpenListener( new IOpenListener() {
            @Override
            public void open( OpenEvent ev ) {
                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                    if (elm instanceof IResourceInfo) {
                        res.set( (IResourceInfo)elm );
                        getContext().openPanel( getSite().getPath(), ResourceInfoPanel.ID );                        
                    }
                    else {
                        viewer.toggleItemExpand( elm );
                    }
                });
            }
        } );
//        viewer.firstSecondaryActionProvider.set( new CatalogEntryDeleteActionProvider() );
        viewer.setInput( P4Plugin.localCatalog() );

        // fill the entiry space as items are expandable; scrollbar would not adopted otherwise
        viewer.getTree().setLayoutData( FormDataFactory.filled().create() );
        
//        viewer.getControl().setLayoutData( new ConstraintData( 
//                new MinWidthConstraint( 500, 1 ), new MinHeightConstraint( 3000, 1 ) ) );
        
//        Button okBtn = getSite().toolkit().createButton( parent, "Ok", SWT.PUSH );
//        //okBtn.setLayoutData( FormDataFactory.filled().top( 0, 100 ).clearBottom().create() );
//        okBtn.addSelectionListener( new SelectionAdapter() {
//            @Override
//            public void widgetSelected( SelectionEvent ev ) {
//                viewer.expandAll();
////                getContext().closePanel( getSite().getPath() );
//            }
//        });
    }

}
