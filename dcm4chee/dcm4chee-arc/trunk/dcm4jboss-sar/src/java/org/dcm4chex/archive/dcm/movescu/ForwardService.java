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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.Association;
import org.dcm4cheri.util.StringUtils;
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
    private ObjectName editContentServiceName;
    
    private String[] editContentAETs;

    private int forwardPriority = 0;

    private ForwardingRules forwardingRules = new ForwardingRules("");

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

	/**
	 * @return Returns the editContentServiceName.
	 */
	public ObjectName getEditContentServiceName() {
		return editContentServiceName;
	}
	/**
	 * @param editContentServiceName The editContentServiceName to set.
	 */
	public void setEditContentServiceName(ObjectName editContentServiceName) {
		this.editContentServiceName = editContentServiceName;
	}
	
	public String getEditContentAETs() {
		if ( editContentAETs == null ) return " ";
		return StringUtils.toString( editContentAETs, '\\');
	}
	
	public void setEditContentAETs( String s ) {
		if ( s == null || s.trim().length() < 1 ) {
			editContentAETs = null;
		} else {
			editContentAETs = StringUtils.split( s, '\\');
		}
	}
	
    protected void startService() throws Exception {
        server.addNotificationListener(storeScpServiceName,
                this,
                StoreScpService.NOTIF_FILTER,
                null);
        server.addNotificationListener(editContentServiceName,
        		contentEditNotificationListener,
                null,
                "contentEdit");
    }

    protected void stopService() throws Exception {
        server.removeNotificationListener(storeScpServiceName,
        		contentEditNotificationListener,
                StoreScpService.NOTIF_FILTER,
                null);
        server.removeNotificationListener(editContentServiceName,
                this,
                null,
                "contentEdit");
    }

    public void handleNotification(Notification notif, Object handback) {
        Association assoc = (Association) notif.getUserData();
        Map ians = (Map) assoc
                .getProperty(StoreScpService.IANS_KEY);
        if (ians != null) {
            String[] destAETs = forwardingRules
                    .getForwardDestinationsFor(assoc);
            if (destAETs.length != 0) forward(destAETs, ians);
        }
    }

    private void forward(String[] destAETs, Map ians) {
        String[] studyIUIDs = new String[1];
        String[] seriesIUIDs = new String[1];
        ArrayList sopIUIDs = new ArrayList();
        Dataset refSOP = null;
        for (Iterator it = ians.values().iterator(); it.hasNext();) {
            Dataset ian = (Dataset) it.next();
            studyIUIDs[0] = ian.getString(Tags.StudyInstanceUID);
            DcmElement refSeriesSeq = ian.get(Tags.RefSeriesSeq);
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
                    MoveOrder order = new MoveOrder(refSOP
                            .getString(Tags.RetrieveAET), ForwardingRules
                            .toAET(destAETs[k]), forwardPriority, null,
                            studyIUIDs, seriesIUIDs, iuids);
                    try {
                        final long scheduledTime = ForwardingRules
                                .toScheduledTime(destAETs[k]);
                        log.info("Scheduling "
                                + order
                                + (scheduledTime > 0L ? (" for " + new Date(
                                        scheduledTime)) : " now"));
                        JMSDelegate.queue(MoveOrder.QUEUE, order, JMSDelegate
                                .toJMSPriority(forwardPriority), scheduledTime);
                    } catch (JMSException e) {
                        log.error("Failed to schedule " + order, e);
                    }
                }
            }
        }
    }
    
    public final NotificationListener contentEditNotificationListener = new NotificationListener() {

		/**
		 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
		 */
		public void handleNotification(Notification notif, Object ctx) {
			if ( editContentAETs == null || editContentAETs.length < 1 ) return;
			Object o = notif.getUserData();
			if ( o == null ) return;
			if ( log.isDebugEnabled() ) {
				log.debug("ContentEditNotification:"); 
				log.debug( o );
			}
			Map map = null;
			if ( o instanceof Dataset) {
				map = new HashMap();
				map.put("ds", o );
			} else if ( o instanceof Map ) {
				map = (Map) o;
			} else {
				log.error("Ignored! ContentEditNotification with wrong userObject type! "+o.getClass().getName());
				return;
			}
			forward( editContentAETs, map);
		}
    	
    };

}