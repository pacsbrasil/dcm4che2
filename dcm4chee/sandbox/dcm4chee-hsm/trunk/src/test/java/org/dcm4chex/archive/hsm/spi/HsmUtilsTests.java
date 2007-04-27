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
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.dcm4che.util.MD5Utils;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Dec 1, 2006
 */
public class HsmUtilsTests {
    private static final String FILE2 = "org/dcm4chex/archive/hsm/spi/test2.txt"; // NON-NLS
    private static final String FILE1 = "org/dcm4chex/archive/hsm/spi/test1.txt"; // NON-NLS
    private Map<String, String> md5Sums;

    private static final String HSM_FS = "hsm:/home/hsm"; // NON-NLS
    private FileInfo[][] localFileInfos;
    private FileInfo[][] archivedFileInfos;
    private FileInfo[][] archivedAndLocalFiles;
    private FileInfo archivedFileInfo2;
    private FileInfo archivedFileInfo1;
    private FileInfo archivedFileInfo4;
    private FileInfo archivedFileInfo5;
    private FileInfo archivedFileInfo6;
    private FileInfo archivedFileInfo3;
    private static final String MD5_1 = "123456789012345678901234567890ad"; // NON-NLS
    private static final String MD5_2 = "123456789012345678901234567890ce"; // NON-NLS

    /*
    @Test
    public void canHandleBigAmountOfFiles() throws Exception {
        String tempDir = null;
        File tarFile = null;
        File parent = null;
        Map<String, String> locations = new HashMap<String, String>(2);
        try {
            tempDir = ResourceUtils.getFile("classpath:").getCanonicalPath();
            parent = new File(tempDir, "too-many-files");
            parent.mkdir();
            for(int i = 0; i < 5000; i++ ) {
                File file = new File(parent, "file" + i + ".txt");
                file.createNewFile();
                locations.put(parent.getName() + i, file.getCanonicalPath());
            }
            tarFile = FileUtils.tar(tempDir, TEST_SERIES_IUID, TEST_TAR_WITH_SLASH, locations, md5Sums, TAR_BUFFER_SIZE);

            assertTrue(tarFile.exists(), "Didn't create the tar file.");
            assertTrue(tarFile.length() > 0, "Didn't create the tar file.");
        } finally {
            cleanup(tarFile, tempDir);
            for(String path : locations.values()) {
                FileUtils.delete(new File(path));
            }
            FileUtils.delete(parent);
        }
    }*/

    @Test
    public void doesntDeleteDirsIfGivenDirectoryIsNotParentOfTheFile() throws Exception {
        File temp = HsmUtils.classpathResource(".");
        File dir1 = new File(temp, "dir1"); // NON-NLS
        dir1.mkdir();
        File dir2 = new File(temp, "dir2"); // NON-NLS
        dir2.mkdir();

        File file = new File(dir1, "test.txt"); // NON-NLS
        file.createNewFile();

        try {
            HsmUtils.deleteParentsTill(file, dir2.getCanonicalPath());
            fail("Didn't throw expected IOException"); // NON-NLS
        } catch (IOException e) {
            assertEquals(e.getMessage(), dir2 +" is not a parent directory of " + file ); // NON-NLS
        } finally {
            file.delete();
            dir1.delete();
            dir2.delete();
        }
    }

}
