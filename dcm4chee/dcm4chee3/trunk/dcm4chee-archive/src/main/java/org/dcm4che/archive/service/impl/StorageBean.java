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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.common.SeriesStored;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.FileDAO;
import org.dcm4che.archive.dao.FileSystemDAO;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.dao.StudyOnFileSystemDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.service.Storage;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DcmServiceException;
import org.dcm4cheri.util.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Storage Bean
 * 
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger </a>
 * @version $Revision: 1.4 $ $Date: 2007/07/12 19:17:25 $
 */
@Transactional(propagation = Propagation.REQUIRED)
public class StorageBean implements Storage {

    private static final int STORED = 0;

    private static final int RECEIVED = 1;

    private static Logger log = Logger.getLogger(StorageBean.class);

    private PatientDAO patDAO;

    private StudyDAO studyDAO;

    private SeriesDAO seriesDAO;

    private InstanceDAO instDAO;

    private FileDAO fileDAO;

    private FileSystemDAO fileSystemDAO;

    private StudyOnFileSystemDAO sofDAO;

    private static final int MAX_PK_CACHE_ENTRIES = 100;

    private static Map seriesPkCache = Collections
            .synchronizedMap(new LinkedHashMap() {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > MAX_PK_CACHE_ENTRIES;
                }
            });

    /**
     * @see org.dcm4che.archive.service.Storage#store(org.dcm4che.data.Dataset,
     *      long, java.lang.String, long, byte[], boolean)
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = DcmServiceException.class)
    public Dataset store(Dataset ds, long fspk, String fileid, long size,
            byte[] md5, boolean updateStudyAccessTime)
            throws DcmServiceException {
        FileMetaInfo fmi = ds.getFileMetaInfo();
        final String iuid = fmi.getMediaStorageSOPInstanceUID();
        final String cuid = fmi.getMediaStorageSOPClassUID();
        final String tsuid = fmi.getTransferSyntaxUID();
        log.info("inserting instance " + fmi);

        try {
            Dataset coercedElements = DcmObjectFactory.getInstance()
                    .newDataset();
            FileSystem fs = fileSystemDAO.findByPrimaryKey(new Long(fspk));
            Instance instance;
            try {
                instance = instDAO.findBySopIuid(iuid);
                coerceInstanceIdentity(instance, ds, coercedElements);
            }
            catch (NoResultException onfe) {
                instance = instDAO.create(ds,
                        getSeries(ds, coercedElements, fs));
            }
            File file = fileDAO.create(fileid, tsuid, size, md5, 0, instance,
                    fs);
            instance.setAvailability(Availability.ONLINE);
            instance.addRetrieveAET(fs.getRetrieveAET());
            instance.setInstanceStatus(RECEIVED);
            instance.getSeries().setSeriesStatus(RECEIVED);
            if (updateStudyAccessTime) {
                touchStudyOnFileSystem(ds.getString(Tags.StudyInstanceUID), fs);
            }
            log.info("inserted records for instance[uid=" + iuid + "]");
            return coercedElements;
        }
        catch (Throwable e) {
            log.error("inserting records for instance[uid=" + iuid
                    + "] failed:", e);
            if (e instanceof DcmServiceException) {
                throw (DcmServiceException) e;
            }
            else {
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        }
    }

    private void touchStudyOnFileSystem(String siud, FileSystem fs)
            throws ContentCreateException, PersistenceException {
        String dirPath = fs.getDirectoryPath();
        try {
            sofDAO.findByStudyAndFileSystem(siud, dirPath).touch();
        }
        catch (NoResultException e) {
            try {
                sofDAO.create(studyDAO.findByStudyIuid(siud), fs);
            }
            catch (ContentCreateException ignore) {
                // Check if concurrent create
                sofDAO.findByStudyAndFileSystem(siud, dirPath).touch();
            }
        }
    }

    /**
     * @see org.dcm4che.archive.service.Storage#makeSeriesStored(java.lang.String)
     */
    public SeriesStored makeSeriesStored(String seriuid)
            throws PersistenceException {
        Series series = findBySeriesIuid(seriuid);
        return makeSeriesStored(series);
    }

    /**
     * @see org.dcm4che.archive.service.Storage#commitSeriesStored(org.dcm4che.archive.common.SeriesStored)
     */
    public void commitSeriesStored(SeriesStored seriesStored)
            throws PersistenceException {
        Dataset ian = seriesStored.getIAN();
        Dataset refSeries = ian.get(Tags.RefSeriesSeq).getItem(0);
        DcmElement refSOPs = refSeries.get(Tags.RefSOPSeq);
        int numI = refSOPs.countItems();
        HashSet iuids = new HashSet(numI * 4 / 3 + 1);
        for (int i = 0; i < numI; i++) {
            iuids.add(refSOPs.getItem(i).getString(Tags.RefSOPInstanceUID));
        }
        String seriuid = refSeries.getString(Tags.SeriesInstanceUID);
        Series series = findBySeriesIuid(seriuid);
        Collection c = series.getInstances();
        int remaining = 0;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Instance inst = (Instance) iter.next();
            if (inst.getInstanceStatus() != RECEIVED) {
                continue;
            }
            if (iuids.remove(inst.getSopIuid())) {
                inst.setInstanceStatus(STORED);
            }
            else {
                ++remaining;
            }
        }
        if (remaining == 0) {
            series.setSeriesStatus(STORED);
        }
    }

    /**
     * @see org.dcm4che.archive.service.Storage#checkSeriesStored(long)
     */
    public SeriesStored[] checkSeriesStored(long maxPendingTime)
            throws PersistenceException {
        Timestamp before = new Timestamp(System.currentTimeMillis()
                - maxPendingTime);
        Collection c = seriesDAO.findByStatusReceivedBefore(RECEIVED, before);
        if (c.isEmpty()) {
            return new SeriesStored[0];
        }
        log.info("Found " + c.size() + " Stored Series");
        ArrayList list = new ArrayList(c.size());
        SeriesStored seriesStored;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            seriesStored = makeSeriesStored((Series) iter.next());
            if (seriesStored != null) {
                list.add(seriesStored);
            }
        }
        return (SeriesStored[]) list.toArray(new SeriesStored[list.size()]);
    }

    private SeriesStored makeSeriesStored(Series series) {
        Study study = series.getStudy();
        Patient pat = study.getPatient();
        Dataset seriesAttrs = series.getAttributes(true);
        Dataset studyAttrs = study.getAttributes(true);
        Dataset patAttrs = pat.getAttributes(true);
        Dataset ian = DcmObjectFactory.getInstance().newDataset();
        ian.putUI(Tags.StudyInstanceUID, study.getStudyIuid());
        DcmElement refPPSSeq = ian.putSQ(Tags.RefPPSSeq);
        Dataset pps = seriesAttrs.getItem(Tags.RefPPSSeq);
        if (pps != null) {
            if (!pps.contains(Tags.PerformedWorkitemCodeSeq)) {
                pps.putSQ(Tags.PerformedWorkitemCodeSeq);
            }
            refPPSSeq.addItem(pps);
        }
        Dataset refSeries = ian.putSQ(Tags.RefSeriesSeq).addNewItem();
        DcmElement refSOPs = refSeries.putSQ(Tags.RefSOPSeq);
        refSeries.putUI(Tags.SeriesInstanceUID, series.getSeriesIuid());
        Collection c = series.getInstances();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Instance inst = (Instance) iter.next();
            if (inst.getInstanceStatus() != RECEIVED) {
                continue;
            }
            Dataset refSOP = refSOPs.addNewItem();
            refSOP.putUI(Tags.RefSOPClassUID, inst.getSopCuid());
            refSOP.putUI(Tags.RefSOPInstanceUID, inst.getSopIuid());
            refSOP.putAE(Tags.RetrieveAET, StringUtils.split(inst
                    .getRetrieveAETs(), '\\')[0]);
            refSOP.putCS(Tags.InstanceAvailability, "ONLINE");
        }
        if (refSOPs.countItems() == 0) {
            return null;
        }
        SeriesStored seriesStored = new SeriesStored(patAttrs, studyAttrs,
                seriesAttrs, ian);
        return seriesStored;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#storeFile(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String, int, byte[],
     *      int)
     */
    public void storeFile(String iuid, String tsuid, String dirpath,
            String fileid, int size, byte[] md5, int status)
            throws ContentCreateException, PersistenceException {
        FileSystem fs = fileSystemDAO.findByDirectoryPath(dirpath);
        Instance instance = instDAO.findBySopIuid(iuid);
        fileDAO.create(fileid, tsuid, size, md5, status, instance, fs);
    }

    private Series getSeries(Dataset ds, Dataset coercedElements, FileSystem fs)
            throws Exception {
        final String uid = ds.getString(Tags.SeriesInstanceUID);
        Series series;
        try {
            series = findBySeriesIuid(uid);
            coerceSeriesIdentity(series, ds, coercedElements);
        }
        catch (NoResultException onfe) {
            try {
                return seriesDAO.create(ds, getStudy(ds, coercedElements, fs));
            }
            catch (ContentCreateException e1) {
                // check if Series record was inserted by concurrent thread
                try {
                    series = findBySeriesIuid(uid);
                }
                catch (Throwable e2) {
                    log.error(e2.getMessage());
                    throw e1;
                }
            }
        }
        coerceSeriesIdentity(series, ds, coercedElements);
        return series;
    }

    private Study getStudy(Dataset ds, Dataset coercedElements, FileSystem fs)
            throws Exception {
        final String uid = ds.getString(Tags.StudyInstanceUID);
        Study study;
        try {
            study = studyDAO.findByStudyIuid(uid);
        }
        catch (NoResultException onfe) {
            study = studyDAO.create(ds, getPatient(ds, coercedElements));
            sofDAO.create(study, fs);
            try {
                study = studyDAO.create(ds, getPatient(ds, coercedElements));
                sofDAO.create(study, fs);
                return study;
            }
            catch (ContentCreateException e1) {
                // check if Study record was inserted by concurrent thread
                try {
                    study = studyDAO.findByStudyIuid(uid);
                }
                catch (Throwable e2) {
                    log.error(e2.getMessage());
                    throw e1;
                }
            }
        }

        coerceStudyIdentity(study, ds, coercedElements);
        return study;
    }

    private Patient getPatient(Dataset ds, Dataset coercedElements)
            throws Exception {
        Patient pat;
        try {
            pat = patDAO.searchFor(ds, true);
        }
        catch (NoResultException e) {
            try {
                return patDAO.create(ds);
            }
            catch (ContentCreateException e1) {
                // check if Patient record was inserted by concurrent thread
                try {
                    pat = patDAO.searchFor(ds, true);
                }
                catch (Throwable e2) {
                    log.error(e2.getMessage());
                    throw e1;
                }
            }
        }

        coercePatientIdentity(pat, ds, coercedElements);
        return pat;
    }

    private void coercePatientIdentity(Patient patient, Dataset ds,
            Dataset coercedElements) throws DcmServiceException,
            ContentCreateException {
        patient.coerceAttributes(ds, coercedElements);
    }

    private void coerceStudyIdentity(Study study, Dataset ds,
            Dataset coercedElements) throws DcmServiceException,
            ContentCreateException {
        coercePatientIdentity(study.getPatient(), ds, coercedElements);
        study.coerceAttributes(ds, coercedElements);
    }

    private void coerceSeriesIdentity(Series series, Dataset ds,
            Dataset coercedElements) throws DcmServiceException,
            ContentCreateException {
        coerceStudyIdentity(series.getStudy(), ds, coercedElements);
        series.coerceAttributes(ds, coercedElements);
    }

    private void coerceInstanceIdentity(Instance instance, Dataset ds,
            Dataset coercedElements) throws DcmServiceException,
            ContentCreateException {
        coerceSeriesIdentity(instance.getSeries(), ds, coercedElements);
        instance.coerceAttributes(ds, coercedElements);
    }

    /**
     * @see org.dcm4che.archive.service.Storage#commit(java.lang.String)
     */
    public void commit(String iuid) throws PersistenceException {
        instDAO.findBySopIuid(iuid).setCommitment(true);
    }

    /**
     * @see org.dcm4che.archive.service.Storage#commited(org.dcm4che.data.Dataset)
     */
    public void commited(Dataset stgCmtResult) throws PersistenceException {
        DcmElement refSOPSeq = stgCmtResult.get(Tags.RefSOPSeq);
        if (refSOPSeq == null)
            return;
        HashSet seriesSet = new HashSet();
        HashSet studySet = new HashSet();
        final String aet0 = stgCmtResult.getString(Tags.RetrieveAET);
        for (int i = 0, n = refSOPSeq.countItems(); i < n; ++i) {
            final Dataset refSOP = refSOPSeq.getItem(i);
            final String iuid = refSOP.getString(Tags.RefSOPInstanceUID);
            final String aet = refSOP.getString(Tags.RetrieveAET, aet0);
            if (iuid != null && aet != null)
                commited(seriesSet, studySet, iuid, aet);
        }
        for (Iterator series = seriesSet.iterator(); series.hasNext();) {
            final Series ser = findBySeriesIuid((String) series.next());
            seriesDAO
                    .updateDerivedFields(ser, false, false, true, false, false);
        }
        for (Iterator studies = studySet.iterator(); studies.hasNext();) {
            final Study study = studyDAO.findByStudyIuid((String) studies
                    .next());
            studyDAO.updateDerivedFields(study, false, false, true, false,
                    false, false);
        }
    }

    private void commited(HashSet seriesSet, HashSet studySet,
            final String iuid, final String aet) throws PersistenceException {
        Instance inst = instDAO.findBySopIuid(iuid);
        inst.setExternalRetrieveAET(aet);
        Series series = inst.getSeries();
        seriesSet.add(series.getSeriesIuid());
        Study study = series.getStudy();
        studySet.add(study.getStudyIuid());
    }

    /**
     * @see org.dcm4che.archive.service.Storage#updateStudy(java.lang.String)
     */
    public void updateStudy(String iuid) throws PersistenceException {
        final Study study = studyDAO.findByStudyIuid(iuid);
        studyDAO
                .updateDerivedFields(study, true, true, false, true, true, true);
    }

    /**
     * @see org.dcm4che.archive.service.Storage#updateSeries(java.lang.String)
     */
    public void updateSeries(String iuid) throws PersistenceException {
        final Series series = findBySeriesIuid(iuid);
        seriesDAO.updateDerivedFields(series, true, true, false, true, true);
    }

    /**
     * @see org.dcm4che.archive.service.Storage#deleteInstances(java.lang.String[],
     *      boolean, boolean)
     */
    public void deleteInstances(String[] iuids, boolean deleteSeries,
            boolean deleteStudy) throws PersistenceException, EJBException,
            ContentDeleteException {
        for (int i = 0; i < iuids.length; i++) {
            Instance inst = instDAO.findBySopIuid(iuids[i]);
            Series series = inst.getSeries();
            Study study = series.getStudy();
            instDAO.remove(inst);
            seriesDAO.updateDerivedFields(series, true, true, true, true, true);
            if (deleteSeries && series.getNumberOfSeriesRelatedInstances() == 0)
                seriesDAO.remove(series);
            studyDAO.updateDerivedFields(study, true, true, true, true, true,
                    true);
            if (deleteStudy && study.getNumberOfStudyRelatedSeries() == 0)
                studyDAO.remove(study);
        }
    }

    private Series findBySeriesIuid(String uid) throws PersistenceException {
        Long pk = (Long) seriesPkCache.get(uid);
        if (pk != null) {
            try {
                return seriesDAO.findByPrimaryKey(pk);
            }
            catch (NoResultException x) {
                log.warn("Series " + uid
                        + " not found with cached pk! Cache entry removed!");
                seriesPkCache.remove(uid);
            }
        }
        Series ser = seriesDAO.findBySeriesIuid(uid);
        seriesPkCache.put(uid, ser.getPk());
        return ser;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#patientExistsWithDifferentDetails(org.dcm4che.data.Dataset,
     *      int[])
     */
    public boolean patientExistsWithDifferentDetails(Dataset ds,
            int[] detailTags) throws PersistenceException {
        String pid = ds.getString(Tags.PatientID);
        String issuer = ds.getString(Tags.IssuerOfPatientID);
        Collection c = issuer != null ? patDAO.findByPatientIdWithIssuer(pid,
                issuer) : patDAO.findByPatientId(pid);
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Patient patient = (Patient) iter.next();
            if (!checkDetails(ds, patient.getAttributes(false), detailTags)) {
                String suid = null;
                suid = ds.getString(Tags.StudyInstanceUID);
                if (suid != null) {
                    try {
                        studyDAO.findByStudyIuid(suid);
                        log
                                .info("Different patient details found but Study Instance UID ("
                                        + suid
                                        + ") already exists! Patient ID not changed!");
                        return false;
                    }
                    catch (NoResultException ignore) {
                    }
                }
                suid = ds.getString(Tags.SeriesInstanceUID);
                if (suid != null) {
                    try {
                        seriesDAO.findBySeriesIuid(suid);
                        log
                                .info("Different patient details found but Series Instance UID ("
                                        + suid
                                        + ") already exists! Patient ID not changed!");
                        return false;
                    }
                    catch (NoResultException ignore) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Check if given Detail Attributes are equal in both Datasets.
     * <p>
     * PN attributes are checked case insensitive!
     * <p>
     * SQ attributes are checked via SQElement equals method. (therefore PN
     * attributes within a Sequence are NOT checked case insensitive!)
     * 
     * @param ds1
     *            Dataset 1: missing detailAttributes are not checked
     * @param ds2
     *            Dataset 2: missing detailAttributes are checked if also
     *            missing in ds1!
     * @param detailTags
     *            List of detail attributes to check
     * 
     * @return true if all given details are equal.
     */
    private boolean checkDetails(Dataset ds1, Dataset ds2, int[] detailTags) {
        DcmElement elem1, elem2;
        int tag;
        for (int i = 0; i < detailTags.length; i++) {
            tag = detailTags[i];
            elem1 = ds1.get(tag);
            if (elem1 != null) {
                elem2 = ds2.get(tag);
                if (elem2 == null)
                    return false; //
                if (elem1.vr() == VRs.PN) {
                    if (!ds1.getString(tag)
                            .equalsIgnoreCase(ds2.getString(tag)))
                        return false;
                }
                else {
                    if (!elem1.equals(elem2))
                        return false;
                }
            } // else ignore detail check for attributes that are not in ds1!
        }
        return true;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getFileDAO()
     */
    public FileDAO getFileDAO() {
        return fileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setFileDAO(org.dcm4che.archive.dao.FileDAO)
     */
    public void setFileDAO(FileDAO fileDAO) {
        this.fileDAO = fileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getFileSystemDAO()
     */
    public FileSystemDAO getFileSystemDAO() {
        return fileSystemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setFileSystemDAO(org.dcm4che.archive.dao.FileSystemDAO)
     */
    public void setFileSystemDAO(FileSystemDAO fileSystemDAO) {
        this.fileSystemDAO = fileSystemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getInstDAO()
     */
    public InstanceDAO getInstDAO() {
        return instDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setInstDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstDAO(InstanceDAO instDAO) {
        this.instDAO = instDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getSofDAO()
     */
    public StudyOnFileSystemDAO getSofDAO() {
        return sofDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setSofDAO(org.dcm4che.archive.dao.StudyOnFileSystemDAO)
     */
    public void setSofDAO(StudyOnFileSystemDAO sofDAO) {
        this.sofDAO = sofDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /**
     * @see org.dcm4che.archive.service.Storage#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

}
