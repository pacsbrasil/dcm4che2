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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.common.FileSystemStatus;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.FileDAO;
import org.dcm4che.archive.dao.FileSystemDAO;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.PrivateFileDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.dao.StudyOnFileSystemDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileDTO;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.FileSystemDTO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.PrivateFile;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.entity.StudyOnFileSystem;
import org.dcm4che.archive.service.FileSystemMgtLocal;
import org.dcm4che.archive.service.FileSystemMgtRemote;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4cheri.util.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1.6 $ $Date: 2007/07/15 16:27:08 $
 * @since 12.09.2004
 */
//EJB3
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
// Spring
@Transactional(propagation = Propagation.REQUIRED)
public class FileSystemMgtBean implements FileSystemMgtLocal, FileSystemMgtRemote {

    private static Logger log = Logger.getLogger(FileSystemMgtBean.class);

    private static final int[] IAN_PAT_TAGS = { Tags.SpecificCharacterSet,
            Tags.PatientName, Tags.PatientID };

    @EJB private PatientDAO patientDAO;

    @EJB private StudyDAO studyDAO;

    @EJB private SeriesDAO seriesDAO;

    @EJB private InstanceDAO instanceDAO;

    @EJB private StudyOnFileSystemDAO sofDAO;

    @EJB private FileDAO fileDAO;

    @EJB private PrivateFileDAO privFileDAO;

    @EJB private FileSystemDAO fileSystemDAO;

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#deletePrivateFile(java.lang.Long)
     */
    public void deletePrivateFile(Long pfPk) throws ContentDeleteException {
        privFileDAO.remove(pfPk);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getDereferencedPrivateFiles(java.lang.String,
     *      int)
     */
    public FileDTO[] getDereferencedPrivateFiles(String dirPath, int limit)
            throws PersistenceException {
        log.debug("Querying for dereferenced files in " + dirPath);
        Collection c = privFileDAO.findDereferencedInFileSystem(dirPath, limit);
        log.debug("Found " + c.size() + " dereferenced files in " + dirPath);
        return toFileDTOsPrivate(c);
    }

    private FileDTO[] toFileDTOsPrivate(Collection c) {
        FileDTO[] dto = new FileDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; ++i) {
            dto[i] = ((PrivateFile) it.next()).getFileDTO();
        }
        return dto;
    }

