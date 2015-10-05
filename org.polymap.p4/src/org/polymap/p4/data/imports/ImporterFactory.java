/* 
 * Copyright (C) 2015, the @authors. All rights reserved.
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
package org.polymap.p4.data.imports;

/**
 * 
 *
 * @see ContextIn
 * @see ContextOut
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ImporterFactory {

    /**
     * Checks to current {@link ContextIn} for applicable objects and builds {@link Importer}s
     * for these ojects.
     */
    public void createImporters( ImporterBuilder builder ) throws Exception;

    
    /**
     * 
     */
    @FunctionalInterface
    public static interface ImporterBuilder {
        
        /**
         * Creates a new {@link Importer} with the given objects in its {@link ContextIn}.
         */
        public void newImporter( Importer importer, Object... contextIn ) throws Exception;
    }
    
}
