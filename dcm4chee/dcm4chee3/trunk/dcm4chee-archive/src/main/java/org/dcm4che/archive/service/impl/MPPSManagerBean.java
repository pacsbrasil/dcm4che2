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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJB;
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
import org.dcm4che.archive.dao.MPPSDAO;
import org.dcm4che.archive.dao.MWLItemDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.MPPS;
import org.dcm4che.archive.entity.MWLItem;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.MPPSManagerLocal;
import org.dcm4che.archive.service.MPPSManagerRemote;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision: 1.2 $ $Date: 2007/06/23 18:59:01 $
 * @since 21.03.2004
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class MPPSManagerBean implements MPPSManagerLocal, MPPSManagerRemote  {

    private static Logger log = Logger.getLogger(MPPSManagerBean.class);

    private static final String NO_LONGER_BE_UPDATED_ERR_MSG = "Performed Procedure Step Object may no longer be updated";

    private static final int NO_LONGER_BE_UPDATED_ERR_ID = 0xA710;

    private static final int DELETED = 1;

    private static final int[] PATIENT_ATTRS_EXC = { Tags.RefPatientSeq,
            Tags.PatientName, Tags.PatientID, Tags.PatientBirthDate,
            Tags.PatientSex, };

    private static final int[] PATIENT_ATTRS_INC = { Tags.PatientName,
            Tags.PatientID, Tags.PatientBirthDate, Tags.PatientSex, };

    @EJB private PatientDAO patDAO;

    @EJB private SeriesDAO seriesDAO;

    @EJB private MPPSDAO mppsDAO;

    @EJB private MWLItemDAO mwlItemDAO;
    
    @EJB private CodeDAO codeDAO;

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#createMPPS(org.dcm4che.data.Dataset)
     */
    public void createMPPS(Dataset ds) throws DcmServiceException {
        checkDuplicate(ds.getString(Tags.SOPInstanceUID));
        try {
            mppsDAO.create(ds.subSet(PATIENT_ATTRS_EXC, true, true),
                    findOrCreatePatient(ds));
        }
        catch (ContentCreateException e) {
            log.error("Creation of MPPS(iuid="
                    + ds.getString(Tags.SOPInstanceUID) + ") failed: ", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private void checkDuplicate(String ppsiuid) throws DcmServiceException {
        try {
            mppsDAO.findBySopIuid(ppsiuid);
            throw new DcmServiceException(Status.DuplicateSOPInstance);
        }
        catch (NoResultException e) { // Ok
        }
        catch (PersistenceException e) {
            log.error("Query for GMPS(iuid=" + ppsiuid + ") failed: ", e);
            throw new DcmServiceException(Status.ProcessingFailure);
        }
    }

    private Patient findOrCreatePatient(Dataset ds)
            throws DcmServiceException {
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

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#getMPPS(java.lang.String)
     */
    public Dataset getMPPS(String iuid) throws PersistenceException {
        final MPPS mpps = mppsDAO.findBySopIuid(iuid);
        final Patient pat = mpps.getPatient();
        Dataset attrs = mpps.getAttributes();
        attrs.putAll(pat.getAttributes(false));
        return attrs;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#updateMPPS(org.dcm4che.data.Dataset)
     */
    public void updateMPPS(Dataset ds) throws DcmServiceException {
        MPPS mpps;
        try {
            mpps = mppsDAO.findBySopIuid(ds.getString(Tags.SOPInstanceUID));
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        catch (PersistenceException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        if (!"IN PROGRESS".equals(mpps.getPpsStatus())) {
            DcmServiceException e = new DcmServiceException(
                    Status.ProcessingFailure, NO_LONGER_BE_UPDATED_ERR_MSG);
            e.setErrorID(NO_LONGER_BE_UPDATED_ERR_ID);
            throw e;
        }
        Dataset attrs = mpps.getAttributes();
        attrs.putAll(ds);
        mpps.setAttributes(attrs, codeDAO);
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#linkMppsToMwl(java.lang.String, java.lang.String, java.lang.String)
     */
    public Map linkMppsToMwl(String rpid, String spsid, String mppsIUID)
            throws DcmServiceException {
        log.info("linkMppsToMwl spsId:" + spsid + " mpps:" + mppsIUID);
        MWLItem mwlItem;
        MPPS mpps;
        try {
            mwlItem = mwlItemDAO.findByRpIdAndSpsId(rpid, spsid);
            mpps = mppsDAO.findBySopIuid(mppsIUID);
            Patient mwlPat = mwlItem.getPatient();
            Patient mppsPat = mpps.getPatient();
            Dataset mwlAttrs = mwlItem.getAttributes();
            Map map = updateLinkedMpps(mpps, mwlItem, mwlAttrs);
            if (!mwlPat.equals(mppsPat)) {
                map.put("mwlPat", mwlPat.getAttributes(true));
                map.put("mppsPat", mppsPat.getAttributes(true));
            }
            return map;
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        catch (PersistenceException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
    }

    private Map updateLinkedMpps(MPPS mpps, MWLItem mwlItem,
            Dataset mwlAttrs) {
        Map map = new HashMap();
        Dataset ssa;
        Dataset mppsAttrs = mpps.getAttributes();
        log.debug("MPPS attrs:");
        log.debug(mppsAttrs);
        log.debug("MWL attrs:");
        log.debug(mwlAttrs);
        String rpid = mwlAttrs.getString(Tags.RequestedProcedureID);
        String spsid = mwlAttrs.getItem(Tags.SPSSeq).getString(Tags.SPSID);
        String accNo = mwlAttrs.getString(Tags.AccessionNumber);
        DcmElement ssaSQ = mppsAttrs.get(Tags.ScheduledStepAttributesSeq);
        String ssaSpsID, studyIUID = null;
        boolean spsNotInList = true;
        for (int i = 0, len = ssaSQ.countItems(); i < len; i++) {
            ssa = ssaSQ.getItem(i);
            if (ssa != null) {
                if (studyIUID == null) {
                    studyIUID = ssa.getString(Tags.StudyInstanceUID);
                    if (!studyIUID.equals(mwlAttrs
                            .getString(Tags.StudyInstanceUID))) {
                        if (mwlItem != null) {
                            log.info("StudyInstanceUID corrected for spsID "
                                    + spsid);
                            mwlAttrs.putUI(Tags.StudyInstanceUID, studyIUID);
                            mwlItem.setAttributes(mwlAttrs);
                        }
                        else {
                            log
                                    .warn("StudyInstanceUID of external MWL entry can not be corrected! spsID "
                                            + spsid);
                            log.warn("--- StudyIUID MWL:"
                                    + mwlAttrs.getString(Tags.StudyInstanceUID)
                                    + "   StudyIUID MPPS:" + studyIUID);
                        }
                    }
                }
                ssaSpsID = ssa.getString(Tags.SPSID);
                if (ssaSpsID == null || spsid.equals(ssaSpsID)) {
                    ssa.putSH(Tags.AccessionNumber, accNo);
                    ssa.putSH(Tags.SPSID, spsid);
                    ssa.putSH(Tags.RequestedProcedureID, rpid);
                    ssa.putUI(Tags.StudyInstanceUID, studyIUID);
                    spsNotInList = false;
                }
            }
        }
        if (spsNotInList) {
            ssa = ssaSQ.addNewItem();
            Dataset spsDS = mwlAttrs.getItem(Tags.SPSSeq);
            ssa.putUI(Tags.StudyInstanceUID, studyIUID);
            ssa.putSH(Tags.SPSID, spsid);
            ssa.putSH(Tags.RequestedProcedureID, rpid);
            ssa.putSH(Tags.AccessionNumber, accNo);
            ssa.putSQ(Tags.RefStudySeq);
            ssa.putSH(Tags.RequestedProcedureID, mwlAttrs
                    .getString(Tags.RequestedProcedureID));
            ssa
                    .putLO(Tags.SPSDescription, spsDS
                            .getString(Tags.SPSDescription));
            DcmElement mppsSPCSQ = ssa.putSQ(Tags.ScheduledProtocolCodeSeq);
            DcmElement mwlSPCSQ = spsDS.get(Tags.ScheduledProtocolCodeSeq);
            if (mwlSPCSQ != null && mwlSPCSQ.countItems() > 0) {
                for (int i = 0, len = mwlSPCSQ.countItems(); i < len; i++) {
                    mppsSPCSQ.addNewItem().putAll(mwlSPCSQ.getItem(i));
                }
            }
            log.debug("add new scheduledStepAttribute item:");
            log.debug(ssa);
            log.debug("new mppsAttrs:");
            log.debug(mppsAttrs);
        }
        mppsAttrs.putAll(mpps.getPatient().getAttributes(false));
        mpps.setAttributes(mppsAttrs, codeDAO);
        map.put("mppsAttrs", mppsAttrs);
        map.put("mwlAttrs", mwlAttrs);
        return map;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#linkMppsToMwl(org.dcm4che.data.Dataset, java.lang.String)
     */
    public Map linkMppsToMwl(Dataset mwlAttrs, String mppsIUID)
            throws DcmServiceException, PersistenceException,
            ContentCreateException {
        String spsID = mwlAttrs.get(Tags.SPSSeq).getItem()
                .getString(Tags.SPSID);
        log.info("linkMppsToMwl sps:" + spsID + " mpps:" + mppsIUID);
        MPPS mpps = mppsDAO.findBySopIuid(mppsIUID);
        AttributeFilter filter = AttributeFilter
                .getPatientAttributeFilter(null);
        Dataset mwlPatDs = filter.filter(mwlAttrs);
        Patient mppsPat = mpps.getPatient();
        Map map = updateLinkedMpps(mpps, null, mwlAttrs);
        if (!isSamePatient(mwlPatDs, mppsPat)) {
            Collection col = patDAO.findByPatientIdWithIssuer(mwlPatDs
                    .getString(Tags.PatientID), mwlPatDs
                    .getString(Tags.IssuerOfPatientID));
            Patient mwlPat = col.isEmpty() ? patDAO.create(mwlPatDs)
                    : (Patient) col.iterator().next();
            map.put("mwlPat", mwlPat.getAttributes(true));
            map.put("mppsPat", mppsPat.getAttributes(true));
        }
        return map;
    }

    private boolean isSamePatient(Dataset mwlPatDs, Patient mppsPat) {
        String mppsPatId = mppsPat.getPatientId();
        if (mppsPatId == null) {
            log
                    .warn("Link MPPS to MWL: MPPS patient without PatientID! try to check via Patient Name");
            String name = mppsPat.getPatientName();
            if (name == null) {
                log
                        .error("Link MPPS to MWL: MPPS patient without Patient Name! Ignore differences to avoid merge!");
                return true;
            }
            return name.equals(mwlPatDs.getString(Tags.PatientName));
        }
        else if (!mppsPat.getPatientId().equals(
                mwlPatDs.getString(Tags.PatientID)))
            return false;
        String issuer = mppsPat.getIssuerOfPatientId();
        return (issuer != null) ? issuer.equals(mwlPatDs
                .getString(Tags.IssuerOfPatientID)) : true;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#unlinkMpps(java.lang.String)
     */
    public void unlinkMpps(String mppsIUID) throws PersistenceException {
        MPPS mpps = mppsDAO.findBySopIuid(mppsIUID);
        Dataset mppsAttrs = mpps.getAttributes();
        DcmElement ssaSQ = mppsAttrs.get(Tags.ScheduledStepAttributesSeq);
        Dataset ds = null;
        String rpID, spsID;
        for (int i = ssaSQ.countItems() - 1; i >= 0; i--) {
            ds = ssaSQ.getItem(i);
            rpID = ds.getString(Tags.RequestedProcedureID);
            spsID = ds.getString(Tags.SPSID);
            if (spsID != null) {
                try {
                    MWLItem mwlItem = mwlItemDAO.findByRpIdAndSpsId(rpID,
                            spsID);
                    Dataset mwlDS = mwlItem.getAttributes();
                    mwlDS.getItem(Tags.SPSSeq).putCS(Tags.SPSStatus,
                            "SCHEDULED");
                    mwlItem.setAttributes(mwlDS);
                }
                catch (PersistenceException ignore) {
                }
            }
        }
        String studyIUID = ds.getString(Tags.StudyInstanceUID);
        ds.clear();
        ds.putUI(Tags.StudyInstanceUID, studyIUID);
        // add empty type 2 attributes.
        ds.putSH(Tags.SPSID, (String) null);
        ds.putSH(Tags.AccessionNumber, (String) null);
        ds.putSQ(Tags.RefStudySeq);
        ds.putSH(Tags.RequestedProcedureID, (String) null);
        ds.putLO(Tags.SPSDescription, (String) null);
        ds.putSQ(Tags.ScheduledProtocolCodeSeq);
        mppsAttrs.putSQ(Tags.ScheduledStepAttributesSeq).addItem(ds);
        mpps.setAttributes(mppsAttrs, codeDAO);
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#deleteMPPSEntries(java.lang.String[])
     */
    public boolean deleteMPPSEntries(String[] iuids) {
        for (int i = 0; i < iuids.length; i++) {
            try {
                mppsDAO.remove(mppsDAO.findBySopIuid(iuids[i]));
            }
            catch (Throwable x) {
                log.error("Cant delete mpps:" + iuids[i], x);
            }
        }
        return true;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#getSeriesIUIDs(java.lang.String)
     */
    public Collection getSeriesIUIDs(String mppsIUID)
            throws PersistenceException {
        Collection col = new ArrayList();
        Collection series = seriesDAO.findByPpsIuid(mppsIUID);
        for (Iterator iter = series.iterator(); iter.hasNext();) {
            col.add(((Series) iter.next()).getSeriesIuid());
        }
        return col;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#getSeriesAndStudyDS(java.lang.String)
     */
    public Collection getSeriesAndStudyDS(String mppsIUID)
            throws PersistenceException {
        Collection col = new ArrayList();
        Collection seriess = seriesDAO.findByPpsIuid(mppsIUID);
        Series series;
        Dataset ds;
        for (Iterator iter = seriess.iterator(); iter.hasNext();) {
            series = (Series) iter.next();
            ds = series.getAttributes(true);
            ds.putAll(series.getStudy().getAttributes(true));
            col.add(ds);
        }
        return col;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSManager#updateSeriesAndStudy(java.util.Collection)
     */
    public Dataset updateSeriesAndStudy(Collection seriesDS)
            throws PersistenceException, ContentCreateException {
        Dataset ds = null;
        String iuid;
        Series series = null;
        Dataset dsN = DcmObjectFactory.getInstance().newDataset();
        DcmElement refSeriesSeq = dsN.putSQ(Tags.RefSeriesSeq);
        Dataset dsSer;
        for (Iterator iter = seriesDS.iterator(); iter.hasNext();) {
            ds = (Dataset) iter.next();
            iuid = ds.getString(Tags.SeriesInstanceUID);
            series = seriesDAO.findBySeriesIuid(iuid);
            seriesDAO.updateAttributes(series, ds, true);
            dsSer = refSeriesSeq.addNewItem();
            dsSer.putAll(series.getAttributes(true));
            Iterator iter2 = series.getInstances().iterator();
            if (iter2.hasNext()) {
                DcmElement refSopSeq = dsSer.putSQ(Tags.RefSOPSeq);
                Instance il;
                Dataset dsInst;
                while (iter2.hasNext()) {
                    il = (Instance) iter2.next();
                    dsInst = refSopSeq.addNewItem();
                    dsInst.putUI(Tags.RefSOPClassUID, il.getSopCuid());
                    dsInst.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());
                    dsInst.putAE(Tags.RetrieveAET, il.getRetrieveAETs());
                }
            }
        }
        if (series != null) {
            Study study = series.getStudy();
            study.setAttributes(ds);
            dsN.putAll(study.getAttributes(true));
        }
        return dsN;
    }

    /**
     * @return the codeDAO
     */
    public CodeDAO getCodeDAO() {
        return codeDAO;
    }

    /**
     * @param codeDAO the codeDAO to set
     */
    public void setCodeDAO(CodeDAO codeDAO) {
        this.codeDAO = codeDAO;
    }
}
