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

package org.dcm4che.archive.service;

import java.sql.Timestamp;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.persistence.PersistenceException;

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
import org.dcm4che.archive.entity.FileDTO;
import org.dcm4che.archive.entity.FileSystemDTO;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.entity.StudyOnFileSystem;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.service.impl.FileSystemMgt
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface FileSystemMgt {

    /**
     * 
     */
    public void deletePrivateFile(Long pfPk) throws ContentDeleteException;

    /**
     * 
     */
    public FileDTO[] getDereferencedPrivateFiles(String dirPath, int limit)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public FileDTO[] findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public FileDTO[] findFilesForMD5Check(String dirPath, Timestamp before,
            int limit) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public FileDTO[] findFilesByStatusAndFileSystem(String dirPath, int status,
            Timestamp before, int limit) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public void updateTimeOfLastMd5Check(long pk) throws PersistenceException;

    /**
     * @throws ContentDeleteException
     * @throws EJBException
     * 
     */
    public void replaceFile(long pk, String path, String tsuid, int size,
            byte[] md5) throws PersistenceException, ContentCreateException,
            EJBException, ContentDeleteException;

    /**
     * 
     */
    public void setFileStatus(long pk, int status) throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO addFileSystem(FileSystemDTO dto)
            throws ContentCreateException;

    /**
     * 
     */
    public void updateFileSystem(FileSystemDTO dto) throws PersistenceException;

    /**
     * 
     */
    public boolean updateFileSystemStatus(String dirPath, int status)
            throws PersistenceException;

    /**
     * 
     */
    public boolean updateFileSystemAvailability(String dirPath, int availability)
            throws PersistenceException;

    /**
     * 
     */
    public boolean updateFileSystemRetrieveAET(String dirPath,
            String retrieveAET) throws PersistenceException;

    /**
     * 
     */
    public int updateInstanceDerivedFields(String dirPath,
            boolean retrieveAETs, boolean availability, int offset, int limit)
            throws PersistenceException;

    /**
     * 
     */
    public Collection getFilesOnFS(String dirPath, int offset, int limit)
            throws PersistenceException;

    /**
     * 
     */
    public void updateFileSystem2(FileSystemDTO fs1, FileSystemDTO fs2)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public FileSystemDTO getFileSystem(Long pk) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public FileSystemDTO getFileSystem(String dirPath)
            throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO removeFileSystem(String dirPath)
            throws PersistenceException, ContentDeleteException;

    /**
     * 
     */
    public FileSystemDTO addAndLinkFileSystem(FileSystemDTO dto)
            throws PersistenceException, ContentCreateException;

    /**
     * 
     */
    public FileSystemDTO[] findRWFileSystemByRetieveAETAndAvailability(
            String aet, int availability) throws PersistenceException;

    /**
     * 
     */
    public void linkFileSystems(String prev, String next)
            throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO[] getAllFileSystems() throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO[] findFileSystems(String retrieveAET)
            throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO[] findFileSystems(String retrieveAET,
            int availability, int status) throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO[] findFileSystems2(String retrieveAET,
            int availability, int status, int alt) throws PersistenceException;

    /**
     * 
     */
    public long sizeOfFilesCreatedAfter(Long fsPk, long after)
            throws PersistenceException;

    /**
     * 
     */
    public FileSystemDTO[] findFileSystemsLikeDirectoryPath(String dirpath,
            int availability, int status) throws PersistenceException;

    /**
     * 
     */
    public void touchStudyOnFileSystem(String siud, String dirPath)
            throws PersistenceException, ContentCreateException;

    /**
     * Check study properties to ensure it can be released.
     * 
     * @param study
     *            The {@link Study} which will be released.
     * @param deleteUncommited
     * @param flushOnMedia
     * @param flushExternal
     * @param flushOnROFs
     * @param validFileStatus
     * @return boolean True if the study can be released.
     */
    public boolean isStudyAbleToBeReleased(Study study,
            boolean deleteUncommited, boolean flushOnMedia,
            boolean flushExternal, boolean flushOnROFs, int validFileStatus);
    
    /**
     * Release a study on spcific file system.
     * 
     * @return a list of files that need to be deleted
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset releaseStudy(Long studyPk, Long fsPk,
            boolean deleteUncommited, boolean deleteEmptyPatient,
            Collection filesToPurge) throws EJBException,
            ContentDeleteException, PersistenceException;
    
    /**
     * Delete a {@link StudyOnFileSystem} record in the database.
     * 
     * @param sof The primary key of the {@link StudyOnFileSystem} to be deleted.
     * 
     * @throws ContentDeleteException
     */
    public void removeStudyOnFSRecord(Long sofPk) throws ContentDeleteException;

    /**
     * @throws PersistenceException
     * 
     */
    public long getStudySize(Long studyPk, Long fsPk)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public Collection getStudiesOnFsByLastAccess(String retrieveAET,
            Timestamp tsBefore) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     */
    public Collection getStudiesOnFsAfterAccessTime(String retrieveAET,
            java.sql.Timestamp tsAfter, int thisBatchSize)
            throws PersistenceException;

    /**
     * Delete a whole study by UID.
     * 
     * @param studyIUID
     *            The study instance UID.
     * @return Array of FileDTO objects.
     * @throws ContentDeleteException
     * @throws {@link PersistenceException}
     */
    public FileDTO[] deleteWholeStudy(String studyIUID,
            boolean deleteEmptyPatient) throws ContentDeleteException,
            PersistenceException;

    /**
     * Get all the files for a SOP instance.
     * 
     * @throws {@link PersistenceException}
     */
    public FileDTO[] getFilesOfInstance(String iuid)
            throws PersistenceException;

    /**
     * Get the external retrieve AET of an instance.
     * 
     * @throws {@link PersistenceException}
     */
    public String getExternalRetrieveAET(String iuid)
            throws PersistenceException;

    /**
     * @return the fileDAO
     */
    public FileDAO getFileDAO();

    /**
     * @param fileDAO
     *            the fileDAO to set
     */
    public void setFileDAO(FileDAO fileDAO);

    /**
     * @return the fileSystemDAO
     */
    public FileSystemDAO getFileSystemDAO();

    /**
     * @param fileSystemDAO
     *            the fileSystemDAO to set
     */
    public void setFileSystemDAO(FileSystemDAO fileSystemDAO);

    /**
     * @return the instanceDAO
     */
    public InstanceDAO getInstanceDAO();

    /**
     * @param instanceDAO
     *            the instanceDAO to set
     */
    public void setInstanceDAO(InstanceDAO instanceDAO);

    /**
     * @return the patientDAO
     */
    public PatientDAO getPatientDAO();

    /**
     * @param patientDAO
     *            the patientDAO to set
     */
    public void setPatientDAO(PatientDAO patientDAO);

    /**
     * @return the privFileDAO
     */
    public PrivateFileDAO getPrivFileDAO();

    /**
     * @param privFileDAO
     *            the privFileDAO to set
     */
    public void setPrivFileDAO(PrivateFileDAO privFileDAO);

    /**
     * @return the seriesDAO
     */
    public SeriesDAO getSeriesDAO();

    /**
     * @param seriesDAO
     *            the seriesDAO to set
     */
    public void setSeriesDAO(SeriesDAO seriesDAO);

    /**
     * @return the sofDAO
     */
    public StudyOnFileSystemDAO getSofDAO();

    /**
     * @param sofDAO
     *            the sofDAO to set
     */
    public void setSofDAO(StudyOnFileSystemDAO sofDAO);

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

}