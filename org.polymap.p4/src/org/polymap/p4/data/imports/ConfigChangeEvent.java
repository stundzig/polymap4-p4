/* 
 * polymap.org
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

import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConfigChangeEvent
        extends EventObject {

    public ConfigChangeEvent( Object source ) {
        super( source );
        assert source instanceof ImporterSite || source instanceof ImporterPrompt;
    }

    
    /**
     * 
     */
    public static class Fire
            extends DefaultPropertyConcern {

        /**
         * This is called *before* the {@link Config2} property is set. However, there is no
         * race condition between event handler thread, that might access property value, and
         * the current thread, that sets the property value, because most {@link EventHandler}s
         * are done in display thread.
         */
        @Override
        public Object doSet( Object obj, Config prop, Object newValue ) {
            Object source = prop.info().getHostObject();
            EventManager.instance().syncPublish( new ConfigChangeEvent( source ) );
            return newValue;
        }
    }

}
