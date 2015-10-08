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
    
    /**
     * Initializes this Importer. This should set {@link ImporterSite#summary},
     * {@link ImporterSite#description} and {@link ImporterSite#icon}. This is called
     * right after contruction.
     *
     * @param site
     * @param monitor
     * @throws Exception
     */
    public void init( ImporterSite site, IProgressMonitor monitor ) throws Exception;
    
    /**
     * Creates the prompts of this Importer. This is called once after
     * {@link #init(ImporterSite, IProgressMonitor)}.
     *
     * @param monitor
     * @throws Exception
     */
    public void createPrompts( IProgressMonitor monitor ) throws Exception;
    
    /**
     * Verifies the context and the state of the prompts. This is called when a
     * prompt has changed its status. This method should prepare contents of the
     * {@link #createResultViewer(Composite)}.
     *
     * @param monitor
     */
    public boolean verify( IProgressMonitor monitor );

    /**
     * If possible this creates a preview of the imported data.
     *
     * @param parent
     */
    public Composite createResultViewer( Composite parent );

    /**
     * Execute this importer and sent results to next level of import. This method
     * is called only if all prompts are {@link ImporterPrompt#ok}.
     * 
     * @param monitor The monitor to report progress to. Frequently check for cancelation.
     */
    public void execute( IProgressMonitor monitor ) throws Exception;
    
}
