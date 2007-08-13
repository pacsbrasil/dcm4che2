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
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
 * Gunter Zeilinger <gunterze@gmail.com>
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

import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.GPPPSDAO;
import org.dcm4che.archive.dao.GPSPSDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.GPPPS;
import org.dcm4che.archive.entity.GPSPS;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.service.GPPPSManagerLocal;
import org.dcm4che.archive.service.GPPPSManagerRemote;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 1.2 $ $Date: 2007/06/23 18:59:01 $
 * @since Apr 9, 2006
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class GPPPSManagerBean implements GPPPSManagerLocal, GPPPSManagerRemote {

    private static Logger log = Logger.getLogger(GPPPSManagerBean.class);

    private static final int GPSPS_NOT_IN_PROGRESS = 0xA504;

    private static final int GPSPS_DIFF_TRANS_UID = 0xA505;

    private static final int GPPPS_NOT_IN_PROGRESS = 0xA506;

    private static final int[] PATIENT_ATTRS_EXC = { Tags.RefPatientSeq,
            Tags.PatientName, Tags.PatientID, Tags.PatientBirthDate,
            Tags.PatientSex, };

    private static final int[] PATIENT_ATTRS_INC = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    @EJB private PatientDAO patDAO;

    @EJB private GPSPSDAO spsDAO;

    @EJB private GPPPSDAO ppsDAO;

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#createGPPPS(org.dcm4che.data.Dataset)
     */
    public void createGPPPS(Dataset ds) throws DcmServiceException {
        checkDuplicate(ds.getString(Tags.SOPInstanceUID));
        Patient pat = findOrCreatePatient(ds);
        Collection gpsps = findRefGpsps(ds.get(Tags.RefGPSPSSeq), pat);
        GPPPS pps = doCreate(ds, pat);
        if (gpsps != null) {
            pps.setGpsps(gpsps);
        }
    }

    private Patient findOrCreatePatient(Dataset ds) throws DcmServiceException {
        try {
            try {
                return patDAO.searchFor(ds, true);
            }
            catch (NoResultException onfe) {
                return patDAO.create(ds.subSet(PATIENT_ATTRS_INC));
            }
        }
        catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private GPPPS doCreate(Dataset ds, Patient pat) throws DcmServiceException {
        try {
            return ppsDAO.create(ds.subSet(PATIENT_ATTRS_EXC, true, true), pat);
        }
        catch (ContentCreateException e) {
            log.error("Creation of GP-PPS(iuid="
                    + ds.getString(Tags.SOPInstanceUID) + ") failed: ", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private void checkDuplicate(String ppsiuid) throws DcmServiceException {
        try {
            ppsDAO.findBySopIuid(ppsiuid);
            throw new DcmServiceException(Status.DuplicateSOPInstance);
        }
        catch (NoResultException e) { // Ok
        }
        catch (PersistenceException e) {
            log.error("Query for GP-PPS(iuid=" + ppsiuid + ") failed: ", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private Collection findRefGpsps(DcmElement spssq, Patient pat)
            throws DcmServiceException {
        if (spssq == null)
            return null;
        int n = spssq.countItems();
        ArrayList c = new ArrayList(n);
        for (int i = 0; i < n; i++) {
            Dataset refSOP = spssq.getItem(i);
            String spsiuid = refSOP.getString(Tags.RefSOPInstanceUID);
            String spstuid = refSOP.getString(Tags.RefGPSPSTransactionUID);
            GPSPS sps;
            try {
                sps = spsDAO.findBySopIuid(spsiuid);
                Patient spspat = sps.getPatient();
                if (!pat.equals(spspat)) {
                    log.info("Patient of referenced GP-SPS(iuid=" + spsiuid
                            + "): " + spspat.toString()
                            + " differes from Patient of GP-PPS: "
                            + pat.toString());
                    throw new DcmServiceException(Status.InvalidAttributeValue,
                            "GP-SPS PID: " + spspat.getPatientId()
                                    + ", GP-PPS PID: " + pat.getPatientId());
                }
                if (!sps.isInProgress()) {
                    String spsstatus = sps.getGpspsStatus();
                    log.info("Status of referenced GP-SPS(iuid=" + spsiuid
                            + ") is not IN PROGRESS, but " + spsstatus);
                    throw new DcmServiceException(GPSPS_NOT_IN_PROGRESS,
                            "ref GP-SPS status: " + spsstatus);
                }
                String tuid = sps.getTransactionUid();
                if (!spstuid.equals(tuid)) {
                    log.info("Referenced GP-SPS Transaction UID: " + spstuid
                            + " does not match the Transaction UID: " + tuid
                            + " of the N-ACTION request");
                    throw new DcmServiceException(GPSPS_DIFF_TRANS_UID);
                }
                c.add(sps);
            }
            catch (NoResultException e) {
                log.info("Referenced GP-SPS(iuid=" + spsiuid
                        + ") not in provided GP-WL");
            }
            catch (PersistenceException e) {
                log.error("Query for GP-SPS(iuid=" + spsiuid + ") failed: ", e);
                throw new DcmServiceException(Status.ProcessingFailure);
            }
        }
        return c;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#getGPPPS(java.lang.String)
     */
    public Dataset getGPPPS(String iuid) throws DcmServiceException {
        GPPPS pps = findBySopIuid(iuid);
        final Patient pat = pps.getPatient();
        Dataset attrs = pps.getAttributes();
        attrs.putAll(pat.getAttributes(false));
        return attrs;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#updateGPPPS(org.dcm4che.data.Dataset)
     */
    public void updateGPPPS(Dataset ds) throws DcmServiceException {
        final String iuid = ds.getString(Tags.SOPInstanceUID);
        GPPPS pps = findBySopIuid(iuid);
        if (!pps.isInProgress()) {
            String ppsstatus = pps.getPpsStatus();
            log.info("Status of GP-PPS(iuid=" + iuid
                    + ") is not IN PROGRESS, but " + ppsstatus);
            throw new DcmServiceException(GPPPS_NOT_IN_PROGRESS,
                    "GP-PPS status: " + ppsstatus);
        }
        Dataset attrs = pps.getAttributes();
        attrs.putAll(ds);
        pps.setAttributes(attrs);
    }

    private GPPPS findBySopIuid(String iuid) throws DcmServiceException {
        try {
            return ppsDAO.findBySopIuid(iuid);
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        catch (PersistenceException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#removeGPPPS(java.lang.String)
     */
    public void removeGPPPS(String iuid) throws ContentDeleteException,
            ContentDeleteException, PersistenceException {
        ppsDAO.remove(ppsDAO.findBySopIuid(iuid));
        log.info("GPPPS removed:" + iuid);
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#getPpsDAO()
     */
    public GPPPSDAO getPpsDAO() {
        return ppsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#setPpsDAO(org.dcm4che.archive.dao.GPPPSDAO)
     */
    public void setPpsDAO(GPPPSDAO ppsDAO) {
        this.ppsDAO = ppsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#getSpsDAO()
     */
    public GPSPSDAO getSpsDAO() {
        return spsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPPPSManager#setSpsDAO(org.dcm4che.archive.dao.GPSPSDAO)
     */
    public void setSpsDAO(GPSPSDAO spsDAO) {
        this.spsDAO = spsDAO;
    }
}
