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

import org.polymap.core.runtime.event.EventManager;

import org.polymap.p4.data.imports.archive.ArchiveFileImporter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class ImportDriver {

    private static Log log = LogFactory.getLog( ImportDriver.class );
    
    // XXX make this an extension point
    private static final Class[]            importers = { ArchiveFileImporter.class };
    
    private Map<Class,Object>               context = new HashMap();    
    
    
    protected abstract void createNextLevelUI( Importer importer );
    
    protected abstract void createResultViewer( Composite parent );

    
    public void addContext( Object o ) {
        Object previous = context.get( o.getClass() );
        context.put( o.getClass(), o );
        EventManager.instance().syncPublish( new ContextChangeEvent( this, o, previous ) );
    }

    
    /**
     * List of {@link Importer}s that are able to handle the current context.
     * @param monitor 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public List<Importer> findAvailableImporters( IProgressMonitor monitor ) throws Exception {
        monitor.beginTask( "Check importers", importers.length*10 );
        List<Importer> result = new ArrayList();
        for (Class<Importer> cl : importers) {
            Importer importer = cl.newInstance();
            injectContext( importer );
            SubProgressMonitor submon = new SubProgressMonitor( monitor, 10 );
            if (importer.init( new ImporterSiteImpl( importer ), submon ) ) {
                assert importer.site() != null;
                result.add( importer );
            }
            submon.done();
        }
        return result;
    }


    public List<ImportPrompt> prompts( Importer importer, IProgressMonitor monitor  ) throws Exception {
        ImporterSiteImpl importerSite = ((ImporterSiteImpl)importer.site());
        if (importerSite.prompts.isEmpty()) {
            importer.createPrompts( monitor );
        }
        return ImmutableList.copyOf( importerSite.prompts.values() );
    }

    
    public void executeImporter( Importer importer, IProgressMonitor monitor ) throws Exception {
        importer.execute( monitor );
    } 

    
    protected void injectContext( Importer importer ) {
        Class cl = importer.getClass();
        while (cl != null) {
            for (Field f : cl.getDeclaredFields()) {
                ContextIn a = f.getAnnotation( ContextIn.class );
                if (a != null) {
                    try {
                        f.setAccessible( true );
                        f.set( importer, context.get( f.getType() ) );
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
    class ImporterSiteImpl
            extends ImporterSite {
        
        protected Importer                  importer;
        
        protected Map<String,ImportPrompt>  prompts = new HashMap();
        
        

        public ImporterSiteImpl( Importer importer ) {
            this.importer = importer;
        }

        @Override
        public ImportPrompt newPrompt( String id ) {
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
    }

    
    /**
     * 
     */
    class ImportPromptImpl
            extends ImportPrompt {
    
        public ImportDriver driver() {
            return ImportDriver.this;
        }
    }
    
    
    /**
     * 
     */
    class ContextChangeEvent
            extends EventObject {
        
        public Object       newValue;
        public Object       oldValue;
        
        protected ContextChangeEvent( ImportDriver source, Object newValue, Object oldValue ) {
            super( source );
            this.newValue = newValue;
            this.oldValue = oldValue;
        }

        @Override
        public ImportDriver getSource() {
            return (ImportDriver)super.getSource();
        }
        
    }
    
}
