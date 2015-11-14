package org.polymap.p4.imports.refine.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.polymap.p4.data.imports.refine.csv.Type;
import org.polymap.p4.data.imports.refine.csv.TypeGuesser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

public class TypeGuesserTest {

    @Test
    public void testText() {
        assertNull( TypeGuesser.guess( null ) );
        assertNull( TypeGuesser.guess( "" ) );
        assertNull( TypeGuesser.guess( "  " ) );
        assertEquals( Type.Text, TypeGuesser.guess( "04683" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "46ab83" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "abe 83" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "e 83" ).type() );
        assertEquals( Type.Decimal, TypeGuesser.guess( "83e34" ).type() );
        assertEquals( Type.Decimal, TypeGuesser.guess( "83" ).type() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83,56" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83,56" ).locale().getLanguage()  );

        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456" ).type() );
        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456.456" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83.456.456" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83 456 456" ).type() );
        assertEquals( "ru", TypeGuesser.guess( "83 456 456" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456,45" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83.456,45" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456,45e12" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83.456,45e12" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456,45e-12" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83.456,45e-12" ).locale().getLanguage() );

        assertEquals( Type.Decimal, TypeGuesser.guess( "83.456.456,45" ).type() );
        assertEquals( "de", TypeGuesser.guess( "83.456.456,45" ).locale().getLanguage() );

        assertEquals( Type.Decimal, TypeGuesser.guess( "83 456 456,45" ).type() );
        assertEquals( "ru", TypeGuesser.guess( "83 456 456,45" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "83,456,456.45" ).type() );
        assertEquals( "en", TypeGuesser.guess( "83,456,456.45" ).locale().getLanguage() );
        
        assertEquals( Type.Decimal, TypeGuesser.guess( "-83.456.456,45" ).type() );
        assertEquals( Type.Decimal, TypeGuesser.guess( "83,456,456.45-" ).type() );
        assertEquals( "ar", TypeGuesser.guess( "83,456,456.45-" ).locale().getLanguage() );
        
        assertEquals( Type.Text, TypeGuesser.guess( "83.456,45e-1-2" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "83,456,45" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "-83.456.46,45" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "83,456,46.45-" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "16.5.1975" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "20:40" ).type() );
        assertEquals( Type.Text, TypeGuesser.guess( "20:40:455" ).type() );
    }


    public static void main( String[] args ) throws ParseException {
        NumberFormat nf = NumberFormat.getInstance();
        // nf.setMaximumIntegerDigits( 3 );
        System.out.println( nf.parse( "123E3" ) );

        ListMultimap<String, Locale> formats = ArrayListMultimap.create();
        for (Locale locale : DecimalFormat.getAvailableLocales()) {
            if (locale.getCountry().length() != 0) {
                continue; // Skip sub-language locales
            }
            formats.put( ((DecimalFormat)NumberFormat.getInstance( locale )).toLocalizedPattern(), locale );
        }
        formats.asMap().forEach( (key, value) -> System.out.println(key + ": " + value) );
    }
}
