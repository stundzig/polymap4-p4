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
package org.polymap.p4.catalog;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.catalog.local.LocalMetadataCatalog;
import org.polymap.core.data.wms.catalog.WmsServiceResolver;

import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalCatalog
        extends LocalMetadataCatalog {

    private static Log log = LogFactory.getLog( LocalCatalog.class );
    
    
    public LocalCatalog() throws Exception {
        super( new RecordStoreAdapter( new LuceneRecordStore( 
                new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "localCatalog" ), false ) ) );
        createEntries();
    }

    
    protected void createEntries() throws Exception {
        // check empty
        if (query( "" ).execute().size() == 0) {
            // create fake entries
            try (Updater update = prepareUpdate()) {
                update.newEntry( metadata -> {
                    metadata.setTitle( "OSM WMS" );
                    metadata.setDescription( "Standardeintrag" );
                    metadata.setConnectionParams( WmsServiceResolver.createParams( "http://ows.terrestris.de/osm/service/" ) );
                });
                update.newEntry( metadata -> {
                    metadata.setTitle( "Schutzgebiete Mittelsachsen" );
                    metadata.setDescription( "Standardeintrag" );
                    metadata.setConnectionParams( WmsServiceResolver.createParams( "http://www.mittelsachsen-atlas.de/polymap-atlas/services/INSPIRE/Schutzgebiete" ) );
                });
                update.commit();
            }
        }
    }
    
}
