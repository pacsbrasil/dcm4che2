/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.movescu;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.Association;
import org.dcm4chex.archive.config.ForwardingRules;
import org.dcm4chex.archive.dcm.storescp.StoreScpService;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 27.08.2004
 *
 */
public class ForwardService extends ServiceMBeanSupport implements
        NotificationListener {

    private ObjectName storeScpServiceName;

    private int forwardPriority = 0;

    private ForwardingRules forwardingRules = new ForwardingRules("");

    private final NotificationFilter filter = new NotificationFilter() {

        public boolean isNotificationEnabled(Notification notif) {
            return StoreScpService.EVENT_TYPE.equals(notif.getType());
        }
    };

    public final String getForwardingRules() {
        return forwardingRules.toString();
    }

    public final void setForwardingRules(String forwardingRules) {
        this.forwardingRules = new ForwardingRules(forwardingRules);
    }

    public final int getForwardPriority() {
        return forwardPriority;
    }

    public final void setForwardPriority(int forwardPriority) {
        this.forwardPriority = forwardPriority;
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    protected void startService() throws Exception {
        server.addNotificationListener(storeScpServiceName, this, filter, null);
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(storeScpServiceName,
                this,
                filter,
                null);
    }

    public void handleNotification(Notification notif, Object handback) {
        Association assoc = (Association) notif.getUserData();
        Map storedStudiesInfo = (Map) assoc
                .getProperty(StoreScpService.EVENT_TYPE);
        if (storedStudiesInfo != null) {
            String[] destAETs = forwardingRules
                    .getForwardDestinationsFor(assoc);
            if (destAETs.length != 0) forward(destAETs, storedStudiesInfo);
        }
    }

    private void forward(String[] destAETs, Map storedStudiesInfo) {
        String[] studyIUIDs = new String[1];
        String[] seriesIUIDs = new String[1];
        ArrayList sopIUIDs = new ArrayList();
        Dataset refSOP = null;
        for (Iterator it = storedStudiesInfo.values().iterator(); it.hasNext();) {
            Dataset info = (Dataset) it.next();
            studyIUIDs[0] = info.getString(Tags.StudyInstanceUID);
            DcmElement refSeriesSeq = info.get(Tags.RefSeriesSeq);
            for (int i = 0, n = refSeriesSeq.vm(); i < n; ++i) {
                Dataset refSeries = refSeriesSeq.getItem(i);
                seriesIUIDs[0] = refSeries.getString(Tags.SeriesInstanceUID);
                DcmElement refSOPSeq = refSeries.get(Tags.RefSOPSeq);
                for (int j = 0, m = refSOPSeq.vm(); j < m; ++j) {
                    refSOP = refSOPSeq.getItem(j);
                    sopIUIDs.add(refSOP.getString(Tags.RefSOPInstanceUID));
                }
                String[] iuids = (String[]) sopIUIDs
                        .toArray(new String[sopIUIDs.size()]);
                sopIUIDs.clear();
                for (int k = 0; k < destAETs.length; ++k) {
                    try {
                        MoveOrder order = new MoveOrder(refSOP
                                .getString(Tags.RetrieveAET), ForwardingRules
                                .toAET(destAETs[k]), forwardPriority, null,
                                studyIUIDs, seriesIUIDs, iuids);
                        final long scheduledTime = ForwardingRules
                                .toScheduledTime(destAETs[k]);
                        log.info("Scheduling "
                                + order
                                + (scheduledTime > 0L ? (" for " + new Date(
                                        scheduledTime)) : " now"));
                        JMSDelegate
                                .getInstance(MoveOrder.QUEUE)
                                .queueMessage(order,
                                        JMSDelegate
                                                .toJMSPriority(forwardPriority),
                                        scheduledTime);
                    } catch (JMSException e) {
                        log.error("Failed to queue move order:", e);
                    }
                }
            }
        }
    }

}