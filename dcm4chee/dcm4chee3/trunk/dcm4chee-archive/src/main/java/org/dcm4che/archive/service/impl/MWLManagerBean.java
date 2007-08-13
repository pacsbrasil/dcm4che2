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

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.CodeDAO;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.MWLItemDAO;
import org.dcm4che.archive.dao.OtherPatientIDDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.MWLItem;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.DuplicateMWLItemException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.archive.exceptions.PatientMismatchException;
import org.dcm4che.archive.service.MWLManagerLocal;
import org.dcm4che.archive.service.MWLManagerRemote;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObject;
import org.dcm4che.dict.Tags;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1.2 $ $Date: 2007/06/23 18:59:01 $
 * @since 10.12.2003
 */
// EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class MWLManagerBean implements MWLManagerLocal, MWLManagerRemote {
    private static final int[] PATIENT_ATTRS = { Tags.PatientName,
            Tags.PatientID, Tags.IssuerOfPatientID, Tags.PatientBirthDate,
            Tags.PatientSex, Tags.OtherPatientIDSeq,
            Tags.PatientMotherBirthName };

    private static Logger log = Logger.getLogger(MWLManagerBean.class);

    @EJB
    private PatientDAO patDAO;

    @EJB
    private MWLItemDAO mwlItemDAO;

    @EJB
    private OtherPatientIDDAO opidDAO;

    /**
     * @see org.dcm4che.archive.service.MWLManager#removeWorklistItem(java.lang.String,
     *      java.lang.String)
     */
    public Dataset removeWorklistItem(String rpid, String spsid)
            throws PersistenceException {
        try {
            MWLItem mwlItem = mwlItemDAO.findByRpIdAndSpsId(rpid, spsid);
            Dataset attrs = toAttributes(mwlItem);
            mwlItemDAO.remove(mwlItem);
            return attrs;
        }
        catch (NoResultException e) {
            return null;
        }
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#removeWorklistItem(org.dcm4che.data.Dataset)
     */
    public Dataset removeWorklistItem(Dataset ds)
            throws PatientMismatchException, PersistenceException,
            ContentDeleteException, Exception {
        try {
            MWLItem mwlItem = getWorklistItem(ds, false);
            Dataset attrs = toAttributes(mwlItem);
            mwlItemDAO.remove(mwlItem);
            return attrs;
        }
        catch (NoResultException e) {
            return null;
        }
    }

    private MWLItem getWorklistItem(Dataset ds, boolean updatePatient)
            throws PersistenceException, PatientMismatchException,
            NonUniquePatientException, PatientMergedException,
            CircularMergedException {
        Dataset sps = ds.getItem(Tags.SPSSeq);
        MWLItem mwlItem = mwlItemDAO.findByRpIdAndSpsId(ds
                .getString(Tags.RequestedProcedureID), sps
                .getString(Tags.SPSID));
        Patient pat = mwlItem.getPatient();
        try {
            if (patDAO.searchFor(ds, false).equals(pat)) {
                if (updatePatient) {
                    pat.updateAttributes(ds.subSet(PATIENT_ATTRS), opidDAO);
                }
                return mwlItem;
            }
        }
        catch (NoResultException onfe) {
        }
        String prompt = "Patient[pid=" + ds.getString(Tags.PatientID)
                + ", issuer=" + ds.getString(Tags.IssuerOfPatientID)
                + "] does not match Patient associated with "
                + mwlItem.toString();
        log.warn(prompt);
        throw new PatientMismatchException(prompt);
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#updateSPSStatus(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public boolean updateSPSStatus(String rpid, String spsid, String status) {
        try {
            MWLItem mwlItem = mwlItemDAO.findByRpIdAndSpsId(rpid, spsid);
            Dataset attrs = mwlItem.getAttributes();
            attrs.getItem(Tags.SPSSeq).putCS(Tags.SPSStatus, status);
            mwlItem.setAttributes(attrs);
            return true;
        }
        catch (NoResultException e) {
            return false;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#updateSPSStatus(org.dcm4che.data.Dataset)
     */
    public boolean updateSPSStatus(Dataset ds) throws PatientMismatchException,
            Exception {
        MWLItem mwlItem;
        try {
            mwlItem = getWorklistItem(ds, true);
        }
        catch (NoResultException e) {
            return false;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
        String status = ds.getItem(Tags.SPSSeq).getString(Tags.SPSStatus);
        Dataset attrs = mwlItem.getAttributes();
        attrs.getItem(Tags.SPSSeq).putCS(Tags.SPSStatus, status);
        mwlItem.setAttributes(attrs);
        return true;
    }

    private Patient updateOrCreatePatient(Dataset ds)
            throws PersistenceException, ContentCreateException,
            NonUniquePatientException, PatientMergedException,
            CircularMergedException {
        try {
            return patDAO.searchFor(ds, false);
        }
        catch (NoResultException onfe) {
            return patDAO.create(ds.subSet(PATIENT_ATTRS));
        }
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#addWorklistItem(org.dcm4che.data.Dataset)
     */
    public Dataset addWorklistItem(Dataset ds) throws ContentCreateException,
            PersistenceException, Exception {
        checkDuplicate(ds);
        MWLItem mwlItem = mwlItemDAO.create(ds
                .subSet(PATIENT_ATTRS, true, true), updateOrCreatePatient(ds));
        return toAttributes(mwlItem);
    }

    private void checkDuplicate(Dataset ds) throws DuplicateMWLItemException,
            PersistenceException {
        try {
            Dataset sps = ds.getItem(Tags.SPSSeq);
            MWLItem mwlItem = mwlItemDAO.findByRpIdAndSpsId(ds
                    .getString(Tags.RequestedProcedureID), sps
                    .getString(Tags.SPSID));
            throw new DuplicateMWLItemException("Duplicate "
                    + mwlItem.toString());
        }
        catch (NoResultException e) { // Ok
        }
    }

    private Dataset toAttributes(MWLItem mwlItem) {
        Dataset attrs = mwlItem.getAttributes();
        attrs.putAll(mwlItem.getPatient().getAttributes(false));
        return attrs;
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#updateWorklistItem(org.dcm4che.data.Dataset)
     */
    public boolean updateWorklistItem(Dataset ds)
            throws PatientMismatchException, Exception {
        MWLItem mwlItem;
        try {
            mwlItem = getWorklistItem(ds, true);
        }
        catch (NoResultException e) {
            return false;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
        Dataset attrs = mwlItem.getAttributes();
        attrs.putAll(ds.subSet(PATIENT_ATTRS, true, true),
                DcmObject.MERGE_ITEMS);
        mwlItem.setAttributes(attrs);
        return true;
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#getMwlItemDAO()
     */
    public MWLItemDAO getMwlItemDAO() {
        return mwlItemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#setMwlItemDAO(org.dcm4che.archive.dao.MWLItemDAO)
     */
    public void setMwlItemDAO(MWLItemDAO mwlItemDAO) {
        this.mwlItemDAO = mwlItemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /**
     * @see org.dcm4che.archive.service.MWLManager#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
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
}
