/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.geojson;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import java.io.IOException;

import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.DelegatingHandler;
import org.geotools.geojson.IContentHandler;
import org.geotools.geojson.feature.CRSHandler;
import org.geotools.geojson.feature.DefaultAttributeIO;
import org.geotools.geojson.feature.FeatureHandler;
import org.geotools.geojson.feature.FeatureTypeHandler;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Based on {@link FeatureTypeHandler}, but with a more stable primitive
 * implementation. Here a type change from Long to Double is supported.
 *
 * @author Steffen Stundzig
 */
public class LaxFeatureTypeHandler
        extends DelegatingHandler<SimpleFeatureType>
        implements IContentHandler<SimpleFeatureType> {

    private Stack<String>             path          = new Stack<String>();

    SimpleFeatureType                 featureType;

    // private boolean inFeatures = false;

    private Map<String,Class<?>>      propertyTypes = new LinkedHashMap<String,Class<?>>();

    private boolean                   inProperties;

    // private String currentProp;

    private CoordinateReferenceSystem crs;

    private boolean                   nullValuesEncoded;

    private GeometryDescriptor        geom;

    private final String              schemaName;

    private FeatureHandler            featureHandler;


    public LaxFeatureTypeHandler( final String schemaName, CoordinateReferenceSystem coordinateReferenceSystem ) {
        this.schemaName = schemaName;
        this.nullValuesEncoded = false;
        this.crs = coordinateReferenceSystem;
    }


    @Override
    public boolean startObjectEntry( String key ) throws ParseException,
            IOException {
        System.out.println( "startObjectEntry: " + path + " with key " + key );

        if (!path.isEmpty() && path.peek().equals( "properties" ) && delegate != NULL
                && delegate instanceof FeatureHandler) {
            inProperties = true;
            if (!propertyTypes.containsKey( key )) {
                // found previously unknown property
                propertyTypes.put( key, Object.class );
            }
        }

        path.push( key );

        if ("crs".equals( key )) {
            delegate = new CRSHandler();
            return true;
        }
        else if ("features".equals( key )) {
            featureHandler = new FeatureHandler( null, new DefaultAttributeIO() );
            if (crs != null) {
                featureHandler.setCRS( crs );
            }
            delegate = featureHandler;
            // inFeatures = true;
            return true;
        }
        return super.startObjectEntry( key );
    }
    //
    //
    // @Override
    // public boolean startArray() throws ParseException, IOException {
    //
    // /*
    // * Use GeoJSONFeatureHandler for the first feature only, to initialize the
    // property
    // * list and obtain the geometry attribute descriptor
    // */
    // if (delegate == UNINITIALIZED) {
    // delegate =
    // return true;
    // }
    //
    // return super.startArray();
    // }


    @Override
    public boolean endObject() throws ParseException, IOException {
        System.out.println( "endObject: " + path );
        super.endObject();

        if (delegate instanceof CRSHandler) {
            CoordinateReferenceSystem coordinateReferenceSystem = ((CRSHandler)delegate).getValue();
            if (coordinateReferenceSystem != null) {
                crs = coordinateReferenceSystem;
                if (featureHandler != null) {
                    featureHandler.setCRS( crs );
                }
            }
        }
        if (delegate instanceof FeatureHandler) {
            // obtain a type from the first feature
            delegate.endObject();
            SimpleFeature feature = ((FeatureHandler)delegate).getValue();
            if (feature != null) {
                if (geom == null && feature.getFeatureType().getGeometryDescriptor() != null) {
                    geom = feature.getFeatureType().getGeometryDescriptor();
                }
                List<AttributeDescriptor> attributeDescriptors = feature.getFeatureType().getAttributeDescriptors();
                for (AttributeDescriptor ad : attributeDescriptors) {
                    if (!ad.equals( geom ) && (!propertyTypes.containsKey( ad.getLocalName() )
                            || propertyTypes.get( ad.getLocalName() ) == Object.class)) {
                        propertyTypes.put( ad.getLocalName(), ad.getType().getBinding() );
                    }
                }
                // delegate = NULL;

                if (foundAllValues()) {
                    buildType();
                    return false;
                }
            }
            // create a new builder
            featureHandler = new FeatureHandler( null, new DefaultAttributeIO() );
            if (crs != null) {
                featureHandler.setCRS( crs );
            }
            delegate = featureHandler;
        }
        return true;
    }


    @Override
    public boolean primitive( Object value ) throws ParseException, IOException {
        System.out.println( "primitive: " + value + " in " + path );

        if (value != null && inProperties) {
            Class<?> newType = value.getClass();
            String currentProp = path.peek();
            if (currentProp != null) {
                Class<?> knownType = propertyTypes.get( currentProp );
                // sst change here
                if (knownType == Object.class || (knownType == Long.class && newType == Double.class)) {
                    propertyTypes.put( currentProp, newType );

                    if (foundAllValues()) {
                        // found the last unknown type, stop parsing
                        buildType();
                        return false;
                    }
                }
                else if (knownType == Double.class && newType == Long.class) {
                    // do nothing
                }
                else if (knownType != newType) {
                    throw new IllegalStateException( "Found conflicting types "
                            + knownType.getSimpleName() + " and " + newType.getSimpleName()
                            + " for property " + currentProp );
                }
            }
        }

        return super.primitive( value );
    }


    /*
     * When null values are encoded there's the possibility of stopping the parsing
     * earlier, i.e.: as soon as all data types and the crs are found.
     */
    private boolean foundAllValues() {
        return nullValuesEncoded && geom != null && crs != null
                && !thereAreUnknownDataTypes();
    }


    private boolean thereAreUnknownDataTypes() {

        for (Class<?> clazz : propertyTypes.values()) {
            if (clazz == Object.class) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean endObjectEntry() throws ParseException, IOException {

        System.out.println( "endObjectEntry: " + path );
        super.endObjectEntry();

        // remove CRS Handler
        if (path.peek().equals( "crs" ) && delegate instanceof CRSHandler) {
            CoordinateReferenceSystem coordinateReferenceSystem = ((CRSHandler)delegate).getValue();
            if (coordinateReferenceSystem != null) {
                crs = coordinateReferenceSystem;
            }
            delegate = featureHandler;
            if (featureHandler != null) {
                featureHandler.setCRS( crs );
            }
            delegate = featureHandler;
        }
        else if (path.peek().equals( "properties" )) {
            inProperties = false;
        }
        path.pop();
        return true;
    }


    @Override
    public void endJSON() throws ParseException, IOException {
        buildType();
    }


    private void buildType() {

        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName( new NameImpl( schemaName ) );

        typeBuilder.add( geom.getLocalName(), geom.getType().getBinding(), geom.getType().getCoordinateReferenceSystem() );

        if (propertyTypes != null) {
            Set<Entry<String,Class<?>>> entrySet = propertyTypes.entrySet();
            for (Entry<String,Class<?>> entry : entrySet) {
                Class<?> binding = entry.getValue();
                if (binding.equals( Object.class )) {
                    binding = String.class;
                }
                typeBuilder.add( entry.getKey(), binding );
            }
        }

        if (crs != null) {
            typeBuilder.setCRS( crs );
        }

        featureType = typeBuilder.buildFeatureType();
    }


    @Override
    public SimpleFeatureType getValue() {
        return featureType;
    }
}