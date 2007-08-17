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
 * Accurate Software Design, LLC.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Damien Evans <damien.daddy@gmail.com>
 * Justin Falk <jfalkmu@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.com>
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
package org.dcm4che.archive.dao.jpa;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.Media;
import org.dcm4che.archive.entity.MediaDTO;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.data.Dataset;
import org.dcm4cheri.util.StringUtils;

/**
 * Data access object used for managing Series records in the database.
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class StudyDAOImpl extends BaseDAOImpl<Study> implements StudyDAO {

    /**
     * Default constructor.
     */
    public StudyDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return Study.class;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#create(org.dcm4che.data.Dataset,
     *      org.dcm4che.archive.entity.Patient)
     */
    public Study create(Dataset ds, Patient patient)
            throws ContentCreateException {

        Study study = new Study();
        ds.setPrivateCreatorID(PrivateTags.CreatorID);
        study.setPatient(patient);
        study.setAttributes(ds);
        save(study);
        return study;
    }

    /*
     * @see org.dcm4che.archive.dao.StudyDAO#findByStudyUID(java.lang.String)
     */
    public Study findByStudyIuid(String iuid) throws NoResultException {
        Study study = (Study) em
                .createQuery(
                        "select study from Study as study where study.studyIuid=:studyUID")
                .setParameter("studyUID", iuid).getSingleResult();
        if (study == null) {
            throw new NoResultException("Study with iuid=" + iuid);
        }

        return study;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findByPatientAndAccessionNumber(java.lang.Long,
     *      java.lang.String)
     */
    public List<Study> findByPatientAndAccessionNumber(Long patientFk,
            String accessionNumber) throws NoResultException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for studies with patient pk=" + patientFk
                    + " and accession number=" + accessionNumber);
        }

        List<Study> studies = null;

        Query query = em
                .createQuery("select study from Study as study where study.patient.pk=:fk and study.accessionNumber=:acn");
        query.setParameter("fk", patientFk);
        query.setParameter("acn", accessionNumber);
        studies = query.getResultList();

        return studies;
    }

    public boolean updateDerivedFields(Study study, boolean numOfInstances,
            boolean availibility, boolean modsInStudies) {
        boolean updated = false;
        if (numOfInstances)
            if (updateNumberOfInstances(study))
                updated = true;
        final int numI = study.getNumberOfStudyRelatedInstances();
        if (availibility)
            if (updateAvailability(study, numI))
                updated = true;
        if (modsInStudies)
            if (updateModalitiesInStudy(study, numI))
                updated = true;
        return updated;
    }

    private boolean updateNumberOfInstances(Study study) {
        boolean updated = false;
        final Long pk = study.getPk();
        final int numS = findNumberOfStudyRelatedSeries(pk);
        if (study.getNumberOfStudyRelatedSeries() != numS) {
            study.setNumberOfStudyRelatedSeries(numS);
            updated = true;
        }
        final int numI = numS > 0 ? getNumberOfStudyRelatedInstances(pk) : 0;
        if (study.getNumberOfStudyRelatedInstances() != numI) {
            study.setNumberOfStudyRelatedInstances(numI);
            updated = true;
        }
        return updated;
    }

    private int getNumberOfStudyRelatedInstances(Long pk) {
        Number n = (Number) em
                .createQuery(
                        "select sum(s.numberOfSeriesRelatedInstances) from Series s where s.study.pk=:pk")
                .setParameter("pk", pk).getSingleResult();
        return n == null ? 0 : n.intValue();
    }

    private int findNumberOfStudyRelatedSeries(Long studyPk) {
        Number n = (Number) em.createQuery(
                "select count(s) from Series s where s.study.pk = :pk")
                .setParameter("pk", studyPk).getSingleResult();
        return n == null ? 0 : n.intValue();
    }

    private boolean updateAvailability(Study study, int numI) {
        int availability = study.getNumberOfStudyRelatedInstances() > 0 ? determineAvailability(study
                .getPk())
                : Availability.UNAVAILABLE;
        boolean updated = availability != study.getAvailability();
        if (updated) {
            study.setAvailability(availability);
        }
        return updated;
    }

    private int determineAvailability(Long pk) {
        Number n = (Number) em
                .createQuery(
                        "select max(s.availability) from Series s where s.study.pk = :pk")
                .setParameter("pk", pk).getSingleResult();
        return n == null ? Availability.UNAVAILABLE : n.intValue();
    }

    private boolean updateModalitiesInStudy(Study study, int numI) {
        boolean updated = false;
        String modalitiesInStudy = "";
        if (numI > 0) {
            Set dbModalitiesInStudy = findModalityInStudies(study.getPk());
            if (dbModalitiesInStudy.remove(null))
                logger.warn("Study[iuid=" + study.getStudyIuid()
                        + "] contains Series with unspecified Modality");
            if (!dbModalitiesInStudy.isEmpty()) {
                Iterator it = dbModalitiesInStudy.iterator();
                StringBuilder sb = new StringBuilder((String) it.next());
                while (it.hasNext())
                    sb.append('\\').append(it.next());
                modalitiesInStudy = sb.toString();
            }
        }
        if (!modalitiesInStudy.equals(study.getModalitiesInStudy())) {
            study.setModalitiesInStudy(modalitiesInStudy);
            updated = true;
        }
        return updated;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#updateDerivedFields(org.dcm4che.archive.entity.Study,
     *      boolean, boolean, boolean, boolean, boolean, boolean)
     */
    public boolean updateDerivedFields(Study study, boolean numOfInstances,
            boolean retrieveAETs, boolean externalRettrieveAETs,
            boolean filesetId, boolean availibility, boolean modsInStudies)
            throws PersistenceException {
        boolean updated = false;
        final Long pk = study.getPk();
        if (numOfInstances)
            if (updateNumberOfInstances(study))
                updated = true;
        final int numI = getNumberOfStudyRelatedInstances(pk);
        if (retrieveAETs)
            if (updateRetrieveAETs(study, numI))
                updated = true;
        if (externalRettrieveAETs)
            if (updateExternalRetrieveAET(study, numI))
                updated = true;
        if (filesetId)
            if (updateFilesetId(study, numI))
                updated = true;
        if (availibility)
            if (updateAvailability(study, numI))
                updated = true;
        if (modsInStudies)
            if (updateModalitiesInStudy(study, numI))
                updated = true;
        return updated;
    }

    private boolean updateRetrieveAETs(Study study, int numI)
            throws PersistenceException {
        String aets = null;
        if (numI > 0) {
            Set seriesAets = selectSeriesRetrieveAETs(study.getPk());
            if (!seriesAets.contains(null)) {
                Iterator it = seriesAets.iterator();
                aets = (String) it.next();
                while (it.hasNext()) {
                    aets = aets == null ? (String) it.next()
                            : commonRetrieveAETs(aets, (String) it.next());
                }
            }
        }

        boolean updated = aets == null ? study.getRetrieveAETs() != null
                : !aets.equals(study.getRetrieveAETs());
        if (updated) {
            study.setRetrieveAETs(aets);
        }
        return updated;
    }

    @SuppressWarnings("unchecked")
    private Set<String> findModalityInStudies(Long studyPk) {
        // SELECT DISTINCT s.modality FROM Study st, IN(st.series) s WHERE st.pk
        // = ?1
        List<String> resultList = em
                .createQuery(
                        "select distinct s.modality from Study st in(st.series) s where st.pk = :pk")
                .setParameter("pk", studyPk).getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#getFiles(java.lang.Long)
     */
    public List<File> getFiles(Study study, Long fsPk)
            throws PersistenceException {
        // SELECT OBJECT(f) FROM File f WHERE f.instance.series.study.pk = ?1
        // AND f.fileSystem.pk = ?2
        Long studyPk = study.getPk();
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for files of studyPk " + studyPk
                    + ", and fileSystemPk " + fsPk);
        }

        List<File> files = null;

        Query query = em
                .createQuery("select files from File f join f.instance i join i.series ser join ser.study st where st.pk = :studyPk AND f.fileSystem.pk = fsPk");
        query.setParameter("studyPk", studyPk);
        query.setParameter("fsPk", fsPk);
        files = query.getResultList();

        return files;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#selectStudySize(java.lang.Long,
     *      java.lang.Long)
     */
    public long selectStudySize(Long studyPk, Long fsPk) {
        if (logger.isDebugEnabled()) {
            logger.debug("Calculating size of studyPk " + studyPk
                    + " on fileSystemPk " + fsPk);
        }

        Long l = selectStudyFileSize(studyPk, fsPk);
        return l == null ? 0l : l.longValue();
    }

    private Long selectStudyFileSize(Long studyPk, Long fsPk) {
        // SELECT SUM(f.fileSize) FROM File f WHERE f.instance.series.study.pk =
        // ?1 AND f.fileSystem.pk = ?2

        Query query = em
                .createQuery("select sum(f.fileSize) from File f join f.instance i join i.series ser join ser.study st where st.pk = :studyPk AND f.fileSystem.pk = fsPk");
        query.setParameter("studyPk", studyPk);
        query.setParameter("fsPk", fsPk);
        return (Long) query.getSingleResult();
    }

    private Set<String> selectSeriesRetrieveAETs(Long studyPk) {
        // SELECT DISTINCT s.retrieveAETs FROM Series s WHERE s.study.pk = ?1
        List<String> resultList = em
                .createQuery(
                        "select distinct s.retrieveAETs from Series s join s.study st where st.pk = :studyPk")
                .setParameter("studyPk", studyPk).getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }

    private String commonRetrieveAETs(String aets1, String aets2) {
        if (aets1.equals(aets2))
            return aets1;
        String[] a1 = StringUtils.split(aets1, '\\');
        String[] a2 = StringUtils.split(aets2, '\\');
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < a1.length; i++)
            for (int j = 0; j < a2.length; j++)
                if (a1[i].equals(a2[j]))
                    sb.append(a1[i]).append('\\');
        int l = sb.length();
        if (l == 0)
            return null;
        sb.setLength(l - 1);
        return sb.toString();
    }

    private boolean updateExternalRetrieveAET(Study study, int numI)
            throws PersistenceException {
        String aet = null;
        if (numI > 0) {
            Set eAetSet = selectExternalRetrieveAETs(study.getPk());
            if (eAetSet.size() == 1)
                aet = (String) eAetSet.iterator().next();
        }

        boolean updated = aet == null ? study.getExternalRetrieveAET() != null
                : !aet.equals(study.getExternalRetrieveAET());
        if (updated) {
            study.setExternalRetrieveAET(aet);
        }
        return updated;
    }

    private Set selectExternalRetrieveAETs(Long studyPk) {
        // SELECT DISTINCT i.externalRetrieveAET FROM Study st, IN(st.series) s,
        // IN(s.instances) i WHERE st.pk = ?1
        List<String> resultList = em
                .createQuery(
                        "select distinct i.externalRetrieveAET from Study st in(st.series) s in(s.instances) i where st.pk = :studyPk")
                .setParameter("studyPk", studyPk).getResultList();
        if (resultList == null)
            return new HashSet<String>();
        return new HashSet<String>(resultList);
    }

    private boolean updateFilesetId(Study study, int numI)
            throws PersistenceException {
        boolean updated = false;
        String fileSetId = null;
        String fileSetIuid = null;
        if (numI > 0) {
            if (selectNumberOfStudyRelatedInstancesOnMediaWithStatus(study
                    .getPk(), MediaDTO.COMPLETED) == numI) {
                Set c = selectMediaWithStatus(study, MediaDTO.COMPLETED);
                if (c.size() == 1) {
                    Media media = (Media) c.iterator().next();
                    fileSetId = media.getFilesetId();
                    fileSetIuid = media.getFilesetIuid();
                }
            }
        }
        if (fileSetId == null ? study.getFilesetId() != null : !fileSetId
                .equals(study.getFilesetId())) {
            study.setFilesetId(fileSetId);
            updated = true;
        }
        if (fileSetIuid == null ? study.getFilesetIuid() != null : !fileSetIuid
                .equals(study.getFilesetIuid())) {
            study.setFilesetIuid(fileSetIuid);
            updated = true;
        }
        return updated;
    }

    public boolean isStudyAvailableOnMedia(Study study)
            throws PersistenceException {
        String fsuid = study.getFilesetIuid();
        return (fsuid != null && fsuid.length() != 0)
                || selectNumberOfStudyRelatedInstancesOnMediaWithStatus(study
                        .getPk(), MediaDTO.COMPLETED) == study
                        .getNumberOfStudyRelatedInstances();
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#selectNumberOfStudyRelatedInstancesOnMediaWithStatus(java.lang.Long,
     *      int)
     */
    public int selectNumberOfStudyRelatedInstancesOnMediaWithStatus(
            Long studyPk, int completed) {
        // SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1 AND
        // i.media.mediaStatus = ?2
        Number n = (Number) em
                .createQuery(
                        "select count(i) from Instance i join i.series ser join ser.study st where st.pk=:studyPk and media.mediaStatus = :completed")
                .setParameter("studyPk", studyPk).setParameter("completed",
                        completed).getSingleResult();
        return n == null ? 0 : n.intValue();
    }

    /**
     * @param study
     * @param completed
     * @return
     */
    public Set selectMediaWithStatus(Study study, int completed) {
        // TODO: SELECT DISTINCT i.media FROM Study st, IN(st.series) s,
        // IN(s.instances) i WHERE st.pk = ?1 AND i.media.mediaStatus = ?2
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findNumberOfCommitedInstances(org.dcm4che.archive.entity.Study)
     */
    public int findNumberOfCommitedInstances(Study study) {
        // TODO: SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1
        // AND i.commitment = TRUE
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#isStudyAvailableOnROFs(org.dcm4che.archive.entity.Study,
     *      int)
     */
    public boolean isStudyAvailableOnROFs(Study study, int validFileStatus) {
        return (selectNumberOfStudyRelatedInstancesOnROFS(study.getPk(),
                validFileStatus) == study.getNumberOfStudyRelatedInstances());
    }

    /**
     * @param pk
     * @param validFileStatus
     * @return
     */
    public int selectNumberOfStudyRelatedInstancesOnROFS(Long pk,
            int validFileStatus) {
        // TODO: SELECT COUNT(DISTINCT i) FROM Instance i, IN(i.files) f WHERE
        // i.series.study.pk = ?1 AND f.fileStatus = ?2 AND
        // f.fileSystem.availability <> 3 AND f.fileSystem.status = 2
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#isStudyExternalRetrievable(org.dcm4che.archive.entity.Study)
     */
    public boolean isStudyExternalRetrievable(Study study) {
        return selectNumberOfExternalRetrieveableInstances(study.getPk()) == study
                .getNumberOfStudyRelatedInstances();
    }

    /**
     * @param pk
     * @return
     */
    public int selectNumberOfExternalRetrieveableInstances(Long pk) {
        // TODO: SELECT COUNT(i) FROM Instance i WHERE i.series.study.pk = ?1
        // AND i.externalRetrieveAET IS NOT NULL
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#isStudyAvailable(Study, int)
     */
    public boolean isStudyAvailable(Study study, int availability)
            throws PersistenceException {
        return (selectNumberOfStudyRelatedInstancesForAvailability(study
                .getPk(), availability) == study
                .getNumberOfStudyRelatedInstances());
    }

    /**
     * @param pk
     * @param availability
     * @return
     */
    public int selectNumberOfStudyRelatedInstancesForAvailability(Long pk,
            int availability) {
        // TODO SELECT COUNT(DISTINCT i) FROM Instance i, IN(i.files) f WHERE
        // i.series.study.pk = ?1 AND f.fileSystem.availability = ?2
        return 0;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudiesFromAE(java.lang.String,
     *      int)
     */
    public Collection findStudiesFromAE(String sourceAET, int limit)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudiesWithStatus(int,
     *      java.sql.Timestamp, int)
     */
    public Collection findStudiesWithStatus(int i, Timestamp timestamp,
            int limit) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudiesWithStatusFromAE(int,
     *      java.lang.String, int)
     */
    public Collection<Study> findStudiesWithStatusFromAE(int i,
            String sourceAET, int limit) throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudyToCheck(java.sql.Timestamp,
     *      java.sql.Timestamp, java.sql.Timestamp, int)
     */
    public Collection findStudyToCheck(Timestamp createdAfter,
            Timestamp createdBefore, Timestamp checkedBefore, int limit)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findInstancesNotOnMedia(org.dcm4che.archive.entity.Study)
     */
    public Collection<Study> findInstancesNotOnMedia(Study study)
            throws PersistenceException {
        // TODO
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudiesNotOnMedia(java.sql.Timestamp)
     */
    public Collection<Study> findStudiesNotOnMedia(Timestamp timestamp)
            throws PersistenceException {
        // SELECT DISTINCT OBJECT(st) FROM Study st, IN(st.series) s,
        // IN(s.instances) i WHERE i.media IS NULL and st.createdTime < ?1
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for studies not on media created before "
                    + timestamp.toString());
        }

        List<Study> studies = null;

        Query query = em
                .createQuery("select distinct study from Study st join st.series as s join s.instances as i where i.media is null and st.createdTime < :timestamp");
        query.setParameter("timestamp", timestamp, TemporalType.TIMESTAMP);
        studies = query.getResultList();

        return studies;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#findStudiesOnMedia(org.dcm4che.archive.entity.Media)
     */
    public Collection<Study> findStudiesOnMedia(Media media)
            throws PersistenceException {
        // SELECT DISTINCT OBJECT(st) FROM Study st, IN(st.series) s,
        // IN(s.instances) i WHERE i.media = ?1
        Long mediaPk = media.getPk();
        if (logger.isDebugEnabled()) {
            logger.debug("Searching for studies on mediaPk " + mediaPk);
        }

        List<Study> studies = null;

        Query query = em
                .createQuery("select distinct study from Study st join st.series as s join s.instances as i where i.media.pk=:mediaPk");
        query.setParameter("mediaPk", mediaPk);
        studies = query.getResultList();

        return studies;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyDAO#getAllFiles(org.dcm4che.archive.entity.Study)
     */
    public Collection<File> getAllFiles(Study study) {
        // TODO
        return null;
    }
}
