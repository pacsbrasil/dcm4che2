/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.storescp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.dcm4cheri.util.StringUtils;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 15.05.2004
 *
 */
class ForwardAETs {

    static final String OTHER = "OTHER";

    static final String NONE = "NONE";

    static final String[] EMPTY = {};

    private String[] defaultAETs = EMPTY;

    private final ArrayList list = new ArrayList();

    private static final class Entry {

        final HashSet callingAETs;

        final String[] forwardAETs;

        Entry(String callingAETs, String[] forwardAETs) {
            this.callingAETs = new HashSet(Arrays.asList(StringUtils.split(
                    callingAETs, ',')));
            this.forwardAETs = forwardAETs;
        }
    }

    public ForwardAETs(String s) {
        StringTokenizer stk = new StringTokenizer(s, "\r\n;");
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken().trim();
            if (tk.length() == 0) continue;
            int colon = tk.indexOf(':');
            String first = colon == -1 ? "" : tk.substring(0, colon).trim();
            String second = tk.substring(colon + 1).trim();
            String[] aets = (second.length() == 0 || NONE
                    .equalsIgnoreCase(second)) ? EMPTY : StringUtils.split(
                    second, ',');
            if (first.length() == 0 || OTHER.equalsIgnoreCase(first))
                defaultAETs = aets;
            else
                list.add(new Entry(first, aets));
        }
    }

    public String[] get(String callingAET) {
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            if (e.callingAETs.contains(callingAET)) return e.forwardAETs;
        }
        return defaultAETs;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Entry e = (Entry) it.next();
            appendTo(e.callingAETs, sb);
            sb.append(':');
            if (e.forwardAETs.length == 0)
                sb.append(NONE);
            else
                appendTo(Arrays.asList(e.forwardAETs), sb);
            sb.append('\r').append('\n');
        }
        sb.append(OTHER).append(':');
        if (defaultAETs.length == 0)
            sb.append(NONE);
        else
            appendTo(Arrays.asList(defaultAETs), sb);
        return sb.toString();
    }

    private static void appendTo(Collection aets, StringBuffer sb) {
        if (aets.isEmpty()) return;
        for (Iterator it = aets.iterator(); it.hasNext();)
            sb.append((String) it.next()).append(',');
        sb.setLength(sb.length() - 1);
    }
}
