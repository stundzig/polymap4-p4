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
package org.polymap.p4.layer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.project.ILayer;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.dashboard.DashletSite;
import org.polymap.rhei.batik.dashboard.DefaultDashlet;
import org.polymap.rhei.batik.dashboard.ISubmitableDashlet;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.DefaultFormPage;
import org.polymap.rhei.form.IFormPageSite;
import org.polymap.rhei.form.batik.BatikFormContainer;

import org.polymap.p4.PropertyAdapter;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
class BasicLayerInfoDashlet
        extends DefaultDashlet
        implements ISubmitableDashlet {

    private ILayer                  layer;
    
    private BatikFormContainer      form;

    
    public BasicLayerInfoDashlet( ILayer layer ) {
        this.layer = layer;
    }

    @Override
    public void init( DashletSite site ) {
        super.init( site );
        site.title.set( layer.label.get() );
        site.constraints.get().add( new PriorityConstraint( 100 ) );
        site.constraints.get().add( new MinWidthConstraint( 350, 1 ) );
    }
    
    @Override
    public void createContents( Composite parent ) {
        form = new BatikFormContainer( new BasicInfoForm() );
        form.createContents( parent );
    }
    
    @Override
    public boolean submit( IProgressMonitor monitor ) throws Exception {
        form.submit( monitor );
        return true;
    }


    /**
     * 
     */
    protected class BasicInfoForm
            extends DefaultFormPage
            implements IFormFieldListener {

        @Override
        public void createFormContents( IFormPageSite site ) {
            super.createFormContents( site );
            
            site.addFieldListener( this );
            
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults()
                    .spacing( 5 /*panelSite.getLayoutPreference( LAYOUT_SPACING_KEY ) / 4*/ )
                    .margins( 0 /*getSite().getLayoutPreference().getSpacing() / 2 )*/ ).create() );
            
            // label/title
            site.newFormField( new PropertyAdapter( layer.label ) )
                    .label.put( "Title" )
                    .tooltip.put( "The title of this layer." )
                    .create();
            
            // description
            site.newFormField( new PropertyAdapter( layer.description ) )
                    .label.put( "Description" )
                    .tooltip.put( "Describes the content and purpose of this layer." )
                    .field.put( new TextFormField() )
                    .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 80 ) );
            
            // keywords
            site.newFormField( new PropertyAdapter( layer.keywords ) )
                    .label.put( "Keywords" )
                    .tooltip.put( "..." )
                    .validator.put( new KeywordsValidator() )
                    .field.put( new TextFormField() )
                    .create().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 80 ) );
        }

        @Override
        public void fieldChange( FormFieldEvent ev ) {
            if (ev.getEventCode() == VALUE_CHANGE) {
                getSite().enableSubmit( form.isValid() && form.isDirty() );
                
                if (ev.getFieldName().equals( layer.label.info().getName() )) {
                    getSite().title.set( (String)ev.getNewModelValue().orElse( "???" ) );
                }
            }
        }
        
    }

}