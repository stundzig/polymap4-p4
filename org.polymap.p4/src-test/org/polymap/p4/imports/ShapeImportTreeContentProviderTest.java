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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.polymap.p4.imports.formats.FileDescription;


/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public class ShapeImportTreeContentProviderTest {

    @Test
    public void testEmptyList() {
        List<FileDescription> fds = new ArrayList<FileDescription>();
        ShapeImportTreeContentProvider contentProvider = new ShapeImportTreeContentProvider(fds);
        Assert.assertEquals(0, contentProvider.getElements( null ).length);
        Assert.assertEquals(null, contentProvider.getParent( null ));
        Assert.assertEquals(null, contentProvider.getParent( new Object() ));
        Assert.assertEquals(0, contentProvider.getChildren( null ).length);
        Assert.assertFalse(contentProvider.hasChildren( null ));
        Assert.assertFalse(contentProvider.hasChildren( new Object() ));
    }

    @Test
    public void testRootWithoutChildren() {
        List<FileDescription> fds = new ArrayList<FileDescription>();
        FileDescription fd = new FileDescription();
        fds.add( fd );
        ShapeImportTreeContentProvider contentProvider = new ShapeImportTreeContentProvider(fds);
        Assert.assertEquals(1, contentProvider.getElements( null ).length);
        Assert.assertEquals(null, contentProvider.getParent( fd ));
        Assert.assertEquals(0, contentProvider.getChildren( fd ).length);
        Assert.assertFalse(contentProvider.hasChildren( fd ));
    }
    
    @Test
    public void testRootWithChildren() {
        List<FileDescription> fds = new ArrayList<FileDescription>();
        FileDescription root = new FileDescription();
        FileDescription child = new FileDescription();
        child.parentFile.set( root );
        root.getContainedFiles().add( child );
        fds.add( root );
        ShapeImportTreeContentProvider contentProvider = new ShapeImportTreeContentProvider(fds);
        Assert.assertEquals(1, contentProvider.getElements( null ).length);
        Assert.assertEquals(root, contentProvider.getParent( child ));
        Assert.assertEquals(1, contentProvider.getChildren( root ).length);
        Assert.assertTrue(contentProvider.hasChildren( root ));
    }

    @Test
    public void testMultipleRoots() {
        List<FileDescription> fds = new ArrayList<FileDescription>();
        FileDescription fd1 = new FileDescription();
        FileDescription fd2 = new FileDescription();
        FileDescription fd3 = new FileDescription();
        fds.add( fd1 );
        fds.add( fd2 );
        fds.add( fd3 );
        ShapeImportTreeContentProvider contentProvider = new ShapeImportTreeContentProvider(fds);
        Assert.assertEquals(3, contentProvider.getElements( null ).length);
    }

    @Test
    public void testMultipleChildren() {
        List<FileDescription> fds = new ArrayList<FileDescription>();
        FileDescription root = new FileDescription();
        FileDescription fd1 = new FileDescription();
        fd1.parentFile.set( root );
        FileDescription fd2 = new FileDescription();
        fd2.parentFile.set( root );
        FileDescription fd3 = new FileDescription();
        fd3.parentFile.set( root );
        root.getContainedFiles().add( fd1 );
        root.getContainedFiles().add( fd2 );
        root.getContainedFiles().add( fd3 );
        fds.add( root );
        ShapeImportTreeContentProvider contentProvider = new ShapeImportTreeContentProvider(fds);
        Assert.assertEquals(1, contentProvider.getElements( null ).length);
        Assert.assertEquals(root, contentProvider.getParent( fd2 ));
        Assert.assertEquals(3, contentProvider.getChildren( root ).length);
        Assert.assertTrue(contentProvider.hasChildren( root ));
    }
}
