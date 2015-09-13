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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;

import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.batik.toolkit.md.Snackbar;
import org.polymap.rhei.batik.toolkit.md.Snackbar.MessageType;

import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.CatalogPanel;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rap.updownload.upload.UploadService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShapeImportPanel
        extends DefaultPanel
        implements IUploadHandler, UpdatableList {

    private static Log                         log   = LogFactory.getLog( ShapeImportPanel.class );

    public static final PanelIdentifier        ID    = PanelIdentifier.parse( "shapeimport" );

    private static final IMessages             i18n  = Messages.forPrefix( "ShapeImportPanel" );

    private Map<String,Map<String,List<File>>> files = new HashMap<String,Map<String,List<File>>>();

    private MdListViewer                       fileList;

    private MdToolkit                          tk;

    private Button                             fab;

    private SelectionAdapter                   selectionListener;

    private Snackbar                           snackBar;


    @Override
    public boolean wantsToBeShown() {
        if (parentPanel().isPresent() && parentPanel().get() instanceof ProjectMapPanel) {
            getSite().setTitle( "Import" );
            getSite().setPreferredWidth( 300 );
            return true;
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        tk = (MdToolkit)getSite().toolkit();

        DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {
            @Override
            public void drop( DropTargetEvent ev ) {
                ClientFile[] clientFiles = (ClientFile[])ev.data;
                Arrays.stream( clientFiles ).forEach( clientFile -> {
                    log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

                    String uploadUrl = UploadService.registerHandler( ShapeImportPanel.this );

                    ClientFileUploader uploader = RWT.getClient().getService( ClientFileUploader.class );
                    uploader.submit( uploadUrl, new ClientFile[] { clientFile } );
                } );
            }
        };

        // FAB
        fab = tk.createFab( /*P4Plugin.instance().imageForName( "resources/icons/png/gray/16/import.png" ), SWT.UP | SWT.RIGHT*/ );
        fab.setToolTipText( "Import this uploaded Shapefile" );
        fab.setVisible( false );
        fab.moveAbove( null );

        // filelIst
        fileList = tk.createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION );
        fileList.setContentProvider( new ShapeImportTreeContentProvider( files ) );
        fileList.iconProvider.set( new ShapeImportImageLabelProvider() );
        fileList.firstLineLabelProvider.set( new ShapeImportCellLabelProvider() );
        fileList.secondLineLabelProvider.set( new MessageCellLabelProvider(files) );

        fileList.firstSecondaryActionProvider.set( new ShapeFileDeleteActionProvider( files, this ) );
        fileList.setInput( files );

        initFileList();

        fileList.addOpenListener( new IOpenListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void open( OpenEvent ev ) {
                org.polymap.core.ui.SelectionAdapter.on( ev.getSelection() ).forEach( (Object elm) -> {
                    fileList.toggleItemExpand( elm );
                } );
            }
        } );
        FormDataFactory.on( fileList.getControl() ).fill().bottom( 90, -5 );
        
        DropTarget fileListDropTarget = new DropTarget( fileList.getControl(), DND.DROP_MOVE );
        fileListDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        fileListDropTarget.addDropListener( dropTargetAdapter );

        // DnD and upload
        Upload upload = new Upload( parent, SWT.NONE/* , Upload.SHOW_PROGRESS */);
        upload.setImage( P4Plugin.images().svgImage( "upload.svg", SvgImageRegistryHelper.NORMAL48 ) );
        upload.setText( "" );
        upload.setToolTipText( "<b>Drop</b> files here<br/>or <b>click</b> to open file dialog" );
        upload.setHandler( ShapeImportPanel.this );
        FormDataFactory.on( upload ).fill().bottom( 100, -5 ).top( 90 );
        upload.moveAbove( null );

        DropTarget labelDropTarget = new DropTarget( upload, DND.DROP_MOVE );
        labelDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        labelDropTarget.addDropListener( dropTargetAdapter );

        snackBar = tk.createFloatingSnackbar( SWT.NONE );
    }


    @SuppressWarnings("unchecked")
    private void initFileList() {
        ShapeFetchOperation shapeFetchOperation = new ShapeFetchOperation();
        OperationSupport.instance().execute2(
                shapeFetchOperation,
                true,
                false,
                ev -> UIThreadExecutor.asyncFast( ( ) -> {
                    if (ev.getResult().isOK()) {
                        List<File> fs = shapeFetchOperation.getFiles();
                        files.clear();
                        Map<String,List<File>> grouped = groupFilesByName( fs );
                        for (Map.Entry<String,List<File>> entry : grouped.entrySet()) {
                            Map<String,List<File>> map = new HashMap<String,List<File>>();
                            map.put( entry.getKey(), entry.getValue() );
                            files.put( entry.getKey(), map );
                        }
                    }
                    else {
                        snackBar.showIssue( MessageType.ERROR, "Couldn't read out data." );
                        log.error( "Couldn't read out data.", ev.getResult().getException() );
                    }
                    if (!files.isEmpty()) {
                        UIThreadExecutor.async( ( ) -> updateListAndFAB( files.keySet().iterator().next(), false ),
                                UIThreadExecutor.runtimeException() );
                    }
                } ) );
    }


    private Map<String,List<File>> groupFilesByName( List<File> fs ) {
        Map<String,List<File>> files = new HashMap<String,List<File>>();
        for (File f : fs) {
            int index = f.getName().lastIndexOf( ".shp.xml" );
            if (index == -1) {
                index = f.getName().lastIndexOf( "." );
            }
            if (index > 0) {
                String fName = f.getName().substring( 0, index );
                List<File> gFs = files.get( fName );
                if (gFs == null) {
                    gFs = new ArrayList<File>();
                }
                gFs.add( f );
                files.put( fName, gFs );
            }
        }
        return files;
    }


    @SuppressWarnings("unchecked")
    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

        try { 
            List<File> read = new FileImporter().run( clientFile.getName(), clientFile.getType(), in );
            if (read.isEmpty()) {
                ShapeFileValidator.reportError( clientFile.getName(), "There are no files contained." );
            }
            else {
                Map<String,List<File>> grouped = groupFilesByName( read );
                if (!files.containsKey( clientFile.getName() )) {
                    files.put( clientFile.getName(), grouped );
                }
                else {
                    files.put( clientFile.getName() + "_duplicated", grouped );
                }
            }
            UIThreadExecutor.async( ( ) -> updateListAndFAB( clientFile.getName(), true ), UIThreadExecutor.runtimeException() );
        } catch(Exception e) {
            files.put( clientFile.getName(), Collections.EMPTY_MAP );
            log.error( "Unable to import file.", e );
            UIThreadExecutor.async( ( ) -> {
                updateListAndFAB( clientFile.getName(), false );
                ShapeFileValidator.reportError( clientFile.getName(), "Unable to import file." );
                snackBar.showIssue( MessageType.ERROR, "Unable to import file." );
            }, UIThreadExecutor.runtimeException() );
        }
    }


    public void updateListAndFAB( Object root, boolean fromUpload ) {
        fileList.refresh();
        fileList.toggleItemExpand( root );

        boolean valid = new ShapeFileValidator().validateAll( files );
        if (valid) {
            List<?> shps = files
                    .values()
                    .stream()
                    .flatMap(
                            map -> map
                                    .values()
                                    .stream()
                                    .flatMap(
                                            fs -> fs.stream()
                                                    .filter( f -> f.getName().toLowerCase().endsWith( ".shp" ) ) ) )
                    .collect( Collectors.toList() );
            if (shps.size() > 0) {
                if (selectionListener != null) {
                    fab.removeSelectionListener( selectionListener );
                }
                selectionListener = new SelectionAdapter() {

                    private static final long serialVersionUID = -1075952252353984655L;


                    @Override
                    public void widgetSelected( SelectionEvent ev ) {
                        for (Object shp : shps) {
                            UIUtils.activateCallback( "importFiles" );
                            importFiles( (File)shp );
                            UIUtils.deactivateCallback( "importFiles" );
                        }
                    }
                };

                fab.addSelectionListener( selectionListener );
                fab.setVisible( true );
                if(fromUpload) {
                    snackBar.showIssue( MessageType.SUCCESS, root + " successfully uploaded." );
                }
            }
        }
        else {
            fab.setVisible( false );
        }
    }


    @SuppressWarnings("unchecked")
    protected void importFiles( File file ) {
        ShapeImportOperation op = new ShapeImportOperation().shpFile.put( file );
        // XXX progress?
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
            if (ev.getResult().isOK()) {
                PanelPath panelPath = getSite().getPath();
                getContext().closePanel( panelPath/* .removeLast( 1 ) */);
                getContext().openPanel( getSite().getPath(), CatalogPanel.ID );
            }
            else {
                ShapeFileValidator.reportError( file, "Unable to import file." );
                log.error( "Unable to import file.", ev.getResult().getException() );
            }
        } ) );
    }


    /* (non-Javadoc)
     * @see org.polymap.p4.imports.UpdatableList#refresh()
     */
    @Override
    public void refresh() {
        fileList.refresh();
    }
}
