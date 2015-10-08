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

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.config.Concern;
import org.polymap.core.runtime.config.Config;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.config.DefaultBoolean;
import org.polymap.core.runtime.config.DefaultPropertyConcern;
import org.polymap.core.runtime.config.Mandatory;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImportPrompt
        extends Configurable {
    
    public enum Severity {
        INFO, VERIFY, MANDATORY;
    }

    /** Defaults to {@link Severity#INFO}. */
    @Mandatory
    @Concern( FireEvent.class )
    public Config2<ImportPrompt,Severity>   severity;

    @Concern( FireEvent.class )
    public Config2<ImportPrompt,String>     summary;

    @Concern( FireEvent.class )
    public Config2<ImportPrompt,String>     description;
    
    @Mandatory
    @DefaultBoolean( false )
    @Concern( FireEvent.class )
    public Config2<ImportPrompt,Boolean>    ok;
    
    @Concern( FireEvent.class )
    public Config2<ImportPrompt,PromptUIBuilder> extendedUI;
    

    protected ImportPrompt() {
        severity.set( Severity.INFO );
    }


    /**
     * 
     */
    @FunctionalInterface
    public static interface PromptUIBuilder {
        
        public Composite createContents( ImportPrompt prompt, Composite parent );
        
    }

    
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
            ImportPrompt prompt = prop.info().getHostObject();
            EventManager.instance().syncPublish( new PromptChangeEvent( prompt ) );
            return newValue;
        }
    }
    

    /**
     * 
     */
    public static class PromptChangeEvent
            extends EventObject {

        public PromptChangeEvent( Object source ) {
            super( source );
        }

        @Override
        public ImportPrompt getSource() {
            return (ImportPrompt)super.getSource();
        }
    }

}