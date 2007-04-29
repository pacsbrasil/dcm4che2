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

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.text.MessageFormat;

/**
 * A value object representing a single HSM entry. Can be a TAR archive containing many
 * files, as well as a single file.
 *
 * @see org.dcm4chex.archive.hsm.spi.HsmRetrieveOrder
 * @author Fuad Ibrahimov
 * @since Feb 15, 2007
 */
public class HsmFile implements Serializable {
    private final String filePath;
    private final String fileSpaceName;
    private final List<FileInfo> entries = new ArrayList<FileInfo>();

    private static final String ILLEGAL_TAR_PATH = "Illegal TAR path: [{0}]. Must contain [!]"; // NON-NLS
    private static final String TAR_SEPARATOR = "!"; // NON-NLS

    /**
     * Constructs an <code>HsmFile</code> instance using the specified file path and file space name. 
     * @param filePath full, OS dependent, file path
     * @param fileSpaceName destination HSM file space name. Must be in an OS dependent format. 
     */
    public HsmFile(String filePath, String fileSpaceName) {
        Assert.hasText(filePath, "filePath"); // NON-NLS
        Assert.hasText(fileSpaceName, "fileSpaceName"); // NON-NLS
        this.filePath = filePath;
        this.fileSpaceName = fileSpaceName;
    }

    /**
     * Instance of this class is equal to other only if <code>this.filePath.equals(o.filePath)</code> and
     * <code>this.fileSpaceName.equals(o.fileSpaceName)</code>.
     * @param o other object to check equality with
     * @return <code>true</code> or <code>false</code> based on the check described above
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HsmFile that = (HsmFile) o;

        return filePath.equals(that.filePath) && fileSpaceName.equals(that.fileSpaceName);

    }

    /**
     * Computes the hash code based on <code>this.filePath</code> and <code>this.fileSpaceName</code> hash codes.
     * @return the computed hash code
     */
    @Override
    public int hashCode() {
        int result;
        result = filePath.hashCode();
        result = 29 * result + fileSpaceName.hashCode();
        return result;
    }

    /**
     * Extracts the TAR archive file path from the specified file path, the part of the path before <code>!</code> in
     * <code>SOME_PATH_HERE.tar!FILE_PATH_IN_THE_ARCHIVE</code> 
     * @param path full file path in the TAR archive
     * @return extracted TAR file path
     */
    public static String extractTarPath(String path) {
        if(!path.contains(TAR_SEPARATOR)) throw new IllegalArgumentException(MessageFormat.format(ILLEGAL_TAR_PATH, path));
        return path.substring(0, path.indexOf(TAR_SEPARATOR));
    }

    /**
     * Extracts the file path from the specified full file path in the TAR archive, the part of the path after
     * <code>!</code> in <code>SOME_PATH_HERE.tar!FILE_PATH_IN_THE_ARCHIVE</code> 
     * @param path full file path in the TAR archive
     * @return extracted file path
     */
    public static String extractFilePath(String path) {
        if(!path.contains(TAR_SEPARATOR)) throw new IllegalArgumentException(MessageFormat.format(ILLEGAL_TAR_PATH, path));
        return path.substring(path.indexOf(TAR_SEPARATOR) + 1);
    }

    public String toString() {
        return new StringBuffer().append("[")
                .append(filePath)
                .append(", ")
                .append(fileSpaceName)
                .append("]").toString();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileSpaceName() {
        return fileSpaceName;
    }

    /**
     * Returns an unmodifiable copy of entries of this HSM object
     * @return entries of this HSM object
     */
    public List<FileInfo> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Adds a copy of the spesified <code>FileInfo</code> as an entry of this HSM object.
     * @param finfo <code>FileInfo</code> to add
     */
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
                extractFilePath(finfo.fileID),
                finfo.tsUID,
                finfo.md5,
                finfo.size,
                finfo.status);
        this.entries.add(newFinfo);
    }

}
