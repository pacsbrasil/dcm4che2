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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.service.impl;

import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.security.auth.Subject;

import org.dcm4che.archive.common.SecurityUtils;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.StudyPermissionDAO;
import org.dcm4che.archive.entity.StudyPermission;
import org.dcm4che.archive.service.StudyPermissionManagerLocal;
import org.dcm4che.archive.service.StudyPermissionManagerRemote;

/**
 * org.dcm4che.archive.service.impl.StudyPermissionManagerBean
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class StudyPermissionManagerBean implements StudyPermissionManagerLocal,
        StudyPermissionManagerRemote {

    @EJB
    private StudyPermissionDAO permissionDAO;

    /**
     * Constructor
     */
    public StudyPermissionManagerBean() {
    }

    public Collection findByPatientPk(Long pk) {
        return permissionDAO.findByPatientPk(pk);
    }

    public Collection findByStudyIuid(String suid) {
        return permissionDAO.findByStudyIuid(suid);
    }

    public Collection findByStudyIuidAndAction(String suid, String action) {
        return permissionDAO.findByStudyIuidAndAction(suid, action);
    }

    public boolean hasPermission(String suid, String action, String role) {
        try {
            permissionDAO.find(suid, action, role);
            return true;
        }
        catch (NoResultException nre) {
            return false;
        }
    }

    public boolean hasPermission(String suid, String action, Subject subject) {
        String[] roles = SecurityUtils.rolesOf(subject);
        for (int i = 0; i < roles.length; i++) {
            if (hasPermission(suid, action, roles[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.dcm4che.archive.service.StudyPermissionManager#grant(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public boolean grant(String suid, String action, String role) {
        if (hasPermission(suid, action, role)) {
            return false;
        }
        try {
            permissionDAO.create(suid, action, role);
            return true;
        }
        catch (ContentCreateException e) {
            if (hasPermission(suid, action, role)) {
                return false;
            }
            throw e;
        }
    }

    public int grant(String suid, String[] actions, String role) {
        int count = 0;
        for (int i = 0; i < actions.length; i++) {
            if (grant(suid, actions[i], role)) {
                ++count;
            }
        }
        return count;
    }

    public boolean revoke(StudyPermission dto) {
        if (dto.getPk() != null || dto.getPk() != -1) {
            permissionDAO.remove(dto);
            return true;
        }
        else {
            return revoke(dto.getStudyIuid(), dto.getAction(), dto.getRole());
        }
    }

    public boolean revoke(String suid, String action, String role) {
        StudyPermission studyPermission;
        try {
            studyPermission = permissionDAO.find(suid, action, role);
        }
        catch (NoResultException nre) {
            return false;
        }
        permissionDAO.remove(studyPermission);
        return true;
    }

    public int revoke(String suid, String[] actions, String role) {
        int count = 0;
        for (int i = 0; i < actions.length; i++) {
            if (revoke(suid, actions[i], role)) {
                ++count;
            }
        }
        return count;
    }

    public int grantForPatient(long patPk, String[] actions, String role) {
        Collection<String> c = permissionDAO
                .selectStudyIuidsByPatientPk(new Long(patPk));
        return grant(c, actions, role);
    }

    public int revokeForPatient(long patPk, String[] actions, String role) {
        Collection<String> c = permissionDAO
                .selectStudyIuidsByPatientPk(new Long(patPk));
        return revoke(c, actions, role);
    }

    public int grantForPatient(String pid, String issuer, String[] actions,
            String role) {
        Collection<String> c = permissionDAO.selectStudyIuidsByPatientId(pid,
                issuer);
        return grant(c, actions, role);
    }

    private int grant(Collection<String> suids, String[] actions, String role) {
        int count = 0;
        for (String uid : suids) {
            count += grant(uid, actions, role);
        }
        return count;
    }

    public int revokeForPatient(String pid, String issuer, String[] actions,
            String role) {
        Collection c = permissionDAO.selectStudyIuidsByPatientId(pid, issuer);
        return revoke(c, actions, role);
    }

    private int revoke(Collection<String> suids, String[] actions, String role) {
        int count = 0;
        for (String uid : suids) {
            count += revoke(uid, actions, role);
        }
        return count;
    }

    public int countStudiesOfPatient(Long patPk) {
        return permissionDAO.selectStudyIuidsByPatientPk(patPk).size();
    }

}
