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
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
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
import com.google.refine.model.Project;
import com.google.refine.model.Row;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public abstract class AbstractRefineFileImporter<T extends FormatAndOptions>
        implements Importer {

    /*
     * GeometryFactory will be used to create the geometry attribute of each feature
     * (a Point object for the location)
     */
    private final static GeometryFactory GEOMETRYFACTORY     = JTSFactoryFinder.getGeometryFactory( null );

    // all known synonyms for longitude in lower case in all languages
    // TODO: this should be part of a *longitude synonym registry* in the user
    // settings too, to better support frequent uploads
    // each user selected columnname for longitude and latitude should be saved in
    // this user settings
    private static final Set<String>     LONGITUDES          = Sets.newHashSet( "geo_longitude", "lon", "longitude",
            "geo_x", "x" );

    // all known synonyms for latitude in lower case in all languages
    private static final Set<String>     LATITUDES           = Sets.newHashSet( "geo_latitude", "lat", "latitude",
            "geo_y",
            "y" );

    private static Log                   log                 = LogFactory.getLog( AbstractRefineFileImporter.class );

    protected ImporterSite               site;

    @ContextIn
    protected File                       file;

    @SuppressWarnings("rawtypes")
    @ContextOut
    protected FeatureCollection          features;

    private RefineService                service;

    private ImportingJob                 importJob;

    private T                            formatAndOptions;

    private Exception                    exception;

    private String                       longitudeColumn;

    private String                       latitudeColumn;

    private TypedContent                 typedContent;

    private Project                      project;

    private boolean                      shouldUpdateOptions = false;


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

        prepare( monitor );
    }


    protected abstract T defaultOptions();


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {

        // create all params for contextOut
        // use the typed column from the preview and cancel the import create a
        project = service.createProject( importJob, formatAndOptions, monitor );
        // reset it
        typedContent = null;
        log.info( "project has rows: " + project.rows.size() );
        // TODO MONITOR for the features
        features = createFeatures();
    }


    private FeatureCollection createFeatures() {
        TypedContent content = typedContent();
        final SimpleFeatureType TYPE = buildFeatureType( content.columns() );
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( TYPE );
        FeatureCollection features = new DefaultFeatureCollection( null, TYPE );
        // TODO FeatureTable shows always latest created on top, therefore reverse
        // the order
        List<RefineRow> rows = content.rows();
        Collections.reverse( rows );
        int latitudeColumnIndex = content.columnIndex( latitudeColumn() );
        int longitudeColumnIndex = content.columnIndex( longitudeColumn() );
        // coordinate columns selected, but final file parsing contains crappy data,
        // so the columns or only String
        if (latitudeColumnIndex != -1 && longitudeColumnIndex != -1
                && (!Number.class.isAssignableFrom( content.columns().get( latitudeColumnIndex ).type() )
                        || !Number.class.isAssignableFrom( content.columns().get( longitudeColumnIndex ).type() ))) {
            // TODO error message to the user
            log.error( "skipping coordinate creation" );
            latitudeColumnIndex = -1;
            longitudeColumnIndex = -1;
        }

        int count = 0;
        for (RefineRow row : rows) {
            if (latitudeColumnIndex != -1 && longitudeColumnIndex != -1) {
                // construct the coordinate
                try {
                    Number latitude = (Number)row.cells().get( latitudeColumnIndex ).guessedValue();
                    Number longitude = (Number)row.cells().get( longitudeColumnIndex ).guessedValue();
                    Point point = GEOMETRYFACTORY
                            .createPoint( new Coordinate( longitude.doubleValue(), latitude.doubleValue() ) );
                    featureBuilder.add( point );
                }
                catch (Exception e) {
                    log.error( String.format( "exception in creating point for imported file with %s and %s",
                            row.cells().get( latitudeColumnIndex ).guessedValue(),
                            row.cells().get( longitudeColumnIndex ).guessedValue() ), e );
                    featureBuilder.add( null );
                }
            }
            else {
                featureBuilder.add( null );
            }
            for (RefineCell cell : row.cells()) {
                // log.info( "cell with type " + (cell != null && cell.value !=
                // null ? cell.value.getClass() : "null"));
                featureBuilder.add( cell == null ? null : cell.guessedValue() );
            }
            ((DefaultFeatureCollection)features).add( featureBuilder.buildFeature( null ) );
            if (count % 10000 == 0) {
                log.info( "created " + count );
            }
            count++;
        }
        return features;
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit toolkit ) {
        if (exception != null) {
            toolkit.createFlowText( parent,
                    "\nUnable to read the data.\n\n" + "**Reason**: " + exception.getMessage() );
        }
        else {
            parent.setLayout( new FormLayout() );
            Label label = toolkit.createLabel( parent,
                    Messages.get( features.size() == 500 ? "importer.refine.rows.shrinked" : "importer.refine.rows",
                            features.size() ) );
            FormDataFactory.on( label );

            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
            FormDataFactory.on( table.getControl() ).fill().top( label, 5 );
        }
    }


    protected void prepare( IProgressMonitor monitor ) throws Exception {
        ImportResponse<T> response = service.importFile( file, defaultOptions(), monitor );
        importJob = response.job();
        formatAndOptions = response.options();
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        log.info( "verify" );
//        typedContent = null;
        if (shouldUpdateOptions) {
            updateOptions( monitor );
            shouldUpdateOptions = false;
        }
        site.terminal.set( true );
        try {
            features = createFeatures();
            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site.ok.set( false );
            e.printStackTrace();
            exception = e;
        }
    }


    private SimpleFeatureType buildFeatureType( List<TypedColumn> columns ) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName( layerName() );
        // TODO the CRS should be a separate prompt
        builder.setCRS( DefaultGeographicCRS.WGS84 );
        // add the default GEOM
        builder.setDefaultGeometry( "theGeom" );
        builder.add( "theGeom", Point.class );

        for (TypedColumn column : columns) {
            builder.add( column.name(), column.type() );
        }
        return builder.buildFeatureType();
    }


    protected synchronized TypedContent typedContent() {
        if (typedContent == null) {
            typedContent = new TypedContent( columnsWithTypes(), rows() );

        }
        return typedContent;
    }


    private List<RefineRow> rows() {
        List<RefineRow> result = Lists.newArrayList();
        List<Row> rows = originalRows();
        for (Row row : rows) {
            RefineRow resultRow = new RefineRow();
            result.add( resultRow );
            for (Cell cell : row.cells) {
                resultRow.add( new RefineCell( cell ) );
            }
        }
        return result;
    }


    /**
     * if the project is not created, the importjob content is used
     */
    protected List<Row> originalRows() {
        return project == null ? importJob.project.rows : project.rows;
    }


    private List<TypedColumn> columnsWithTypes() {
        List<TypedColumn> columnsWithType = Lists.newArrayList();
        for (Column column : originalColumns()) {
            columnsWithType.add( new TypedColumn( column.getName() ) );
        }
        // check all cells for its type and set the column type
        // String is the default in all cases
        for (RefineRow row : rows()) {
            int i = 0;
            for (RefineCell cell : row.cells()) {
                if (cell != null && cell.guessedValue() != null) {
                    TypedColumn column = columnsWithType.get( i );
                    Class currentType = cell.guessedValue().getClass();
                    // if null, set it
                    // if string dont change it
                    // if current type = string, set it
                    // if current double and assigned long, set it
                    if (column.type() == null) {
                        column.setType( currentType );
                    }
                    else {
                        // if it was string before, it never gets overridden
                        if (!(String.class.isAssignableFrom( column.type() ))) {
                            if (Long.class.isAssignableFrom( currentType )
                                    && !(Double.class.isAssignableFrom( column.type() ))) {
                                // dont override double with long
                                column.setType( currentType );
                            }
                            else {
                                column.setType( currentType );
                            }
                        }
                    }
                }
                i++;
            }
        }
        columnsWithType.stream().filter( c -> c.type() == null ).forEach( c -> c.setType( String.class ) );
        return columnsWithType;
    }


    /**
     * if the project is not created, the importjob content is used
     */
    protected List<Column> originalColumns() {
        return project == null ? importJob.project.columnModel.columns : project.columnModel.columns;
    }


    protected String layerName() {
        return FilenameUtils.getBaseName( file.getName() ).replace( ".", "_" );
    }


    protected void updateOptions( IProgressMonitor monitor ) {
        typedContent = null;
        service.updateOptions( importJob, formatAndOptions, monitor );
    }


    public T formatAndOptions() {
        return formatAndOptions;
    }


    protected String[] numberColumnNames() {
        return typedContent().columns().stream().filter( c -> Number.class.isAssignableFrom( c.type() ) )
                .map( column -> column.name() ).toArray( String[]::new );
    }


    protected PromptUIBuilder coordinatesPromptUiBuilder() {
        return new PromptUIBuilder() {

            private String latValue;

            private String lonValue;


            @Override
            public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
                parent.setLayout( new FormLayout() );
                //
                String[] columnNames = numberColumnNames();
                if (columnNames.length == 0) {
                    FormDataFactory.on( tk.createLabel( parent, Messages.get( "importer.prompt.coordinates.nocolumns" ),
                            SWT.LEFT ) ).left( 0 ).top( 5 ).width( 300 );
                }
                else {
                    // 2 lists with all columns to select and a label in
                    // front
                    Label lonLabel = new Label( parent, SWT.RIGHT );
                    lonLabel.setText( Messages.get( "importer.refine.lon.label" ) );

                    Combo lonValues = new Combo( parent, SWT.DROP_DOWN );
                    lonValues.setItems( columnNames );
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
                    latLabel.setText( Messages.get( "importer.refine.lat.label" ) );

                    Combo latValues = new Combo( parent, SWT.DROP_DOWN );
                    latValues.setItems( columnNames );

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

                    FormDataFactory.on( latLabel ).left( 0 ).top( 12 ).width( 100 );
                    FormDataFactory.on( latValues ).left( latLabel, 5 ).top( 5 ).width( 350 );
                    FormDataFactory.on( lonLabel ).top( latValues, 12 ).width( 100 );
                    FormDataFactory.on( lonValues ).left( lonLabel, 5 ).top( latValues, 5 ).width( 350 ).bottom( 95 );
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
            for (TypedColumn column : typedContent().columns()) {
                String name = column.name();
                Class type = column.type();
                if (Number.class.isAssignableFrom( type ) && LONGITUDES.contains( name.toLowerCase() )) {
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
            for (TypedColumn column : typedContent().columns()) {
                String name = column.name();
                Class type = column.type();
                if (Number.class.isAssignableFrom( type ) && LATITUDES.contains( name.toLowerCase() )) {
                    latitudeColumn = name;
                    break;
                }
            }
        }
        return latitudeColumn;
    }


    protected void setLatitudeColumn( String latValue ) {
        // TODO make this persistent in the user settings
        if (!LATITUDES.contains( latValue )) {
            LATITUDES.add( latValue );
        }
        latitudeColumn = latValue;
    }


    protected String coordinatesPromptLabel() {
        String lat = latitudeColumn();
        String lon = longitudeColumn();
        return lat != null && lon != null ? (lat != null ? lat : "") + "/" + (lon != null ? lon : "") : "";
    }


    public void triggerUpdateOptions() {
        shouldUpdateOptions = true;
    }
}
