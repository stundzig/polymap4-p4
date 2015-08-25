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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.rap.rwt.template.Cell;
import org.eclipse.rap.rwt.template.Template;
import org.eclipse.rap.rwt.template.TextCell;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;
import org.polymap.p4.Messages;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rap.updownload.upload.UploadService;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

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

    private SelectionAdapter selectionListener;


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

        // DnD
        Label dropLabel = tk.createLabel( parent, "Drop files here...", SWT.BORDER | SWT.CENTER );
        FormDataFactory.on( dropLabel ).fill().noBottom().height( dp( 100 ).pix() );

        // upload field
        Upload upload = new Upload( parent, SWT.NONE/* , Upload.SHOW_PROGRESS */);
        upload.setHandler( ShapeImportPanel.this );
        FormDataFactory.on( upload ).noBottom().top( 0, dp( 40 ).pix() ).width( dp( 200 ).pix() );
        upload.moveAbove( dropLabel );

        DropTarget dropTarget = new DropTarget( dropLabel, DND.DROP_MOVE );
        dropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        dropTarget.addDropListener( new DropTargetAdapter() {

            private static final long serialVersionUID = 4701279360320517568L;


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
        } );

        fab = tk.createFab();
        fab.setToolTipText( "Import this uploaded Shapefile" );
        fab.setVisible( false );

        //
        fileList = tk.createListViewer( parent );
        
        fileList.setContentProvider( new ShapeImportTreeContentProvider( files ) );
        fileList.firstLineLabelProvider.set( new ShapeImportCellLabelProvider() );
        fileList.secondLineLabelProvider.set( new MessageCellLabelProvider( files ) );

        fileList.firstSecondaryActionProvider.set( new DeleteActionProvider( files, this ) );
        fileList.setInput( files );

        initFileList();

        FormDataFactory.on( fileList.getControl() ).fill().top( dropTarget.getControl(), 40 );
    }


    private void initFileList() {
        ShapeFetchOperation shapeFetchOperation = new ShapeFetchOperation();
        OperationSupport.instance().execute2( shapeFetchOperation, true, false,
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
                        StatusDispatcher.handleError( "Couldn't read out data.", ev.getResult().getException() );
                    }
                    UIThreadExecutor.async( ( ) -> updateListAndFAB(), UIThreadExecutor.runtimeException() );
                } ) );
    }


    private Map<String,List<File>> groupFilesByName( List<File> fs ) {
        Map<String,List<File>> files = new HashMap<String,List<File>>();
        for (File f : fs) {
            int index = f.getName().lastIndexOf( "." );
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


    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

        List<File> read = new FileImporter()/* .overwrite.put( true ) */
        .run( clientFile.getName(), clientFile.getType(), in );
        Map<String,List<File>> grouped = groupFilesByName( read );
        if(!files.containsKey( clientFile.getName() )) {
            files.put( clientFile.getName(), grouped );
        } else {
            files.put( clientFile.getName() + "_duplicated", grouped );
        }

        UIThreadExecutor.async( ( ) -> updateListAndFAB(), UIThreadExecutor.runtimeException() );
    }


    public void updateListAndFAB() {
        fileList.refresh();
        fileList.expandAll();
        
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
                if(selectionListener != null) {
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
            }
        } else {
            fab.setVisible( false );
        }
    }


    protected void importFiles( File file ) {
        ShapeImportOperation op = new ShapeImportOperation().shpFile.put( file );
        // XXX progress?
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( ( ) -> {
            if (ev.getResult().isOK()) {
                PanelPath panelPath = getSite().getPath();
                getContext().closePanel( panelPath.removeLast( 1 /* 2 */) );
            }
            else {
                StatusDispatcher.handleError( file, "Unable to import file.", ev.getResult().getException() );
            }
        } ) );
    }

}
