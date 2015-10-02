/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.imports.csv;

import static org.polymap.rhei.batik.toolkit.md.dp.dp;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.ClientFile;
import org.eclipse.rap.rwt.dnd.ClientFileTransfer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
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
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.p4.Messages;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.map.ProjectMapPanel;
import org.polymap.rap.updownload.upload.IUploadHandler;
import org.polymap.rap.updownload.upload.Upload;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.md.MdListViewer;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import com.google.refine.importing.ImportingJob;
import com.google.refine.model.Cell;
import com.google.refine.model.Column;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CSVImportPanel
        extends DefaultPanel {

    public static final PanelIdentifier ID   = PanelIdentifier.parse( "csvimport" );

    private static final IMessages      i18n = Messages.forPrefix( "CSVImportPanel" );

    // todo handle start/stop of this service
    private RefineService               service;

    private TableViewer                 viewer;

    private ImportingJob                importJob;

    private FormatAndOptions            formatOptions;

    private Composite                   tableComposite;


    public CSVImportPanel() {
        super();
        ServiceReference<?> serviceReference = P4Plugin.instance().getBundle().getBundleContext()
                .getServiceReference( RefineService.class.getName() );
        service = (RefineService)P4Plugin.instance().getBundle().getBundleContext()
                .getService( serviceReference );

    }


    @Override
    public boolean wantsToBeShown() {
        return parentPanel().filter( parent -> parent instanceof ProjectMapPanel ).map( parent -> {
            setTitle();
            getSite().setPreferredWidth( 500 );
            return true;
        } ).orElse( false );
    }


    private void setTitle() {
        getSite().setTitle( "Import CSV" );
    }


    @Override
    public void createContents( Composite parent ) {
        setTitle();

        parent.setLayout( FormLayoutFactory.defaults().spacing( dp( 16 ).pix() ).create() );
        MdToolkit tk = (MdToolkit)getSite().toolkit();

        Composite upload = createUpload( parent );

        FormDataFactory.on( upload ).top( 0 );

        // IPanelSection tableSection = tk.createPanelSection( parent, "data",
        // SWT.NONE );
        Composite table = createTable( parent );
        FormDataFactory.on( table ).fill().top( upload, 5 );

        // Button importFab = createImportFAB( tk );
        // MdToast mdToast = tk.createToast( 70, SWT.NONE );

        // MdListViewer fileList = tk.createListViewer( parent, SWT.VIRTUAL,
        // SWT.FULL_SELECTION );

        // IssueReporter issueReporter = new IssueReporter( mdToast );
        // List<FileDescription> files = new ArrayList<FileDescription>();
        // ShapeImportPanelUpdater shapeImportPanelUpdater =
        // createShapeImportPanelUpdater( importFab, issueReporter,
        // files, fileList );

        // final UploadHelper uploadHelper = new UploadHelper( files,
        // shapeImportPanelUpdater, issueReporter );
        // DropTargetAdapter dropTargetAdapter =
        // uploadHelper.createDropTargetAdapter();

        // initFileList( fileList, shapeImportPanelUpdater, issueReporter,
        // dropTargetAdapter, files );
    }
    //
    // public class MyLabelProvider extends LabelProvider implements
    // ITableLabelProvider {
    //
    // public Image getColumnImage(Object element, int columnIndex) {
    // return null;
    // }
    //
    // public String getColumnText(Object element, int columnIndex) {
    // return "Column " + columnIndex + " => " + element.toString();
    // }
    // }


    private Composite createTable( Composite parent ) {
        tableComposite = new Composite( parent, SWT.FILL );
        // final Table table = new Table( tableComposite,
        // SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER |
        // SWT.VIRTUAL );
        // viewer = new TableViewer( tableComposite, SWT.MULTI | SWT.H_SCROLL |
        // SWT.V_SCROLL
        // | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL );
        // viewer.setContentProvider(new ILazyContentProvider() {
        //
        // @Override
        // public void inputChanged(Viewer viewer, Object oldInput, Object
        // newInput) {
        // }
        //
        // @Override
        // public void dispose() {
        // }
        //
        // @Override
        // public void updateElement(int index) {
        // viewer.replace(importJob.project.rows.get(index), index);
        // }
        // });
        // viewer.setLabelProvider(new LabelProvider() {
        // @Override
        // public String getText(Object element) {
        // return String.valueOf(((Cell)element).value);
        // }
        // });

        // viewer.setContentProvider(new IStructuredContentProvider() {
        //
        // @Override
        // public void inputChanged(Viewer viewer, Object oldInput, Object
        // newInput) {
        // System.out.println("inputChanged");
        // }
        //
        // @Override
        // public void dispose() {
        // }
        //
        // @SuppressWarnings("unchecked")
        // @Override
        // public Object[] getElements(Object inputElement) {
        // System.out.println(inputElement);
        // Object[] rows = ((List<Row>) inputElement).toArray();
        // System.out.println(rows.length + " rows");
        // return rows;
        // }
        // });

        // table.
        // viewer.setLabelProvider(new MyLabelProvider());

        // refreshViewer();
        return tableComposite;
    }


    @SuppressWarnings("unchecked")
    private void recreateViewer() {

        UIThreadExecutor.async( () -> {
            if (viewer != null) {
                Table table = viewer.getTable();
                // Composite tableComposite = table.getParent();
                table.dispose();
            }
            if (importJob != null) {
                TableColumnLayout tableColumnLayout = new TableColumnLayout();
                tableComposite.setLayout( tableColumnLayout );
                viewer = new TableViewer( tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
                        | SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL );
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
                ((TableColumnLayout)table.getParent().getLayout()).setColumnData(
                        actionsNameCol.getColumn(), new ColumnPixelData( 80, false ) );

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
                            new ColumnWeightData( weigth, 40, true ) );

                    // tableColumn.setWidth(50);
                    final int currentIndex = index;
                    viewerColumn.setLabelProvider( new ColumnLabelProvider() {

                        @Override
                        public void update( ViewerCell cell ) {
                            super.update( cell );
                            TableItem item = (TableItem)cell.getItem();
                            item.setData( RWT.TOOLTIP_MARKUP_ENABLED, Boolean.TRUE );
                            item.setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
                            item.setData(
                                    /*
                                     * MarkupValidator.MARKUP_VALIDATION_DISABLED
                                     */"org.eclipse.rap.rwt.markupValidationDisabled", false );

                        }


                        @Override
                        public String getText( Object element ) {
                            Row row = (Row)element;
                            Cell cell = row.cells.get( currentIndex );
                            return cell == null || cell.value == null ? "" //$NON-NLS-1$
                                    : cell.value.toString().replace( "\n", "<br/>" ).replace( "&",
                                            "&amp;" );
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
                tableComposite.layout( true );
                // table.setRedraw(true);
            }

        } , throwable -> throwable.printStackTrace() );
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


    private Composite createUpload( Composite parent ) {
        // DnD and upload
        Upload upload = new Upload( parent, SWT.NONE/* , Upload.SHOW_PROGRESS */ );
        upload.setImage(
                P4Plugin.images().svgImage( "upload.svg", SvgImageRegistryHelper.NORMAL48 ) );
        upload.setText( "" );
        upload.setData( RWT.TOOLTIP_MARKUP_ENABLED, true );
        upload.setData(
                /* MarkupValidator.MARKUP_VALIDATION_DISABLED */"org.eclipse.rap.rwt.markupValidationDisabled",
                false );
        upload.setToolTipText( "<b>Drop</b> files here<br/>or <b>click</b> to open file dialog" );
        upload.setHandler( new IUploadHandler() {

            @Override
            public void uploadStarted( ClientFile clientFile, InputStream in ) throws Exception {
                ImportResponse response = service.importFile( in, clientFile.getName(),
                        clientFile.getType() );
                fileImported( response );
            }

        } );

        upload.moveAbove( null );
        //
        // DropTarget labelDropTarget = new DropTarget( upload, DND.DROP_MOVE );
        // labelDropTarget.setTransfer( new Transfer[] {
        // ClientFileTransfer.getInstance() } );
        // labelDropTarget.addDropListener( dropTargetAdapter );
        return upload;
    }


    private void fileImported( ImportResponse response ) {
        importJob = response.job();
        formatOptions = response.options();
        recreateViewer();
    }


    //
    // private MdListViewer initFileList( MdListViewer fileList,
    // ShapeImportPanelUpdater shapeImportPanelUpdater,
    // IssueReporter issueReporter, DropTargetAdapter dropTargetAdapter,
    // List<FileDescription> files ) {
    //
    // initFileListProviders( files, fileList, shapeImportPanelUpdater );
    // // label providers have to be in place here, as otherwise the tileHeight
    // // wouldn't be
    // // adjusted and then the custom height is 0, leading to a / by zero in
    // // getVisibleRowCount
    // initFileListWithContent( issueReporter, files, fileList,
    // shapeImportPanelUpdater );
    //
    // initOpenListener( fileList );
    //
    // enableDropTarget( dropTargetAdapter, fileList );
    //
    // ColumnViewerToolTipSupport.enableFor( fileList );
    //
    // fileList.getControl().setData( RWT.MARKUP_ENABLED, true );
    // fileList.getControl().setData(
    // /* MarkupValidator.MARKUP_VALIDATION_DISABLED
    // */"org.eclipse.rap.rwt.markupValidationDisabled", false );
    //
    // FormDataFactory.on( fileList.getControl() ).fill().bottom( 90, -5 );
    // return fileList;
    // }
    //
    //
    // private ShapeImportPanelUpdater createShapeImportPanelUpdater( Button
    // importFab, IssueReporter issueReporter,
    // List<FileDescription> files, MdListViewer fileList ) {
    // ShapeFileImporter shapeFileImporter = new ShapeFileImporter( this );
    // ShapeImportPanelUpdater shapeImportPanelUpdater = new
    // ShapeImportPanelUpdater(
    // files, fileList, importFab,
    // issueReporter, shapeFileImporter );
    // return shapeImportPanelUpdater;
    // }
    //

    private void enableDropTarget( DropTargetAdapter dropTargetAdapter, MdListViewer fileList ) {
        DropTarget fileListDropTarget = new DropTarget( fileList.getControl(), DND.DROP_MOVE );
        fileListDropTarget.setTransfer( new Transfer[] { ClientFileTransfer.getInstance() } );
        fileListDropTarget.addDropListener( dropTargetAdapter );
    }


    private void initOpenListener( MdListViewer fileList ) {
        fileList.addOpenListener( new IOpenListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void open( OpenEvent ev ) {
                org.polymap.core.ui.SelectionAdapter.on( ev.getSelection() )
                        .forEach( ( Object elm ) -> {
                    fileList.toggleItemExpand( elm );
                } );
            }
        } );
    }


    //
    // private void initFileListProviders( List<FileDescription> files,
    // MdListViewer
    // fileList,
    // ShapeImportPanelUpdater shapeImportPanelUpdater ) {
    // fileList.iconProvider.set( new ShapeImportImageLabelProvider() );
    // fileList.firstLineLabelProvider.set( new ShapeImportCellLabelProvider()
    // );
    // fileList.secondLineLabelProvider.set( new MessageCellLabelProvider() );
    // fileList.firstSecondaryActionProvider.set( new
    // ShapeFileDeleteActionProvider(
    // files, shapeImportPanelUpdater ) );
    //// fileList.setLabelProvider( new ShapeImportCellLabelProvider() );
    // }
    //
    //
    // private void initFileListWithContent( IssueReporter issueReporter,
    // List<FileDescription> files,
    // MdListViewer fileList, ShapeImportPanelUpdater shapeImportPanelUpdater )
    // {
    // fileList.setContentProvider( new ShapeImportTreeContentProvider( files )
    // );
    // fileList.setInput( files );
    // InitFileListHelper initFileListHelper = new InitFileListHelper( files,
    // shapeImportPanelUpdater, issueReporter );
    // initFileListHelper.initFileList();
    // }
    //

    private Button createImportFAB( MdToolkit tk ) {
        Button fab = tk.createFab( /*
                                    * P4Plugin.instance().imageForName(
                                    * "resources/icons/png/gray/16/import.png" ),
                                    * SWT.UP | SWT.RIGHT
                                    */ );
        fab.setToolTipText( "Import this uploaded Shapefile" );
        fab.setVisible( false );
        fab.moveAbove( null );
        return fab;
    }
}
