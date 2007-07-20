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

package org.dcm4che.archive.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.MPPSEmulator;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emulate MPPS messages (in order to close studies/series') in situations where
 * the sending systems do not utilize MPPS. This is important when you want to
 * trigger additional actions after the objects have been stored.
 * 
 * @author gunter.zeilinger@tiani.com
 */
@Transactional(propagation = Propagation.REQUIRED)
public class MPPSEmulatorBean implements MPPSEmulator {

    private static final int[] PATIENT_TAGS = { Tags.SpecificCharacterSet,
            Tags.PatientName, Tags.PatientID, Tags.IssuerOfPatientID,
            Tags.PatientBirthDate, Tags.PatientSex };

    private static final int[] SERIES_TAGS = { Tags.SeriesDescription,
            Tags.PerformingPhysicianName, Tags.ProtocolName,
            Tags.SeriesInstanceUID };

    private static final int[] STUDY_TAGS = { Tags.ProcedureCodeSeq,
            Tags.StudyID };

    private static final int[] SERIES_PPS_TAGS = { Tags.PPSStartDate,
            Tags.PPSStartTime, Tags.PPSID };

    private SeriesDAO seriesDAO;

    /** 
     * @see org.dcm4che.archive.service.MPPSEmulator#generateMPPS(java.lang.String, long)
     */
    public Dataset[] generateMPPS(String sourceAET, long delay) {
        List<Series> seriess = seriesDAO
                .findWithNoPpsIuidFromSrcAETReceivedBefore(sourceAET,
                        new Timestamp(System.currentTimeMillis() - delay));
        Map mppsMap = new HashMap();
        for (int i = 0; i < seriess.size(); i++) {
            addSeries(seriess.get(i), mppsMap, sourceAET);
        }

        Dataset[] result = new Dataset[mppsMap.size()];
        Iterator it = mppsMap.values().iterator();
        for (int i = 0; i < result.length; ++i) {
            result[i] = updateSeries((List) it.next());
        }
        return result;
    }

    private Dataset updateSeries(List list) {
        Dataset mpps = (Dataset) list.get(0);
        // use series receive time as fall back for PPS Start/End Date/Time
        Date ppsStartDT = mpps
                .getDateTime(Tags.PPSStartDate, Tags.PPSStartTime);
        Date ppsEndDT = mpps.getDateTime(Tags.PPSEndDate, Tags.PPSEndTime);
        boolean calcPpsStartDT = ppsStartDT == null;
        boolean calcPpsEndDT = ppsEndDT == null;
        if (calcPpsStartDT || calcPpsEndDT) {
            Series series = (Series) list.get(1);
            ppsStartDT = ppsEndDT = series.getCreatedTime();
            for (int i = 2, n = list.size(); i < n; ++i) {
                series = (Series) list.get(i);
                Date dt = series.getCreatedTime();
                if (calcPpsStartDT && ppsStartDT.compareTo(dt) > 0)
                    ppsStartDT = dt;
                if (calcPpsEndDT && ppsEndDT.compareTo(dt) > 0)
                    ppsEndDT = dt;
            }
            if (calcPpsStartDT) {
                mpps.putDA(Tags.PPSStartDate, ppsStartDT);
                mpps.putTM(Tags.PPSStartTime, ppsStartDT);
            }
            if (calcPpsEndDT) {
                mpps.putDA(Tags.PPSEndDate, ppsEndDT);
                mpps.putTM(Tags.PPSEndTime, ppsEndDT);
            }
        }
        for (int i = 1, n = list.size(); i < n; ++i) {
            Series series = (Series) list.get(i);
            Dataset seriesAttrs = series.getAttributes(false);
            seriesAttrs.putAll(mpps.subSet(SERIES_PPS_TAGS));
            Dataset refPPS = seriesAttrs.putSQ(Tags.RefPPSSeq).addNewItem();
            refPPS.putUI(Tags.RefSOPClassUID, mpps.getString(Tags.SOPClassUID));
            refPPS.putUI(Tags.RefSOPInstanceUID, mpps
                    .getString(Tags.SOPInstanceUID));
            series.setAttributes(seriesAttrs);
        }
        return mpps;
    }

    private void addSeries(Series series, Map mppsMap, String sourceAET) {
        final Study study = series.getStudy();
        final String suid = study.getStudyIuid();
        final String md = series.getModality();
        final Dataset seriesAttrs = series.getAttributes(false);
        final String key = md + suid;
        List list = (List) mppsMap.get(key);
        if (list == null) {
            list = new ArrayList();
            mppsMap.put(key, list);
            Dataset mpps = DcmObjectFactory.getInstance().newDataset();
            list.add(mpps);
            final Dataset patAttrs = study.getPatient().getAttributes(false);
            final Dataset studyAttrs = study.getAttributes(false);
            mpps.putAll(patAttrs.subSet(PATIENT_TAGS));
            mpps.putAll(studyAttrs.subSet(STUDY_TAGS));
            DcmElement rqaSq = seriesAttrs.get(Tags.RequestAttributesSeq);
            int rqaSqSize = rqaSq != null ? rqaSq.countItems() : 0;
            DcmElement ssaSq = mpps.putSQ(Tags.ScheduledStepAttributesSeq);
            if (rqaSqSize == 0) { // unscheduled case
                Dataset ssa = ssaSq.addNewItem();
                ssa.putSH(Tags.AccessionNumber);
                ssa.putSQ(Tags.RefStudySeq);
                ssa.putUI(Tags.StudyInstanceUID, studyAttrs
                        .getString(Tags.StudyInstanceUID));
                ssa.putLO(Tags.RequestedProcedureDescription);
                ssa.putSH(Tags.SPSID);
                ssa.putLO(Tags.SPSDescription);
                ssa.putSQ(Tags.ScheduledProtocolCodeSeq);
                ssa.putSH(Tags.RequestedProcedureID);
            }
            else {
                for (int i = 0, n = rqaSqSize; i < n; i++) {
                    Dataset ssa = rqaSq.getItem(i);
                    ssaSq.addItem(ssa);
                    ssa.putSH(Tags.AccessionNumber, studyAttrs
                            .getString(Tags.AccessionNumber));
                    ssa.putUI(Tags.StudyInstanceUID, studyAttrs
                            .getString(Tags.StudyInstanceUID));
                    if (!ssa.contains(Tags.RefStudySeq)) {
                        ssa.putSQ(Tags.RefStudySeq);
                    }
                    if (!ssa.contains(Tags.SPSID)) {
                        ssa.putSH(Tags.SPSID);
                    }
                    if (!ssa.contains(Tags.SPSDescription)) {
                        ssa.putLO(Tags.SPSDescription);
                    }
                    if (!ssa.contains(Tags.ScheduledProtocolCodeSeq)) {
                        ssa.putSQ(Tags.ScheduledProtocolCodeSeq);
                    }
                    if (!ssa.contains(Tags.RequestedProcedureID)) {
                        ssa.putSH(Tags.RequestedProcedureID);
                    }
                }
            }

            mpps.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance()
                    .createUID());
            mpps.putUI(Tags.SOPClassUID, UIDs.ModalityPerformedProcedureStep);
            mpps.putAE(Tags.PerformedStationAET, sourceAET);
            mpps.putSH(Tags.PerformedStationName, seriesAttrs
                    .getString(Tags.StationName));
            mpps.putCS(Tags.Modality, seriesAttrs.getString(Tags.Modality));
            mpps.putSH(Tags.PPSID, makePPSID(md, suid));
            mpps.putLO(Tags.PerformedProcedureTypeDescription, studyAttrs
                    .getString(Tags.StudyDescription));
            mpps.putSQ(Tags.PerformedSeriesSeq);
        }
        Dataset mpps = (Dataset) list.get(0);
        list.add(series);
        // derive PPS Start/End Date/Time from Series Date/Time
        Date ppsStartDT = mpps
                .getDateTime(Tags.PPSStartDate, Tags.PPSStartTime);
        Date ppsEndDT = mpps.getDateTime(Tags.PPSEndDate, Tags.PPSEndTime);
        Date seriesDT = seriesAttrs.getDateTime(Tags.SeriesDate,
                Tags.SeriesTime);
        if (seriesDT != null) {
            if (ppsStartDT == null || ppsStartDT.compareTo(seriesDT) > 0) {
                ppsStartDT = seriesDT;
                mpps.putDA(Tags.PPSStartDate, seriesDT);
                mpps.putTM(Tags.PPSStartTime, seriesDT);
            }
            if (ppsEndDT == null || ppsEndDT.compareTo(seriesDT) < 0) {
                ppsEndDT = seriesDT;
                mpps.putDA(Tags.PPSEndDate, seriesDT);
                mpps.putTM(Tags.PPSEndTime, seriesDT);
            }
        }
        Dataset seriesItem = mpps.get(Tags.PerformedSeriesSeq).addNewItem();
        seriesItem.putAll(seriesAttrs.subSet(SERIES_TAGS));
        // TODO put references to non-images into separate
        // Referenced Non- Image Composite SOP Instance Sequence
        DcmElement refImageSq = seriesItem.putSQ(Tags.RefImageSeq);
        Collection c = series.getInstances();
        for (Iterator it = c.iterator(); it.hasNext();) {
            Instance inst = (Instance) it.next();
            Dataset refSOP = refImageSq.addNewItem();
            refSOP.putUI(Tags.RefSOPClassUID, inst.getSopCuid());
            refSOP.putUI(Tags.RefSOPInstanceUID, inst.getSopIuid());
        }
    }

    private String makePPSID(String md, String suid) {
        return md.substring(0, 2)
                + suid.substring(Math.max(0, suid.length() - 14));
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSEmulator#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.MPPSEmulator#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

}
