/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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

package org.dcm4che.util;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Calendar;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since September 21, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class DTFormat extends SimpleDateFormat {
    
    
    // Constants -----------------------------------------------------
    
    // Variables -----------------------------------------------------
    
    // Constructors --------------------------------------------------
    public DTFormat() {
        super("yyyyMMddHHmmss.SSS");
    }
    
    // Methods -------------------------------------------------------
    public Date parse(String source, ParsePosition pos) {        
        calendar.clear();
        try {
            String s = parseTZ(source);
            int l = s.length();
            calendar.set(Calendar.YEAR,
                Integer.parseInt(s.substring(0,4)));
            pos.setIndex(4);
            if (l > 4) {
                calendar.set(Calendar.MONTH,
                    Integer.parseInt(s.substring(4,6)) - 1);
                pos.setIndex(6);
                if (l > 6) {
                    calendar.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(s.substring(6,8)));
                    pos.setIndex(8);
                    if (l > 8) {
                        calendar.set(Calendar.HOUR_OF_DAY,
                            Integer.parseInt(s.substring(8,10)));
                        pos.setIndex(10);
                        if (l > 10) {
                            calendar.set(Calendar.MINUTE,
                                Integer.parseInt(s.substring(10,12)));
                            pos.setIndex(12);
                            if (l > 12) {
                                float f = Float.parseFloat(s.substring(12));
                                int i = (int) f;
                                calendar.set(Calendar.SECOND, i);
                                calendar.set(Calendar.MILLISECOND,
                                    (int) (1000 * (f - i)));
                            }
                        }
                    }
                }
            }            
            pos.setIndex(source.length());
            return calendar.getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    private String parseTZ(String source) {
        int zpos = source.length() - 5;
        if (zpos >= 0) {
            char ch = source.charAt(zpos);
            if (ch == '+' || ch == '-') {
                calendar.set(Calendar.ZONE_OFFSET, 
                    Integer.parseInt(source.substring(zpos)));
                calendar.set(Calendar.DST_OFFSET, 0);
                return source.substring(0, zpos);
            }
        }
        return source;
    }    
}
