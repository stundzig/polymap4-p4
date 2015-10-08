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

import java.util.Optional;

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ImporterSite
        extends Configurable {

    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterSite,Image>      icon;

    /** Short summary of the content of this importer. */
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterSite,String>     summary;

    /** What do I get from this Importer? */
    @Concern( ConfigChangeEvent.Fire.class )
    public Config2<ImporterSite,String>     description;

    
    abstract ImporterContext context();

    public abstract ImporterPrompt newPrompt( String id );

    public abstract Optional<ImporterPrompt> prompt( String id );

}
