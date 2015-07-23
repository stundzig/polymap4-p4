/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4;

import java.util.function.Consumer;

import org.opengis.style.Style;

/**
 * Provides the connection between layers and the style used to renders its content.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IStyleResolver {

    /**
     * 
     *
     * @param uid The UID of the {@link Style} to resolve.
     * @param resultHandler
     * @param errorHandler
     */
    public void resolve( String fsuid, Consumer<Style> resultHandler, Consumer<Throwable> errorHandler );
    
}
