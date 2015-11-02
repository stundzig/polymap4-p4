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
package org.polymap.p4.data.imports.refine.csv;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.ComboBasedPromptUiBuilder;
import org.polymap.p4.data.imports.refine.NumberfieldBasedPromptUiBuilder;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporter
        extends AbstractRefineFileImporter<CSVFormatAndOptions> {

    private static Log log = LogFactory.getLog( CSVFileImporter.class );


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "csv.svg", NORMAL24 ) );
        site.summary.set( "CSV / TSV / separator based file: " + file.getName() );
        site.description.set( "" );

        super.init( site, monitor );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        site.newPrompt( "ignoreBeforeHeadline" ).summary.put( "Ignorieren bis zur Kopfzeile" ).description
                .put( "Wieviele Zeilen befinden sich über den Spaltenüberschriften?" ).value
                        .put( String.valueOf( Math.max( 0, formatAndOptions().ignoreLines() ) ) ).extendedUI
                                .put( new NumberfieldBasedPromptUiBuilder( this) {

                                    @Override
                                    public void onSubmit( ImporterPrompt prompt ) {
                                        formatAndOptions().setIgnoreLines( value );
                                    }


                                    @Override
                                    protected int initialValue() {
                                        return Math.max( 0, formatAndOptions().ignoreLines() );
                                    }
                                } );
        site.newPrompt( "headline" ).summary.put( "Kopfzeilen" ).description
                .put( "Wieviele Zeilen enhalten die Spaltenüberschriften?" ).value
                        .put( String.valueOf( formatAndOptions().headerLines() ) ).extendedUI
                                .put( new NumberfieldBasedPromptUiBuilder( this) {

                                    @Override
                                    public void onSubmit( ImporterPrompt prompt ) {
                                        formatAndOptions().setHeaderLines( value );
                                    }


                                    @Override
                                    protected int initialValue() {
                                        return formatAndOptions().headerLines( );
                                    }
                                } );
        site.newPrompt( "ignoreAfterHeadline" ).summary.put( "Überflüssige Datenzeilen" ).description
                .put( "Wieviele Zeilen können nach der Spaltenüberschrift ignoriert werden?" ).value
                        .put( String.valueOf( formatAndOptions().skipDataLines() ) ).extendedUI
                                .put( new NumberfieldBasedPromptUiBuilder( this) {

                                    @Override
                                    public void onSubmit( ImporterPrompt prompt ) {
                                        formatAndOptions().setSkipDataLines( value );
                                    }


                                    @Override
                                    protected int initialValue() {
                                        return formatAndOptions().skipDataLines( );
                                    }
                                } );
        site.newPrompt( "separator" ).summary.put( "Trennzeichen" ).description
                .put( "Mit welchem Zeichen werden die einzelnen Zellen getrennt?" ).value
                        .put( formatAndOptions().separator() ).extendedUI
                                .put( new ComboBasedPromptUiBuilder( this) {

                                    @Override
                                    protected String initialValue() {
                                        return formatAndOptions().separator();
                                    }


                                    @Override
                                    protected List<String> allValues() {
                                        return Lists.newArrayList( ",", "|", ";", "\\t", " " );
                                    }


                                    @Override
                                    protected void onSubmit( ImporterPrompt prompt ) {
                                        formatAndOptions().setSeparator( value );
                                    }
                                } );
        site.newPrompt( "quoteCharacter" ).summary.put( "Hochkomma" ).description
                .put( "Mit welchem Zeichen werden Zeichenketten begrenzt?" ).value
                        .put( formatAndOptions().quoteCharacter() ).extendedUI
                                .put( new ComboBasedPromptUiBuilder( this) {

                                    @Override
                                    protected String initialValue() {
                                        return formatAndOptions().quoteCharacter();
                                    }


                                    @Override
                                    protected List<String> allValues() {
                                        return Lists.newArrayList( "\"", "'" );
                                    }


                                    @Override
                                    protected void onSubmit( ImporterPrompt prompt ) {
                                        formatAndOptions().setQuoteCharacter( value );
                                    }
                                } );
        site.newPrompt( "encoding" ).summary.put( "Zeichensatz der Daten" ).description
                .put( "Die Daten können bspw. deutsche Umlaute enthalten, die nach dem Hochladen falsch dargestellt werden. "
                        + "Mit dem Ändern des Zeichensatzes kann dies korrigiert werden." ).value
                                .put( formatAndOptions().encoding() ).extendedUI
                                        .put( new PromptUIBuilder() {

                                            private String encoding;


                                            @Override
                                            public void submit( ImporterPrompt prompt ) {
                                                formatAndOptions().setEncoding( encoding );
                                                updateOptions();
                                                prompt.ok.set( true );
                                                prompt.value.set( encoding );
                                            }


                                            @Override
                                            public void createContents( ImporterPrompt prompt, Composite parent ) {
                                                // select box
                                                Combo combo = new Combo( parent, SWT.SINGLE );
                                                List<String> encodings = Lists.newArrayList( Charsets.ISO_8859_1.name(),
                                                        Charsets.US_ASCII.name(), Charsets.UTF_8.name(),
                                                        Charsets.UTF_16.name(),
                                                        Charsets.UTF_16BE.name(), Charsets.UTF_16LE.name() );

                                                // java.nio.charset.Charset.forName(
                                                // )
                                                combo.setItems( encodings.toArray( new String[encodings.size()] ) );
                                                // combo.add

                                                combo.addSelectionListener( new SelectionAdapter() {

                                                    @Override
                                                    public void widgetSelected( SelectionEvent e ) {
                                                        Combo c = (Combo)e.getSource();
                                                        encoding = encodings.get( c.getSelectionIndex() );
                                                    }
                                                } );
                                                encoding = formatAndOptions().encoding();
                                                int index = encodings.indexOf( encoding );
                                                if (index != -1) {
                                                    combo.select( index );
                                                }
                                            }
                                        } );

    }


    @Override
    protected CSVFormatAndOptions defaultOptions() {
        return CSVFormatAndOptions.createDefault();
    }
}
