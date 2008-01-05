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

package org.dcm4che.archive.dao.jpa;

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.StudyPermissionDAO;
import org.dcm4che.archive.entity.StudyPermission;

/**
 * org.dcm4che.archive.dao.jpa.StudyPermissionDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class StudyPermissionDAOImpl extends BaseDAOImpl<StudyPermission>
        implements StudyPermissionDAO {

    /**
     * 
     */
    public StudyPermissionDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return StudyPermission.class;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#create(java.lang.String,
     *      java.lang.String, java.sql.Timestamp, java.sql.Timestamp,
     *      org.dcm4che.archive.entity.Study)
     */
    public StudyPermission create(String suid, String action, String role)
            throws ContentCreateException {
        StudyPermission sp = new StudyPermission(suid, action, role);
        save(sp);
        if (logger.isInfoEnabled()) {
            logger.info("Created " + sp);
        }
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#find(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public StudyPermission find(String suid, String action, String role)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Find study permission by suid=" + suid + ", action="
                    + action + ", role=" + role);
        }

        Query query = em
                .createQuery("select sp from StudyPermission as sp where sp.studyIuid =:suid and sp.action=:action and sp.role=:role");
        query.setParameter("suid", suid);
        query.setParameter("action", action);
        query.setParameter("role", role);

        StudyPermission permission = (StudyPermission) query.getSingleResult();
        if (permission == null) {
            throw new NoResultException("StudyPermission with suid=" + suid
                    + ", action=" + action + ", role=" + role);
        }

        return permission;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#findByStudyIuid(java.lang.String)
     */
    public Collection<StudyPermission> findByStudyIuid(String suid)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Find study permissions by suid=" + suid);
        }

        Query query = em
                .createQuery("select sp from StudyPermission as sp where sp.studyIuid =:suid");
        query.setParameter("suid", suid);

        List<StudyPermission> permissions = query.getResultList();

        if (permissions == null) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Could not find permissions for study UID "
                                + suid);
            }
        }
        else {
            if (logger.isDebugEnabled())
                logger.debug("Found " + permissions.size() + " results.");
        }

        return permissions;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#findByStudyIuidAndAction(java.lang.String,
     *      java.lang.String)
     */
    public Collection<StudyPermission> findByStudyIuidAndAction(String suid,
            String action) throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Find study permissions by suid=" + suid + ", action="
                    + action);
        }

        Query query = em
                .createQuery("select sp from StudyPermission as sp where sp.studyIuid =:suid and sp.action=:action");
        query.setParameter("suid", suid);
        query.setParameter("action", action);

        List<StudyPermission> permissions = query.getResultList();

        if (permissions == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find permissions for study UID " + suid
                        + ", action=" + action);
            }
        }
        else {
            if (logger.isDebugEnabled())
                logger.debug("Found " + permissions.size() + " results.");
        }

        return permissions;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#findByPatientPk(java.lang.Long)
     */
    public Collection<StudyPermission> findByPatientPk(Long patientPk)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Find study permissions by patientPk=" + patientPk);
        }

        String sql = "select sp from StudyPermission sp, Patient p join p.studies where p.pk = :patientPk and s.studyIuid = sp.:studyIuid";
        Query query = em.createQuery(sql);
        query.setParameter("patientPk", patientPk);

        List<StudyPermission> permissions = query.getResultList();

        if (permissions == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not find permissions for patientPk "
                        + patientPk);
            }
        }
        else {
            if (logger.isDebugEnabled())
                logger.debug("Found " + permissions.size() + " results.");
        }

        return permissions;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyPermissionDAO#ejbHomeSelectStudyIuidsByPatientId(java.lang.String,
     *      java.lang.String)
     */
    public Collection ejbHomeSelectStudyIuidsByPatientId(String pid,
            String issuer) throws PersistenceException {
        return issuer != null && issuer.length() != 0 ? selectStudyIuidsByPatientId(
                pid, issuer)
                : selectStudyIuidsByPatientId(pid);
    }

    public Collection<String> selectStudyIuidsByPatientPk(Long patientPk)
            throws PersistenceException {
        // TODO
        // "SELECT s.studyIuid FROM Patient p, IN(p.studies) s WHERE p.pk = ?1"
        return null;
    }

    public Collection<String> selectStudyIuidsByPatientId(String pid)
            throws PersistenceException {
        // TODO
        // SELECT s.studyIuid FROM Patient p, IN(p.studies) s WHERE p.patientId
        // = ?1
        return null;
    }

    public Collection<String> selectStudyIuidsByPatientId(String pid,
            String issuer) throws PersistenceException {
        // TODO
        // SELECT s.studyIuid FROM Patient p, IN(p.studies) s WHERE p.patientId
        // = ?1 AND (p.issuerOfPatientId IS NULL OR p.issuerOfPatientId = ?2)
        return null;
    }

}