    private FileDTO[] toFileDTOs(Collection c) {
        FileDTO[] dto = new FileDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; ++i) {
            dto[i] = ((File) it.next()).getFileDTO();
        }
        return dto;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFilesToCompress(java.lang.String,
     *      java.lang.String, java.sql.Timestamp, int)
     */
    public FileDTO[] findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to compress in " + dirPath);
        Collection c = fileDAO
                .findFilesToCompress(dirPath, cuid, before, limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size() + " files to compress in " + dirPath);
        return toFileDTOs(c);

    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFilesForMD5Check(java.lang.String,
     *      java.sql.Timestamp, int)
     */
    public FileDTO[] findFilesForMD5Check(String dirPath, Timestamp before,
            int limit) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("Querying for files to check md5 in " + dirPath);
        Collection c = fileDAO.findToCheckMd5(dirPath, before, limit);
        if (log.isDebugEnabled())
            log
                    .debug("Found " + c.size() + " files to check md5 in "
                            + dirPath);
        return toFileDTOs(c);

    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFilesByStatusAndFileSystem(java.lang.String,
     *      int, java.sql.Timestamp, int)
     */
    public FileDTO[] findFilesByStatusAndFileSystem(String dirPath, int status,
            Timestamp before, int limit) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("Querying for files with status " + status + " in "
                    + dirPath);
        Collection c = fileDAO.findByStatusAndFileSystem(dirPath, status,
                before, limit);
        if (log.isDebugEnabled())
            log.debug("Found " + c.size() + " files with status " + status
                    + " in " + dirPath);
        return toFileDTOs(c);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateTimeOfLastMd5Check(long)
     */
    public void updateTimeOfLastMd5Check(long pk) throws PersistenceException {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        if (log.isDebugEnabled())
            log.debug("update time of last md5 check to " + ts);
        File fl = fileDAO.findByPrimaryKey(new Long(pk));
        fl.setTimeOfLastMd5Check(ts);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#replaceFile(long,
     *      java.lang.String, java.lang.String, int, byte[])
     */
    public void replaceFile(long pk, String path, String tsuid, int size,
            byte[] md5) throws PersistenceException, ContentCreateException,
            EJBException, ContentDeleteException {
        File oldFile = fileDAO.findByPrimaryKey(new Long(pk));
        oldFile.setFilePath(path);
        oldFile.setFileTsuid(tsuid);
        oldFile.setFileSize(new Long(size));
        oldFile.setFileMd5(md5);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setFileStatus(long, int)
     */
    public void setFileStatus(long pk, int status) throws PersistenceException {
        fileDAO.findByPrimaryKey(new Long(pk)).setFileStatus(status);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#addFileSystem(org.dcm4che.archive.entity.FileSystemDTO)
     */
    public FileSystemDTO addFileSystem(FileSystemDTO dto)
            throws ContentCreateException {
        return fileSystemDAO.create(dto).toDTO();
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateFileSystem(org.dcm4che.archive.entity.FileSystemDTO)
     */
    public void updateFileSystem(FileSystemDTO dto) throws PersistenceException {
        FileSystem fs = (dto.getPk() == -1) ? fileSystemDAO
                .findByDirectoryPath(dto.getDirectoryPath()) : fileSystemDAO
                .findByPrimaryKey(new Long(dto.getPk()));
        fs.fromDTO(dto);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateFileSystemStatus(java.lang.String,
     *      int)
     */
    public boolean updateFileSystemStatus(String dirPath, int status)
            throws PersistenceException {
        FileSystem fs = fileSystemDAO.findByDirectoryPath(dirPath);
        if (fs.getStatus() == status) {
            return false;
        }
        fs.setStatus(status);
        return true;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateFileSystemAvailability(java.lang.String,
     *      int)
     */
    public boolean updateFileSystemAvailability(String dirPath, int availability)
            throws PersistenceException {
        FileSystem fs = fileSystemDAO.findByDirectoryPath(dirPath);

        // If we set the file system to OFFLINE, we need to make sure its status
        // should not
        // DEF_RW.
        if (availability >= Availability.OFFLINE
                && fs.getStatus() == FileSystemStatus.DEF_RW)
            fs.setStatus(FileSystemStatus.RW);
        int oldAvail = fs.getAvailability();
        fs.setAvailability(availability);
        boolean changed = availability != oldAvail;
        return changed;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateFileSystemRetrieveAET(java.lang.String,
     *      java.lang.String)
     */
    public boolean updateFileSystemRetrieveAET(String dirPath,
            String retrieveAET) throws PersistenceException {
        FileSystem fs = fileSystemDAO.findByDirectoryPath(dirPath);

        String oldAET = fs.getRetrieveAET();
        boolean changed = oldAET == null ? retrieveAET != null : !oldAET
                .equals(retrieveAET);
        if (changed)
            fs.setRetrieveAET(retrieveAET);
        return changed;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateInstanceDerivedFields(java.lang.String,
     *      boolean, boolean, int, int)
     */
    public int updateInstanceDerivedFields(String dirPath,
            boolean retrieveAETs, boolean availability, int offset, int limit)
            throws PersistenceException {
        Collection files = fileDAO.findByFileSystem(dirPath, offset, limit);
        HashSet seriess;
        Instance instance;
        Iterator iter;
        if (files.size() > 0) {
            seriess = new HashSet();
            for (iter = files.iterator(); iter.hasNext();) {
                instance = ((File) iter.next()).getInstance();
                if (instance != null
                        && instanceDAO.updateDerivedFields(instance,
                                retrieveAETs, availability)) {
                    seriess.add(instance.getSeries());
                }
            }
            updateSeriesDerivedFields(seriess, retrieveAETs, availability);
            offset += 1000;
        }
        return files.size();
    }

    /**
     * @param seriess
     * @throws PersistenceException
     */
    private void updateSeriesDerivedFields(HashSet seriess,
            boolean retrieveAETs, boolean availability)
            throws PersistenceException {
        Series series;
        HashSet studies = new HashSet();
        for (Iterator iter = seriess.iterator(); iter.hasNext();) {
            series = (Series) iter.next();
            seriesDAO.updateDerivedFields(series, false, retrieveAETs, false,
                    false, availability);
            studies.add(series.getStudy());
        }
        Study study;
        for (Iterator iter = studies.iterator(); iter.hasNext();) {
            study = (Study) iter.next();
            studyDAO.updateDerivedFields(study, false, retrieveAETs, false,
                    false, availability, false);
        }
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFilesOnFS(java.lang.String,
     *      int, int)
     */
    public Collection getFilesOnFS(String dirPath, int offset, int limit)
            throws PersistenceException {
        Collection files = fileDAO.findByFileSystem(dirPath, offset, limit);
        Collection dtos = new ArrayList();
        for (Iterator iter = files.iterator(); iter.hasNext();) {
            dtos.add(((File) iter.next()).getFileDTO());
        }
        return dtos;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#updateFileSystem2(org.dcm4che.archive.entity.FileSystemDTO,
     *      org.dcm4che.archive.entity.FileSystemDTO)
     */
    public void updateFileSystem2(FileSystemDTO fs1, FileSystemDTO fs2)
            throws PersistenceException {
        updateFileSystem(fs1);
        updateFileSystem(fs2);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFileSystem(java.lang.Long)
     */
    public FileSystemDTO getFileSystem(Long pk) throws PersistenceException {
        return fileSystemDAO.findByPrimaryKey(pk).toDTO();
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFileSystem(java.lang.String)
     */
    public FileSystemDTO getFileSystem(String dirPath)
            throws PersistenceException {
        return fileSystemDAO.findByDirectoryPath(dirPath).toDTO();
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#removeFileSystem(java.lang.String)
     */
    public FileSystemDTO removeFileSystem(String dirPath)
            throws PersistenceException, ContentDeleteException {
        FileSystem fs = fileSystemDAO.findByDirectoryPath(dirPath);
        FileSystemDTO dto = fs.toDTO();
        removeFileSystem(fs);
        return dto;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#addAndLinkFileSystem(org.dcm4che.archive.entity.FileSystemDTO)
     */
    public FileSystemDTO addAndLinkFileSystem(FileSystemDTO dto)
            throws PersistenceException, ContentCreateException {
        FileSystem prev0 = getRWFileSystem(dto);
        FileSystem fs;
        if (prev0 == null) {
            dto.setStatus(FileSystemStatus.DEF_RW);
            fs = fileSystemDAO.create(dto);
            if (dto.getAvailability() == Availability.ONLINE) {
                fs.setNextFileSystem(fs);
            }
        }
        else {
            FileSystem prev;
            FileSystem next = prev0;
            do {
                prev = next;
                next = prev.getNextFileSystem();
            } while (next != null && !next.equals(prev0));
            fs = fileSystemDAO.create(dto);
            prev.setNextFileSystem(fs);
            fs.setNextFileSystem(next);
        }
        return fs.toDTO();
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findRWFileSystemByRetieveAETAndAvailability(java.lang.String,
     *      int)
     */
    public FileSystemDTO[] findRWFileSystemByRetieveAETAndAvailability(
            String aet, int availability) throws PersistenceException {
        return toDTO(fileSystemDAO
                .findByRetrieveAETAndAvailabilityAndStatus2(aet, availability,
                        FileSystemStatus.DEF_RW, FileSystemStatus.RW));
    }

    private FileSystem getRWFileSystem(FileSystemDTO dto)
            throws PersistenceException {
        Collection c = fileSystemDAO.findByRetrieveAETAndAvailabilityAndStatus(
                dto.getRetrieveAET(), dto.getAvailability(),
                FileSystemStatus.DEF_RW);
        if (c.isEmpty())
            return null;
        if (c.size() > 1)
            throw new PersistenceException(
                    "More than one RW+ Filesystem found for " + dto);
        return (FileSystem) c.iterator().next();
    }

    private void removeFileSystem(FileSystem fs) throws ContentDeleteException,
            PersistenceException {
        if (fileSystemDAO.countFiles(fs.getPk()) > 0
                || fileSystemDAO.countPrivateFiles(fs.getPk()) > 0)
            throw new ContentDeleteException(fs.toString() + " not empty");
        FileSystem next = fs.getNextFileSystem();
        if (next != null && fs.equals(next)) {
            next = null;
        }
        Collection prevs = fs.getPreviousFileSystems();
        for (Iterator iter = new ArrayList(prevs).iterator(); iter.hasNext();) {
            FileSystem prev = (FileSystem) iter.next();
            prev.setNextFileSystem(next);
        }
        if (fs.getStatus() == FileSystemStatus.DEF_RW && next != null
                && next.getStatus() == FileSystemStatus.RW) {
            next.setStatus(FileSystemStatus.DEF_RW);
        }
        fileSystemDAO.remove(fs);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#linkFileSystems(java.lang.String,
     *      java.lang.String)
     */
    public void linkFileSystems(String prev, String next)
            throws PersistenceException {
        FileSystem prevfs = fileSystemDAO.findByDirectoryPath(prev);
        FileSystem nextfs = (next != null && next.length() != 0) ? fileSystemDAO
                .findByDirectoryPath(next)
                : null;
        prevfs.setNextFileSystem(nextfs);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getAllFileSystems()
     */
    public FileSystemDTO[] getAllFileSystems() throws PersistenceException {
        return toDTO(fileSystemDAO.findAll());
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFileSystems(java.lang.String)
     */
    public FileSystemDTO[] findFileSystems(String retrieveAET)
            throws PersistenceException {
        return toDTO(fileSystemDAO.findByRetrieveAET(retrieveAET));
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFileSystems(java.lang.String,
     *      int, int)
     */
    public FileSystemDTO[] findFileSystems(String retrieveAET,
            int availability, int status) throws PersistenceException {
        return toDTO(fileSystemDAO.findByRetrieveAETAndAvailabilityAndStatus(
                retrieveAET, availability, status));
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFileSystems2(java.lang.String,
     *      int, int, int)
     */
    public FileSystemDTO[] findFileSystems2(String retrieveAET,
            int availability, int status, int alt) throws PersistenceException {
        return toDTO(fileSystemDAO.findByRetrieveAETAndAvailabilityAndStatus2(
                retrieveAET, availability, status, alt));
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#sizeOfFilesCreatedAfter(java.lang.Long,
     *      long)
     */
    public long sizeOfFilesCreatedAfter(Long fsPk, long after)
            throws PersistenceException {
        return fileSystemDAO
                .sizeOfFilesCreatedAfter(fsPk, new Timestamp(after));
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#findFileSystemsLikeDirectoryPath(java.lang.String,
     *      int, int)
     */
    public FileSystemDTO[] findFileSystemsLikeDirectoryPath(String dirpath,
            int availability, int status) throws PersistenceException {
        return toDTO(fileSystemDAO.findByLikeDirectoryPath(dirpath,
                availability, status));
    }

    private FileSystemDTO[] toDTO(Collection c) {
        FileSystemDTO[] dto = new FileSystemDTO[c.size()];
        Iterator it = c.iterator();
        for (int i = 0; i < dto.length; i++) {
            dto[i] = ((FileSystem) it.next()).toDTO();
        }
        return dto;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#touchStudyOnFileSystem(java.lang.String,
     *      java.lang.String)
     */
    public void touchStudyOnFileSystem(String siud, String dirPath)
            throws PersistenceException, ContentCreateException {
        try {
            sofDAO.findByStudyAndFileSystem(siud, dirPath).touch();
        }
        catch (NoResultException e) {
            try {
                sofDAO.create(studyDAO.findByStudyIuid(siud), fileSystemDAO
                        .findByDirectoryPath(dirPath));
            }
            catch (ContentCreateException ignore) {
                // Check if concurrent create
                sofDAO.findByStudyAndFileSystem(siud, dirPath).touch();
            }
        }
    }
    
    public boolean isStudyAbleToBeReleased(Study study,
            boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal, boolean flushOnROFs, int validFileStatus) {
        boolean release = (flushExternal
                && studyDAO.isStudyExternalRetrievable(study) || flushOnMedia
                && studyDAO.isStudyAvailableOnMedia(study) || flushOnROFs
                && studyDAO.isStudyAvailableOnROFs(study, validFileStatus));
        deleteUncommited = (deleteUncommited && studyDAO
                .findNumberOfCommitedInstances(study) == 0);
        
        return (release || deleteUncommited);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#releaseStudy(java.lang.Long,
     *      java.lang.Long, boolean, boolean, java.util.Collection)
     */
    public Dataset releaseStudy(Long studyPk, Long fsPk,
            boolean deleteUncommited, boolean deleteEmptyPatient,
            Collection filesToPurge) throws EJBException,
            ContentDeleteException, PersistenceException {
        Dataset ian = DcmObjectFactory.getInstance().newDataset();

        Study study = studyDAO.findByPrimaryKey(studyPk);
        FileSystem fs = fileSystemDAO.findByPrimaryKey(fsPk);
        String studyOnFsStr = log.isInfoEnabled() ? study.toString() + " on "
                + fs.toString() : null;

        final Patient patient = study.getPatient();
        Dataset patAttrs = patient.getAttributes(false);
        ian.putAll(patAttrs.subSet(IAN_PAT_TAGS));
        ian.putSH(Tags.StudyID, study.getStudyId());
        ian.putUI(Tags.StudyInstanceUID, study.getStudyIuid());
        DcmElement ppsSeq = ian.putSQ(Tags.RefPPSSeq);// We dont need this
        // information (if
        // available) at this
        // point.
        DcmElement refSerSeq = ian.putSQ(Tags.RefSeriesSeq);

        //
        // Get a list of files
        //
        Collection c = studyDAO.getFiles(study, fsPk);
        if (log.isDebugEnabled())
            log.debug("Release " + c.size() + " files from " + studyOnFsStr);
        File file;
        Instance il;
        Map seriesLocals = new HashMap();
        Map seriesSopSeq = new HashMap();
        Series sl;
        DcmElement refSopSeq;
        String fsPath = fs.getDirectoryPath();
        long size = 0;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            file = (File) iter.next();
            if (log.isDebugEnabled())
                log.debug("Release File:" + file.toString());
            size += file.getFileSize();

            il = file.getInstance();
            sl = il.getSeries();
            if (!seriesLocals.containsKey(sl.getPk())) {
                seriesLocals.put(sl.getPk(), sl);
                Dataset ds = refSerSeq.addNewItem();
                ds.putUI(Tags.SeriesInstanceUID, sl.getSeriesIuid());
                seriesSopSeq.put(sl.getPk(), refSopSeq = ds
                        .putSQ(Tags.RefSOPSeq));
            }
            else {
                refSopSeq = (DcmElement) seriesSopSeq.get(sl.getPk());
            }
            Dataset refSOP = refSopSeq.addNewItem();
            refSOP.putAE(Tags.RetrieveAET, StringUtils.split(il
                    .getRetrieveAETs(), '\\'));
            refSOP.putUI(Tags.RefSOPClassUID, il.getSopCuid());
            refSOP.putUI(Tags.RefSOPInstanceUID, il.getSopIuid());

            // Add this file to purge list
            filesToPurge.add(fsPath + '/' + file.getFilePath());

            if (!deleteUncommited) {
                // Delete the file record from database
                fileDAO.remove(file);

                instanceDAO.updateDerivedFields(il, true, true);
                int avail = il.getAvailability();
                refSOP.putCS(Tags.InstanceAvailability, Availability
                        .toString(avail));
                if (avail == Availability.OFFLINE) {
                    refSOP.putSH(Tags.StorageMediaFileSetID, il.getMedia()
                            .getFilesetId());
                    refSOP.putUI(Tags.StorageMediaFileSetUID, il.getMedia()
                            .getFilesetIuid());
                }
            }
            else {
                refSOP.putCS(Tags.InstanceAvailability, Availability
                        .toString(Availability.UNAVAILABLE));
            }
        }

        if (!deleteUncommited) {
            for (Iterator iter = seriesLocals.values().iterator(); iter
                    .hasNext();) {
                final Series ser = (Series) iter.next();
                seriesDAO.updateDerivedFields(ser, false, true, false, false,
                        true);
            }
            studyDAO.updateDerivedFields(study, false, true, false, false,
                    true, false);
            if (log.isInfoEnabled())
                log.info("Release Files of " + studyOnFsStr + " - "
                        + (size / 1000000.f) + "MB");
        }
        else {
            if (log.isInfoEnabled())
                log.info("Delete " + studyOnFsStr + " - " + (size / 1000000.f)
                        + "MB");

            // Cascade-delete the study
            // FIXME: this will delete files stored on all file systems, but
            // currently we only delete the one specified.
            studyDAO.remove(study);
            if (deleteEmptyPatient) {
                doDeleteEmptyPatient(patient);
            }
        }

        return ian;
    }
    
    /**
     * Delete a {@link StudyOnFileSystem} record in the database.
     * 
     * @param sof The primary key of the {@link StudyOnFileSystem} to be deleted.
     * 
     * @throws ContentDeleteException
     */
    public void removeStudyOnFSRecord(Long sofPk) throws ContentDeleteException {
        sofDAO.remove(sofDAO.findByPrimaryKey(sofPk));
    }

    private void doDeleteEmptyPatient(final Patient patient) {
        if (patient.getStudies().size() == 0
                && patient.getMwlItems().size() == 0
                && patient.getGpsps().size() == 0
                && patient.getMpps().size() == 0
                && patient.getGppps().size() == 0) {
            log.info("Delete empty patient:" + patient.toString());
            try {
                patientDAO.remove(patient);
            }
            catch (Exception ignore) {
                log.error("Cant remove empty patient!", ignore);
            }
        }
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getStudySize(java.lang.Long,
     *      java.lang.Long)
     */
    public long getStudySize(Long studyPk, Long fsPk)
            throws PersistenceException {
        return studyDAO.selectStudySize(studyPk, fsPk);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getStudiesOnFsByLastAccess(java.lang.String,
     *      java.sql.Timestamp)
     */
    public Collection getStudiesOnFsByLastAccess(String retrieveAET,
            Timestamp tsBefore) throws PersistenceException {
        return sofDAO.findByRetrieveAETAndAccessBefore(retrieveAET, tsBefore);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getStudiesOnFsAfterAccessTime(java.lang.String,
     *      java.sql.Timestamp, int)
     */
    public Collection getStudiesOnFsAfterAccessTime(String retrieveAET,
            java.sql.Timestamp tsAfter, int thisBatchSize)
            throws PersistenceException {
        return sofDAO.findByRetrieveAETAndAccessAfter(retrieveAET, tsAfter,
                thisBatchSize);
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#deleteWholeStudy(java.lang.String,
     *      boolean)
     */
    public FileDTO[] deleteWholeStudy(String studyIUID,
            boolean deleteEmptyPatient) throws ContentDeleteException,
            PersistenceException {
        Study study = studyDAO.findByStudyIuid(studyIUID);
        Collection<File> files = studyDAO.getAllFiles(study);
        FileDTO[] fileDTOs = new FileDTO[files.size()];
        int i = 0;
        for (Iterator iter = files.iterator(); iter.hasNext();) {
            fileDTOs[i++] = ((File) iter.next()).getFileDTO();
        }
        final Patient pat = study.getPatient();
        studyDAO.remove(study);
        if (deleteEmptyPatient) {
            doDeleteEmptyPatient(pat);
        }
        return fileDTOs;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFilesOfInstance(java.lang.String)
     */
    public FileDTO[] getFilesOfInstance(String iuid)
            throws PersistenceException {
        return toFileDTOs(instanceDAO.findBySopIuid(iuid).getFiles());
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getExternalRetrieveAET(java.lang.String)
     */
    public String getExternalRetrieveAET(String iuid)
            throws PersistenceException {
        return instanceDAO.findBySopIuid(iuid).getExternalRetrieveAET();
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFileDAO()
     */
    public FileDAO getFileDAO() {
        return fileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setFileDAO(org.dcm4che.archive.dao.FileDAO)
     */
    public void setFileDAO(FileDAO fileDAO) {
        this.fileDAO = fileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getFileSystemDAO()
     */
    public FileSystemDAO getFileSystemDAO() {
        return fileSystemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setFileSystemDAO(org.dcm4che.archive.dao.FileSystemDAO)
     */
    public void setFileSystemDAO(FileSystemDAO fileSystemDAO) {
        this.fileSystemDAO = fileSystemDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getInstanceDAO()
     */
    public InstanceDAO getInstanceDAO() {
        return instanceDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setInstanceDAO(org.dcm4che.archive.dao.InstanceDAO)
     */
    public void setInstanceDAO(InstanceDAO instanceDAO) {
        this.instanceDAO = instanceDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getPatientDAO()
     */
    public PatientDAO getPatientDAO() {
        return patientDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setPatientDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatientDAO(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getPrivFileDAO()
     */
    public PrivateFileDAO getPrivFileDAO() {
        return privFileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setPrivFileDAO(org.dcm4che.archive.dao.PrivateFileDAO)
     */
    public void setPrivFileDAO(PrivateFileDAO privFileDAO) {
        this.privFileDAO = privFileDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getSeriesDAO()
     */
    public SeriesDAO getSeriesDAO() {
        return seriesDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setSeriesDAO(org.dcm4che.archive.dao.SeriesDAO)
     */
    public void setSeriesDAO(SeriesDAO seriesDAO) {
        this.seriesDAO = seriesDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getSofDAO()
     */
    public StudyOnFileSystemDAO getSofDAO() {
        return sofDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setSofDAO(org.dcm4che.archive.dao.StudyOnFileSystemDAO)
     */
    public void setSofDAO(StudyOnFileSystemDAO sofDAO) {
        this.sofDAO = sofDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /**
     * @see org.dcm4che.archive.service.FileSystemMgt#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

}