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

package org.dcm4chee.web.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.UIDUtils;
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
                MPPS mpps = s.getModalityPerformedProcedureStep();
                if (mpps!=null) mpps.getAccessionNumber();//initialize MPPS
                entityTree = moveInstancesToTrash(instances, false, entityTree);
            }
            if (deleteSeries) {
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
        }
        return entityTree == null ? new EntityTree() : entityTree;
    }

    private void moveInstanceToTrash(Instance instance) {
        DicomObject attrs = instance.getAttributes(true);
        PrivateInstance pInst = new PrivateInstance();
        pInst.setAttributes(attrs);
        pInst.setPrivateType(DELETED);
        Series series = instance.getSeries();
        PrivateSeries ps = moveSeriesToTrash(series);
        pInst.setSeries(ps);
        for ( File f : instance.getFiles() ) {
            PrivateFile pf = new PrivateFile();
            pf.setFileSystem(f.getFileSystem());
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
            pSeries.setAttributes(series.getAttributes(true));
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
            pStudy.setAttributes(study.getAttributes(true));
            pStudy.setPrivateType(DELETED);
            Patient pat = study.getPatient();
            PrivatePatient pPat = movePatientToTrash(pat);
            pStudy.setPatient(pPat);
            em.persist(pStudy);
        }
        return pStudy;
    }

    private PrivatePatient movePatientToTrash(Patient patient) {
        PrivatePatient pPat;
        try {
            Query q = em.createNamedQuery("PrivatePatient.findByIdAndIssuer");
            q.setParameter("patId", patient.getPatientID());
            q.setParameter("issuer", patient.getIssuerOfPatientID());
            pPat = (PrivatePatient) q.getSingleResult();
        } catch (NoResultException nre) {
            pPat = new PrivatePatient();
            pPat.setAttributes(patient.getAttributes());
            pPat.setPrivateType(DELETED);
            em.persist(pPat);
        }
        return pPat;
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree[] moveInstancesToSeries(long[] pks, long pk) {
        Query qI = QueryUtil.getQueryForPks(em, "SELECT OBJECT(i) FROM Instance i WHERE i.pk ", pks);
        Query qS = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE pk = :pk")
            .setParameter("pk", pk);
        return moveInstancesToSeries(qI.getResultList(), (Series) qS.getSingleResult());
    }

    @SuppressWarnings("unchecked")
    public EntityTree[] moveInstanceToSeries(String sopIuid, String seriesIuid) {
        Query qI = em.createNamedQuery("Instance.findByIUID");
        qI.setParameter("iuid", sopIuid.trim());
        Query qS = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE seriesInstanceUID = :iuid")
            .setParameter("iuid", seriesIuid.trim());
        return moveInstancesToSeries(qI.getResultList(), (Series) qS.getSingleResult());
    }

    private EntityTree[] moveInstancesToSeries(List<Instance> instances, Series destSeries) {
        EntityTree oldInstances = new EntityTree(instances.size());
        EntityTree newInstances = new EntityTree(instances.size());
        HashSet<Series> srcSeriesList = new HashSet<Series>();
        HashSet<Study> srcStudyList = new HashSet<Study>();
        Instance oldInstance;
        Series srcSeries;
        Study srcStudy;
        DicomObject attrs;
        for (Instance i : instances) {
            srcSeries = i.getSeries();
            srcStudy = srcSeries.getStudy();
            srcSeriesList.add(srcSeries);
            srcStudyList.add(srcStudy);
            oldInstance = new Instance();
            attrs = i.getAttributes(true);
            oldInstance.setAttributes(attrs);
            oldInstance.setSeries(srcSeries);
            oldInstances.addInstance(oldInstance);
            attrs.putString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
            i.setAttributes(attrs);
            i.setSeries(destSeries);
            i.setStorageStatus(StorageStatus.RECEIVING);
            newInstances.addInstance(i);
            srcSeries.setNumberOfSeriesRelatedInstances(srcSeries.getNumberOfSeriesRelatedInstances()-1);
            srcStudy.setNumberOfStudyRelatedInstances(srcStudy.getNumberOfStudyRelatedInstances()-1);
        }
        for ( Series s : srcSeriesList ) {
            em.merge(s);
            em.merge(s.getStudy());
        }
        if ( instances.size() > 0) {
            destSeries.setNumberOfSeriesRelatedInstances(destSeries.getNumberOfSeriesRelatedInstances()+instances.size());
            destSeries.setStorageStatus(StorageStatus.RECEIVING);
        }
        em.merge(destSeries);
        Study destStudy = destSeries.getStudy();
        destStudy.setNumberOfStudyRelatedInstances(destStudy.getNumberOfStudyRelatedInstances()+instances.size());
        em.merge(destStudy);
        getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(destStudy);
        for (Study s : srcStudyList) {
            getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(s);
        }
        EntityTree[] result = new EntityTree[]{oldInstances, newInstances};
        return result;
    }

    @SuppressWarnings("unchecked")
    public EntityTree[] moveSeriesToStudy(long[] pks, long pk) {
        Query qSeries = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Series s WHERE s.pk ", pks);
        Query qStudy = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE pk = :pk")
            .setParameter("pk", pk);
        return moveSeriesToStudy(qSeries.getResultList(), (Study) qStudy.getSingleResult());
    }

    @SuppressWarnings("unchecked")
    public EntityTree[] moveSeriesToStudy(String seriesIuid, String studyIuid) {
        Query qI = em.createNamedQuery("Series.findByIUID");
        qI.setParameter("iuid", seriesIuid.trim());
        Query qS = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid")
            .setParameter("iuid", studyIuid.trim());
        return moveSeriesToStudy((List<Series>)qI.getResultList(), (Study) qS.getSingleResult());
    }

    private EntityTree[] moveSeriesToStudy(List<Series> series, Study destStudy) {
        EntityTree oldInstances = new EntityTree();
        EntityTree newInstances = new EntityTree();
        HashSet<Study> srcStudyList = new HashSet<Study>();
        Set<Instance> instances; 
        Instance oldInstance;
        Series oldSeries;
        Study oldStudy;
        DicomObject instAttrs, seriesAttrs;
        for (Series s : series) {
            srcStudyList.add(s.getStudy());
            oldSeries = new Series();
            s.setModalityPerformedProcedureStep(null);
            oldStudy = s.getStudy();
            oldSeries.setStudy(oldStudy);
            seriesAttrs = s.getAttributes(true);
            oldSeries.setAttributes(seriesAttrs);
            seriesAttrs.putString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
            s.setAttributes(seriesAttrs);
            s.setStudy(destStudy);
            instances = s.getInstances();
            for (Instance i : instances) {
                oldInstance = new Instance();
                instAttrs = i.getAttributes(true);
                oldInstance.setAttributes(instAttrs);
                oldInstance.setSeries(oldSeries);
                oldInstances.addInstance(oldInstance);
                instAttrs.putString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
                i.setAttributes(instAttrs);
                newInstances.addInstance(i);
            }
        }
        for ( Study s : srcStudyList ) {
            getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(s);
        }
        em.merge(destStudy);
        getUpdateDerivedFieldsUtil().updateDerivedFieldsOfStudy(destStudy);
        EntityTree[] result = new EntityTree[]{oldInstances, newInstances};
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public EntityTree[] moveStudiesToPatient(long[] pks, long pk) {
        Query qS = QueryUtil.getQueryForPks(em, "SELECT OBJECT(s) FROM Study s WHERE s.pk ", pks);
        Query qP = em.createQuery("SELECT OBJECT(p) FROM Patient p WHERE pk = :pk")
            .setParameter("pk", pk);
        return moveStudiesToPatient(qS.getResultList(), (Patient) qP.getSingleResult());
    }

    @SuppressWarnings("unchecked")
    public EntityTree[] moveStudyToPatient(String studyIuid, String patId, String issuer) {
        Query qS = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid")
        .setParameter("iuid", studyIuid.trim());
        return moveStudiesToPatient((List<Study>)qS.getResultList(), 
                (Patient) QueryUtil.getPatientQuery(em, patId, issuer).getSingleResult());
    }

    public EntityTree[] moveStudiesToPatient(Collection<Study> studies, Patient pat) {
        EntityTree oldTree = new EntityTree();
        EntityTree newTree = new EntityTree();
        Study oldStudy;
        for ( Study s : studies ) {
            oldStudy = new Study();
            oldStudy.setAttributes(s.getAttributes(true));
            oldStudy.setSeries(s.getSeries());
            oldTree.addStudy(oldStudy);
            s.setPatient(pat);
            em.merge(s);
            newTree.addStudy(s);
        }
        return new EntityTree[]{oldTree, newTree};
    }
    
}
