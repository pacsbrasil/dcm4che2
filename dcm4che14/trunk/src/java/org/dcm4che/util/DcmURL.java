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
 * @version 1.0.3
 */
public final class DcmURL extends Object {
    // Constants -----------------------------------------------------    
    public static final String ANONYMOUS = "ANONYMOUS";
    public static final int DICOM_PORT = 104;
    
    private static final int DELIMITER = -1;
    private static final int CALLED_AET = 0;
    private static final int CALLING_AET = 1;
    private static final int HOST = 2;
    private static final int PORT = 3;
    private static final int END = 4;
    
    private static final String[] PROTOCOLS = {
        "dicom",
        "dicom-tls",
        "dicom-tls.nodes",
        "dicom-tls.3des"
    };

    private static final String[][] CIPHERSUITES = {
        null,
        new String[] {
            "SSL_RSA_WITH_NULL_SHA", 
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
        },
        new String[] {
            "SSL_RSA_WITH_NULL_SHA", 
        },
        new String[] {
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
        }
    };
    
    // Attributes ----------------------------------------------------
    private String protocol;
    private String[] cipherSuites;
    private String calledAET;
    private String callingAET = ANONYMOUS;
    private String host;
    private int port = DICOM_PORT;
    private boolean tls;

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

    public DcmURL(String protocol, String calledAET, String callingAET,
            String host, int port) {
        this.protocol = protocol.toLowerCase();
        this.cipherSuites = toCipherSuites(protocol);
        this.calledAET = calledAET;
        this.callingAET = callingAET;
        this.host = host;
        this.port = port;
    }
    
    // Public --------------------------------------------------------
    public final String getProtocol() {
        return protocol;
    }
    
    public final String[] getCipherSuites() {
        return cipherSuites;
    }
    
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
        return protocol + "://" + calledAET + ':' + callingAET
                + '@' + host + ':' + port;
    }

    public static String[] toCipherSuites(String protocol) {
        String tmp = protocol.toLowerCase();
        for (int i = 0; i < PROTOCOLS.length; ++i) {
            if (tmp.equals(PROTOCOLS[i])) {
                return i > 0 ? (String[])CIPHERSUITES[i].clone() : null;
            }            
        }
        throw new IllegalArgumentException("Unregonized protocol: " + protocol);
    }
    // Private -------------------------------------------------------
    
    private void parse(String s) {
        int delimPos = s.indexOf("://");
        if (delimPos == -1) {
            throw new IllegalArgumentException(s);
        }
        protocol = s.substring(0, delimPos).toLowerCase();
        cipherSuites = toCipherSuites(protocol);
        StringTokenizer stk = new StringTokenizer(
            s.substring(delimPos+3),":@/", true);
        String tk;
        int state = CALLED_AET;
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case ':':
                    state = state == HOST ? PORT : CALLING_AET;
                    break;
                case '@':
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
