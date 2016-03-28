package org.polymap.p4.data.importer.refine.csv;

public class GuessedQuoteCharacter {

    private int                 singleQuote     = 0;

    private int                 doubleQuote     = 0;

    private int                 noQuote         = 0;

    private static final String singleQuoteChar = "'";

    private static final String noQuoteChar     = "\0";

    private static final String doubleQuoteChar = "\"";


    public void increaseSingle() {
        singleQuote++;
    }


    public void increaseDouble() {
        doubleQuote++;
    }


    public void increaseNo() {
        noQuote++;
    }


    public String quoteCharacter() {
//        if (doubleQuote > singleQuote) {
//            return doubleQuote > noQuote ? doubleQuoteChar : noQuoteChar;
//        }
//        if (singleQuote > doubleQuote) {
//            return singleQuote > noQuote ? singleQuoteChar : noQuoteChar;
//        }
//        if (noQuote > doubleQuote) {
//            return noQuote > singleQuote ? noQuoteChar : singleQuoteChar;
//        }
        if (doubleQuote > 0) {
            return doubleQuoteChar;
        } 
        if (singleQuote > 0) {
            return singleQuoteChar;
        }
        return noQuoteChar;
    }
}
