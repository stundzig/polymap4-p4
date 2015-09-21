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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    private ShapeFileValidator              shapeFileValidator;

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

        EventManager eventManager = executeTest( group, false );

        String expectedMessage = "txt is not a valid shape file extension";
        int expectedSeverity = IStatus.ERROR;
        assertValidationEvent( eventManager, expectedMessage, expectedSeverity, fd );
    }
    
    @Test
    public void testAlreadyExistingCatalogEntry() {
        FileDescription group = new FileDescription().groupName.put( "test" );

        EventManager eventManager = executeTest( group, false, "test" );

        String expectedMessage = "test is already imported as catalog entry.";
        int expectedSeverity = IStatus.ERROR;
        assertValidationEvent( eventManager, expectedMessage, expectedSeverity, group );
    }

    
    private void assertValidationEvent( EventManager eventManager, String expectedMessage,
            int expectedSeverity, FileDescription fd ) {
        Mockito.verify( eventManager ).publish( captor.capture() );

        Assert.assertEquals( expectedMessage, captor.getValue().getMessage() );
        Assert.assertEquals( expectedSeverity, captor.getValue().getSeverity() );
        Assert.assertEquals( fd, captor.getValue().getSource() );
    }


    private EventManager executeTest( FileDescription fd, boolean expectedValid, String... existingCatalogEntries ) {
        LocalCatalog localCatalog = Mockito.mock( LocalCatalog.class );
        MetadataQuery metadataQuery = Mockito.mock( MetadataQuery.class );
        EventManager eventManager = Mockito.mock( EventManager.class );

        mockExistingCatalogEntries( localCatalog, metadataQuery, existingCatalogEntries );

        ShapeFileValidator.setEventManager( eventManager );
        shapeFileValidator.setLocalCatalog( localCatalog );

        Assert.assertEquals( expectedValid, shapeFileValidator.validate( fd ) );

        return eventManager;
    }


    private void mockExistingCatalogEntries( LocalCatalog localCatalog, MetadataQuery metadataQuery, String[] existingCatalogEntries  ) {
        Mockito.when( localCatalog.query( "" ) ).thenReturn( metadataQuery );
        ResultSet resultSet = new ResultSet() {

            @Override
            public Iterator<IMetadata> iterator() {
                Arrays.asList( existingCatalogEntries).stream().map( existingCatalogEntry -> new IMetadata() {

                    @Override
                    public String getIdentifier() {
                        return existingCatalogEntry;
                    }

                    @Override
                    public String getTitle() {
                        return existingCatalogEntry;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Set<String> getKeywords() {
                        return Collections.emptySet();
                    }

                    @Override
                    public Map<String,String> getConnectionParams() {
                        return Collections.emptyMap();
                    }
                    
                });
                return new ArrayList<IMetadata>().iterator();
            }


            @Override
            public int size() {
                return existingCatalogEntries.length;
            }


            @Override
            public void close() {
            }
        };
        Mockito.when( metadataQuery.execute() ).thenReturn( resultSet );
    }
}
