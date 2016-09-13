/*
 * GeoTools - The Open Source Java GIS Toolkit http://geotools.org
 *
 * (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.geojson;

import java.io.IOException;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.AttributeIO;
import org.geotools.geojson.feature.CRSHandler;
import org.geotools.geojson.feature.FeatureHandler;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

/**
 * copied from gt-geotools to support also inline CRS
 *
 * @author Steffen Stundzig
 */
public class GeoJSONFeatureHandler
        extends FeatureHandler {

    private ContentHandler oldDelegate;


    public GeoJSONFeatureHandler( SimpleFeatureBuilder builder, AttributeIO attio ) {
        super( builder, attio );
    }


    @Override
    public boolean startObjectEntry( String key ) throws ParseException, IOException {
        if ("crs".equals( key )) {
            oldDelegate = delegate;
        }
        return super.startObjectEntry( key );
    }


    @Override
    public boolean endObject() throws ParseException, IOException {
        boolean useOldDelegate = false;
        if (delegate instanceof CRSHandler) {
            useOldDelegate  = true;
        }
        boolean result = super.endObject();
        if (useOldDelegate) {
            delegate = oldDelegate;
        }
        return result;
    }
}
