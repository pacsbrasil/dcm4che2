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

package org.dcm4che.archive.dao.jpa;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.Query;

import org.dcm4che.archive.dao.ContentCreateException;
import org.dcm4che.archive.dao.ContentDeleteException;
import org.dcm4che.archive.dao.PrivateFileDAO;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.PrivateFile;
import org.dcm4che.archive.entity.PrivateInstance;

/**
 * org.dcm4che.archive.dao.jpa.PrivateFileDAOImpl
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Stateless
@TransactionManagement(value = TransactionManagementType.CONTAINER)
public class PrivateFileDAOImpl extends BaseDAOImpl<PrivateFile> implements
        PrivateFileDAO {

    /**
     * @see org.dcm4che.archive.dao.jpa.BaseDAOImpl#getPersistentClass()
     */
    @Override
    public Class getPersistentClass() {
        return PrivateFile.class;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivateFileDAO#create(java.lang.String,
     *      java.lang.String, long, byte[], int,
     *      org.dcm4che.archive.entity.PrivateInstance,
     *      org.dcm4che.archive.entity.FileSystem)
     */
    public PrivateFile create(String path, String tsuid, long size, byte[] md5,
            int status, PrivateInstance instance, FileSystem filesystem)
            throws ContentCreateException {
        
        PrivateFile pf = new PrivateFile();
        pf.setFilePath(path);
        pf.setFileTsuid(tsuid);
        pf.setFileSize(size);
        pf.setFileMd5(md5);
        pf.setFileStatus(status);
        pf.setInstance(instance);
        pf.setFileSystem(filesystem);
        save(pf);
        if (logger.isInfoEnabled())
            logger.info("Created: " + pf);

        return pf;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivateFileDAO#findDereferencedInFileSystem(java.lang.String,
     *      int)
     */
    public List<PrivateFile> findDereferencedInFileSystem(String dirPath,
            int limit) {

        if (logger.isDebugEnabled()) {
            logger.debug("Searching for dereferenced files with path="
                    + dirPath);
        }

        List<PrivateFile> files = null;

        Query query = em
                .createQuery("select pf from PrivateFile as pf where pf.instance is null and pf.fileSystem.directoryPath=:path");
        query.setParameter("path", dirPath);
        query.setMaxResults(limit);
        files = query.getResultList();

        return files;
    }

    /**
     * @see org.dcm4che.archive.dao.PrivateFileDAO#remove(java.lang.Long)
     */
    public void remove(Long pfPk) throws ContentDeleteException {
        // TODO
    }

}
