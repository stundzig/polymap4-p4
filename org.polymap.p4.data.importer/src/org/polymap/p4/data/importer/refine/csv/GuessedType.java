package org.polymap.p4.data.importer.refine.csv;

import java.util.Locale;

public class GuessedType {

    public static final GuessedType TEXT       = new GuessedType( Type.Text );

    public static final GuessedType DECIMAL    = new GuessedType( Type.Decimal );

    public static final GuessedType DECIMAL_RU = new GuessedType( Type.Decimal, new Locale( "ru" ) );

    public static final GuessedType DECIMAL_AR = new GuessedType( Type.Decimal, new Locale( "ar" ) );

    public static final GuessedType DECIMAL_EN = new GuessedType( Type.Decimal, Locale.ENGLISH );

    public static final GuessedType DECIMAL_DE = new GuessedType( Type.Decimal, Locale.GERMAN );


    public GuessedType( Type type ) {
        this( type, null );
    }


    public GuessedType( Type type, Locale locale ) {
        this.type = type;
        this.locale = locale;
    }

    private Type   type;

    private Locale locale;


    public Type type() {
        return type;
    }


    public Locale locale() {
        return locale;
    }
}
