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
package org.polymap.p4;

import java.util.Arrays;

import java.io.InputStream;

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
import org.eclipse.swt.widgets.Label;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;

import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;

import org.polymap.p4.project.ProjectPanel;
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

    public static final PanelIdentifier ID = PanelIdentifier.parse( "import" );
    
    private static final IMessages      i18n = Messages.forPrefix( "ImportPanel" );
    
    
    @Override
    public boolean wantsToBeShown() {
        if (parentPanel().isPresent() && parentPanel().get() instanceof ProjectPanel) {
            getSite().setTitle( "Import" );
            getSite().setPreferredWidth( 300 );
            return true;
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        // upload field
        Upload upload = new Upload( parent, SWT.NONE );
        upload.setHandler( ImportPanel.this );
        
        // DnD
        Label dropLabel = new Label( parent, SWT.BORDER );
        dropLabel.setText( "Drop files here" );
        
        DropTarget dropTarget = new DropTarget( dropLabel, DND.DROP_MOVE );
        dropTarget.setTransfer( new Transfer[]{ ClientFileTransfer.getInstance() } );
        dropTarget.addDropListener( new DropTargetAdapter() {
            @Override
            public void drop( DropTargetEvent ev ) {
                ClientFile[] clientFiles = (ClientFile[])ev.data;
                Arrays.stream( clientFiles ).forEach( clientFile -> {
                    log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );
                    
                    String uploadUrl = UploadService.registerHandler( ImportPanel.this );
                    
                    ClientFileUploader uploader = RWT.getClient().getService( ClientFileUploader.class );
                    uploader.submit( uploadUrl, new ClientFile[] { clientFile } );
                });
            }
        });
    }

    
    public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
        log.info( clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize() );
        log.info( IOUtils.toString( in ) );
    }
    
}
