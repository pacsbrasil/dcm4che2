/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che2.cda;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 11, 2008
 */
class TimeElement extends BaseElement {

    private static final int MILLISECONDS_PER_MIN = 60000;

    protected TimeElement(String name, String value) {
        super(name, "value", value);
    }

    protected TimeElement(String name, Date time, TimeZone tz) {
        super(name, "value", formatTime(time, tz));
    }

    protected TimeElement(String name, Date time, boolean tz) {
        super(name, "value", formatTime(time, tz));
    }

    public String getValue() {
        return (String) getAttribute("value");
    }

    public Date getTime() {
        return parseTime(getValue());
    }

    static Date parseTime(String s) {
        int len = s.length();
        Calendar c = new GregorianCalendar();
        c.clear();
        if (len > 5) {
            int tzPos = s.indexOf('+', len - 5);
            if (tzPos == -1) {
                tzPos = s.indexOf('-', len - 5);
            }
            if (tzPos != -1) {
                c.setTimeZone(TimeZone.getTimeZone("GMT" + s.substring(tzPos)));
                len = tzPos;
            }
        }
        try {
            c.set(Calendar.YEAR, Integer.parseInt(s.substring(0, Math.min(4,
                            len))));
            if (len > 4) {
                c.set(Calendar.MONTH, Integer.parseInt(s.substring(4, Math.min(6,
                        len))) - 1);
                if (len > 6) {
                    c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(6,
                            Math.min(8, len))));
                    if (len > 8) {
                        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(s.substring(8,
                                Math.min(10, len))));
                        if (len > 10) {
                            c.set(Calendar.MINUTE, Integer.parseInt(s.substring(10,
                                    Math.min(12, len))));
                            if (len > 12) {
                                c.set(Calendar.SECOND, Integer.parseInt(s
                                        .substring(12, Math.min(14, len))));
                                if (len > 16) {
                                    c.set(Calendar.MILLISECOND,
                                            (int) (Float.parseFloat(s.substring(15,
                                                    len)) * 1000));
                                }
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return c.getTime();
    }

    static String formatTime(Date time, boolean tz) {
        Calendar c = new GregorianCalendar();
        c.setTime(time);
        return formatTime(c, tz);
    }

    static String formatTime(Date time, TimeZone tz) {
        Calendar c = new GregorianCalendar(tz);
        c.setTime(time);
        return formatTime(c, true);
    }

    private static String formatTime(Calendar c, boolean tz) {
        StringBuffer sb = new StringBuffer(tz ? 19 : 14);
        sb.append(c.get(Calendar.YEAR));
        appendNNTo(c.get(Calendar.MONTH)+1, sb);
        appendNNTo(c.get(Calendar.DAY_OF_MONTH), sb);
        appendNNTo(c.get(Calendar.HOUR_OF_DAY), sb);
        appendNNTo(c.get(Calendar.MINUTE), sb);
        appendNNTo(c.get(Calendar.SECOND), sb);
        if (tz)
            appendZONETo(c.get(Calendar.ZONE_OFFSET), sb);
        return sb.toString();
    }

    private static void appendNNTo(int i, StringBuffer sb) {
        if (i < 10)
            sb.append('0');
        sb.append(i);
    }

    private static void appendZONETo(int ms, StringBuffer sb) {
        if (ms < 0) {
            sb.append('-');
            ms = -ms;
        } else {
            sb.append('+');
        }
        int min = ms / MILLISECONDS_PER_MIN;
        appendNNTo(min / 60, sb);
        appendNNTo(min % 60, sb);
    }
}
