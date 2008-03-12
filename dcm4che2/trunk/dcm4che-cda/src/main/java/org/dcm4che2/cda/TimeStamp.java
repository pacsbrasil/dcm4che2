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
 * Portions created by the Initial Developer are Copyright (C) 2002-2008
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

/**
 * @author Gunter Zeilinger<gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Mar 10, 2008
 */
class TimeStamp {

    private static final int MILLISECONDS_PER_MIN = 60000;

    private final Date time; 

    public TimeStamp(Date time) {
        this.time = time;
    }

    public final Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        Calendar c = new GregorianCalendar();
        c.setTime(time);
        return containsTime(c) ? formatDateAndTime(c) : formatDate(c);
    }

    private boolean containsTime(Calendar c) {
        return c.get(Calendar.HOUR_OF_DAY) != 0
        || c.get(Calendar.MINUTE) != 0
        || c.get(Calendar.SECOND) != 0;
    }

    private String formatDate(Calendar c) {
        StringBuffer sb = new StringBuffer(8);
        appendYYYYMMDDTo(c, sb);
        return sb.toString();
    }

    private void appendYYYYMMDDTo(Calendar c, StringBuffer sb) {
        sb.append(c.get(Calendar.YEAR));
        appendNNTo(c.get(Calendar.MONTH)+1, sb);
        appendNNTo(c.get(Calendar.DAY_OF_MONTH), sb);
    }
    
    private void appendNNTo(int i, StringBuffer sb) {
        if (i < 10)
            sb.append('0');
        sb.append(i);
    }

    private String formatDateAndTime(Calendar c) {
        StringBuffer sb = new StringBuffer(19);
        appendYYYYMMDDTo(c, sb);
        appendNNTo(c.get(Calendar.HOUR_OF_DAY), sb);
        appendNNTo(c.get(Calendar.MINUTE), sb);
        appendNNTo(c.get(Calendar.SECOND), sb);
        appendZONETo(c.get(Calendar.ZONE_OFFSET), sb);
        return null;
    }

    private void appendZONETo(int ms, StringBuffer sb) {
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
