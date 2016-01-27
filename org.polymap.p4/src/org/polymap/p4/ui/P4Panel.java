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
package org.polymap.p4.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.ui.feature.FeaturePanel;
import org.polymap.p4.ui.feature.FeatureSelection;
import org.polymap.p4.ui.feature.LayersFeatureTableContribution;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class P4Panel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( FeaturePanel.class );
    
    /**
     * The <b>active</b> layer and the <b>selected</b> features from this layer.
     * There is just one active layer at a given time. This layer is selected by
     * choosing the feature table to open ({@link LayersFeatureTableContribution}).
     */
    @Mandatory
    @Scope( P4Plugin.Scope )
    protected Context<FeatureSelection>     featureSelection;
    
    
    protected MdToolkit tk() {
        return (MdToolkit)site().toolkit();
    }
    
    
    protected void createErrorContents( Composite parent, String msg, Throwable cause ) {
        log.warn( msg, cause );
        tk().createFlowText( parent, msg );
    }
    
    
//    protected void openErrorDialog( String msg, Throwable cause ) {
//        tk().createSimpleDialog( "" )
//    }
    
}
