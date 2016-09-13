/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.geojson;

import java.util.Collection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import org.apache.commons.io.FilenameUtils;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ContextOut;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterPlugin;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.prompts.CharsetPrompt;
import org.polymap.p4.data.importer.prompts.CrsPrompt;
import org.polymap.p4.data.importer.prompts.SchemaNamePrompt;
import org.polymap.p4.data.importer.shapefile.ShpFeatureTableViewer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author Steffen Stundzig
 */
public class GeoJSONImporter
        implements Importer {

    private ImporterSite        site      = null;

    @ContextIn
    protected File              geojsonFile;

    @ContextOut
    protected FeatureCollection features  = null;

    private Exception           exception = null;

    private CrsPrompt           crsPrompt;

    private CharsetPrompt       charsetPrompt;

    private SchemaNamePrompt    schemaNamePrompt;


    @Override
    public void init( ImporterSite importerSite, IProgressMonitor monitor ) throws Exception {
        this.site = importerSite;
        importerSite.icon.set( ImporterPlugin.images().svgImage( "json.svg", SvgImageRegistryHelper.NORMAL24 ) );
        importerSite.summary.set( "GeoJSON file: " + geojsonFile.getName() );
        importerSite.description.set( "GeoJSON is a format for encoding a variety of geographic data structures." );
        importerSite.terminal.set( true );
    }


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        charsetPrompt = new CharsetPrompt( site, "Content encoding", "The encoding of the feature content. If unsure use UTF-8.", () -> {
            return Charset.forName( "UTF-8" );
        } );
        // http://geojson.org/geojson-spec.html#coordinate-reference-system-objects
        crsPrompt = new CrsPrompt( site, "Coordinate reference system", "The coordinate reference system for projecting the feature content."
                + "If unsure use EPSG:4326.", () -> {
                    return getPredefinedCRS();
                } );
        schemaNamePrompt = new SchemaNamePrompt( site, "Name of the dataset ", "Each dataset has its own name in the local database. The name must be unqiue.", () -> {
            return FilenameUtils.getBaseName( geojsonFile.getName() );
        } );
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        System.err.println( "verify " + System.currentTimeMillis() );
        GeoJSONFeatureIterator featureIterator = new GeoJSONFeatureIterator( geojsonFile, charsetPrompt.selection(), schemaNamePrompt.selection(), crsPrompt.selection(), monitor );
        try {

            ListFeatureCollection featureList = new ListFeatureCollection( featureIterator.getFeatureType() );
            int i = 0;
            while (i < 100 && featureIterator.hasNext()) {
                SimpleFeature next = featureIterator.next();
                featureList.add( next );
                i++;
            }
            features = featureList;
            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site.ok.set( false );
            exception = e;
            e.printStackTrace();
        }
        finally {
            if (featureIterator != null) {
                featureIterator.close();
            }
        }
        System.err.println( "verify done " + System.currentTimeMillis() );
    }


    CoordinateReferenceSystem getPredefinedCRS() {
        CoordinateReferenceSystem predefinedCRS = null;
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader( new FileInputStream( geojsonFile ), CharsetPrompt.DEFAULT );
            FeatureJSON featureJSON = new FeatureJSON();
            predefinedCRS = featureJSON.readCRS( isr );
        }
        catch (Exception ioe) {
            exception = ioe;
        }
        finally {
            if (isr != null) {
                try {
                    isr.close();
                }
                catch (IOException e) {
                    // do nothing
                }
            }
        }
        if (predefinedCRS == null) {
            try {
                predefinedCRS = CRS.decode( "EPSG:4326" );
            }
            catch (Exception e) {
                // do nothing
            }
        }
        return predefinedCRS;
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit toolkit ) {
        if (exception != null) {
            toolkit.createFlowText( parent, "\nUnable to read the data.\n\n" + "**Reason**: "
                    + exception.getMessage() );
        }
        else {
            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        final GeoJSONFeatureIterator featureIterator = new GeoJSONFeatureIterator( geojsonFile, charsetPrompt.selection(), schemaNamePrompt.selection(), crsPrompt.selection(), monitor );
        features = new FeatureCollection() {

            @Override
            public FeatureIterator features() {
                return featureIterator;
            }


            @Override
            public FeatureType getSchema() {
                return featureIterator.getFeatureType();
            }


            @Override
            public String getID() {
                return null;
            }


            @Override
            public void accepts( FeatureVisitor visitor, ProgressListener progress ) throws IOException {
                FeatureIterator iterator = features();
                while (iterator.hasNext()) {
                    visitor.visit( iterator.next() );
                }
            }


            @Override
            public FeatureCollection subCollection( Filter filter ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public FeatureCollection sort( SortBy order ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public ReferencedEnvelope getBounds() {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public boolean contains( Object o ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public boolean containsAll( Collection o ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public boolean isEmpty() {
                return false;
            }


            @Override
            public int size() {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public Object[] toArray() {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }


            @Override
            public Object[] toArray( Object[] a ) {
                // TODO Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }

        };
    }
}