package org.polymap.p4.data.imports.refine;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TypedContent {

    private final List<TypedColumn> columns;

    private final List<RefineRow>   rows;


    public TypedContent( List<TypedColumn> columns, List<RefineRow> rows ) {
        this.columns = columns;
        this.rows = rows;
    }


    public List<TypedColumn> columns() {
        return columns;
    }


    public List<RefineRow> rows() {
        return rows;
    }


    public int columnIndex( String columnName ) {
        if (!StringUtils.isBlank( columnName )) {
            int i = 0;
            for (TypedColumn column : columns) {
                if (columnName.equals( column.name() )) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

}
