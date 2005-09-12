/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.data;

import java.io.IOException;
import java.util.Properties;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Aug 22, 2005
 *
 */
public class Implementation {

    private static String classUID;
    private static String versionName;

    static  {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Properties p = new Properties();
        try {
            p.load(cl.getResourceAsStream("org/dcm4che2/data/Implementation.properties"));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load resource org/dcm4che2/data/Implementation.properties", e);
        }
        classUID = p.getProperty("classUID");
        versionName = p.getProperty("versionName");
    }

    public static final String classUID() {
        return classUID;
    }

    public static final String versionName() {
        return versionName;
    }
}
