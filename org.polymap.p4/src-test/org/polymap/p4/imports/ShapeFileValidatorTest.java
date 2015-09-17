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
package org.polymap.p4.imports;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.MetadataQuery;
import org.polymap.core.catalog.MetadataQuery.ResultSet;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.imports.formats.FileDescription;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ShapeFileValidatorTest {

    private ShapeFileValidator shapeFileValidator;

    @Captor
    private ArgumentCaptor<ValidationEvent> captor;
    
    @Before
    public void setUp() {
        shapeFileValidator = new ShapeFileValidator();
    }


    @Test
    public void testGroupWithoutEntries() {
        FileDescription group = new FileDescription().groupName.put( "test" );
        executeTest( group, true );
    }


    @Test
    public void testGroupWithOneEntryWithInvalidFileExtension() {
        FileDescription group = new FileDescription().groupName.put( "test" );
        FileDescription fd = new FileDescription();

        File file = Mockito.mock( File.class );
        Mockito.when( file.getName() ).thenReturn( "test.txt" );
        fd.file.set( file );

        group.addContainedFile( fd );

        EventManager eventManager = Mockito.mock( EventManager.class );
        ShapeFileValidator.setEventManager( eventManager );

        executeTest( group, false );
        
        Mockito.verify( eventManager ).publish(captor.capture());
        
        Assert.assertEquals("txt is not a valid shape file extension", captor.getValue().getMessage());
        Assert.assertEquals(IStatus.ERROR, captor.getValue().getSeverity());
        Assert.assertEquals(fd, captor.getValue().getSource());
    }


    private void executeTest( FileDescription fd, boolean expectedValid ) {
        LocalCatalog localCatalog = Mockito.mock( LocalCatalog.class );
        MetadataQuery metadataQuery = Mockito.mock( MetadataQuery.class );
        Mockito.when( localCatalog.query( "" ) ).thenReturn( metadataQuery );
        ResultSet resultSet = new ResultSet() {

            @Override
            public Iterator<IMetadata> iterator() {
                return new ArrayList<IMetadata>().iterator();
            }


            @Override
            public int size() {
                return 0;
            }


            @Override
            public void close() {
            }
        };
        Mockito.when( metadataQuery.execute() ).thenReturn( resultSet );

        shapeFileValidator.setLocalCatalog( localCatalog );
        Assert.assertEquals( expectedValid, shapeFileValidator.validate( fd ) );
    }
}
