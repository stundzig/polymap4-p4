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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.polymap.p4.imports.ShapeFileValidator;
import org.polymap.p4.imports.ValidationEvent;
import org.polymap.p4.imports.formats.ArchiveFormats;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.DbfFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.PrjFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShapeFileRootDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShpFileDescription;
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
        FileDescription fileDesc;
        for (ArchiveFormats archiveFormat : ArchiveFormats.values()) {
            expectedLabel = "<b>description:</b> " + archiveFormat.getDescription();
            fileDesc = new FileDescription().name.put( "test." + archiveFormat.getFileExtension() ).format
                    .put( archiveFormat );
            executeSecondLineTextTest( fileDesc, expectedLabel );
        }
    }


    @Test
    public void testSetSecondLineForShapefileFromArchive() {
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName( "myFeatureType" );
        SimpleFeatureType featureType = featureTypeBuilder.buildFeatureType();
        ShapeFileRootDescription fileDesc = new ShapeFileRootDescription().featureType.put( featureType ).featureCount
                .put( 30 ).size.put( 50567893L ).name.put( "test." + ArchiveFormats.ZIP.getFileExtension() ).format
                .put( ArchiveFormats.ZIP ).groupName.put( "testshape" );
        executeSecondLineTextTest(
                fileDesc,
                "<b>description:</b> application/zip, <b>feature type:</b> myFeatureType, <b>feature count:</b> 30, <b>file size:</b> 48,225 MB" );
    }


    @Test
    public void testSetSecondLineForShapefile() {
        SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName( "myFeatureType" );
        SimpleFeatureType featureType = featureTypeBuilder.buildFeatureType();
        ShapeFileRootDescription fileDesc = new ShapeFileRootDescription().featureType.put( featureType ).featureCount
                .put( 30 ).size.put( 50567893L ).groupName.put( "testshape" );
        executeSecondLineTextTest( fileDesc,
                "<b>feature type:</b> myFeatureType, <b>feature count:</b> 30, <b>file size:</b> 48,225 MB" );
    }


    @Test
    public void testSetSecondLineForShapefilePartAIH() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.AIH );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> an attribute index of the active fields in a table" );
    }


    @Test
    public void testSetSecondLineForShapefilePartAIN() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.AIN );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> an attribute index of the active fields in a table" );
    }


    @Test
    public void testSetSecondLineForShapefilePartATX() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.ATX );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> an attribute index for the .dbf file in the form of shapefile.columnname.atx (ArcGIS 8 and later)" );
    }


    @Test
    public void testSetSecondLineForShapefilePartCPG() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.CPG );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> used to specify the code page (only for .dbf) for identifying the character encoding to be used" );
    }


    @Test
    public void testSetSecondLineForShapefilePartDBF() {
        DbfFileDescription fileDesc = new DbfFileDescription().charset.put( "UTF-8" ).groupName.put( "testshape" ).format
                .put( ShapeFileFormats.DBF );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> attribute format; columnar attributes for each shape, in dBase IV format, <b>charset:</b> UTF-8" );
    }


    @Test
    public void testSetSecondLineForShapefilePartFBN() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.FBN );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> a spatial index of the features that are read-only" );
    }


    @Test
    public void testSetSecondLineForShapefilePartFBX() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.FBX );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> a spatial index of the features that are read-only" );
    }


    @Test
    public void testSetSecondLineForShapefilePartIXS() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.IXS );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> a geocoding index for read-write datasets" );
    }


    @Test
    public void testSetSecondLineForShapefilePartMXS() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.MXS );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> a geocoding index for read-write datasets (ODB format)" );
    }


    @Test
    public void testSetSecondLineForShapefilePartPRJ() {
        PrjFileDescription fileDesc = new PrjFileDescription().crs.put( DefaultGeographicCRS.WGS84 ).groupName
                .put( "testshape" ).format.put( ShapeFileFormats.PRJ );
        executeSecondLineTextTest(
                fileDesc,
                "<b>description:</b> projection format; the coordinate system and projection information, a plain text file describing the projection using well-known text format, <b>crs:</b> WGS84(DD)" );
    }


    @Test
    public void testSetSecondLineForShapefilePartQIX() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.QIX );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> an alternative quadtree spatial index used by MapServer and GDAL/OGR software" );
    }


    @Test
    public void testSetSecondLineForShapefilePartSBN() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.SBN );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> a spatial index of the features" );
    }


    @Test
    public void testSetSecondLineForShapefilePartSBX() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.SBX );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> a spatial index of the features" );
    }


    @Test
    public void testSetSecondLineForShapefilePartSHP() {
        ShpFileDescription fileDesc = new ShpFileDescription().geometryType.put( ShapeType.POLYGON ).groupName
                .put( "testshape" ).format.put( ShapeFileFormats.SHP );
        executeSecondLineTextTest( fileDesc, "<b>description:</b> shape format; the feature geometry itself, <b>geometry type:</b> Polygon" );
    }


    @Test
    public void testSetSecondLineForShapefilePartSHP_XML() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.SHP_XML );
        executeSecondLineTextTest( fileDesc,
                "<b>description:</b> geospatial metadata in XML format, such as ISO 19115 or other XML schema" );
    }


    @Test
    public void testSetSecondLineForShapefilePartSHX() {
        ShapeFileDescription fileDesc = new ShapeFileDescription().groupName.put( "testshape" ).format
                .put( ShapeFileFormats.SHX );
        executeSecondLineTextTest(
                fileDesc,
                "<b>description:</b> shape index format; a positional index of the feature geometry to allow seeking forwards and backwards quickly" );
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
        // for error case
        when( textMetricHelper.getTextExtent( viewerCell, "" ) ).thenReturn( new Point( 0, 0 ) );
        when( textMetricHelper.getTextExtent( viewerCell, expectedLabel ) ).thenReturn( textExtent );
        when( textMetricHelper.getFontMetrics( viewerCell ) ).thenReturn( fontMetrics );

        labelProvider.setShapeFileValidator( shapeFileValidator );
        labelProvider.setTextMetricHelper( textMetricHelper );
        labelProvider.update( viewerCell );

        verify( treeItem ).setData( RWT.CUSTOM_VARIANT, "firstRow" );
        verify( viewerCell ).setText( expectedLabel );
    }
}
