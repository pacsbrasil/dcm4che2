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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.common.PrivateTags;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.MPPSDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.PrivateInstanceDAO;
import org.dcm4che.archive.dao.PrivatePatientDAO;
import org.dcm4che.archive.dao.PrivateSeriesDAO;
import org.dcm4che.archive.dao.PrivateStudyDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.dao.jdbc.QueryPrivateStudiesCmd;
import org.dcm4che.archive.dao.jdbc.QueryStudiesCmd;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.MPPS;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.PrivateFile;
import org.dcm4che.archive.entity.PrivateInstance;
import org.dcm4che.archive.entity.PrivatePatient;
import org.dcm4che.archive.entity.PrivateSeries;
import org.dcm4che.archive.entity.PrivateStudy;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.ContentManager;
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
 * @version $Revision: 1.2 $ $Date: 2007/06/23 18:59:01 $
 * @since 14.01.2004
 */
@Transactional(propagation = Propagation.REQUIRED)
public class ContentManagerBean implements ContentManager {

    private static final int[] MPPS_FILTER_TAGS = { Tags.PerformedStationAET,
            Tags.PerformedStationName, Tags.PPSStartDate, Tags.PPSStartTime,
            Tags.PPSEndDate, Tags.PPSEndTime, Tags.PPSStatus, Tags.PPSID,
            Tags.PPSDescription, Tags.PerformedProcedureTypeDescription,
            Tags.PerformedProtocolCodeSeq, Tags.ScheduledStepAttributesSeq,
            Tags.PPSDiscontinuationReasonCodeSeq };

    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private PatientDAO patDAO;

    private StudyDAO studyDAO;

    private SeriesDAO seriesDAO;

    private InstanceDAO instanceDAO;

    private PrivatePatientDAO privPatDAO;

    private PrivateStudyDAO privStudyDAO;

    private PrivateSeriesDAO privSeriesDAO;

    private PrivateInstanceDAO privInstanceDAO;

