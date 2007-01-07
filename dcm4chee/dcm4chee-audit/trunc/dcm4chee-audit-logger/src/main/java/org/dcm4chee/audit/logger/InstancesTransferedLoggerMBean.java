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
 * Agfa-Gevaert AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che2.audit.message.InstancesTransferedMessage;
import org.dcm4che2.audit.message.NetworkAccessPoint;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.Patient;
import org.dcm4che2.audit.message.Source;
import org.dcm4che2.audit.message.Study;
import org.dcm4chex.archive.common.SeriesStored;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jan 5, 2007
 */
@Service(objectName = "dcm4chee.archive.logger:name=InstancesTransferedLogger,type=service")
@Management(InstancesTransferedLogger.class)
public class InstancesTransferedLoggerMBean 
        implements InstancesTransferedLogger {
    private static final Logger auditlog = Logger.getLogger("audit");
    private static final Logger log = 
            Logger.getLogger(InstancesTransferedLoggerMBean.class);

    @Depends ("dcm4chee.archive:service=StoreScp")
    private ObjectName storeSCPName;
    
    @Depends ("dcm4chee.archive.logger:name=SecurityAlertLogger,type=service")
    private SecurityAlertLogger alertLogger;    

    private MBeanServer server;

    public void create() throws Exception {
        server = MBeanServerLocator.locate();
    }

    public void destroy() {
        server = null;
    }
    
    public void start() throws Exception {
        registerSeriesStoredListener();
        
    }
    public void stop() {
        unregisterSeriesStoredListener();    
    }

    private void registerSeriesStoredListener() 
            throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(SeriesStored.class.getName());
        server.addNotificationListener(storeSCPName, seriesStoredListener, f ,
                null);
    }

    private void unregisterSeriesStoredListener() {
        try {
            server.removeNotificationListener(storeSCPName, 
                    seriesStoredListener);
        } catch (Exception e) {
            log.warn("Failed to unregister SeriesStored Notification" +
                    "Listener from " + storeSCPName, e);
        }
    }

    private final NotificationListener seriesStoredListener = 
            new NotificationListener() {

        public void handleNotification(Notification notif, Object handback) {
            try {
                SeriesStored series = (SeriesStored) notif.getUserData();
                InstancesTransferedMessage msg = new InstancesTransferedMessage(
                        new InstancesTransferedMessage.AuditEvent.Create(),
                        mkRemoteSource(series), alertLogger.mkLocalDestination(),
                        mkPatient(series), mkStudy(series));
                InstancesTransferedLoggerMBean.auditlog.info(msg);
            } catch (Throwable th) {
                log.warn("Failed to emit Audit Log message for stored Series: ", th);
            }
        }};

    private Source mkRemoteSource(SeriesStored series) {
        String remoteAET = series.getCallingAET();
        InetAddress remoteAddr = series.getRemoteAddress();
        Source src;
        if (remoteAddr == null) {
            src = new Source(remoteAET);
        } else {
            String host = alertLogger.isDisableHostLookup() 
                    ? remoteAddr.getHostAddress()
                    : remoteAddr.getHostName();
            src = new Source(host);
            src.setNetworkAccessPoint(Character.isDigit(host.charAt(0))
                    ? (NetworkAccessPoint) new NetworkAccessPoint.IPAddress(host)
                    : (NetworkAccessPoint) new NetworkAccessPoint.HostName(host));
        }
        src.setAETitle(remoteAET);
        return src;
    }

    private Study mkStudy(SeriesStored seriesStored) {
        Dataset ian = seriesStored.getIAN();
        Dataset series = ian.getItem(Tags.RefSeriesSeq);
        DcmElement refSops = series.get(Tags.RefSOPSeq);
        HashMap cuids = new HashMap();
        for (int i = 0, n = refSops.countItems(); i < n; i++) {
            final Dataset refSop = refSops.getItem(i);
            final String cuid = refSop.getString(Tags.RefSOPClassUID);
            HashSet iuids = (HashSet) cuids.get(cuid);
            if (iuids == null) {
                cuids.put(cuid, iuids = new HashSet());
            }
            iuids.add(refSop.getString(Tags.RefSOPInstanceUID));
        }
        String accno = seriesStored.getAccessionNumber();
        Dataset pps = ian.getItem(Tags.RefPPSSeq);
        Study study = new Study(ian.getString(Tags.StudyInstanceUID));
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        if (accno != null) {
            desc.addAccession(accno);
        }
        if (pps != null) {
            desc.addMPPS(pps.getString(Tags.RefSOPInstanceUID));
        }
        for (Iterator iter = cuids.entrySet().iterator(); iter.hasNext();) {
            Map.Entry el = (Map.Entry) iter.next();
            ParticipantObjectDescription.SOPClass sopClass =
                    new ParticipantObjectDescription.SOPClass(
                            (String) el.getKey());
            sopClass.setNumberOfInstances(((HashSet) el.getValue()).size());
            desc.addSOPClass(sopClass);
        }
        study.addParticipantObjectDescription(desc); 
        return study;
    }

    private Patient mkPatient(SeriesStored seriesStored) {
        Patient pat = new Patient(seriesStored.getPatientID());
        pat.setParticipantObjectName(seriesStored.getPatientName());
        return pat;
    }

    
}
