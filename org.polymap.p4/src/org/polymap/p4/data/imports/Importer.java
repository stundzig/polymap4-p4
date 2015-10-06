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

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * @see ContextIn
 * @see ContextOut
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface Importer {

    public ImporterSite site();

    public void init( ImporterSite site, IProgressMonitor monitor ) throws Exception;
    
    public void createPrompts( IProgressMonitor monitor ) throws Exception;
    
    /**
     * Execute this importer and sent results to next level of import. This method
     * is called only if all prompts are {@link ImportPrompt#ok}.
     * 
     * @param monitor The monitor to report progress to. Frequently check for cancelation.
     */
    public void execute( IProgressMonitor monitor ) throws Exception;
    
    public Composite createResultViewer( Composite parent );
    
}
