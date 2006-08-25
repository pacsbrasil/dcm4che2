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

package org.dcm4chex.archive.dcm.mppsscu;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulator;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulatorHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 26.02.2005
 */

public class MPPSEmulatorService extends ServiceMBeanSupport implements
        NotificationListener {

    private static final String IN_PROGRESS = "IN PROGRESS";
    private static final String COMPLETED = "COMPLETED";
    private static final int[] MPPS_CREATE_TAGS = { Tags.SpecificCharacterSet,
            Tags.SOPInstanceUID, Tags.Modality, Tags.ProcedureCodeSeq,
            Tags.RefPatientSeq, Tags.PatientName, Tags.PatientID,
            Tags.IssuerOfPatientID, Tags.PatientBirthDate, Tags.PatientSex,
            Tags.PerformedStationAET, Tags.PerformedStationName,
            Tags.PPSStartDate, Tags.PPSStartTime, Tags.PPSEndDate,
            Tags.PPSEndTime, Tags.PPSStatus, Tags.PPSID,
            Tags.PPSDescription, Tags.PerformedProcedureTypeDescription,
            Tags.PerformedProtocolCodeSeq, Tags.ScheduledStepAttributesSeq, };

    private static final int[] MPPS_SET_TAGS = { Tags.SpecificCharacterSet,
            Tags.SOPInstanceUID, Tags.PPSEndDate, Tags.PPSEndTime,
            Tags.PPSStatus, Tags.PerformedSeriesSeq, };
    
    private static final int[] MWL_TAGS = {
            Tags.AccessionNumber, Tags.RefStudySeq, Tags.StudyInstanceUID,
            Tags.RequestedProcedureDescription, Tags.RequestedProcedureID, 
            Tags.PlacerOrderNumber, Tags.FillerOrderNumber         
    };

    private static final int[] SPS_TAGS = {
        Tags.SPSDescription, Tags.ScheduledProtocolCodeSeq, Tags.SPSID        
    };

    private static final int[] PAT_TAGS = {
        Tags.PatientName, Tags.PatientID, Tags.IssuerOfPatientID,
        Tags.PatientBirthDate, Tags.PatientSex
	};


    private final TimerSupport timer = new TimerSupport(this);
    
    private long pollInterval = 0L;

    private Integer schedulerID;

    private String calledAET;

    private String[] stationAETs = {};
    private long[] delays;
    private int[] mwlAttrs;
    
    private ObjectName mppsScuServiceName;

    private ObjectName mwlScuServiceName;

    private ObjectName hl7SendServiceName;

    public final ObjectName getMppsScuServiceName() {
        return mppsScuServiceName;
    }

    public final void setMppsScuServiceName(ObjectName mppsScuServiceName) {
        this.mppsScuServiceName = mppsScuServiceName;
    }

    public final ObjectName getMwlScuServiceName() {
        return mwlScuServiceName;
    }

    public final void setMwlScuServiceName(ObjectName mwlScuServiceName) {
        this.mwlScuServiceName = mwlScuServiceName;
    }

	/**
	 * @return Returns the hl7SendServiceName.
	 */
	public ObjectName getHl7SendServiceName() {
		return hl7SendServiceName;
	}
	/**
	 * @param hl7SendServiceName The hl7SendServiceName to set.
	 */
	public void setHl7SendServiceName(ObjectName hl7SendServiceName) {
		this.hl7SendServiceName = hl7SendServiceName;
	}
    
    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        this.calledAET = calledAET;
    }
    
    public final String getEmulateMPPSforModalities() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < stationAETs.length; i++) {
            sb.append(stationAETs[i]);
            sb.append('[');
            sb.append(RetryIntervalls.formatInterval(delays[i]));
            switch (mwlAttrs[i]) {
            case Tags.AccessionNumber:
                sb.append("]->MWL[AccessionNumber");
                break;
            case Tags.StudyInstanceUID:
                sb.append("]->MWL[StudyInstanceUID");
                break;
            case Tags.SPSID:
                sb.append("]->MWL[SPSID");
                break;
            }
            sb.append("]\r\n");
        }
        return sb.toString();
    }
        
    public final void setEmulateMPPSforModalities(String s) {
        StringTokenizer stk = new StringTokenizer(s, "\r\n \t;");        
        int tkcount = stk.countTokens();
        String[] newStationAETs = new String[tkcount];
        long[] newDelays = new long[tkcount];
        int[] newMWLAttrs = new int[tkcount];
        for (int i = 0; i < tkcount; i++) {
            StringTokenizer entry = new StringTokenizer(stk.nextToken(), "[]");
            try {
                newStationAETs[i] = entry.nextToken();
                newDelays[i] = RetryIntervalls.parseInterval(entry.nextToken());
                if (entry.hasMoreTokens()) {
                    entry.nextToken(); // skip "->MWL"
                    newMWLAttrs[i] = Tags.forName(entry.nextToken());
                    if (!(newMWLAttrs[i] == Tags.SPSID 
                            || newMWLAttrs[i] == Tags.StudyInstanceUID 
                            || newMWLAttrs[i] == Tags.AccessionNumber)) {
                        throw new IllegalArgumentException(s);
                    }
                }
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException(s);
            }
        }
        stationAETs = newStationAETs;
        delays = newDelays;
        mwlAttrs = newMWLAttrs;
    }
    
    public final String getPollInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(pollInterval);
    }

    public void setPollInterval(String interval) {
        this.pollInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            timer.stopScheduler("CheckSeriesWithoutMPPS", schedulerID, this);
            schedulerID = timer.startScheduler("CheckSeriesWithoutMPPS",
            		pollInterval, this);
        }
    }

    public void handleNotification(Notification notification, Object handback) {
        emulateMPPS();
    }

    public int emulateMPPS() {
        log.info("Check for received series without MPPS");
        if (stationAETs.length == 0)
            return 0;
        int num = 0;
        MPPSEmulator mppsEmulator;
        try {
            mppsEmulator = getMPPSEmulatorHome().create();
        } catch (Exception e) {
            log.error("Failed to emulate MPPS:", e);
            return 0;
        }
        for (int i = 0; i < stationAETs.length; ++i) {
            Dataset[] mpps;
            try {
                mpps = mppsEmulator.generateMPPS(stationAETs[i],
                        delays[i]);
            } catch (Exception e) {
                log.error("Failed to emulate MPPS for series received from " + 
                        stationAETs[i] + " failed:", e);
                continue;
            }
            for (int j = 0; j < mpps.length; ++j) {
                Dataset ssa = mpps[j].getItem(Tags.ScheduledStepAttributesSeq);
                String suid = ssa.getString(Tags.StudyInstanceUID);
                log.info("Emulate MPPS for Study:" + suid + " of Patient:"
                        + mpps[j].getString(Tags.PatientName)
                        + " received from Station:" + stationAETs[i]);
                try {
                    if (mwlAttrs[i] != 0) {
                        addMWLAttrs(mpps[j], mwlAttrs[i]);
                    }
                    createMPPS(mpps[j]);
                    updateMPPS(mpps[j]);
                    ++num;
                } catch (Exception e) {
                    log.error("Failed to emulate MPPS for Study:" + suid 
                            + " of Patient:" + mpps[j].getString(Tags.PatientName)
                            + " received from Station:" + stationAETs[i] + ":",
                            e);
                }
            }
        }
        return num;
    }

    private boolean addMWLAttrs(Dataset mpps, int mwlAttr) {
        Dataset ssa = mpps.getItem(Tags.ScheduledStepAttributesSeq);
        String imgAccNo = ssa.getString(Tags.AccessionNumber);
        String imgSIUD = ssa.getString(Tags.StudyInstanceUID);
        String imgSPSID = ssa.getString(Tags.SPSID);
        String imgPID = mpps.getString(Tags.PatientID);
        String imgPName = mpps.getString(Tags.PatientName);
        String mwlAttrVal = ssa.getString(mwlAttr);
        if (mwlAttrVal == null) {
            log.info("Missing attribute " + Tags.toString(mwlAttr) 
                    + " in received Study with UID: " + imgSIUD + " and AccNo: "
                    + imgAccNo + " for Patient: " + imgPName + " with ID: "
                    + imgPID + " -> cannot query for matching MWL entries.");
            makeUnscheduled(mpps, imgSIUD);
            return true;            
        }
        Dataset filter = mkFilter(mwlAttr, mwlAttrVal);   
        List mwlEntries = findMWLEntries(filter);
        if (mwlEntries == null)
            return false;
        if (mwlEntries.isEmpty()) {
            log.info("No matching MWL[" + Tags.toString(mwlAttr) 
                    + "=" + mwlAttrVal + "] item for received Study with UID: " 
                    + imgSIUD + " and AccNo: " + imgAccNo + " for Patient: "
                    + imgPName + " with ID: " + imgPID + " found.");
            makeUnscheduled(mpps, imgSIUD);
            return true;
        }
        log.info("Found " + mwlEntries.size() 
                + " matching MWL[" + Tags.toString(mwlAttr) 
                + "=" + mwlAttrVal + "] item(s) for received Study with UID: " 
                + imgSIUD + " and AccNo: " + imgAccNo + " for Patient: "
                + imgPName + " with ID: " + imgPID);
        Dataset mwlitem1 = ((Dataset) mwlEntries.get(0));
        String mwlPID = mwlitem1.getString(Tags.PatientID);
        String mwlPName = mwlitem1.getString(Tags.PatientName);
        int n = mwlEntries.size();
        for (int i = 1; i < n; ++i) {
            if (!mwlPID.equals(((Dataset) mwlEntries.get(i)).getString(Tags.PatientID))) {
                log.warn("Found " + mwlEntries.size() 
                        + " matching MWL[" + Tags.toString(mwlAttr) 
                        + "=" + mwlAttrVal + "] items for received Study with UID: " 
                        + imgSIUD + " and AccNo: " + imgAccNo + " for Patient: "
                        + imgPName + " with ID: " + imgPID
                        + " associated to more than one Patient -> treat as unscheduled!");               
                makeUnscheduled(mpps, imgSIUD);
                return true;
            }
        }
        if (!mwlPID.equals(imgPID)) {
            log.info("Patient Information in matching MWL[" 
                    + Tags.toString(mwlAttr) + "=" + mwlAttrVal
                    + "] item for received Study with UID: " + imgSIUD
                    + " and AccNo: " + imgAccNo 
                    + " differs from Patient Information in received objects -> "
                    + " Merge Patient " + imgPName + " with ID: " + imgPID
                    + " with Patient "+ mwlPName + " with ID: " + mwlPID);               
            sendHL7Merge(mwlitem1, mpps);
            mpps.putAll(mwlitem1.subSet(PAT_TAGS));
        }
        DcmElement ssaSq = mpps.putSQ(Tags.ScheduledStepAttributesSeq);
        for (int i = 0; i < n; ++i) {
            ssa = ssaSq.addNewItem();
            Dataset mwlItem = (Dataset) mwlEntries.get(i);
            String mwlAccNo = mwlItem.getString(Tags.AccessionNumber);
            String mwlSIUID = mwlItem.getString(Tags.StudyInstanceUID);
            Dataset spsSq = mwlItem.getItem(Tags.SPSSeq);
            String mwlSPSID = spsSq.getString(Tags.SPSID);
            if (imgSPSID != null && !imgSPSID.equals(mwlSPSID)) {
                log.info("SPS ID: " + mwlSPSID + " of matching MWL["
                        + Tags.toString(mwlAttr) + "=" + mwlAttrVal
                        + "] item for received Study with UID: " + imgSIUD 
                        + " for Patient: " + imgPName + " with ID: " 
                        + imgPID + " differs from SPS ID: " + imgSPSID
                        + "in received objects.");
            }
            if (imgAccNo != null && !imgAccNo.equals(mwlAccNo)) {
                log.info("AccNo: " + mwlAccNo + " in matching MWL["
                        + Tags.toString(mwlAttr) + "=" + mwlAttrVal
                        + "] item for received Study with UID: " + imgSIUD 
                        + " for Patient: " + imgPName + " with ID: " 
                        + imgPID + " differs from AccNo: " + imgAccNo
                        + "in received objects.");
            }
            if (!imgSIUD.equals(mwlSIUID)) {
                log.info("Study Instance UID: " + mwlSIUID 
                        + " in matching MWL[" + Tags.toString(mwlAttr) + "="
                        + mwlAttrVal + "] item for received Study with UID: "
                        + imgSIUD  + " and AccNo: " + imgAccNo
                        + " for Patient: " + imgPName + " with ID: " + imgPID
                        + " differs from Study Instance UID in received objects.");                
            }
            ssa.putAll(mwlItem.subSet(MWL_TAGS));
            ssa.putAll(spsSq.subSet(SPS_TAGS)); 
        }
        return true;
    }

    private void makeUnscheduled(Dataset mpps, String suid) {
        Dataset ssa = mpps.putSQ(Tags.ScheduledStepAttributesSeq).addNewItem();
        ssa.putUI(Tags.StudyInstanceUID, suid);
        fillType2Attrs(ssa, MWL_TAGS);
        fillType2Attrs(ssa, SPS_TAGS);
    }

    private Dataset mkFilter(int mwlAttr, String mwlAttrVal) {
        Dataset filter = DcmObjectFactory.getInstance().newDataset();
        for (int i = 0; i < MWL_TAGS.length; i++) {
            filter.putXX(MWL_TAGS[i]);
        }
        for (int i = 0; i < PAT_TAGS.length; i++) {//we need pat atrributes for merge (if necessary)!
            filter.putXX(PAT_TAGS[i]);
        }
        Dataset sps = filter.putSQ(Tags.SPSSeq).addNewItem();
        if (mwlAttr == Tags.SPSID) {
            sps.putLO(Tags.SPSDescription);
            sps.putSQ(Tags.ScheduledProtocolCodeSeq);
            sps.putSH(Tags.SPSID, mwlAttrVal);
        } else {
            filter.putXX(mwlAttr, mwlAttrVal);
        }
        return filter;
    }

    private void fillType2Attrs(Dataset ds, int[] tags) {
        for (int i = 0; i < tags.length; i++) {
            if (!ds.contains(tags[i]))
                ds.putXX(tags[i]);
        }		
    }

    private void createMPPS(Dataset mpps) throws Exception {
        mpps.putCS(Tags.PPSStatus, IN_PROGRESS);
        fillType2Attrs(mpps, MPPS_CREATE_TAGS);
        sendMPPS(true, mpps.subSet(MPPS_CREATE_TAGS), calledAET);
    }

    private void updateMPPS(Dataset mpps) throws Exception {
        mpps.putCS(Tags.PPSStatus, COMPLETED);
        sendMPPS(false, mpps.subSet(MPPS_SET_TAGS), calledAET);
    }

    private MPPSEmulatorHome getMPPSEmulatorHome() throws HomeFactoryException {
        return (MPPSEmulatorHome) EJBHomeFactory.getFactory().lookup(
                MPPSEmulatorHome.class, MPPSEmulatorHome.JNDI_NAME);
    }

    private void sendMPPS(boolean create, Dataset mpps, String destination) 
    throws Exception {
        server.invoke(mppsScuServiceName,
                "sendMPPS", new Object[] { Boolean.valueOf(create), mpps, destination },
                new String[] { boolean.class.getName(), Dataset.class.getName(),
                        String.class.getName() });
    }

    public List findMWLEntries(Dataset ds) {
        try {
            return (List) server.invoke(mwlScuServiceName, "findMWLEntries",
                    new Object[] { ds },
                    new String[] { Dataset.class.getName() });
        } catch (Exception x) {
            log.error("Exception occured in findMWLEntries: " + x.getMessage(),
                    x);
        }
        return null;
    }
    
    void sendHL7Merge(Dataset dsDominant, Dataset priorPat) {
        try {
            server.invoke(this.hl7SendServiceName, "sendHL7PatientMerge",
                    new Object[] {
                        dsDominant,
                        new Dataset[] { priorPat }, 
            		"LOCAL^LOCAL",
			"LOCAL^LOCAL",
			Boolean.FALSE
                    },
                    new String[] {
                        Dataset.class.getName(),
        		Dataset[].class.getName(),
			String.class.getName(),
			String.class.getName(),
			boolean.class.getName()
                    });
        } catch (Exception e) {
            log.error("Failed to send HL7 patient merge message:", e);
            log.error(dsDominant);
        }
    }
    
    protected void startService() throws Exception {
        timer.init();
        schedulerID = timer.startScheduler("CheckSeriesWithoutMPPS",
        		pollInterval, this);
    }

    protected void stopService() throws Exception {
        timer.stopScheduler("CheckSeriesWithoutMPPS", schedulerID, this);
        super.stopService();
    }
}
