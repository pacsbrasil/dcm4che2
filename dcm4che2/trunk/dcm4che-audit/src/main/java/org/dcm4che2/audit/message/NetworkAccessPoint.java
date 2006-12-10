/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
 
package org.dcm4che2.audit.message;

/**
 * The network access point identifies the logical network location for
 * application activity.
 *  
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 17, 2006
 */
public class NetworkAccessPoint {
    public static final String HOST_NAME = "1";
    public static final String IP_ADDRESS = "2";
    public static final String TEL_NR = "3";
    
    protected final String id;
    protected final String typeCode;

    protected NetworkAccessPoint(String id, String typeCode) {
        this.id = id;
        this.typeCode = typeCode;
    }
    
    public static class HostName extends NetworkAccessPoint {

        public HostName(String host) {
            super(host, HOST_NAME);
        }
        
        public String getNodeID() {
            int domainNameStart = id.indexOf('.') + 1;
            return domainNameStart == 0 ? id
                    : (id.substring(0, domainNameStart-1) + '@' 
                            + id.substring(domainNameStart));
        }        
    }
    
    public static class IPAddress extends NetworkAccessPoint {

        public IPAddress(String ip) {
            super(ip, IP_ADDRESS);
        }
    }
    
    public static class TelephoneNumber extends NetworkAccessPoint {

        public TelephoneNumber(String telno) {
            super(telno, TEL_NR);
        }
    }

    public final String getID() {
        return id;
    }

    public final String getTypeCode() {
        return typeCode;
    }
    
    public String getNodeID() {
        return id;
    }
}
