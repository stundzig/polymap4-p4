/*
 * polymap.org 
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data.importer.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Composite;

import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.util.ProgressListener;

import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;
import org.polymap.core.runtime.i18n.IMessages;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.table.FeatureCollectionContentProvider;
import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.ContextOut;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterPlugin;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.Messages;
import org.polymap.p4.data.importer.prompts.CharsetPrompt;
import org.polymap.p4.data.importer.prompts.CrsPrompt;
import org.polymap.p4.data.importer.prompts.SchemaNamePrompt;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ShpImporter
        implements Importer {

    private static final IMessages                 i18nPrompt = Messages.forPrefix( "ImporterPrompt" );

    private static final IMessages                 i18n = Messages.forPrefix( "ImporterShp" );

    private static Log                             log        = LogFactory.getLog( ShpImporter.class );

    private static final ShapefileDataStoreFactory dsFactory  = new ShapefileDataStoreFactory();

    private ImporterSite                           site;

    @ContextIn
    protected List<File>                           files;

    @ContextIn
    protected File                                 shp;

    @ContextOut
    private FeatureCollection                      features;

    private Exception                              exception;

    private ShapefileDataStore                     ds;

    private ContentFeatureSource                   fs;

    private CharsetPrompt                          charsetPrompt;

    private CrsPrompt                              crsPrompt;

    private SchemaNamePrompt schemaNamePrompt;


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    @SuppressWarnings( "hiding" )
    public void init( ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;
        site.icon.set( ImporterPlugin.images().svgImage( "shp.svg", SvgImageRegistryHelper.NORMAL24 ) );
        site.summary.set( i18n.get( "summary", shp.getName() ) );
        site.description.set( i18n.get( "description") );
        site.terminal.set( true );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        charsetPrompt = new CharsetPrompt( site, i18nPrompt.get("encodingSummary"), i18nPrompt.get( "encodingDescription" ), () -> {
            Charset crs = null;
            try (ExceptionCollector<RuntimeException> exc = Streams.exceptions()) {
                crs = Charset.forName( files.stream()
                        .filter( f -> "cpg".equalsIgnoreCase( getExtension( f.getName() ) ) ).findAny()
                        .map( f -> exc.check( () -> readFileToString( f ).trim() ) )
                        .orElse( CharsetPrompt.DEFAULT.name() ) );
            }
            return crs;
        } );
        
        crsPrompt = new CrsPrompt( site, i18nPrompt.get("crsSummary"), i18nPrompt.get( "crsDescription" ), () -> {
            Optional<File> prjFile = files.stream().filter( f -> "prj".equalsIgnoreCase( FilenameUtils.getExtension( f.getName() ) ) ).findAny();
            if (prjFile.isPresent()) {
                try {
                    // encoding used in geotools' PrjFileReader
                    String wkt = FileUtils.readFileToString( prjFile.get(), Charset.forName( "ISO-8859-1" ) );
                    return ReferencingFactoryFinder.getCRSFactory( null ).createFromWKT( wkt );
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            }
            return null;
        } );
        schemaNamePrompt = new SchemaNamePrompt( site, i18nPrompt.get("schemaSummary"), i18nPrompt.get( "schemaDescription" ), () -> {
            return FilenameUtils.getBaseName( shp.getName() );
        } );
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            if (ds != null) {
                ds.dispose();
            }
            Map<String,Serializable> params = new HashMap<String,Serializable>();
            params.put( "url", shp.toURI().toURL() );
            params.put( "create spatial index", Boolean.TRUE );

            ds = (ShapefileDataStore)dsFactory.createNewDataStore( params );
            ds.setCharset( charsetPrompt.selection() );
            ds.forceSchemaCRS( crsPrompt.selection() );
            
            fs = ds.getFeatureSource();
            
            // check all features
            try (
                SimpleFeatureIterator it = fs.getFeatures().features();
            ){
                SimpleFeatureType schema = fs.getSchema();
                while (it.hasNext()) {
                    SimpleFeature feature = it.next();
                    // geometry
                    if (schema.getGeometryDescriptor() != null
                            && feature.getDefaultGeometry() == null) {
                        throw new RuntimeException( "Feature has no geometry: " + feature.getIdentifier().getID() );
                    }
                    // other checks...?
                }
            }
            log.info( "Verified: ok" );

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
            tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
        }
        else {
            try {
                SimpleFeatureType schema = (SimpleFeatureType)fs.getSchema();
                //log.info( "Features: " + features.size() + " : " + schema.getTypeName() );
                // tk.createFlowText( parent, "Features: *" + features.size() + "*" );
                
                ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
                table.setContentProvider( new FeatureCollectionContentProvider() );
                
                // XXX GeoTools shapefile impl does not handle setFirstResult() well
                // so we can just display 100 features :(
                Query query = new Query();
                query.setMaxFeatures( 1000 );
                ContentFeatureCollection content = fs.getFeatures( query );
                table.setInput( content );
            }
            catch (IOException e) {
                tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
            }
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // no maxResults restriction
        ContentFeatureCollection underlying = fs.getFeatures();
        SimpleFeatureType original = underlying.getSchema();
        SimpleFeatureTypeImpl schema = new SimpleFeatureTypeImpl( new NameImpl( schemaNamePrompt.selection() ), original.getAttributeDescriptors(), original.getGeometryDescriptor(), original.isAbstract(), original.getRestrictions(), original.getSuper(), original.getDescription() );
        features = new FeatureCollection() {

            @Override
            public void accepts( FeatureVisitor visitor, ProgressListener listener ) throws IOException {
                FeatureIterator iterator = features();
                while (iterator.hasNext()) {
                    // change schema and also set ID to null
                    visitor.visit( new SimpleFeatureImpl(((SimpleFeature)iterator.next()).getAttributes(), schema, null ) );
                }
            }

            @Override
            public boolean contains( Object arg0 ) {
                return underlying.contains( arg0 );
            }

            @Override
            public boolean containsAll( Collection arg0 ) {
                return underlying.containsAll( arg0 );
            }

            @Override
            public FeatureIterator features() {
                return underlying.features();
            }

            @Override
            public ReferencedEnvelope getBounds() {
                return underlying.getBounds();
            }

            @Override
            public String getID() {
                return underlying.getID();
            }

            @Override
            public FeatureType getSchema() {
                return schema;
            }

            @Override
            public boolean isEmpty() {
                return underlying.isEmpty();
            }

            @Override
            public int size() {
                return underlying.size();
            }

            @Override
            public FeatureCollection sort( SortBy arg0 ) {
                return underlying.sort( arg0 );
            }

            @Override
            public FeatureCollection subCollection( Filter arg0 ) {
                return underlying.subCollection( arg0 );
            }

            @Override
            public Object[] toArray() {
                return underlying.toArray();
            }

            @Override
            public Object[] toArray( Object[] arg0 ) {
                return underlying.toArray( arg0 );
            }
            
        };
    }

}
