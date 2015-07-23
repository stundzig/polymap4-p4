/* 
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
 */
package org.polymap.p4.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.PanelIdentifier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LayersPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LayersPanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "layers" );

    
    @Override
    public boolean wantsToBeShown() {
        return parentPanel()
                .filter( parent -> parent instanceof ProjectPanel )
                .map( parent -> {
                    getSite().setTitle( "Layers" );
                    getSite().setPreferredWidth( 200 );
                    return true;
                })
                .orElse( false );
    }


    @Override
    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().margins( 10 ).create() );

        getSite().toolkit().createLabel( parent, "Layers... (" + hashCode() + ")" )
                .setLayoutData( FormDataFactory.filled().width( 200 ).clearBottom().create() );
        
        Button okBtn = getSite().toolkit().createButton( parent, "Ok", SWT.PUSH );
        okBtn.setLayoutData( FormDataFactory.filled().top( 0, 100 ).clearBottom().create() );
        okBtn.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                getContext().closePanel( getSite().getPath() );
            }
        });
    }

}
