/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4che.util;

import java.util.StringTokenizer;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class DcmURL extends Object {
    // Constants -----------------------------------------------------    
    public static final String ANONYMOUS = "anonymous";
    public static final int DICOM_PORT = 104;
    
    private static final int DELIMITER = -1;
    private static final int CALLED_AET = 0;
    private static final int CALLING_AET = 1;
    private static final int HOST = 2;
    private static final int PORT = 3;
    private static final int END = 4;
    
    // Attributes ----------------------------------------------------
    private String calledAET;
    private String callingAET = ANONYMOUS;
    private String host;
    private int port = DICOM_PORT;

    // Constructors --------------------------------------------------
    public DcmURL(String spec) {
        parse(spec.trim());
        if (calledAET == null) {
            throw new IllegalArgumentException("Missing called AET");
        }
        if (host == null) {
            throw new IllegalArgumentException("Missing host name");
        }
    }

    public DcmURL(String calledAET, String callingAET, String host, int port) {
        this.calledAET = calledAET;
        this.callingAET = callingAET;
        this.host = host;
        this.port = port;
    }
    
    // Public --------------------------------------------------------
    public final String getCallingAET() {
        return callingAET;
    }
    
    public final String getCalledAET() {
        return calledAET;
    }
    
    public final String getHost() {
        return host;
    }
    
    public final int getPort() {
        return port;
    }
    
    public String toString() {
        return "dicom://" + calledAET + ':' + callingAET
                + '@' + host + ':' + port;
    }

    // Private -------------------------------------------------------
    private void parse(String s) {
        if (!s.startsWith("dicom://")) {
            throw new IllegalArgumentException(
                    "DICOM URL must starts with \"dicom://\"");
        }
        StringTokenizer stk = new StringTokenizer(s.substring(8),":@/", true);
        String tk;
        int state = CALLED_AET;
        boolean tcpPart = false;
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case ':':
                    state = tcpPart ? PORT : CALLING_AET;
                    break;
                case '@':
                    tcpPart = true;
                    state = HOST;
                    break;
                case '/':
                    return;
                default:
                    switch (state) {
                        case CALLED_AET:
                            calledAET = tk;
                            break;
                        case CALLING_AET:
                            callingAET = tk;
                            break;
                        case HOST:
                            host = tk;
                            break;
                        case PORT:
                            port = Integer.parseInt(tk);
                            return;
                        default:
                            throw new RuntimeException();
                    }
                    state = DELIMITER;
                    break;
            }
        }
    }
}
