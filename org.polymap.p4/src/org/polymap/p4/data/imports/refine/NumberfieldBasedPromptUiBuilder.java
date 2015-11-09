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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.polymap.core.data.refine.impl.LineBasedFormatAndOptions;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;

/**
 * An abstract Builder for different prompts in the CSV and Excel imports.
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public abstract class NumberfieldBasedPromptUiBuilder
        implements PromptUIBuilder {

    protected int                                                             value;

    protected AbstractRefineFileImporter<? extends LineBasedFormatAndOptions> importer;


    public NumberfieldBasedPromptUiBuilder( AbstractRefineFileImporter<? extends LineBasedFormatAndOptions> importer ) {
        this.importer = importer;
    }


    @Override
    public void createContents( ImporterPrompt prompt, Composite parent ) {
        // TODO use a rhei numberfield here
        Text text = new Text( parent, SWT.RIGHT | SWT.BORDER );

        text.setText( String.valueOf( initialValue() ) );
        text.addModifyListener( event -> {
            Text t = (Text)event.getSource();
            // can throw an exception
            value = Integer.parseInt( t.getText() );
        } );
        // initial value
        value = Integer.parseInt( text.getText() );
        prompt.value.set( text.getText() );
    }


    protected abstract int initialValue();


    protected abstract void onSubmit( ImporterPrompt prompt );


    @Override
    public void submit( ImporterPrompt prompt ) {
        onSubmit( prompt );
        importer.updateOptions();
        prompt.ok.set( true );
        prompt.value.set( String.valueOf( value ) );
    }
}
