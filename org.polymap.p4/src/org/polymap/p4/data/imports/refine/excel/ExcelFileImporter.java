/*
 * polymap.org 
 * Copyright (C) @year@ individual contributors as indicated by the @authors tag. 
 * All rights reserved.
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
package org.polymap.p4.data.imports.refine.excel;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.polymap.core.data.refine.impl.ExcelFormatAndOptions;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class ExcelFileImporter
        extends AbstractRefineFileImporter<ExcelFormatAndOptions> {

    private static Log log = LogFactory.getLog( ExcelFileImporter.class );


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor)
            throws Exception {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "xls.svg", NORMAL24 ) );
        site.summary.set( "Excel file: " + file.getName() );
        // site.description.set("Description");

        super.init( site, monitor );
        formatAndOptions().setSheets( 0 );
        updateOptions();
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        if (formatAndOptions().sheetRecords().size() == 1) {
            site.newPrompt( "headline" ).summary.put( "Kopfzeile" ).description
                    .put( "Welche Zeile enhält die Spaltenüberschriften?" ).extendedUI
                            .put( new PromptUIBuilder() {
                                
                                @Override
                                public void submit(ImporterPrompt prompt) {
                                    // TODO Auto-generated method stub
                                    
                                }
                                
                                @Override
                                public void createContents(ImporterPrompt prompt, Composite parent) {
                                 // TODO use a rhei numberfield here
                                    Text text = new Text( parent, SWT.RIGHT );
                                    text.setText( formatAndOptions().headerLines() );
                                    text.addModifyListener( event -> {
                                        Text t = (Text)event.getSource();
                                        // can throw an exception
                                        int index = Integer.parseInt( t.getText() );
                                        formatAndOptions().setHeaderLines( index );
                                        updateOptions();
                                        prompt.ok.set( true );
                                    } );
                                }
                            });
        }
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (formatAndOptions().sheetRecords().size() > 1) {
            // select the correct sheet
//            Label label = new Label( parent, SWT.LEFT );
//            label.setText( "Das Excel Dokument enthält mehrere Arbeitsblätter. Bitte wählen Sie die Arbeitsblätter die sie importieren möchten." );
//            FormDataFactory.on( label );
            org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List( parent,
                    SWT.SINGLE );
            
//            FormDataFactory.on( list ).fill().top( label, 5 ).bottom( 90, -5 ).height( 50 ).width( 300 );
            formatAndOptions().sheetRecords().forEach( record -> list.add( record.name + ": " + record.rows + " zeilen" ) );
            list.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    // TODO open the sheet in a new importer window, copy also the base file on the second selection to avoid side effects
                    log.error( "open in new importer panel, similar to the archive importer" );
                }
            } );
        }
        else {
            super.createResultViewer( parent, tk );
        }
    }


    @Override
    protected ExcelFormatAndOptions defaultOptions() {
        return ExcelFormatAndOptions.createDefault();
    }

}
