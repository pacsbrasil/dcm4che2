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

import java.util.Collection;
import java.util.List;

import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.InstanceDAO;
import org.dcm4che.archive.dao.MPPSDAO;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.PrivateInstanceDAO;
import org.dcm4che.archive.dao.PrivatePatientDAO;
import org.dcm4che.archive.dao.PrivateSeriesDAO;
import org.dcm4che.archive.dao.PrivateStudyDAO;
import org.dcm4che.archive.dao.SeriesDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.service.impl.ContentManager
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
public interface ContentManager {

    /**
     * @throws PersistenceException
     * 
     */
    public Dataset getPatientByID(String pid, String issuer)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getStudy(long studyPk) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getStudyByIUID(String studyIUID) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getSeries(long seriesPk) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getSeriesByIUID(String seriesIUID)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getInstanceByIUID(String sopiuid)
            throws PersistenceException;

    /**
     * 
     */
    public int countStudies(Dataset filter, boolean hideWithoutStudies);

    /**
     * Get the Info of an instance.
     * <p>
     * Info means the Dataset with all attributes stored in DB for the instance
     * (instance, series, study and patient attributes)
     * 
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getInstanceInfo(String iuid, boolean supplement)
            throws PersistenceException;

    /**
     * Get the Info of an instance.
     * <p>
     * Info means the Dataset with all attributes stored in DB for the instance
     * (instance, series, study and patient attributes)
     * 
     * 
     * @ejb.transaction type="Required"
     */
    public List listInstanceInfos(String[] iuids, boolean supplement)
            throws PersistenceException;

    /**
     * Get the Info of an instance.
     * <p>
     * Info means the Dataset with all attributes stored in DB for the instance
     * (instance, series, study and patient attributes)
     */
    public List listInstanceInfosByPatientAndSRCode(String pid, String issuer,
            Collection codes, Collection cuids) throws PersistenceException;

    /**
     * Get the Info of an instance.
     * <p>
     * Info means the Dataset with all attributes stored in DB for the instance
     * (instance, series, study and patient attributes)
     * 
     * 
     * @ejb.transaction type="Required"
     */
    public List listInstanceInfosByStudyAndSRCode(String suid, String cuid,
            String code, String designator, boolean supplement)
            throws PersistenceException;

    /**
     * 
     */
    public List listStudies(Dataset filter, boolean hideWithoutStudies,
            boolean noMatchForNoValue, int offset, int limit);

    /**
     * 
     */
    public int countPrivateStudies(Dataset filter, int privateType,
            boolean hideWithoutStudies);

    /**
     * 
     */
    public List listPrivateStudies(Dataset filter, int privateType,
            boolean hideWithoutStudies, int offset, int limit);

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listStudiesOfPatient(long patientPk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listSeriesOfStudy(long studyPk) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listInstancesOfSeries(long seriesPk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listFilesOfInstance(long instancePk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listStudiesOfPrivatePatient(long patientPk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listSeriesOfPrivateStudy(long studyPk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listInstancesOfPrivateSeries(long seriesPk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List listFilesOfPrivateInstance(long instancePk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List[] listInstanceFilesToRecover(long pk)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List[] listSeriesFilesToRecover(long pk) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List[] listStudyFilesToRecover(long pk) throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public List[] listPatientFilesToRecover(long pk)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getSOPInstanceRefMacro(long studyPk, boolean insertModality)
            throws PersistenceException;

    /**
     * Get a collection of SOP Instance Reference Macro Datasets.
     * <p>
     * The parameter <code>instanceUIDs</code> can either use SOP Instance
     * UIDs (String) or Instance.pk values (Long).
     * 
     * @throws PersistenceException
     * 
     * @ejb.transaction type="Required"
     */
    public Collection getSOPInstanceRefMacros(Collection instanceUIDs)
            throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getPatientForStudy(long studyPk) throws PersistenceException;

    /**
     * @throws PersistenceException
     * 
     * 
     * @ejb.transaction type="Required"
     */
    public Dataset getPatientForStudy(String studyIUID)
            throws PersistenceException;

    /**
     * 
     * @ejb.transaction type="Required"
     */
    public boolean isStudyAvailable(long studyPk, int availability)
            throws PersistenceException;

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
     * @return the mppsDAO
     */
    public MPPSDAO getMppsDAO();

    /**
     * @param mppsDAO
     *            the mppsDAO to set
     */
    public void setMppsDAO(MPPSDAO mppsDAO);

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
     * @return the privInstanceDAO
     */
    public PrivateInstanceDAO getPrivInstanceDAO();

    /**
     * @param privInstanceDAO
     *            the privInstanceDAO to set
     */
    public void setPrivInstanceDAO(PrivateInstanceDAO privInstanceDAO);

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