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

package org.dcm4chex.archive.mbean;

import java.util.Date;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.timer.TimerNotification;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version Revision $Date$
 * @since 06.02.2005
 */

public class TimerSupport {
    public static String DEFAULT_TIMER_NAME = "jboss:service=Timer";
    private final ServiceMBeanSupport service;
    private ObjectName mTimer;

    private static class NotificationFilter implements
            javax.management.NotificationFilter {

		private static final long serialVersionUID = 3257853160218178097L;

		private Integer mId;

        /**
         * Create a Filter.
         * @param id the Scheduler id
         */
        public NotificationFilter(Integer pId) {
            mId = pId;
        }

        /**
         * Determine if the notification should be sent to this Scheduler
         */
        public boolean isNotificationEnabled(Notification notification) {
            if (notification instanceof TimerNotification) {
                TimerNotification lTimerNotification = (TimerNotification) notification;
                return lTimerNotification.getNotificationID().equals(mId);
            }
            return false;
        }
    }

    public TimerSupport(ServiceMBeanSupport service) {
        this.service = service;
    }
    
    public void init() throws Exception {
        // Create Timer MBean if need be

        mTimer = new ObjectName(DEFAULT_TIMER_NAME);

        MBeanServer server = service.getServer();
        if (!server.isRegistered(mTimer)) {
            server.createMBean("javax.management.timer.Timer", mTimer);
        }
        if (!((Boolean) server.getAttribute(mTimer, "Active"))
                .booleanValue()) {
            // Now start the Timer
            server.invoke(mTimer, "start", new Object[] {},
                    new String[] {});
        }
    }

    public Integer startScheduler(String name, long period, NotificationListener listener) {
        if (period <= 0L) return null;
        Logger log = service.getLog();
        try {
            // delay start of scheduler, because in JBoss-4.0.4 the EntityContainer
            // cannot be used immediately after it's been started.
            // Should be fixed in JBoss-4.0.5            
            log.info("Start Scheduler " + name + " with period of " + period 
                    + "ms in 1 min.");
            Date now = new Date(System.currentTimeMillis() + 60000);
            MBeanServer server = service.getServer();
            Integer id = (Integer) server.invoke(
                    mTimer,
                    "addNotification",
                    new Object[] { "Schedule", "Scheduler Notification", null,
                            now, new Long(period) },
                    new String[] { String.class.getName(),
                            String.class.getName(), Object.class.getName(),
                            Date.class.getName(), Long.TYPE.getName() });
            server.addNotificationListener(mTimer, listener,
                    new TimerSupport.NotificationFilter(id), null);
            return id;
        } catch (Exception e) {
            log.error("operation failed", e);
        }
        return null;
    }

    public void stopScheduler(String name, Integer id, NotificationListener listener) {
        if (id == null) return;
        Logger log = service.getLog();
        try {
            log.info("Stop Scheduler " + name);
            MBeanServer server = service.getServer();
            server.removeNotificationListener(mTimer, listener);
            server.invoke(mTimer, "removeNotification",
                    new Object[] { id },
                    new String[] { Integer.class.getName() });
        } catch (Exception e) {
            log.error("operation failed", e);
        }
    }
}
