/*
 * polymap.org Copyright (C) 2016 individual contributors as indicated by
 * the @authors tag. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.p4.style.color;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Joerg Reichert <joerg@mapzone.io>
 * @author Steffen Stundzig <steffen@mapzone.io>
 */
public interface ColorPanelInfo {

    RGB getColor();


    void updateColor( RGB newFieldValue );
}
