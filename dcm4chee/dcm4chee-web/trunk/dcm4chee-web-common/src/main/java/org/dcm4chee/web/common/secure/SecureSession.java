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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.common.secure;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.wicket.Request;
import org.apache.wicket.security.WaspApplication;
import org.apache.wicket.security.WaspSession;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 01.09.2010
 */
public class SecureSession extends WaspSession {

    private static final long serialVersionUID = 1L;
    
    private String username;
    private boolean isRoot;
    private HashMap<String, String> swarmPrincipals;
    private List<String> dicomRoles;
    private StudyPermissionRight studyPermissionRight;

    private boolean manageUsers;

    public SecureSession(WaspApplication application, Request request) {
        super(application, request);
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setDicomSubject(Subject dicomSubject) {
        if (dicomSubject == null) {
            dicomRoles = null;
        } else {
            dicomRoles = new ArrayList<String>();
            Iterator<Principal> i = dicomSubject.getPrincipals().iterator();
            @SuppressWarnings("unused")
            String dicomUsername = (i.hasNext() ? i.next().getName() : null);
            Principal rolesPrincipal = (i.hasNext() ? i.next() : null);
            if (rolesPrincipal instanceof Group) {
                Enumeration<? extends Principal> e = ((Group) rolesPrincipal).members();
                while (e.hasMoreElements()) 
                    dicomRoles.add(e.nextElement().getName());
            }
        }
    }

    public void setSwarmPrincipals(HashMap<String, String> principals) {
        this.swarmPrincipals = principals;
    }
    
    public HashMap<String, String> getSwarmPrincipals() {
        return swarmPrincipals;
    }

    public List<String> getDicomRoles() {
        return dicomRoles;
    }
    
    public boolean getUseStudyPermissions() {
        return dicomRoles != null;
    }

    public void setStudyPermissionRight(StudyPermissionRight studyPermissionRight) {
        this.studyPermissionRight = studyPermissionRight;
    }

    public StudyPermissionRight getStudyPermissionRight() {
        return studyPermissionRight;
    }
    
    public enum StudyPermissionRight {
        NONE, OWN, ALL 
    }

    public void setManageUsers(boolean manageUsers) {
        this.manageUsers = manageUsers;
    }

    public boolean getManageUsers() {
        return manageUsers;
    }
}
