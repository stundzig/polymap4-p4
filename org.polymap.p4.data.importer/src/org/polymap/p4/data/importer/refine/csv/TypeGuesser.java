package org.polymap.p4.data.importer.refine.csv;

import java.util.regex.Pattern;

public class TypeGuesser {

    private static Pattern allowedChars               = Pattern.compile( "[\\-eE \\,\\.\\d]+" );

    private static Pattern trailingZero               = Pattern.compile( "0\\d+" );

    // private static Pattern integer = Pattern.compile( "\\d+" );
    private static Pattern integerWithE               = Pattern.compile( "\\-?\\d+([eE]\\-?\\d+)?" );

    private static Pattern negativeArabicIntegerWithE = Pattern.compile( "\\d+([eE]\\-?\\d+)?\\-?" );

    private static Pattern groupingWithComma          = Pattern.compile( "(\\d?\\d?\\d\\,)+\\d\\d\\d([eE]\\-?\\d+)?" );

    private static Pattern groupingWithPoint          = Pattern.compile( "(\\d?\\d?\\d\\.)+\\d\\d\\d([eE]\\-?\\d+)?" );

    private static Pattern groupingWithSpace          = Pattern.compile( "(\\d?\\d?\\d\\ )+\\d\\d\\d([eE]\\-?\\d+)?" );

    private static Pattern decimalWithComma           = Pattern.compile( "\\-?\\d+\\,\\d+([eE]\\-?\\d+)?" );

    private static Pattern decimalWithPoint           = Pattern.compile( "\\-?\\d+\\.\\d+([eE]\\-?\\d+)?" );

    private static Pattern arabicDecimalWithPoint     = Pattern.compile( "\\d+\\.\\d+\\-([eE]\\-?\\d+)?" );

    private static Pattern germanDecimalWithGrouping  = Pattern
                                                              .compile(
                                                                      "\\-?(\\d?\\d?\\d\\.\\d\\d)+\\d\\,\\d+([eE]\\-?\\d+)?" );

    private static Pattern englishDecimalWithGrouping = Pattern
                                                              .compile(
                                                                      "\\-?(\\d?\\d?\\d\\,\\d\\d)+\\d\\.\\d+([eE]\\-?\\d+)?" );

    private static Pattern arabicDecimalWithGrouping  = Pattern
                                                              .compile(
                                                                      "(\\d?\\d?\\d\\,\\d\\d)+\\d\\.\\d+\\-([eE]\\-?\\d+)?" );

    private static Pattern russianDecimalWithGrouping = Pattern
                                                              .compile(
                                                                      "\\-?(\\d?\\d?\\d\\ \\d\\d)+\\d\\,\\d+([eE]\\-?\\d+)?" );


    public static GuessedType guess( String value ) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        value = value.trim();
        // does it contain only allowed number characters?
        if (allowedChars.matcher( value ).matches()) {
            // postcode 03453 should be text
            if (trailingZero.matcher( value ).matches()) {
                return GuessedType.TEXT;
            }
            // // simple long, integer, Locale unknown
            // if (integer.matcher( value ).matches()) {
            // return GuessedType.DECIMAL;
            // }
            // integer with exponents
            if (integerWithE.matcher( value ).matches()) {
                return GuessedType.DECIMAL;
            }
            if (negativeArabicIntegerWithE.matcher( value ).matches()) {
                return GuessedType.DECIMAL_AR;
            }
            // grouping with comma
            if (groupingWithComma.matcher( value ).matches()) {
                return GuessedType.DECIMAL_EN;
            }
            // grouping with point
            if (groupingWithPoint.matcher( value ).matches()) {
                return GuessedType.DECIMAL_DE;
            }
            // grouping with space
            if (groupingWithSpace.matcher( value ).matches()) {
                return GuessedType.DECIMAL_RU;
            }
            // decimal or grouping
            if (decimalWithComma.matcher( value ).matches()) {
                return GuessedType.DECIMAL_DE;
            }
            // decimal or grouping
            if (decimalWithPoint.matcher( value ).matches()) {
                return GuessedType.DECIMAL_EN;
            }
            if (arabicDecimalWithPoint.matcher( value ).matches()) {
                return GuessedType.DECIMAL_AR;
            }
            // grouping and decimal
            if (germanDecimalWithGrouping.matcher( value ).matches()) {
                return GuessedType.DECIMAL_DE;
            }
            if (englishDecimalWithGrouping.matcher( value ).matches()) {
                return GuessedType.DECIMAL_EN;
            }
            if (arabicDecimalWithGrouping.matcher( value ).matches()) {
                return GuessedType.DECIMAL_AR;
            }
            if (russianDecimalWithGrouping.matcher( value ).matches()) {
                return GuessedType.DECIMAL_RU;
            }
        }
        return GuessedType.TEXT;
    }
}
