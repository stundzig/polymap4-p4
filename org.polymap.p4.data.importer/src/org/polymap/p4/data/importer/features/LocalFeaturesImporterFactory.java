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

import org.geotools.feature.FeatureCollection;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ImporterFactory;

/**
 * 
 *
 * @deprecated Dropped in favour of {@link ImportFeaturesOperation}.
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LocalFeaturesImporterFactory
        implements ImporterFactory {

    @ContextIn
    private FeatureCollection       features;

    
    @Override
    public void createImporters( ImporterBuilder builder ) throws Exception {
        if (features != null) {
            builder.newImporter( new LocalFeaturesImporter(), features );
        }
    }

}
