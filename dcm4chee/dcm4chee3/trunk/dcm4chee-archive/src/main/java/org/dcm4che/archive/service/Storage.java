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

import javax.ejb.EJBException;
import javax.persistence.PersistenceException;

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
import org.dcm4che.data.Dataset;
import org.dcm4che.net.DcmServiceException;

/**
 * org.dcm4che.archive.service.impl.Storage
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface Storage {

    /**
     * 
     */
    public Dataset store(Dataset ds, long fspk, String fileid, long size,
            byte[] md5, boolean updateStudyAccessTime) throws DcmServiceException;

    /**
     * 
     */
    public SeriesStored makeSeriesStored(String seriuid)
            throws PersistenceException;

    /**
     * 
     */
    public void commitSeriesStored(SeriesStored seriesStored)
            throws PersistenceException;

    /**
     * 
     */
    public SeriesStored[] checkSeriesStored(long maxPendingTime)
            throws PersistenceException;

    /**
     * 
     */
    public void storeFile(String iuid, String tsuid, String dirpath,
            String fileid, int size, byte[] md5, int status)
            throws ContentCreateException, PersistenceException;

    /**
     * 
     */
    public void commit(String iuid) throws PersistenceException;

    /**
     * 
     */
    public void commited(Dataset stgCmtResult) throws PersistenceException;

    /**
     * 
     */
    public void updateStudy(String iuid) throws PersistenceException;

    /**
     * 
     */
    public void updateSeries(String iuid) throws PersistenceException;

    /**
     * 
     */
    public void deleteInstances(String[] iuids, boolean deleteSeries,
            boolean deleteStudy) throws PersistenceException, EJBException,
            ContentDeleteException;

    /**
     * @throws PersistenceException
     * 
     */
    public boolean patientExistsWithDifferentDetails(Dataset ds,
            int[] detailTags) throws PersistenceException;

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
     * @return the instDAO
     */
    public InstanceDAO getInstDAO();

    /**
     * @param instDAO
     *            the instDAO to set
     */
    public void setInstDAO(InstanceDAO instDAO);

    /**
     * @return the patDAO
     */
    public PatientDAO getPatDAO();

    /**
     * @param patDAO
     *            the patDAO to set
     */
    public void setPatDAO(PatientDAO patDAO);

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