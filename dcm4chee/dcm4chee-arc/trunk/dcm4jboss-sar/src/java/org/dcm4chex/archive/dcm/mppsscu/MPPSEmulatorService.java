/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.mppsscu;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
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

    private long checkSeriesInterval = 0L;

    private Integer checkSeriesIntervalID;

    private String[] callingAETs;

    private long[] minSeriesAge;

    private long defMinSeriesAge = 60000L;

    private ObjectName mppsScuServiceName;

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
                num += mpps.length;
            }
        } finally {
            try {
                mppsEmulator.remove();
            } catch (Exception ignore) {
            }
        }
        return num;
    }


    private MPPSEmulatorHome getMPPSEmulatorHome() throws HomeFactoryException {
        return (MPPSEmulatorHome) EJBHomeFactory.getFactory().lookup(
                MPPSEmulatorHome.class, MPPSEmulatorHome.JNDI_NAME);
    }
}
