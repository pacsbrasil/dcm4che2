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
import java.util.Iterator;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.exceptions.PatientException;
import org.dcm4che.archive.service.PatientUpdate;
import org.dcm4che.archive.service.StudyReconciliation;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.net.DcmServiceException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision: 1.1 $ $Date: 2007/06/23 18:59:01 $
 * @since Jun 6, 2005
 */
@Transactional(propagation = Propagation.REQUIRED)
public class StudyReconciliationBean implements StudyReconciliation  {

    private static final Logger log = Logger
            .getLogger(StudyReconciliationBean.class);

    private StudyDAO studyDAO;

    private PatientUpdate patientUpdate;

    private Study getStudy(String suid) throws PersistenceException,
            DcmServiceException {
        try {
            return studyDAO.findByStudyIuid(suid);
        }
        catch (NoResultException e) {
            throw new DcmServiceException(Status.NoSuchSOPClass, suid);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#getStudyIuidsWithStatus(int, java.sql.Timestamp, int)
     */
    public Collection getStudyIuidsWithStatus(int status,
            Timestamp createdBefore, int limit) throws PersistenceException {
        Collection col = studyDAO.findStudiesWithStatus(status, createdBefore,
                limit);
        ArrayList studyIuids = new ArrayList();
        for (Iterator iter = col.iterator(); iter.hasNext();) {
            studyIuids.add(((Study) iter.next()).getStudyIuid());
        }
        return studyIuids;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#updateStatus(java.util.Collection, int)
     */
    public void updateStatus(Collection studyIuids, int status)
            throws PersistenceException, DcmServiceException {
        if (studyIuids == null)
            return;
        for (Iterator iter = studyIuids.iterator(); iter.hasNext();) {
            getStudy((String) iter.next()).setStudyStatus(status);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#updateStatus(java.lang.String, int)
     */
    public void updateStatus(String studyIuid, int status)
            throws PersistenceException, DcmServiceException {
        if (studyIuid == null)
            return;
        getStudy(studyIuid).setStudyStatus(status);
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#updatePatient(org.dcm4che.data.Dataset)
     */
    public void updatePatient(Dataset attrs) throws PatientException,
            ContentCreateException {
        patientUpdate.updatePatient(attrs);
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#mergePatient(org.dcm4che.data.Dataset, org.dcm4che.data.Dataset)
     */
    public void mergePatient(Dataset dominant, Dataset prior)
            throws PatientException, ContentCreateException {
        patientUpdate.mergePatient(dominant, prior);
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#updateStudyAndSeries(java.lang.String, int, java.util.Map)
     */
    public void updateStudyAndSeries(String studyIuid, int studyStatus, Map map)
            throws PersistenceException, DcmServiceException {
        if (studyIuid == null)
            return;
        Study study = getStudy(studyIuid);
        study.setStudyStatus(studyStatus);
        if (map != null && !map.isEmpty()) {
            Iterator iter = study.getSeries().iterator();
            Dataset ds, dsOrig;
            Series sl;
            do {
                sl = (Series) iter.next();
                ds = sl.getAttributes(false);
                dsOrig = (Dataset) map.get(sl.getSeriesIuid());
                ds.putAll(dsOrig);
                sl.setAttributes(ds);
            } while (iter.hasNext());
            ds = study.getAttributes(false);
            ds.putAll(dsOrig);
            study.setAttributes(ds);
            studyDAO.updateDerivedFields(study, false, false, false, false, false, true);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#getPatientUpdate()
     */
    public PatientUpdate getPatientUpdate() {
        return patientUpdate;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#setPatientUpdate(org.dcm4che.archive.service.PatientUpdate)
     */
    public void setPatientUpdate(PatientUpdate patientUpdate) {
        this.patientUpdate = patientUpdate;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.StudyReconciliation#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

}
