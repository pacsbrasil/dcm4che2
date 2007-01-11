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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;
import org.dcm4che2.audit.message.DataExportMessage;
import org.dcm4che2.audit.message.DestinationMedia;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.Patient;
import org.dcm4che2.audit.message.Study;
import org.dcm4che2.audit.message.DataExportMessage.AuditEvent;
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
                new ObjectName("dcm4chee.cdw:service=MediaWriter,*"), null);
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
            DirReader dirreader = DirBuilderFactory.getInstance().newDirReader(
                    mcrq.getDicomDirFile());            
            DirRecord patrec = dirreader.getFirstRecord();
            DataExportMessage msg = new DataExportMessage(toEvent(mcrq) , 
                    LoggerUtils.toSource(true, mcrq.getRequestAET(), 
                            mcrq.getRequestIP()), toDestinationMedia(mcrq), 
                            toPatient(patrec) );
            addStudies(msg, patrec);
            while ((patrec = patrec.getNextSibling()) != null) {
                msg.addPatient(toPatient(patrec));
                addStudies(msg, patrec);
            }
            LoggerUtils.log.info(msg);
        } catch (Throwable th) {
            log.warn("Failed to emit Data Export Audit Log message: ", th);
        }
    }

    };

    private static Patient toPatient(DirRecord patrec) {
        Dataset ds = patrec.getDataset();
        return new Patient(ds.getString(Tags.PatientID))
                .setPatientName(ds.getString(Tags.PatientName));
    }

    private static void addStudies(DataExportMessage msg, DirRecord patrec) 
            throws IOException {
        for (DirRecord styrec = patrec.getFirstChild(); styrec != null;
                styrec = styrec.getNextSibling()) {
            msg.addStudy(toStudy(styrec));
        }
    }

    private static Study toStudy(DirRecord styrec) throws IOException {
        Dataset styattrs = styrec.getDataset();
        HashMap cuids = new HashMap();
        for (DirRecord serrec = styrec.getFirstChild(); serrec != null;
                serrec = serrec.getNextSibling()) {
            for (DirRecord instrec = serrec.getFirstChild(); instrec != null;
                    instrec = instrec.getNextSibling()) {
                Dataset instattrs = instrec.getDataset();
                String cuid = instattrs.getString(Tags.RefSOPClassUIDInFile);
                ArrayList iuids = (ArrayList) cuids.get(cuid);
                if (iuids == null) {
                    cuids.put(cuid, iuids = new ArrayList());
                }
                iuids.add(instattrs.getString(Tags.RefSOPInstanceUIDInFile));
            }           
        }
        Study sty = new Study(styattrs.getString(Tags.StudyInstanceUID));
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        for (Iterator iter = cuids.entrySet().iterator(); iter.hasNext();) {
            Map.Entry el = (Map.Entry) iter.next();
            ParticipantObjectDescription.SOPClass sopClass =
                    new ParticipantObjectDescription.SOPClass(
                            (String) el.getKey());
            sopClass.setNumberOfInstances(((ArrayList) el.getValue()).size());
            desc.addSOPClass(sopClass);
        }
        sty.addParticipantObjectDescription(desc);
        return sty ;
    }

    private static DestinationMedia toDestinationMedia(MediaCreationRequest mcrq) {
        String id = mcrq.getFilesetID();
        String uid = mcrq.getFilesetUID();
        DestinationMedia dest = new DestinationMedia(
                id != null && id.length() > 0 ? id : uid);
        dest.setAlternativeUserID(uid);
        return dest;
    }

    private static DataExportMessage.AuditEvent toEvent(MediaCreationRequest mcrq) {
        DataExportMessage.AuditEvent event = new DataExportMessage.AuditEvent();
        if (mcrq.isFailed()) {
            event.setEventOutcomeIndicator(AuditEvent.OutcomeIndicator.MINOR_FAILURE);
        }
        return event;
    }

}
