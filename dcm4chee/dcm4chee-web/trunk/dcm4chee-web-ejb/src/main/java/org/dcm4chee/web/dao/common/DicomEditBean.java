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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

package org.dcm4chee.web.dao.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.archive.common.Availability;
import org.dcm4chee.archive.common.PrivateTag;
import org.dcm4chee.archive.common.StorageStatus;
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.MPPS;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PrivateFile;
import org.dcm4chee.archive.entity.PrivateInstance;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateSeries;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyOnFileSystem;
import org.dcm4chee.web.dao.util.QueryUtil;
import org.dcm4chee.web.dao.util.UpdateDerivedFieldsUtil;
import org.dcm4chee.web.dao.vo.EntityTree;
import org.jboss.annotation.ejb.LocalBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Franz Willer <fwiller@gmail.com>
 * @version $Revision$ $Date$
 * @since Feb 01, 2010
 */

@Stateless
@LocalBinding (jndiBinding=DicomEditLocal.JNDI_NAME)
public class DicomEditBean implements DicomEditLocal {

    private static final int DELETED = 1;
    
    private static Logger log = LoggerFactory.getLogger(DicomEditBean.class);   
            
    private UpdateDerivedFieldsUtil updateUtil;
    
    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    private UpdateDerivedFieldsUtil getUpdateDerivedFieldsUtil() {
        if ( updateUtil == null) {
            updateUtil = new UpdateDerivedFieldsUtil(em);
        }
        return updateUtil;
    }
    @SuppressWarnings("unchecked")
    public EntityTree moveInstancesToTrash(long[] pks) {
        Query q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(i) FROM Instance i WHERE i.pk ", pks);
        return moveInstancesToTrash(q.getResultList(), true);
    }

    @SuppressWarnings("unchecked")
    public EntityTree moveInstanceToTrash(String iuid) {
        Query q = em.createNamedQuery("Instance.findByIUID");
        q.setParameter("iuid", iuid.trim());
        return moveInstancesToTrash(q.getResultList(), true);
    }
    public EntityTree moveInstancesToTrash(Collection<Instance> instances, boolean deleteInstance) {
        return moveInstancesToTrash(instances, deleteInstance, null);
    }    
    public EntityTree moveInstancesToTrash(Collection<Instance> instances, boolean deleteInstance, EntityTree entityTree) {
        log.debug("Move {} instances to trash!",instances.size());
        Set<Study> studies = new HashSet<Study>();
        for ( Instance instance : instances) {
            moveInstanceToTrash(instance);
            if (deleteInstance) {
                studies.add(instance.getSeries().getStudy());
                log.debug("Delete Instance:{}",instance.getAttributes(false));
                em.remove(instance);
            }

        }
        if (deleteInstance) {
            removeInstancesFromMpps(instances);
            for (Study st : studies) {
                getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(st);
            }
        }
        return entityTree == null ? new EntityTree(instances) : entityTree.addInstances(instances);
    }

    @SuppressWarnings("unchecked")
    public EntityTree moveSeriesToTrash(long[] pks) {
        Query q;
        q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Series s WHERE pk ", pks);
        return moveSeriesToTrash(q.getResultList(), true, null);
    }

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public EntityTree moveSeriesToTrash(String iuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE seriesInstanceUID = :iuid")
            .setParameter("iuid", iuid.trim());
        return this.moveSeriesToTrash(q.getResultList(), true, null);
    }

    private EntityTree moveSeriesToTrash(Collection<Series> series, boolean deleteSeries, EntityTree entityTree) {
        Set<Instance> instances;
        Study study;
        Set<Study> studies = new HashSet<Study>();
        for (Series s : series) {
            instances = s.getInstances();
            if (instances.isEmpty()) {
                log.debug("move empty series to trash:{}",s.getSeriesInstanceUID());
                this.moveSeriesToTrash(s);
            } else {
                entityTree = moveInstancesToTrash(instances, false, entityTree);
            }
            MPPS mpps = s.getModalityPerformedProcedureStep();
            if (mpps!=null) mpps.getAccessionNumber();//initialize MPPS
            if (deleteSeries) {
                removeSeriesFromMPPS(mpps, s.getSeriesInstanceUID());
                studies.add(study = s.getStudy());
                em.remove(s);
                study.getSeries().remove(s);
            }
        }
        if (deleteSeries) {
            for (Study st : studies) {
                getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(st);
            }
        }
        return entityTree == null ? new EntityTree() : entityTree;
    }

