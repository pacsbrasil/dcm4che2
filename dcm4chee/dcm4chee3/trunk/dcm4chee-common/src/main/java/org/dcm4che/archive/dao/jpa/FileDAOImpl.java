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
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.FileDAO;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.Instance;

/**
 * org.dcm4che.archive.dao.jpa.FileDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class FileDAOImpl extends BaseDAOImpl<File> implements FileDAO {
    private static final String FILES_TO_COMPRESS = "from File f join f.fileSystem fs join f.instance i "
            + "where f.fileStatus=:status and "
            + "f.Tsuid in ('1.2.840.10008.1.2','1.2.840.10008.1.2.1','1.2.840.10008.1.2.2') and "
            + "fs.directoryPath=:dirPath and i.sopCuid=:cuid and and (f.createdTime is null or f.createdTime < :before)";

    private static final String BY_STATUS_AND_FS = "from File f join f.fileSystem fs "
            + "where fs.directoryPath=:dirPath and f.fileStatus=:status and (f.createdTime is null or f.createdTime < :before) "
            + "order by f.pk";

    private static final String FILES_TO_MD5CHECK = "from File f join f.fileSystem fs "
            + "where fs.directoryPath=:dirPath and f.fileMd5Field is not null and "
            + "(f.timeOfLastMd5Check is null or f.timeOfLastMd5Check < :before) ";

    /**
     * 
     */
    public FileDAOImpl() {
    }

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return File.class;
    }

    public File create(String path, String tsuid, long size, byte[] md5,
            int status, Instance instance, FileSystem fs)
            throws ContentCreateException {
        File file = new File();
        file.setFilePath(path);
        file.setFileTsuid(tsuid);
        file.setFileSize(size);
        file.setFileMd5(md5);
        file.setFileStatus(status);
        file.setInstance(instance);
        file.setFileSystem(fs);
        logger.info("Created " + file.toString());
        save(file);
        return file;
    }

    /**
     * @see org.dcm4che.archive.dao.FileDAO#findByFileSystem(java.lang.String,
     *      int, int)
     */
    public List<File> findByFileSystem(String dirPath, int offset, int limit) {
        Query q = em
                .createQuery("from File f join f.fileSystem fs where fs.directoryPath=:dirPath order by f.pk");
        q.setParameter("dirPath", dirPath);
        q.setFirstResult(offset);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.FileDAO#findByStatusAndFileSystem(java.lang.String,
     *      int, java.sql.Timestamp, int)
     */
    public List<File> findByStatusAndFileSystem(String dirPath, Integer status,
            Timestamp before, int limit) {

        Query q = em.createQuery(BY_STATUS_AND_FS);
        q.setParameter("dirPath", dirPath);
        q.setParameter("status", status);
        q.setParameter("before", before, TemporalType.TIMESTAMP);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.FileDAO#findFilesToCompress(java.lang.String,
     *      java.lang.String, java.sql.Timestamp, int)
     */
    public List<File> findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit) {
        Query q = em.createQuery(FILES_TO_COMPRESS);
        q.setParameter("dirPath", dirPath);
        q.setParameter("cuid", cuid);
        q.setParameter("before", before, TemporalType.TIMESTAMP);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    /**
     * @see org.dcm4che.archive.dao.FileDAO#findToCheckMd5(java.lang.String,
     *      java.sql.Timestamp, int)
     */
    public List<File> findToCheckMd5(String dirPath, Timestamp before, int limit) {
        Query q = em.createQuery(FILES_TO_MD5CHECK);
        q.setParameter("dirPath", dirPath);
        q.setParameter("before", before, TemporalType.TIMESTAMP);
        q.setMaxResults(limit);

        return q.getResultList();
    }
}
