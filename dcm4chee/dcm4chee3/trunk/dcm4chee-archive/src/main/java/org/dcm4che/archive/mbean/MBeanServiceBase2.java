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
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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
 
package org.dcm4che.archive.mbean;

import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

/**
 * An abstract base class for MBean services.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public abstract class MBeanServiceBase2 extends NotificationBroadcasterSupport implements MBeanRegistration {
    protected Logger log = Logger.getLogger(getClass().getName());

    private boolean started;

    private boolean autostart;

    private AtomicLong sequenceNumber;

    protected MBeanServer server;

    protected ObjectName serviceName;

    /**
     * Return the next sequence number for use in notifications.
     * 
     * @return a <code>long</code> value
     */
    protected long getNextNotificationSequenceNumber() {
        return sequenceNumber.incrementAndGet();
    }

    public MBeanServer getServer() {
        return server;
    }

    public void setMBeanServer(MBeanServer mbeanServer) {
        this.server = mbeanServer;
    }

    /**
     * @return the serviceName
     */
    public ObjectName getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName
     *            the serviceName to set
     */
    public void setServiceName(ObjectName objectName) {
        this.serviceName = objectName;
    }

    @ManagedAttribute(defaultValue = "true")
    public void setAutostart(boolean start) {
        this.autostart = start;
    }

    /**
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    public void postDeregister() {
        stop();
    }

    @ManagedOperation
    public void stop() {
        if (!started)
            return;

        try {
            stopService();
        }
        catch (Exception e) {
            log.error("Could not stop " + getServiceName(), e);
        }
        
        started = false;
    }

    /**
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    public void postRegister(Boolean registrationDone) {
        start();
    }

    @ManagedOperation
    public void start() {
        if (!autostart || started)
            return;

        try {
            startService();
        }
        catch (Exception e) {
            log.error("Could not start " + getServiceName(), e);
        }
    }

    /**
     * Start the service
     */
    protected abstract void startService() throws Exception;

    /**
     * Stop the service
     */
    protected abstract void stopService() throws Exception;

    /**
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception {
    }

    /**
     * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer,
     *      javax.management.ObjectName)
     */
    public ObjectName preRegister(MBeanServer mbServer, ObjectName name) throws Exception {
        return null;
    }

    protected boolean isStarted() {
        return started;
    }

    /**
     * @return the log
     */
    public Logger getLogger() {
        return log;
    }
}
