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
package org.polymap.p4.data.importer.features;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterSite;

/**
 * Imports features to the {@link LocalCatalog#localFeaturesStore()}.
 *
 * @deprecated Dropped in favour of {@link ImportFeaturesOperation}.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalFeaturesImporter
        implements Importer {

    private static Log log = LogFactory.getLog( LocalFeaturesImporter.class );
    
    private ImporterSite            site;

    @ContextIn
    private FeatureCollection       features;
    
    
    @Override
    public void init( @SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor ) throws Exception {
        this.site = site;
    }

    @Override
    public ImporterSite site() {
        return site;
    }

    
    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        DataAccess ds = P4Plugin.localCatalog().localFeaturesStore();
        
        // XXX transaction that spans createSchema() and addFeatures()!?
        
        ds.createSchema( features.getSchema() );
        FeatureStore fs = (FeatureStore)ds.getFeatureSource( features.getSchema().getName() );
        fs.addFeatures( features );
    }

    
    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void verify( IProgressMonitor monitor ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void createResultViewer( Composite parent, IPanelToolkit toolkit ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
