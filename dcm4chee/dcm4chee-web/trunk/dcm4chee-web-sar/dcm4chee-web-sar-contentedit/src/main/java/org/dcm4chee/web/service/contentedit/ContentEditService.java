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
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.entity.Code;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.web.dao.DicomEditLocal;
import org.dcm4chee.web.dao.vo.EntityTree;
import org.dcm4chee.web.service.common.HttpUserInfo;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gmail.com
 * @version $Revision$ $Date$
 * @since Jan 29, 2009
 */
public class ContentEditService extends ServiceMBeanSupport {

    private String NONE ="NONE";
    
    private Code rejectNoteCode = new Code();

    private String[] moveDestinationAETs;
    
    private DicomEditLocal dicomEdit;

    private boolean auditEnabled = true;

    private boolean forceNewRejNoteStudyIUID;
    
    private ObjectName rejNoteServiceName;
    private ObjectName ianScuServiceName;
    private ObjectName hl7sendServiceName;
    private ObjectName moveScuServiceName;

    private boolean processIAN;
    private boolean processRejNote;
        
    public String getRejectionNoteCode() {
        return rejectNoteCode.toString()+"\r\n";
    }

    public void setRejectionNoteCode(String code) {
        rejectNoteCode = new Code(code);
    }

    public boolean isProcessIAN() {
        return processIAN;
    }

    public void setProcessIAN(boolean processIAN) {
        this.processIAN = processIAN;
    }

    public boolean isProcessRejNote() {
        return processRejNote;
    }

    public void setProcessRejNote(boolean processRejNote) {
        this.processRejNote = processRejNote;
    }
    
    public String getMoveDestinationAETs() {
        return moveDestinationAETs == null ? NONE : StringUtils.join(moveDestinationAETs, '\\');
    }
    public void setMoveDestinationAETs(String aets) {
        this.moveDestinationAETs = NONE.equals(aets) ? null : StringUtils.split(aets, '\\');
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

    public ObjectName getHl7sendServiceName() {
        return hl7sendServiceName;
    }

    public void setMoveScuServiceName(ObjectName name) {
        this.moveScuServiceName = name;
    }

    public ObjectName getMoveScuServiceName() {
        return moveScuServiceName;
    }

    public void setHl7sendServiceName(ObjectName hl7sendServiceName) {
        this.hl7sendServiceName = hl7sendServiceName;
    }
    
    public DicomObject moveInstanceToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveInstanceToTrash(iuid);
        if (entityTree.isEmpty()) 
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }

