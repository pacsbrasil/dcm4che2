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

package org.dcm4chex.archive.ejb.interfaces;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since July 23, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class AEData implements Serializable {
    
    // Constants -----------------------------------------------------
    static final long serialVersionUID = 9128665077590256461L;
    static final String[] EMPTY_STRING_ARRAY = {};
    
    // Variables -----------------------------------------------------
    private final String title;
    private final String host;
    private final int port;
    private final String cipherSuites;
    
    // Constructors --------------------------------------------------
    public AEData(String title, String host, int port, String cipherSuites) {
        this.title = title;
        this.host = host;
        this.port = port;
        this.cipherSuites = cipherSuites;
    }
            
    /** Getter for property title.
     * @return Value of property title.
     */
    public java.lang.String getTitle() {
        return title;
    }
    
    /** Getter for property host.
     * @return Value of property host.
     */
    public java.lang.String getHost() {
        return host;
    }
    
    /** Getter for property port.
     * @return Value of property port.
     */
    public int getPort() {
        return port;
    }
    
    /** Getter for property cipherSuites.
     * @return Value of property cipherSuites.
     */
    public java.lang.String[] getCipherSuites() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        StringTokenizer stk = new StringTokenizer(cipherSuites, " ,");
        String[] retval = new String[stk.countTokens()];
        for (int i = 0; i < retval.length; ++i) {
            retval[i] = stk.nextToken();
        }
        return retval;
    }
        
    // Methods -------------------------------------------------------
    private String prefix() {
        if (cipherSuites == null || cipherSuites.length() == 0) {
            return "dicom://";
        }
        if ("SSL_RSA_WITH_NULL_SHA".equals(cipherSuites)) {
            return "dicom-tls.nodes://";
        }
        if ("SSL_RSA_WITH_3DES_EDE_CBC_SHA".equals(cipherSuites)) {
            return "dicom-tls.3des://";
        }
        return "dicom-tls://";
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(prefix())
          .append(getTitle()).append('@')
          .append(getHost()).append(':').append(getPort());
        return sb.toString();
    }
}
