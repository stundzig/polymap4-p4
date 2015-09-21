/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag. 
 * All rights reserved.
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
package org.polymap.p4.imports.labels;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.polymap.p4.imports.ShapeFileValidator;
import org.polymap.p4.imports.ValidationEvent;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;
import org.polymap.p4.imports.utils.TextMetricHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(FontMetrics.class)
public class MessageCellLabelProviderTest {

    private MessageCellLabelProvider labelProvider;


    @Before
    public void setUp() {
        labelProvider = new MessageCellLabelProvider();
    }


    @Test
    public void testSetSecondLineForArchive() {
        String expectedLabel;
        for (ArchiveFormats archiveFormat : ArchiveFormats.values()) {
            expectedLabel = "description: " + archiveFormat.getDescription();
            executeSecondLineTextTest( "test." + archiveFormat.getFileExtension(), expectedLabel );
        }
    }


    @Test
    public void testSetSecondLineForShapefileFromArchive() {
        executeSecondLineTextTest( "test.zip / testshape", "Shapefile: test.zip / testshape" );
    }


    @Test
    public void testSetSecondLineForShapefile() {
        executeSecondLineTextTest( "testshape", "Shapefile: testshape" );
    }


    @Test
    public void testSetSecondLineForShapefilePart() {
        File file = mock( File.class );
        String fileName;
        for (ShapeFileFormats shapeFileFormat : ShapeFileFormats.values()) {
            fileName = "test." + shapeFileFormat.getFileExtension();
            when( file.getName() ).thenReturn( fileName );
            executeSecondLineTextTest( file, fileName );
        }
    }


    private void executeSecondLineTextTest( File file, String expectedLabel ) {
        FileDescription elementAssociatedWithCell = new FileDescription().name.put( file.getName() );
        executeSecondLineTextTest( elementAssociatedWithCell, expectedLabel );
    }


    private void executeSecondLineTextTest( String fileName, String expectedLabel ) {
        FileDescription elementAssociatedWithCell = new FileDescription().name.put( fileName );
        executeSecondLineTextTest( elementAssociatedWithCell, expectedLabel );
    }


    private void executeSecondLineTextTest( FileDescription elementAssociatedWithCell, String expectedLabel ) {
        ViewerCell viewerCell = mock( ViewerCell.class );
        ViewerRow viewerRow = mock( ViewerRow.class );
        TreeItem treeItem = mock( TreeItem.class );
        ShapeFileValidator shapeFileValidator = mock( ShapeFileValidator.class );
        Control control = mock( Control.class );
        int cellWidth = 100;
        Rectangle bounds = new Rectangle( 0, 0, cellWidth, 0 );
        TextMetricHelper textMetricHelper = mock( TextMetricHelper.class );
        int textWidth = 80;
        Point textExtent = new Point( textWidth, 0 );
        FontMetrics fontMetrics = PowerMockito.mock( FontMetrics.class );

        ValidationEvent validationEvent = new ValidationEvent( elementAssociatedWithCell, IStatus.OK, "" );

        when( shapeFileValidator.validate( elementAssociatedWithCell ) ).then( new Answer<Boolean>() {

            @Override
            public Boolean answer( InvocationOnMock invocation ) throws Throwable {
                labelProvider.handleStatus( viewerCell, validationEvent );
                return Boolean.TRUE;
            }
        } );

        when( viewerCell.getElement() ).thenReturn( elementAssociatedWithCell );
        when( viewerCell.getViewerRow() ).thenReturn( viewerRow );
        when( viewerRow.getItem() ).thenReturn( treeItem );
        when( treeItem.getExpanded() ).thenReturn( true );
        when( viewerCell.getControl() ).thenReturn( control );
        when( control.getBounds() ).thenReturn( bounds );
        when( textMetricHelper.getTextExtent( viewerCell, expectedLabel ) ).thenReturn( textExtent );
        when( textMetricHelper.getFontMetrics( viewerCell ) ).thenReturn( fontMetrics );

        labelProvider.setShapeFileValidator( shapeFileValidator );
        labelProvider.setTextMetricHelper( textMetricHelper );
        labelProvider.update( viewerCell );

        verify( treeItem ).setData( RWT.CUSTOM_VARIANT, "firstRow" );
        verify( viewerCell ).setText( expectedLabel );
    }
}
