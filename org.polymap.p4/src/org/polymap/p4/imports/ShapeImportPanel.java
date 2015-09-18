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
package org.polymap.p4.imports;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.labels.MessageCellLabelProvider;
import org.polymap.p4.imports.labels.ShapeImportCellLabelProvider;
import org.polymap.p4.imports.labels.ShapeImportImageLabelProvider;
import org.polymap.p4.imports.utils.InitFileListHelper;
import org.polymap.p4.imports.utils.IssueReporter;
import org.polymap.p4.imports.utils.UploadHelper;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.batik.toolkit.md.Snackbar;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeImportPanel
        extends DefaultPanel {

    public static final PanelIdentifier ID   = PanelIdentifier.parse( "shapeimport" );

    private static final IMessages      i18n = Messages.forPrefix( "ShapeImportPanel" );


    @Override
    public boolean wantsToBeShown() {
        return parentPanel().filter( parent -> parent instanceof ProjectMapPanel ).map( parent -> {
            setTitle();
            getSite().setPreferredWidth( 350 );
            return true;
        } ).orElse( false );
    }


    private void setTitle() {
        getSite().setTitle( "Import" );
    }


    @Override
    public void createContents( Composite parent ) {
        setTitle();
        
        parent.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        MdToolkit tk = (MdToolkit)getSite().toolkit();

        Button importFab = createImportFAB( tk );
        Snackbar snackBar = tk.createFloatingSnackbar( SWT.NONE );

        MdListViewer fileList = tk.createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION );

        IssueReporter issueReporter = new IssueReporter( snackBar );
        List<FileDescription> files = new ArrayList<FileDescription>();
        ShapeImportPanelUpdater shapeImportPanelUpdater = createShapeImportPanelUpdater( importFab, issueReporter,
                files, fileList );

        final UploadHelper uploadHelper = new UploadHelper( files, shapeImportPanelUpdater, issueReporter );
        DropTargetAdapter dropTargetAdapter = uploadHelper.createDropTargetAdapter();

        initFileList( fileList, shapeImportPanelUpdater, issueReporter, dropTargetAdapter, files );
        createUpload( parent, uploadHelper, dropTargetAdapter );
    }


    private void createUpload( Composite parent, final UploadHelper uploadHelper, DropTargetAdapter dropTargetAdapter ) {
        // DnD and upload
        Upload upload = new Upload( parent, SWT.NONE/* , Upload.SHOW_PROGRESS */);
        upload.setImage( P4Plugin.images().svgImage( "upload.svg", SvgImageRegistryHelper.NORMAL48 ) );
        upload.setText( "" );
        upload.setData( RWT.TOOLTIP_MARKUP_ENABLED, true );
        upload.setData( /*MarkupValidator.MARKUP_VALIDATION_DISABLED*/"org.eclipse.rap.rwt.markupValidationDisabled", false ); 
        upload.setToolTipText( "<b>Drop</b> files here<br/>or <b>click</b> to open file dialog" );
        upload.setHandler( uploadHelper );
        FormDataFactory.on( upload ).fill().bottom( 100, -5 ).top( 90 );
        upload.moveAbove( null );

        DropTarget labelDropTarget = new DropTarget( upload, DND.DROP_MOVE );
        labelDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        labelDropTarget.addDropListener( dropTargetAdapter );
    }


    private MdListViewer initFileList( MdListViewer fileList, ShapeImportPanelUpdater shapeImportPanelUpdater,
            IssueReporter issueReporter, DropTargetAdapter dropTargetAdapter, List<FileDescription> files ) {

        initFileListProviders( files, fileList, shapeImportPanelUpdater );
        // label providers have to be in place here, as otherwise the tileHeight
        // wouldn't be
        // adjusted and then the custom height is 0, leading to a / by zero in
        // getVisibleRowCount
        initFileListWithContent( issueReporter, files, fileList, shapeImportPanelUpdater );

        initOpenListener( fileList );

        enableDropTarget( dropTargetAdapter, fileList );

        ColumnViewerToolTipSupport.enableFor( fileList );

        fileList.getControl().setData( RWT.MARKUP_ENABLED, true );
        fileList.getControl().setData(
                /* MarkupValidator.MARKUP_VALIDATION_DISABLED */"org.eclipse.rap.rwt.markupValidationDisabled", false );

        FormDataFactory.on( fileList.getControl() ).fill().bottom( 90, -5 );
        return fileList;
    }


    private ShapeImportPanelUpdater createShapeImportPanelUpdater( Button importFab, IssueReporter issueReporter,
            List<FileDescription> files, MdListViewer fileList ) {
        ShapeFileImporter shapeFileImporter = new ShapeFileImporter( this );
        ShapeImportPanelUpdater shapeImportPanelUpdater = new ShapeImportPanelUpdater( files, fileList, importFab,
                issueReporter, shapeFileImporter );
        return shapeImportPanelUpdater;
    }


    private void enableDropTarget( DropTargetAdapter dropTargetAdapter, MdListViewer fileList ) {
        DropTarget fileListDropTarget = new DropTarget( fileList.getControl(), DND.DROP_MOVE );
        fileListDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        fileListDropTarget.addDropListener( dropTargetAdapter );
    }


    private void initOpenListener( MdListViewer fileList ) {
        fileList.addOpenListener( new IOpenListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void open( OpenEvent ev ) {
                org.polymap.core.ui.SelectionAdapter.on( ev.getSelection() ).forEach( ( Object elm ) -> {
                    fileList.toggleItemExpand( elm );
                } );
            }
        } );
    }


    private void initFileListProviders( List<FileDescription> files, MdListViewer fileList,
            ShapeImportPanelUpdater shapeImportPanelUpdater ) {
        fileList.iconProvider.set( new ShapeImportImageLabelProvider() );
        fileList.firstLineLabelProvider.set( new ShapeImportCellLabelProvider() );
        fileList.secondLineLabelProvider.set( new MessageCellLabelProvider() );
        fileList.firstSecondaryActionProvider.set( new ShapeFileDeleteActionProvider( files, shapeImportPanelUpdater ) );
//        fileList.setLabelProvider( new ShapeImportCellLabelProvider() );
    }


    private void initFileListWithContent( IssueReporter issueReporter, List<FileDescription> files,
            MdListViewer fileList, ShapeImportPanelUpdater shapeImportPanelUpdater ) {
        fileList.setContentProvider( new ShapeImportTreeContentProvider( files ) );
        fileList.setInput( files );
        InitFileListHelper initFileListHelper = new InitFileListHelper( files, shapeImportPanelUpdater, issueReporter );
        initFileListHelper.initFileList();
    }


    private Button createImportFAB( MdToolkit tk ) {
        Button fab = tk.createFab( /*
                                    * P4Plugin.instance().imageForName(
                                    * "resources/icons/png/gray/16/import.png" ),
                                    * SWT.UP | SWT.RIGHT
                                    */);
        fab.setToolTipText( "Import this uploaded Shapefile" );
        fab.setVisible( false );
        fab.moveAbove( null );
        return fab;
    }
}
