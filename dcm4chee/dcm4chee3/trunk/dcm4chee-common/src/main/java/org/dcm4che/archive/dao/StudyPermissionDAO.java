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

package org.dcm4che.archive.dao;

import java.util.Collection;

import javax.ejb.Local;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.StudyPermission;

/**
 * org.dcm4che.archive.dao.StudyPermissionDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface StudyPermissionDAO extends DAO<StudyPermission> {
    public static final String JNDI_NAME = "dcm4cheeArchive/StudyPermissionDAOImpl/local";

    /**
     * Create a {@link StudyPermission} record in the database.
     * 
     * @param suid
     *            The study UID.
     * @param action
     *            The action pertaining to the permission
     * @param role
     *            The role pertaining to the permission
     * @return The persistent {@link StudyPermission} object
     * @throws ContentCreateException
     */
    public StudyPermission create(String suid, String action, String role)
            throws ContentCreateException;

    /**
     * Find a {@link StudyPermission} record.
     * 
     * @param suid
     *            The study UID.
     * @param action
     *            The action that the permission is for.
     * @param role
     *            The role of the user/group the permission affects.
     * @return {@link StudyPermission}
     * @throws PersistenceException
     */
    public StudyPermission find(String suid, String action, String role)
            throws PersistenceException;

    /**
     * Find {@link StudyPermission} records for a study and specific action.
     * 
     * @param suid
     *            The study UID.
     * @param action
     *            The action that the permissions are for.
     * @return {@link Collection} of {@link StudyPermission} objects.
     * @throws PersistenceException
     */
    public Collection<StudyPermission> findByStudyIuidAndAction(String suid,
            String action) throws PersistenceException;

    /**
     * Find {@link StudyPermission} records for a study.
     * 
     * @param suid
     *            The study UID.
     * @return {@link Collection} of {@link StudyPermission} objects.
     * @throws PersistenceException
     */
    public Collection<StudyPermission> findByStudyIuid(String suid)
            throws PersistenceException;

    /**
     * Find {@link StudyPermission} objects from the database that are related
     * to the studies for a particular patient.
     * 
     * @param patientPk
     *            The primary key of the patient owning studies with
     *            permissions.
     * @return {@link Collection} of {@link StudyPermission} objects
     * @throws PersistenceException
     */
    public Collection<StudyPermission> findByPatientPk(Long patientPk)
            throws PersistenceException;

    public Collection<String> selectStudyIuidsByPatientPk(Long patientPk)
            throws PersistenceException;

    public Collection<String> selectStudyIuidsByPatientId(String pid)
            throws PersistenceException;

    public Collection<String> selectStudyIuidsByPatientId(String pid,
            String issuer) throws PersistenceException;
}
