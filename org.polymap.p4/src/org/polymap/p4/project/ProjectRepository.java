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
package org.polymap.p4.project;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.runtime.session.SessionSingleton;

import org.polymap.model2.Entity;
import org.polymap.model2.query.Query;
import org.polymap.model2.runtime.ConcurrentEntityModificationException;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.ModelRuntimeException;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectRepository {

    private static Log log = LogFactory.getLog( ProjectRepository.class );

    private static EntityRepository         repo;

    static {
        try {
            File dir = new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "project" );
            dir.mkdirs();
            LuceneRecordStore store = new LuceneRecordStore( dir, false );
            repo = EntityRepository.newConfiguration()
                    .entities.set( new Class[] {ILayer.class, IMap.class} )
                    .store.set( new RecordStoreAdapter( store ) )
                    .create();
            
            checkInitRepo();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    protected static void checkInitRepo() {
        try (UnitOfWork uow = repo.newUnitOfWork()) {
            if (uow.entity( IMap.class, "root" ) == null) {
                uow.createEntity( IMap.class, "root", (IMap prototype) -> {
                    prototype.label.set( "The Map" );
                    prototype.visible.set( true );
                    return prototype;
                });
                uow.commit();
            }
        }
    }

    
    /**
     * The instance of the current {@link SessionContext}. This is the <b>read</b>
     * cache for all entities used by the UI.
     * <p/>
     * Do <b>not</b> use this for <b>modifications</b> that might be canceled or
     * otherwise may left pending changes! Create a
     * {@link UnitOfWorkWrapper#newUnitOfWork()} nested instance for that. This
     * prevents your modifications from being committed by another party are leaving
     * half-done, uncommitted changes. Commiting a nested instance commits also the
     * parent, hence making changes persistent, in one atomic action. If that fails
     * the <b>parent</b> is rolled back.
     */
    public static UnitOfWork unitOfWork() {
        return UnitOfWorkWrapper.instance( UnitOfWorkWrapper.class );
    }

    
    public static UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }
    
    
    /**
     * 
     */
    static class UnitOfWorkWrapper
            extends SessionSingleton
            implements UnitOfWork {
        
        private UnitOfWork          delegate;
        
        private UnitOfWork          parent;

        /** This is the {@link SessionSingleton} ctor. */
        public UnitOfWorkWrapper() {
            this.delegate = repo.newUnitOfWork();    
        }
        
        /** This is the ctor fpr nested instances. */
        public UnitOfWorkWrapper( UnitOfWork parent ) {
            this.delegate = parent.newUnitOfWork();
            this.parent = parent;
        }
        
        public <T extends Entity> T entityForState( Class<T> entityClass, Object state ) {
            return delegate.entityForState( entityClass, state );
        }

        public <T extends Entity> T entity( Class<T> entityClass, Object id ) {
            return delegate.entity( entityClass, id );
        }

        public <T extends Entity> T entity( T entity ) {
            return delegate.entity( entity );
        }

        public <T extends Entity> T createEntity( Class<T> entityClass, Object id, ValueInitializer<T>... initializers ) {
            return delegate.createEntity( entityClass, id, initializers );
        }

        public void removeEntity( Entity entity ) {
            delegate.removeEntity( entity );
        }

        public void prepare() throws IOException, ConcurrentEntityModificationException {
            throw new RuntimeException( "the nested UoW thing does not (yet) support prepare()." );
            //delegate.prepare();
        }

        public void commit() throws ModelRuntimeException {
            synchronized( parent) {
                try {
                    delegate.commit();
                    parent.commit();
                }
                catch (Exception e) {
                    log.info( "Commit nested ProjectRepository failed.", e );
                    parent.rollback();
                }
            }
        }

        public void rollback() throws ModelRuntimeException {
            delegate.rollback();
        }

        public void close() {
            delegate.close();
        }

        public boolean isOpen() {
            return delegate.isOpen();
        }

        public <T extends Entity> Query<T> query( Class<T> entityClass ) {
            return delegate.query( entityClass );
        }

        public UnitOfWork newUnitOfWork() {
            return new UnitOfWorkWrapper( delegate );
        }
    }
    
}
