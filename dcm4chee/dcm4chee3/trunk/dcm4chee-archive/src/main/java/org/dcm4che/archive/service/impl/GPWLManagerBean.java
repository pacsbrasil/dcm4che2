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
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.GPSPSStatus;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.GPSPSDAO;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.entity.GPPPS;
import org.dcm4che.archive.entity.GPSPS;
import org.dcm4che.archive.entity.GPSPSRequest;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.archive.service.GPWLManager;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.util.UIDGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date: 2007/06/23 18:59:01 $
 * @since 28.03.2005
 */
@Transactional(propagation = Propagation.REQUIRED)
public class GPWLManagerBean implements GPWLManager {

    private static final int MAY_NO_LONGER_BE_UPDATED = 0xA501;

    private static final int WRONG_TRANSACTION_UID = 0xA502;

    private static final int ALREADY_IN_PROGRESS = 0xA503;

    private static final int[] PATIENT_ATTRS = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    private static final int[] OUTPUT_INFO_TAGS = {
            Tags.RequestedSubsequentWorkitemCodeSeq,
            Tags.NonDICOMOutputCodeSeq, Tags.OutputInformationSeq };

    private static Logger log = Logger.getLogger(GPWLManagerBean.class);

    private PatientDAO patDAO;

    private InstanceDAO instanceDAO;

