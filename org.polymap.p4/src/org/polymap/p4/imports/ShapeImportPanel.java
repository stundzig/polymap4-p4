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
import java.util.List;
import java.util.Optional;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.rap.rwt.internal.serverpush.ServerPushManager;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;
import org.polymap.core.ui.UIUtils;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PanelPath;
import org.polymap.rhei.batik.toolkit.md.ListTreeContentProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.p4.Messages;
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
        implements IUploadHandler {

    private static Log log = LogFactory.getLog( ShapeImportPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "shapeimport" );
    
    private static final IMessages      i18n = Messages.forPrefix( "ShapeImportPanel" );
    
    private List<File>                  files = new ArrayList();

    private MdListViewer                fileList;

    private MdToolkit                   tk;

    private Button fab;
    
    
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
        Upload upload = new Upload( parent, SWT.NONE/*, Upload.SHOW_PROGRESS*/ );
        FormDataFactory.on( upload ).noBottom().top( 0, dp( 40 ).pix() ).width( dp( 200 ).pix() );
        upload.setHandler( ShapeImportPanel.this );
        upload.moveAbove( dropLabel );
        
        DropTarget dropTarget = new DropTarget( dropLabel, DND.DROP_MOVE );
        dropTarget.setTransfer( new Transfer[]{ ClientFileTransfer.getInstance() } );
        dropTarget.addDropListener( new DropTargetAdapter() {
            @Override
            public void drop( DropTargetEvent ev ) {
                ClientFile[] clientFiles = (ClientFile[])ev.data;
                Arrays.stream( clientFiles ).forEach( clientFile -> {
                    log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );
                    
                    String uploadUrl = UploadService.registerHandler( ShapeImportPanel.this );
                    
                    ClientFileUploader uploader = RWT.getClient().getService( ClientFileUploader.class );
                    uploader.submit( uploadUrl, new ClientFile[] { clientFile } );
                });
            }
        });
        
        //
        fileList = tk.createListViewer( parent );
        fileList.setContentProvider( new ListTreeContentProvider() {
            @Override
            public Object[] getElements( Object elm ) {
                return files.toArray();
            }
        });
        fileList.firstLineLabelProvider.set( new CellLabelProvider() {
            @Override
            public void update( ViewerCell cell ) {
                File f = (File)cell.getElement();
                cell.setText( f.getName() );
            }
        });
        fileList.setInput( files );
        FormDataFactory.on( fileList.getControl() ).fill().top( dropLabel );

        fab = tk.createFab();
        fab.setToolTipText( "Import this uploaded Shapefile" );
        fab.setVisible( false );
    }

    
    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );
        
        List<File> read = new FileImporter()
                .run( clientFile.getName(), clientFile.getType(), in );
        files.addAll( read );
        
        UIThreadExecutor.async( () -> updateListAndFAB(), UIThreadExecutor.runtimeException() );
    }


    protected void updateListAndFAB() {
        fileList.refresh();
        Optional<File> shp = files.stream().filter( f -> f.getName().toLowerCase().endsWith( ".shp" ) ).findAny();
        if (shp.isPresent()) {
            fab.addSelectionListener( new SelectionAdapter() {
                @Override
                public void widgetSelected( SelectionEvent ev ) {
                    importFiles( shp.get() );
                }
            } );
            fab.setVisible( true );
        }
        UIUtils.deactivateCallback( "upload" );
    }


    protected void importFiles( File file ) {
        ShapeImportOperation op = new ShapeImportOperation().shpFile.put( file );
        // XXX progress?
        OperationSupport.instance().execute2( op, true, false, ev -> UIThreadExecutor.asyncFast( () -> {
            if (ev.getResult().isOK()) {
                PanelPath panelPath = getSite().getPath();
                getContext().closePanel( panelPath.removeLast( 1 /*2*/ ) );
            }
            else {
                StatusDispatcher.handleError( "Unable to import file.", ev.getResult().getException() );
            }
        }));
    }
    
}
