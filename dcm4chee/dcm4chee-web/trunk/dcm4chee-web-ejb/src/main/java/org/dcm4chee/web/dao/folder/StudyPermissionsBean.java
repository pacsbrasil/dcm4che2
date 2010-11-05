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

package org.dcm4chee.web.dao.folder;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dcm4chee.archive.entity.StudyPermission;
import org.dcm4chee.usr.entity.Role;
import org.jboss.annotation.ejb.LocalBinding;

/**
 * @author Robert David <robert.david@agfa.com>
 * @version $Revision$ $Date$
 * @since 05.10.2010
 */
@Stateless
@LocalBinding (jndiBinding=StudyPermissionsLocal.JNDI_NAME)
public class StudyPermissionsBean implements StudyPermissionsLocal {

    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;
    @PersistenceContext(unitName="dcm4chee-usr")
    private EntityManager emUsr;

    @SuppressWarnings("unchecked")
    public List<StudyPermission> getStudyPermissions(String studyInstanceUID) {
        return (List<StudyPermission>) em.createQuery("SELECT sp FROM StudyPermission sp WHERE sp.studyInstanceUID = :studyInstanceUID")
        .setParameter("studyInstanceUID", studyInstanceUID)
        .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<StudyPermission> getStudyPermissionsForPatient(long pk) {
        return (List<StudyPermission>) em.createQuery("SELECT sp FROM StudyPermission sp, Study s WHERE sp.studyInstanceUID = s.studyInstanceUID AND s.patientFk = :pk ORDER BY sp.role, sp.action")
        .setParameter("pk", pk)
        .getResultList();
    }

    public void grant(StudyPermission studyPermission) {
        em.persist(studyPermission);        
    }

    public void revoke(long pk) {
        this.em.createQuery("DELETE FROM StudyPermission sp WHERE sp.pk = :pk")
        .setParameter("pk", pk)
        .executeUpdate();
    }
    
    // TODO: change this to generic version using JPA 2.0 implementation
    @SuppressWarnings("unchecked")
    public void grantForPatient(long pk, String action, String role) {
        for (String studyInstanceUID : (List<String>) 
                em.createQuery("SELECT s.studyInstanceUID FROM Study s WHERE s.patientFk = :pk AND s.studyInstanceUID NOT IN(SELECT sp.studyInstanceUID FROM StudyPermission sp WHERE sp.action = :action AND sp.role = :role)")
                    .setParameter("pk", pk)
                    .setParameter("action", action)
                    .setParameter("role", role)
                    .getResultList()) {
            StudyPermission sp = new StudyPermission();
            sp.setAction(action);
            sp.setRole(role);
            sp.setStudyInstanceUID(studyInstanceUID);
            em.persist(sp);
        }
    }
    
    public void revokeForPatient(long pk, String action, String role) {
        this.em.createQuery("DELETE FROM StudyPermission sp WHERE sp.studyInstanceUID IN(SELECT s.studyInstanceUID FROM Study s WHERE s.patientFk = :pk) AND sp.action = :action AND sp.role = :role")
        .setParameter("pk", pk)
        .setParameter("action", action)
        .setParameter("role", role)
        .executeUpdate();
    }
    
    public long countStudiesOfPatient(long pk) {
        return (Long) 
        em.createQuery("SELECT COUNT(s) FROM Patient p, IN(p.studies) s WHERE p.pk = :pk")
        .setParameter("pk", pk)
        .getSingleResult();
    }
    
    // TODO: change this to the generic version using JPA2.0 implementation
    @SuppressWarnings("unchecked")
    public void updateDicomRoles() {
        List<String> roleList = emUsr.createQuery("SELECT DISTINCT r.rolename FROM Role r").getResultList();
        List<String> newRoles = 
            (roleList.size() == 0) ? em.createQuery("SELECT DISTINCT sp.role FROM StudyPermission sp")
                                        .getResultList()
                                   : em.createQuery("SELECT DISTINCT sp.role FROM StudyPermission sp WHERE sp.role NOT IN (:roles)")
                                        .setParameter("roles", roleList)
                                        .getResultList();
        for (String rolename : newRoles) 
            emUsr.persist(new Role(rolename, "StudyPermissions"));
// TODO: put the role type into the config service
    }
}
