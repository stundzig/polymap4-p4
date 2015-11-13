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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgi.framework.ServiceReference;
import org.polymap.core.data.refine.RefineService;
import org.polymap.core.data.refine.impl.FormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.shapefile.ShpFeatureTableViewer;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;
import com.vividsolutions.jts.geom.Point;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public abstract class AbstractRefineFileImporter<T extends FormatAndOptions>
        implements Importer {

    // all known synonyms for longitude in lower case in all languages
    // TODO: this should be part of a *longitude synonym registry* in the user
    // settings too, to better support frequent uploads
    // each user selected columnname for longitude and latitude should be saved in
    // this user settings
    private static final Set<String> LONGITUDES = Sets.newHashSet( "geo_longitude", "lon", "longitude", "geo_x", "x" );

    // all known synonyms for latitude in lower case in all languages
    private static final Set<String> LATITUDES  = Sets.newHashSet( "geo_latitude", "lat", "latitude", "geo_y", "y" );

    private static Log               log        = LogFactory.getLog( AbstractRefineFileImporter.class );

    protected ImporterSite           site;

    @ContextIn
    protected File                   file;

    @SuppressWarnings("rawtypes")
    @ContextOut
    protected FeatureCollection      features;

    private RefineService            service;

    private ImportingJob             importJob;

    private T                        formatAndOptions;

    private Exception                exception;

    private String                   longitudeColumn;

    private String                   latitudeColumn;


    // private Composite tableComposite;

    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) throws Exception {
        this.site = site;

        ServiceReference<?> serviceReference = P4Plugin.instance().getBundle().getBundleContext()
                .getServiceReference( RefineService.class.getName() );
        service = (RefineService)P4Plugin.instance().getBundle().getBundleContext().getService( serviceReference );

        prepare();
    }


    protected abstract T defaultOptions();


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        // create all params for contextOut
        // all is done in verify
        log.info( "execute" );
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit toolkit ) {
        if (exception != null) {
            toolkit.createFlowText( parent,
                    "\nUnable to read the data.\n\n" + "**Reason**: " + exception.getMessage() );
        }
        else {
            parent.setLayout( new FormLayout() );
            Label label = toolkit.createLabel( parent, Messages.get( "importer.refine.rows", features.size() ) );
            FormDataFactory.on( label );

            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
            FormDataFactory.on( table.getControl() ).fill().top( label, 5 );
        }
    }


    protected void prepare() throws Exception {
        ImportResponse<T> response = service.importFile( file, defaultOptions() );
        importJob = response.job();
        formatAndOptions = response.options();
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        log.info( "verify" );
        site.terminal.set( true );
        try {
            ColumnModel columnModel = importJob.project.columnModel;
            StringBuffer typeSpec = new StringBuffer();
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName( layerName() );
            // TODO the CRS should be a separate prompt
            builder.setCRS( DefaultGeographicCRS.WGS84 );
            // add the default GEOM
            builder.setDefaultGeometry( "theGeom" );
            builder.add( "theGeom", Point.class );

            for (RefineColumn column : columnsWithTypes()) {
                builder.add( column.name(), column.type() );
                // if (!StringUtils.isBlank( typeSpec )) {
                // typeSpec.append( "," );
                // }
                // typeSpec.append( column.getName().replaceAll( ":", "_" ) ).append(
                // ":String" );
            }
            // final SimpleFeatureType TYPE = DataUtilities.createType( layerName(),
            // typeSpec.toString() );
            final SimpleFeatureType TYPE = builder.buildFeatureType();
            final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( TYPE );
            features = new DefaultFeatureCollection( null, TYPE );
            // TODO FeatureTable show always latest created on top
            List<Row> reverseRows = Lists.newArrayList(importJob.project.rows);
            Collections.reverse( reverseRows );
            for (Row row : reverseRows) {
                // TODO add geom
                featureBuilder.add( null );
                for (Cell cell : row.cells) {
                    // log.info( "cell with type " + (cell != null && cell.value !=
                    // null ? cell.value.getClass() : "null"));
                    featureBuilder.add( cell == null ? null : cell.value );
                }
                ((DefaultFeatureCollection)features).add( featureBuilder.buildFeature( null ) );
            }
            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site.ok.set( false );
            e.printStackTrace();
            exception = e;
        }
    }


    private List<RefineColumn> columnsWithTypes() {
        List<RefineColumn> result = Lists.newArrayList();
        for (Column column : importJob.project.columnModel.columns) {
            result.add( new RefineColumn( column.getName() ) );
        }
        // check all cells for its types and set the column type also to this type
        // String is the default in all cases
        for (Row row : importJob.project.rows) {
            int i = 0;
            for (Cell cell : row.cells) {
                if (cell != null && cell.value != null) {
                    Class currentType = cell.value.getClass();
                    RefineColumn column = result.get( i );
                    if (column.type() == null || !(column.type().isAssignableFrom( String.class ))) {
                        column.setType( currentType );
                    }
                }
                i++;
            }
        }
        return result;
    }


    protected String layerName() {
        return FilenameUtils.getBaseName( file.getName() );
    }


    protected void updateOptions() {
        service.updateOptions( importJob, formatAndOptions );
    }


    protected T formatAndOptions() {
        return formatAndOptions;
    }


    protected String[] columnNames() {
        return importJob.project.columnModel.columns.stream().map( column -> column.getName() )
                .toArray( String[]::new );
    }


    protected PromptUIBuilder coordinatesPromptUiBuilder() {
        return new PromptUIBuilder() {

            private String latValue;

            private String lonValue;


            @Override
            public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
                parent.setLayout( new FormLayout() );
                // 2 lists with all columns to select and a label in
                // front
                Label lonLabel = new Label( parent, SWT.RIGHT );
                lonLabel.setText( Messages.get( "importer.refine.lon.label" ) );
                FormDataFactory.on( lonLabel );

                Combo lonValues = new Combo( parent, SWT.DROP_DOWN );
                lonValues.setItems( columnNames() );
                FormDataFactory.on( lonValues ).fill().left( lonLabel, 5 );
                lonValues.addSelectionListener( new SelectionAdapter() {

                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        Combo c = (Combo)e.getSource();
                        lonValue = c.getItem( c.getSelectionIndex() );
                    }
                } );
                lonValue = longitudeColumn();
                if (!StringUtils.isBlank( lonValue )) {
                    for (int i = 0; i < lonValues.getItems().length; i++) {
                        if (lonValues.getItem( i ).equals( lonValue )) {
                            lonValues.select( i );
                        }
                    }
                }

                Label latLabel = new Label( parent, SWT.RIGHT );
                lonLabel.setText( Messages.get( "importer.refine.lat.label" ) );
                FormDataFactory.on( latLabel ).top( lonLabel, 5 );

                Combo latValues = new Combo( parent, SWT.DROP_DOWN );
                latValues.setItems( columnNames() );
                FormDataFactory.on( latValues ).fill().left( latLabel, 5 ).top( lonValues );

                latValues.addSelectionListener( new SelectionAdapter() {

                    @Override
                    public void widgetSelected( SelectionEvent e ) {
                        Combo c = (Combo)e.getSource();
                        latValue = c.getItem( c.getSelectionIndex() );
                    }
                } );
                latValue = latitudeColumn();
                if (!StringUtils.isBlank( latValue )) {
                    for (int i = 0; i < latValues.getItems().length; i++) {
                        if (latValues.getItem( i ).equals( latValue )) {
                            latValues.select( i );
                        }
                    }
                }
            }


            @Override
            public void submit( ImporterPrompt prompt ) {
                setLatitudeColumn( latValue );
                setLongitudeColumn( lonValue );
            }
        };
    }


    protected String longitudeColumn() {
        if (longitudeColumn == null) {
            // try to find one
            for (Column column : importJob.project.columnModel.columns) {
                String name = column.getName();
                if (LONGITUDES.contains( name.toLowerCase() )) {
                    longitudeColumn = name;
                    break;
                }
            }
        }
        return longitudeColumn;
    }


    protected void setLongitudeColumn( String lonValue ) {
        // TODO make this persistent in the user settings
        if (!LONGITUDES.contains( lonValue )) {
            LONGITUDES.add( lonValue );
        }
        longitudeColumn = lonValue;
    }


    protected String latitudeColumn() {
        if (latitudeColumn == null) {
            // try to find one
            for (Column column : importJob.project.columnModel.columns) {
                String name = column.getName();
                if (LATITUDES.contains( name.toLowerCase() )) {
                    latitudeColumn = name;
                    break;
                }
            }
        }
        return latitudeColumn;
    }


    protected void setLatitudeColumn( String latValue ) {
        if (!LATITUDES.contains( latValue )) {
            LATITUDES.add( latValue );
        }
        latitudeColumn = latValue;
    }
}
