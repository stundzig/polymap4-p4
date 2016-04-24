/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
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
package org.polymap.p4.style;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.StyleGroup;

import org.polymap.model2.Composite;

/**
 * 
 * <p/>
 * This is also a cache for already returned Style {@link Composite} instances. As the cache behaviour of Model2
 * Composite properties is not clearly defined it is vary important to cachereturn <b>just one</b> Composite instance 
 *
 * @author Falko Bräutigam
 */
public class FeatureStyleContentProvider
        implements ITreeContentProvider {

    private static Log log = LogFactory.getLog( FeatureStyleContentProvider.class );

    private FeatureStyle        input;
    
    private Map                 parents = new HashMap();
    

    @Override
    public Object[] getElements( Object _input ) {
        return getChildren( input );
        //return new Object[] {input};
    }


    @Override
    public Object[] getChildren( Object elm ) {
        Object[] results = new Object[] {};
        if (elm instanceof FeatureStyle) {
            results = ((FeatureStyle)elm).members().toArray();
        }
        else if (elm instanceof StyleGroup) {
            results = ((StyleGroup)elm).members.toArray();
        }
        return register( elm, results );
    }

    
    /**
     * 
     */
    protected Object[] register( Object _parent, Object[] _children ) {
        Arrays.stream( _children ).forEach( child -> parents.put( child, _parent ) );
        return _children;
    }

    
    @Override
    public boolean hasChildren( Object elm ) {
        return getChildren( elm ).length > 0;
    }


    @Override
    public Object getParent( Object child ) {
        return parents.get( child );
    }


    @Override
    public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
        this.input = (FeatureStyle)newInput;
    }


    @Override
    public void dispose() {
    }
    
}
