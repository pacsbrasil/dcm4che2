/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.common;

import java.util.Arrays;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Aug 3, 2005
 */
public class HPLevel {
    private static final String[] ENUM = { "MANUFACTURER", "IN PROGRESS", 
        "USER_GROUP", "SINGLE_USER" };

    public static final int MANUFACTURER = 0;    
    public static final int SITE = 1;    
    public static final int USER_GROUP = 2;    
    public static final int SINGLE_USER = 3;
	
    public static final String toString(int value) {
        return ENUM[value];
    }

    public static final int toInt(String s) {        
        final int index = Arrays.asList(ENUM).indexOf(s);
        if (index == -1)
            throw new IllegalArgumentException(s);
        return index;
    }

}
