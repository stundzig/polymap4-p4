/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
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
package org.polymap.p4.layer;

import java.util.EventObject;

import org.opengis.filter.Filter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeatureSelectionEvent
        extends EventObject {

    private Filter              newSelection;
    
    private Filter              oldSelection;
    
    
    public FeatureSelectionEvent( FeatureSelection source, Filter newSelection, Filter oldSelection ) {
        super( source );
        this.newSelection = newSelection;
        this.oldSelection = oldSelection;
    }

    @Override
    public FeatureSelection getSource() {
        return (FeatureSelection)super.getSource();
    }

    public Filter getNewSelection() {
        return newSelection;
    }
    
    public Filter getOldSelection() {
        return oldSelection;
    }
    
}
