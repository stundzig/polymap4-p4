/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.importer.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class KMLFeatureIteratorTest {

    @Test
    public void zippedFile() throws IOException {
        File file = new File( this.getClass().getResource( "BTW2013BE_Abgeordnetenhauswahlkreise.kmz" ).getFile() );
        KMLFeatureIterator it = new KMLFeatureIterator( file, "foo" );
        SimpleFeatureType featureType = it.getFeatureType();
        int i = 0;
        while (it.hasNext()) {
            SimpleFeature feature = (SimpleFeature)it.next();
            Object defaultGeometry = feature.getDefaultGeometry();
            GeometryAttribute defaultGeometryProperty = feature.getDefaultGeometryProperty();
            i++;
        }
        it.close();
        assertEquals( 78, i );
        assertEquals( null, featureType.getName().getNamespaceURI() );
        assertEquals( "foo", featureType.getName().getLocalPart() );
    }


    @Test
    public void normalFile() throws IOException {
        File file = new File( this.getClass().getResource( "doc.kml" ).getFile() );
        KMLFeatureIterator it = new KMLFeatureIterator( file, "foo" );
        SimpleFeatureType featureType = it.getFeatureType();
        int i = 0;
        while (it.hasNext()) {
            SimpleFeature feature = it.next();
            assertNull( feature.getIdentifier());
            i++;
        }
        it.close();
        assertEquals( 78, i );
        assertEquals( null, featureType.getName().getNamespaceURI() );
        assertEquals( "foo", featureType.getName().getLocalPart() );
    }
}
