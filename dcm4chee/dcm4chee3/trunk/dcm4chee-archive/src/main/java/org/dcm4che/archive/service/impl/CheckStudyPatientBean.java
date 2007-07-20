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

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.dcm4che.archive.common.Availability;
import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.PatientDAO;
import org.dcm4che.archive.dao.StudyDAO;
import org.dcm4che.archive.entity.FileDTO;
import org.dcm4che.archive.entity.Instance;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Series;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.exceptions.CircularMergedException;
import org.dcm4che.archive.exceptions.NonUniquePatientException;
import org.dcm4che.archive.exceptions.PatientMergedException;
import org.dcm4che.archive.service.CheckStudyPatient;
import org.dcm4che.archive.service.ContentEdit;
import org.dcm4che.data.Dataset;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author franz.willer@gwi-ag.com
 */
@Transactional(propagation = Propagation.REQUIRED)
public class CheckStudyPatientBean implements CheckStudyPatient {

    private StudyDAO studyDAO;

    private PatientDAO patDAO;

    private ContentEdit contentEdit;

    private static final Logger log = Logger
            .getLogger(CheckStudyPatientBean.class);

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#findStudiesForTest(java.lang.Integer, java.lang.String, int)
     */
    public Collection findStudiesForTest(Integer status, String sourceAET,
            int limit) throws PersistenceException {
        if (log.isDebugEnabled())
            log.debug("findStudyWithPatientCoercion: status:" + status
                    + " sourceAET:" + sourceAET + " limit:" + limit);
        Collection col;
        if (status != null) {
            if (sourceAET != null) {
                col = studyDAO.findStudiesWithStatusFromAE(status.intValue(),
                        sourceAET, limit);
            }
            else {
                col = studyDAO.findStudiesWithStatus(status.intValue(),
                        new Timestamp(System.currentTimeMillis()), limit);
            }
        }
        else {
            col = studyDAO.findStudiesFromAE(sourceAET, limit);
        }
        Study study;
        Series series;
        Instance instance;
        Dataset ds;
        FileDTO dto;
        Collection result = new ArrayList();
        for (Iterator iter = col.iterator(); iter.hasNext();) {
            study = (Study) iter.next();
            if (study.getAvailability() != Availability.ONLINE)
                continue;
            ds = study.getAttributes(true);
            ds.putAll(study.getPatient().getAttributes(false));
            series = study.getSeries().iterator().next();
            instance = series.getInstances().iterator().next();
            dto = (instance.getFiles().iterator().next()).getFileDTO();
            result.add(new Object[] { ds, dto });
        }
        return result;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#moveStudyToNewPatient(org.dcm4che.data.Dataset, long)
     */
    public Dataset moveStudyToNewPatient(Dataset patDS, long studyPk)
            throws ContentCreateException, PersistenceException,
            PatientMergedException, NonUniquePatientException,
            CircularMergedException {
        Patient pat = getOrCreatePatient(patDS);
        contentEdit.moveStudies(new long[] { studyPk }, pat.getPk()
                .longValue());
        return patDS;
    }

    private Patient getOrCreatePatient(Dataset ds)
            throws ContentCreateException, PersistenceException,
            PatientMergedException, NonUniquePatientException,
            CircularMergedException {
        try {
            return patDAO.searchFor(ds, false);
        }
        catch (NoResultException e) {
            return patDAO.create(ds);
        }
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#updateStudyStatus(long, java.lang.Integer)
     */
    public void updateStudyStatus(long studyPk, Integer studyStatus)
            throws PersistenceException {
        if (studyStatus == null)
            return;
        Study study = studyDAO.findByPrimaryKey(new Long(studyPk));
        study.setStudyStatus(studyStatus.intValue());
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#getContentEditHome()
     */
    public ContentEdit getContentEditHome() {
        return contentEdit;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#setContentEdit(org.dcm4che.archive.service.ContentEdit)
     */
    public void setContentEdit(ContentEdit contentEdit) {
        this.contentEdit = contentEdit;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#getPatDAO()
     */
    public PatientDAO getPatDAO() {
        return patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#setPatDAO(org.dcm4che.archive.dao.PatientDAO)
     */
    public void setPatDAO(PatientDAO patDAO) {
        this.patDAO = patDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#getStudyDAO()
     */
    public StudyDAO getStudyDAO() {
        return studyDAO;
    }

    /** 
     * @see org.dcm4che.archive.service.CheckStudyPatient#setStudyDAO(org.dcm4che.archive.dao.StudyDAO)
     */
    public void setStudyDAO(StudyDAO studyDAO) {
        this.studyDAO = studyDAO;
    }

}