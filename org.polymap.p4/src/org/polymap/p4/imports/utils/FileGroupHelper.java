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
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.geotools.data.PrjFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.polymap.p4.imports.formats.FileDescription;
import org.polymap.p4.imports.formats.IFileFormat;
import org.polymap.p4.imports.formats.ShapeFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.DbfFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.PrjFileDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShapeFileRootDescription;
import org.polymap.p4.imports.formats.ShapeFileDescription.ShpFileDescription;
import org.polymap.p4.imports.formats.ShapeFileFormats;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class FileGroupHelper {

    public static Map<String,List<File>> groupFilesByName( List<File> fs ) {
        Map<String,List<File>> files = new HashMap<String,List<File>>();
        for (File f : fs) {
            int index = f.getName().lastIndexOf( "." + ShapeFileFormats.SHP_XML.getFileExtension() );
            if (index == -1) {
                index = f.getName().lastIndexOf( "." );
            }
            if (index > 0) {
                String fName = f.getName().substring( 0, index );
                List<File> gFs = files.get( fName );
                if (gFs == null) {
                    gFs = new ArrayList<File>();
                }
                gFs.add( f );
                files.put( fName, gFs );
            }
        }
        return files;
    }


    public static void fillFilesList( List<FileDescription> files, String rootFileName, long fileSize, List<File> read ) {
        Map<String,List<File>> grouped = FileGroupHelper.groupFilesByName( read );
        String localRootFileName = rootFileName;
        if (rootFileName != null) {
            if (files.stream().anyMatch( f -> rootFileName.equals( f.name.get() ) )) {
                localRootFileName = "_duplicated";
            }
        }
        FileDescription root;
        for (String groupName : grouped.keySet()) {
            try {
                List<File> values = grouped.get( groupName );
                Optional<File> shpFile = values
                        .stream()
                        .filter(
                                file -> ShapeFileFormats.SHP.getFileExtension().equalsIgnoreCase(
                                        IFileFormat.getFileExtension( file.getName() ) ) ).findFirst();
                DbaseFileHeader dbaseHeader = null;
                ShapefileHeader shapeHeader = null;
                if (shpFile.isPresent()) {
                    ShpFiles shpFilesPart = new ShpFiles( shpFile.get() );
                    dbaseHeader = parseDbaseFileHeader( shpFilesPart );
                    shapeHeader = parseShapeFile( shpFilesPart );
                    // TODO: where to fetch feature type from?
                    root = new ShapeFileRootDescription().featureCount.put( dbaseHeader != null ? dbaseHeader
                            .getNumRecords() : -1 )/*.featureType.put( shapeHeader != null ? shapeHeader.getShapeType() : null )*/;
                }
                else {
                    root = new FileDescription();
                }
                files.add( root );
                root.groupName.put( groupName );
                if (localRootFileName != null) {
                    root.name.put( localRootFileName ).format.put( IFileFormat.getKnownFileFormat( localRootFileName ) );
                }
                if (fileSize != -1) {
                    root.size.set( fileSize );
                }
                FileDescription fileDesc;
                values.sort( (File file1, File file2) -> file1.getName().compareTo( file2.getName() ) );
                for (File file : values) {
                    ShapeFileFormats format = ShapeFileFormats.getFileFormat( file );
                    if (format != null) {
                        if (format == ShapeFileFormats.DBF) {
                            // TODO: where to fetch charset from?
                            fileDesc = new DbfFileDescription()/*.charset.put( "UTF-8" )*/;
                        }
                        else if (format == ShapeFileFormats.PRJ) {
                            CoordinateReferenceSystem crs = parseCRS(file);
                            fileDesc = new PrjFileDescription().crs.put( crs );
                        }
                        else if (format == ShapeFileFormats.SHP) {
                            fileDesc = new ShpFileDescription().geometryType.put( shapeHeader != null ? shapeHeader.getShapeType() : null );
                        }
                        else {
                            fileDesc = new ShapeFileDescription();
                        }
                    }
                    else {
                        fileDesc = new FileDescription();
                    }
                    fileDesc.groupName.put( groupName ).name.put( file.getName() ).file.put( file ).size.put( file.length() ).format
                            .put( IFileFormat.getKnownFileFormat( file.getName() ) );
                    root.addContainedFile( fileDesc );
                }
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


    private static ShapefileHeader parseShapeFile( ShpFiles shpFilesPart ) {
        ShapefileReader shapefileReader = null;
        try {
            shapefileReader = new ShapefileReader( shpFilesPart, false, false, new GeometryFactory() );
            return shapefileReader.getHeader();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if(shapefileReader != null) {
                try {
                    shapefileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static DbaseFileHeader parseDbaseFileHeader( ShpFiles shpFilesPart ) {
        DbaseFileReader dbaseFileReader = null;
        try {
            dbaseFileReader = new DbaseFileReader( shpFilesPart, false, Charset.forName( "UTF-8" ),
                    TimeZone.getDefault() );
            return dbaseFileReader.getHeader();
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (dbaseFileReader != null) {
                try {
                    dbaseFileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static CoordinateReferenceSystem parseCRS( File prjFile ) {
        PrjFileReader prjFileReader = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(prjFile);
            FileChannel channel = in.getChannel();
            prjFileReader = new PrjFileReader( channel );
            return prjFileReader.getCoordinateReferenceSystem();
        }
        catch (IOException | FactoryException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if (prjFileReader != null) {
                try {
                    prjFileReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
