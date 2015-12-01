package org.polymap.p4.data.imports.refine;

import com.google.refine.model.Cell;

/**
 * Wrapper around the original refine cell, to store also the guest value
 * 
 * @author <a href="http://stundzig.it">Steffen Stundzig</a>
 */
public class RefineCell {

    private final Cell   original;

    private final Object guessedValue;


    public RefineCell( Cell original ) {
        this( original, null );
    }


    public RefineCell( Cell original, Object guessedValue ) {
        this.original = original;
        this.guessedValue = guessedValue;
    }


    public Object value() {
        return original != null ? original.value : null;
    }


    public Object guessedValue() {
        return guessedValue != null ? guessedValue : value();
    }
}
