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
package org.polymap.p4.data.imports.shapefile;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.List;
import java.util.Set;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Streams;
import org.polymap.core.runtime.Streams.ExceptionCollector;

import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.utils.FilteredListPromptUIBuilder;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CharsetPrompt {

    private static Log log = LogFactory.getLog( CharsetPrompt.class );
    
    private static Charset      DEFAULT  = Charset.forName( "ISO-8859-1" );

    private ImporterSite        site;

    private Charset             selection = DEFAULT;


    public CharsetPrompt( ImporterSite site, List<File> files ) {
        this.site = site;
        
        try (ExceptionCollector<RuntimeException> exc = Streams.exceptions()) {
            selection = Charset.forName( files.stream()
                    .filter( f -> "cpg".equalsIgnoreCase( getExtension( f.getName() ) ) ).findAny()
                    .map( f -> exc.check( () -> readFileToString( f ).trim() ) )
                    .orElse( DEFAULT.name() ) );
        }

        site.newPrompt( "charset" )
                .summary.put( "Content encoding" )
                .description.put( "The encoding of the feature content. If unsure use ISO-8859-1." )
                .value.put( selection.name() )
                .severity.put( Severity.VERIFY ).extendedUI.put( new FilteredListPromptUIBuilder() {
                    @Override
                    public void submit( ImporterPrompt prompt ) {
                        prompt.ok.set( true );
                        prompt.value.put( selection.displayName() );
                    }

                    @Override
                    protected String[] listItems() {
                        Set<String> charsetCodes = Charset.availableCharsets().keySet();
                        return charsetCodes.toArray( new String[charsetCodes.size()] );
                    }

                    @Override
                    protected String initiallySelectedItem() {
                        return selection.displayName();
                    }

                    @Override
                    protected void handleSelection( String selectedCharset ) {
                        selection = Charset.forName( selectedCharset );
                        assert selection != null;
                    }
                } );
    }

    
    /**
     * The selected {@link Charset}. 
     */
    public Charset selection() {
        return selection;
    }
    
}
