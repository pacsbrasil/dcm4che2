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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2003-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 * Franz Willer <franz.willer@gwi-ag.com>
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
import java.util.Iterator;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.common.SPSStatus;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.OtherPatientIDDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.MWLItem;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.archive.service.PatientUpdateLocal;
import org.dcm4che.archive.service.PatientUpdateRemote;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 */
// EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class PatientUpdateBean implements PatientUpdateLocal,
        PatientUpdateRemote {

    @EJB
    private PatientDAO patDAO;
    
    @EJB
    private OtherPatientIDDAO opidDAO;

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#mergePatient(org.dcm4che.data.Dataset,
     *      org.dcm4che.data.Dataset)
     */
    public void mergePatient(Dataset dominant, Dataset prior)
            throws ContentCreateException, PersistenceException,
            PatientException {
        Patient dominantPat = updateOrCreate(dominant);
        Patient priorPat = updateOrCreate(prior);
        dominantPat.getStudies().addAll(priorPat.getStudies());
        dominantPat.getMpps().addAll(priorPat.getMpps());
        dominantPat.getMwlItems().addAll(priorPat.getMwlItems());
        dominantPat.getGpsps().addAll(priorPat.getGpsps());
        dominantPat.getGppps().addAll(priorPat.getGppps());
        dominantPat.getMerged().addAll(priorPat.getMerged());
        priorPat.setMergedWith(dominantPat);
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#updatePatient(org.dcm4che.archive.entity.Study,
     *      org.dcm4che.data.Dataset)
     */
    public void updatePatient(Study study, Dataset attrs)
            throws PersistenceException, ContentCreateException,
            PatientException {
        String pid = attrs.getString(Tags.PatientID);

        // If the patient id is not included, then we don't have to do any
        // patient update. Although patient id is type 2 in DICOM, but for DC,
        // we enforce this.
        if (pid == null || pid.length() == 0)
            return;

        Patient newPatient = updateOrCreate(attrs);

        // Case 1: it's matching the same patient. Do nothing
        if (study.getPatient().getPatientId().equals(pid))
            return;

        // Case 2: there's no matching, a new patient is created. The study is
        // updated.
        // Case 3: it's matching another existing patient. The study is updated.
        study.setPatient(newPatient);
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#updatePatient(org.dcm4che.data.Dataset)
     */
    public void updatePatient(Dataset attrs) throws ContentCreateException,
            PersistenceException, PatientException {
        updateOrCreate(attrs);
    }

    private Patient updateOrCreate(Dataset ds) throws ContentCreateException,
            PersistenceException, NonUniquePatientException,
            PatientMergedException, CircularMergedException {
        try {
            Patient pat = patDAO.searchFor(ds, false);
            pat.updateAttributes(ds, opidDAO);
            return pat;
        }
        catch (NoResultException e) {
            return patDAO.create(ds);
        }
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#deletePatient(org.dcm4che.data.Dataset)
     */
    public boolean deletePatient(Dataset ds) throws ContentDeleteException,
            PersistenceException, PatientException {
        try {
            patDAO.remove(patDAO.searchFor(ds, false));
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#patientArrived(org.dcm4che.data.Dataset)
     */
    public void patientArrived(Dataset ds) throws PersistenceException,
            PatientException {
        try {
            Patient pat = patDAO.searchFor(ds, false);
            Collection c = pat.getMwlItems();
            for (Iterator iter = c.iterator(); iter.hasNext();) {
                MWLItem mwlitem = (MWLItem) iter.next();
                if (mwlitem.getSpsStatusAsInt() == SPSStatus.SCHEDULED)
                    mwlitem.updateSpsStatus(SPSStatus.ARRIVED);
            }
        }
        catch (NoResultException e) {
        }
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#updateOtherPatientIDsOrCreate(org.dcm4che.data.Dataset)
     */
    public void updateOtherPatientIDsOrCreate(Dataset ds)
            throws PersistenceException, ContentCreateException,
            PatientException {
        try {
            patDAO.updateOtherPatientIDs(patDAO.searchFor(ds, false), ds);
        }
        catch (NoResultException e) {
            patDAO.create(ds);
        }
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /**
     * @see org.dcm4che.archive.service.PatientUpdate#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patHome) {
        this.patDAO = patHome;
    }

    /**
     * @return the opidDAO
     */
    public OtherPatientIDDAO getOpidDAO() {
        return opidDAO;
    }

    /**
     * @param opidDAO the opidDAO to set
     */
    public void setOpidDAO(OtherPatientIDDAO opidDAO) {
        this.opidDAO = opidDAO;
    }
}