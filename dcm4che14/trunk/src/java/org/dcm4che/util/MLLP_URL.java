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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.3
 */
public final class MLLP_URL extends Object {
    // Constants -----------------------------------------------------        
    private static final String[] PROTOCOLS = {
        "mllp",
        "mllp-tls",
        "mllp-tls.nodes",
        "mllp-tls.3des"
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
    private String host;
    private int port = 0;

    // Constructors --------------------------------------------------
    public MLLP_URL(String spec) {
        parse(spec.trim());
    }

    public MLLP_URL(String protocol, String host, int port) {
        this.protocol = protocol.toLowerCase();
        this.cipherSuites = toCipherSuites(protocol);
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
    
    public final String getHost() {
        return host;
    }
    
    public final int getPort() {
        return port;
    }
    
    public String toString() {
        return protocol + "://" + host + ':' + port;
    }

    public static String[] toCipherSuites(String protocol) {
        String tmp = protocol.toLowerCase();
        for (int i = 0; i < PROTOCOLS.length; ++i) {
            if (tmp.equals(PROTOCOLS[i])) {
                return i > 0 ? (String[])CIPHERSUITES[i].clone() : null;
            }            
        }
        throw new IllegalArgumentException("Unrecognized protocol: " + protocol);
    }
    // Private -------------------------------------------------------
    
    private void parse(String s) {
        int delimPos1 = s.indexOf("://");
        if (delimPos1 == -1) {
            throw new IllegalArgumentException(s);
        }
        int delimPos2 = s.indexOf(':', delimPos1+3);
        if (delimPos2 == -1) {
            throw new IllegalArgumentException(s);
        }
        protocol = s.substring(0, delimPos1).toLowerCase();
        cipherSuites = toCipherSuites(protocol);
        host = s.substring(delimPos1+3, delimPos2);
        port = Integer.parseInt(s.substring(delimPos2+1));
    }
}
