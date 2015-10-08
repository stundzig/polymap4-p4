/* 
 * Copyright (C) 2015, the @autors. All rights reserved.
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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.p4.data.imports.ImporterFactory.ImporterBuilder;
import org.polymap.p4.data.imports.archive.ArchiveFileImporterFactory;
import org.polymap.p4.data.imports.csv.CSVFileImporterFactory;

/**
 * Provides the execution context of an {@link Importer}. It handles inbound context
 * variables, allows to find importers that are able to execute within this context,
 * executes one of them and provides outbound variables for the next level driver.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ImporterContext
        extends Configurable {

    private static Log log = LogFactory.getLog( ImporterContext.class );
    
    // XXX make this an extension point
    private static final Class[]            factories = { ArchiveFileImporterFactory.class, CSVFileImporterFactory.class };
    
    private Importer                        importer;
    
    private ImporterSite                    site;
    
    private Map<Class,Object>               contextIn = new HashMap();
    
    private Map<String,ImportPrompt>        prompts;


    /**
     * Creates a starting context without importer and no context set.
     */
    public ImporterContext() {
    }

    
    /**
     * Creates a context for the given importer. The importer gets initialized by
     * this ctor.
     * 
     * @throws Exception If {@link Importer#init(ImporterSite, IProgressMonitor)}
     *         throws Exception.
     */
    public ImporterContext( Importer importer, Object[] contextIn, IProgressMonitor monitor )
            throws Exception {
        assert importer != null && contextIn != null;
        this.importer = importer;
        this.contextIn = stream( contextIn ).collect( toMap( o -> o.getClass(), o -> o ) );

        injectContext( importer );
        site = new ImporterSite( importer ) {
            @Override
            public ImportPrompt newPrompt( String id ) {
                assert prompts != null : "newPrompt() called before createPrompts()!";
                assert !prompts.containsKey( id );
                ImportPrompt result = new ImportPromptImpl();
                prompts.put( id, result );
                EventManager.instance().publish( new ImporterChangeEvent( importer ) );
                return result;
            }
            @Override
            public Optional<ImportPrompt> prompt( String id ) {
                return Optional.ofNullable( prompts.get( id ) );
            }
        };
        importer.init( site, monitor );
        assert importer.site() == site;
    }
    
    
//    protected abstract void createNextLevelUI( Importer importer );
//    
//    protected abstract void createResultViewer( Composite parent );

    
    
    public Importer importer() {
        return importer;
    }


    public ImporterSite site() {
        return site;
    }


    public void addContextIn( Object o ) {
        Object previous = contextIn.get( o.getClass() );
        contextIn.put( o.getClass(), o );
        EventManager.instance().syncPublish( new ContextChangeEvent( this, o, previous ) );
    }

    
    /**
     * List of {@link Importer}s that are able to handle the current context.
     * @param monitor 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public List<ImporterContext> findAvailable( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Check importers", factories.length*10 );
        
        List<ImporterContext> result = new ArrayList();
        
        for (Class<ImporterFactory> cl : factories) {
            ImporterFactory factory = cl.newInstance();
            injectContext( factory );
            SubProgressMonitor submon = new SubProgressMonitor( monitor, 10 );

            factory.createImporters( new ImporterBuilder() {
                @Override
                public void newImporter( Importer newImporter, Object... newContextIn ) throws Exception {
                    result.add( new ImporterContext( newImporter, newContextIn, submon ) );
                }
            });
            submon.done();
        }
        return result;
    }


    public List<ImportPrompt> prompts( IProgressMonitor monitor ) throws Exception {
        if (prompts == null) {
            prompts = new HashMap();
            importer.createPrompts( monitor );
        }
        return ImmutableList.copyOf( prompts.values() );
    }

    
    public void createResultViewer( Composite parent ) {
        importer.createResultViewer( parent );
    }


    public void createPromptViewer( Composite parent, ImportPrompt prompt ) {
        prompt.extendedUI.ifPresent( uibuilder -> uibuilder.createContents( prompt, parent ) );
    }


    public void execute( IProgressMonitor monitor ) throws Exception {
        importer.execute( monitor );
    } 

    
    protected void injectContext( Object obj ) {
        Class cl = obj.getClass();
        while (cl != null) {
            for (Field f : cl.getDeclaredFields()) {
                ContextIn a = f.getAnnotation( ContextIn.class );
                if (a != null) {
                    try {
                        f.setAccessible( true );
                        f.set( obj, contextIn.get( f.getType() ) );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }
            cl = cl.getSuperclass();
        }
    }
    
    
    /**
     * 
     */
    class ImportPromptImpl
            extends ImportPrompt {
    
        public ImporterContext driver() {
            return ImporterContext.this;
        }
    }
    
    
    /**
     * 
     */
    class ContextChangeEvent
            extends EventObject {
        
        public Object       newValue;

        public Object       oldValue;
        
        protected ContextChangeEvent( ImporterContext source, Object newValue, Object oldValue ) {
            super( source );
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        @Override
        public ImporterContext getSource() {
            return (ImporterContext)super.getSource();
        }
        
    }
    
}
