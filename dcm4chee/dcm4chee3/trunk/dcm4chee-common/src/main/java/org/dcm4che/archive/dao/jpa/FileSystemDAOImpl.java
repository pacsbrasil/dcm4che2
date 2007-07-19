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
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.FileSystemDAO;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.FileSystemDTO;

/**
 * org.dcm4che.archive.dao.jpa.FileSystemDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class FileSystemDAOImpl extends BaseDAOImpl<FileSystem> implements
        FileSystemDAO {

    /**
     * 
     */
    public FileSystemDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#getFileSystem(java.lang.String)
     */
    public FileSystem findByDirectoryPath(String dirPath)
            throws NoResultException, PersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Querying for FileSystem with path=" + dirPath);
        }

        return (FileSystem) em
                .createQuery(
                        "select fs from FileSystem as fs where fs.directoryPath=:directoryPath")
                .setParameter("directoryPath", dirPath).getSingleResult();
    }

    /**
     * @see org.dcm4che.archive.dao.DAO#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return FileSystem.class;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#countFiles(java.lang.Long)
     */
    public int countFiles(Long fsPk) throws PersistenceException {
        Number n = (Number) em.createQuery(
                "select count(f) from File f where f.pk = :fsPk").setParameter(
                "pk", fsPk).getSingleResult();
        return n == null ? 0 : n.intValue();
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#create(java.lang.String,
     *      java.lang.String, int, int, java.lang.String)
     */
    public FileSystem create(FileSystemDTO dto) throws ContentCreateException {
        FileSystem fs = new FileSystem(dto);
        save(fs);
        logger.info("Created " + fs.toString());
        return fs;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#findByLikeDirectoryPath(java.lang.String,
     *      int, int)
     */
    public List<FileSystem> findByLikeDirectoryPath(String dirpath,
            int availability, int status) throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for FileSystem entries with path like "
                    + dirpath + " and availability=" + availability
                    + " and status=" + status);
        }

        List<FileSystem> fileSystems = null;

        Query query = em
                .createQuery("select fs from FileSystem as fs where fs.directoryPath like :path AND fs.availability = :availability AND fs.status = :status");
        query.setParameter("path", dirpath);
        query.setParameter("availability", availability);
        query.setParameter("status", status);
        fileSystems = query.getResultList();

        return fileSystems;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#findByRetrieveAET(java.lang.String)
     */
    public List<FileSystem> findByRetrieveAET(String retrieveAET)
            throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for FileSystem entries with retrieve AET="
                    + retrieveAET);
        }

        List<FileSystem> fileSystems = null;

        Query query = em
                .createQuery("select fs from FileSystem as fs where fs.retrieveAET =:retrieveAET");
        query.setParameter("retrieveAET", retrieveAET);
        fileSystems = query.getResultList();

        return fileSystems;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#findByRetrieveAETAndAvailabilityAndStatus(java.lang.String,
     *      int, int)
     */
    public List<FileSystem> findByRetrieveAETAndAvailabilityAndStatus(
            String retrieveAET, int availability, int def_rw)
            throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for FileSystem entries with retrieve AET="
                    + retrieveAET + " and availability=" + availability
                    + " and status=" + def_rw);
        }

        List<FileSystem> fileSystems = null;

        Query query = em
                .createQuery("select fs from FileSystem as fs where fs.retrieveAET =:retrieveAET AND fs.availability = :availability AND fs.status = :status");
        query.setParameter("retrieveAET", retrieveAET);
        query.setParameter("availability", availability);
        query.setParameter("status", def_rw);
        fileSystems = query.getResultList();

        return fileSystems;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#findByRetrieveAETAndAvailabilityAndStatus2(java.lang.String,
     *      int, int, int)
     */
    public List<FileSystem> findByRetrieveAETAndAvailabilityAndStatus2(
            String retrieveAET, int availability, int def_rw, int rw)
            throws PersistenceException {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for FileSystem entries with retrieve AET="
                    + retrieveAET + " and availability=" + availability
                    + " and status=" + def_rw + " and rw=" + rw);
        }

        List<FileSystem> fileSystems = null;

        Query query = em
                .createQuery("select fs from FileSystem as fs where fs.retrieveAET =:retrieveAET AND fs.availability = :availability  AND (fs.status = :status1 OR fs.status = :status2)");
        query.setParameter("retrieveAET", retrieveAET);
        query.setParameter("availability", availability);
        query.setParameter("status1", def_rw);
        query.setParameter("status1", rw);
        fileSystems = query.getResultList();

        return fileSystems;
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#sizeOfFilesCreatedAfter(java.lang.Long,
     *      java.sql.Timestamp)
     */
    public long sizeOfFilesCreatedAfter(Long fsPk, Timestamp timestamp)
            throws PersistenceException {
        Number n = (Number) em
                .createQuery(
                        "select sum(f.fileSize) from File f where f.fileSystem.pk=:fsPk and f.createdTime > :createdTime")
                .setParameter("pk", fsPk)
                .setParameter("createdTime", timestamp).getSingleResult();
        return n == null ? 0 : n.longValue();
    }

    /**
     * @see org.dcm4che.archive.dao.FileSystemDAO#countPrivateFiles(java.lang.Long)
     */
    public int countPrivateFiles(Long pk) {
        Number n = (Number) em
                .createQuery(
                        "select count(f) from PrivateFile f where f.fileSystem.pk=:fspk")
                .setParameter("fspk", pk).getSingleResult();
        return n == null ? 0 : n.intValue();
    }
}
