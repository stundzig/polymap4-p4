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

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.ImporterPrompt.Severity;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.data.importer.Messages;

/**
 * Prompt toshow duplicate schema names *before* importing them.
 * 
 * @author Steffen Stundzig
 */
public class SchemaNamePrompt {

    private static final IMessages    i18n       = Messages.forPrefix( "SchemaNamePrompt" );
    
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
                Label desc = FormDataFactory.on( tk.createLabel( parent, prompt.description.get(), SWT.WRAP ) ).top( 0 ).left( 0 ).width( 350 ).control();
                Label l = tk.createLabel( parent, "", SWT.WRAP );
                // XXX use form here, but without label. I don't know, how do suppress the label.
//                BatikFormContainer form = new BatikFormContainer( new DefaultFormPage() {
//                    
//                    @Override
//                    public void createFormContents( IFormPageSite site ) {
//                        site.newFormField( new PlainValuePropertyAdapter( "name", currentName ) )
//                        .field.put( new StringFormField() ).validator.put( new NotEmptyValidator() {
//                            @Override
//                            public String validate( Object fieldValue ) {
//                                currentName = (String)fieldValue;
//                                if (nameExists()) {
//                                    return i18n.get( "wrongName" );
//                                }
//                                return null;
//                            }
//                        } )
//                        
//                        .label.put( "label" ).tooltip.put( "tooltip" )
//                        .create();
//                    }
//                } );
//                form.createContents( parent );
//                
//                FormDataFactory.on( form.getContents() ).left( 1 ).top( desc, 15 ).width( 350 );

                Text text = tk.createText( parent, currentName, SWT.BORDER );
                text.addModifyListener( event -> {
                    Text t = (Text)event.getSource();
                    currentName = t.getText();
                    checkName( prompt, l );
                } );

//                FormDataFactory.on( text ).left( 1 ).top( form.getContents(), 15 ).width( 350 );
                FormDataFactory.on( text ).left( 1 ).top( desc, 15 ).width( 350 );
                FormDataFactory.on( l ).left( 1 ).top( text, 15 ).width( 350 );

                checkName( prompt, l );
            }


            private void checkName( ImporterPrompt prompt, Label l ) {
                if (nameExists()) {
                    l.setText( i18n.get( "wrongName" ) );
                    prompt.ok.set( false );
                }
                else {
                    l.setText( i18n.get( "rightName" ) );
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
