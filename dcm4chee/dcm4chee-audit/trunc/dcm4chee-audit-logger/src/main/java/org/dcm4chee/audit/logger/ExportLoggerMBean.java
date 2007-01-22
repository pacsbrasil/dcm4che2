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
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che2.audit.message.DataExportMessage;
import org.dcm4che2.audit.message.DestinationMedia;
import org.dcm4che2.audit.message.NetworkAccessPoint;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.Patient;
import org.dcm4che2.audit.message.Source;
import org.dcm4che2.audit.message.Study;
import org.dcm4che2.audit.message.DataExportMessage.AuditEvent;
import org.dcm4che2.audit.message.ParticipantObjectDescription.SOPClass;
import org.dcm4chex.archive.notif.Export;
import org.dcm4chex.archive.notif.RIDExport;
import org.jboss.annotation.ejb.Depends;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SecurityAssociation;


/**
 * Export Audit Logger
 * 
 * @author Franz Willer <franz.willer@gwi-ag.com>
 * @version $Revision$ $Date$
 * @since Jan 2, 2007
 */
@Service(objectName = "dcm4chee.archive.logger:name=ExportLogger,type=service")
@Depends ("dcm4chee.archive.logger:name=SecurityAlertLogger,type=service")
@Management(ExportLogger.class)
public class ExportLoggerMBean implements ExportLogger, NotificationListener {

    private Logger log = 
            Logger.getLogger(ExportLoggerMBean.class);

    private boolean auditFailuresEnabled = true;
    
    private MBeanServer server;

    @Depends ("dcm4chee.archive:service=XDS-I")
    private ObjectName xdsiServicename;
    
    @Depends ("dcm4chee.archive:service=RIDService")
    private ObjectName ridServicename;

    /**
     * @return the auditFailures
     */
    public boolean isAuditFailures() {
        return auditFailuresEnabled;
    }

    /**
     * @param auditFailures the auditFailures to set
     */
    public void setAuditFailures(boolean auditFailures) {
        this.auditFailuresEnabled = auditFailures;
    }

     
    public void create() throws Exception {
        try {
            xdsiServicename = new ObjectName("dcm4chee.archive:service=XDS-I");
            ridServicename = new ObjectName("dcm4chee.archive:service=RIDService");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
        server = MBeanServerLocator.locate();
    }

    public void destroy() throws Exception {
        server = null;
    }
    
    public void start() throws Exception {
        registerRIDExportListerner();
        registerExportListeners();
    }

    public void stop() {
        unregisterRIDExportListerner();
        unregisterExportListeners();
    }

    private void registerRIDExportListerner() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(RIDExport.class.getName());
        server.addNotificationListener(ridServicename, ridExportListener, f , null);
    }

    private void unregisterRIDExportListerner() {
        try {
            server.removeNotificationListener( ridServicename, ridExportListener );
        } catch (Exception e) {
            log.warn("Failed to unregister RID Export Notification Listener", e);
        }
    }

