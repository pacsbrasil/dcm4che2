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
package org.dcm4che.archive.dao.jpa;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.OtherPatientIDDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.OtherPatientID;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;

@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PatientDAOImpl extends BaseDAOImpl<Patient> implements PatientDAO {
    @EJB
    private OtherPatientIDDAO opidDAO;

    @EJB
    private StudyDAO studyDAO;

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return Patient.class;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#create(org.dcm4che.data.Dataset)
     */
    public Patient create(Dataset ds) throws ContentCreateException {
        Patient patient = new Patient();
        patient.setAttributes(ds);
        createOtherPatientIds(patient, ds.get(Tags.OtherPatientIDSeq));
        if (logger.isInfoEnabled()) {
            logger.info("Created " + patient);
        }
        return patient;
    }

    private void createOtherPatientIds(Patient patient, DcmElement opidsq)
            throws ContentCreateException {
        if (opidsq == null || opidsq.isEmpty() || opidsq.getItem().isEmpty()) {
            return;
        }
        Collection opids = patient.getOtherPatientIds();
        for (int i = 0, n = opidsq.countItems(); i < n; i++) {
            Dataset opid = opidsq.getItem(i);
            opids.add(opidDAO.valueOf(opid.getString(Tags.PatientID), opid
                    .getString(Tags.IssuerOfPatientID)));
        }
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findByPatientId(java.lang.String)
     */
    public List<Patient> findByPatientId(String pid)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up patient with id=" + pid);
        }

        Query q = em
                .createQuery("from org.dcm4che.archive.entity.Patient p where p.patientId = :pid");
        q.setParameter("pid", pid);
        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findByPatientIdWithIssuer(java.lang.String,
     *      java.lang.String)
     */
    public List<Patient> findByPatientIdWithIssuer(String pid, String issuer)
            throws PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up patient with id=" + pid + " and issuer="
                    + issuer);
        }

        Query q = em
                .createQuery("from org.dcm4che.archive.entity.Patient p where p.patientId = :pid and (p.issuerOfPatientId is null or p.issuerOfPatientId = :issuer)");
        q.setParameter("pid", pid);
        q.setParameter("issuer", issuer);
        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findByPatientIdWithExactIssuer(java.lang.String,
     *      java.lang.String)
     */
    public List<Patient> findByPatientIdWithExactIssuer(String pid,
            String issuer) {
        if (logger.isDebugEnabled()) {
            logger.debug("Looking up patient with id=" + pid
                    + " and exact issuer=" + issuer);
        }

        Query q = em
                .createQuery("from org.dcm4che.archive.entity.Patient p where p.patientId = :pid and p.issuerOfPatientId = :issuer");
        q.setParameter("pid", pid);
        q.setParameter("issuer", issuer);
        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#searchFor(org.dcm4che.data.Dataset,
     *      boolean)
     */
    public Patient searchFor(Dataset ds, boolean followMergedWith)
            throws PersistenceException, NonUniquePatientException,
            PatientMergedException, CircularMergedException {
        String pid = ds.getString(Tags.PatientID);
        String issuer = ds.getString(Tags.IssuerOfPatientID);
        Collection c = issuer == null ? findByPatientId(pid)
                : findByPatientIdWithIssuer(pid, issuer);

        if (c.isEmpty()) {
            throw new NoResultException();
        }
        if (c.size() > 1) {
            throw new NonUniquePatientException("Patient ID[id=" + pid
                    + ",issuer=" + issuer + " ambiguous");
        }
        Patient pat = (Patient) c.iterator().next();
        Patient merged = pat.getMergedWith();
        if (merged == null) {
            return pat;
        }
        if (!followMergedWith) {
            String prompt = "Patient ID[id=" + pat.getPatientId() + ",issuer="
                    + pat.getIssuerOfPatientId()
                    + "] merged with Patient ID[id=" + merged.getPatientId()
                    + ",issuer=" + merged.getIssuerOfPatientId() + "]";
            logger.warn(prompt);
            throw new PatientMergedException(prompt);
        }
        Patient result = pat;
        while ((merged = result.getMergedWith()) != null) {
            if (merged.equals(pat)) {
                String prompt = "Detect circular merged Patient "
                        + pat.toString();
                logger.warn(prompt);
                throw new CircularMergedException(prompt);
            }
            result = merged;
        }
        return result;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#updateOtherPatientIDs(org.dcm4che.archive.entity.Patient,
     *      org.dcm4che.data.Dataset)
     */
    public boolean updateOtherPatientIDs(Patient pat, Dataset ds)
            throws ContentDeleteException, ContentCreateException {
        Dataset attrs = pat.getAttributes(false);
        DcmElement opidsq = attrs.remove(Tags.OtherPatientIDSeq);
        DcmElement nopidsq = ds.get(Tags.OtherPatientIDSeq);
        boolean update = false;
        if (opidsq != null) {
            for (int n = 0; n < opidsq.countItems(); n++) {
                Dataset opid = opidsq.getItem();
                String pid = opid.getString(Tags.PatientID);
                String issuer = opid.getString(Tags.IssuerOfPatientID);
                if (nopidsq == null
                        || !Patient.containsPID(pid, issuer, nopidsq)) {
                    OtherPatientID otherPatientId = opidDAO
                            .findByPatientIdAndIssuer(pid, issuer);
                    pat.getOtherPatientIds().remove(otherPatientId);
                    if (otherPatientId.getPatients().isEmpty()) {
                        opidDAO.remove(otherPatientId);
                    }
                    update = true;
                    logger.info("Remove Other Patient ID: " + pid + "^^^"
                            + issuer + " from " + toString());
                }
            }
        }

        if (nopidsq != null) {
            for (int n = 0; n < nopidsq.countItems(); n++) {
                Dataset nopid = nopidsq.getItem();
                String pid = nopid.getString(Tags.PatientID);
                String issuer = nopid.getString(Tags.IssuerOfPatientID);
                if (opidsq == null || !Patient.containsPID(pid, issuer, opidsq)) {
                    pat.getOtherPatientIds().add(opidDAO.valueOf(pid, issuer));
                    update = true;
                    logger.info("Add additional Other Patient ID: " + pid
                            + "^^^" + issuer + " to " + toString());
                }
            }
            if (update) {
                opidsq = attrs.putSQ(Tags.OtherPatientIDSeq);
                for (int n = 0; n < nopidsq.countItems(); n++) {
                    opidsq.addItem(nopidsq.getItem());
                }
            }
        }

        if (update) {
            pat.setAttributes(attrs);
        }
        return update;
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#remove(org.dcm4che.archive.entity.EntityBase)
     */
    @Override
    public void remove(Patient pat) throws ContentDeleteException {
        logger.info("Deleting " + pat);
        // Remove OtherPatientIDs only related to this Patient
        for (Iterator iter = pat.getOtherPatientIds().iterator(); iter
                .hasNext();) {
            OtherPatientID opid = (OtherPatientID) iter.next();
            iter.remove();
            if (opid.getPatients().isEmpty()) {
                opidDAO.remove(opid);
            }
        }
        // we have to delete studies explicitly here due to an foreign key
        // constrain error
        // if an mpps key is set in one of the series.
        for (Iterator iter = pat.getStudies().iterator(); iter.hasNext();) {
            Study study = (Study) iter.next();
            iter.remove();
            studyDAO.remove(study);
        }
    }

    /**
     * @return the opidDAO
     */
    public OtherPatientIDDAO getOpidDAO() {
        return opidDAO;
    }

    /**
     * @param opidDAO
     *            the opidDAO to set
     */
    public void setOpidDAO(OtherPatientIDDAO opidDAO) {
        this.opidDAO = opidDAO;
    }

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorresponding(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorresponding(String patientID, String issuer)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorrespondingByOtherPatientID(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorrespondingByOtherPatientID(
            String patientID, String issuer) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorrespondingByOtherPatientIDLike(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorrespondingByOtherPatientIDLike(
            String string, String issuer) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorrespondingByPrimaryPatientID(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorrespondingByPrimaryPatientID(
            String patientID, String issuer) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorrespondingByPrimaryPatientIDLike(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorrespondingByPrimaryPatientIDLike(
            String string, String issuer) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.PatientDAO#findCorrespondingLike(java.lang.String,
     *      java.lang.String)
     */
    public Collection<Patient> findCorrespondingLike(String string,
            String issuer) throws PersistenceException {
        // TODO
        return null;
    }

}
