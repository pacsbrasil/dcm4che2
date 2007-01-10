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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below. 
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

package org.dcm4chee.audit.logger;

import java.util.Iterator;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Id$
 * @since Jan 10, 2007
 */
@Service(objectName = "dcm4chee.archive.logger:name=MediaExportLogger,type=service")
@Management(MediaExportLogger.class)
public class MediaExportLoggerMBean implements MediaExportLogger {
    private static final Logger log = 
        Logger.getLogger(InstancesTransferredLoggerMBean.class);

    private MBeanServer server;
    private Set sources;

    public void create() throws Exception {
        server = MBeanServerLocator.locate();
    }
    
    public void destroy() {
        server = null;
    }

    public void start() throws Exception {
        registerMediaCreationListener();
    }

    public void stop() {
        unregisterMediaCreationListener();
    }

    private void registerMediaCreationListener() 
            throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(MediaCreationRequest.class.getName());
        sources = server.queryNames(
                new ObjectName("dcm4chee.cdw:service=MediaWriter"), null);
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            server.addNotificationListener((ObjectName) iter.next(),
                    mediaCreationListener, f, null);
        }
    }

    private void unregisterMediaCreationListener() {
        for (Iterator iter = sources.iterator(); iter.hasNext();) {
            try {
                server.removeNotificationListener((ObjectName) iter.next(),
                        mediaCreationListener);
            } catch (Exception e) {
                log.warn("Failed to unregister Media Creation Notification Listener", e);
            }
        }
    }
    
    private final NotificationListener mediaCreationListener = 
        new NotificationListener() {
    
    public void handleNotification(Notification notif, Object handback) {
        try {
            MediaCreationRequest mcrq = 
                    (MediaCreationRequest) notif.getUserData();
            //TODO
        } catch (Throwable th) {
            log.warn("Failed to emit Data Export Audit Log message: ", th);
        }
    }
    };
}