    private GPSPSDAO gpspsDAO;

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#getWorklistItem(java.lang.String)
     */
    public Dataset getWorklistItem(String spsid) throws PersistenceException {
        try {
            return getWorklistItem(spsid, false);
        }
        catch (ContentDeleteException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#removeWorklistItem(java.lang.String)
     */
    public Dataset removeWorklistItem(String iuid) throws EJBException,
            ContentDeleteException, PersistenceException {
        return getWorklistItem(iuid, true);
    }

    private Dataset getWorklistItem(String iuid, boolean remove)
            throws ContentDeleteException, PersistenceException {
        GPSPS gpsps;
        try {
            gpsps = gpspsDAO.findBySopIuid(iuid);
        }
        catch (NoResultException onf) {
            return null;
        }
        final Patient pat = gpsps.getPatient();
        Dataset attrs = gpsps.getAttributes();
        attrs.putAll(pat.getAttributes(false));
        if (remove) {
            gpspsDAO.remove(gpsps);
        }
        return attrs;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#addWorklistItem(org.dcm4che.data.Dataset)
     */
    public String addWorklistItem(Dataset ds) {
        String iuid = ds.getString(Tags.SOPInstanceUID);
        if (iuid == null) {
            iuid = UIDGenerator.getInstance().createUID();
            ds.putUI(Tags.SOPInstanceUID, iuid);
        }
        try {
            gpspsDAO.create(ds.subSet(PATIENT_ATTRS, true, true),
                    findOrCreatePatient(ds));
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
        return iuid;
    }

    private Patient findOrCreatePatient(Dataset ds)
            throws PersistenceException, ContentCreateException,
            NonUniquePatientException, PatientMergedException,
            CircularMergedException {
        try {
            return patDAO.searchFor(ds, true);
        }
        catch (NoResultException onfe) {
            return patDAO.create(ds.subSet(PATIENT_ATTRS));
        }
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#updateWorklistItem(org.dcm4che.data.Dataset)
     */
    public void updateWorklistItem(Dataset ds) {
        try {
            final String iuid = ds.getString(Tags.SOPInstanceUID);
            GPSPS gpsps = gpspsDAO.findBySopIuid(iuid);
            Dataset attrs = gpsps.getAttributes();
            attrs.putAll(ds.subSet(PATIENT_ATTRS, true, true));
            gpsps.setAttributes(attrs);
        }
        catch (Exception e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#modifyStatus(java.lang.String, org.dcm4che.data.Dataset)
     */
    public void modifyStatus(String iuid, Dataset ds)
            throws DcmServiceException {
        try {
            GPSPS gpsps = gpspsDAO.findBySopIuid(iuid);
            String tsuid = ds.getString(Tags.TransactionUID);
            String status = ds.getString(Tags.GPSPSStatus);
            int statusAsInt = GPSPSStatus.toInt(status);
            switch (gpsps.getGpspsStatusAsInt()) {
            case GPSPSStatus.IN_PROGRESS:
                if (statusAsInt == GPSPSStatus.IN_PROGRESS)
                    throw new DcmServiceException(ALREADY_IN_PROGRESS);
                else if (!tsuid.equals(gpsps.getTransactionUid()))
                    throw new DcmServiceException(WRONG_TRANSACTION_UID);
                break;
            case GPSPSStatus.COMPLETED:
            case GPSPSStatus.DISCONTINUED:
                throw new DcmServiceException(MAY_NO_LONGER_BE_UPDATED);
            }
            Dataset attrs = gpsps.getAttributes();
            attrs.putCS(Tags.GPSPSStatus, status);
            gpsps
                    .setTransactionUid(statusAsInt == GPSPSStatus.IN_PROGRESS ? tsuid
                            : null);
            addActualHumanPerformers(attrs, ds
                    .get(Tags.ActualHumanPerformersSeq));
            gpsps.setAttributes(attrs);
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        catch (DcmServiceException e) {
            throw e;
        }
        catch (Exception e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private void addActualHumanPerformers(Dataset attrs, DcmElement src) {
        if (src == null || src.countItems() == 0)
            return;
        HashSet perfs = new HashSet();
        DcmElement dest = attrs.get(Tags.ActualHumanPerformersSeq);
        if (dest == null) {
            dest = attrs.putSQ(Tags.ActualHumanPerformersSeq);
        }
        else {
            Dataset item, code;
            for (int i = 0, n = dest.countItems(); i < n; ++i) {
                item = dest.getItem(i);
                code = item.getItem(Tags.HumanPerformerCodeSeq);
                perfs.add(code.getString(Tags.CodeValue) + '\\'
                        + code.getString(Tags.CodingSchemeDesignator));
            }
        }
        Dataset item, code;
        for (int i = 0, n = src.countItems(); i < n; ++i) {
            item = src.getItem(i);
            code = item.getItem(Tags.HumanPerformerCodeSeq);
            if (code != null) {
                if (perfs.add(code.getString(Tags.CodeValue) + '\\'
                        + code.getString(Tags.CodingSchemeDesignator))) {
                    dest.addItem(item);
                }
            }
        }
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#getOutputInformation(java.lang.String)
     */
    public Dataset getOutputInformation(String iuid)
            throws PersistenceException {
        Dataset result = DcmObjectFactory.getInstance().newDataset();
        GPSPS gpsps = gpspsDAO.findBySopIuid(iuid);
        Collection c = gpsps.getGppps();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Dataset gppps = ((GPPPS) iter.next()).getAttributes();
            result.putAll(gppps.subSet(OUTPUT_INFO_TAGS), Dataset.ADD_ITEMS);
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#addWorklistItem(org.dcm4che.data.Dataset, boolean, boolean)
     */
    public void addWorklistItem(Dataset wkitem, boolean checkPGP,
            boolean checkAppend) {
        if (checkPGP || checkAppend) {
            DcmElement reqSeq = wkitem.get(Tags.RefRequestSeq);
            if (reqSeq.countItems() > 1) {
                log
                        .info("Detect Group Case - do not check for previous Work Items");
            }
            else if (!reqSeq.isEmpty()) {
                try {
                    String rpid = reqSeq.getItem().getString(
                            Tags.RequestedProcedureID);
                    boolean pgp = checkPGP
                            && extendInputInfoIfPGP(wkitem, rpid);
                    if (pgp || checkAppend) {
                        GPSPS prevWkItem = findPrevWkItem(rpid, wkitem
                                .getItem(Tags.ScheduledWorkitemCodeSeq));
                        if (prevWkItem != null) {
                            if (pgp) {
                                removeRequestFrom(prevWkItem, rpid);
                            }
                            else {
                                appendInputInfoTo(prevWkItem, rpid, wkitem
                                        .get(Tags.InputInformationSeq));
                                return;
                            }
                        }
                    }
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    throw new EJBException(e);
                }
            }
        }
        addWorklistItem(wkitem);
    }

    private boolean removeRequestFrom(GPSPS prevWkItem, String rpid)
            throws ContentDeleteException {
        Collection c = prevWkItem.getRefRequests();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            GPSPSRequest rq = (GPSPSRequest) iter.next();
            if (rpid.equals(rq.getRequestedProcedureId())) {
                Dataset attrs = prevWkItem.getAttributes();
                DcmElement srcSq = attrs.get(Tags.RefRequestSeq);
                DcmElement dstSq = attrs.putSQ(Tags.RefRequestSeq);
                for (int i = 0, n = srcSq.countItems(); i < n; i++) {
                    Dataset item = srcSq.getItem(i);
                    if (!rpid.equals(item.getString(Tags.RequestedProcedureID))) {
                        dstSq.addItem(item);
                    }
                }
                String iuid = attrs.getString(Tags.SOPInstanceUID);
                if (dstSq.isEmpty()) {
                    log.info("Remove previous Work Item(uid:" + iuid
                            + ") for Requested Procedure(id:" + rpid + ")");
                    gpspsDAO.remove(prevWkItem);
                }
                else {
                    log.info("Detach previous Work Item(uid:" + iuid
                            + ") from Requested Procedure(id:" + rpid + ")");
                    iter.remove();
                    prevWkItem.setAttributes(attrs);
                }
                return true;
            }
        }
        return false;
    }

    private void appendInputInfoTo(GPSPS wkItem, String rpid,
            DcmElement srcInputSeq) {
        int numSOP = 0;
        Dataset attrs = wkItem.getAttributes();
        DcmElement destInputSeq = attrs.get(Tags.InputInformationSeq);
        for (int i = 0, n = srcInputSeq.countItems(); i < n; i++) {
            Dataset srcSty = srcInputSeq.getItem(i);
            Dataset dstSty = selectItem(destInputSeq, srcSty,
                    Tags.StudyInstanceUID, Tags.RefSeriesSeq);
            DcmElement srcSerSq = srcSty.get(Tags.RefSeriesSeq);
            DcmElement destSerSq = dstSty.get(Tags.RefSeriesSeq);
            for (int j = 0, m = srcSerSq.countItems(); j < m; j++) {
                Dataset srcSer = srcSerSq.getItem(j);
                Dataset dstSer = selectItem(destSerSq, srcSer,
                        Tags.SeriesInstanceUID, Tags.RefSOPSeq);
                dstSer.putAE(Tags.RetrieveAET, srcSer
                        .getString(Tags.RetrieveAET));
                DcmElement srcSOPSq = srcSer.get(Tags.RefSOPSeq);
                DcmElement destSOPSq = dstSer.get(Tags.RefSOPSeq);
                for (int k = 0, l = srcSOPSq.countItems(); k < l; k++) {
                    destSOPSq.addItem(srcSOPSq.getItem(k));
                    numSOP++;
                }
            }
        }
        String iuid = attrs.getString(Tags.SOPInstanceUID);
        log.info("Append Input Information of previous Work Item(uid:" + iuid
                + ") for Requested Procedure(id:" + rpid + ") with " + numSOP
                + " references.");
        wkItem.setAttributes(attrs);
    }

    private Dataset selectItem(DcmElement dstSeq, Dataset srcItem, int uidTag,
            int sqTag) {
        String uid = srcItem.getString(Tags.StudyInstanceUID);
        for (int i = 0, n = dstSeq.countItems(); i < n; i++) {
            Dataset sty = dstSeq.getItem(i);
            if (uid.equals(sty.getString(Tags.StudyInstanceUID))) {
                return sty;
            }
        }
        Dataset sty = dstSeq.addNewItem();
        sty.putUI(Tags.StudyInstanceUID, uid);
        sty.putSQ(Tags.RefSeriesSeq);
        return sty;
    }

    private GPSPS findPrevWkItem(String rpid, Dataset code)
            throws PersistenceException, ContentCreateException {
        Collection c = gpspsDAO.findByReqProcId(GPSPSStatus.SCHEDULED, code
                .getString(Tags.CodeValue), code
                .getString(Tags.CodingSchemeDesignator), rpid);
        int numPrevWkItem = c.size();
        return numPrevWkItem == 1 ? (GPSPS) c.iterator().next() : null;
    }

    private boolean extendInputInfoIfPGP(Dataset wkitem, String rpid)
            throws PersistenceException {
        DcmElement inputInfoSeq = wkitem.get(Tags.InputInformationSeq);
        if (inputInfoSeq.countItems() != 1) {
            return false;
        }
        DcmElement seriesSeq = inputInfoSeq.getItem().get(Tags.RefSeriesSeq);
        if (seriesSeq.countItems() != 1) {
            return false;
        }
        Dataset series = seriesSeq.getItem();
        DcmElement sopSeq = series.get(Tags.RefSOPSeq);
        if (sopSeq.countItems() != 1) {
            return false;
        }
        Dataset refSOP = sopSeq.getItem();
        if (!(UIDs.GrayscaleSoftcopyPresentationStateStorage.equals(refSOP
                .getString(Tags.RefSOPClassUID)))) {
            return false;
        }
        String retrieveAET = series.getString(Tags.RetrieveAET);
        String gspsuid = refSOP.getString(Tags.RefSOPInstanceUID);
        Dataset gsps;
        try {
            gsps = instanceDAO.findBySopIuid(gspsuid).getAttributes(true);
        }
        catch (NoResultException e) {
            log.warn("No such GSPS object (uid=" + gspsuid
                    + ") - no PGP support");
            return false;
        }
        DcmElement gspsSeriesSeq = gsps.get(Tags.RefSeriesSeq);
        int numI = 0;
        for (int i = 0, n = gspsSeriesSeq.countItems(); i < n; i++) {
            Dataset gspsSeries = gspsSeriesSeq.getItem(i);
            DcmElement imgSeq = gspsSeries.get(Tags.RefImageSeq);
            series = seriesSeq.addNewItem();
            series.putAE(Tags.RetrieveAET, retrieveAET);
            sopSeq = series.putSQ(Tags.RefSOPSeq);
            for (int j = 0, m = imgSeq.countItems(); j < m; j++) {
                Dataset img = imgSeq.getItem(j);
                refSOP = sopSeq.addNewItem();
                refSOP.putUI(Tags.RefSOPClassUID, img
                        .getString(Tags.RefSOPClassUID));
                refSOP.putUI(Tags.RefSOPInstanceUID, img
                        .getString(Tags.RefSOPInstanceUID));
                numI++;
            }
            series.putUI(Tags.SeriesInstanceUID, gspsSeries
                    .getString(Tags.SeriesInstanceUID));
        }
        log.info("Detect PGP case - create Work Item for Requested Procedure "
                + rpid + " referring " + numI + " images");
        return true;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#getGpspsDAO()
     */
    public GPSPSDAO getGpspsDAO() {
        return gpspsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#setGpspsDAO(org.dcm4che.archive.dao.GPSPSDAO)
     */
    public void setGpspsDAO(GPSPSDAO gpspsDAO) {
        this.gpspsDAO = gpspsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#getInstanceDAO()
     */
    public InstanceDAO getInstanceDAO() {
        return instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#setInstanceDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstanceDAO(InstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.GPWLManager#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

}
