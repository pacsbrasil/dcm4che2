/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.ejb.util;

import java.util.ArrayList;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 03.12.2003
 */
public class StringUtils {
    
    public static String[] EMPTY_STRING_ARRAY = {};
    
    public static String[] split(String s, char delim) {
        if (s == null) {
            return null;
        }
        if (s.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        int end = s.indexOf(delim);
        if (end == -1) {
            return new String[]{s};
        }
        ArrayList list = new ArrayList();
        int start = 0;
        do {
            list.add(s.substring(start, end));
            start = end + 1;
        } while ((end = s.indexOf(delim, start)) != -1);
        list.add(s.substring(start));
        return (String[]) list.toArray(new String[list.size()]);
    }
    
    public static String toString(String[] a, char delim) {
        if (a == null) {
            return null;
        }
        if (a.length == 0) {
            return "";
        }
        if (a.length == 1) {
            return a[0];
        }
        StringBuffer sb = new StringBuffer(a[0]);
        for (int i = 1; i < a.length; ++i) {
            sb.append(delim).append(a[i]);
        }
        return sb.toString();
    }
}
