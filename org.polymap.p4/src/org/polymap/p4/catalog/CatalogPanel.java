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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
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
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CatalogPanel
        extends DefaultPanel {

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
                    getSite().setTitle( "Catalog" );
                    getSite().setPreferredWidth( 350 );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Catalog" );
        parent.setLayout( new FillLayout() );
        
        viewer = ((MdToolkit)getSite().toolkit()).createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION );
        viewer.setContentProvider( new MetadataContentProvider( LocalResolver.instance() ) );
        viewer.firstLineLabelProvider.set( new MetadataLabelProvider() );
        viewer.secondLineLabelProvider.set( new MetadataDescriptionProvider() );
        viewer.iconProvider.set( new CellLabelProvider() {
            @Override
            public void update( ViewerCell cell ) {
                if (cell.getElement() instanceof IMetadata) {
                    cell.setImage( P4Plugin.instance().imageForName( "resources/icons/archive.png" ) );
                }
                else if (cell.getElement() == MetadataContentProvider.LOADING) {
                    cell.setImage( BatikPlugin.instance().imageForName( "resources/icons/md/loading24.gif" ) );
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
        viewer.firstSecondaryActionProvider.set( new CatalogEntryDeleteActionProvider() );
        viewer.setInput( P4Plugin.instance().localCatalog );
        
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
