/* 
 * polymap.org
 * Copyright (C) 2016, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.p4.catalog;

import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.rap.rwt.RWT;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DateValidator
        implements IFormFieldValidator<String,Date> {

    @Override
    public String validate( String fieldValue ) {
        return null;
    }


    @Override
    public String transform2Field( Date modelValue ) throws Exception {
        DateFormat df = SimpleDateFormat.getDateInstance( SimpleDateFormat.MEDIUM, RWT.getLocale() );
        return modelValue != null ? df.format( modelValue ) : "-";
    }


    @Override
    public Date transform2Model( String fieldValue ) throws Exception {
        return null;
    }
    
}
