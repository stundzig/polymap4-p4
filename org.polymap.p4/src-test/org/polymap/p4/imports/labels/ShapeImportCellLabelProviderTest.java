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

import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Before;
import org.junit.Test;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportCellLabelProviderTest {

    private ShapeImportCellLabelProvider labelProvider;


    @Before
    public void setUp() {
        labelProvider = new ShapeImportCellLabelProvider();
    }


    @Test
    public void testSetFirstLineForArchive() {
        String expectedLabel;
        FileDescription fileDesc;
        for (ArchiveFormats archiveFormat : ArchiveFormats.values()) {
            expectedLabel = "<b>Shapefile:</b> test." + archiveFormat.getFileExtension();
            fileDesc = new ShapeFileDescription().name.put( "test." + archiveFormat.getFileExtension() ).format
                    .put( archiveFormat );
            executeFirstLineTextTest( fileDesc, expectedLabel );
        }
    }


    @Test
    public void testSetFirstLineForShapefileFromArchive() {
        String expectedLabel;
        FileDescription fileDesc;
        for (ArchiveFormats archiveFormat : ArchiveFormats.values()) {
            expectedLabel = "<b>Shapefile:</b> test." + archiveFormat.getFileExtension() + " / testshape";
            fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).name.put( "test."
                    + archiveFormat.getFileExtension() ).format.put( archiveFormat );
            executeFirstLineTextTest( fileDesc, expectedLabel );
        }
    }


    @Test
    public void testSetFirstLineForShapefile() {
        String expectedLabel;
        FileDescription fileDesc;
        for (ArchiveFormats archiveFormat : ArchiveFormats.values()) {
            expectedLabel = "<b>Shapefile:</b> testshape";
            fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format.put( archiveFormat );
            executeFirstLineTextTest( fileDesc, expectedLabel );
        }
    }


    @Test
    public void testSetFirstLineForShapefilePart() {
        String expectedLabel;
        FileDescription parentFile = new FileDescription();
        FileDescription fileDesc;
        for (ShapeFileFormats shapeFileFormat : ShapeFileFormats.values()) {
            expectedLabel = "test." + shapeFileFormat.getFileExtension();
            fileDesc = new ShapeFileDescription().parentFile.put( parentFile ).groupName.put( "testshape" ).name.put( "test."
                    + shapeFileFormat.getFileExtension() ).format.put( shapeFileFormat );
            executeFirstLineTextTest( fileDesc, expectedLabel );
        }
    }


    private void executeFirstLineTextTest( Object elementAssociatedWithCell, String expectedLabel ) {
        ViewerCell viewerCell = mock( ViewerCell.class );
        ViewerRow viewerRow = mock( ViewerRow.class );
        TreeItem treeItem = mock( TreeItem.class );

        when( viewerCell.getElement() ).thenReturn( elementAssociatedWithCell );
        when( viewerCell.getViewerRow() ).thenReturn( viewerRow );
        when( viewerRow.getItem() ).thenReturn( treeItem );
        when( treeItem.getExpanded() ).thenReturn( true );

        labelProvider.update( viewerCell );

        verify( treeItem ).setData( RWT.CUSTOM_VARIANT, "firstRow" );
        verify( viewerCell ).setText( expectedLabel );
    }
}
