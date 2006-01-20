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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Properties;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 21, 2005
 * 
 */
public class UIDUtils
{

    private static final int EXPECT_DOT = 0;
    private static final int EXPECT_FIRST_DIGIT = 1;
    private static final int EXPECT_DOT_OR_DIGIT = 2;
    private static final int ILLEGAL_UID = -1;

    private static final String hostAddress = getHostAddress();
    private static final String hostUnique = getHostUnique();
    private static String lastTime = getTime();
    private static short lastCount = Short.MIN_VALUE;
    private static String root;
    private static boolean useHostAddress;
    private static boolean useHostUnique;
    private static Object lock = new Object();
    private static boolean acceptLeadingZero = false;

    private static boolean toBoolean(String name)
    {
        return ((name != null) && name.equalsIgnoreCase("true"));
    }

    static
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Properties p = new Properties();
        try
        {
            p.load(cl.getResourceAsStream("org/dcm4che2/util/UIDUtils.properties"));
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Failed to load resource org/dcm4che2/util/UIDUtils.properties",
                    e);
        }
        useHostAddress = toBoolean(p.getProperty("useHostAddress"));
        useHostUnique = toBoolean(p.getProperty("useHostUnique"));
        setRoot(p.getProperty("root"));
    }

    public static final boolean isAcceptLeadingZero()
    {
        return acceptLeadingZero;
    }

    public static final void setAcceptLeadingZero(boolean acceptLeadingZero)
    {
        UIDUtils.acceptLeadingZero = acceptLeadingZero;
    }

    private static String getHostAddress()
    {
        try
        {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            return "127.0.0.1";
        }
    }

    static private void appendInt(int i, int width, StringBuffer sb)
    {
        String s = Integer.toString(i);
        for (i = s.length(); i < width; ++i)
            sb.append('0');
        sb.append(s);
    }

    private static String getTime()
    {
        Calendar cal = Calendar.getInstance();
        StringBuffer sb = new StringBuffer(17);
        sb.append(cal.get(Calendar.YEAR));
        appendInt(cal.get(Calendar.MONTH) + 1, 2, sb);
        appendInt(cal.get(Calendar.DAY_OF_MONTH), 2, sb);
        appendInt(cal.get(Calendar.HOUR_OF_DAY), 2, sb);
        appendInt(cal.get(Calendar.MINUTE), 2, sb);
        appendInt(cal.get(Calendar.SECOND), 2, sb);
        appendInt(cal.get(Calendar.MILLISECOND), 3, sb);
        return sb.toString();
    }

    private static String getHostUnique()
    {
        return Long.toString((new Object()).hashCode() & 0xffffffffL);
    }

    public static void setRoot(String root)
    {
        verifyUID(root);
        verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.root = root;
    }

    private static void verifyMaxCreatedUIDLength(String root,
            boolean useHostAddress, boolean useHostUnique)
    {
        int len = root.length();
        if (useHostAddress)
            len += 16;
        if (useHostUnique)
            len += 11;
        if (len > 40)
            throw new IllegalArgumentException("With root=" + root
                    + ", useHostAddress=" + useHostAddress + ", useHostUnique="
                    + useHostUnique
                    + " created UIDs exceeds length limit of 64.");

    }

    public static String getRoot()
    {
        return root;
    }

    public static final boolean isUseHostAddress()
    {
        return useHostAddress;
    }

    public static final void setUseHostAddress(boolean useHostAddress)
    {
        if (useHostAddress)
            verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.useHostAddress = useHostAddress;
    }

    public static boolean isUseHostUnique()
    {
        return useHostUnique;
    }

    public static void setUseHostUnique(boolean useHostUnique)
    {
        if (useHostUnique)
            verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.useHostUnique = useHostUnique;
    }

    public static void verifyUID(String uid)
    {
        verifyUID(uid, acceptLeadingZero);
    }

    public static void verifyUID(String uid, boolean acceptLeadingZero)
    {
        if (!isValidUID(uid, acceptLeadingZero))
            throw new IllegalArgumentException(uid);
    }

    public static boolean isValidUID(String uid)
    {
        return isValidUID(uid, acceptLeadingZero);
    }

    public static boolean isValidUID(String uid, boolean acceptLeadingZero)
    {
        int len = uid.length();
        if (len > 64)
            return false;

        int state = EXPECT_FIRST_DIGIT;
        for (int i = 0; i < len; i++)
        {
            state = nextState(state, uid.charAt(i), acceptLeadingZero);
            if (state == ILLEGAL_UID)
                return false;
        }
        return state != EXPECT_FIRST_DIGIT;
    }

    private static int nextState(int state, int ch, boolean acceptLeadingZero)
    {
        return ch == '.'
                ? (state == EXPECT_FIRST_DIGIT
                        ? ILLEGAL_UID
                        : EXPECT_FIRST_DIGIT)
                : (state == EXPECT_DOT || ch < '0' || ch > '9')
                        ? ILLEGAL_UID
                        : !acceptLeadingZero && state == EXPECT_FIRST_DIGIT
                                && ch == '0' ? EXPECT_DOT : EXPECT_DOT_OR_DIGIT;
    }

    public static String createUID()
    {
        StringBuffer sb = new StringBuffer(64).append(root).append('.');
        if (useHostAddress)
            sb.append(hostAddress).append('.');
        if (useHostUnique)
            sb.append(hostUnique).append('.');
        synchronized (lock)
        {
            if (lastCount == Short.MAX_VALUE)
            {
                lastTime = getTime();
                lastCount = Short.MIN_VALUE;
            }
            sb.append(lastTime).append('.').append(lastCount++);
        }
        return sb.toString();
    }
}
