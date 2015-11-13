package org.polymap.p4.data.imports.refine;

public class RefineColumn {

    private String name;

    private Class  type;


    public RefineColumn( String name ) {
        this.name = name;
    }


    public String name() {
        return name;
    }


    public Class type() {
        return type != null ? type : String.class;
    }


    void setType( Class type ) {
        this.type = type;
    }
}
