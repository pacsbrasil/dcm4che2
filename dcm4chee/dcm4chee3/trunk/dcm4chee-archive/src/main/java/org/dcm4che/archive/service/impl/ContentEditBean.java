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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.ContentEditLocal;
import org.dcm4che.archive.service.ContentEditRemote;
import org.dcm4che.archive.util.Convert;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1.1 $ $Date: 2007/06/23 18:59:01 $
 * @since 14.01.2004
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class ContentEditBean implements ContentEditLocal, ContentEditRemote {

    private static final int CHANGE_MODE_NO = 0;

    private static final int CHANGE_MODE_STUDY = 0x04;

    private static final int CHANGE_MODE_SERIES = 0x02;

    private static final int CHANGE_MODE_INSTANCE = 0x01;

    private static final int DELETED = 1;

    @EJB private PatientDAO patDAO;

    @EJB private StudyDAO studyDAO;

    @EJB private SeriesDAO seriesDAO;

    @EJB private InstanceDAO instDAO;

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private static Logger log = Logger.getLogger(ContentEditBean.class
            .getName());

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#createPatient(org.dcm4che.data.Dataset)
     */
    public Dataset createPatient(Dataset ds) throws ContentCreateException {
        return patDAO.create(ds).getAttributes(true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#mergePatients(long, long[])
     */
    public Map mergePatients(long patPk, long[] mergedPks) {
        Map map = new HashMap();
        try {
            Patient dominant = patDAO.findByPrimaryKey(new Long(patPk));
            if (checkCircularMerge(dominant, mergedPks)) {
                log.warn("Circular merge detected (dominant:"
                        + dominant.getPatientId() + "^^^"
                        + dominant.getIssuerOfPatientId()
                        + ")! Merge order ignored!");
                map.put("ERROR", "Circular Merge detected!");
                return map;
            }
            map.put("DOMINANT", dominant.getAttributes(false));
            Dataset[] mergedPats = new Dataset[mergedPks.length];
            map.put("MERGED", mergedPats);
            ArrayList list = new ArrayList();
            for (int i = 0; i < mergedPks.length; i++) {
                if (patPk == mergedPks[i])
                    continue;
                Patient priorPat = patDAO.findByPrimaryKey(new Long(
                        mergedPks[i]));
                mergedPats[i] = priorPat.getAttributes(false);
                list.addAll(priorPat.getStudies());
                dominant.getStudies().addAll(priorPat.getStudies());
                dominant.getMpps().addAll(priorPat.getMpps());
                dominant.getMwlItems().addAll(priorPat.getMwlItems());
                dominant.getGpsps().addAll(priorPat.getGpsps());
                priorPat.setMergedWith(dominant);
            }
            ArrayList col = new ArrayList();
            Iterator iter = list.iterator();
            Study sl;
            while (iter.hasNext()) {
                sl = (Study) iter.next();
                col.add(getStudyMgtDataset(sl, sl.getSeries(), null));
            }
            map.put("NOTIFICATION_DS", col);
            return map;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    private boolean checkCircularMerge(Patient pat, long[] mergedPks) {
        Patient mergedWith = pat.getMergedWith();
        if (mergedWith != null) {
            long pk = mergedWith.getPk().longValue();
            for (int i = 0; i < mergedPks.length; i++) {
                if (pk == mergedPks[i]) {
                    return true;
                }
            }
            return checkCircularMerge(pat, mergedPks);
        }
        return false;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#createStudy(org.dcm4che.data.Dataset, long)
     */
    public Dataset createStudy(Dataset ds, long patPk)
            throws ContentCreateException {
        try {
            Patient patient = patDAO.findByPrimaryKey(new Long(patPk));
            Dataset ds1 = studyDAO.create(ds, patient).getAttributes(true);
            if (log.isDebugEnabled()) {
                log.debug("createStudy ds1:");
                log.debug(ds1);
            }
            ds1.putAll(patient.getAttributes(true));
            if (log.isDebugEnabled()) {
                log.debug("createStudy ds1 with patient:");
                log.debug(ds1);
            }
            return ds1;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }

    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#createSeries(org.dcm4che.data.Dataset, long)
     */
    public Dataset createSeries(Dataset ds, long studyPk)
            throws ContentCreateException {
        try {
            Study study = studyDAO.findByPrimaryKey(new Long(studyPk));
            Series series = seriesDAO.create(ds, study);
            Collection col = new ArrayList();
            col.add(series);
            return getStudyMgtDataset(study, col, null, CHANGE_MODE_SERIES,
                    series.getAttributes(true));
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }

    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#updatePatient(org.dcm4che.data.Dataset)
     */
    public Collection updatePatient(Dataset ds) {

        try {
            Collection col = new ArrayList();
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final long pk = Convert.toLong(ds.getByteBuffer(
                    PrivateTags.PatientPk).array());
            Patient patient = patDAO.findByPrimaryKey(new Long(pk));
            patient.setAttributes(ds);
            Collection studies = patient.getStudies();
            Iterator iter = patient.getStudies().iterator();
            Study sl;
            while (iter.hasNext()) {
                sl = (Study) iter.next();
                col.add(getStudyMgtDataset(sl, sl.getSeries(), null));
            }
            return col;
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#updateStudy(org.dcm4che.data.Dataset)
     */
    public Dataset updateStudy(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final long pk = Convert.toLong(ds
                    .getByteBuffer(PrivateTags.StudyPk).array());
            Study study = studyDAO.findByPrimaryKey(new Long(pk));
            study.setAttributes(ds);
            return getStudyMgtDataset(study, study.getSeries(), null,
                    CHANGE_MODE_STUDY, study.getAttributes(true));
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#updateSeries(org.dcm4che.data.Dataset)
     */
    public Dataset updateSeries(Dataset ds) {

        try {
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            final long pk = Convert.toLong(ds.getByteBuffer(
                    PrivateTags.SeriesPk).array());
            Series series = seriesDAO.findByPrimaryKey(new Long(pk));
            series.setAttributes(ds);
            Study study = series.getStudy();
            studyDAO.updateDerivedFields(study, false, false, false, false,
                    false, true);
            Collection col = new ArrayList();
            col.add(series);
            return getStudyMgtDataset(study, col, null, CHANGE_MODE_SERIES,
                    series.getAttributes(true));
        }
        catch (PersistenceException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#moveStudies(long[], long)
     */
    public Collection moveStudies(long[] study_pks, long patient_pk)
            throws PersistenceException {
        Collection col = new ArrayList();
        Patient pat = patDAO.findByPrimaryKey(new Long(patient_pk));
        Collection studies = pat.getStudies();
        Dataset dsPat = pat.getAttributes(true);
        Dataset ds1;
        for (int i = 0; i < study_pks.length; i++) {
            Study study = studyDAO.findByPrimaryKey(new Long(study_pks[i]));
            Patient oldPat = study.getPatient();
            if (oldPat.equals(pat))
                continue;
            studies.add(study);
            ds1 = getStudyMgtDataset(study, study.getSeries(), null,
                    CHANGE_MODE_STUDY, dsPat);
            col.add(ds1);

        }
        return col;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#moveSeries(long[], long)
     */
    public Dataset moveSeries(long[] series_pks, long study_pk)
            throws PersistenceException {
        Study study = studyDAO.findByPrimaryKey(new Long(study_pk));
        Collection seriess = study.getSeries();
        Collection movedSeriess = new ArrayList();
        for (int i = 0; i < series_pks.length; i++) {
            Series series = seriesDAO.findByPrimaryKey(new Long(series_pks[i]));
            Study oldStudy = series.getStudy();
            if (oldStudy.equals(study))
                continue;
            seriess.add(series);
            movedSeriess.add(series);
            studyDAO.updateDerivedFields(oldStudy, true, true, true, true,
                    true, true);
        }
        studyDAO.updateDerivedFields(study, true, true, true, true, true, true);
        return getStudyMgtDataset(study, movedSeriess, null);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#moveInstances(long[], long)
     */
    public Dataset moveInstances(long[] instance_pks, long series_pk)
            throws PersistenceException {
        Series series = seriesDAO.findByPrimaryKey(new Long(series_pk));
        Collection instances = series.getInstances();
        for (int i = 0; i < instance_pks.length; i++) {
            Instance instance = instDAO.findByPrimaryKey(new Long(
                    instance_pks[i]));
            Series oldSeries = instance.getSeries();
            if (oldSeries.equals(series))
                continue;
            instances.add(instance);
            seriesDAO.updateDerivedFields(oldSeries, true, true, true, true,
                    true);
            studyDAO.updateDerivedFields(oldSeries.getStudy(), true, true,
                    true, true, true, true);
        }
        seriesDAO.updateDerivedFields(series, true, true, true, true, true);
        studyDAO.updateDerivedFields(series.getStudy(), true, true, true, true,
                true, true);
        Collection col = new ArrayList();
        col.add(series);
        return getStudyMgtDataset(series.getStudy(), col, instances);
    }

    private Dataset getStudyMgtDataset(Study study, Collection series,
            Collection instances) {
        return getStudyMgtDataset(study, series, instances, 0, null);
    }

    private Dataset getStudyMgtDataset(Study study, Collection series,
            Collection instances, int chgMode, Dataset changes) {
        Dataset ds = dof.newDataset();
        ds.putUI(Tags.StudyInstanceUID, study.getStudyIuid());
        log.debug("getStudyMgtDataset: studyIUID:" + study.getStudyIuid());
        ds.putSH(Tags.AccessionNumber, study.getAccessionNumber());
        ds.putLO(Tags.PatientID, study.getPatient().getPatientId());
        ds.putLO(Tags.IssuerOfPatientID, study.getPatient()
                .getIssuerOfPatientId());
        ds.putPN(Tags.PatientName, study.getPatient().getPatientName());
        if (chgMode == CHANGE_MODE_STUDY)
            ds.putAll(changes);
        DcmElement refSeriesSeq = ds.putSQ(Tags.RefSeriesSeq);
        Iterator iter = series.iterator();
        while (iter.hasNext()) {
            Series sl = (Series) iter.next();
            Dataset dsSer = refSeriesSeq.addNewItem();
            if (chgMode == CHANGE_MODE_SERIES)
                dsSer.putAll(changes);
            dsSer.putUI(Tags.SeriesInstanceUID, sl.getSeriesIuid());
            Collection colInstances = (instances != null && series.size() == 1) ? instances
                    : sl.getInstances();
            Iterator iter2 = colInstances.iterator();
            DcmElement refSopSeq = null;
            if (iter2.hasNext())
                refSopSeq = dsSer.putSQ(Tags.RefSOPSeq);
            while (iter2.hasNext()) {
                Instance il = (Instance) iter2.next();
                Dataset dsInst = refSopSeq.addNewItem();
                if (chgMode == CHANGE_MODE_INSTANCE)
                    dsInst.putAll(changes);
                dsInst.putUI(Tags.RefSOPClassUID, il.getSopCuid());
                dsInst.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());
                dsInst.putAE(Tags.RetrieveAET, il.getRetrieveAETs());
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("return StgMgtDataset:");
            log.debug(ds);
        }
        return ds;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#getInstDAO()
     */
    public InstanceDAO getInstDAO() {
        return instDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#setInstDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstDAO(InstanceDAO instDAO) {
        this.instDAO = instDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentEdit#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }
}