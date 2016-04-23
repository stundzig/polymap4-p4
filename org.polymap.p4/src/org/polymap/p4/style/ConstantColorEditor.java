/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.polymap.core.style.model.ConstantColor;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Scope;

import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.style.color.ColorPanel;
import org.polymap.p4.style.color.ColorPanelInfo;

/**
 * Editor that creates one {@link ConstantColor}.
 *
 * @author Steffen Stundzig
 */
class ConstantColorEditor
        extends StylePropertyEditor<ConstantColor> {

    private static Log log = LogFactory.getLog( ConstantColorEditor.class );


    @Override
    public String label() {
        return "Constant color";
    }


    @Override
    public boolean init( Property<ConstantColor> _prop ) {
        return Color.class.isAssignableFrom( targetType( _prop ) ) ? super.init( _prop ) : false;
    }

    @Scope(P4Plugin.Scope)
    private Context<ColorPanelInfo> colorInfo;


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantColor>() {

            @Override
            public ConstantColor initialize( ConstantColor proto ) throws Exception {
                // TODO default value here
                proto.r.set( 255 );
                proto.g.set( 0 );
                proto.b.set( 0 );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        final Button button = new Button( parent, SWT.PUSH );
        button.setText( "Configure..." );
        button.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                colorInfo.set(
                        new ColorPanelInfo() {

                            @Override
                            public RGB getColor() {
                                return new RGB( prop.get().r.get(), prop.get().g.get(), prop.get().b.get() );
                            }


                            @Override
                            public void updateColor( RGB rgb ) {
                                System.out.println( "new color " + rgb );
                                prop.get().r.set( rgb.red );
                                prop.get().b.set( rgb.blue );
                                prop.get().g.set( rgb.green );
                                button.setBackground( new org.eclipse.swt.graphics.Color( Display.getDefault(), rgb ) );
                            }
                        } );
                // open the panel
                context.openPanel( site.getPath(), ColorPanel.ID );
            }

        } );
        button.setBackground( new org.eclipse.swt.graphics.Color( Display.getCurrent(),
                new RGB( prop.get().r.get(), prop.get().g.get(), prop.get().b.get() ) ) );
        return contents;
    }
}
