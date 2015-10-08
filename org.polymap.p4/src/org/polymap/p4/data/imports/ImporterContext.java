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
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.polymap.core.runtime.SubMonitor;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.config.Configurable;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikPlugin;

import org.polymap.p4.data.imports.ImporterFactory.ImporterBuilder;
import org.polymap.p4.data.imports.ImporterPrompt.Severity;
import org.polymap.p4.data.imports.archive.ArchiveFileImporterFactory;

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
    private static final Class[]            factories = { ArchiveFileImporterFactory.class };
    
    private Importer                        importer;
    
    private ImporterSite                    site;
    
    private Map<Class,Object>               contextIn = new HashMap();
    
    private Map<String,ImporterPrompt>      prompts = new HashMap();
    
    private UIJob                           verifier;


    /**
     * Creates a starting context without importer and no context set.
     */
    public ImporterContext() {
    }

    
    /**
     * Creates a context for the given {@link Importer}. The importer gets initialized by
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
        
        site = new ImporterSite() {
            @Override
            public ImporterContext context() {
                return ImporterContext.this;
            }
            @Override
            public ImporterPrompt newPrompt( String id ) {
                assert prompts != null : "newPrompt() called before createPrompts()!";
                assert !prompts.containsKey( id );
                ImporterPrompt result = new ImporterPrompt() {
                    @Override
                    public ImporterContext context() {
                        return ImporterContext.this;
                    }                    
                };
                prompts.put( id, result );
                EventManager.instance().publish( new ContextChangeEvent( ImporterContext.this ) );
                return result;
            }
            @Override
            public Optional<ImporterPrompt> prompt( String id ) {
                return Optional.ofNullable( prompts.get( id ) );
            }
        };
        monitor.beginTask( "Initialize importer", 10 );
        // init
        SubMonitor.on( monitor, 1 ).complete( submon -> importer.init( site, submon ) );
        assert importer.site() == site;
        // create prompts
        SubMonitor.on( monitor, 1 ).complete( submon -> importer.createPrompts( submon ) );
        // verify
        promptChanged( null );
        //SubMonitor.on( monitor, 8 ).complete( submon -> importer.verify( submon ) );
        
        // listen to prompt changes
        EventManager.instance().subscribe( this, ev -> 
                ev instanceof ConfigChangeEvent && prompts.containsValue( ev.getSource() ) );
    }
    
    
    @EventHandler( delay=100 )
    protected void promptChanged( List<ConfigChangeEvent> evs ) {
        if (verifier != null) {
            log.info( "Cancel VERIFIER!" );
            verifier.cancel();
            verifier.getThread().interrupt();
            verifier = null;
        }
        verifier = new UIJob( "Progress import" ) {
            @Override
            protected void runWithException( IProgressMonitor monitor ) throws Exception {
                importer.verify( monitor );
            }
        };
        verifier.addJobChangeListener( new JobChangeAdapter() {
            private UIJob myVerifier = verifier;
            @Override
            public void done( IJobChangeEvent ev ) {
                if (verifier == myVerifier) {
                    verifier = null;
                }
            }
        });
        verifier.scheduleWithUIUpdate();
    }
    
    
    public Importer importer() {
        return importer;
    }


    public ImporterSite site() {
        return site;
    }


    public void addContextIn( Object o ) {
        contextIn.put( o.getClass(), o );
        EventManager.instance().syncPublish( new ContextChangeEvent( this ) );
    }

    
    /**
     * List of {@link Importer}s that are able to handle the current context.
     * 
     * @param monitor 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public List<ImporterContext> findNext( IProgressMonitor monitor ) throws Exception {
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


    /**
     * Get or initially create prompts.
     *
     * @param monitor
     * @throws Exception
     */
    public List<ImporterPrompt> prompts( IProgressMonitor monitor ) throws Exception {
//        if (prompts == null) {
//            prompts = new HashMap();
//            importer.createPrompts( monitor );
//        }
        return ImmutableList.copyOf( prompts.values() );
    }

    
    protected Severity maxFailingPromptSeverity() {
        return prompts == null 
                ? Severity.INFO
                : prompts.values().stream()
                        .filter( prompt -> !prompt.ok.get() )
                        .map( prompt -> prompt.severity.get() )
                        .max( Comparator.comparing( s -> s.ordinal() ) )
                        .orElse( Severity.INFO );
    }
    
    
    public void createResultViewer( Composite parent ) {
        JobChangeAdapter task = new JobChangeAdapter() {
            @Override
            public void done( IJobChangeEvent ev ) {
                if (ev == null || ev.getResult().isOK()) {
                    UIThreadExecutor.asyncFast( () -> {
                        UIUtils.disposeChildren( parent );
                        importer.createResultViewer( parent );
                        parent.layout( true );
                    });
                }
            }
        };
        if (verifier != null) {
            Label msg = new Label( parent, SWT.NONE );
            msg.setText( "Crunching data..." );
            msg.setImage( BatikPlugin.images().image( "resources/icons/loading24.gif" ) );
            verifier.addJobChangeListenerWithContext( task );
        }
        else {
            task.done( null );
        }
    }


    public void createPromptViewer( Composite parent, ImporterPrompt prompt ) {
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
     * Fired when {@link ImporterContext#addContextIn(Object)} is called after a file
     * was uploaded or anything else has been added to the context. Also fired when a
     * prompt has been added by calling {@link ImporterSite#newPrompt(String)} .
     */
    class ContextChangeEvent
            extends EventObject {
        
        protected ContextChangeEvent( ImporterContext source ) {
            super( source );
        }

        @Override
        public ImporterContext getSource() {
            return (ImporterContext)super.getSource();
        }
        
    }
    
}
