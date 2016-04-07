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
package org.polymap.p4;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * Only necessary to export to export the product. If started from Inside IDE then
 * this is not called at all.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class P4Application
        implements IApplication {

    private static Log log = LogFactory.getLog( P4Application.class );


    @Override
    public Object start( IApplicationContext context ) throws Exception {
        log.info( "start()" );
        while (true) {
            Thread.sleep( 1000 );
        }
//        int returnCode = EXIT_OK;
//        try {
//            returnCode = PlatformUI.createAndRunWorkbench( display, workbenchAdvisor );
//        }
//        catch (Throwable e) {
//            log.warn( "Error:" + e.getLocalizedMessage(), e );
//        }
//        finally {
//            context.applicationRunning();
//            display.dispose();
//        }
//        if (returnCode == PlatformUI.RETURN_RESTART) {
//            return EXIT_RESTART;
//        }
//        return EXIT_OK;
    }


    @Override
    public void stop() {
        log.info( "stop()" );
    }
    
}
