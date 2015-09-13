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

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ViewerCell;

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
 */
public class LayerDeleteActionProvider 
        extends ActionProvider {
	
	private static Log log = LogFactory.getLog( LayerDeleteActionProvider.class );
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.
	 * viewers.ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell) {
		cell.setImage( P4Plugin.images().svgImage( "ic_delete_48px.svg", NORMAL24 ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.polymap.rhei.batik.toolkit.md.ActionProvider#perform(org.polymap.
	 * rhei.batik.toolkit.md.MdListViewer, java.lang.Object)
	 */
	@Override
    public void perform( MdListViewer viewer, Object element ) {
        if (element instanceof ILayer) {
            ILayer layer = (ILayer)element;
            try {
                UnitOfWork uow = ProjectRepository.instance.get().newUnitOfWork();
                uow.removeEntity( layer );
                layer.parentMap.get().layers.remove( layer );
                viewer.refresh();
            }
            catch (Throwable e) {
                log.error( e );
            }
        }
    }
	
}