    public DicomObject[] moveInstancesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveInstancesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances:");
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject moveSeriesToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(iuid);
        if (entityTree.isEmpty())
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }
    public DicomObject[] moveSeriesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveSeriesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Deleted Series:");
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }

    public DicomObject moveStudyToTrash(String iuid) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveStudyToTrash(iuid);
        if (entityTree.isEmpty())
            return null;
        DicomObject kos = getRejectionNotes(entityTree)[0];
        logStudyDeleted(kos);
        processRejectionNote(kos);
        processIANs(entityTree, Availability.UNAVAILABLE);
        return kos;
    }
    public DicomObject[] moveStudiesToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().moveStudiesToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
            processRejectionNote(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject[] movePatientToTrash(String pid, String issuer) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().movePatientToTrash(pid, issuer);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }
    public DicomObject[] movePatientsToTrash(long[] pks) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree entityTree = lookupDicomEditLocal().movePatientsToTrash(pks);
        if (entityTree.isEmpty())
            return null;
        DicomObject[] rejNotes = getRejectionNotes(entityTree);
        for (DicomObject kos : rejNotes) {
            logStudyDeleted(kos);
        }
        processIANs(entityTree, Availability.UNAVAILABLE);
        return rejNotes;
    }

    public int moveInstancesToSeries(long[] instPks, long seriesPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveInstancesToSeries(instPks, seriesPk);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }

    public int moveInstanceToSeries(String sopIUID, String seriesIUID) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveInstanceToSeries(sopIUID, seriesIUID);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }
    
    public int moveSeriesToStudy(long[] seriesPks, long studyPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveSeriesToStudy(seriesPks, studyPk);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }

    public int moveSeriesToStudy(String seriesIUID, String studyIUID) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveSeriesToStudy(seriesIUID, studyIUID);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }

    public int moveStudiesToPatient(long[] studyPks, long patPk) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveStudiesToPatient(studyPks, patPk);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }
    public int moveStudyToPatient(String studyIUID, String patId, String issuer) throws InstanceNotFoundException, MBeanException, ReflectionException {
        EntityTree[] entityTree = lookupDicomEditLocal().moveStudyToPatient(studyIUID, patId, issuer);
        processAfterMoveEntities(entityTree);
        return entityTree[0].getAllInstances().size();
    }

    private void processAfterMoveEntities(EntityTree[] entityTree)
        throws InstanceNotFoundException, MBeanException,ReflectionException {
        if (!entityTree[0].isEmpty()) { 
            DicomObject[] rejNotes = getRejectionNotes(entityTree[0]);
            for (DicomObject kos : rejNotes) {
                logInstancesAccessed(kos, InstancesAccessedMessage.DELETE, true, "Referenced Series of deleted Instances for move entities:");
                processRejectionNote(kos);
            }
            processIANs(entityTree[0], Availability.UNAVAILABLE);
            processIANs(entityTree[1], Availability.ONLINE);
            processMoveRequests(entityTree[1]);
        }
    }


    private DicomObject[] getRejectionNotes(EntityTree entityTree) {
        Map<Patient, Map<Study, Map<Series, Set<Instance>>>> entityTreeMap = entityTree.getEntityTreeMap();
        DicomObject[] rejNotes = new DicomObject[entityTreeMap.size()];
        int i = 0;
        for ( Map<Study, Map<Series, Set<Instance>>> studies : entityTreeMap.values()) {
            rejNotes[i] = toRejectionNote(studies);
            log.info("Rejection Note! KOS:"+rejNotes[i++]);
        }
        return rejNotes;
    }
    
    private DicomObject toRejectionNote(Map<Study, Map<Series, Set<Instance>>> entityTree) {
        String suid = forceNewRejNoteStudyIUID ? UIDUtils.createUID() : 
            entityTree.keySet().iterator().next().getStudyInstanceUID(); 
        DicomObject kos = newKeyObject(suid);
        DicomElement crpeSeq = kos.putSequence(Tag.CurrentRequestedProcedureEvidenceSequence);
        entityTree.keySet().iterator().next().getPatient().getAttributes().copyTo(kos);
        for (Map.Entry<Study, Map<Series, Set<Instance>>> entry : entityTree.entrySet() ) {
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
        
    private void addProcedureEvidenceSequenceItem(DicomElement crpeSeq, Study study, Map<Series, Set<Instance>> series) {
        DicomObject item = new BasicDicomObject();
        crpeSeq.addDicomObject(item);
        item.putString(Tag.StudyInstanceUID, VR.UI, study.getStudyInstanceUID());
        DicomElement refSeriesSeq = item.putSequence(Tag.ReferencedSeriesSequence);
        DicomElement refSopSeq;
        DicomObject refSeriesSeqItem, refSopSeqItem;
        for ( Map.Entry<Series, Set<Instance>> instances : series.entrySet()) {
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
        }
    }
    
    private DicomObject makeIAN(Study study, Map<Series, Set<Instance>> mapSeries, Availability availability) {
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

        for (Map.Entry<Series, Set<Instance>> entry : mapSeries.entrySet() ) {
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
                refSopItem.putString(Tag.InstanceAvailability, VR.CS, 
                        availability == null ? instance.getAvailability().name() : availability.name());
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
        if (processRejNote) {
            log.info("RejectionNote KOS:"+rejNote);
            server.invoke(rejNoteServiceName, "scheduleRejectionNote", 
                    new Object[]{rejNote}, new String[]{DicomObject.class.getName()});
        }
    }

    private ArrayList<DicomObject> getIANs(EntityTree entityTree, Availability availability) throws InstanceNotFoundException, MBeanException, ReflectionException {
        ArrayList<DicomObject> ians = new ArrayList<DicomObject>();
        Map<Patient, Map<Study, Map<Series, Set<Instance>>>> entityTreeMap = entityTree.getEntityTreeMap();
        for (Map<Study, Map<Series, Set<Instance>>> studyMap : entityTreeMap.values()) {
            for ( Map.Entry<Study, Map<Series, Set<Instance>>> studyEntry: studyMap.entrySet()) {
                ians.add(makeIAN(studyEntry.getKey(), studyEntry.getValue(), availability));
            }
        }
        return ians;
    }
    
    private ArrayList<DicomObject> processIANs(EntityTree entityTree, Availability availability) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (processIAN) {
            ArrayList<DicomObject> ians = getIANs(entityTree, availability);
            for (DicomObject ian : ians) {
                server.invoke(ianScuServiceName, "scheduleIAN", 
                        new Object[]{ian}, new String[]{DicomObject.class.getName()});
            }
            return ians;
        }
        return new ArrayList<DicomObject>();
    }

    private ArrayList<DicomObject> processMoveRequests(EntityTree entityTree) throws InstanceNotFoundException, MBeanException, ReflectionException {
        if (moveDestinationAETs != null) {
            ArrayList<DicomObject> ians = getIANs(entityTree, null);
            for (DicomObject ian : ians) {
                for (String aet : moveDestinationAETs) {
                    log.info("Schedule move request for IAN:"+ian);
                    server.invoke(moveScuServiceName, "scheduleMoveInstances", 
                            new Object[]{ian, aet, null}, 
                            new String[]{DicomObject.class.getName(), 
                            String.class.getName(), Integer.class.getName()});
                }
            }
            return ians;
        }
        return new ArrayList<DicomObject>();
    }
    
}

