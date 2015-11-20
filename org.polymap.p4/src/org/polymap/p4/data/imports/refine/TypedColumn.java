package org.polymap.p4.data.imports.refine;

import java.util.Locale;

public class TypedColumn {

    private String name;

    private Class  type;


    public TypedColumn( String name ) {
        this.name = name;
    }


    public String name() {
        return name;
    }


    public Class type() {
        return type;
    }


    public void setType( Class type ) {
        this.type = type;
    }


    public void addLocale( Locale guessedLocale ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String toString() {
        return name + ": " + (type != null ? type.getSimpleName() : "unknown"); 
    }
}
