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

package org.dcm4chee.web.war;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.security.auth.Subject;

import org.apache.wicket.RequestCycle;
import org.dcm4chee.web.common.base.BaseWicketApplication;
import org.dcm4chee.web.war.common.model.AbstractDicomModel;
import org.dcm4chee.web.war.config.delegate.WebCfgDelegate;
import org.dcm4chee.web.war.folder.model.StudyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <franz.willer@gmail.com>
 * @version $Revision$ $Date$
 * @since Dec 20, 2010
 */
public class StudyPermissionHelper implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private List<String> dicomRoles;
    private boolean useStudyPermissions;
    private boolean webStudyPermissions;
    private StudyPermissionRight studyPermissionRight;
    
    protected static Logger log = LoggerFactory.getLogger(StudyPermissionHelper.class);

    public static StudyPermissionHelper get() {
        return ((AuthenticatedWebSession) AuthenticatedWebSession.get()).getStudyPermissionHelper();
    }

    public StudyPermissionHelper(String username, String password, org.apache.wicket.security.hive.authentication.Subject webSubject) throws AttributeNotFoundException, InstanceNotFoundException, MalformedObjectNameException, MBeanException, ReflectionException, NullPointerException, IOException {

        studyPermissionRight = StudyPermissionRight.NONE;
        useStudyPermissions = WebCfgDelegate.getInstance().getUseStudyPermissions();
        webStudyPermissions = WebCfgDelegate.getInstance().getWebStudyPermissions();

        try {
            javax.security.auth.Subject dicomSubject = new javax.security.auth.Subject();
            WebCfgDelegate.getInstance().getMBeanServer().invoke(
                    new ObjectName(((BaseWicketApplication) RequestCycle.get().getApplication()).getInitParameter("DicomSecurityService")), 
                    "isValid",
                    new Object[] { username, password, dicomSubject },
                    new String[] { String.class.getName(), 
                            String.class.getName(), 
                            javax.security.auth.Subject.class.getName()}
            );
            setDicomSubject(dicomSubject);
            setStudyPermissionRight(webSubject);
        } catch (Exception e) {
            log.error("Error creating StudyPermissionHelper: revoked rights: ", e);
        }
    }

    private void setDicomSubject(Subject dicomSubject) {
        if (!useStudyPermissions && !webStudyPermissions) {
            dicomRoles = null;
        } else {
            dicomRoles = new ArrayList<String>();
            Principal rolesPrincipal;
            for (Iterator<Principal> i = dicomSubject.getPrincipals().iterator() ; i.hasNext() ; ) {
                rolesPrincipal = i.next();
                if (rolesPrincipal instanceof Group) {
                    Enumeration<? extends Principal> e = ((Group) rolesPrincipal).members();
                    while (e.hasMoreElements()) 
                        dicomRoles.add(e.nextElement().getName());
                }
            }
        }
    }

    public List<String> getDicomRoles() {
        return dicomRoles;
    }
    
    public boolean useStudyPermissions() {
        return useStudyPermissions;
    }

    public void setWebStudyPermissions(boolean webStudyPermissions) {
        this.webStudyPermissions = webStudyPermissions;
    }

    public boolean isWebStudyPermissions() {
        return webStudyPermissions;
    }

    public void setStudyPermissionRight(StudyPermissionRight studyPermissionRight) {
        this.studyPermissionRight = studyPermissionRight;
    }

    public StudyPermissionRight getStudyPermissionRight() {
        return studyPermissionRight;
    }
    
    public boolean checkPermission(Set<? extends AbstractDicomModel> c, String action) {
        if (!isWebStudyPermissions()
        || (dicomRoles == null))
            return true;
        if (dicomRoles.size() == 0)
            return false;
        if (c == null || c.isEmpty())
            return true;
        for (AbstractDicomModel m : c) {
            if (!checkStudyPermission(m, action)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean checkPermission(AbstractDicomModel m, String action) {
        if ((!isWebStudyPermissions())
        || (dicomRoles == null))
            return true;
        if (dicomRoles.size() == 0)
            return false;
        return checkStudyPermission(m, action);
    }
    
    private boolean checkStudyPermission(AbstractDicomModel m, String action) {
        if (m.levelOfModel() == AbstractDicomModel.PATIENT_LEVEL)
            return true;
        while (m.levelOfModel() > AbstractDicomModel.STUDY_LEVEL)
            m = m.getParent();
        return ((StudyModel) m).getStudyPermissionActions().contains(action);
    }

    
    private void setStudyPermissionRight(org.apache.wicket.security.hive.authentication.Subject webSubject) {
        studyPermissionRight = StudyPermissionRight.NONE;
        String studyPermissionsAll = WebCfgDelegate.getInstance().getStudyPermissionsAllRolename();
        String studyPermissionsOwn = WebCfgDelegate.getInstance().getStudyPermissionsOwnRolename();
        if (studyPermissionsAll != null || studyPermissionsOwn != null) {
            Iterator<org.apache.wicket.security.hive.authorization.Principal> i = webSubject.getPrincipals().iterator();
            while (i.hasNext()) {
                String rolename = i.next().getName();    
                if (rolename.equals(studyPermissionsAll)) {
                    studyPermissionRight = StudyPermissionRight.ALL;
                    break;
                } else if (rolename.equals(studyPermissionsOwn))
                    studyPermissionRight = StudyPermissionRight.OWN;
            }
        }
    }
    
    public enum StudyPermissionRight {
        NONE, OWN, ALL 
    }
}
