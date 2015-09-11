/*
 * polymap.org and individual contributors as indicated by the @authors tag.
 * Copyright (C) 2009-2015 
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.imports;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.p4.Messages;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rap.updownload.upload.UploadService;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;
import org.polymap.rhei.batik.toolkit.md.Snackbar;
import org.polymap.rhei.batik.toolkit.md.Snackbar.MessageType;

/**
 * A panel for client file uploads. It can handle zip, tar and gz files
 * automatically. During upload, the file contents are flattened and for each
 * file, one of the registered FileImportHandler is called.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class FileImportPanel extends DefaultPanel implements IUploadHandler {

	private static Log log = LogFactory.getLog(FileImportPanel.class);

	public static final PanelIdentifier ID = PanelIdentifier.parse("clientfileimport");

	private static final IMessages i18n = Messages.forPrefix(FileImportPanel.class.getSimpleName());

//	private Map<String, Map<String, List<File>>> files = new HashMap<String, Map<String, List<File>>>();

	private MdToolkit tk;

	private Snackbar snackBar;

	private Composite importComposite;

	private List<FileImportHandler> handlers;

	@Override
	public boolean wantsToBeShown() {
		if (parentPanel().isPresent() && parentPanel().get() instanceof ProjectMapPanel) {
			getSite().setTitle("Import");
			getSite().setPreferredWidth(300);
			return true;
		}
		return false;
	}

	@Override
	public void createContents(Composite parent) {
		handlers = null;
		parent.setLayout(FormLayoutFactory.defaults().spacing(dp(16).pix()).create());
		tk = (MdToolkit) getSite().toolkit();

		// DnD
		Label dropLabel = tk.createLabel(parent, "Drop files here...", SWT.BORDER | SWT.CENTER);
		FormDataFactory.on(dropLabel).fill().noBottom().height(dp(100).pix());

		// upload field
		Upload upload = new Upload(parent, SWT.NONE/* , Upload.SHOW_PROGRESS */);
		upload.setHandler(FileImportPanel.this);
		FormDataFactory.on(upload).noBottom().top(0, dp(40).pix()).width(dp(200).pix());
		upload.moveAbove(dropLabel);

		DropTarget dropTarget = new DropTarget(dropLabel, DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { ClientFileTransfer.getInstance() });
		dropTarget.addDropListener(new DropTargetAdapter() {

			private static final long serialVersionUID = 4701279360320517568L;

			@Override
			public void drop(DropTargetEvent ev) {
				ClientFile[] clientFiles = (ClientFile[]) ev.data;
				Arrays.stream(clientFiles).forEach(clientFile -> {
					log.info(clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize());

					String uploadUrl = UploadService.registerHandler(FileImportPanel.this);

					ClientFileUploader uploader = RWT.getClient().getService(ClientFileUploader.class);
					uploader.submit(uploadUrl, new ClientFile[] { clientFile });
				});
			}
		});

		importComposite = tk.createComposite(parent, SWT.NONE);
		snackBar = tk.createFloatingSnackbar(SWT.NONE);
	}

	@SuppressWarnings("unchecked")
	public void uploadStarted(ClientFile clientFile, InputStream in) throws Exception {
		log.info(clientFile.getName() + " - " + clientFile.getType() + " - " + clientFile.getSize());
		try {
			List<File> read = new FileImporter().run(clientFile.getName(), clientFile.getType(), in);
			if (read.isEmpty()) {
				snackBar.showIssue(MessageType.ERROR, "There are no files contained.");
			} else {
				boolean canHandle = false;
				for (File file : read) {
					for (FileImportHandler handler : handlers()) {
						if (handler.canHandle(file)) {
							handler.handle(file, importComposite);
							canHandle = true;
							continue;
						}
					}
					if (!canHandle) {
						snackBar.showIssue(MessageType.ERROR, "I don't know, how to handle " + file.getName());
					}
				}
			}
			// UIThreadExecutor.async(() ->
			// updateListAndFAB(clientFile.getName(), true),
			// UIThreadExecutor.runtimeException());
		} catch (Exception e) {
//			files.put(clientFile.getName(), Collections.EMPTY_MAP);
			log.error("Unable to import file.", e);
			UIThreadExecutor.async(() -> {
				// updateListAndFAB(clientFile.getName(), false);
				snackBar.showIssue(MessageType.ERROR, "Unable to import file.");
			} , UIThreadExecutor.runtimeException());
		}
	}

	private List<FileImportHandler> handlers() {
		if (handlers == null) {
			handlers = FileImportHandlerContributionsRegistry.INSTANCE().handlers();
		}
		return handlers;
	}
}
