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
 * Justin Falk <jfalkmu@gmail.com>
 * Jeremy Vosters <jlvosters@gmail.com>
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
package org.dcm4che.archive.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.Media;
import org.dcm4che.archive.entity.Patient;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.data.Dataset;

/**
 * org.dcm4che.archive.dao.StudyDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface StudyDAO extends DAO<Study> {

    /**
     * @param iuid
     * @return {@link Study}
     * @throws NoResultException
     *             If the series cannot be found.
     */
    public Study findByStudyIuid(String iuid) throws NoResultException,
            PersistenceException;

    public List<Study> findByPatientAndAccessionNumber(Long patientFk,
            String accessionNumber);

    /**
     * @param ds
     * @param patient
     * @return
     */
    public Study create(Dataset ds, Patient patient)
            throws ContentCreateException;

    /**
     * Update a study's derived fields (retrieve AE titles, number of instances,
     * etc.)
     * 
     * @param study
     * @param numOfInstances
     * @param retrieveAETs
     * @param externalRettrieveAETs
     * @param filesetId
     * @param availability
     * @param modsInStudies
     * @return True if the update was successful.
     */
    public boolean updateDerivedFields(Study study, boolean numOfInstances,
            boolean retrieveAETs, boolean externalRettrieveAETs,
            boolean filesetId, boolean availability, boolean modsInStudies)
            throws PersistenceException;

    /**
     * @param study
     * @param fsPk
     * @return
     */
    public List<File> getFiles(Study study, Long fsPk)
            throws PersistenceException;

    /**
     * Select the size of the study on storage as determined by the sum of the
     * sizes of all its files.
     * 
     * @param studyPk The {@link Study} primary key.
     * @param fsPk The {@link FileSystem} primary key.
     * @return A long containing the size of the study in bytes.
     */
    public long selectStudySize(Long studyPk, Long fsPk)
            throws PersistenceException;

    /**
     * @param study
     * @return
     */
    public int findNumberOfCommitedInstances(Study study)
            throws PersistenceException;

    /**
     * @param study
     * @return
     * @throws PersistenceException
     */
    public boolean isStudyAvailableOnMedia(Study study)
            throws PersistenceException;

    /**
     * @param pk
     * @param completed
     * @return
     */
    public int selectNumberOfStudyRelatedInstancesOnMediaWithStatus(Long pk,
            int completed) throws PersistenceException;

    /**
     * @param study
     * @return
     */
    public boolean isStudyExternalRetrievable(Study study)
            throws PersistenceException;

    /**
     * @param study
     * @param validFileStatus
     * @return
     */
    public boolean isStudyAvailableOnROFs(Study study, int validFileStatus)
            throws PersistenceException;

    /**
     * @param availability
     * @return
     */
    public boolean isStudyAvailable(Study study, int availability)
            throws PersistenceException;

    /**
     * @param i
     * @param sourceAET
     * @param limit
     * @return
     */
    public Collection<Study> findStudiesWithStatusFromAE(int i,
            String sourceAET, int limit) throws PersistenceException;

    /**
     * @param i
     * @param timestamp
     * @param limit
     * @return
     */
    public Collection<Study> findStudiesWithStatus(int i, Timestamp timestamp,
            int limit) throws PersistenceException;

    /**
     * @param sourceAET
     * @param limit
     * @return
     */
    public Collection<Study> findStudiesFromAE(String sourceAET, int limit)
            throws PersistenceException;

    /**
     * @param createdAfter
     * @param createdBefore
     * @param checkedBefore
     * @param limit
     * @return
     */
    public Collection<Study> findStudyToCheck(Timestamp createdAfter,
            Timestamp createdBefore, Timestamp checkedBefore, int limit)
            throws PersistenceException;

    /**
     * @param timestamp
     * @return
     */
    public Collection<Study> findStudiesNotOnMedia(Timestamp timestamp)
            throws PersistenceException;;

    public Collection<Study> findInstancesNotOnMedia(Study study)
            throws PersistenceException;

    /**
     * @param media
     * @return
     */
    public Collection<Study> findStudiesOnMedia(Media media)
            throws PersistenceException;

    /**
     * Get all of the files that belong to a study.
     * 
     * @param study
     * @return {@link Collection} of {@link File} objects.
     */
    public Collection<File> getAllFiles(Study study);

}
