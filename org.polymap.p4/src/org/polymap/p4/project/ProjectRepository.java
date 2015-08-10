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

import java.util.function.Supplier;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.CorePlugin;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

import org.polymap.model2.Composite;
import org.polymap.model2.runtime.CompositeInfo;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.p4.P4Plugin;
import org.polymap.recordstore.lucene.LuceneRecordStore;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ProjectRepository
        implements Supplier<EntityRepository>, AutoCloseable {

    private static Log log = LogFactory.getLog( ProjectRepository.class );

    /** The global instance. */
    public static Lazy<ProjectRepository> instance = new LockedLazyInit( () -> new ProjectRepository() );

    
    // instance *******************************************
    
    private EntityRepository        repo;
    
    
    private ProjectRepository() {
        try {
            File dir = new File( CorePlugin.getDataLocation( P4Plugin.instance() ), "project" );
            dir.mkdirs();
            LuceneRecordStore store = new LuceneRecordStore( dir, false );
            repo = EntityRepository.newConfiguration()
                    .entities.set( new Class[] {ILayer.class, IMap.class} )
                    .store.set( new RecordStoreAdapter( store ) )
                    .create();
            
            initRepo();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    protected void initRepo() {
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


    @Override
    public EntityRepository get() {
        return repo;
    }

    @Override
    public void close() {
        repo.close();
    }

    public <T extends Composite> CompositeInfo<T> infoOf( Class<T> compositeClass ) {
        return repo.infoOf( compositeClass );
    }

    public UnitOfWork newUnitOfWork() {
        return repo.newUnitOfWork();
    }
    
}
