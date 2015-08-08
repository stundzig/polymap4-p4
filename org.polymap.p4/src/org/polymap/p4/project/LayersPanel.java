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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.project.IMap;
import org.polymap.core.project.ui.ProjectNodeContentProvider;
import org.polymap.core.project.ui.ProjectNodeLabelProvider;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.batik.tx.TxProvider;
import org.polymap.rhei.batik.tx.TxProvider.Propagation;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayersPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LayersPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layers" );

    @Mandatory
    @Scope(P4Plugin.Scope)
    private Context<ProjectUowProvider> uowProvider;
    
    private TxProvider<UnitOfWork>.Tx   uow;
    
    @Mandatory
    @Scope(P4Plugin.Scope)
    protected Context<IMap>             map;
    
    private MdListViewer                viewer;

    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectPanel )
                .map( parent -> {
                    getSite().setTitle( "Layers" );
                    getSite().setPreferredWidth( 200 );
                    uow = uowProvider.get().newTx( this ).start( Propagation.MANDATORY );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "Layers" );
        parent.setLayout( new FillLayout() );
        
        viewer = ((MdToolkit)getSite().toolkit()).createListViewer( parent, SWT.FULL_SELECTION );
        viewer.setContentProvider( new ProjectNodeContentProvider() );
        viewer.firstLineLabelProvider.set( new ProjectNodeLabelProvider() );
        
//        viewer.secondLineLabelProvider.set( new MetadataDescriptionProvider() );
//        viewer.iconProvider.set( new CellLabelProvider() {
//            @Override
//            public void update( ViewerCell cell ) {
//                if (cell.getElement() instanceof IMetadata) {
//                    cell.setImage( P4Plugin.instance().imageForName( "resources/icons/archive.png" ) );
//                }
//                else if (cell.getElement() == MetadataContentProvider.LOADING) {
//                    cell.setImage( BatikPlugin.instance().imageForName( "resources/icons/md/loading24.gif" ) );
//                }
//                else {
//                    cell.setImage( null );
//                }
//            }
//        });
        
//        viewer.addOpenListener( new IOpenListener() {
//            @Override
//            public void open( OpenEvent ev ) {
//                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
//                    if (elm instanceof IResourceInfo) {
//                        res.set( (IResourceInfo)elm );
//                        getContext().openPanel( getSite().getPath(), ResourceInfoPanel.ID );                        
//                    }
//                    else {
//                        viewer.toggleItemExpand( elm );
//                    }
//                });
//            }
//        } );
        viewer.setInput( map.get() );
    }

}
