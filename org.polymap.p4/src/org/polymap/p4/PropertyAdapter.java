/* 
 * polymap.org
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
package org.polymap.p4;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.polymap.model2.runtime.PropertyInfo;

/**
 * Adapts {@link org.polymap.model2.Property} to {@link org.opengis.feature.Property}
 * to be used by forms.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PropertyAdapter
        implements Property {

    public static PropertyDescriptor descriptorFor( org.polymap.model2.Property prop ) {
        PropertyInfo info = prop.info();
        NameImpl name = new NameImpl( info.getName() );
        AttributeType type = new AttributeTypeImpl( name, info.getType(), true, false, null, null, null );
        return new AttributeDescriptorImpl( type, name, 1, 1, false, null );
    }


    public static PropertyDescriptor descriptorFor( String _name, Class binding ) {
        NameImpl name = new NameImpl( _name );
        AttributeType type = new AttributeTypeImpl( name, binding, true, false, null, null, null );
        return new AttributeDescriptorImpl( type, name, 1, 1, false, null );
    }

    
    // instance *******************************************
    
    private org.polymap.model2.Property    delegate;
    

    public PropertyAdapter( org.polymap.model2.Property delegate ) {
        assert delegate != null;
        this.delegate = delegate;
    }

    @Override
    public Object getValue() {
        return delegate.get();
    }

    @Override
    public void setValue( Object newValue ) {
        if (newValue != null 
                && !delegate.info().getType().isAssignableFrom( newValue.getClass() )) {
            throw new ClassCastException( "Wrong value for Property of type '" + delegate.info().getType() + "': " + newValue.getClass() );
        }
        delegate.set( newValue );
    }

    @Override
    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), delegate.info().getType(), false, false, null, null, null );
    }

    @Override
    public PropertyDescriptor getDescriptor() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Name getName() {
        return new NameImpl( delegate.info().getName() );
    }

    @Override
    public boolean isNillable() {
        return delegate.info().isNullable();
    }

    @Override
    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }
    
}
