/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.print;

import javax.print.attribute.EnumSyntax;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  February 6, 2003
 * @version  $Revision$
 */
public class PrinterStatus extends EnumSyntax
{

    /**  Description of the Field */
    public final static PrinterStatus NORMAL = new PrinterStatus(0);
    /**  Description of the Field */
    public final static PrinterStatus WARNING = new PrinterStatus(1);
    /**  Description of the Field */
    public final static PrinterStatus FAILURE = new PrinterStatus(2);


    // Constructors --------------------------------------------------
    /**
     *  Constructor for the PrinterStatus object
     *
     * @param  value Description of the Parameter
     */
    protected PrinterStatus(int value)
    {
        super(value);
    }


    // Public --------------------------------------------------------

    // EnumSyntax overrides ---------------------------------------------------
    private final static String[] myStringTable = {
            "NORMAL",
            "WARNING",
            "FAILURE"
            };

    private final static PrinterStatus[] myEnumValueTable = {
            NORMAL,
            WARNING,
            FAILURE
            };


    /**
     *  Returns the string table for class PrinterStatus.
     *
     * @return  The stringTable value
     */
    protected String[] getStringTable()
    {
        return myStringTable;
    }


    /**
     *  Returns the enumeration value table for class PrinterStatus.
     *
     * @return  The enumValueTable value
     */
    protected EnumSyntax[] getEnumValueTable()
    {
        return myEnumValueTable;
    }

    // Y implementation ----------------------------------------------
}

