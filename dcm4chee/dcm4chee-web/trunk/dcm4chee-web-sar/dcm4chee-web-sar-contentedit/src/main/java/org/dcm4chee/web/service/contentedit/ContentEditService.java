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
package org.dcm4chee.web.service.contentedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.dcm4che2.audit.message.AuditEvent;
import org.dcm4che2.audit.message.AuditMessage;
import org.dcm4che2.audit.message.InstancesAccessedMessage;
import org.dcm4che2.audit.message.ParticipantObject;
import org.dcm4che2.audit.message.ParticipantObjectDescription;
import org.dcm4che2.audit.message.StudyDeletedMessage;
import org.dcm4che2.audit.util.InstanceSorter;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.DicomEditLocal;
import org.dcm4chee.web.wicket.util.HttpUserInfo;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Jan 29, 2009
 */
public class ContentEditService extends ServiceMBeanSupport {

    private String NONE ="NONE";
    
    private Code rejectNoteCode = new Code();
    
    private DicomEditLocal dicomEdit;

    private boolean auditEnabled = true;

    private boolean forceNewRejNoteStudyIUID;
    
    private ObjectName rejNoteServiceName;
    private ObjectName ianScuServiceName;
        
    public String getRejectionNoteCode() {
        return rejectNoteCode.toString()+"\r\n";
    }

    public void setRejectionNoteCode(String code) {
        rejectNoteCode.parse(code);
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean auditEnabled) {
        this.auditEnabled = auditEnabled;
    }

    public boolean isForceNewRejNoteStudyIUID() {
        return forceNewRejNoteStudyIUID;
    }

    public void setForceNewRejNoteStudyIUID(boolean forceNewRejNoteStudyIUID) {
        this.forceNewRejNoteStudyIUID = forceNewRejNoteStudyIUID;
    }

    public ObjectName getRejectionNoteServiceName() {
        return rejNoteServiceName;
    }

    public void setRejectionNoteServiceName(ObjectName name) {
        this.rejNoteServiceName = name;
    }

    public ObjectName getIANScuServiceName() {
        return ianScuServiceName;
    }

    public void setIANScuServiceName(ObjectName name) {
        this.ianScuServiceName = name;
    }

