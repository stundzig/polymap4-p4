/*
 * polymap.org 
 * Copyright (C) 2015 individual contributors as indicated by the @authors tag.
 * All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.data.imports.utils;

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FilteredListPromptUIBuilder
        implements PromptUIBuilder {

    protected abstract String[] listItems();

    protected abstract String initiallySelectedItem();

    protected abstract void handleSelection( String selectedItem );

    
    @Override
    public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 0 ).create() );
        
        Label label = on( new Label( parent, SWT.NONE ) )
                .fill().noBottom().control();
        label.setText( "Filter:" );
        
        Text filterText = on( new Text( parent, SWT.BORDER ) )
                .left( 0 ).top( label ).right( 100 ).control();
        filterText.setToolTipText( "Name of the charset or part thereof" );
        filterText.forceFocus();

        org.eclipse.swt.widgets.List list = on( new org.eclipse.swt.widgets.List( parent, SWT.V_SCROLL ) )
                .fill().top( filterText, 10 ).width( 250 ).height( 250 ).control();
        
        list.setItems( listItems() );
        list.setSelection( new String[] { initiallySelectedItem() } );
        list.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                String item = list.getItem( list.getSelectionIndex() );
                handleSelection( item );
            }
        } );
        filterText.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent event ) {
                List<String> filtered = filterSelectable( filterText.getText() );
                list.setItems( filtered.toArray( new String[filtered.size()] ) );
                if (list.getItems().length > 0) {
                    list.select( 0 );
                    handleSelection( list.getItem( list.getSelectionIndex() ) );
                }
            }
        } );
        parent.pack();
    }


    protected List<String> filterSelectable( String text ) {
        return Arrays.stream( listItems() )
                .filter( item -> item.toLowerCase().contains( text.toLowerCase() ) )
                .collect( Collectors.toList() );
    }
}
