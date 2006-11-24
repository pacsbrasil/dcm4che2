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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Identifies a user for the purpose of documenting accountability for the
 * audited event. A user may be a person, or a hardware device or software
 * process for events that are not initiated by a person.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 17, 2006
 */
public class ActiveParticipant extends BaseElement {

    private ArrayList roleIDCodes = new ArrayList(1);
    
    public ActiveParticipant(String userId) {
        super("ActiveParticipant");
        addAttribute("UserID", userId);
    }
    
    public String getUserID() {
        return (String) getAttribute("UserID");
    }
    
    public String getAlternativeUserID() {
        return (String) getAttribute("AlternativeUserID");
    }
    
    public ActiveParticipant setAlternativeUserID(String id) {
        addAttribute("AlternativeUserID", id);
        return this;
    }
    
    public List getAETitles() {
        String altID = getAlternativeUserID();
        if (altID != null && altID.startsWith("AETITLES=")) {
            List aets = new ArrayList();
            int left = 9;
            int right;
            while ((right = altID.indexOf(';', left)) != -1) {
                if (right > left) {
                    aets.add(altID.substring(left, right));
                }
                left = right + 1;
            }
            if (left < altID.length()) {
                aets.add(altID.substring(left));
            }
            return aets;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public ActiveParticipant setAETitle(String aet) {
        if (aet.length() == 0) {
            throw new IllegalArgumentException("empty value");
        }
        setAlternativeUserID("AETITLES=" + aet);
        return this;
    }

    public ActiveParticipant setAETitles(List aets) {
        if (aets.isEmpty()) {
            throw new IllegalArgumentException("empty value");
        }
        StringBuffer sb = new StringBuffer("AETITLES=");
        for (Iterator iter = aets.iterator(); iter.hasNext();) {
            String aet = (String) iter.next();
            if (aet.length() == 0) {
                throw new IllegalArgumentException("empty value");
            }
            sb.append(aet).append(';');
        }
        setAlternativeUserID(sb.substring(0,sb.length()-1));
        return this;            
    }
    
    public String getUserName() {
        return (String) getAttribute("UserName");
    }
    
    public ActiveParticipant setUserName(String name) {
        addAttribute("UserName", name);
        return this;
    }
    
    public boolean isUserIsRequestor() {
        Boolean requestor = (Boolean) getAttribute("UserIsRequestor");
        return requestor == null || requestor.booleanValue();
    }
    
    public ActiveParticipant setUserIsRequestor(boolean requestor) {
        addAttribute("UserIsRequestor", Boolean.valueOf(requestor));
        return this;
    }
    
    public String getNetworkAccessPointID() {
        return (String) getAttribute("NetworkAccessPointID");
    }

    public String getNetworkAccessPointTypeCode() {
        return (String) getAttribute("NetworkAccessPointTypeCode");
    }

    public ActiveParticipant setNetworkAccessPoint(NetworkAccessPoint nap) {
        addAttribute("NetworkAccessPointID", nap.getID());
        addAttribute("NetworkAccessPointTypeCode", nap.getTypeCode());
        return this;
    }
    
    public List getRoleIDCodeIDs() {
        return Collections.unmodifiableList(roleIDCodes);
    }
    
    public ActiveParticipant addRoleIDCode(RoleIDCode code) {
        if (code == null) {
            throw new NullPointerException();
        }
        roleIDCodes.add(code);
        return this;
    }
    
    protected boolean isEmpty() {
        return roleIDCodes.isEmpty();
    }
        
    protected void outputContent(Writer out) throws IOException {
        outputChilds(out, roleIDCodes);
    }

    public static class RoleIDCode extends CodeElement {
        
        public static final RoleIDCode APPLICATION = 
                new RoleIDCode("110150","DCM","Application");
        public static final RoleIDCode APPLICATION_LAUNCHER = 
                new RoleIDCode("110151","DCM","Application Launcher");
        public static final RoleIDCode DESTINATION = 
                new RoleIDCode("110152","DCM","Destination");
        public static final RoleIDCode SOURCE = 
                new RoleIDCode("110153","DCM","Source");
        public static final RoleIDCode DESTINATION_MEDIA = 
                new RoleIDCode("110154","DCM","Destination Media");
        public static final RoleIDCode SOURCE_MEDIA = 
                new RoleIDCode("110155","DCM","Source Media");


        public RoleIDCode(String code) {
            super("RoleIDCode", code);
        }

        public RoleIDCode(String code, String codeSystemName, 
                String displayName) {
            super("RoleIDCode", code, codeSystemName, displayName);
        }
    }
    
}
