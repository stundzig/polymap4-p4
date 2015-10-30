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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import org.osgi.framework.ServiceReference;
import org.polymap.core.data.refine.RefineService;
import org.polymap.core.data.refine.impl.FormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.p4.data.imports.shapefile.ShpFeatureTableViewer;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public abstract class AbstractRefineFileImporter<T extends FormatAndOptions>
        implements Importer {

    private static Log          log = LogFactory.getLog( AbstractRefineFileImporter.class );

    protected ImporterSite      site;

    @ContextIn
    protected MdToolkit         tk;

    @ContextIn
    protected File              file;

    @SuppressWarnings("rawtypes")
    @ContextOut
    protected FeatureCollection features;

    private RefineService       service;

    private ImportingJob        importJob;

    private T                   formatAndOptions;

    private Exception           exception;


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
            tk.createFlowText( parent, "\nUnable to read the data.\n\n" + "**Reason**: " + exception.getMessage() );
        }
        else {
            SimpleFeatureType schema = (SimpleFeatureType)features.getSchema();
            ShpFeatureTableViewer table = new ShpFeatureTableViewer( parent, schema );
            table.setContent( features );
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
            for (Column column : columnModel.columns) {
                if (!StringUtils.isBlank( typeSpec )) {
                    typeSpec.append( "," );
                }
                typeSpec.append( column.getName().replaceAll( ":", "_" ) ).append( ":String" );
            }
            final SimpleFeatureType TYPE = DataUtilities.createType( layerName(), typeSpec.toString() );
            final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( TYPE );
            features = new DefaultFeatureCollection( null, TYPE );
            for (Row row : importJob.project.rows) {
                for (Cell cell : row.cells) {
                    featureBuilder.add( cell == null || cell.value == null ? "" //$NON-NLS-1$
                            : cell.value.toString() );
                }
                ((DefaultFeatureCollection)features).add( featureBuilder.buildFeature( null ) );
            }
            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            site.ok.set( false );
            exception = e;
        }
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
}
