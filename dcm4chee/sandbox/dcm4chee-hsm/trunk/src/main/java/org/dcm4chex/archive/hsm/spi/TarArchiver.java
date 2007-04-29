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
 * Fuad Ibrahimov, Diagnoseklinik Muenchen.de GmbH,
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Fuad Ibrahimov <fuad@ibrahimov.de>
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
package org.dcm4chex.archive.hsm.spi;

import org.dcm4chex.archive.ejb.jdbc.FileInfo;

import java.io.File;
import java.util.Map;
import java.util.List;

/**
 * <code>TarArchiver</code> is used to pack files into a TAR archive and unpack them on demand.
 * @author Fuad Ibrahimov
 * @since Feb 19, 2007
 */
public interface TarArchiver {
    /**
     * Unpacks a TAR file specified by <code>tarFilePath</code> into <code>destinationDir</code>.
     * <code>tarFilePath</code> is expected to be a full file path in an OS dependent format.
     * @see #unpack(java.io.File, String) 
     * @param tarFilePath OS dependent full file path of an archive to unpack
     * @param destinationDir destination directory to unpack files to
     * @throws Exception in case of errors
     */
    void unpack(String tarFilePath, String destinationDir) throws Exception;

    /**
     * Unpacks <code>tarFile</code> into <code>destinationDir</code>.
     * @see #unpack(String, String)
     * @param tarFile TAR archive to unpack
     * @param destinationDir destination directory to unpack files to
     * @throws Exception in case of errors
     */
    void unpack(File tarFile, String destinationDir) throws Exception;

    /**
     * Packs given files into a TAR archive and returns the TAR archive file. Uses <code>baseDir</code> to create
     * the TAR file in.
     * @param baseDir a directory to create the TAR file in
     * @param files list of files to be packed into an archive
     * @return created TAR archive file
     * @throws Exception in case of errors
     */
    File pack(String baseDir, List<FileInfo> files) throws Exception;
}
