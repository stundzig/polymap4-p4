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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.core.runtime.Polymap;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.ComboBasedPromptUiBuilder;
import org.polymap.p4.data.imports.refine.NumberfieldBasedPromptUiBuilder;
import org.polymap.p4.data.imports.refine.RefineCell;
import org.polymap.p4.data.imports.refine.RefineRow;
import org.polymap.p4.data.imports.refine.TypedColumn;
import org.polymap.p4.data.imports.refine.TypedContent;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.TreeMultimap;
import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.Row;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporter
        extends AbstractRefineFileImporter<CSVFormatAndOptions> {

    private static Log   log = LogFactory.getLog( CSVFileImporter.class );

    private TypedContent csvTypedContent;


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "csv.svg", NORMAL24 ) );
        site.summary.set( Messages.get( "importer.csv.summary", file.getName() ) );
        site.description.set( Messages.get( "importer.csv.description" ) );

        super.init( site, monitor );
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
                                return Lists.newArrayList( ",", "|", ";", "\\t", " " );
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
        site.newPrompt( "encoding" ).value
                .put( formatAndOptions().encoding() ).extendedUI
                        .put( new ComboBasedPromptUiBuilder( this) {

                            @Override
                            protected String initialValue() {
                                return formatAndOptions().encoding();
                            }


                            @Override
                            protected List<String> allValues() {
                                return Lists.newArrayList( Charsets.ISO_8859_1.name(),
                                        Charsets.US_ASCII.name(), Charsets.UTF_8.name(),
                                        Charsets.UTF_16.name(),
                                        Charsets.UTF_16BE.name(), Charsets.UTF_16LE.name() );
                            }


                            @Override
                            protected void onSubmit( ImporterPrompt prompt ) {
                                formatAndOptions().setEncoding( value );
                            }
                        } );
        // site.newPrompt( "dataTypes" ).value
        // .put( "TODO: 12 Spalten, 3 Text 9 Zahl" ).extendedUI
        // .put( new NumberFormatPromptUiBuilder( this, Polymap.getSessionLocale() )
        // );
        site.newPrompt( "coordinates" ).value.put( coordinatesPromptLabel() ).extendedUI
                .put( coordinatesPromptUiBuilder() );
    }


    @Override
    protected CSVFormatAndOptions defaultOptions() {
        return CSVFormatAndOptions.createDefault();
    }


    //
    //
    // @Override
    // protected void prepare() throws Exception {
    // super.prepare();
    // // formatAndOptions().setNumberLocale( Polymap.getSessionLocale() );
    // // formatAndOptions().setNumberFormat(
    // // ((DecimalFormat)DecimalFormat.getInstance( Polymap.getSessionLocale()
    // // )).toLocalizedPattern() );
    // // formatAndOptions().enableGuessCellValueTypes();
    // // this.updateOptions();
    // }
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
                    if (cell == null || cell.value == null
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
                                log.error( "error in parsing " + cell.value.toString(), e );
                            }
                        }
                        else {
                            // defaults to string
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
    //
    //
    // public static void main( String[] args ) throws ParseException {
    // // Locale[] locales = NumberFormat.getAvailableLocales();
    // // double myNumber = -1234.56;
    // // NumberFormat form;
    // // for (int j = 0; j < 4; ++j) {
    // System.out.println( "FORMAT" );
    // TreeMultimap<String,String> formats = TreeMultimap.create();
    // for (Locale locale : DecimalFormat.getAvailableLocales()) {
    // if (locale.getCountry().length() != 0) {
    // continue; // Skip language-only locales
    // }
    // if (!StringUtils.isBlank( locale.getDisplayName( Locale.ENGLISH ) )) {
    // formats.put( ((DecimalFormat)NumberFormat.getInstance( locale
    // )).toLocalizedPattern(),
    // locale.getDisplayName( Locale.ENGLISH ) );
    // }
    // // System.out.print( locales[i].getDisplayName() );
    // // switch (j) {
    // // case 0:
    // // form = NumberFormat.getInstance( locales[i] );
    // // break;
    // // case 1:
    // // form = NumberFormat.getIntegerInstance( locales[i] );
    // // break;
    // // case 2:
    // // form = NumberFormat.getCurrencyInstance( locales[i] );
    // // break;
    // // default:
    // // form = NumberFormat.getPercentInstance( locales[i] );
    // // break;
    // // }
    // // if (form instanceof DecimalFormat) {
    // // System.out.print( ": " + ((DecimalFormat)form).toPattern() );
    // // }
    // // System.out.print( " -> " + form.format( myNumber ) );
    // //
    // // System.out.println( " -> " + form.parse( form.format( myNumber ) )
    // // );
    //
    // }
    // formats.asMap().forEach( ( key, values ) -> System.out.println( key + ": " +
    // values ) );
    //
    // System.out.println( new DecimalFormat( "#,##0.###" ).parse( "1,234.56" ) );
    // ParsePosition p = new ParsePosition( 0 );
    // String value = "31.102,00";
    // System.out.println( DecimalFormat.getInstance( Locale.GERMANY ).parse( value,
    // p ) );
    // System.out.println( value.length() + " <> " + p.getIndex() + ": " +
    // p.getErrorIndex() );
    //
    // // System.out.println( new DecimalFormat( "#.##0,###" ).parse( "1,234.56" )
    // // );
    //
    // // String strange = "#.##0,###";
    // DecimalFormat weirdFormatter = (DecimalFormat)DecimalFormat.getInstance(
    // Locale.GERMANY );
    // // weirdFormatter.applyPattern( strange );
    // // weirdFormatter.setDecimalFormatSymbols( unusualSymbols );
    // // weirdFormatter.setGroupingSize( 3 );
    // p = new ParsePosition( 0 );
    // Number number = weirdFormatter.parse( value, p );
    // System.out.println( number );
    // System.out.println( weirdFormatter.format( number ) );
    //
    // System.out.println( number.toString().length() + "<>" + value.length()
    // + " <> " + p.getIndex() + ": " + p.getErrorIndex() );
    //
    // }
}
