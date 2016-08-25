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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.UIThreadExecutor;
import org.polymap.core.runtime.config.Config2;
import org.polymap.core.runtime.config.Immutable;
import org.polymap.core.style.model.FeatureStyle;

import org.polymap.rhei.batik.toolkit.SimpleDialog;
import org.polymap.p4.P4Plugin;

/**
 * Extends the basic {@link org.polymap.core.project.ops.NewLayerOperation
 * NewLayerOperation} by
 * <ul>
 * <li>default {@link FeatureStyle} building</li>
 * <li>WMS resource/layer check</li>
 * <li>resource id creation</li>
 * </ul>
 * <p/>
 * Do not set {@link org.polymap.core.project.ops.NewLayerOperation#initializer
 * #initializer} directly!
 * <p/>
 * Impl. note: This is not an operation concern because we need the additional
 * {@link IResourceInfo}.
 *
 * @author Falko Bräutigam
 */
public class NewLayerOperation
        extends org.polymap.core.project.ops.NewLayerOperation {

    private static final Log log = LogFactory.getLog( NewLayerOperation.class );
    
    /**
     * Inbound: (optional) Label of the newly created layer.
     */
    @Immutable
    public Config2<NewLayerOperation,String>        label;

    /**
     * Inbound: (optional) Linked to {@link ILayer#styleIdentifier} of the newly
     * created layer. Stored {@link #onSuccess()}.
     */
    @Immutable
    public Config2<NewLayerOperation,FeatureStyle>  featureStyle;

    /** Inbound: */
    @Immutable
    public Config2<NewLayerOperation,IResourceInfo> res;

    /** Internal: allow concerns to build identifier for the given {@link #res}. */ 
    @Immutable
    public Config2<NewLayerOperation,String>        resId;


    @Override
    public IStatus doWithCommit( IProgressMonitor monitor, IAdaptable info ) throws Exception {
        // check WMS resource addable
        if (res.isPresent() && res.get().getName() == null) {
            UIThreadExecutor.sync( () -> {
                new SimpleDialog()
                        .title.put( "WMS resource" )
                        .addOkAction( () -> true )
                        .setContents( parent -> {
                            Label msg = new Label( parent, SWT.NONE );
                            // XXX add markup enabled
                            msg.setText( "This WMS resource has no name.<br/> It does not seem to be a layer." );
                        })
                        .open();
                return null;
            });
            return Status.OK_STATUS;
        }

        // build resource identifier
        if (!resId.isPresent()) {
            resId.set( P4Plugin.allResolver().resourceIdentifier( res.get() ) );
        }
        initializer.set( (ILayer proto) -> {
            proto.label.set( label
                    .orElse( () -> res.get().getName() ) );
            proto.description.set( res
                    .map( r -> r.getDescription().orElse( "" ) ).orElse( "" ) );
            proto.resourceIdentifier.set( resId
                    .orElse( () -> P4Plugin.allResolver().resourceIdentifier( res.get() ) ) );
            proto.styleIdentifier.set( featureStyle
                    .map( fs -> fs.id() )
                    .orElse( null ) );
            return proto;
        });

        // call super
        return super.doWithCommit( monitor, info );
    }


    @Override
    protected void onSuccess() {
        super.onSuccess();
        featureStyle.ifPresent( fs -> fs.store() );
    }
    
}
