/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.data.importer.shapefile;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import java.io.File;
import java.nio.charset.Charset;

import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.polymap.core.data.util.Geometries;

import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.utils.FilteredListPromptUIBuilder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CrsPrompt {

    private static Log log = LogFactory.getLog( CrsPrompt.class );
    
    private ImporterSite                site;

    private CoordinateReferenceSystem   selection;

    private HashMap<String,String>      crsNames;


    public CrsPrompt( ImporterSite site, List<File> files ) {
        this.site = site;
        
        initCrsNames();
        
        // read *.prj file
        Optional<File> prjFile = files.stream().filter( f -> "prj".equalsIgnoreCase( getExtension( f.getName() ) ) ).findAny();
        String readError = null;
        try {
            if (prjFile.isPresent()) {
                // encoding used in geotools' PrjFileReader
                String wkt = FileUtils.readFileToString( prjFile.get(), Charset.forName( "ISO-8859-1" ) );
                selection = ReferencingFactoryFinder.getCRSFactory( null ).createFromWKT( wkt );
            }
        }
        catch (Exception e) {
            readError = e.getMessage();
        }

        site.newPrompt( "crs" )
                .summary.put( "CRS" )
                .description.put( readError != null ? "Unable to read *.prj: "+readError : "The Coordinate Reference System." )
                .value.put( selection != null ? crsName( selection ) : "???" )
                .severity.put( selection != null ? Severity.VERIFY : Severity.REQUIRED )
                .extendedUI.put( new FilteredListPromptUIBuilder() {
                    
                    @Override
                    public void submit( ImporterPrompt prompt ) {
                        prompt.ok.set( true );
                        prompt.value.put( crsName( selection ) );
                    }
                    
                    @Override
                    protected Set<String> listItems() {
                        return new HashSet( crsNames.values() );  //CRS.getSupportedCodes( "EPSG" );
                    }
                    
                    @Override
                    protected List<String> filterSelectable( String text ) {
                        List<String> result = super.filterSelectable( text );
                        return result.size() > 100 ? result.subList( 0, 100 ) : result;
                    }

                    @Override
                    protected String initiallySelectedItem() {
                        return selection != null ? crsName( selection ) : "???";
                    }
                    
                    @Override
                    protected void handleSelection( String selected ) {
                        try {
                            String code = crsNames.entrySet().stream()
                                    .filter( entry -> entry.getValue().equals( selected ) )
                                    .map( entry -> entry.getKey() )
                                    .findAny().get();
                            selection = Geometries.crs( code );
                            assert selection != null;
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                });
    }

    
    /**
     * The selected {@link CoordinateReferenceSystem}. 
     */
    public CoordinateReferenceSystem selection() {
        return selection;
    }


    protected String crsName( CoordinateReferenceSystem crs ) {
        String code = CRS.toSRS( crs );
        return crsNames.computeIfAbsent( code, key -> key );
    }
    

    /**
     * All CRS Names from all available CRS authorities.
     */
    protected void initCrsNames() {
        crsNames = new HashMap<String,String>();
        Set<String> descriptions = new TreeSet<String>();
        for (Object object : ReferencingFactoryFinder.getCRSAuthorityFactories( null )) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory)object;
            try {
                Set<String> codes = factory.getAuthorityCodes( CoordinateReferenceSystem.class );
                for (Object codeObj : codes) {
                    String code = (String)codeObj;
                    
                    // XXX falko: dirty hack to allow just EPSG:XXX CRSs
                    if (code != null && code.startsWith( "EPSG" )) {
                        try {
                            String description = Joiner.on( "" ).join( factory.getDescriptionText( code ).toString(), " (", code, ")" );
                            descriptions.add( description );
                            crsNames.put( code, description );
                        }
                        catch (Exception e1) {
                            // XXX falko: no UNNAMED CRSs
                            continue;
                        }
                    }
                }
            }
            catch (FactoryException e) {
                throw new RuntimeException( e );
            }
        }
    }

}