    @SuppressWarnings("unchecked")
    public EntityTree moveSeriesOfPpsToTrash(long[] pks)
    {
        Query q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(p) FROM MPPS p WHERE pk ", pks);
        Query qs = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Series s WHERE s.modalityPerformedProcedureStep.pk ", pks);
        List<Series> seriess = qs.getResultList();
        EntityTree tree = moveSeriesToTrash(seriess, true, null);
        List<MPPS> mppss = q.getResultList();
        for(MPPS mpps : mppss) {
            em.remove(mpps);
        }
        return tree;
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree moveStudiesToTrash(long[] pks) {
        Query q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Study s WHERE pk ", pks);
        return moveStudiesToTrash(q.getResultList(), null);
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree moveStudyToTrash(String iuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid")
            .setParameter("iuid", iuid.trim());
        return this.moveStudiesToTrash(q.getResultList(), null);
    }

    @SuppressWarnings("unchecked")
    private EntityTree moveStudiesToTrash(Collection<Study> studies, EntityTree entityTree) {
        Set<Series> series;
        for (Study st : studies) {
            series = st.getSeries();
            if (series.isEmpty()) {
                log.debug("move empty study to trash:{}",st.getStudyInstanceUID());
                this.moveStudyToTrash(st);
            } else {
                entityTree = moveSeriesToTrash(series, false, entityTree);
            }
            log.debug("Delete Study:{}",st.getAttributes(false));
            Query q = em.createQuery("SELECT OBJECT(sof) FROM StudyOnFileSystem sof WHERE study_fk = :pk");
            q.setParameter("pk", st.getPk());
            for ( StudyOnFileSystem sof : (List<StudyOnFileSystem>)q.getResultList()) {
                em.remove(sof);
            }
            
            em.remove(st);
        }
        return entityTree == null ? new EntityTree() : entityTree;
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree movePatientsToTrash(long[] pks) {
        Query q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(p) FROM Patient p WHERE pk ", pks);
        return movePatientsToTrash(q.getResultList(), null);
    }

    @SuppressWarnings("unchecked")
    public EntityTree movePatientToTrash(String patId, String issuer) {
        return this.movePatientsToTrash(QueryUtil.getPatientQuery(em, patId, issuer).getResultList(), null);
    }
    
    private EntityTree movePatientsToTrash(Collection<Patient> patients, EntityTree entityTree) {
        Set<Study> studies;
        for (Patient p : patients) {
            studies = p.getStudies();
            if (studies.isEmpty()) {
                log.debug("move empty patient to trash:"+p.getPatientID()+"^^^"+p.getIssuerOfPatientID()+":"+p.getPatientName());
                this.movePatientToTrash(p);
            } else {
                entityTree = moveStudiesToTrash(studies, entityTree);
            }
            deletePatient(p);
        }
        return entityTree == null ? new EntityTree() : entityTree;
    }

    private void moveInstanceToTrash(Instance instance) {
        DicomObject attrs = instance.getAttributes(false);
        PrivateInstance pInst = new PrivateInstance();
        pInst.setAttributes(attrs);
        pInst.setPrivateType(DELETED);
        Series series = instance.getSeries();
        PrivateSeries ps = moveSeriesToTrash(series);
        pInst.setSeries(ps);
        for ( File f : instance.getFiles() ) {
            PrivateFile pf = new PrivateFile();
            pf.setFileSystem(f.getFileSystem());
            f.getFileSystem().getAvailability();//initialize FileSystem
            pf.setFilePath(f.getFilePath());
            pf.setTransferSyntaxUID(f.getTransferSyntaxUID());
            pf.setFileStatus(f.getFileStatus());
            pf.setFileSize(f.getFileSize());
            pf.setFileMD5(f.getMD5Sum());
            pf.setInstance(pInst);
            em.persist(pf);
        }
        em.persist(pInst);
    }

    private PrivateSeries moveSeriesToTrash(Series series) {
        PrivateSeries pSeries;
        try {
            Query q = em.createNamedQuery("PrivateSeries.findByIUID");
            q.setParameter("iuid", series.getSeriesInstanceUID());
            pSeries = (PrivateSeries) q.getSingleResult();
            series.getStudy().getPatient().getPk();
        } catch (NoResultException nre) {
            pSeries = new PrivateSeries();//we need parents initialized.
            DicomObject attrs = series.getAttributes(false);
            attrs.putString(attrs.resolveTag(PrivateTag.CallingAET, PrivateTag.CreatorID), 
                    VR.AE, series.getSourceAET());
            pSeries.setAttributes(attrs);
            pSeries.setPrivateType(DELETED);
            Study study = series.getStudy();
            PrivateStudy pStudy = moveStudyToTrash(study);
            pSeries.setStudy(pStudy);
            em.persist(pSeries);
        }
        return pSeries;
    }

    private PrivateStudy moveStudyToTrash(Study study) {
        PrivateStudy pStudy;
        try {
            Query q = em.createNamedQuery("PrivateStudy.findByIUID");
            q.setParameter("iuid", study.getStudyInstanceUID());
            pStudy = (PrivateStudy) q.getSingleResult();
            study.getPatient().getPk();
        } catch (NoResultException nre) {
            pStudy = new PrivateStudy();
            pStudy.setAttributes(study.getAttributes(false));
            pStudy.setPrivateType(DELETED);
            Patient pat = study.getPatient();
            PrivatePatient pPat = movePatientToTrash(pat);
            pStudy.setPatient(pPat);
            em.persist(pStudy);
        }
        return pStudy;
    }

    @SuppressWarnings("unchecked")
    private PrivatePatient movePatientToTrash(Patient patient) {
        PrivatePatient pPat = null;
        try {
            if ( patient.getIssuerOfPatientID() != null) {
                Query q = em.createNamedQuery("PrivatePatient.findByIdAndIssuer");
                q.setParameter("patId", patient.getPatientID());
                q.setParameter("issuer", patient.getIssuerOfPatientID());
                pPat = (PrivatePatient) q.getSingleResult();
            } else {
                Query q = em.createQuery("select object(p) from PrivatePatient p where patientID = :patId and patientName = :name");
                q.setParameter("patId", patient.getPatientID());
                q.setParameter("name", patient.getPatientName());
                List<PrivatePatient> pList = (List<PrivatePatient>) q.getResultList();
                PrivatePatient p;
                String birthdate = patient.getAttributes().getString(Tag.PatientBirthDate, "X");
                for (int i = 0, len = pList.size() ;  i  < len ; i++) {
                    p = pList.get(i);
                    if (p.getAttributes().getString(Tag.PatientBirthDate, "X").equals(birthdate)) {
                        pPat = p;
                        break;
                    }
                }
            }
        } catch (NoResultException nre) {            
        }
        if (pPat == null) {
            pPat = new PrivatePatient();
            pPat.setAttributes(patient.getAttributes());
            pPat.setPrivateType(DELETED);
            em.persist(pPat);
        }
        return pPat;
    }

    private void deletePatient(Patient patient) {
        log.debug("Delete Patient:{}",patient);
        for (MPPS mpps : patient.getModalityPerformedProcedureSteps()) {
            em.remove(mpps);
            log.debug("  MPPS deleted:{}",mpps);
        }
        em.remove(patient);
    }

    @SuppressWarnings("unchecked")
    public List<MPPS> deletePps(long[] pks) {
        Query q = QueryUtil.getQueryForPks(em, "SELECT OBJECT(p) FROM MPPS p WHERE pk ", pks);
        List<MPPS> mppss = q.getResultList();
        DicomObject seriesAttrs;
        for(MPPS mpps : mppss) {
           for (Series series : mpps.getSeries()) {
                seriesAttrs = series.getAttributes(true);
                seriesAttrs.remove(Tag.ReferencedPerformedProcedureStepSequence);
                seriesAttrs.remove(Tag.PerformedProcedureStepStartDate);
                seriesAttrs.remove(Tag.PerformedProcedureStepStartTime);
                series.setAttributes(seriesAttrs);
                series.setModalityPerformedProcedureStep(null);
                em.merge(series);
            }
            mpps.getPatient().getPatientID();
            em.remove(mpps);
        }
        return mppss;
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree moveStudiesToPatient(long pks[], long pk)
    {
        Query qP = em.createQuery("SELECT OBJECT(p) FROM Patient p WHERE pk = :pk").setParameter("pk", Long.valueOf(pk));
        Query qS = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Study s WHERE pk ", pks);
        return moveStudiesToPatient(qS.getResultList(), (Patient)qP.getSingleResult());
    }

    @SuppressWarnings("unchecked")
    public EntityTree moveStudyToPatient(String iuid, String patId, String issuer)
    {
        Query qS = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid").setParameter("iuid", iuid.trim());
        Query qP = QueryUtil.getPatientQuery(em, patId, issuer);
        return moveStudiesToPatient(qS.getResultList(), (Patient)qP.getSingleResult());
    }

    private EntityTree moveStudiesToPatient(List<Study> studies, Patient patient)
    {
        EntityTree tree = new EntityTree();
        for(Study s : studies) {
            tree.addStudy(s);
            s.setPatient(patient);
            for (Series series : s.getSeries()) {
                MPPS mpps = series.getModalityPerformedProcedureStep();
                if(mpps != null) {
                    mpps.setPatient(patient);
                    em.merge(mpps);
                }
            } 
        }
        return tree;
    }
    
    public DicomObject getCompositeObjectforSeries(String iuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE seriesInstanceUID = :iuid")
            .setParameter("iuid", iuid.trim());
        Series s = (Series) q.getSingleResult();
        DicomObject attrs = s.getAttributes(false);
        attrs.putString(attrs.resolveTag(PrivateTag.CallingAET, PrivateTag.CreatorID), VR.AE, s.getSourceAET());
        s.getStudy().getAttributes(false).copyTo(attrs);
        s.getStudy().getPatient().getAttributes().copyTo(attrs);
        return attrs;

    }
    public DicomObject getCompositeObjectforSeries(long pk) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE pk = :pk").setParameter("pk", pk);
        Series s = (Series) q.getSingleResult();
        DicomObject attrs = s.getAttributes(false);
        s.getStudy().getAttributes(false).copyTo(attrs);
        s.getStudy().getPatient().getAttributes().copyTo(attrs);
        return attrs;
    }
    public DicomObject getCompositeObjectforStudy(String studyIuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid").setParameter("iuid", studyIuid.trim());
        Study s = (Study) q.getSingleResult();
        DicomObject attrs = s.getAttributes(false);
        s.getPatient().getAttributes().copyTo(attrs);
        return attrs;
    }
    public DicomObject getCompositeObjectforStudy(long pk) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE pk = :pk").setParameter("pk", pk);
        Study s = (Study) q.getSingleResult();
        DicomObject attrs = s.getAttributes(false);
        s.getPatient().getAttributes().copyTo(attrs);
        return attrs;
    }
    public DicomObject getPatientAttributes(String patId, String issuer) {
        return ((Patient) QueryUtil.getPatientQuery(em, patId, issuer).getSingleResult()).getAttributes();
    }
    public DicomObject getPatientAttributes(long pk) {
        Query q = em.createQuery("SELECT OBJECT(p) FROM Patient p WHERE pk = :pk").setParameter("pk", pk);
        return ((Patient)q.getSingleResult()).getAttributes();
    }
    
    public Series updateSeries(Series series) {
        if (series.getPk() == -1) {
            if (series.getStudy().getPk() == -1) {
                updateStudy(series.getStudy());
            }
            em.persist(series);
        } else {
            em.merge(series);
        }
        return series;
    }
    public Series createSeries(DicomObject seriesAttrs, long studyPk) {
        Study study = em.find(Study.class, studyPk);
        Series series = new Series();
        series.setAvailability(Availability.ONLINE);
        series.setNumberOfSeriesRelatedInstances(0);
        series.setStorageStatus(StorageStatus.STORED);
        series.setAttributes(seriesAttrs);
        series.setStudy(study);
        em.persist(series);
        return series;
    }
    
    public Study updateStudy(Study study) {
        if (study.getPk() == -1) {
            if (study.getPatient().getPk() == -1) {
                em.persist(study.getPatient());
            }
            em.persist(study);
        } else {
            em.merge(study);
        }
        return study;
    }
    
    @SuppressWarnings("deprecation")
    public Study createStudy(DicomObject studyAttrs, long patPk) {
        Patient patient = em.find(Patient.class, patPk);
        Study study = new Study();
        study.setAttributes(studyAttrs);
        study.setAvailability(Availability.ONLINE);
        study.setNumberOfStudyRelatedInstances(0);
        study.setNumberOfStudyRelatedSeries(0);
        study.setStudyStatus(0);
        study.setPatient(patient);
        em.persist(study);
        return study;
    }
 
    private void removeSeriesFromMPPS(MPPS mpps, String seriesIUID) {
        if(mpps != null && mpps.getAttributes() != null) {
            DicomObject mppsAttrs = mpps.getAttributes();
            removeFromMPPS(mppsAttrs, seriesIUID, null);
            mpps.setAttributes(mppsAttrs);
            em.merge(mpps);
        }
    }

    private void removeFromMPPS(DicomObject mppsAttrs, String seriesIUID, Collection<String> sopIUIDs) {
        DicomElement psSq = mppsAttrs.get(Tag.PerformedSeriesSequence);
        for(int i = psSq.countItems() - 1; i >= 0; i--) {
            DicomObject psItem = psSq.getDicomObject(i);
            if(!seriesIUID.equals(psItem.getString(Tag.SeriesInstanceUID)))
                continue;
            if(sopIUIDs == null) {
                psSq.removeDicomObject(i);
                break;
            }
            DicomElement refImgSq = psItem.get(Tag.ReferencedImageSequence);
            for(int j = refImgSq.countItems() - 1 ; j >= 0 ; j--) {
                DicomObject refImgItem = refImgSq.getDicomObject(j);
                if(sopIUIDs.contains(refImgItem.getString(Tag.ReferencedSOPInstanceUID)))
                    refImgSq.removeDicomObject(j);
            }
        }
    }

    private void removeInstancesFromMpps(Collection<Instance> instances) {
        Map<Series, Set<String>> map = new HashMap<Series, Set<String>>();
        Set<String> iuidsPerSeries;
        for(Instance i : instances)
        {
            Series s = i.getSeries();
            iuidsPerSeries = (Set<String>)map.get(s);
            if(iuidsPerSeries == null)
            {
                iuidsPerSeries = new HashSet<String>();
                map.put(s, iuidsPerSeries);
            }
            iuidsPerSeries.add(i.getSOPInstanceUID());
        }

        for (Map.Entry<Series, Set<String>> entry : map.entrySet()) {
            Series s = (Series)entry.getKey();
            MPPS mpps = s.getModalityPerformedProcedureStep();
            if(mpps != null && mpps.getAttributes() != null) {
                DicomObject mppsAttrs = mpps.getAttributes();
                removeFromMPPS(mppsAttrs, s.getSeriesInstanceUID(), entry.getValue());
                mpps.setAttributes(mppsAttrs);
                try {
                    em.merge(mpps);
                } catch (Throwable x) {
                    log.warn("MPPS update failed! mpps:"+mpps);
                }
            }
        }
    }
}
