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

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.Association;
import org.dcm4che2.audit.message.QueryMessage;
import org.dcm4che2.audit.message.QuerySOPClass;
import org.dcm4chex.archive.common.DatasetUtils;
import org.dcm4chex.archive.notif.Query;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Id$
 * @since Jan 8, 2007
 */
@Service(objectName = "dcm4chee.archive.logger:name=QueryLogger,type=service")
@Management(QueryLogger.class)
public class QueryLoggerMBean implements QueryLogger {

    private static final Logger log = 
            Logger.getLogger(QueryLoggerMBean.class);

    @Depends ("dcm4chee.archive:service=QueryRetrieveScp")
    private ObjectName qrSCPName;

    @Depends ("dcm4chee.archive:service=MWLFindScp")
    private ObjectName mwlSCPName;

    @Depends ("dcm4chee.archive:service=GPWLScp")
    private ObjectName gpwlSCPName;

    private MBeanServer server;

    public void create() throws Exception {
        server = MBeanServerLocator.locate();
    }

    public void destroy() {
        server = null;
    }
    
    public void start() throws Exception {
        registerQueryListener();
    }
    public void stop() {
        unregisterQueryListener();
    }

    private void registerQueryListener() 
            throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(Query.class.getName());
        server.addNotificationListener(qrSCPName, queryListener, f, null);
        server.addNotificationListener(mwlSCPName, queryListener, f, null);
        server.addNotificationListener(gpwlSCPName, queryListener, f, null);
    }

    private void unregisterQueryListener() {
        try {
            server.removeNotificationListener(qrSCPName, queryListener);
            server.removeNotificationListener(mwlSCPName, queryListener);
            server.removeNotificationListener(gpwlSCPName, queryListener);
        } catch (Exception e) {
            log.warn("Failed to unregister Query Notification Listener", e);
        }
    }

    private final NotificationListener queryListener = 
            new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            try {
                Query query = (Query) notif.getUserData();
                Association assoc = query.getAssociation();
                QueryMessage msg = new QueryMessage(
                        new QueryMessage.AuditEvent(),
                        LoggerUtils.toSource(true, assoc.getCallingAET(),
                                assoc.getSocket().getInetAddress()),
                        LoggerUtils.toDestination(false, assoc.getCalledAET(),
                                assoc.getSocket().getLocalAddress()),
                        new QuerySOPClass(query.getSopClassUID(),
                                UIDs.ExplicitVRLittleEndian,
                                DatasetUtils.toByteArray(query.getKeys())));
                LoggerUtils.log.info(msg);
            } catch (Throwable th) {
                log.warn("Failed to emit Instances Transferred Audit Log message: ", th);
            }
        }
    };
}
