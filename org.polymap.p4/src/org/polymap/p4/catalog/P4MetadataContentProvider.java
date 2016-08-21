/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.catalog;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.IContentProvider;

import org.polymap.core.catalog.DefaultMetadata;
import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.ui.MetadataContentProvider;

/**
 * Creates a dummy entry if not {@link LocalCatalog} and
 * {@link IMetadataCatalog#ALL_QUERY}.
 *
 * @author Falko Bräutigam
 */
public class P4MetadataContentProvider
        extends MetadataContentProvider
        implements IContentProvider {

    private static final Log log = LogFactory.getLog( P4MetadataContentProvider.class );

    /**
     * 
     */
    public static final IMetadata DUMMY = new DefaultMetadata() {
        @Override
        public String getTitle() {
            return "A lot of entries...";
        }
        @Override
        public Optional<String> getDescription() {
            return Optional.of( "Enter a search to filter entries!" );
        }
        @Override
        public String getIdentifier() {
            throw new RuntimeException( "not yet implemented." );
        }
    };

    
    public P4MetadataContentProvider( IMetadataResourceResolver resolver ) {
        super( resolver );
    }

    
    @Override
    protected void updateMetadataCatalog( IMetadataCatalog elm, int currentChildCount ) {
        if (!(elm instanceof LocalCatalog) 
                && catalogQuery.get().equals( IMetadataCatalog.ALL_QUERY )) {
            updateChildren( elm, new IMetadata[] {DUMMY}, currentChildCount );
        }
        else {
            super.updateMetadataCatalog( elm, currentChildCount );
        }
    }
    
}
