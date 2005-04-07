/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.gpwlscp;

import java.io.FileNotFoundException;
import java.util.Date;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.dcm.mppsscp.MPPSScpService;
import org.dcm4chex.archive.ejb.interfaces.GPWLManager;
import org.dcm4chex.archive.ejb.interfaces.GPWLManagerHome;
import org.dcm4chex.archive.ejb.interfaces.MPPSManager;
import org.dcm4chex.archive.ejb.interfaces.MPPSManagerHome;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.jboss.system.ServiceMBeanSupport;
import org.xml.sax.InputSource;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.04.2005
 *
 */

public class GPWLFeedService extends ServiceMBeanSupport implements
		NotificationListener {

	private static final int[] PAT_ATTR_TAGS = {
		Tags.PatientName,
		Tags.PatientID,
		Tags.PatientBirthDate,
		Tags.PatientSex,
	};
	private static final int[] REF_REQUEST_TAGS = {
		Tags.AccessionNumber,
		Tags.RefStudySeq,
		Tags.StudyInstanceUID,
		Tags.RequestedProcedureDescription,
		Tags.RequestedProcedureID,
	};

    private ObjectName mppsScpServiceName;
	
    public String getEjbProviderURL() {
        return EJBHomeFactory.getEjbProviderURL();
    }        

    public void setEjbProviderURL(String ejbProviderURL) {
        EJBHomeFactory.setEjbProviderURL(ejbProviderURL);
    }
    
	public final ObjectName getMppsScpServiceName() {
        return mppsScpServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScpServiceName) {
        this.mppsScpServiceName = mppsScpServiceName;
    }

    protected void startService() throws Exception {
        server.addNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(mppsScpServiceName,
                this,
                MPPSScpService.NOTIF_FILTER,
                null);
    }

	public void handleNotification(Notification notif, Object handback) {
        Dataset mpps = (Dataset) notif.getUserData();
        // if N_CREATE
        if (mpps.contains(Tags.ScheduledStepAttributesSeq)) return;
        if (!"COMPLETED".equals(mpps.getString(Tags.PPSStatus))) return;
        final String mppsiuid = mpps.getString(Tags.SOPInstanceUID);
       	addWorklistItem(makeGPWLItem(getMPPS(mppsiuid)));
    }

	private void addWorklistItem(Dataset ds) {
		if (ds == null) return;
		try {
			GPWLManager mgr = getGPWLManagerHome().create();
			try {
				mgr.addWorklistItem(ds);
			} finally {
				try {
					mgr.remove();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			log.error("Failed to add Worklist Item:", e);
		}
	}

	private Dataset makeGPWLItem(Dataset mpps) {
        Dataset codeItem = mpps.getItem(Tags.ProcedureCodeSeq);
        StringBuffer sb = new StringBuffer("resource:gpwl/");
        final String codeValueDesignator = codeItem.getString(Tags.CodingSchemeDesignator);
        final String codeValue = codeItem.getString(Tags.CodeValue);
		sb.append(codeValue);
        sb.append('.');
		sb.append(codeValueDesignator);
        sb.append(".xml");
        for (int i = 14, n = sb.length(); i < n; ++i) {
        	char ch = sb.charAt(i);
        	if (!(ch >= '0' && ch <= '9'
        			|| ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z'
        			|| ch == ':' || ch == '-' || ch == '_' || ch == '.')) {
        		sb.setCharAt(i, '_');
        	}
        }
        try {
			Dataset gpsps = DatasetUtils.fromXML(new InputSource(sb.toString()));
			gpsps.putAll(mpps.subSet(PAT_ATTR_TAGS));
			gpsps.putUI(Tags.SOPClassUID, UIDs.GeneralPurposeScheduledProcedureStepSOPClass);
			final String iuid = UIDGenerator.getInstance().createUID();
			gpsps.putUI(Tags.SOPInstanceUID, iuid);
			DcmElement ssaSq = mpps.get(Tags.ScheduledStepAttributesSeq);
			String siuid = ssaSq.getItem().getString(Tags.StudyInstanceUID);
			gpsps.putUI(Tags.StudyInstanceUID, siuid);
			gpsps.putSH(Tags.SPSID, mpps.getString(Tags.PPSID));
			if (!gpsps.contains(Tags.SPSStartDateAndTime)) {
				gpsps.putDT(Tags.SPSStartDateAndTime, new Date());
			}
			DcmElement ppsSq = gpsps.putSQ(Tags.RefPPSSeq);
			Dataset refPPS = ppsSq.addNewItem();
			refPPS.putUI(Tags.RefSOPClassUID,
					mpps.getString(Tags.SOPClassUID));
			refPPS.putUI(Tags.RefSOPInstanceUID,
					mpps.getString(Tags.SOPInstanceUID));
			DcmElement perfSeriesSq = mpps.get(Tags.PerformedSeriesSeq);
			DcmElement inSq = gpsps.putSQ(Tags.InputInformationSeq);
			Dataset inputInfo = inSq.addNewItem();
			inputInfo.putUI(Tags.StudyInstanceUID, siuid);
			DcmElement inSeriesSq = inputInfo.putSQ(Tags.RefSeriesSeq);
			for (int i = 0, n = perfSeriesSq.vm(); i < n; ++i) {
				Dataset perfSeries = perfSeriesSq.getItem(i);
				Dataset inSeries = inSeriesSq.addNewItem();
				inSeries.putUI(Tags.SeriesInstanceUID,
						perfSeries.getString(Tags.SeriesInstanceUID));
				DcmElement inRefSopSq = inSeries.putSQ(Tags.RefSOPSeq);
				DcmElement refImgSopSq = perfSeries.get(Tags.RefImageSeq);
				for (int j = 0, m = refImgSopSq.vm(); j < m; ++j) {
					inRefSopSq.addItem(refImgSopSq.getItem(j));
				}
				DcmElement refNoImgSopSq = perfSeries.get(Tags.RefNonImageCompositeSOPInstanceSeq);
				for (int j = 0, m = refNoImgSopSq.vm(); j < m; ++j) {
					inRefSopSq.addItem(refNoImgSopSq.getItem(j));
				}
			}
			if (!gpsps.contains(Tags.RefRequestSeq)) {
				DcmElement refRqSq = gpsps.putSQ(Tags.RefRequestSeq);
				for (int i = 0, n = ssaSq.vm(); i < n; ++i) {
					refRqSq.addItem(ssaSq.getItem(i).subSet(REF_REQUEST_TAGS));
				}
			}
	       	log.info("create workitem using template " + sb);
	       	log.debug(gpsps);
			return gpsps;
		} catch (FileNotFoundException e) {
			log.info("no workitem configured for procedure");
			log.info(codeItem);
		} catch (Exception e) {
			log.error("Failed to load workitem configuration from " + sb, e);
		}
		return null;        
	}

	private Dataset getMPPS(String iuid) {
		try {
			MPPSManager mgr = getMPPSManagerHome().create();
			try {
				Dataset ds = mgr.getMPPS(iuid);
				if (ds == null)
					log.error("No such MPPS - " + iuid);
				return ds;
			} finally {
				try {
					mgr.remove();
				} catch (Exception ignore) {
				}
			}
		} catch (Exception e) {
			log.error("Failed to load MPPS - " + iuid, e);
			return null;
		}
	}

	private MPPSManagerHome getMPPSManagerHome() throws HomeFactoryException {
        return (MPPSManagerHome) EJBHomeFactory.getFactory().lookup(
                MPPSManagerHome.class, MPPSManagerHome.JNDI_NAME);
    }

	private GPWLManagerHome getGPWLManagerHome() throws HomeFactoryException {
        return (GPWLManagerHome) EJBHomeFactory.getFactory().lookup(
                GPWLManagerHome.class, GPWLManagerHome.JNDI_NAME);
    }
}
