/*****************************************************************************
 *                                                                           *
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
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import javax.print.attribute.EnumSyntax;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since February 6, 2003
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class PrinterStatus extends EnumSyntax {
   
   public static final PrinterStatus NORMAL = new PrinterStatus(0);
   public static final PrinterStatus WARNING = new PrinterStatus(1);
   public static final PrinterStatus FAILURE = new PrinterStatus(2);
   
   // Constructors --------------------------------------------------
   protected PrinterStatus(int value) {
      super(value);
   }
   
   // Public --------------------------------------------------------
   
   // EnumSyntax overrides ---------------------------------------------------
    private static final String[] myStringTable = {
	"NORMAL",
	"WARNING",
	"FAILURE"
    };

    private static final PrinterStatus[] myEnumValueTable = {
	NORMAL,
	WARNING,
	FAILURE
    };

    /**
     * Returns the string table for class PrinterStatus.
     */
    protected String[] getStringTable() {
	return myStringTable;
    }

    /**
     * Returns the enumeration value table for class PrinterStatus.
     */
    protected EnumSyntax[] getEnumValueTable() {
	return myEnumValueTable;
    }
   
   // Y implementation ----------------------------------------------
}
