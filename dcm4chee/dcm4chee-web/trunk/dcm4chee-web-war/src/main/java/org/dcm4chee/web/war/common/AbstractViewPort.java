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

package org.dcm4chee.web.war.common;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.WebApplication;
import org.dcm4chee.usr.dao.UserAccess;
import org.dcm4chee.usr.model.AETGroup;
import org.dcm4chee.usr.ui.config.delegate.UsrCfgDelegate;
import org.dcm4chee.usr.util.JNDIUtils;
import org.dcm4chee.web.common.secure.SecureSession;
import org.jfree.util.Log;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since Apr. 26, 2011
 */
public abstract class AbstractViewPort implements Serializable {

    private static final long serialVersionUID = 1L;

    int offset = 0;
    int total = 0;

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void clear() {
        offset = total = 0;
    }
    
    public List<String> getAetChoices() {
        List<AETGroup> aetGroups = ((UserAccess) JNDIUtils.lookup(UserAccess.JNDI_NAME))
            .getAETGroups(getAssignedRoles());
        List<String> groupChoices = new ArrayList<String>();
        Set<String> aetChoices = new HashSet<String>();
        for (AETGroup aetGroup : aetGroups) {
            if (aetGroup.getGroupname().equals("*")) {
                groupChoices.add(aetGroup.getGroupname());
                aetChoices.addAll(UsrCfgDelegate.getInstance().getSourceAETList());
                aetChoices.addAll(UsrCfgDelegate.getInstance().getStationAETList());
                continue;
            }
            groupChoices.add("(" + aetGroup.getGroupname() + ")");
            aetChoices.addAll(aetGroup.getAets());
        }
        groupChoices.addAll(aetChoices);
        return groupChoices;
    }
    
    private List<String> getAssignedRoles() {
        List<String> roles = new ArrayList<String>();
        String rolesGroupName = ((WebApplication) RequestCycle.get().getApplication()).getInitParameter("rolesGroupName");
        if (rolesGroupName == null) rolesGroupName = "Roles";
        try {
            for (Principal principal : ((Subject) PolicyContext.getContext("javax.security.auth.Subject.container")).getPrincipals()) {
                if ((principal instanceof Group) && rolesGroupName.equalsIgnoreCase(principal.getName())) {
                    Enumeration<? extends Principal> members = ((Group) principal).members();
                    while (members.hasMoreElements()) 
                        roles.add(members.nextElement().getName());
                }
            }
        } catch (Exception e) {
            Log.error("Failed to get jaas subject from javax.security.auth.Subject.container", e);
        }
        return roles;
    }
}
