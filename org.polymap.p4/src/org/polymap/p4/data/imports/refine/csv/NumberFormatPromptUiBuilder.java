package org.polymap.p4.data.imports.refine.csv;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.Polymap;
import org.polymap.p4.data.imports.ImporterPrompt;
import org.polymap.p4.data.imports.ImporterPrompt.PromptUIBuilder;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;

public class NumberFormatPromptUiBuilder
        implements PromptUIBuilder {

    public class LocalizedFormat
    /* implements Comparable<LocalizedFormat> */ {

        private String             key;

        private Collection<Locale> values;


        public LocalizedFormat( String key, Collection<Locale> values ) {
            this.key = key;
            this.values = values;
        }


        //
        // @Override
        // public int compareTo( LocalizedFormat o ) {
        // return values.iterator().next().compareTo( o.values.iterator().next() );
        // }
        @Override
        public String toString() {
            StringBuffer ret = new StringBuffer( "<strong>").append( key ).append( "</strong>: " );
            List<String> names = Lists.newArrayList();
            values.forEach( locale -> names.add( locale.getDisplayLanguage( sessionLocale ) ) );
            Collections.sort( names );
            names.forEach( name -> ret.append( name ).append( ", " ) );
            return ret.toString().substring( 0, ret.toString().length() - 2 );
        }

    }

    private LocalizedFormat value;

    private CSVFileImporter importer;

    private Locale          sessionLocale;


    public NumberFormatPromptUiBuilder( CSVFileImporter importer, Locale sessionLocale ) {
        this.importer = importer;
        this.sessionLocale = sessionLocale;
    }


    @Override
    public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
        System.out.println( "create contents" );
        org.eclipse.swt.widgets.List list = tk.createList( parent, SWT.SINGLE );
        List<LocalizedFormat> allValues = allValues();
        list.setItems( allValues.stream().map( lf -> lf.toString() ).toArray( String[]::new ) );
        list.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                value = allValues.get( list.getSelectionIndex() );
            }
        } );
        value = initialValue();
        int index = allValues.indexOf( value );
        if (index != -1) {
            list.select( index );
        }
        prompt.value.set( value.key );
    }


    @Override
    public void submit( ImporterPrompt prompt ) {
//        importer.formatAndOptions().setNumberLocale( value.values.iterator().next() );
//        importer.formatAndOptions().setNumberFormat( value.key );
        importer.updateOptions();
        prompt.ok.set( true );
        prompt.value.set( value.key );
    }


    private LocalizedFormat initialValue() {
        Locale initialLocale = Polymap.getSessionLocale();
        for (LocalizedFormat format : allValues()) {
            if (format.values.contains( initialLocale )) {
                return format;
            }
        }
        // Locale not found, use Locale.EN as default
        for (LocalizedFormat format : allValues()) {
            if (format.values.contains( Locale.ENGLISH )) {
                return format;
            }
        }
        throw new IllegalStateException(initialLocale + " and " + Locale.ENGLISH + " are not supported");
    }

    private List<LocalizedFormat> allValues;


    private List<LocalizedFormat> allValues() {
        if (allValues == null) {
            ListMultimap<String,Locale> formatsMap = ArrayListMultimap.create();
            for (Locale locale : DecimalFormat.getAvailableLocales()) {
                if (locale.getCountry().length() != 0) {
                    continue; // Skip sub-language locales
                }
                if (!StringUtils.isBlank( locale.getDisplayName( sessionLocale ) )) {
                    formatsMap.put( ((DecimalFormat)NumberFormat.getInstance( locale )).toLocalizedPattern(),
                            locale );
                }
            }
            allValues = Lists.newArrayList();
            formatsMap.asMap().forEach( ( key, values ) -> allValues.add( new LocalizedFormat( key, values ) ) );
        }
        return allValues;
    }
}
