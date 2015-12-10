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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolTip;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.ComboBasedPromptUiBuilder;
import org.polymap.p4.data.imports.refine.NumberfieldBasedPromptUiBuilder;
import org.polymap.p4.data.imports.refine.RefineCell;
import org.polymap.p4.data.imports.refine.RefineRow;
import org.polymap.p4.data.imports.refine.TypedColumn;
import org.polymap.p4.data.imports.refine.TypedContent;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Row;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporter
        extends AbstractRefineFileImporter<CSVFormatAndOptions> {

    private static final Log      log                       = LogFactory.getLog( CSVFileImporter.class );

    private static final Pattern  ASCIIONLY                 = Pattern.compile( "\\p{ASCII}*" );

    private TypedContent          csvTypedContent;

    private List<String>          potentialEncodingProblems = null;

    private GuessedQuoteCharacter guessedQuoteCharacter     = null;


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "csv.svg", NORMAL24 ) );
        site.summary.set( Messages.get( "importer.csv.summary", file.getName() ) );
        site.description.set( Messages.get( "importer.csv.description" ) );

        super.init( site, monitor );
    }


    @Override
    protected void prepare( IProgressMonitor monitor ) throws Exception {
        super.prepare( monitor );
        // formatAndOptions().setProcessQuotes(false);
        // formatAndOptions().setQuoteCharacter( "\0" );
        // updateOptions( monitor );
        // TODO guess find the quote characters
        //typedContent();
        GuessedQuoteCharacter guessed = guessedQuoteCharacter();
        if (!guessed.quoteCharacter().equals( formatAndOptions().quoteCharacter())) {
            formatAndOptions().setQuoteCharacter( guessed.quoteCharacter() );
            updateOptions( monitor );
        }
        
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        site.newPrompt( "ignoreBeforeHeadline" ).value
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
        site.newPrompt( "headlines" ).value
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
        site.newPrompt( "ignoreAfterHeadline" ).value
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
        site.newPrompt( "separator" ).value
                .put( formatAndOptions().separator() ).extendedUI
                        .put( new ComboBasedPromptUiBuilder( this) {

                            @Override
                            protected String initialValue() {
                                return formatAndOptions().separator();
                            }


                            @Override
                            protected List<String> allValues() {
                                List<String> ret = Lists.newArrayList( ",", "|", ";", "\\t", " ");
                                if (!ret.contains( initialValue() )) {
                                    ret.add( 0, initialValue());
                                }
                                return ret;
                            }


                            @Override
                            protected void onSubmit( ImporterPrompt prompt ) {
                                formatAndOptions().setSeparator( value );
                            }
                        } );
        site.newPrompt( "quoteCharacter" ).value
                .put( formatAndOptions().quoteCharacter() ).extendedUI
                        .put( new ComboBasedPromptUiBuilder( this) {

                            @Override
                            protected String initialValue() {
                                // TODO guess the quote character
                                return formatAndOptions().quoteCharacter();
                            }


                            @Override
                            protected List<String> allValues() {
                                return Lists.newArrayList( "", "\"", "'" );
                            }


                            @Override
                            protected void onSubmit( ImporterPrompt prompt ) {
                                formatAndOptions().setQuoteCharacter( value );
                                // formatAndOptions().setProcessQuotes(
                                // !StringUtils.isBlank( value ) );
                            }
                        } );
        site.newPrompt( "encoding" ).value
                .put( formatAndOptions().encoding() ).extendedUI
                        .put( encodingPromptUiBuilder() ).severity
                                .set( Severity.REQUIRED );
        site.newPrompt( "coordinates" ).value.put( coordinatesPromptLabel() ).extendedUI
                .put( coordinatesPromptUiBuilder() );
    }


    @Override
    protected CSVFormatAndOptions defaultOptions() {
        return CSVFormatAndOptions.createDefault();
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        csvTypedContent = null;
        super.execute( monitor );
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        csvTypedContent = null;
        super.verify( monitor );
    }


    @Override
    protected void updateOptions( IProgressMonitor monitor ) {
        csvTypedContent = null;
        potentialEncodingProblems = null;

        super.updateOptions( monitor );
    }


    @Override
    protected synchronized TypedContent typedContent() {
        if (csvTypedContent == null) {
            List<TypedColumn> columnsWithType = Lists.newArrayList();
            for (Column column : originalColumns()) {
                columnsWithType.add( new TypedColumn( column.getName() ) );
            }
            // in CSV all cells are strings only, so type guess its value
            // String is the default in all cases
            List<RefineRow> rows = Lists.newArrayList();
            for (Row originalRow : originalRows()) {
                int i = 0;
                RefineRow row = new RefineRow();
                rows.add( row );
                for (Cell cell : originalRow.cells) {
                    TypedColumn column = columnsWithType.get( i );
                    if (cell == null || cell.value == null || cell.value.toString().trim().equals( "" )
                            || (column.type() != null && column.type().isAssignableFrom( String.class ))) {
                        // seems to be empty or a string was found in the same column
                        // earlier
                        row.add( new RefineCell( cell ) );
                    }
                    else {
                        // guess the type, fallback in any error case is String
                        // log.info( "guessing " + cell.value.toString() );
                        GuessedType guessedType = TypeGuesser.guess( cell.value.toString() );
                        if (guessedType.type().equals( Type.Decimal )) {
                            try {
                                // convert to number
                                NumberFormat formatter = DecimalFormat.getInstance(
                                        guessedType.locale() != null ? guessedType.locale()
                                                : Polymap.getSessionLocale() );
                                Object guessedValue = formatter.parse( cell.value.toString().trim() );
                                if (column.type() == null || !column.type().isAssignableFrom( Double.class )) {
                                    // dont overwrite a double, with a long
                                    column.setType( guessedValue.getClass() );
                                }
                                column.addLocale( guessedType.locale() );
                                row.add( new RefineCell( cell, guessedValue ) );
                            }
                            catch (ParseException e) {
                                // default to string
                                column.setType( String.class );
                                row.add( new RefineCell( cell ) );
                                throw new RuntimeException( e );
                            }
                        }
                        else {
                            // defaults to string
                            log.info( "Setting string in column " + column.name() + " because of '"
                                    + cell.value.toString() + "'" );
                            column.setType( String.class );
                            row.add( new RefineCell( cell ) );
                        }
                    }
                    i++;
                }
            }
            columnsWithType.stream().filter( c -> c.type() == null ).forEach( c -> c.setType( String.class ) );
            csvTypedContent = new TypedContent( columnsWithType, rows );
        }
        return csvTypedContent;
    }


    private PromptUIBuilder encodingPromptUiBuilder() {
        return new PromptUIBuilder() {

            private String encoding;


            @Override
            public void submit( ImporterPrompt prompt ) {
                formatAndOptions().setEncoding( encoding );
                triggerUpdateOptions();
                prompt.ok.set( true );
                prompt.value.set( encoding );
            }


            @Override
            public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
                parent.setLayout( new FormLayout() );

                Combo combo = new Combo( parent, SWT.SINGLE | SWT.BORDER );
                TableViewer viewer = null;
                if (potentialEncodingProblems() != null && !potentialEncodingProblems().isEmpty()) {
                    viewer = new TableViewer( parent, SWT.H_SCROLL |
                            SWT.V_SCROLL );
                    // create preview table
                    ColumnViewerToolTipSupport.enableFor( viewer );
                    TableLayout layout = new TableLayout();
                    viewer.getTable().setLayout( layout );
                    viewer.getTable().setHeaderVisible( true );
                    viewer.getTable().setLinesVisible( true );

                    layout.addColumnData( new ColumnPixelData( 700 ) );
                    TableViewerColumn viewerColumn = new TableViewerColumn( viewer, SWT.H_SCROLL );
                    viewerColumn.getColumn().setText( Messages.get( "importer.prompt.encoding.samples" ) );
                    viewerColumn.setLabelProvider( new ColumnLabelProvider() {

                        @Override
                        public String getToolTipText( Object element ) {
                            return super.getText( element );
                        }
                    } );

                    viewer.setContentProvider( ArrayContentProvider.getInstance() );
                    viewer.setInput( potentialEncodingProblems() );

                    FormDataFactory.on( viewer.getTable() ).left( 1 ).top( combo, 15 ).width( 700 ).height( 400 );
                }

                final TableViewer finalViewer = viewer;
                List<String> allValues = allValues();
                combo.setItems( allValues.stream().toArray( String[]::new ) );
                combo.addSelectionListener( new SelectionAdapter() {

                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        encoding = allValues.get( combo.getSelectionIndex() );
                        if (finalViewer != null) {
                            finalViewer.refresh();
                        }
                    }
                } );
                encoding = initialValue();
                int index = allValues.indexOf( encoding );
                if (index != -1) {
                    combo.select( index );
                }
                prompt.value.set( encoding );
                FormDataFactory.on( combo ).left( 1 ).top( 5 ).width( 250 );
            }


            protected String initialValue() {
                return formatAndOptions().encoding();
            }


            protected List<String> allValues() {
                return Lists.newArrayList( StandardCharsets.ISO_8859_1.name(),
                        StandardCharsets.US_ASCII.name(), StandardCharsets.UTF_8.name(),
                        StandardCharsets.UTF_16.name(),
                        StandardCharsets.UTF_16BE.name(), StandardCharsets.UTF_16LE.name() );
            }

        };
    }


    protected List<String> potentialEncodingProblems() {
        if (potentialEncodingProblems == null) {
            potentialEncodingProblems = Lists.newArrayList();

            for (Row originalRow : originalRows()) {
                for (Cell cell : originalRow.cells) {
                    // check also for umlauts
                    if (cell != null && cell.value != null && potentialEncodingProblems.size() < 20
                            && !ASCIIONLY.matcher( cell.value.toString() ).matches()) {
                        potentialEncodingProblems.add( cell.value.toString() );
                    }
                }
            }
        }
        return potentialEncodingProblems;
    }


    protected GuessedQuoteCharacter guessedQuoteCharacter() {
        if (guessedQuoteCharacter == null) {
            guessedQuoteCharacter = new GuessedQuoteCharacter();
            int maxChecks = 1000;
            int i = 0;
            for (Row originalRow : originalRows()) {
                for (Cell cell : originalRow.cells) {
                    // check also for umlauts
                    if (cell != null && cell.value != null && i < maxChecks) {
                        String str = cell.value.toString().trim();
                        if (!StringUtils.isBlank( str )) {
                            i++;
                            if (str.startsWith( "\"" ) && str.endsWith( "\"" )) {
                                guessedQuoteCharacter.increaseDouble();
                            }
                            else if (str.startsWith( "'" ) && str.endsWith( "'" )) {
                                guessedQuoteCharacter.increaseSingle();
                            }
                            else {
                                guessedQuoteCharacter.increaseNo();
                            }
                        }
                    }
                }
            }
        }
        return guessedQuoteCharacter;
    }
}
