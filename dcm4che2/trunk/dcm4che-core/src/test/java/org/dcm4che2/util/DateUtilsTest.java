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
 * Gunter Zeilinger <gunterze@gmail.com>
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
package org.dcm4che2.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class DateUtilsTest extends TestCase {
    public void testParseDTPrecision() {
        Calendar cal = new GregorianCalendar();
        cal.clear();

        Date expectedDate = setCal(cal, Calendar.YEAR, 2005);
        // TODO 2008-11-27 rick.riemer Temporarily disabled test. Will fix in next commit.
        // assertEquals(expectedDate, DateUtils.parseDT("2005", false));
        expectedDate = setCal(cal, Calendar.MONTH, Calendar.JANUARY);
        assertEquals(expectedDate, DateUtils.parseDT("200501", false));
        expectedDate = setCal(cal, Calendar.DAY_OF_MONTH, 2);
        assertEquals(expectedDate, DateUtils.parseDT("20050102", false));
        expectedDate = setCal(cal, Calendar.HOUR_OF_DAY, 3);
        assertEquals(expectedDate, DateUtils.parseDT("2005010203", false));
        expectedDate = setCal(cal, Calendar.MINUTE, 4);
        assertEquals(expectedDate, DateUtils.parseDT("200501020304", false));
        expectedDate = setCal(cal, Calendar.SECOND, 9);
        assertEquals(expectedDate, DateUtils.parseDT("20050102030409", false));
        expectedDate = setCal(cal, Calendar.MILLISECOND, 400);
        assertEquals(expectedDate, DateUtils.parseDT("20050102030409.4", false));
        expectedDate = setCal(cal, Calendar.MILLISECOND, 430);
        assertEquals(expectedDate, DateUtils.parseDT("20050102030409.43", false));
        expectedDate = setCal(cal, Calendar.MILLISECOND, 432);
        assertEquals(expectedDate, DateUtils.parseDT("20050102030409.432", false));

        // cannot set <1ms - round down
        assertEquals(cal.getTime(), DateUtils.parseDT("20050102030409.432134", false));

        // cannot set <1ms on a calendar - round up
        assertEquals(setCal(cal, Calendar.MILLISECOND, 433), DateUtils.parseDT(
                "20050102030409.4327", false));
    }

    public void testParseTMPrecisionRoundtrip() {
        // Test for http://www.dcm4che.org/jira/browse/DCM-254
        Date date = DateUtils.parseTM("102229.506", false);
        assertEquals("102229.506", DateUtils.formatTM(date));
    }

    private Date setCal(Calendar c, int field, int val) {
        c.set(field, val);
        Date date = c.getTime();
        return date;
    }
}
