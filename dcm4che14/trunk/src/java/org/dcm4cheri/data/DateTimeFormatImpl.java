/* $Id$ */
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

package org.dcm4cheri.data;

import org.dcm4che.data.DateTimeFormat;
import java.util.Date;
import java.util.Calendar;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 0.9.9
 */
public final class DateTimeFormatImpl
    implements org.dcm4che.data.DateTimeFormat {

    // Constants ----------------------------------------------------
    private static final java.text.DateFormat DATE_FORMAT =
            new java.text.SimpleDateFormat("yyyyMMdd");
    private static final java.text.DateFormat TIME_FORMAT =
            new java.text.SimpleDateFormat("HHmmss.SSS");
    private static final java.text.DateFormat DATE_TIME_FORMAT =
            new java.text.SimpleDateFormat("yyyyMMddHHmmss.SSS");
    
    // Attributs -----------------------------------------------------
    static final DateTimeFormatImpl inst = new DateTimeFormatImpl();

    // Constructors --------------------------------------------------
    private DateTimeFormatImpl() {}
    
    public static DateTimeFormat getInstance() {
        return inst;
    }

    // Methodes ------------------------------------------------------
    public String formatDate(Date date) {
        return date != null ? DATE_FORMAT.format(date) : "";
    }
    
    public String formatTime(Date time) {
        return time != null ? TIME_FORMAT.format(time) : "";
    }
    
    public String formatDateTime(Date dateTime) {
        return dateTime != null ? DATE_TIME_FORMAT.format(dateTime) : "";
    }

    public String formatDateRange(Date from, Date to) {
        return formatDate(from) + '-' + formatDate(to);
    }
    
    public String formatTimeRange(Date from, Date to) {
        return formatTime(from) + '-' + formatTime(to);
    }
    
    public String formatDateTimeRange(Date from, Date to) {
        return formatDateTime(from) + '-' + formatDateTime(to);
    }
    
    public Date parseDate(String s) {
        if (s == null || s.length() == 0)
            return null;
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(0L));
        parseDate(c,s);
        return c.getTime();
    }

    private void parseDate(Calendar c, String s) {
        switch (s.length()) {
            case 8:
                parseNewDate(c,s);
                break;
            case 10:
                if (s.charAt(4) != '.' || s.charAt(7) != '.')
                    throw new IllegalArgumentException(s);
                parseOldDate(c,s);
                break;
            default:
                throw new IllegalArgumentException(s);
        }
    }
    
    private void parseNewDate(Calendar c, String s) {
        c.set(Integer.parseInt(s.substring(0,4)),
              Integer.parseInt(s.substring(4,6))-1,
              Integer.parseInt(s.substring(6,8)));
    }
    
    private void parseOldDate(Calendar c, String s) {
        c.set(Integer.parseInt(s.substring(0,4)),
              Integer.parseInt(s.substring(5,7))-1,
              Integer.parseInt(s.substring(8,10)));
    }

    public Date parseTime(String s) {
        if (s == null || s.length() == 0)
            return null;
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(0L));
        parseTime(c,s);
        return c.getTime();
    }
    
    private void parseTime(Calendar c, String s) {
        switch (s.indexOf(':')) {
            case -1:
                parseNewTime(c,s);
                break;
            case 2:
                parseOldTime(c,s);
                break;
            default:
                throw new IllegalArgumentException(s);
        }
    }

    private void parseNewTime(Calendar c, String s) {
        final int l = s.length();
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(0,2)));
        if (l > 2) {
            c.set(Calendar.MINUTE, Integer.parseInt(s.substring(2,4)));
            if (l > 4)
                parseSec(c, s.substring(4));
        }
    }

    private void parseOldTime(Calendar c, String s) {
        final int l = s.length();
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(0,2)));
        if (l > 3) {
            c.set(Calendar.MINUTE, Integer.parseInt(s.substring(3,5)));
            if (l > 5) {
                if (s.charAt(5) != ':')
                    throw new IllegalArgumentException(s);
                if (l > 6)
                    parseSec(c, s.substring(6));
            }
        }
    }

    private void parseSec(Calendar c, String s) {
        int l = s.length();
        c.set(Calendar.SECOND, Integer.parseInt(s.substring(0,2)));
        if (l > 2) {       
            if (s.charAt(2) != '.')
                throw new IllegalArgumentException(s);
            if (l > 3) {
                float frac = Float.parseFloat(s.substring(2,Math.min(l,9)));
                c.set(Calendar.MILLISECOND, (int)(frac*1000));
                if (l > 9) {
                    switch (s.charAt(9)) {
                        case '+':
                        case '-':
                            c.set(Calendar.ZONE_OFFSET,
                                    Integer.parseInt(s.substring(9,l)));
                            break;
                        default:
                            throw new IllegalArgumentException(s);
                    }
                }
            }
        }
    }
    
    public Date parseDateTime(String s) {
        if (s == null || s.length() == 0)
            return null;
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(0L));
        parseNewDate(c,s);
        if (s.length() > 8)
            parseNewTime(c,s.substring(8));
        return c.getTime();
    }
        
    public Date parseDateTime(String date, String time) {
        if (date == null || date.length() == 0)
            return null;
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(0L));
        parseDate(c,date);
        if (time != null && time.length() > 0)
            parseTime(c,time);
        return c.getTime();
    }

    public boolean isDateRange(String s) {
        return s != null && s.indexOf('-') != -1;
    }

    public Date[] parseDateRange(String date) {
        if (date == null || date.length() == 0)
            return null;
        
        int hypPos = date.indexOf('-');
        Date[] range = new Date[2];
        range[1] = parseDate(date.substring(hypPos+1));
        if (hypPos == -1)
            range[0] = range[1];
        else if (hypPos != 0)
            range[0] = parseDate(date.substring(0, hypPos));
        
        return range;
    }
    
    public Date[] parseTimeRange(String time) {
        if (time == null || time.length() == 0)
            return null;
        
        int hypPos = time.indexOf('-');
        Date[] range = new Date[2];
        range[1] = parseTime(time.substring(hypPos+1));
        if (hypPos == -1)
            range[0] = range[1];
        else if (hypPos != 0)
            range[0] = parseTime(time.substring(0, hypPos));
        
        return range;
    }
    
    public Date[] parseDateTimeRange(String datetime) {
        if (datetime == null || datetime.length() == 0)
            return null;
        
        int hypPos = datetime.indexOf('-');
        Date[] range = new Date[2];
        range[1] = parseDateTime(datetime.substring(hypPos+1));
        if (hypPos == -1)
            range[0] = range[1];
        else if (hypPos != 0)
            range[0] = parseDateTime(datetime.substring(0, hypPos));
        
        return range;
    }

    private static final int MS_PER_DAY = 86400000;    
    
    public Date[] parseDateTimeRange(String date, String time) {
        if (date == null || date.length() == 0)
            return null;
        
        Date[] dateRange = parseDateRange(date);
        if (time == null || time.length() == 0)
            return dateRange;
        
        Date[] timeRange = parseTimeRange(time);
        return new Date[] {
                dateRange[0] == null ? null : new Date(dateRange[0].getTime()
                         + (timeRange[0] == null ? 0 : timeRange[0].getTime())),
                dateRange[1] == null ? null : new Date(dateRange[1].getTime()
                         + (timeRange[1] == null ? MS_PER_DAY
                                                 : timeRange[1].getTime()))};
    }
}
