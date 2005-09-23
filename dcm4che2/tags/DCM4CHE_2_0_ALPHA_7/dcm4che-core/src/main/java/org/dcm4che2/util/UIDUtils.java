/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

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
public class UIDUtils {

    private static final String hostAddress = getHostAddress();
    private static final String hostUnique = getHostUnique();
    private static String lastTime = getTime();
    private static short lastCount = Short.MIN_VALUE;
    private static String root;
    private static boolean useHostAddress;
    private static boolean useHostUnique;
    private static Object lock = new Object();

    private static boolean toBoolean(String name) { 
        return ((name != null) && name.equalsIgnoreCase("true"));
    }
    
    static  {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Properties p = new Properties();
        try {
            p.load(cl.getResourceAsStream("org/dcm4che2/util/UIDUtils.properties"));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load resource org/dcm4che2/util/UIDUtils.properties", e);
        }
        useHostAddress = toBoolean(p.getProperty("useHostAddress"));
        useHostUnique = toBoolean(p.getProperty("useHostUnique"));
        setRoot(p.getProperty("root"));
    }
    
    private static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }    

    static private void appendInt(int i, int width, StringBuffer sb) {
        String s = Integer.toString(i);
        for (i = s.length(); i < width; ++i)
            sb.append('0');
        sb.append(s);
    }
    
    private static String getTime() {
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

    private static String getHostUnique() {
        return Long.toString((new Object()).hashCode() & 0xffffffffL);
    }

    public static void setRoot(String root) {
        verifyUID(root);
        verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.root = root;
    }

    private static void verifyMaxCreatedUIDLength(String root, 
            boolean useHostAddress, boolean useHostUnique) {
        int len = root.length();
        if (useHostAddress)
            len += 16;
        if (useHostUnique)
            len += 11;
        if (len > 40)
            throw new IllegalArgumentException("With root=" + root 
                    + ", useHostAddress=" + useHostAddress
                    + ", useHostUnique=" + useHostUnique
                    + " created UIDs exceeds length limit of 64.");
        
    }

    public static String getRoot() {
        return root;
    }

    public static final boolean isUseHostAddress() {
        return useHostAddress;
    }

    public static final void setUseHostAddress(boolean useHostAddress) {
        if (useHostAddress)
            verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.useHostAddress = useHostAddress;
    }

    public static boolean isUseHostUnique() {
        return useHostUnique;
    }

    public static void setUseHostUnique(boolean useHostUnique) {
        if (useHostUnique)
            verifyMaxCreatedUIDLength(root, useHostAddress, useHostUnique);
        UIDUtils.useHostUnique = useHostUnique;
    }
    
    public static void verifyUID(String uid) {
        int len = uid.length();
        if (len > 64) {
            throw new IllegalArgumentException(
                    "exceeds maximal length - " + uid);            
        }
        boolean first = true;
        for (int i = 0; i < len; i++) {
            char ch = uid.charAt(i);
            if (first) {
                if (ch <= '0' || ch > '9')
                    throw new IllegalArgumentException(uid);
                first = false;
            } else {
                if (ch == '.')
                    first = true;
                else
                    if (ch < '0' || ch > '9')
                        throw new IllegalArgumentException(uid);
            }
        }
        if (first)
            throw new IllegalArgumentException(uid);
    }

    public static String createUID() {
        StringBuffer sb = new StringBuffer(64).append(root).append('.');
        if (useHostAddress)
            sb.append(hostAddress).append('.');
        if (useHostUnique)
            sb.append(hostUnique).append('.');
        synchronized (lock) {
            if (lastCount == Short.MAX_VALUE) {
                lastTime = getTime();
                lastCount = Short.MIN_VALUE;
            }
            sb.append(lastTime).append('.').append(lastCount++);
        }
        return sb.toString();
    }
}
