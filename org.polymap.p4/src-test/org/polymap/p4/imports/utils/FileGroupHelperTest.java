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
package org.polymap.p4.imports.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.shp.ShapeType;
import org.junit.Assert;
import org.junit.Test;
import org.polymap.p4.imports.FileImporter;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.DbfFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.PrjFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShapeFileRootDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShpFileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FileGroupHelperTest {

    @Test
    public void test() throws FileNotFoundException {
        File zipFile = new File( "resources-test/nyc_roads.zip" );
        FileInputStream in = new FileInputStream( zipFile );
        List<File> read = new FileImporter().run( "nyc_roads.zip", "application/zip", in );
        List<FileDescription> files = new ArrayList<FileDescription>();
        String rootFileName = "test";
        long fileSize = 1013L;

        FileGroupHelper.fillFilesList( files, rootFileName, fileSize, read );

        Assert.assertEquals( 1, files.size() );
        Assert.assertTrue( files.get( 0 ) instanceof ShapeFileRootDescription );
        ShapeFileRootDescription root = (ShapeFileRootDescription)files.get( 0 );
        assertRoot( root );

        Assert.assertEquals( 4, root.getContainedFiles().size() );

        assertDbfFile( root );
        assertPrjFile( root );
        assertShpFile( root );
        assertShxFile( root );
    }


    private void assertRoot( ShapeFileRootDescription root ) {
        Assert.assertEquals( new Integer( 1301 ), root.featureCount.get() );
        Assert.assertEquals( "nyc_roads", root.featureType.get().getName().getLocalPart() );
        Assert.assertFalse( root.file.isPresent() );
        Assert.assertFalse( root.format.isPresent() );
        Assert.assertEquals( "nyc_roads", root.groupName.get() );
        Assert.assertEquals( "test", root.name.get() );
        Assert.assertFalse( root.parentFile.isPresent() );
        Assert.assertEquals( new Long( 1013 ), root.size.get() );
    }


    private void assertDbfFile( ShapeFileRootDescription root ) {
        Assert.assertTrue( root.getContainedFiles().get( 0 ) instanceof DbfFileDescription );
        DbfFileDescription dbfFileDesc = (DbfFileDescription)root.getContainedFiles().get( 0 );
        Assert.assertEquals( "nyc_roads.dbf", dbfFileDesc.file.get().getName() );
        Assert.assertEquals( ShapeFileFormats.DBF, dbfFileDesc.format.get() );
        Assert.assertEquals( "nyc_roads", dbfFileDesc.groupName.get() );
        Assert.assertEquals( "nyc_roads.dbf", dbfFileDesc.name.get() );
        Assert.assertEquals( root, dbfFileDesc.parentFile.get() );
        Assert.assertEquals( new Long( 297013 ), dbfFileDesc.size.get() );
        Assert.assertEquals( null, dbfFileDesc.charset.get() );
    }


    private void assertPrjFile( ShapeFileRootDescription root ) {
        Assert.assertTrue( root.getContainedFiles().get( 1 ) instanceof PrjFileDescription );
        PrjFileDescription prjFileDesc = (PrjFileDescription)root.getContainedFiles().get( 1 );
        Assert.assertEquals( "nyc_roads.prj", prjFileDesc.file.get().getName() );
        Assert.assertEquals( ShapeFileFormats.PRJ, prjFileDesc.format.get() );
        Assert.assertEquals( "nyc_roads", prjFileDesc.groupName.get() );
        Assert.assertEquals( "nyc_roads.prj", prjFileDesc.name.get() );
        Assert.assertEquals( root, prjFileDesc.parentFile.get() );
        Assert.assertEquals( new Long( 971 ), prjFileDesc.size.get() );
        Assert.assertEquals( "EPSG:NAD83(HARN) / New York Long Island (ftUS)", prjFileDesc.crs.get().getName()
                .toString() );
    }


    private void assertShpFile( ShapeFileRootDescription root ) {
        Assert.assertTrue( root.getContainedFiles().get( 2 ) instanceof ShpFileDescription );
        ShpFileDescription shpFileDesc = (ShpFileDescription)root.getContainedFiles().get( 2 );
        Assert.assertEquals( "nyc_roads.shp", shpFileDesc.file.get().getName() );
        Assert.assertEquals( ShapeFileFormats.SHP, shpFileDesc.format.get() );
        Assert.assertEquals( "nyc_roads", shpFileDesc.groupName.get() );
        Assert.assertEquals( "nyc_roads.shp", shpFileDesc.name.get() );
        Assert.assertEquals( root, shpFileDesc.parentFile.get() );
        Assert.assertEquals( new Long( 187628 ), shpFileDesc.size.get() );
        Assert.assertEquals( ShapeType.ARC, shpFileDesc.geometryType.get() );
    }


    private void assertShxFile( ShapeFileRootDescription root ) {
        Assert.assertTrue( root.getContainedFiles().get( 3 ) instanceof ShapeFileDescription );
        ShapeFileDescription shxFileDesc = (ShapeFileDescription)root.getContainedFiles().get( 3 );
        Assert.assertEquals( "nyc_roads.shx", shxFileDesc.file.get().getName() );
        Assert.assertEquals( ShapeFileFormats.SHX, shxFileDesc.format.get() );
        Assert.assertEquals( "nyc_roads", shxFileDesc.groupName.get() );
        Assert.assertEquals( "nyc_roads.shx", shxFileDesc.name.get() );
        Assert.assertEquals( root, shxFileDesc.parentFile.get() );
        Assert.assertEquals( new Long( 10508 ), shxFileDesc.size.get() );
    }
}
