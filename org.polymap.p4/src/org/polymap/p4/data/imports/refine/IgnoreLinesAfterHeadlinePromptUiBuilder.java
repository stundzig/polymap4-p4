package org.polymap.p4.data.imports.refine;

import org.polymap.core.data.refine.impl.LineBasedFormatAndOptions;
import org.polymap.p4.data.imports.ImporterPrompt;

/**
 * A prompt to select the number of rows to skip after the headline, needed for CSV
 * and EXCEL files.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class IgnoreLinesAfterHeadlinePromptUiBuilder
        extends NumberfieldBasedPromptUiBuilder {

    public IgnoreLinesAfterHeadlinePromptUiBuilder( AbstractRefineFileImporter<? extends LineBasedFormatAndOptions> importer ) {
        super( importer );
    }


    @Override
    public void submit( ImporterPrompt prompt ) {
        importer.formatAndOptions().setSkipDataLines( value );
        super.submit( prompt );
    }


    @Override
    protected int initialValue() {
        return importer.formatAndOptions().skipDataLines();
    }
}
