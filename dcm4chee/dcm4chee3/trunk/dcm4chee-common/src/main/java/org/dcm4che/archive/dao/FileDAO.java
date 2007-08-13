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

import org.dcm4che.archive.common.FileStatus;
import org.dcm4che.archive.entity.File;
import org.dcm4che.archive.entity.FileSystem;
import org.dcm4che.archive.entity.Instance;

/**
 * org.dcm4che.archive.dao.FileDAO
 * 
 * @author <a href="mailto:damien.daddy@gmail.com">Damien Evans</a>
 */
@Local
public interface FileDAO extends DAO<File> {
    public static final String JNDI_NAME = "dcm4cheeArchive/FileDAOImpl/local";

    /**
     * Create a File record in the database with the specified initial
     * parameters.
     * 
     * @param path
     *            The path to the file.
     * @param tsuid
     *            TheTransfer Syntax of the file.
     * @param size
     *            The size of the file.
     * @param md5
     *            The MD5 calculated for the file.
     * @param status
     *            The status of the file. Must be a status contained in
     *            {@link FileStatus}.
     * @param instance
     *            The {@link Instance} the file belongs to.
     * @param fs
     *            The {@link FileSystem} the file is stored on.
     */
    public File create(String path, String tsuid, long size, byte[] md5,
            int status, Instance instance, FileSystem fs)
            throws ContentCreateException;

    /**
     * Find files on the specified file system to compress. Only files with an
     * uncompressed transfer syntax (Implicit VR Little Endian, Explicit VR
     * Little Endian, Big Endian) will be queried for.
     * 
     * @param dirPath
     *            The directory path.
     * @param cuid
     *            The SOP Class UID of the files to be found.
     * @param before
     *            Timestamp containing the time before which files should be
     *            found, based on the file's created date.
     * @param limit
     *            The maximum number of query results to return.
     * @return List of File objects.
     */
    public List<File> findFilesToCompress(String dirPath, String cuid,
            Timestamp before, int limit);

    /**
     * @param dirPath
     *            The directory path.
     * @param before
     *            Timestamp containing the time before which files should be
     *            found, based on the file's created date.
     * @param limit
     *            The maximum number of query results to return.
     * @return List of File objects.
     */
    public List<File> findToCheckMd5(String dirPath, Timestamp before, int limit);

    /**
     * Find the files on a specific file system by status.
     * 
     * @param dirPath
     *            The directory path.
     * @param status
     *            The status of the file. Must be a status contained in
     *            {@link FileStatus}.
     * @param before
     *            Timestamp containing the time before which files should be
     *            found, based on the file's created date.
     * @param limit
     *            The maximum number of query results to return.
     * @return List of File objects.
     */
    public List<File> findByStatusAndFileSystem(String dirPath, Integer status,
            Timestamp before, int limit);

    /**
     * Find all of the files on a file system.
     * 
     * @param dirPath
     *            The directory path.
     * @param offset
     *            The offset into the result set for paging the results.
     * @param limit
     *            The maximum number of query results to return.
     * @return List of File objects.
     */
    public List<File> findByFileSystem(String dirPath, int offset, int limit);

}
