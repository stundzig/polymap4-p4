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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.shapefile.ShpFeatureTableViewer;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class GeojsonImporter
        implements Importer {

    private ImporterSite      site             = null;

    @ContextIn
    protected File            geojsonFile;

    private FeatureCollection features         = null;

    private Exception         exception        = null;

    private CharSetSelection  charSetSelection = new CharSetSelection();

    private CRSSelection      crsSelection     = new CRSSelection();


    @Override
    public void init( ImporterSite site, IProgressMonitor monitor ) throws Exception {
        this.site = site;
        site.icon.set( P4Plugin.images().svgImage( "shp.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( "GeoJSON file: " + geojsonFile.getName() );
        site.description.set( "GeoJSON is a format for encoding a variety of geographic data structures." );
        site.terminal.set( true );
    }


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        site.newPrompt( "charset" ).summary.put( "GeoJSON file content encoding" ).description
                .put( "The encoding of the GeoJSON file content. If unsure use UTF-8." ).value
                .put( "UTF-8" ).severity
                .put( Severity.VERIFY ).extendedUI.put( new CharsetPromptBuilder( charSetSelection ) );
        // http://geojson.org/geojson-spec.html#coordinate-reference-system-objects
        CoordinateReferenceSystem predefinedCRS = getPredefinedCRS();
        if (predefinedCRS != null) {
            crsSelection.setSelected( predefinedCRS );
        }
        else {
            site.newPrompt( "crs" ).summary.put( "Coordinate reference system" ).description
                    .put( "The coordinate reference system for projecting the feature content. "
                            + "If unsure use EPSG:4326 (= WGS 84)." ).value
                    .put( "EPSG:4326" ).severity.put( Severity.VERIFY ).extendedUI
                    .put( new CRSPromptBuilder( crsSelection ) );
        }
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        String encoding = null;
        if (charSetSelection
                .getSelected() != null) {
            encoding = charSetSelection.getSelected().name();
        }
        else {
            encoding = charSetSelection.getDefault().getKey();
        }
        FeatureJSON featureJSON = new FeatureJSON();
        try (InputStreamReader isr = new InputStreamReader( new FileInputStream( geojsonFile ), encoding )) {
            features = featureJSON.readFeatureCollection( isr );
            if (crsSelection.getSelected() != null) {
                FeatureType schema = features.getSchema();
                if (schema instanceof SimpleFeatureType) {
                    SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype( (SimpleFeatureType)schema,
                            crsSelection.getSelected() );
                    if (features instanceof SimpleFeatureCollection) {
                        ListFeatureCollection listFeatures = new ListFeatureCollection( featureType );
                        SimpleFeatureIterator iterator = ((SimpleFeatureCollection)features).features();
                        while (iterator.hasNext()) {
                            listFeatures.add( iterator.next() );
                        }
                        features = listFeatures;
                    }
                }
            }
        }
        catch (IOException e) {
            exception = e;
        }
    }


    private CoordinateReferenceSystem getPredefinedCRS() {
        CoordinateReferenceSystem predefinedCRS = null;
        JSONParser parser = new JSONParser();
        try (InputStreamReader isr2 = new InputStreamReader( new FileInputStream( geojsonFile ) )) {
            JSONObject root = (JSONObject)parser.parse( isr2 );
            // http://www.macwright.org/2015/03/23/geojson-second-bite.html#coordinate
            JSONObject crs = (JSONObject)root.get( "crs" );
            if (crs != null) {
                String type = (String)crs.get( "type" );
                if ("link".equals( type )) {
                    JSONObject properties = (JSONObject)crs.get( "properties" );
                    if (properties != null) {
                        // TODO: handle CRS link
                        // String linkType = properties.getString( "type" );
                        // String href = properties.getString( "href" );
                    }
                }
                else if ("name".equals( type )) {
                    JSONObject properties = (JSONObject)crs.get( "properties" );
                    if (properties != null) {
                        String name = (String)properties.get( "name" );
                        if (name != null) {
                            try {
                                // http://www.geotoolkit.org/modules/referencing/faq.html#lookupURN
                                predefinedCRS = CRS.decode( name, false );
                            }
                            catch (FactoryException e) {
                                exception = e;
                            }
                        }
                    }
                }
            }
        }
        catch (IOException ioe) {
            exception = ioe;
        }
        catch (ParseException pe) {
            exception = pe;
        }
        return predefinedCRS;
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit toolkit ) {
        if (exception != null) {
            toolkit.createFlowText( parent, "\nUnable to read the data.\n\n" + "**Reason**: " + exception.getMessage() );
        }
        else {
            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // TODO Auto-generated method stub

    }
}