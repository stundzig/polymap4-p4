/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
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
package org.polymap.p4.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.polymap.core.data.DataPlugin;
import org.polymap.core.project.ILayer;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Plugin;
import org.polymap.rhei.batik.toolkit.md.ActionProvider;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;

/**
 * Intermediate solution, should be removed when
 * org.polymap.core.project.operations.RemoveLayerOperation and
 * org.polymap.core.project.ui.layer.RemoveLayerAction migrated.
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class LayerDeleteActionProvider extends ActionProvider {
	private static final long serialVersionUID = 6411582131381118166L;
	
	private static Log log = LogFactory.getLog( LayerDeleteActionProvider.class );
	
	private Image image = P4Plugin.imageDescriptorFromPlugin(
			DataPlugin.PLUGIN_ID, "icons/etool16/delete.gif").createImage();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.
	 * viewers.ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell) {
		cell.setImage(image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.polymap.rhei.batik.toolkit.md.ActionProvider#perform(org.polymap.
	 * rhei.batik.toolkit.md.MdListViewer, java.lang.Object)
	 */
	@Override
	public void perform(MdListViewer viewer, Object element) {
		if (element instanceof ILayer) {
			ILayer layer = (ILayer) element;
			try {
				UnitOfWork uow = ProjectRepository.instance.get().newUnitOfWork();
				uow.removeEntity(layer);
				layer.parentMap.get().layers.remove(layer);
				viewer.refresh();
			} catch (Throwable e) {
				log.error(e);
			}
		}
	}
}