    private void registerExportListeners() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType(Export.class.getName());
        server.addNotificationListener(xdsiServicename, this, f , null);
    }

    private void unregisterExportListeners() {
        ObjectName serviceName = null;
        try {
            server.removeNotificationListener( serviceName=xdsiServicename, this );
        } catch (Exception e) {
            log.warn("Failed to unregister Export Notification Listener(s) from " + serviceName, e);
        }
    }
    
    public void handleNotification(Notification notif, Object handback) {
      try {
        log.debug("Audit Export Notification notif:"+notif);
        Export export = (Export) notif.getUserData();
        AuditEvent event = new DataExportMessage.AuditEvent();
        if ( !export.isSuccess() ) {
            if ( auditFailuresEnabled ) {
                event.setEventOutcomeIndicator(AuditEvent.OutcomeIndicator.MINOR_FAILURE);
            } else {
                log.debug("Export failed! Audit log of this failure is disabled! "+export);
                return;
            }
        }
        log.debug("Audit Export Notification user data:"+export);
        String user = export.getUser();
        Source srcProcess = getSource( export.getSourceId(), export.getSourceHost() );
        Dataset kos = export.getKeyObject();
        log.debug("Export KOS:");log.debug(kos);
        DataExportMessage msg = getDataExportMessage(event, srcProcess, user, 
                getDestinationMedia(export), getPatient(kos));
        addStudies(kos, msg);
        LoggerUtils.log.info(msg);
      } catch ( Throwable t) {
          t.printStackTrace();
      }
    }

    private DataExportMessage getDataExportMessage(AuditEvent event, Source srcProcess, String user, DestinationMedia destMedia, Patient patient) {
        if ( user == null ) {
            Principal p = SecurityAssociation.getPrincipal();
            log.debug("principal:"+p);
            if (p != null) user = p.getName();
        }
        if (user != null) {
            Source srcUser = new Source(user);
            srcUser.setUserIsRequestor(true);
            srcProcess.setUserIsRequestor(false);
            return new DataExportMessage( event,srcUser,srcProcess, destMedia, patient);
        } else {
            srcProcess.setUserIsRequestor(true);
            return new DataExportMessage( event,srcProcess, destMedia, patient);
        }
    }

    private Source getSource(String id, String host) {
       if ( host != null ) {
            NetworkAccessPoint nap;
            try {
                nap = LoggerUtils.toNetworkAccessPoint( InetAddress.getByName(host));
                Source src = new Source( id != null ? id : nap.getNodeID() );
                src.setNetworkAccessPoint(nap);
            } catch (UnknownHostException e) {
                log.warn("Source host not found! Cant get NetworkAccessPoint for:"+host);
            }
        }
        return new Source(id);
        
    }

    private DestinationMedia getDestinationMedia(Export export) {
        DestinationMedia destMedia = new DestinationMedia(export.getDestinationID());
        String destHost = export.getDestHost();
        if ( destHost != null ) {
            NetworkAccessPoint nap;
            try {
                nap = LoggerUtils.toNetworkAccessPoint( InetAddress.getByName(destHost));
                destMedia.setNetworkAccessPoint(nap);
            } catch (UnknownHostException e) {
                log.warn("Destination host not found! Cant get NetworkAccessPoint for:"+destHost);
            }
        }
        return destMedia;
    }

    private Patient getPatient(Dataset kos) {
        Patient patient = new Patient(kos.getString(Tags.PatientID));
        patient.setPatientName(kos.getString(Tags.PatientName));
        return patient;
    }

    private DataExportMessage addStudies(Dataset kos, DataExportMessage msg) {
        if ( kos == null ) return msg;
        DcmElement sq = kos.get(Tags.CurrentRequestedProcedureEvidenceSeq);
        if ( sq != null ) {
            Dataset ds;
            Study study;
            for ( int i = 0,len=sq.countItems() ; i < len ; i++ ) {
                ds = sq.getItem(i);
                study = new Study(ds.getString(Tags.StudyInstanceUID));
                study.addParticipantObjectDescription(getParticipantObjectDescription(ds));
                msg.addStudy(study);
            }
        } else {
            String suid = kos.getString(Tags.StudyInstanceUID);
            if ( suid != null ) {
                msg.addStudy( new Study(suid) );
            }
        }
        return msg;
    }

    private ParticipantObjectDescription getParticipantObjectDescription(Dataset ds) {
        ParticipantObjectDescription descr = new ParticipantObjectDescription();
        if ( ds.getString(Tags.AccessionNumber) != null) {
            descr.addAccession(ds.getString(Tags.AccessionNumber));
        }
        DcmElement refSerSq = ds.get(Tags.RefSeriesSeq);
        Set<Entry<String, int[]>> entries = getSopClassInfo(refSerSq).entrySet();
        SOPClass sopClass;
        Entry<String, int[]> entry;
        for ( Iterator<Entry<String, int[]>> it = entries.iterator(); it.hasNext(); ) {
            entry = it.next();
            sopClass = new SOPClass(entry.getKey());
            sopClass.setNumberOfInstances(entry.getValue()[0]);
            descr.addSOPClass(sopClass);
        }
        return descr;
    }

    private Map<String, int[]> getSopClassInfo(DcmElement refSerSq) {
        Dataset item;
        DcmElement refSopSq;
        HashMap<String, int[]> mapSopClass = new HashMap<String, int[]>();
        String sopClassUID;
        int[] counter;
        for ( int i = 0,len=refSerSq.countItems() ; i < len ; i++ ) {
            item = refSerSq.getItem(i);
            refSopSq = item.get(Tags.RefSOPSeq);
            for ( int j = 0,lenj=refSopSq.countItems() ; j < lenj ; j++ ) {
                sopClassUID = refSopSq.getItem(j).getString(Tags.RefSOPClassUID);
                counter = mapSopClass.get(sopClassUID);
                if ( counter == null ) {
                    mapSopClass.put(sopClassUID, new int[]{1} );
                } else {
                    counter[0]++;
                }
            }
        }
        return mapSopClass;
    }
 
    private NotificationListener ridExportListener = new NotificationListener(){

        public void handleNotification(Notification notif, Object handback) {
            try {
                RIDExport export = (RIDExport) notif.getUserData();
                HttpServletRequest request = export.getRequest();
                Dataset ds = export.getDataset();
                Patient pat = new Patient(export.getPatId());
                if ( export.getPatName() != null ) 
                        pat.setPatientName(export.getPatName());
                DataExportMessage msg = new DataExportMessage(
                        new DataExportMessage.AuditEvent(), 
                        LoggerUtils.toLocalSource(request), 
                        LoggerUtils.toRemoteDestinationMedia(request), pat);
                String suid = ds.getString(Tags.StudyInstanceUID);
                if (suid != null) {
                    Study sty = new Study(suid);
                    String cuid = ds.getString(Tags.SOPClassUID);
                    if (cuid != null) {
                        ParticipantObjectDescription desc = 
                            new ParticipantObjectDescription();
                        ParticipantObjectDescription.SOPClass sopClass =
                            new ParticipantObjectDescription.SOPClass(cuid);
                        sopClass.setNumberOfInstances(1);
                        desc.addSOPClass(sopClass);
                        sty.addParticipantObjectDescription(desc);
                    }
                    msg.addStudy(sty);
                }
                LoggerUtils.log.info(msg);
            } catch (Throwable th) {
                log.warn("Failed to emit Data Export Audit Log message: ", th);
            }        
        }
        
        };   
 
}
