/*
 * polymap.org Copyright (C) @year@ individual contributors as indicated by
 * the @authors tag. All rights reserved.
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
package org.polymap.p4.data.imports.refine;

import org.polymap.core.data.refine.impl.LineBasedFormatAndOptions;
import org.polymap.p4.data.imports.ImporterPrompt;

/**
 * A prompt to select the number of rows to skip before the headline, needed for CSV
 * and EXCEL files.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class IgnoreLinesBeforeHeadlinePromptUiBuilder
        extends NumberfieldBasedPromptUiBuilder {

    public IgnoreLinesBeforeHeadlinePromptUiBuilder( AbstractRefineFileImporter<? extends LineBasedFormatAndOptions> importer ) {
        super( importer );
    }


    @Override
    public void submit( ImporterPrompt prompt ) {
        importer.formatAndOptions().setIgnoreLines( value );
        super.submit( prompt );
    }


    @Override
    protected int initialValue() {
        // initial set to -1, but means 0
        return Math.max( 0, importer.formatAndOptions().ignoreLines());
    }
}
