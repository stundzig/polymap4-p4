package org.polymap.p4.data.importer.geojson;

import java.io.IOException;
import java.io.Reader;

import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.GeoJSONUtil;
import org.geotools.geojson.feature.IFeatureCollectionHandler;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * copied from gt-geojson to change the GeoJSONFeatureCollectionHandler
 * implementation.
 * 
 * @author Steffen Stundzig
 */
class GeoJSONFeatureCollectionIterator
        implements FeatureIterator<SimpleFeature> {

    Reader                    reader;

    IFeatureCollectionHandler  handler;

    JSONParser                parser;

    SimpleFeature             next;

    private SimpleFeatureType featureType;


    GeoJSONFeatureCollectionIterator( Object input, SimpleFeatureType featureType ) {
        this.featureType = featureType;
        try {
            this.reader = GeoJSONUtil.toReader( input );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
        this.parser = new JSONParser();
    }


    IFeatureCollectionHandler getHandler() {
        return handler;
    }


    public boolean hasNext() {
        if (next != null) {
            return true;
        }

        if (handler == null) {
            // FIX only this line
            handler = new GeoJSONFeatureCollectionHandler( featureType, null );
            // handler = GeoJSONUtil.trace(handler, IFeatureCollectionHandler.class);
        }
        next = readNext();
        return next != null;
    }


    public SimpleFeature next() {
        SimpleFeature feature = next;
        next = null;
        return feature;
    }


    SimpleFeature readNext() {
        try {
            parser.parse( reader, handler, true );
            return handler.getValue();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

    }


    public void remove() {
        throw new UnsupportedOperationException();
    }


    public void close() {
        if (reader != null) {
            try {
                reader.close();
            }
            catch (IOException e) {
                // nothing to do
            }
            reader = null;
        }
        parser = null;
        handler = null;
    }
}