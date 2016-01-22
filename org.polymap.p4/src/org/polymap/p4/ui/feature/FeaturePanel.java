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

import static org.polymap.core.runtime.UIThreadExecutor.async;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.polymap.core.operation.DefaultOperation;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.SelectionListenerAdapter;
import org.polymap.core.ui.StatusDispatcher;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.toolkit.Snackbar.Appearance;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.ui.P4Panel;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FeaturePanel
        extends P4Panel 
        implements IFormFieldListener {

    private static Log log = LogFactory.getLog( FeaturePanel.class );

    public static final PanelIdentifier ID = PanelIdentifier.parse( "feature" );
    
    /** */
    @Scope( P4Plugin.Scope )
    protected Context<Feature>          feature;

    private Button                      fab;

    private BatikFormContainer          form;

    private boolean                     previouslyValid = true;
    
    
    @Override
    public void createContents( Composite parent ) {
        form = new BatikFormContainer( new StandardFeatureForm() );
        form.createContents( parent );
        
        form.addFieldListener( this );
        
        fab = tk().createFab();
        fab.setToolTipText( "Save changes" );
        fab.setVisible( false );
        fab.addSelectionListener( new SelectionListenerAdapter( ev -> submit() ) );
    }

    
    @Override
    public void fieldChange( FormFieldEvent ev ) {
        if (ev.hasEventCode( VALUE_CHANGE )) {
            boolean isDirty = form.isDirty();
            boolean isValid = form.isValid();
            
            fab.setVisible( isDirty  );
            fab.setEnabled( isDirty && isValid );
            
            if (previouslyValid && !isValid) {
                tk().createSnackbar( Appearance.FadeIn, "There are invalid settings" );
            }
            if (!previouslyValid && isValid) {
                tk().createSnackbar( Appearance.FadeIn, "Settings are ok" );
            }
            previouslyValid = isValid;
        }
    }

    
    protected void submit() {
        try {
            // XXX doing this inside operation cause "Invalid thread access"
            form.submit( null );
        }
        catch (Exception e) {
            StatusDispatcher.handleError( "Unable to submit form.", e );
        }
        
        SubmitOperation op = new SubmitOperation();
        OperationSupport.instance().execute2( op, false, false, ev -> {
            async( () -> {
                tk().createSnackbar( Appearance.FadeIn, ev.getResult().isOK()
                        ? "Saved" : "Unable to save: " + ev.getResult().getMessage() );
            });
        });
    }

    
    /**
     * 
     */
    public class SubmitOperation
            extends DefaultOperation {

        public SubmitOperation() {
            super( "Submit" );
        }

        @Override
        protected IStatus doExecute( IProgressMonitor monitor, IAdaptable info ) throws Exception {
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
    
    /**
     * 
     */
    class StandardFeatureForm
            extends DefaultFormPage {

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            site.getPageBody().setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).spacing( 3 ).create() );
            
            for (Property prop : FeaturePanel.this.feature.get().getProperties()) {
                if (Geometry.class.isAssignableFrom( prop.getType().getBinding() )) {
                    // skip Geometry
                }
                else {
                    site.newFormField( prop ).create();
                }
            }
        }
    }
    
}
