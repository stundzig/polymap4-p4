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
package org.polymap.p4.data.importer.prompts;

import java.util.function.Supplier;

import org.geotools.feature.NameImpl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;

/**
 * Prompt toshow duplicate schema names *before* importing them.
 * 
 * @author Steffen Stundzig
 */
public class SchemaNamePrompt {

    private String currentName = null;


    public SchemaNamePrompt( final ImporterSite site, final String summary, final String description,
            final Supplier<String> nameSupplier ) {

        currentName = nameSupplier.get();
        if (currentName == null) {
            currentName = "features";
        }

        site.newPrompt( "schemaName" )
            .summary.put( summary )
            .description.put( description )
            .value.put( currentName )
            .severity.put( Severity.REQUIRED )
            .ok.put( !nameExists() )
            .extendedUI.put( new PromptUIBuilder() {

            @Override
            public void submit( ImporterPrompt prompt ) {
                prompt.ok.set( !nameExists() );
                prompt.value.put( currentName );
            }


            @Override
            public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
                parent.setLayout( new FormLayout() );
                Label l = tk.createLabel( parent, "", SWT.NONE );
                Text text = tk.createText( parent, currentName, SWT.BORDER );

                text.addModifyListener( event -> {
                    Text t = (Text)event.getSource();
                    currentName = t.getText();
                    checkName( prompt, l );
                } );

                FormDataFactory.on( text ).left( 1 ).top( 5 ).width( 350 );
                FormDataFactory.on( l ).left( 1 ).top( text, 5 );

                checkName( prompt, l );
            }


            private void checkName( ImporterPrompt prompt, Label l ) {
                if (nameExists()) {
                    l.setText( "This name always exists in your database. Please use another name." );
                    prompt.ok.set( false );
                }
                else {
                    l.setText( "" );
                    prompt.ok.set( true );
                }
            }
        } );
    }


    protected boolean nameExists() {
        try {
            return P4Plugin.localCatalog().localFeaturesStore().getSchema( new NameImpl( currentName ) ) != null;
        }
        catch (Exception e) {
            // do nothing, exception is thrown, if schema doesnt exist
        }
        return false;
    }


    public String selection() {
        return currentName;
    }

}
