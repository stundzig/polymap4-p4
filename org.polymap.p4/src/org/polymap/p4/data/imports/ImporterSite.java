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

import java.util.EventObject;
import java.util.Optional;

import org.eclipse.swt.graphics.Image;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.p4.data.imports.ImportDriver.ImporterSiteImpl;

/**
 * 
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ImporterSite
        extends Configurable {

    @Concern( FireEvent.class )
    public Config2<ImporterSite,Image>      icon;

    @Concern( FireEvent.class )
    public Config2<ImporterSite,String>     summary;

    @Concern( FireEvent.class )
    public Config2<ImporterSite,String>     description;

    
    public abstract ImportPrompt newPrompt( String id );

    public abstract Optional<ImportPrompt> prompt( String id );

    
    /**
     * 
     */
    public static class FireEvent
            extends DefaultPropertyConcern {

        /**
         * This is called *before* the {@link Config2} property is set. However, there is no
         * race condition between event handler thread, that might access property value, and
         * the current thread, that sets the property value, because most {@link EventHandler}s
         * are done in display thread.
         */
        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            ImporterSiteImpl importerSite = prop.info().getHostObject();
            EventManager.instance().syncPublish( new ImporterChangeEvent( importerSite.importer ) );
            return newValue;
        }
    }
    

    /**
     * 
     */
    public static class ImporterChangeEvent
            extends EventObject {

        public ImporterChangeEvent( Importer source ) {
            super( source );
        }

        @Override
        public Importer getSource() {
            return (Importer)super.getSource();
        }
    }

}
