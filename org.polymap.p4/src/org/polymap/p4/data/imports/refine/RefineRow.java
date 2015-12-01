package org.polymap.p4.data.imports.refine;

import java.util.List;

import com.google.common.collect.Lists;

public class RefineRow {

    private final List<RefineCell> cells = Lists.newArrayList();


    public void add( RefineCell cell ) {
        cells.add( cell );
    }


    public List<RefineCell> cells() {
        return cells;
    }
}
