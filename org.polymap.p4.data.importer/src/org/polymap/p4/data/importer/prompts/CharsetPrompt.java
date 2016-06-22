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
package org.polymap.p4.data.importer.prompts;

import java.util.Set;
import java.util.function.Supplier;

import java.nio.charset.Charset;

import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CharsetPrompt {
    
    public static Charset       DEFAULT  = Charset.forName( "ISO-8859-1" );

    private Charset             selection = null;


    public CharsetPrompt( final ImporterSite site, final String summary, final String description, Supplier<Charset> charsetSupplier ) {
        
        selection = charsetSupplier.get();
        if (selection == null) {
            selection = DEFAULT;
        }

        site.newPrompt( "charset" )
                .summary.put( summary )
                .description.put( description )
                .value.put( selection.name() )
                .severity.put( Severity.VERIFY )
                .extendedUI.put( new FilteredListPromptUIBuilder() {
                    
                    @Override
                    public void submit( ImporterPrompt prompt ) {
                        prompt.ok.set( true );
                        prompt.value.put( selection.displayName() );
                    }
                    
                    @Override
                    protected Set<String> listItems() {
                        return Charset.availableCharsets().keySet();
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
                });
    }

    
    /**
     * The selected {@link Charset}. 
     */
    public Charset selection() {
        return selection;
    }
    
}
