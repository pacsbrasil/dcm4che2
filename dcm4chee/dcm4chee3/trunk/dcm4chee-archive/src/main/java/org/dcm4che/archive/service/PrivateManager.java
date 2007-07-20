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

import java.rmi.RemoteException;
import java.util.Collection;

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.FileDAO;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.PrivateFileDAO;
import org.dcm4che.archive.dao.PrivateInstanceDAO;
import org.dcm4che.archive.dao.PrivatePatientDAO;
import org.dcm4che.archive.dao.PrivateSeriesDAO;
import org.dcm4che.archive.dao.PrivateStudyDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.service.impl.PrivateManager
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface PrivateManager {

    /**
     * 
     * @throws PersistenceException
     */
    public void deletePrivateSeries(long series_pk)
            throws ContentDeleteException;

    /**
     * 
     * @throws PersistenceException
     */

    public Collection deletePrivateStudy(long study_pk)
            throws ContentDeleteException;

    public void deletePrivatePatient(long patient_pk)
            throws ContentDeleteException;

    /**
     * 
     * @throws PersistenceException
     * 
     * @throws PersistenceException
     * 
     * 
     * 
     */

    public void deletePrivateInstance(long instance_pk)
            throws ContentDeleteException;

    public void deletePrivateFile(long file_pk) throws RemoteException;

    public void deletePrivateFiles(Collection fileDTOs)
            throws ContentDeleteException;

    public void deleteAll(int privateType) throws ContentDeleteException;

    /**
     * 
     * Delete a list of instances, i.e., move them to trash bin
     * 
     * @param iuids
     *            A list of instance uid
     * 
     * @param cascading
     *            True to delete the series/study if there's no instance/series
     * 
     * @return a collection of Dataset containing the actuall detetion
     *         information per study
     * 
     * @throws ContentDeleteException
     */
    public Collection moveInstancesToTrash(String[] iuids, boolean cascading)
            throws ContentDeleteException;

    public Dataset moveInstanceToTrash(long instance_pk)
            throws ContentDeleteException;

    public Dataset moveSeriesToTrash(long series_pk)
            throws ContentDeleteException;

    public Collection moveSeriesOfPPSToTrash(String ppsIUID,
            boolean removeEmptyParents);

    /**
     * @param iuid
     * @return
     * @throws ContentDeleteException
     */
    public Dataset moveStudyToTrash(String iuid) throws ContentDeleteException;

    public Dataset moveStudyToTrash(long study_pk)
            throws ContentDeleteException;

    public Collection movePatientToTrash(long pat_pk) throws RemoteException;

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
     * @return the privFileDAO
     */
    public PrivateFileDAO getPrivFileDAO();

    /**
     * @param privFileDAO
     *            the privFileDAO to set
     */
    public void setPrivFileDAO(PrivateFileDAO privFileDAO);

    /**
     * @return the privInstDAO
     */
    public PrivateInstanceDAO getPrivInstDAO();

    /**
     * @param privInstDAO
     *            the privInstDAO to set
     */
    public void setPrivInstDAO(PrivateInstanceDAO privInstDAO);

    /**
     * @return the privPatDAO
     */
    public PrivatePatientDAO getPrivPatDAO();

    /**
     * @param privPatDAO
     *            the privPatDAO to set
     */
    public void setPrivPatDAO(PrivatePatientDAO privPatDAO);

    /**
     * @return the privSeriesDAO
     */
    public PrivateSeriesDAO getPrivSeriesDAO();

    /**
     * @param privSeriesDAO
     *            the privSeriesDAO to set
     */
    public void setPrivSeriesDAO(PrivateSeriesDAO privSeriesDAO);

    /**
     * @return the privStudyDAO
     */
    public PrivateStudyDAO getPrivStudyDAO();

    /**
     * @param privStudyDAO
     *            the privStudyDAO to set
     */
    public void setPrivStudyDAO(PrivateStudyDAO privStudyDAO);

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
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO
     *            the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

}