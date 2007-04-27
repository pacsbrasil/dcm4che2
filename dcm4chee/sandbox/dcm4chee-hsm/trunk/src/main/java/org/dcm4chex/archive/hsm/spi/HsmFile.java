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
import org.dcm4chex.archive.hsm.spi.utils.Assert;
import org.dcm4chex.archive.util.FileUtils;

import java.io.Serializable;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.text.MessageFormat;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Feb 15, 2007
 */
public class HsmFile implements Serializable {
    private final String tarPath;
    private final String fileSpaceName;
    private final List<FileInfo> entries = new ArrayList<FileInfo>();

    private static final String ILLEGAL_TAR_PATH = "Illegal TAR path: [{0}]. Must contain [!]"; // NON-NLS
    private static final String TAR_SEPARATOR = "!"; // NON-NLS

    public HsmFile(String tarPath, String baseDir) {
        Assert.hasText(tarPath, "tarPath"); // NON-NLS
        Assert.hasText(baseDir, "fileSpaceName"); // NON-NLS
        this.tarPath = tarPath;
        this.fileSpaceName = baseDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HsmFile that = (HsmFile) o;

        return tarPath.equals(that.tarPath) && fileSpaceName.equals(that.fileSpaceName);

    }

    @Override
    public int hashCode() {
        int result;
        result = tarPath.hashCode();
        result = 29 * result + fileSpaceName.hashCode();
        return result;
    }

    public static String extractTarPath(String path) {
        if(!path.contains(HsmFile.TAR_SEPARATOR)) throw new IllegalArgumentException(MessageFormat.format(HsmFile.ILLEGAL_TAR_PATH, path));
        return path.substring(0, path.indexOf(HsmFile.TAR_SEPARATOR));
    }

    public static String extractFilePath(String path) {
        if(!path.contains(HsmFile.TAR_SEPARATOR)) throw new IllegalArgumentException(MessageFormat.format(HsmFile.ILLEGAL_TAR_PATH, path));
        return path.substring(path.indexOf(HsmFile.TAR_SEPARATOR) + 1);
    }

    public String toString() {
        return new StringBuffer().append("[")
                .append(tarPath)
                .append(", ")
                .append(fileSpaceName)
                .append("]").toString();
    }

    public String getTarPath() {
        return tarPath;
    }

    public String getFileSpaceName() {
        return fileSpaceName;
    }

    public List<FileInfo> getEntries() {
        return entries;
    }

    public void addEntry(FileInfo finfo) {
        FileInfo newFinfo = new FileInfo(finfo.pk,
                finfo.patID,
                finfo.patName,
                finfo.patAttrs,
                finfo.studyIUID,
                finfo.seriesIUID,
                finfo.studyAttrs,
                finfo.seriesAttrs,
                finfo.instAttrs,
                finfo.sopIUID,
                finfo.sopCUID,
                finfo.extRetrieveAET,
                finfo.fileRetrieveAET,
                finfo.availability,
                finfo.basedir,
                HsmFile.extractFilePath(finfo.fileID),
                finfo.tsUID,
                finfo.md5,
                finfo.size,
                finfo.status);
        this.entries.add(newFinfo);
    }

}
