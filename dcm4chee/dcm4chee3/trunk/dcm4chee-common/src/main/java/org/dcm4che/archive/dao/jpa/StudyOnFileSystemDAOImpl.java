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
package org.dcm4che.archive.dao.jpa;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.StudyOnFileSystemDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.Study;
import org.dcm4che.archive.entity.StudyOnFileSystem;

/**
 * org.dcm4che.archive.dao.jpa.StudyOnFileSystemDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class StudyOnFileSystemDAOImpl extends BaseDAOImpl<StudyOnFileSystem>
        implements StudyOnFileSystemDAO {

    /**
     * 
     */
    public StudyOnFileSystemDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return StudyOnFileSystem.class;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyOnFileSystemDAO#create(org.dcm4che.archive.entity.Study,
     *      org.dcm4che.archive.entity.FileSystem)
     */
    public StudyOnFileSystem create(Study study, FileSystem fs)
            throws ContentCreateException {
        StudyOnFileSystem sfs = new StudyOnFileSystem();
        sfs.setStudy(study);
        sfs.setFileSystem(fs);
        sfs.touch();
        save(sfs);
        return sfs;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyOnFileSystemDAO#findFilesForStudyOnFileSystem(java.lang.Long,
     *      java.lang.Long)
     */
    public List<File> findFilesForStudyOnFileSystem(Long studyPk,
            Long fileSystemPk) {
        // TODO
        // query="SELECT OBJECT(f) FROM File f WHERE f.instance.series.study.pk
        // = ?1 AND f.fileSystem.pk = ?2"
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyOnFileSystemDAO#findByRetrieveAETAndAccessAfter(java.lang.String,
     *      java.sql.Timestamp, int)
     */
    public Collection<StudyOnFileSystem> findByRetrieveAETAndAccessAfter(
            String retrieveAET, Timestamp tsAfter, int thisBatchSize)
            throws PersistenceException {
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyOnFileSystemDAO#findByRetrieveAETAndAccessBefore(java.lang.String,
     *      java.sql.Timestamp)
     */
    public Collection<StudyOnFileSystem> findByRetrieveAETAndAccessBefore(
            String retrieveAET, Timestamp tsBefore) throws PersistenceException {
        return null;
    }

    /**
     * @see org.dcm4che.archive.dao.StudyOnFileSystemDAO#findByStudyAndFileSystem(java.lang.String,
     *      java.lang.String)
     */
    public StudyOnFileSystem findByStudyAndFileSystem(String siud,
            String dirPath) throws PersistenceException {
        return null;
    }
}