    public DicomObject moveInstanceToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveInstanceToTrash(iuid);
        if (instances.isEmpty()) 
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
        processRejectionNote(kos);
        processIANs(entityTree);
        return kos;
    }

    public DicomObject[] moveInstancesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveInstancesToTrash(pks);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
            processRejectionNote(kos);
        }
        processIANs(entityTree);
        return rejNotes;
    }
    public DicomObject moveSeriesToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveSeriesToTrash(iuid);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
        processRejectionNote(kos);
        processIANs(entityTree);
        return kos;
    }
    public DicomObject[] moveSeriesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveSeriesToTrash(pks);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
            processRejectionNote(kos);
        }
        processIANs(entityTree);
        return rejNotes;
    }

    public DicomObject moveStudyToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveStudyToTrash(iuid);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logStudyDeleted(kos);
        processRejectionNote(kos);
        processIANs(entityTree);
        return kos;
    }
    public DicomObject[] moveStudiesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().moveStudiesToTrash(pks);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
            processRejectionNote(kos);
        }
        processIANs(entityTree);
        return rejNotes;
    }
    public DicomObject[] movePatientToTrash(String pid, String issuer) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().movePatientToTrash(pid, issuer);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree);
        return rejNotes;
    }
    public DicomObject[] movePatientsToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        Collection<Instance> instances = lookupDicomEditLocal().movePatientsToTrash(pks);
        if (instances.isEmpty())
            return null;
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = getEntityTree(instances);
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree);
        return rejNotes;
    }
    
    private DicomObject[] getRejectionNotes(Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree) {
        DicomObject[] rejNotes = new DicomObject[entityTree.size()];
        int i = 0;
        for ( Map<Study, Map<Series, List<Instance>>> studies : entityTree.values()) {
            rejNotes[i] = toRejectionNote(studies);
            log.info("Rejection Note! KOS:"+rejNotes[i++]);
        }
        return rejNotes;
    }

    private Map<Patient, Map<Study, Map<Series, List<Instance>>>> getEntityTree(
            Collection<Instance> instances) {
        Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree = 
            new HashMap<Patient, Map<Study, Map<Series, List<Instance>>>>();
        for (Instance i : instances) {
            this.updateEntityTree(entityTree, i);
        }
        return entityTree;
    }
    
    private void updateEntityTree(Map<Patient, Map<Study, Map<Series, List<Instance>>>> iuids,
            Instance instance) {
        Series series = instance.getSeries();
        Study study = series.getStudy();
        Patient pat = study.getPatient();
        Map<Study, Map<Series, List<Instance>>> mapStudies = iuids.get(pat);
        if (mapStudies == null) {
            mapStudies = new HashMap<Study, Map<Series, List<Instance>>>();
            iuids.put(pat, mapStudies);
        }
        Map<Series, List<Instance>> mapSeries = mapStudies.get(study);
        List<Instance> instances;
        if (mapSeries == null) {
            mapSeries = new HashMap<Series, List<Instance>>();
            instances = new ArrayList<Instance>();
            mapSeries.put(series, instances);
            mapStudies.put(study, mapSeries);
        } else {
            instances = mapSeries.get(series);
            if (instances == null) {
                instances = new ArrayList<Instance>();
                mapSeries.put(series, instances);
            }
        }
        instances.add(instance);
    }
    
    private DicomObject toRejectionNote(Map<Study, Map<Series, List<Instance>>> entityTree) {
        String suid = forceNewRejNoteStudyIUID ? UIDUtils.createUID() : 
            entityTree.keySet().iterator().next().getStudyInstanceUID(); 
        DicomObject kos = newKeyObject(suid);
        DicomElement crpeSeq = kos.putSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
        entityTree.keySet().iterator().next().getPatient().getAttributes().copyTo(kos);
        for (Map.Entry<Study, Map<Series, List<Instance>>> entry : entityTree.entrySet() ) {
            addProcedureEvidenceSequenceItem(crpeSeq, entry.getKey(), entry.getValue());
        }
        return kos;
    }
    
    private DicomObject newKeyObject(String studyIUID) {
        DicomObject kos = new BasicDicomObject();
        kos.putString(Tag.StudyInstanceUID,VR.UI, studyIUID);
        kos.putString(Tag.SeriesInstanceUID,VR.UI, UIDUtils.createUID());
        kos.putString(Tag.SOPInstanceUID,VR.UI, UIDUtils.createUID());
        kos.putString(Tag.SOPClassUID,VR.UI, UID.KeyObjectSelectionDocumentStorage);
        kos.putString(Tag.Modality, VR.CS, "KO");
        kos.putInt(Tag.InstanceNumber, VR.IS, 1);
        kos.putDate(Tag.ContentDate, VR.DA, new Date());
        kos.putDate(Tag.ContentTime, VR.TM, new Date());
        DicomElement cncSeq = kos.putSequence(Tag.ConceptNameCodeSequence);
        cncSeq.addDicomObject(rejectNoteCode.toCodeItem());
        kos.putString(Tag.ValueType, VR.CS, "CONTAINER");
        DicomElement tmplSeq = kos.putSequence(Tag.ContentTemplateSequence);
        DicomObject tmplItem = new BasicDicomObject();
        tmplItem.putString(Tag.TemplateIdentifier, VR.CS, "2010");
        tmplItem.putString(Tag.MappingResource, VR.CS, "DCMR");
        tmplSeq.addDicomObject(tmplItem);
        kos.putSequence(Tag.ReferencedPerformedProcedureStepSequence);
        kos.putString(Tag.SeriesDescription, VR.LO, "Rejection Note");
        return kos;
    }
        
    private void addProcedureEvidenceSequenceItem(DicomElement crpeSeq, Study study, Map<Series, List<Instance>> series) {
        DicomObject item = new BasicDicomObject();
        crpeSeq.addDicomObject(item);
        item.putString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        DicomElement refSeriesSeq = item.putSequence(Tag.ReferencedSeriesSequence);
        DicomElement refSopSeq;
        DicomObject refSeriesSeqItem, refSopSeqItem;
        for ( Map.Entry<Series, List<Instance>> instances : series.entrySet()) {
            refSeriesSeqItem = new BasicDicomObject();
            refSeriesSeq.addDicomObject(refSeriesSeqItem);
            refSeriesSeqItem.putString(Tag.SeriesInstanceUID, VR.UI, instances.getKey().getSeriesInstanceUID());
            refSopSeq = refSeriesSeqItem.putSequence(Tag.ReferencedSOPSequence);
            for ( Instance inst : instances.getValue()) {
                refSopSeqItem = new BasicDicomObject();
                refSopSeq.addDicomObject(refSopSeqItem);
                refSopSeqItem.putString(Tag.ReferencedSOPInstanceUID, VR.UI, inst.getSOPInstanceUID());
                refSopSeqItem.putString(Tag.ReferencedSOPClassUID, VR.UI, inst.getSOPClassUID());
            }
            if (study == null)
                study = instances.getValue().get(0).getSeries().getStudy();
        }
    }
    
    private DicomObject makeIAN(Study study, Map<Series, List<Instance>> mapSeries) {
        log.debug("makeIAN: studyIUID:" + study.getStudyInstanceUID());
        Patient pat = study.getPatient();
        DicomObject ian = new BasicDicomObject();
        ian.putString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        ian.putString(Tag.AccessionNumber, VR.SH, study.getAccessionNumber());
        ian.putString(Tag.PatientID, VR.LO, pat.getPatientID());
        ian.putString(Tag.IssuerOfPatientID, VR.LO, pat.getIssuerOfPatientID());
        ian.putString(Tag.PatientName, VR.PN, pat.getPatientName());
        DicomElement refPPSSeq = ian.putSequence(Tag.ReferencedPerformedProcedureStepSequence);
        HashSet<String> mppsuids = new HashSet<String>();
        DicomElement refSeriesSeq = ian.putSequence(Tag.ReferencedSeriesSequence);

        for (Map.Entry<Series, List<Instance>> entry : mapSeries.entrySet() ) {
            Series sl = entry.getKey();
            MPPS mpps = sl.getModalityPerformedProcedureStep();
            if (mpps != null) {
                String mppsuid = mpps.getSopInstanceUID();
                if (mppsuids.add(mppsuid)) {
                    DicomObject refmpps = new BasicDicomObject();
                    refPPSSeq.addDicomObject(refmpps);
                    refmpps.putString(Tag.ReferencedSOPClassUID, VR.UI, UID.ModalityPerformedProcedureStepSOPClass);
                    refmpps.putString(Tag.ReferencedSOPInstanceUID, VR.UI, mppsuid);
                    refmpps.putSequence(Tag.PerformedWorkitemCodeSequence);
                }
            }
            DicomObject refSerItem = new BasicDicomObject();
            refSeriesSeq.addDicomObject(refSerItem);
            refSerItem.putString(Tag.SeriesInstanceUID, VR.UI, sl.getSeriesInstanceUID());
            DicomElement refSopSeq = refSerItem.putSequence(Tag.ReferencedSOPSequence);
            for (Instance instance : entry.getValue()) {
                DicomObject refSopItem = new BasicDicomObject();
                refSopSeq.addDicomObject(refSopItem);
                refSopItem.putString(Tag.RetrieveAETitle, VR.AE, instance.getRetrieveAETs());
                refSopItem.putString(Tag.InstanceAvailability, VR.CS, "UNAVAILABLE");
                refSopItem.putString(Tag.ReferencedSOPClassUID, VR.UI, instance.getSOPClassUID());
                refSopItem.putString(Tag.ReferencedSOPInstanceUID, VR.UI, instance.getSOPInstanceUID());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("IAN:"+ ian);
        }
        return ian;
    }
    
    private DicomEditLocal lookupDicomEditLocal() {
        if ( dicomEdit == null ) {
            try {
                InitialContext jndiCtx = new InitialContext();
                dicomEdit = (DicomEditLocal) jndiCtx.lookup(DicomEditLocal.JNDI_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dicomEdit;
    }
    
    private void logInstancesAccessed(DicomObject kos,
            AuditEvent.ActionCode actionCode, boolean addIUID, String detailMessage) {
        if (!auditEnabled )
            return;
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                .isEnableDNSLookups());
        log.debug("log instances Accessed! actionCode:" + actionCode);
        try {
            InstancesAccessedMessage msg = new InstancesAccessedMessage(
                    actionCode);
            msg.addUserPerson(userInfo.getUserId(), null, null, userInfo
                    .getHostName(), true);
            msg.addPatient(kos.getString(Tag.PatientID), kos.getString(Tag.PatientName));
            ParticipantObject study;
            DicomElement crpeSeq = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence);
            DicomObject item;
            for ( int i = 0, len = crpeSeq.countItems() ; i < len ; i++ ) {
                item = crpeSeq.getDicomObject(i);
                study = msg.addStudy(item.getString(Tag.StudyInstanceUID),
                        getStudyDescription(item, addIUID));
                if ( detailMessage != null )
                    study.addParticipantObjectDetail("Description", getStudySeriesDetail(detailMessage, item));
            }
            msg.validate();
            Logger.getLogger("auditlog").info(msg);
        } catch (Exception x) {
            log.warn("Audit Log 'Instances Accessed' (actionCode:" + actionCode
                    + ") failed:", x);
        }
    }

    private String getStudySeriesDetail(String detailMessage, DicomObject crpeSeqItem) {
        DicomElement refSeries = crpeSeqItem.get(Tag.ReferencedSeriesSequence);
        StringBuffer sb = new StringBuffer();
        sb.append(detailMessage);
        int len = refSeries.countItems();
        if ( len > 0 ) {
            sb.append(refSeries.getDicomObject(0).getString(Tag.SeriesInstanceUID));
            for (int i = 1; i < len; i++) {
                sb.append(", ").append(refSeries.getDicomObject(i).getString(Tag.SeriesInstanceUID));
            }
        }
        return sb.toString();
    }

    private void logStudyDeleted(DicomObject kos) {
        if (!auditEnabled)
            return;
        HttpUserInfo userInfo = new HttpUserInfo(AuditMessage
                .isEnableDNSLookups());
        try {
            String patId = kos.getString(Tag.PatientID);
            String patName = kos.getString(Tag.PatientName);
            StudyDeletedMessage msg; 
            DicomElement crpeSeq = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence);
            for ( int i = 0, len = crpeSeq.countItems() ; i < len ; i++ ) {
                msg = new StudyDeletedMessage();
                msg.addUserPerson(userInfo.getUserId(), null, null, userInfo
                        .getHostName(), true);
                msg.addPatient(patId, patName);
                DicomObject crpeSeqItem = kos.get(Tag.CurrentRequestedProcedureEvidenceSequence).getDicomObject(i);
                msg.addStudy(crpeSeqItem.getString(Tag.StudyInstanceUID),
                        getStudyDescription(crpeSeqItem, true));
                msg.validate();
                Logger.getLogger("auditlog").info(msg);
            }
        } catch (Exception x) {
            log.warn("Audit Log 'Study Deleted' failed:", x);
        }
    }

    private ParticipantObjectDescription getStudyDescription(DicomObject crpeSeqItem, boolean addIUID) {
        ParticipantObjectDescription desc = new ParticipantObjectDescription();
        String accNr = crpeSeqItem.getString(Tag.AccessionNumber);
        if (accNr != null)
            desc.addAccession(accNr);
        addSOPClassInfo(desc, crpeSeqItem, addIUID);
        return desc;
    }

    private void addSOPClassInfo(ParticipantObjectDescription desc,
            DicomObject studyMgtDs, boolean addIUID) {
        DicomElement refSeries = studyMgtDs.get(Tag.ReferencedSeriesSequence);
        if (refSeries == null)
            return;
        String suid = studyMgtDs.getString(Tag.StudyInstanceUID);
        InstanceSorter sorter = new InstanceSorter();
        DicomObject ds;
        DicomElement refSopSeq;
        for (int i = 0, len = refSeries.countItems(); i < len; i++) {
            refSopSeq = refSeries.getDicomObject(i).get(Tag.ReferencedSOPSequence);
            if (refSopSeq != null) {
                for (int j = 0, jlen = refSopSeq.countItems(); j < jlen; j++) {
                    ds = refSopSeq.getDicomObject(j);
                    sorter.addInstance(suid,
                            ds.getString(Tag.ReferencedSOPClassUID),
                            ds.getString(Tag.ReferencedSOPInstanceUID), null);
                }
            }
        }
        
        for (String cuid : sorter.getCUIDs(suid)) {
            ParticipantObjectDescription.SOPClass sopClass = new ParticipantObjectDescription.SOPClass(cuid);
            sopClass.setNumberOfInstances(sorter.countInstances(suid, cuid));
            if ( addIUID ) {
                for ( String iuid : sorter.getIUIDs(suid, cuid) ) {
                    sopClass.addInstance(iuid);
                }
            }
            desc.addSOPClass(sopClass);
        }
    }
    
    private void processRejectionNote(DicomObject rejNote) throws InstanceNotFoundException, MBeanException, ReflectionException {
        log.info("RejectionNote KOS:"+rejNote);
        server.invoke(rejNoteServiceName, "scheduleRejectionNote", 
                new Object[]{rejNote}, new String[]{DicomObject.class.getName()});
    }
    
    private void processIANs(Map<Patient, Map<Study, Map<Series, List<Instance>>>> entityTree) throws InstanceNotFoundException, MBeanException, ReflectionException {
        for (Map<Study, Map<Series, List<Instance>>> studyMap : entityTree.values()) {
            for ( Map.Entry<Study, Map<Series, List<Instance>>> studyEntry: studyMap.entrySet()) {
                DicomObject ian = makeIAN(studyEntry.getKey(), studyEntry.getValue());
                log.info("IAN:"+ian);
                server.invoke(ianScuServiceName, "scheduleIAN", 
                        new Object[]{ian}, new String[]{DicomObject.class.getName()});
            }
        }
    }
    
}

