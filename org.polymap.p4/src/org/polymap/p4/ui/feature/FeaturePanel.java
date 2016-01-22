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
package org.polymap.p4.ui.feature;

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.form.DefaultFormPage;

import org.polymap.p4.P4Plugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeaturePanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( FeaturePanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "feature" );
    
    /** */
    @Scope( P4Plugin.Scope )
    protected Context<Feature>              feature;
    
    
    @Override
    public void createContents( Composite panelBody ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    /**
     * 
     */
    class StandardFeatureForm
            extends DefaultFormPage {
        
    }
    
}
