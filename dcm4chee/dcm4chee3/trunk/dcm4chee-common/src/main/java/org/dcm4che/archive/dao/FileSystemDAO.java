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
package org.dcm4che.archive.dao;

import java.sql.Timestamp;
import java.util.List;

import javax.ejb.Local;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.FileSystemDTO;

/**
 * org.dcm4che.archive.dao.FileSystemDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface FileSystemDAO extends DAO<FileSystem> {
    public FileSystem findByDirectoryPath(String dirPath)
            throws NoResultException, PersistenceException;

    /**
     * @param dto
     * @return
     * @throws ContentCreateException
     */
    public FileSystem create(FileSystemDTO dto)
            throws ContentCreateException;

    /**
     * @param retrieveAET
     * @param availability
     * @param def_rw
     * @return
     */
    public List<FileSystem> findByRetrieveAETAndAvailabilityAndStatus(
            String retrieveAET, int availability, int def_rw)
            throws PersistenceException;

    /**
     * @param aet
     * @param availability
     * @param def_rw
     * @param rw
     * @return
     */
    public List<FileSystem> findByRetrieveAETAndAvailabilityAndStatus2(
            String aet, int availability, int def_rw, int rw)
            throws PersistenceException;

    /**
     * Find the number of files on a given file system.
     * 
     * @param fsPk
     *            The primary key of the file system.
     * @return An int representing the number of files.
     */
    public int countFiles(Long fsPk) throws PersistenceException;

    /**
     * @param retrieveAET
     * @return
     */
    public List<FileSystem> findByRetrieveAET(String retrieveAET)
            throws PersistenceException;

    /**
     * @param fsPk
     * @param timestamp
     * @return
     */
    public long sizeOfFilesCreatedAfter(Long fsPk, Timestamp timestamp)
            throws PersistenceException;

    /**
     * @param dirpath
     * @param availability
     * @param status
     * @return
     */
    public List<FileSystem> findByLikeDirectoryPath(String dirpath,
            int availability, int status) throws PersistenceException;

    /**
     * @param pk
     * @return
     */
    public int countPrivateFiles(Long pk);

}
