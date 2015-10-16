package org.polymap.p4.data.imports.refine;

import java.util.List;

import com.google.refine.model.Cell;
import com.google.refine.model.Row;

public class RefineRow {

    private final int index;
    private final Row underlying;

    public RefineRow( int index, Row underlying ) {
        this.index = index;
        this.underlying = underlying;
    }

    public List<Cell> cells() {
        return underlying.cells;
    }

    public int index() {
        return index;
    }

}
