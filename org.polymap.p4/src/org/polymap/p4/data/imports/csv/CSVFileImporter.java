/* 
 * polymap.org
 * Copyright (C) 2015, the @autors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports.csv;

import static org.polymap.rhei.batik.app.SvgImageRegistryHelper.NORMAL24;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.framework.ServiceReference;
import org.polymap.core.data.refine.RefineService;
import org.polymap.core.data.refine.impl.FormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.data.imports.ContextIn;
import org.polymap.p4.data.imports.ContextOut;
import org.polymap.p4.data.imports.Importer;
import org.polymap.p4.data.imports.ImporterSite;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;

/**
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class CSVFileImporter
        implements Importer {

    private static Log       log = LogFactory.getLog( CSVFileImporter.class );

    protected ImporterSite   site;

    @ContextIn
    protected MdToolkit      tk;

    @ContextIn
    protected File           file;

    @ContextOut
    protected List<File>     result;

    private RefineService    service;

    // private TableViewer viewer;

    private ImportingJob     importJob;

    private FormatAndOptions formatOptions;


    // private Composite tableComposite;

    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    public void init( @SuppressWarnings("hiding" ) ImporterSite site, IProgressMonitor monitor) {
        this.site = site;

        site.icon.set( P4Plugin.images().svgImage( "cvs.svg", NORMAL24 ) );
        site.summary.set( "CSV / TSV / separator based file: " + file.getName() );
        // site.description.set("Description");

        ServiceReference<?> serviceReference = P4Plugin.instance().getBundle().getBundleContext()
                .getServiceReference( RefineService.class.getName() );
        service = (RefineService)P4Plugin.instance().getBundle().getBundleContext()
                .getService( serviceReference );
        try {
            ImportResponse response = service.importFile( new FileInputStream( file ),
                    file.getName(),
                    MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType( file ) );
            importJob = response.job();
            formatOptions = response.options();
            // throw new RuntimeException("blah");
        }
        catch (Exception e) {
            // MdToast mdToast = .createToast(70, SWT.NONE);
            // IssueReporter issueReporter = new IssueReporter(mdToast);
            // issueReporter.showIssue(org.polymap.rhei.batik.toolkit.md.AbstractFeedbackComponent.MessageType.ERROR,
            // "my message");
        }
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {
        // charset prompt
        site.newPrompt( "encoding" ).summary.put( "Zeichensatz der Daten" ).description
                .put( "Die Daten können bspw. deutsche Umlaute enthalten, die nach dem Hochladen falsch dargestellt werden. Mit dem Ändern des Zeichensatzes kann dies korrigiert werden." ).extendedUI
                        .put( ( prompt, parent ) -> {
                            // select box
                            Combo combo = new Combo( parent, SWT.SINGLE );
                            List<String> encodings = Lists.newArrayList( Charsets.ISO_8859_1.name(),
                                    Charsets.US_ASCII.name(), Charsets.UTF_8.name(),
                                    Charsets.UTF_16.name() );
                            combo.setItems( encodings.toArray(new String[encodings.size()]) );
                            combo.addSelectionListener( new SelectionAdapter() {

                                @Override
                                public void widgetSelected( SelectionEvent e ) {
                                    Combo c = (Combo)e.getSource();
                                    String selected = encodings.get( c.getSelectionIndex() );
                                    formatOptions.setEncoding( selected );
                                    service.updateOptions( importJob, formatOptions );
                                    prompt.ok.set( true );
                                }
                            } );
                            int index = encodings.indexOf( formatOptions.encoding() );
                            if (index != -1) {
                                combo.select( index );
                            }
                            return parent;
                        } );
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        throw new UnsupportedOperationException();

    }


    @Override
    public Composite createResultViewer( Composite parent ) {
        Composite tableComposite = new Composite( parent, SWT.FILL );
        if (importJob != null) {
            log.info( "creating new table" );
            TableColumnLayout tableColumnLayout = new TableColumnLayout();
            tableComposite.setLayout( tableColumnLayout );
            TableViewer viewer = new TableViewer( tableComposite, SWT.MULTI | SWT.H_SCROLL
                    | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL );
            viewer.setContentProvider( ArrayContentProvider.getInstance() );
            viewer.setUseHashlookup( true );
            Table table = viewer.getTable();
            table.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
            table.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
            table.setHeaderVisible( true );
            table.setLinesVisible( true );
            table.setTouchEnabled( true );
            ColumnViewerToolTipSupport.enableFor( viewer );

            // menu
            Menu contextMenu = new Menu( viewer.getTable() );
            viewer.getTable().setMenu( contextMenu );
            MenuItem i1 = new MenuItem( contextMenu, SWT.CHECK );
            i1.setText( "foo" );
            i1.addSelectionListener( new SelectionAdapter() {

                @Override
                public void widgetSelected( SelectionEvent e ) {
                    System.out.println( e );
                }
            } );

            // if (importJob == null) {
            // viewer.getTable().setVisible( false );
            // }
            // else {
            // viewer.getTable().setVisible( true );
            // viewer.setItemCount(importJob.project.rows.size());
            // add columns
            // add Index and Action Column
            TableViewerColumn actionsNameCol = new TableViewerColumn( viewer, SWT.NONE );
            viewer.setData( RWT.FIXED_COLUMNS, new Integer( 1 ) );
            actionsNameCol.setLabelProvider( new ColumnLabelProvider() {

                // make sure you dispose these buttons when viewer input
                // changes
                Map<Object,Button> buttons = new HashMap<Object,Button>();


                @Override
                public void update( ViewerCell cell ) {

                    TableItem item = (TableItem)cell.getItem();
                    // item.setText( 0, cell.getViewerRow(). );
                    Button button;
                    if (buttons.containsKey( cell.getElement() )) {
                        button = buttons.get( cell.getElement() );
                    }
                    else {
                        button = new Button( (Composite)cell.getViewerRow().getControl(),
                                SWT.NONE );
                        button.setText( "Remove" );
                        buttons.put( cell.getElement(), button );
                    }
                    TableEditor editor = new TableEditor( item.getParent() );
                    editor.grabHorizontal = true;
                    editor.grabVertical = true;
                    editor.setEditor( button, item, cell.getColumnIndex() );
                    editor.layout();
                }

            } );
            ((TableColumnLayout)table.getParent().getLayout())
                    .setColumnData( actionsNameCol.getColumn(), new ColumnPixelData( 80, false ) );

            ColumnModel columnModel = importJob.project.columnModel;
            int index = 0;
            for (Column column : columnModel.columns) {
                TableViewerColumn viewerColumn = new TableViewerColumn( viewer, SWT.NONE );
                TableColumn tableColumn = viewerColumn.getColumn();
                tableColumn.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
                tableColumn.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                tableColumn.setText( column.getName() );
                tableColumn.setToolTipText( column.getOriginalHeaderLabel() );

                // TODO, calculate weigth per column
                int weigth = 100;
                ((TableColumnLayout)table.getParent().getLayout()).setColumnData( tableColumn,
                        new ColumnWeightData( weigth, 80, true ) );

                // tableColumn.setWidth(50);
                final int currentIndex = index;
                viewerColumn.setLabelProvider( new ColumnLabelProvider() {

                    @Override
                    public void update( ViewerCell cell ) {
                        super.update( cell );
                        TableItem item = (TableItem)cell.getItem();
                        item.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
                        item.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                        item.setData( /*
                                       * MarkupValidator. MARKUP_VALIDATION_DISABLED
                                       */"org.eclipse.rap.rwt.markupValidationDisabled", false );

                    }


                    @Override
                    public String getText( Object element ) {
                        Row row = (Row)element;
                        Cell cell = row.cells.get( currentIndex );
                        String value = cell == null || cell.value == null ? "" //$NON-NLS-1$
                                : cell.value.toString().replace( "\n", "<br/>" ).replace( "&",
                                        "&amp;" );
                        log.info( value );
                        return value;
                    }


                    @Override
                    public String getToolTipText( Object element ) {
                        return getText( element );
                    }
                } );
                index++;

                // viewer.refresh();
                // columnModel.columns.size()));
            }
            viewer.setInput( importJob.project.rows );// createMockData(importJob.project.rows.size(),
            // tableComposite.layout(true);
            // table.setRedraw(true);
        }
        return tableComposite;
    }


    //
    // private Object createMockData(int rowCount, int columnCount) {
    // String[][] rows = new String[rowCount][columnCount];
    // for (int i = 0; i < rowCount; i++) {
    // for (int j = 0; j < columnCount; j++) {
    // rows[i][j] = i + ":" + j;
    // }
    // }
    // return rows;
    // }
    @Override
    public boolean verify( IProgressMonitor monitor ) {
        // update the format
        return false;
    }
}
