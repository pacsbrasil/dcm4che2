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

package org.dcm4chex.archive.ejb.session;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.ejb.interfaces.InstanceLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocal;
import org.dcm4chex.archive.ejb.interfaces.SeriesLocalHome;
import org.dcm4chex.archive.ejb.interfaces.StudyLocal;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 26.02.2005
 * 
 * @ejb.bean name="MPPSEmulator" type="Stateless" view-type="remote"
 *           jndi-name="ejb/MPPSEmulator"
 * @ejb.transaction-type  type="Container"
 * @ejb.transaction type="Required"
 * @ejb.ejb-ref ejb-name="Series" view-type="local" ref-name="ejb/Series"
 */

public abstract class MPPSEmulatorBean implements SessionBean {

    private static final int[] PATIENT_TAGS = { Tags.SpecificCharacterSet,
            Tags.PatientName, Tags.PatientID, Tags.IssuerOfPatientID,
            Tags.PatientBirthDate, Tags.PatientSex };

    private static final int[] SERIES_TAGS = { Tags.SeriesDescription,
            Tags.PerformingPhysicianName, Tags.ProtocolName,
            Tags.SeriesInstanceUID };

    private static final int[] STUDY_TAGS = { Tags.ProcedureCodeSeq,
            Tags.StudyID };

    private static final int[] STUDY_SSA_TAGS = { Tags.AccessionNumber,
            Tags.StudyInstanceUID };

    private static final int[] SERIES_PPS_TAGS = {  
            Tags.PPSStartDate, Tags.PPSStartTime, Tags.PPSID };

    private SeriesLocalHome seriesHome;

    public void setSessionContext(SessionContext ctx) {
        Context jndiCtx = null;
        try {
            jndiCtx = new InitialContext();
            seriesHome = (SeriesLocalHome) jndiCtx
                    .lookup("java:comp/env/ejb/Series");
        } catch (NamingException e) {
            throw new EJBException(e);
        } finally {
            if (jndiCtx != null) {
                try {
                    jndiCtx.close();
                } catch (NamingException ignore) {
                }
            }
        }
    }

    public void unsetSessionContext() {
        seriesHome = null;
    }

    /**
     * @ejb.interface-method
     */
    public Dataset[] generateMPPS(String sourceAET, long delay)
            throws FinderException {
        Collection c = seriesHome.findWithNoPpsIuidFromSrcAETReceivedBefore(
                sourceAET, new Timestamp(System.currentTimeMillis() - delay));
        HashMap mppsMap = new HashMap();
        for (Iterator it = c.iterator(); it.hasNext();) {
            addSeries((SeriesLocal) it.next(), mppsMap, sourceAET);
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
        Date ppsStartDT = mpps.getDateTime(Tags.PPSStartDate, Tags.PPSStartTime);
        Date ppsEndDT = mpps.getDateTime(Tags.PPSEndDate, Tags.PPSEndTime);
        boolean calcPpsStartDT = ppsStartDT == null;
        boolean calcPpsEndDT = ppsEndDT == null;
        if (calcPpsStartDT || calcPpsEndDT) {
            SeriesLocal series = (SeriesLocal) list.get(1);
            ppsStartDT = ppsEndDT = series.getCreatedTime();
            for (int i = 2, n = list.size(); i < n; ++i) {
                series = (SeriesLocal) list.get(i);
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
            SeriesLocal series = (SeriesLocal) list.get(i);
            Dataset seriesAttrs = series.getAttributes(false);
            seriesAttrs.putAll(mpps.subSet(SERIES_PPS_TAGS));
            Dataset refPPS = seriesAttrs.putSQ(Tags.RefPPSSeq).addNewItem();
            refPPS.putUI(Tags.RefSOPClassUID, mpps.getString(Tags.SOPClassUID));
            refPPS.putUI(Tags.RefSOPInstanceUID, mpps.getString(Tags.SOPInstanceUID));            
            series.setAttributes(seriesAttrs);
        }
        return mpps;
    }
    
    private void addSeries(SeriesLocal series, HashMap mppsMap, String sourceAET) {
        final StudyLocal study = series.getStudy();
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
            for (int i = 0, n = Math.max(1,rqaSqSize); i < n; i++) {
                Dataset ssa = ssaSq.addNewItem();
                ssa.putAll(studyAttrs.subSet(STUDY_SSA_TAGS));
                if (i < rqaSqSize) {
                    ssa.putAll(rqaSq.getItem(i));
                }
            }
            
            mpps.putUI(Tags.SOPInstanceUID, UIDGenerator.getInstance().createUID());
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
            InstanceLocal inst = (InstanceLocal) it.next();
            Dataset refSOP = refImageSq.addNewItem();
            refSOP.putUI(Tags.RefSOPClassUID, inst.getSopCuid());
            refSOP.putUI(Tags.RefSOPInstanceUID, inst.getSopIuid());
        }
    }

    private String makePPSID(String md, String suid) {
        return md.substring(0, 2)
                + suid.substring(Math.max(0, suid.length() - 14));
    }

}
