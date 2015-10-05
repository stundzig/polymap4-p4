/* 
 * Copyright (C) 2015, the @authors. All rights reserved.
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
package org.polymap.p4.data.imports;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;
import java.util.Arrays;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;

import org.polymap.core.CorePlugin;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImportPrompt.PromptChangeEvent;
import org.polymap.p4.data.imports.ImportsLabelProvider.Type;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rap.updownload.upload.UploadService;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImportPanel
        extends DefaultPanel 
        implements IUploadHandler {

    private static Log log = LogFactory.getLog( ImportPanel.class );

    public static final PanelIdentifier ID   = PanelIdentifier.parse( "dataimport" );

    private static final IMessages      i18n = Messages.forPrefix( "ImportPanel" );
    
    private static final File           baseTempDir = new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "importTemp" );

    static {
        try {
            baseTempDir.mkdirs();
            //FileUtils.cleanDirectory( baseTempDir );
            log.info( "temp dir: " + baseTempDir + " ... XXX: not cleaned!" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    // instance *******************************************
    
    private Context<ImportDriver>       driver;
    
    private boolean                     rootImportPanel;
    
    private MdListViewer                importsList;

    private Composite                   resultViewer;

    private File                        tempDir;

    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectMapPanel )
                .map( parent -> {
                    site().title.set( "" );
                    site().icon.set( P4Plugin.images().svgImage( "import.svg", NORMAL24 ) );
                    site().preferredWidth.set( 350 );
                    return true;
                } )
                .orElse( false );
    }
    
    
    @Override
    public void init() {
        // temp dir
        try {
            tempDir = Files.createTempDirectory( baseTempDir.toPath(), null ).toFile();
        }
        catch (IOException e) {
            throw new RuntimeException( e );            
        }

        // driver
        rootImportPanel = !driver.isPresent();
        driver.compareAndSet( null, new ImportDriver() {
            @Override
            protected void createResultViewer( Composite parent ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
            @Override
            protected void createNextLevelUI( Importer importer ) {
                getContext().openPanel( site().path(), ImportPanel.ID );
            }
        });
    }


    @Override
    public void dispose() {
        if (rootImportPanel) {
            driver.set( null );
        }
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 0, 8 ).create() );
        MdToolkit tk = (MdToolkit)site().toolkit();
        
        // upload button
        Upload upload = tk.adapt( new Upload( parent, SWT.NONE/* , Upload.SHOW_PROGRESS */), false, false );
        upload.setImage( P4Plugin.images().svgImage( "file-multiple.svg", NORMAL24 ) );
        upload.setText( "" );
        upload.setToolTipText( "<b>Drop</b> files here<br/>or <b>click</b> to open file dialog" );
        upload.setHandler( this );
        upload.moveAbove( null );

        DropTarget labelDropTarget = new DropTarget( upload, DND.DROP_MOVE );
        labelDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        labelDropTarget.addDropListener( createDropTargetAdapter() );

        // imports and prompts
        importsList = tk.createListViewer( parent, SWT.VIRTUAL, SWT.FULL_SELECTION );
        importsList.setContentProvider( new ImportsContentProvider() );
        importsList.firstLineLabelProvider.set( new ImportsLabelProvider().type.put( Type.Summary ) );
        importsList.secondLineLabelProvider.set( new ImportsLabelProvider().type.put( Type.Description ) );
        importsList.iconProvider.set( new ImportsLabelProvider().type.put( Type.Icon ) );
        importsList.addOpenListener( new IOpenListener() {
            @Override
            public void open( OpenEvent ev ) {
                SelectionAdapter.on( ev.getSelection() ).forEach( elm -> {
                    importsList.toggleItemExpand( elm );
                });
            }
        } );
        importsList.setInput( driver.get() );
        
        // result viewer
        resultViewer = tk.createComposite( parent );
        
        // layout
        FormDataFactory.on( upload ).fill().bottom( 0, 50 );
        FormDataFactory.on( importsList.getControl() ).fill().top( upload ).bottom( 50 );
        FormDataFactory.on( resultViewer ).fill().top( importsList.getControl() );
        
        //
        EventManager.instance().subscribe( this, ev -> ev instanceof PromptChangeEvent );
    }

    
    @EventHandler( display=true )
    protected void importPromptChanged( PromptChangeEvent ev ) {
        importsList.update( ev.getSource(), null );
    }
    
    
    @Override
    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

        // upload file
        assert clientFile.getName() != null : "Null client file name is not supported yet.";
        File f = new File( tempDir, clientFile.getName() );
        try (
            OutputStream out = new FileOutputStream( f )
        ) {
            IOUtils.copy( in, out );
        }
        catch (Exception e) {
            UIThreadExecutor.async( () -> site().status.set( new Status( IStatus.ERROR, P4Plugin.ID, "Unable to upload file.", e ) ) );
            return;
        }

        UIThreadExecutor.async( () -> {
            driver.get().addContext( f );
        });
    }

    
    protected DropTargetAdapter createDropTargetAdapter() {
        return new DropTargetAdapter() {
            @Override
            public void drop( DropTargetEvent ev ) {
                ClientFile[] clientFiles = (ClientFile[])ev.data;
                Arrays.stream( clientFiles ).forEach( clientFile -> {
                    log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );

                    String uploadUrl = UploadService.registerHandler( ImportPanel.this );

                    ClientFileUploader uploader = RWT.getClient().getService( ClientFileUploader.class );
                    uploader.submit( uploadUrl, new ClientFile[] { clientFile } );
                } );
            }
        };
    }

}
