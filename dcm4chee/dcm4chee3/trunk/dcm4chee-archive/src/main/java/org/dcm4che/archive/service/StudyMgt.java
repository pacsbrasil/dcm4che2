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

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.exceptions.PatientException;
import org.dcm4che.archive.service.PatientUpdate;
import org.dcm4che.data.Dataset;
import org.dcm4che.net.DcmServiceException;

/**
 * org.dcm4che.archive.service.impl.StudyMgt
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface StudyMgt {

    /**
     * 
     */
    public void createStudy(Dataset ds) throws DcmServiceException,
            ContentCreateException, PersistenceException, Exception;

    /**
     * This method is invoked when post-storage message is processed. All
     * patient and study attributes will be replaced with the new data, which is
     * different from data coercion during the storage, where only empty
     * attibutes are updated.
     * @throws PatientException 
     * 
     * 
     */
    public void updateStudyAndPatientOnly(String iuid, Dataset ds)
            throws DcmServiceException, PersistenceException, PatientException;

    /**
     * 
     */
    public void updateStudy(String iuid, Dataset ds)
            throws DcmServiceException, PersistenceException;

    /**
     * 
     */
    public void deleteStudy(String iuid) throws DcmServiceException,
            PersistenceException;

    /**
     * 
     */
    public void deleteSeries(String[] iuids);

    /**
     * 
     */
    public void deleteInstances(String[] iuids) throws DcmServiceException;

    /**
     * 
     */
    public void updateStudyStatusId(String iuid, String statusId)
            throws PersistenceException, DcmServiceException;

    /**
     * @return the instDAO
     */
    public InstanceDAO getInstDAO();

    /**
     * @param instDAO the instDAO to set
     */
    public void setInstDAO(InstanceDAO instDAO);

    /**
     * @return the patDAO
     */
    public PatientDAO getPatDAO();

    /**
     * @param patDAO the patDAO to set
     */
    public void setPatDAO(PatientDAO patDAO);

    /**
     * @return the patientUpdate
     */
    public PatientUpdate getPatientUpdate();

    /**
     * @param patientUpdate the patientUpdate to set
     */
    public void setPatientUpdate(PatientUpdate patientUpdate);

    /**
     * @return the seriesDAO
     */
    public SeriesDAO getSeriesDAO();

    /**
     * @param seriesDAO the seriesDAO to set
     */
    public void setSeriesDAO(SeriesDAO seriesDAO);

    /**
     * @return the studyDAO
     */
    public StudyDAO getStudyDAO();

    /**
     * @param studyDAO the studyDAO to set
     */
    public void setStudyDAO(StudyDAO studyDAO);

}