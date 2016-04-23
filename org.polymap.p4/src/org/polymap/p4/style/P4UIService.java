/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.style;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.style.ui.UIService;

import org.polymap.rhei.batik.toolkit.SimpleDialog;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class P4UIService
        extends UIService {

    private static Log log = LogFactory.getLog( P4UIService.class );

    @Override
    public void openDialog( String title, Consumer<Composite> contents, Callable<Boolean> okAction ) {
        SimpleDialog dialog = new SimpleDialog();
        dialog.title.set( title );
        dialog.setContents( contents );
        dialog.addCancelAction();
        dialog.addOkAction( okAction );
        dialog.setBlockOnOpen( true );
        dialog.open();
    }
    
}
