/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mppsscu;

import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulator;
import org.dcm4chex.archive.ejb.interfaces.MPPSEmulatorHome;
import org.dcm4chex.archive.mbean.TimerSupport;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 26.02.2005
 */

public class MPPSEmulatorService extends TimerSupport implements
        NotificationListener {

    private static final String IN_PROGRESS = "IN PROGRESS";

    private static final String COMPLETED = "COMPLETED";
    
	private static final int[] MPPS_CREATE_TAGS = {
		//TODO
	};
	
	private static final int[] MPPS_SET_TAGS = {
		//TODO
	};
	
    private long checkSeriesInterval = 0L;

    private Integer checkSeriesIntervalID;

    private String calledAET;

    private String[] callingAETs;

    private long[] minSeriesAge;

    private long defMinSeriesAge = 60000L;

    private ObjectName mppsScuServiceName;

	private ObjectName mwlScuServiceName;


    public final String getCheckSeriesInterval() {
        return RetryIntervalls.formatIntervalZeroAsNever(checkSeriesInterval);
    }

    public void setCheckSeriesInterval(String interval) {
        this.checkSeriesInterval = RetryIntervalls
                .parseIntervalOrNever(interval);
        if (getState() == STARTED) {
            stopScheduler(checkSeriesIntervalID, this);
            checkSeriesIntervalID = startScheduler(checkSeriesInterval, this);
        }
    }

    public final ObjectName getMppsScuServiceName() {
        return mppsScuServiceName;
    }

    public final void setMppsScpServiceName(ObjectName mppsScuServiceName) {
        this.mppsScuServiceName = mppsScuServiceName;
    }
    
	public final ObjectName getMwlScuServiceName() {
		return mwlScuServiceName;
	}

	public final void setMwlScuServiceName(ObjectName mwlScuServiceName) {
		this.mwlScuServiceName = mwlScuServiceName;
	}

	public void handleNotification(Notification notification, Object handback) {
        try {
            emulateMPPS();
        } catch (Exception e) {
            log.error("emulateMPPS failed:", e);
        }
    }

    public int emulateMPPS() throws Exception {
        if (callingAETs == null)
            return 0;
        int num = 0;
        MPPSEmulator mppsEmulator = getMPPSEmulatorHome().create();
        try {
            for (int i = 0; i < callingAETs.length; ++i) {
                Dataset[] mpps = mppsEmulator.generateMPPS(callingAETs[i], minSeriesAge[i]);
                for (int j = 0; j < mpps.length; ++j) {
                	if (addMWLAttrs(mpps[j]) && createMPPS(mpps[j]) && updateMPPS(mpps[j]));
               			++num;
                }
            }
        } finally {
            try {
                mppsEmulator.remove();
            } catch (Exception ignore) {
            }
        }
        return num;
    }


	private boolean addMWLAttrs(Dataset mpps) {
		Dataset filter = DcmObjectFactory.getInstance().newDataset();
    	List mwlEntries = findMWLEntries(filter);
    	if (mwlEntries == null)
    		return false;
    	//TODO
    	return true;
	}

	private boolean createMPPS(Dataset mpps) {
    	mpps.putCS(Tags.PPSStatus, IN_PROGRESS);
    	int status = sendMPPS(mpps.subSet(MPPS_CREATE_TAGS), calledAET);
    	if (status != 0) {
    		log.error("Failed to create MPPS! Error status:"
    				+ Integer.toHexString(status));
    		return false;
    	}
		return true;
	}

	private boolean updateMPPS(Dataset mpps) {
    	mpps.putCS(Tags.PPSStatus, COMPLETED);
    	int status = sendMPPS(mpps.subSet(MPPS_SET_TAGS), calledAET);
    	if (status != 0) {
    		log.error("Failed to update MPPS! Error status:"
    				+ Integer.toHexString(status));
    		return false;
    	}
		return true;
	}
	
	private MPPSEmulatorHome getMPPSEmulatorHome() throws HomeFactoryException {
        return (MPPSEmulatorHome) EJBHomeFactory.getFactory().lookup(
                MPPSEmulatorHome.class, MPPSEmulatorHome.JNDI_NAME);
    }
    
    private int sendMPPS(Dataset mpps, String destination) {
		try {
			Integer status = (Integer) server.invoke(mppsScuServiceName, "sendMPPS",
					new Object[] { mpps, destination },
					new String[] { Dataset.class.getName(), String.class.getName() });
			return status.intValue();
		} catch (Exception x) {
			log.error("Exception occured in sendMPPS;", x);
		}
		return -1;
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
}
