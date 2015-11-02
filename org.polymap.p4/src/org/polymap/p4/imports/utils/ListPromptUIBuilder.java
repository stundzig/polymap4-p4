/*
 * polymap.org Copyright (C) 2015 individual contributors as indicated by the
 * 
 * @authors tag. All rights reserved.
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
package org.polymap.p4.imports.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 *
 */
public abstract class ListPromptUIBuilder
        implements PromptUIBuilder {

    @Override
    public void createContents( ImporterPrompt prompt, Composite parent ) {
        parent.setLayout( new GridLayout( 1, false ) );
        Composite filterComp = new Composite( parent, SWT.NULL );
        filterComp.setLayout( new GridLayout( 2, false ) );
        Label label = new Label( filterComp, SWT.NONE );
        label.setText( "Filter:" );
        Text filterText = new Text( filterComp, SWT.BORDER );
        filterText.setLayoutData( createHorizontalFill() );
        filterComp.setLayoutData( createHorizontalFill() );

        org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List( parent, SWT.V_SCROLL );
        list.setItems( getListItems() );
        list.setSelection( new String[] { getInitiallySelectedItem() } );
        list.setLayoutData( createHorizontalFillWithHeightHint( 200 ) );
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


    private GridData createHorizontalFill() {
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = SWT.FILL;
        return gridData;
    }


    private GridData createHorizontalFillWithHeightHint( int heightHint ) {
        GridData gridData = createHorizontalFill();
        gridData.heightHint = 200;
        return gridData;
    }


    protected abstract String[] getListItems();


    protected abstract String getInitiallySelectedItem();


    protected abstract void handleSelection( String selectedItem );


    protected List<String> filterSelectable( String text ) {
        return Arrays.asList( getListItems() ).stream().filter( item -> {
            if (text.startsWith( "*" )) {
                return item != null && item.contains( text.substring( 1 ) );
            }
                else {
                    return item != null && item.startsWith( text );
                }
            } ).collect( Collectors.toList() );
    }
}
