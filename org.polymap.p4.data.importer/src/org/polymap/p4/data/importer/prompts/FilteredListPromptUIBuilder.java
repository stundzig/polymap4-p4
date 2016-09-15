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
package org.polymap.p4.data.importer.prompts;

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.Messages;

/**
 * 
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FilteredListPromptUIBuilder
        implements PromptUIBuilder {

    protected static final IMessages        i18n          = Messages.forPrefix( "ImporterPrompt" );

    protected Set<String>                   items;
    
    private org.eclipse.swt.widgets.List    list;
    
    
    protected abstract Set<String> listItems();

    protected abstract String initiallySelectedItem();

    protected abstract void handleSelection( String selectedItem );

    
    @Override
    public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 0 ).create() );
        
        items = listItems();
        String initiallySelected = initiallySelectedItem();
        if (StringUtils.isBlank( initiallySelected )) {
            initiallySelected = i18n.get("filterText");
        }
        
        Label label = on( new Label( parent, SWT.NONE ) ).fill().noBottom().control();
        label.setText( i18n.get("filterSummary") );
        
        Text filterText = on( new Text( parent, SWT.BORDER ) ).left( 0 ).top( label ).right( 100 ).control();
        filterText.setToolTipText( i18n.get("filterDescription") );
        filterText.forceFocus();
        filterText.setText( initiallySelected );
        filterText.selectAll();

        filterText.addModifyListener( new ModifyListener() {
            @Override
            public void modifyText( ModifyEvent ev ) {
                setListItems( filterSelectable( filterText.getText() ) );
                if (list.getItems().length > 0) {
                    list.select( 0 );
                    handleSelection( list.getItem( list.getSelectionIndex() ) );
                }
            }
        } );

        list = on( new org.eclipse.swt.widgets.List( parent, SWT.V_SCROLL ) )
                .fill().top( filterText, 10 ).width( 350 ).height( 250 ).control();
                
        setListItems( filterSelectable( initiallySelected ) );
        list.setSelection( new String[] { initiallySelected } );
        list.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent ev ) {
                String item = list.getItem( list.getSelectionIndex() );
                handleSelection( item );
            }
        } );
        parent.pack();
    }

    
    protected void setListItems( List<String> filtered ) {
        String[] array = filtered.toArray( new String[ filtered.size() ] );
        list.setItems( array );
    }

    
    protected List<String> filterSelectable( String text ) {
        List<String> tags = Splitter.on( " " ).omitEmptyStrings().trimResults().splitToList( text.toLowerCase() );
        List<String> result = new ArrayList( 128 );
        outer: for (String item : items) {
            for (String tag : tags) {
                if (!item.toLowerCase().contains( tag )) {
                    continue outer;
                }
            }
            result.add( item );
        }
        return result;
    }
    
}
