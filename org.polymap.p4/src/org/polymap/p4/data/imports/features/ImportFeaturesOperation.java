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
package org.polymap.p4.data.imports.features;

import java.util.Map;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.data.rs.catalog.RServiceInfo;
import org.polymap.core.operation.DefaultOperation;
import org.polymap.core.runtime.SubMonitor;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImportFeaturesOperation
        extends DefaultOperation
        implements IUndoableOperation {

    private static Log log = LogFactory.getLog( ImportFeaturesOperation.class );
    
    private ImporterContext         context;

    private FeatureStore            fs;

    
    public ImportFeaturesOperation( ImporterContext context ) {
        super( "Import features" );
        this.context = context;
    }

    
    public FeatureStore createdFeatureStore() {
        assert fs != null;
        return fs;
    }

    
    /**
     * The resource id of the newly created schema. 
     */
    public String resourceIdentifier() {
        IResolvableInfo info = P4Plugin.localCatalog().localFeaturesStoreInfo();
        IResourceInfo res = ((RServiceInfo)info.getServiceInfo()).resource( fs );
        return P4Plugin.localResolver().resourceIdentifier( res );
    }

    
    @Override
    protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        monitor.beginTask( getLabel(), 10 );
        
        // execute previous importer
        SubMonitor submon = SubMonitor.on( monitor, 3 );
        Map<Class,Object> contextOut = context.execute( submon );
        submon.done();

        // add features
        submon = SubMonitor.on( monitor, 10 );
        submon.beginTask( "Adding features", 100 );

        FeatureCollection features = (FeatureCollection)contextOut.get( FeatureCollection.class );
        assert features != null : "No FeatureCollection in @ContextOut";
        
        DataAccess ds = P4Plugin.localCatalog().localFeaturesStore();        
        // XXX transaction that spans createSchema() and addFeatures()!?
        ds.createSchema( features.getSchema() );
        
        fs = (FeatureStore)ds.getFeatureSource( features.getSchema().getName() );
        fs.addFeatures( features );
        submon.done();

//        DefaultTransaction tx = new DefaultTransaction();
//        try {
//            fs.setTransaction( tx );
//            int chunkSize = Math.max( 1, featuresSize / 10 );
//            for (int featuresCount=0; featuresCount<featuresSize; ) {
//                for (int i=0; i<chunkSize; i++) {
//                    
//                }
//            }
//            fs.addFeatures( features );
//        }
//        finally {
//            tx.close();
//        }
        
        return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
    }
    
}
