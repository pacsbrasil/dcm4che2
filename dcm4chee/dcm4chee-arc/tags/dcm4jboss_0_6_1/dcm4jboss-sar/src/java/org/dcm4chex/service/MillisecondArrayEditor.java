/* $Id$
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
package org.dcm4chex.service;

import java.beans.PropertyEditorSupport;
import java.util.StringTokenizer;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 17.12.2003
 */
final class MillisecondArrayEditor extends PropertyEditorSupport {
    public String getAsText() {
        long[] a = (long[]) getValue();
        if (a == null || a.length == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(a[i]);
        }
        return sb.toString();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        StringTokenizer stk = new StringTokenizer(text, ", \t\n\r");
        long[] a = new long[stk.countTokens()];
        for (int i = 0; i < a.length; i++) {
            a[i] = parseMilliseconds(stk.nextToken());
        }
        setValue(a);
    }

    private long parseMilliseconds(String text) {
        int len = text.length();
        long k = 1L;
        switch (text.charAt(len - 1)) {
            case 'd' :
                k *= 24;
            case 'h' :
                k *= 60;
            case 'm' :
                k *= 60;
            case 's' :
                k *= 1000;
                --len;
        }
        return k * Long.parseLong(text.substring(0, len));
    }

}
