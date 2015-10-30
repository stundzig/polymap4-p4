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
package org.polymap.p4.data.imports.refine.excel;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.geotools.feature.DefaultFeatureCollection;
import org.polymap.core.data.refine.impl.ExcelFormatAndOptions;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.IgnoreLinesAfterHeadlinePromptUiBuilder;
import org.polymap.p4.data.imports.refine.IgnoreLinesBeforeHeadlinePromptUiBuilder;
import org.polymap.p4.data.imports.refine.NumberOfHeadlinesPromptUiBuilder;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import com.google.common.io.Files;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class ExcelFileImporter
        extends AbstractRefineFileImporter<ExcelFormatAndOptions> {

    private static Log log           = LogFactory.getLog( ExcelFileImporter.class );

    @ContextIn
    protected Sheet    sheetIn;

    @ContextOut
    protected Sheet    sheetOut;

    protected int      selectedSheet = -1;

    private File       copyOfOriginalFile;


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "xls.svg", NORMAL24 ) );
        if (sheetIn.index() != -1) {
            site.summary.set( "Excel file: " + file.getName() + " [" + sheetIn.name() + "]" );
        }
        else {
            site.summary.set( "Excel file: " + file.getName() );
        }
        // site.description.set("Description");

        super.init( site, monitor );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        if (sheetIn.index() != -1 || formatAndOptions().sheetRecords().size() == 1) {
            site.newPrompt( "ignoreBeforeHeadline" ).summary.put( "Ignorieren bis zur Kopfzeile" ).description
            .put( "Wieviele Zeilen befinden sich über den Spaltenüberschriften?" ).extendedUI
                    .put( new IgnoreLinesBeforeHeadlinePromptUiBuilder( this ) );
            site.newPrompt( "headline" ).summary.put( "Kopfzeilen" ).description
                    .put( "Wieviele Zeilen enhalten die Spaltenüberschriften?" ).extendedUI
                            .put( new NumberOfHeadlinesPromptUiBuilder( this ) );
            site.newPrompt( "ignoreAfterHeadline" ).summary.put( "Überflüssige Datenzeilen" ).description
            .put( "Wieviele Zeilen können nach der Spaltenüberschrift ignoriert werden?" ).extendedUI
                    .put( new IgnoreLinesAfterHeadlinePromptUiBuilder( this ) );
        }
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        if (sheetIn.index() == -1 && selectedSheet != -1) {
            // NPE otherwise in ImporterContext.java:357
            features = new DefaultFeatureCollection();

            File copy = new File( Files.createTempDir(), FilenameUtils.getName( copyOfOriginalFile.getName() ) );
            Files.copy( copyOfOriginalFile, copy );
            sheetOut = new Sheet( copy, selectedSheet, formatAndOptions().sheetRecords().get( selectedSheet ).name );
        }
        else {
            super.execute( monitor );
        }
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        if (sheetIn.index() != -1 || formatAndOptions().sheetRecords().size() == 1) {
            // verify and create feature collection only, if its a *single sheet* file
            super.verify( monitor );
        }
        else {
            site.ok.set( false );
            site.terminal.set( false );
        }
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (sheetIn.index() == -1 && formatAndOptions().sheetRecords().size() > 1) {
            // select the correct sheet
            // Label label = new Label( parent, SWT.LEFT );
            // label.setText( "Das Excel Dokument enthält mehrere
            // Arbeitsblätter. Bitte wählen Sie die Arbeitsblätter die sie
            // importieren möchten." );
            // FormDataFactory.on( label );
            org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List( parent, SWT.SINGLE );

            // FormDataFactory.on( list ).fill().top( label, 5 ).bottom( 90, -5
            // ).height( 50 ).width( 300 );
            formatAndOptions().sheetRecords()
                    .forEach( record -> list.add( record.name + ": " + record.rows + " zeilen" ) );
            if (selectedSheet != -1) {
                list.select( selectedSheet );
            }
            list.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    // TODO open the sheet in a new importer window, copy also
                    // the base file on the second selection to avoid side
                    // effects
                    selectedSheet = list.getSelectionIndex();
                    log.error( "open in new importer panel, similar to the archive importer" );
                    // selectedSheet = e.
                    site.ok.set( true );
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


    @Override
    protected void prepare() throws Exception {
        // during super.prepare() the file is moved to another location, so make copy
        // here, to support multiworksheet for initial loads
        if (sheetIn.index() == -1) {
            copyOfOriginalFile = new File( Files.createTempDir(), FilenameUtils.getName( file.getName() ) );
            Files.copy( file, copyOfOriginalFile );
        }
        super.prepare();
        // autoselect the first sheet, if only one exists
        if (formatAndOptions().sheetRecords().size() == 1) {
            formatAndOptions().setSheet( 0 );
            updateOptions();
        }
        else {
            // if more the one sheets exists, and we are in the *subimporter*, select
            // this one
            if (sheetIn.index() != -1) {
                formatAndOptions().setSheet( sheetIn.index() );
                updateOptions();
            }
        }
    }


    @Override
    protected String layerName() {
        if (formatAndOptions().sheetRecords().size() == 1) {
            return super.layerName();
        }
        else {
            return super.layerName() + " [" + sheetIn.name() + "]";
        }
    }
}
