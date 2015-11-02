/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data.imports.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeatureType;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.imports.utils.ListPromptUIBuilder;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShpImporter
        implements Importer {

    private static Log                             log                   = LogFactory.getLog( ShpImporter.class );

    private static String                          DEFAULT_CHARSET_CODE  = "ISO-8859-1";

    private static final ShapefileDataStoreFactory dsFactory             = new ShapefileDataStoreFactory();

    private ImporterSite                           site;

    @ContextIn
    protected List<File>                           files;

    @ContextIn
    protected File                                 shp;

    @ContextOut
    private FeatureCollection                      features;

    private Exception                              exception;

    private ShapefileDataStore                     ds;

    private Charset                                featureContentCharset = Charset.forName( DEFAULT_CHARSET_CODE );


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding") ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;
        site.icon.set( P4Plugin.images().svgImage( "shp.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( "Shapefile: " + shp.getName() );
        site.description.set( "A Shapefile is a common file format which contains features of the same type." );
        site.terminal.set( true );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        createNewDataStore();

        handleCharset();
    }


    private void handleCharset() {
        String predefinedCharsetCode = readoutPredefinedCharsetCode();
        if(predefinedCharsetCode != null) {
            featureContentCharset = Charset.forName( predefinedCharsetCode );
        } else {
            site.newPrompt( "charset" ).summary.put( "Feature content encoding" ).description
                    .put( "The encoding of the feature content. If unsure use ISO-8859-1." ).value
                    .put( DEFAULT_CHARSET_CODE ).severity
                    .put( Severity.VERIFY ).extendedUI.put( new ListPromptUIBuilder() {

                private String charsetCode = null;


                @Override
                public void submit( ImporterPrompt prompt ) {
                    featureContentCharset = Charset.forName( charsetCode );
                    prompt.ok.set( true );
                    prompt.value.put( featureContentCharset.displayName() );
                }


                @Override
                protected String[] getListItems() {
                    return getCharsetCodes();
                }


                @Override
                protected String getInitiallySelectedItem() {
                    return DEFAULT_CHARSET_CODE;
                }


                @Override
                protected void handleSelection( String selectedCharset ) {
                    charsetCode = selectedCharset;
                }
            } );
        }
    }


    private String readoutPredefinedCharsetCode() {
        String predefinedCharsetCode = null;
        Optional<File> cpgFile = findFileWithExtension( "cpg" );
        if (cpgFile.isPresent()) {
            try {
                String content = FileUtils.readFileToString( cpgFile.get() );
                Optional<String> providedCharset = Arrays.asList( getCharsetCodes() ).stream()
                        .filter( charsetCode -> content.trim().equalsIgnoreCase( charsetCode ) ).findFirst();
                if (providedCharset.isPresent()) {
                    predefinedCharsetCode = providedCharset.get();
                }
            }
            catch (Exception e) {
                site.ok.set( false );
                exception = e;
            }
        }
        return predefinedCharsetCode;
    }


    private String[] getCharsetCodes() {
        Set<String> charsetCodes = Charset.availableCharsets().keySet();
        return charsetCodes.toArray( new String[charsetCodes.size()] );
    }


    private void createNewDataStore() throws MalformedURLException, IOException {
        try {
            if (ds != null) {
                ds.dispose();
            }
            Map<String,Serializable> params = new HashMap<String,Serializable>();
            params.put( "url", shp.toURI().toURL() );
            params.put( "create spatial index", Boolean.TRUE );

            ds = (ShapefileDataStore)dsFactory.createNewDataStore( params );
        }
        catch (Exception e) {
            site.ok.set( false );
            exception = e;
        }
    }


    private Optional<File> findFileWithExtension( String extension ) {
        return files.stream()
                .filter( file -> extension.equalsIgnoreCase( FilenameUtils.getExtension( file.getName() ) ) )
                .findFirst();
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            ds.setCharset( featureContentCharset );
            Query query = new Query();
            query.setMaxFeatures( 10 );
            features = ds.getFeatureSource().getFeatures( query );
            features.accepts( f -> log.info( "Feature: " + f ), null );

            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site.ok.set( false );
            exception = e;
        }
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (exception != null) {
            tk.createFlowText( parent,
                    "\nUnable to read the data.\n\n" +
                            "**Reason**: " + exception.getMessage() );
        }
        else {
            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            log.info( "Features: " + features.size() + " : " + schema.getTypeName() );
            // tk.createFlowText( parent, "Features: *" + features.size() + "*" );
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // everything done in verify()
    }

}
