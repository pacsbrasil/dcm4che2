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
 * Damien Evans <damien.daddy@gmail.com>
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

package org.dcm4che.archive.service.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.exceptions.PatientException;
import org.dcm4che.archive.service.PatientUpdate;
import org.dcm4che.archive.service.StudyMgtLocal;
import org.dcm4che.archive.service.StudyMgtRemote;
import org.dcm4che.archive.util.AttributeFilter;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.DcmServiceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1.2 $ $Date: 2007/06/23 18:59:01 $
 * @since Jun 6, 2005
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class StudyMgtBean implements StudyMgtLocal, StudyMgtRemote {

    private static final Logger log = Logger.getLogger(StudyMgtBean.class);

    @EJB private PatientDAO patDAO;

    @EJB private StudyDAO studyDAO;

    @EJB private SeriesDAO seriesDAO;

    @EJB private InstanceDAO instDAO;

    @EJB private PatientUpdate patientUpdate;

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#createStudy(org.dcm4che.data.Dataset)
     */
    public void createStudy(Dataset ds) throws DcmServiceException,
            ContentCreateException, PersistenceException, Exception {
        checkDuplicateStudy(ds.getString(Tags.StudyInstanceUID));
        studyDAO.create(ds, findOrCreatePatient(ds));
    }

    private Patient findOrCreatePatient(Dataset ds)
            throws PersistenceException, ContentCreateException, Exception {
        try {
            return patDAO.searchFor(ds, true);
        }
        catch (NoResultException onfe) {
            return patDAO.create(ds);
        }
    }

    private void checkDuplicateStudy(String suid) throws PersistenceException,
            DcmServiceException {
        try {
            studyDAO.findByStudyIuid(suid);
            throw new DcmServiceException(Status.DuplicateSOPInstance, suid);
        }
        catch (NoResultException e) {
        }
    }

    private Study getStudy(String suid) throws PersistenceException,
            DcmServiceException {
        try {
            return studyDAO.findByStudyIuid(suid);
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance, suid);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#updateStudyAndPatientOnly(java.lang.String, org.dcm4che.data.Dataset)
     */
    public void updateStudyAndPatientOnly(String iuid, Dataset ds)
            throws DcmServiceException, PatientException, PersistenceException {
        Study study = getStudy(iuid);
        AttributeFilter patientFilter = AttributeFilter.getPatientAttributeFilter();
        AttributeFilter studyFilter = AttributeFilter.getStudyAttributeFilter();
        Dataset patientAttr = patientFilter.filter(ds);
        Dataset studyAttr = studyFilter.filter(ds);

        patientUpdate.updatePatient(study, patientAttr);
        updateStudy(iuid, studyAttr);
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#updateStudy(java.lang.String, org.dcm4che.data.Dataset)
     */
    public void updateStudy(String iuid, Dataset ds)
            throws DcmServiceException, PersistenceException {
        Study study = getStudy(iuid);
        if (study == null) {
            // Study may be deleted already
            log
                    .warn("Unable to update the study that does not exist. StudyIuid: "
                            + iuid);
            return;
        }

        Dataset attrs = study.getAttributes(false);
        attrs.putAll(ds);
        study.setAttributes(attrs);
        DcmElement seriesSq = ds.get(Tags.RefSeriesSeq);
        if (seriesSq != null) {
            Set dirtyStudies = new HashSet();
            Set dirtySeries = new HashSet();
            for (int i = 0, n = seriesSq.countItems(); i < n; ++i) {
                updateSeries(seriesSq.getItem(i), study, dirtyStudies,
                        dirtySeries);
            }
            updateDerivedSeriesFields(dirtySeries);
            updateDerivedStudyFields(dirtyStudies);
        }
    }

    private void updateDerivedStudyFields(Set dirtyStudies)
            throws PersistenceException {
        for (Iterator it = dirtyStudies.iterator(); it.hasNext();) {
            String iuid = (String) it.next();
            Study study = studyDAO.findByStudyIuid(iuid);
            studyDAO.updateDerivedFields(study, true, true, true, true, true,
                    true);
        }
    }

    private void updateDerivedSeriesFields(Set dirtySeries)
            throws PersistenceException {
        for (Iterator it = dirtySeries.iterator(); it.hasNext();) {
            String iuid = (String) it.next();
            Series series = seriesDAO.findBySeriesIuid(iuid);
            seriesDAO
                    .updateDerivedFields(series, true, true, true, true, true);
        }
    }

    private void updateSeries(Dataset ds, Study study, Set dirtyStudies,
            Set dirtySeries) throws ContentCreateException,
            PersistenceException {
        try {
            Series series = seriesDAO.findBySeriesIuid(ds
                    .getString(Tags.SeriesInstanceUID));
            Study prevStudy = series.getStudy();
            if (!study.equals(prevStudy)) {
                log.info("Move " + series.toString() + " from "
                        + prevStudy.toString() + " to " + study.toString());
                series.setStudy(study);
                dirtyStudies.add(study.getStudyIuid());
                dirtyStudies.add(prevStudy.getStudyIuid());
            }
            Dataset attrs = series.getAttributes(false);
            String newModality = ds.getString(Tags.Modality);
            if (newModality != null
                    && !newModality.equals(attrs.getString(Tags.Modality))) {
                dirtyStudies.add(study.getStudyIuid());
            }
            attrs.putAll(ds);
            series.setAttributes(attrs);
            DcmElement sopSq = ds.get(Tags.RefSOPSeq);
            if (sopSq != null) {
                for (int i = 0, n = sopSq.countItems(); i < n; ++i) {
                    updateInstance(sopSq.getItem(i), series, dirtyStudies,
                            dirtySeries);
                }
            }
        }
        catch (NoResultException e) {
            seriesDAO.create(ds, study);
            dirtyStudies.add(study.getStudyIuid());
        }
    }

    private void updateInstance(Dataset ds, Series series, Set dirtyStudies,
            Set dirtySeries) throws PersistenceException,
            ContentCreateException {
        try {
            Instance inst = instDAO.findBySopIuid(ds
                    .getString(Tags.RefSOPInstanceUID));
            Series prevSeries = inst.getSeries();
            if (!series.equals(prevSeries)) {
                log.info("Move " + inst.toString() + " from "
                        + prevSeries.toString() + " to " + series.toString());
                inst.setSeries(series);
                dirtySeries.add(series.getSeriesIuid());
                dirtyStudies.add(series.getStudy().getStudyIuid());
                dirtySeries.add(prevSeries.getSeriesIuid());
                dirtyStudies.add(prevSeries.getStudy().getStudyIuid());
            }
            Dataset attrs = inst.getAttributes(false);
            attrs.putAll(ds);
            inst.setAttributes(attrs);
        }
        catch (NoResultException e) {
            instDAO.create(ds, series);
            dirtySeries.add(series.getSeriesIuid());
            dirtyStudies.add(series.getStudy().getStudyIuid());
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#deleteStudy(java.lang.String)
     */
    public void deleteStudy(String iuid) throws DcmServiceException,
            PersistenceException {
        studyDAO.remove(getStudy(iuid));
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#deleteSeries(java.lang.String[])
     */
    public void deleteSeries(String[] iuids) {
        try {
            Set dirtyStudies = new HashSet();
            for (int i = 0; i < iuids.length; i++) {
                Series series = seriesDAO.findBySeriesIuid(iuids[i]);
                dirtyStudies.add(series.getStudy().getStudyIuid());
                seriesDAO.remove(series);
            }
            updateDerivedStudyFields(dirtyStudies);
        }
        catch (NoResultException ignore) {
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#deleteInstances(java.lang.String[])
     */
    public void deleteInstances(String[] iuids) throws DcmServiceException {
        try {
            Set dirtySeries = new HashSet();
            Set dirtyStudies = new HashSet();
            for (int i = 0; i < iuids.length; i++) {
                Instance inst = instDAO.findBySopIuid(iuids[i]);
                Series series = inst.getSeries();
                dirtySeries.add(series.getSeriesIuid());
                dirtyStudies.add(series.getStudy().getStudyIuid());
                instDAO.remove(inst);
            }
            updateDerivedSeriesFields(dirtySeries);
            updateDerivedStudyFields(dirtyStudies);
        }
        catch (NoResultException ignore) {
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#updateStudyStatusId(java.lang.String, java.lang.String)
     */
    public void updateStudyStatusId(String iuid, String statusId)
            throws PersistenceException, DcmServiceException {
        getStudy(iuid).setStudyStatusId(statusId);
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#getInstDAO()
     */
    public InstanceDAO getInstDAO() {
        return instDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#setInstDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstDAO(InstanceDAO instDAO) {
        this.instDAO = instDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#getPatientUpdate()
     */
    public PatientUpdate getPatientUpdate() {
        return patientUpdate;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#setPatientUpdate(org.dcm4che.archive.service.PatientUpdate)
     */
    public void setPatientUpdate(PatientUpdate patientUpdate) {
        this.patientUpdate = patientUpdate;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyMgt#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }
}
