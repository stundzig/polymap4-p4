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
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.geotools.feature.DefaultFeatureCollection;
import org.polymap.core.data.refine.impl.ExcelFormatAndOptions;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.p4.data.importer.ImporterPlugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.Messages;
import org.polymap.p4.data.imports.refine.AbstractRefineFileImporter;
import org.polymap.p4.data.imports.refine.NumberfieldBasedPromptUiBuilder;
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

        site.icon.set( ImporterPlugin.images().svgImage( "xls.svg", NORMAL24 ) );
        if (sheetIn.index() != -1) {

            site.summary.set( Messages.get( "importer.excel.summary.sheet", file.getName(), sheetIn.name() ) );
        }
        else {
            site.summary.set( Messages.get( "importer.excel.summary", file.getName() ) );
        }
        site.description.set( Messages.get( "importer.excel.description" ) );

        super.init( site, monitor );
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        if (sheetIn.index() != -1 || formatAndOptions().sheetRecords().size() == 1) {
            site.newPrompt( "ignoreBeforeHeadline" ).summary
                    .put( Messages.get( "importer.prompt.ignoreBeforeHeadline.summary" ) ).description
                            .put( Messages.get( "importer.prompt.ignoreBeforeHeadline.description" ) ).value
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
            site.newPrompt( "headlines" ).summary
                    .put( Messages.get( "importer.prompt.headlines.summary" ) ).description
                            .put( Messages.get( "importer.prompt.headlines.description" ) ).value
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
            site.newPrompt( "ignoreAfterHeadline" ).summary
                    .put( Messages.get( "importer.prompt.ignoreAfterHeadline.summary" ) ).description
                            .put( Messages.get( "importer.prompt.ignoreAfterHeadline.description" ) ).value
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
            site.newPrompt( "coordinates" ).summary
                    .put( Messages.get( "importer.prompt.coordinates.summary" ) ).description
                            .put( Messages.get( "importer.prompt.coordinates.description" ) ).value
                                    .put( latitudeColumn() + "/" + longitudeColumn() ).extendedUI
                                            .put( coordinatesPromptUiBuilder() );
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
            // verify and create feature collection only, if its a *single sheet*
            // file
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
            parent.setLayout( new FormLayout() );

            Label label = tk.createLabel( parent, Messages.get( "importer.excel.sheets" ), SWT.LEFT );
            FormDataFactory.on( label );

            org.eclipse.swt.widgets.List list = tk.createList( parent, SWT.SINGLE );
            FormDataFactory.on( list ).fill().top( label, 5 );

            formatAndOptions().sheetRecords()
                    .forEach( record -> list.add( Messages.get( "importer.excel.sheet", record.name, record.rows ) ) );
            if (selectedSheet != -1) {
                list.select( selectedSheet );
            }
            list.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    selectedSheet = list.getSelectionIndex();
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
    protected void prepare( IProgressMonitor monitor ) throws Exception {
        // during super.prepare() the file is moved to another location, so make copy
        // here, to support multiworksheet for initial loads
        if (sheetIn.index() == -1) {
            copyOfOriginalFile = new File( Files.createTempDir(), FilenameUtils.getName( file.getName() ) );
            Files.copy( file, copyOfOriginalFile );
        }
        super.prepare( monitor );
        // autoselect the first sheet, if only one exists
        if (formatAndOptions().sheetRecords().size() == 1) {
            formatAndOptions().setSheet( 0 );
            triggerUpdateOptions();
        }
        else {
            // if more the one sheets exists, and we are in the *subimporter*, select
            // this one
            if (sheetIn.index() != -1) {
                formatAndOptions().setSheet( sheetIn.index() );
                triggerUpdateOptions();
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
