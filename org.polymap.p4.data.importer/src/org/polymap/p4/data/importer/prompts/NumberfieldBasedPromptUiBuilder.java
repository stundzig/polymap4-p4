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
package org.polymap.p4.data.importer.prompts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.polymap.core.data.refine.impl.LineBasedFormatAndOptions;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.refine.AbstractRefineFileImporter;

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
    public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
        parent.setLayout( new FormLayout() );
        // TODO add numbervalidator here
        Text text = tk.createText( parent, String.valueOf( initialValue() ), SWT.RIGHT | SWT.BORDER );
        FormDataFactory.on( text ).left( 1 ).top( 5 ).width( 350 );

        text.addModifyListener( event -> {
            Text t = (Text)event.getSource();
            // can throw an exception
            try {
                value = Integer.parseInt( t.getText() );
            }
            catch (Exception e) {
                // do nothing
            }
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
        importer.triggerUpdateOptions();
        prompt.ok.set( true );
        prompt.value.set( String.valueOf( value ) );
    }
}
