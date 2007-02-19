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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

package org.dcm4che2.audit.message;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 2, 2007
 */
public class AuditMessageUtils {

    private static final int millisPerMinute = 60 * 1000;

    private static boolean enableDNSLookups = false;
    private static boolean timezonedDateTime = true;
    private static boolean utcDateTime = false;

    private static InetAddress localHost = null;
    private static String processID = null;
    
    public static final boolean isEnableDNSLookups() {
        return enableDNSLookups;
    }

    public static final void setEnableDNSLookups(boolean enableDNSLookups) {
        AuditMessageUtils.enableDNSLookups = enableDNSLookups;
    }

    public static String getHostName(InetAddress node) {
        return enableDNSLookups ? node.getCanonicalHostName() : node
                .getHostAddress();
    }

    public static String getLocalHostName() {
        return getHostName(getLocalHost());
    }

    public static InetAddress getLocalHost() {
        if (localHost == null) {
            try {
                localHost = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                try {
                    localHost = InetAddress.getByName(null);
                } catch (UnknownHostException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
        return localHost;
    }
    
    public static String aetToAltUserID(String aet) {
        if (aet == null || aet.length() == 0) {
            return null;
        }
        return "AETITLES=" + aet;
    }

    public static String aetsToAltUserID(String[] aets) {
        if (aets == null || aets.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer("AETITLES=");
        for (int i = 0; i < aets.length; i++) {
            if (aets[i] == null) {
                throw new NullPointerException("aets[" + i + "]");
            }                      
            if (aets[i].length() == 0) {
                throw new IllegalArgumentException("aets[" + i + "]=\"\"");
            }
            if (i > 0) {
                sb.append(';');
            }
            sb.append(aets[i]);
        }
        return sb.toString();
    }
    
    public static String[] altUserIDToAETs(String altUserID) {
        if (altUserID == null || !altUserID.startsWith("AETITLES=")) {
            return new String[0];
        }
        return altUserID.substring(9).split(";");
    }

    public static String toDateTimeStr(Date date) {
        Calendar c = Calendar.getInstance(Locale.ENGLISH);
        c.setTime(date);
        if (utcDateTime) {
            c.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        StringBuffer sb = new StringBuffer(30);
        sb.append(c.get(Calendar.YEAR));
        appendNNTo('-', c.get(Calendar.MONTH), sb);
        appendNNTo('-', c.get(Calendar.DAY_OF_MONTH), sb);
        appendNNTo('T', c.get(Calendar.HOUR_OF_DAY), sb);
        appendNNTo(':', c.get(Calendar.MINUTE), sb);
        appendNNTo(':', c.get(Calendar.SECOND) 
                      + c.get(Calendar.MILLISECOND) / 1000f, sb);
        if (timezonedDateTime) {
            appendTZTo(c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET),
                    sb);
        }
        return sb.toString();
    }

    private static void appendNNTo(char c, int i, StringBuffer sb) {
        sb.append(c);
        if (i < 10) sb.append('0');
        sb.append(i);
    }

    private static void appendNNTo(char c, float f, StringBuffer sb) {
        sb.append(c);
        if (f < 10) sb.append('0');
        sb.append(f);
    }

    private static void appendTZTo(int millis, StringBuffer sb) {
        int mm = millis / millisPerMinute;
        if (mm == 0) {
            sb.append('Z');
            return;
        }
        char sign;
        if (mm > 0) {
            sign = '+';
        } else {
            sign = '-';
            mm = -mm;
        }
        int hh = mm / 60;
        mm -= hh * 60;
        appendNNTo(sign, hh, sb);
        appendNNTo(':', mm, sb);
    }

    public static String getProcessID() {
        if (processID == null) {
            try {
                Class c1 = Class.forName("java.lang.management.ManagementFactory");
                Method getRuntime = c1.getMethod("getRuntimeMXBean", null);
                Object rt = getRuntime.invoke(null, null);
                Class c2 = Class.forName("java.lang.management.RuntimeMXBean");
                Method getName = c2.getMethod("getName", null);
                processID = (String) getName.invoke(rt, null);
            } catch (Exception e) { // fallback for JDK 1.4
                int random = Math.abs(new Random().nextInt());
                processID = "" + random + "@" + getLocalHostName();
            }
        }
        return processID;
    }
    
}
