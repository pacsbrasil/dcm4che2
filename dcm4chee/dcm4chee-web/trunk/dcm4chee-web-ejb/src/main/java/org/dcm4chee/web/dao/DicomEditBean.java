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

import java.util.ArrayList;
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
import org.dcm4chee.archive.entity.File;
import org.dcm4chee.archive.entity.Instance;
import org.dcm4chee.archive.entity.Patient;
import org.dcm4chee.archive.entity.PrivateFile;
import org.dcm4chee.archive.entity.PrivateInstance;
import org.dcm4chee.archive.entity.PrivatePatient;
import org.dcm4chee.archive.entity.PrivateSeries;
import org.dcm4chee.archive.entity.PrivateStudy;
import org.dcm4chee.archive.entity.Series;
import org.dcm4chee.archive.entity.Study;
import org.dcm4chee.archive.entity.StudyOnFileSystem;
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
            
    @PersistenceContext(unitName="dcm4chee-arc")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public Collection<Instance> moveInstancesToTrash(long[] pks) {
        Query q = getQueryForPks("SELECT OBJECT(i) FROM Instance i WHERE i.pk ", pks);
        return moveInstancesToTrash(q.getResultList(), true);
    }

    public Collection<Instance> moveInstanceToTrash(String iuid) {
        Query q = em.createNamedQuery("Instance.findByIUID");
        q.setParameter("iuid", iuid);
        return moveInstancesToTrash(q.getResultList(), true);
    }
    
    public Collection<Instance> moveInstancesToTrash(Collection<Instance> instances, boolean deleteInstance) {
        log.info("Move "+instances.size()+" instances to trash!");
        Set<Series> series = new HashSet<Series>();
        for ( Instance instance : instances) {
            moveInstanceToTrash(instance);
            if (deleteInstance) {
                series.add(instance.getSeries());
                log.info("Delete Instance:"+instance.getAttributes(false));
                em.remove(instance);
            }

        }
        if (deleteInstance) {
            Study st;
            for (Series s : series) {
                Query q = em.createQuery("SELECT COUNT(i) FROM Instance i WHERE i.series.pk = :pk").setParameter("pk", s.getPk());
                s.setNumberOfSeriesRelatedInstances(((Long)q.getSingleResult()).intValue());
                st = s.getStudy();
                q = em.createQuery("SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = :pk").setParameter("pk", st.getPk());
                st.setNumberOfStudyRelatedInstances(((Long)q.getSingleResult()).intValue());
            }
        }
        return instances;
    }

    public Collection<Instance> moveSeriesToTrash(long[] pks) {
        Query q;
        q = getQueryForPks("SELECT OBJECT(s) FROM Series s WHERE pk ", pks);
        return moveSeriesToTrash(q.getResultList(), true);
    }

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Collection<Instance> moveSeriesToTrash(String iuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Series s WHERE seriesInstanceUID = :iuid")
            .setParameter("iuid", iuid);
        return this.moveSeriesToTrash(q.getResultList(), true);
    }

    private Collection<Instance> moveSeriesToTrash(Collection<Series> series, boolean deleteSeries) {
        List<Instance> allInstances = new ArrayList<Instance>();
        Set<Instance> instances;
        Set<Study> studies = new HashSet<Study>();
        for (Series s : series) {
            instances = s.getInstances();
            if (instances.isEmpty()) {
                log.info("move empty series to trash:"+s.getSeriesInstanceUID());
                this.moveSeriesToTrash(s);
            } else {
                allInstances.addAll( moveInstancesToTrash(instances, false) );
            }
            if (deleteSeries) {
                studies.add(s.getStudy());
                em.remove(s);
            }
        }
        if (deleteSeries) {
            for (Study st : studies) {
                Query q = em.createQuery("SELECT COUNT(s) FROM Series s WHERE s.study.pk = :pk").setParameter("pk", st.getPk());
                st.setNumberOfStudyRelatedSeries(((Long)q.getSingleResult()).intValue());
                q = em.createQuery("SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = :pk").setParameter("pk", st.getPk());
                st.setNumberOfStudyRelatedInstances(((Long)q.getSingleResult()).intValue());
            }
        }
        return allInstances;
    }

    public Collection<Instance> moveStudiesToTrash(long[] pks) {
        Query q = getQueryForPks("SELECT OBJECT(s) FROM Study s WHERE pk ", pks);
        return moveStudiesToTrash(q.getResultList());
    }
    
    @SuppressWarnings("unchecked")
    public Collection<Instance> moveStudyToTrash(String iuid) {
        Query q = em.createQuery("SELECT OBJECT(s) FROM Study s WHERE studyInstanceUID = :iuid")
            .setParameter("iuid", iuid);
        return this.moveStudiesToTrash(q.getResultList());
    }

    private Collection<Instance> moveStudiesToTrash(Collection<Study> studies) {
        List<Instance> allInstances = new ArrayList<Instance>();
        Set<Series> series;
        for (Study st : studies) {
            series = st.getSeries();
            if (series.isEmpty()) {
                log.info("move empty study to trash:"+st.getStudyInstanceUID());
                this.moveStudyToTrash(st);
            } else {
                allInstances.addAll( moveSeriesToTrash(series, false) );
            }
            log.info("Delete Study:"+st.getAttributes(false));
            Query q = em.createQuery("SELECT OBJECT(sof) FROM StudyOnFileSystem sof WHERE study_fk = :pk");
            q.setParameter("pk", st.getPk());
            for ( StudyOnFileSystem sof : (List<StudyOnFileSystem>)q.getResultList()) {
                em.remove(sof);
            }
            
            em.remove(st);
        }
        return allInstances;
    }
    
    public Collection<Instance> movePatientsToTrash(long[] pks) {
        Query q = getQueryForPks("SELECT OBJECT(p) FROM Patient p WHERE pk ", pks);
        return movePatientsToTrash(q.getResultList());
    }

    public Collection<Instance> movePatientToTrash(String Id, String issuer) {
        Query q = em.createQuery("SELECT OBJECT(p) FROM Patient p "+
                "WHERE patientID = :patId AND issuerOfPatientID = :issuer" )
            .setParameter("patId", Id).setParameter("issuer", issuer);
        return this.movePatientsToTrash(q.getResultList());
    }
    
    private Collection<Instance> movePatientsToTrash(Collection<Patient> patients) {
        List<Instance> allInstances = new ArrayList<Instance>();
        Set<Study> studies;
        for (Patient p : patients) {
            studies = p.getStudies();
            if (studies.isEmpty()) {
                log.info("move empty patient to trash:"+p.getPatientID()+"^^^"+p.getIssuerOfPatientID()+":"+p.getPatientName());
                this.movePatientToTrash(p);
            } else {
                allInstances.addAll( moveStudiesToTrash(studies) );
            }
        }
        return allInstances;
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

    private Query getQueryForPks(String base, long[] pks) {
        Query q;
        int len=pks.length;
        if (len == 1) {
            q = em.createQuery(base+"= :pk").setParameter("pk", pks[0]);
        } else {
            StringBuilder sb = new StringBuilder(base);
            appendIN(sb, len);
            q = em.createQuery(sb.toString());
            this.setParametersForIN(q, pks);
        }
        return q;
    }
    
    private void appendIN(StringBuilder sb, int len) {
        sb.append(" IN ( ?");
        for (int i = 1 ; i < len ; i++ ) {
            sb.append(i).append(", ?");
        }
        sb.append(len).append(" )");
    }
        
    private void setParametersForIN(Query q, long[] pks) {
        int i = 1;
        for ( long pk : pks ) {
            q.setParameter(i++, pk);
        }
    }    
}