    private MPPSDAO mppsDAO;

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPatientByID(java.lang.String, java.lang.String)
     */
    public Dataset getPatientByID(String pid, String issuer)
            throws PersistenceException {
        Patient pat = this.getPatient(pid, issuer);
        if (pat == null) {
            return null;
        }
        else if (issuer == null && pat.getIssuerOfPatientId() != null) {
            return null;
        }
        return pat.getAttributes(true);

    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getStudy(long)
     */
    public Dataset getStudy(long studyPk) throws PersistenceException {
        return studyDAO.findByPrimaryKey(new Long(studyPk)).getAttributes(true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getStudyByIUID(java.lang.String)
     */
    public Dataset getStudyByIUID(String studyIUID) throws PersistenceException {
        return studyDAO.findByStudyIuid(studyIUID).getAttributes(true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getSeries(long)
     */
    public Dataset getSeries(long seriesPk) throws PersistenceException {
        return seriesDAO.findByPrimaryKey(new Long(seriesPk)).getAttributes(
                true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getSeriesByIUID(java.lang.String)
     */
    public Dataset getSeriesByIUID(String seriesIUID)
            throws PersistenceException {
        return seriesDAO.findBySeriesIuid(seriesIUID).getAttributes(true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getInstanceByIUID(java.lang.String)
     */
    public Dataset getInstanceByIUID(String sopiuid)
            throws PersistenceException {
        return instanceDAO.findBySopIuid(sopiuid).getAttributes(true);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#countStudies(org.dcm4che.data.Dataset, boolean)
     */
    public int countStudies(Dataset filter, boolean hideWithoutStudies) {
        try {
            return new QueryStudiesCmd(filter, hideWithoutStudies).count();
        }
        catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getInstanceInfo(java.lang.String, boolean)
     */
    public Dataset getInstanceInfo(String iuid, boolean supplement)
            throws PersistenceException {
        Instance il = instanceDAO.findBySopIuid(iuid);
        return getInstanceInfo(il, supplement);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstanceInfos(java.lang.String[], boolean)
     */
    public List listInstanceInfos(String[] iuids, boolean supplement)
            throws PersistenceException {
        Collection c = instanceDAO.listByIUIDs(iuids);
        return toDatasetList(c, supplement);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstanceInfosByPatientAndSRCode(java.lang.String, java.lang.String, java.util.Collection, java.util.Collection)
     */
    public List listInstanceInfosByPatientAndSRCode(String pid, String issuer,
            Collection codes, Collection cuids) throws PersistenceException {
        Patient pat = this.getPatient(pid, issuer);
        if (pat == null)
            return null;
        List srCodes = null;
        if (codes != null) {
            srCodes = new ArrayList(codes.size());
            Dataset ds;
            for (Iterator iter = codes.iterator(); iter.hasNext();) {
                ds = (Dataset) iter.next();
                srCodes.add(ds.getString(Tags.CodeValue) + "^"
                        + ds.getString(Tags.CodingSchemeDesignator));
            }
        }
        Collection c = instanceDAO.listByPatientAndSRCode(pat, srCodes, cuids);
        return toDatasetList(c, false);
    }

    private Patient getPatient(String pid, String issuer)
            throws PersistenceException {
        Collection col;
        if (issuer != null) {
            col = patDAO.findByPatientIdWithExactIssuer(pid, issuer);
        }
        else {
            col = patDAO.findByPatientId(pid);
        }
        if (col.isEmpty())
            return null;
        if (col.size() > 1) {
            throw new PersistenceException("Patient for pid " + pid
                    + " and issuer " + issuer + " is ambiguous!");
        }
        return (Patient) col.iterator().next();
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstanceInfosByStudyAndSRCode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public List listInstanceInfosByStudyAndSRCode(String suid, String cuid,
            String code, String designator, boolean supplement)
            throws PersistenceException {
        Collection c = instanceDAO.findByStudyAndSrCode(suid, cuid, code,
                designator);
        return toDatasetList(c, supplement);
    }

    private List toDatasetList(Collection c, boolean supplement) {
        ArrayList list = new ArrayList(c.size());
        Instance il;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            il = (Instance) iter.next();
            list.add(getInstanceInfo(il, supplement));
        }
        return list;
    }

    private Dataset getInstanceInfo(Instance il, boolean supplement) {
        Dataset ds = il.getAttributes(supplement);
        Series series = il.getSeries();
        ds.putAll(series.getAttributes(supplement));
        Study study = series.getStudy();
        ds.putAll(study.getAttributes(supplement));
        ds.putAll(study.getPatient().getAttributes(supplement));
        return ds;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listStudies(org.dcm4che.data.Dataset, boolean, boolean, int, int)
     */
    public List listStudies(Dataset filter, boolean hideWithoutStudies,
            boolean noMatchForNoValue, int offset, int limit) {
        try {
            return new QueryStudiesCmd(filter, hideWithoutStudies,
                    noMatchForNoValue).list(offset, limit);
        }
        catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#countPrivateStudies(org.dcm4che.data.Dataset, int, boolean)
     */
    public int countPrivateStudies(Dataset filter, int privateType,
            boolean hideWithoutStudies) {
        try {
            return new QueryPrivateStudiesCmd(filter, privateType,
                    hideWithoutStudies).count();
        }
        catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listPrivateStudies(org.dcm4che.data.Dataset, int, boolean, int, int)
     */
    public List listPrivateStudies(Dataset filter, int privateType,
            boolean hideWithoutStudies, int offset, int limit) {
        try {
            return new QueryPrivateStudiesCmd(filter, privateType,
                    hideWithoutStudies).list(offset, limit);
        }
        catch (SQLException e) {
            throw new EJBException(e);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listStudiesOfPatient(long)
     */
    public List listStudiesOfPatient(long patientPk)
            throws PersistenceException {
        Collection c = patDAO.findByPrimaryKey(new Long(patientPk))
                .getStudies();
        List result = new ArrayList(c.size());
        Study study;
        for (Iterator it = c.iterator(); it.hasNext();) {
            study = (Study) it.next();
            result.add(study.getAttributes(true));
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listSeriesOfStudy(long)
     */
    public List listSeriesOfStudy(long studyPk) throws PersistenceException {
        Collection c = studyDAO.findByPrimaryKey(new Long(studyPk)).getSeries();
        List result = new ArrayList(c.size());
        Series series;
        for (Iterator it = c.iterator(); it.hasNext();) {
            series = (Series) it.next();
            result.add(mergeMPPSAttr(series.getAttributes(true), series
                    .getMpps()));
        }
        return result;
    }

    /**
     * @param attributes
     * @param ppsIuid
     * @return
     */
    private Dataset mergeMPPSAttr(Dataset ds, MPPS mpps) {
        if (mpps != null) {
            ds.putAll(mpps.getAttributes().subSet(MPPS_FILTER_TAGS));
        }
        return ds;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstancesOfSeries(long)
     */
    public List listInstancesOfSeries(long seriesPk)
            throws PersistenceException {
        Collection c = instanceDAO.findBySeriesPk(new Long(seriesPk));
        List result = new ArrayList(c.size());
        Instance inst;
        for (Iterator it = c.iterator(); it.hasNext();) {
            inst = (Instance) it.next();
            result.add(inst.getAttributes(true));
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listFilesOfInstance(long)
     */
    public List listFilesOfInstance(long instancePk)
            throws PersistenceException {
        Collection c = instanceDAO.findByPrimaryKey(new Long(instancePk))
                .getFiles();
        List result = new ArrayList(c.size());
        for (Iterator it = c.iterator(); it.hasNext();) {
            File file = (File) it.next();
            result.add(file.getFileDTO());
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listStudiesOfPrivatePatient(long)
     */
    public List listStudiesOfPrivatePatient(long patientPk)
            throws PersistenceException {
        Collection c = privPatDAO.findByPrimaryKey(new Long(patientPk))
                .getStudies();
        List result = new ArrayList(c.size());
        PrivateStudy study;
        Dataset ds;
        for (Iterator it = c.iterator(); it.hasNext();) {
            study = (PrivateStudy) it.next();
            ds = study.getAttributes();
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.StudyPk, Convert.toBytes(study.getPk()
                    .longValue()));
            result.add(ds);
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listSeriesOfPrivateStudy(long)
     */
    public List listSeriesOfPrivateStudy(long studyPk)
            throws PersistenceException {
        Collection c = privStudyDAO.findByPrimaryKey(new Long(studyPk))
                .getSeries();
        List result = new ArrayList(c.size());
        PrivateSeries series;
        Dataset ds;
        Dataset refPPS;
        String ppsUID;
        for (Iterator it = c.iterator(); it.hasNext();) {
            series = (PrivateSeries) it.next();
            ds = series.getAttributes();
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.SeriesPk, Convert.toBytes(series.getPk()
                    .longValue()));
            refPPS = ds.getItem(Tags.RefPPSSeq);
            if (refPPS != null) {
                ppsUID = refPPS.getString(Tags.RefSOPInstanceUID);
                if (ppsUID != null) {
                    try {
                        this.mergeMPPSAttr(ds, mppsDAO.findBySopIuid(ppsUID));
                    }
                    catch (PersistenceException ignore) {
                    }
                }
            }
            result.add(ds);
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstancesOfPrivateSeries(long)
     */
    public List listInstancesOfPrivateSeries(long seriesPk)
            throws PersistenceException {
        Collection c = privSeriesDAO.findByPrimaryKey(new Long(seriesPk))
                .getInstances();
        List result = new ArrayList(c.size());
        PrivateInstance inst;
        Dataset ds;
        for (Iterator it = c.iterator(); it.hasNext();) {
            inst = (PrivateInstance) it.next();
            ds = inst.getAttributes();
            ds.setPrivateCreatorID(PrivateTags.CreatorID);
            ds.putOB(PrivateTags.InstancePk, Convert.toBytes(inst.getPk()
                    .longValue()));
            result.add(ds);
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listFilesOfPrivateInstance(long)
     */
    public List listFilesOfPrivateInstance(long instancePk)
            throws PersistenceException {
        Collection c = privInstanceDAO.findByPrimaryKey(new Long(instancePk))
                .getFiles();
        List result = new ArrayList(c.size());
        PrivateFile file;
        for (Iterator it = c.iterator(); it.hasNext();) {
            file = (PrivateFile) it.next();
            result.add(file.getFileDTO());
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listInstanceFilesToRecover(long)
     */
    public List[] listInstanceFilesToRecover(long pk)
            throws PersistenceException {
        List[] result = new List[] { new ArrayList(), new ArrayList() };
        addInstanceFilesToRecover(privInstanceDAO
                .findByPrimaryKey(new Long(pk)), result, null);
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listSeriesFilesToRecover(long)
     */
    public List[] listSeriesFilesToRecover(long pk) throws PersistenceException {
        List[] result = new List[] { new ArrayList(), new ArrayList() };
        addSeriesToRecover(privSeriesDAO.findByPrimaryKey(new Long(pk)),
                result, null);
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listStudyFilesToRecover(long)
     */
    public List[] listStudyFilesToRecover(long pk) throws PersistenceException {
        List[] result = new List[] { new ArrayList(), new ArrayList() };
        addStudyToRecover(privStudyDAO.findByPrimaryKey(new Long(pk)), result,
                null);
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#listPatientFilesToRecover(long)
     */
    public List[] listPatientFilesToRecover(long pk)
            throws PersistenceException {
        List[] result = new List[] { new ArrayList(), new ArrayList() };
        PrivatePatient pat = privPatDAO.findByPrimaryKey(new Long(pk));
        for (Iterator iter = pat.getStudies().iterator(); iter.hasNext();) {
            addStudyToRecover((PrivateStudy) iter.next(), result, pat
                    .getAttributes());
        }
        return result;
    }

    private void addStudyToRecover(PrivateStudy study, List[] result,
            Dataset patAttrs) throws PersistenceException {
        Dataset studyAttrs = study.getAttributes();
        if (patAttrs == null)
            patAttrs = study.getPatient().getAttributes();
        studyAttrs.putAll(patAttrs);
        for (Iterator iter = study.getSeries().iterator(); iter.hasNext();) {
            addSeriesToRecover((PrivateSeries) iter.next(), result, studyAttrs);
        }
    }

    private void addSeriesToRecover(PrivateSeries series, List[] result,
            Dataset studyAttrs) throws PersistenceException {
        Dataset seriesAttrs = series.getAttributes();
        if (studyAttrs == null) {
            studyAttrs = series.getStudy().getAttributes();
            studyAttrs.putAll(series.getStudy().getPatient().getAttributes());
        }
        seriesAttrs.putAll(studyAttrs);
        for (Iterator iter = series.getInstances().iterator(); iter.hasNext();) {
            addInstanceFilesToRecover((PrivateInstance) iter.next(), result,
                    seriesAttrs);
        }
    }

    private void addInstanceFilesToRecover(PrivateInstance instance,
            List[] result, Dataset seriesAttrs) throws PersistenceException {
        Dataset instanceAttrs = instance.getAttributes();
        if (seriesAttrs == null) {
            seriesAttrs = instance.getSeries().getAttributes();
            seriesAttrs.putAll(instance.getSeries().getStudy().getAttributes());
            seriesAttrs.putAll(instance.getSeries().getStudy().getPatient()
                    .getAttributes());
        }
        instanceAttrs.putAll(seriesAttrs);
        Iterator iter = listFilesOfPrivateInstance(instance.getPk().longValue())
                .iterator();
        if (iter.hasNext()) {
            result[0].add(iter.next());
            result[1].add(instanceAttrs);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getSOPInstanceRefMacro(long, boolean)
     */
    public Dataset getSOPInstanceRefMacro(long studyPk, boolean insertModality)
            throws PersistenceException {
        Dataset ds = dof.newDataset();
        Study sl = studyDAO.findByPrimaryKey(new Long(studyPk));
        ds.putUI(Tags.StudyInstanceUID, sl.getStudyIuid());
        DcmElement refSerSq = ds.putSQ(Tags.RefSeriesSeq);
        Iterator iterSeries = sl.getSeries().iterator();
        Series series;
        String aet;
        int pos;
        while (iterSeries.hasNext()) {
            series = (Series) iterSeries.next();
            Dataset serDS = refSerSq.addNewItem();
            serDS.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid());
            aet = series.getRetrieveAETs();
            if (aet != null) {
                pos = aet.indexOf('\\');
                if (pos != -1)
                    aet = aet.substring(0, pos);
            }
            serDS.putAE(Tags.RetrieveAET, aet);
            serDS.putAE(Tags.StorageMediaFileSetID, series.getFilesetId());
            serDS.putAE(Tags.StorageMediaFileSetUID, series.getFilesetIuid());
            if (insertModality) {
                serDS.putCS(Tags.Modality, series.getModality());
                serDS.putIS(Tags.SeriesNumber, series.getSeriesNumber()); // Q&D
            }
            DcmElement refSopSq = serDS.putSQ(Tags.RefSOPSeq);
            Collection col = series.getInstances();
            List l = (col instanceof List) ? (List) col : new ArrayList(col);
            Collections.sort(l, new InstanceNumberComparator());
            Iterator iterInstances = l.iterator();
            Instance instance;
            while (iterInstances.hasNext()) {
                instance = (Instance) iterInstances.next();
                Dataset instDS = refSopSq.addNewItem();
                instDS.putUI(Tags.RefSOPInstanceUID, instance.getSopIuid());
                instDS.putUI(Tags.RefSOPClassUID, instance.getSopCuid());
            }
        }
        return ds;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getSOPInstanceRefMacros(java.util.Collection)
     */
    public Collection getSOPInstanceRefMacros(Collection instanceUIDs)
            throws PersistenceException {
        HashMap result = new HashMap();
        HashMap mapRefSopSQ = new HashMap();
        Instance instance;
        Series series;
        Study study;
        Object o;
        for (Iterator iter = instanceUIDs.iterator(); iter.hasNext();) {
            o = iter.next();
            instance = (o instanceof Long) ? instanceDAO
                    .findByPrimaryKey((Long) o) : instanceDAO.findBySopIuid(o
                    .toString());
            series = instance.getSeries();
            study = series.getStudy();
            Dataset ds = (Dataset) result.get(study.getPk());
            if (ds == null) {
                ds = dof.newDataset();
                ds.putUI(Tags.StudyInstanceUID, study.getStudyIuid());
                ds.putSQ(Tags.RefSeriesSeq);
                result.put(study.getPk(), ds);
            }
            DcmElement refSopSq = (DcmElement) mapRefSopSQ.get(series.getPk());
            if (refSopSq == null) {
                DcmElement refSeriesSq = ds.get(Tags.RefSeriesSeq);
                Dataset serDS = refSeriesSq.addNewItem();
                serDS.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid());
                String aet = series.getRetrieveAETs();
                if (aet != null) {
                    int pos = aet.indexOf('\\');
                    if (pos != -1)
                        aet = aet.substring(0, pos);
                    serDS.putAE(Tags.RetrieveAET, aet);
                }
                serDS.putAE(Tags.StorageMediaFileSetID, series.getFilesetId());
                serDS.putAE(Tags.StorageMediaFileSetUID, series
                        .getFilesetIuid());
                refSopSq = serDS.putSQ(Tags.RefSOPSeq);
                mapRefSopSQ.put(series.getPk(), refSopSq);
            }
            Dataset instDS = refSopSq.addNewItem();
            instDS.putUI(Tags.RefSOPInstanceUID, instance.getSopIuid());
            instDS.putUI(Tags.RefSOPClassUID, instance.getSopCuid());
        }
        return result.values();
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPatientForStudy(long)
     */
    public Dataset getPatientForStudy(long studyPk) throws PersistenceException {
        Study sl = studyDAO.findByPrimaryKey(new Long(studyPk));
        return sl.getPatient().getAttributes(false);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPatientForStudy(java.lang.String)
     */
    public Dataset getPatientForStudy(String studyIUID)
            throws PersistenceException {
        Study sl = studyDAO.findByStudyIuid(studyIUID);
        return sl.getPatient().getAttributes(false);
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#isStudyAvailable(long, int)
     */
    public boolean isStudyAvailable(long studyPk, int availability)
            throws PersistenceException {
        Study study = studyDAO.findByPrimaryKey(new Long(studyPk));
        return studyDAO.isStudyAvailable(study, availability);
    }

    public class InstanceNumberComparator implements Comparator {

        public InstanceNumberComparator() {
        }

        /**
         * Compares the instance number of two Instance objects.
         * <p>
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         * <p>
         * Throws an Exception if one of the arguments is null or neither a
         * InstanceContainer or Instance object.<br>
         * Also both arguments must be of the same type!
         * <p>
         * If arguments are of type Instance, the getInstanceSize Method of
         * InstanceCollector is used to get filesize.
         * 
         * @param arg0
         *            First argument
         * @param arg1
         *            Second argument
         * 
         * @return <0 if arg0<arg1, 0 if equal and >0 if arg0>arg1
         */
        public int compare(Object arg0, Object arg1) {
            String in0 = ((Instance) arg0).getInstanceNumber();
            String in1 = ((Instance) arg1).getInstanceNumber();
            if (in0 == null) {
                return in1 == null ? 0 : 1;
            }
            else if (in1 == null) {
                return 0;
            }
            else {
                return new Integer(in0).compareTo(new Integer(in1));
            }
        }

    }// end class

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getInstanceDAO()
     */
    public InstanceDAO getInstanceDAO() {
        return instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setInstanceDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstanceDAO(InstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getMppsDAO()
     */
    public MPPSDAO getMppsDAO() {
        return mppsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setMppsDAO(org.dcm4che.archive.dao.MPPSDAO)
     */
    public void setMppsDAO(MPPSDAO mppsDAO) {
        this.mppsDAO = mppsDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPrivInstanceDAO()
     */
    public PrivateInstanceDAO getPrivInstanceDAO() {
        return privInstanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setPrivInstanceDAO(org.dcm4che.archive.dao.PrivateInstanceDAO)
     */
    public void setPrivInstanceDAO(PrivateInstanceDAO privInstanceDAO) {
        this.privInstanceDAO = privInstanceDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPrivPatDAO()
     */
    public PrivatePatientDAO getPrivPatDAO() {
        return privPatDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setPrivPatDAO(org.dcm4che.archive.dao.PrivatePatientDAO)
     */
    public void setPrivPatDAO(PrivatePatientDAO privPatDAO) {
        this.privPatDAO = privPatDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPrivSeriesDAO()
     */
    public PrivateSeriesDAO getPrivSeriesDAO() {
        return privSeriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setPrivSeriesDAO(org.dcm4che.archive.dao.PrivateSeriesDAO)
     */
    public void setPrivSeriesDAO(PrivateSeriesDAO privSeriesDAO) {
        this.privSeriesDAO = privSeriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getPrivStudyDAO()
     */
    public PrivateStudyDAO getPrivStudyDAO() {
        return privStudyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setPrivStudyDAO(org.dcm4che.archive.dao.PrivateStudyDAO)
     */
    public void setPrivStudyDAO(PrivateStudyDAO privStudyDAO) {
        this.privStudyDAO = privStudyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.ContentManager#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

}
