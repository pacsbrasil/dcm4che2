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
 * Justin Falk <jfalkmu@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.com>
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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.dao.PatientDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface PatientDAO extends DAO<Patient> {
    public static final String JNDI_NAME = "dcm4cheeArchive/PatientDAOImpl/local";

    public List<Patient> findByPatientIdWithIssuer(String pid, String issuer)
            throws PersistenceException;

    public List<Patient> findByPatientId(String pid)
            throws PersistenceException;

    /**
     * @param ds
     * @return
     */
    public Patient create(Dataset ds) throws ContentCreateException;

    /**
     * @param pid
     * @param issuer
     * @return
     */
    public List<Patient> findByPatientIdWithExactIssuer(String pid,
            String issuer);

    public Patient searchFor(Dataset ds, boolean followMergedWith)
            throws NoResultException, NonUniquePatientException,
            PatientMergedException, CircularMergedException,
            PersistenceException;

    /**
     * Update the patient's other patient ids (stored in a separate table).
     * 
     * @param pat
     *            The {@link Patient} object.
     * @param ds
     *            The {@link Dataset} containing the other patient id
     *            information.
     * @return boolean True if a successful update was performed.
     * @throws ContentDeleteException
     * @throws ContentCreateException
     */
    public boolean updateOtherPatientIDs(Patient pat, Dataset ds)
            throws ContentDeleteException, ContentCreateException;

    public OtherPatientIDDAO getOpidDAO();

    /**
     * @param opidDAO
     *            the opidDAO to set
     */
    public void setOpidDAO(OtherPatientIDDAO opidDAO);

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

    /**
     * @param string
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorrespondingByOtherPatientIDLike(
            String pid, String issuer) throws PersistenceException;

    /**
     * @param patientID
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorrespondingByOtherPatientID(
            String pid, String issuer) throws PersistenceException;

    /**
     * @param patientID
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorrespondingByPrimaryPatientID(
            String pid, String issuer) throws PersistenceException;

    /**
     * @param string
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorrespondingByPrimaryPatientIDLike(
            String pid, String issuer) throws PersistenceException;

    /**
     * @param patientID
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorresponding(String pid, String issuer)
            throws PersistenceException;

    /**
     * @param string
     * @param issuer
     * @return
     */
    public Collection<Patient> findCorrespondingLike(String pid,
            String issuer) throws PersistenceException;

    /**
     * @param pnLike
     * @return
     */
    public Collection<Patient> findByPatientName(String pnLike)
            throws PersistenceException;

    /**
     * @param pid
     * @param pnLike
     * @return
     */
    public Collection<Patient> findByPatientIdAndName(String pid, String pnLike)
            throws PersistenceException;

    /**
     * @param pnLike
     * @param ts
     * @return
     */
    public Collection<Patient> findByPatientNameAndBirthDate(String pnLike,
            Timestamp ts) throws PersistenceException;

    /**
     * @param pid
     * @param pnLike
     * @param ts
     * @return
     */
    public Collection<Patient> findByPatientIdAndNameAndBirthDate(String pid,
            String pnLike, Timestamp ts) throws PersistenceException;
}
