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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.dcm4che.util.MD5Utils;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.apache.commons.compress.tar.TarInputStream;
import org.apache.commons.compress.tar.TarEntry;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.text.MessageFormat;

/**
 * @author Fuad Ibrahimov
 * @since Feb 19, 2007
 */
public class TarServiceTests {
    private static final String FILE2 = "org/dcm4chex/archive/hsm/spi/test2.txt"; // NON-NLS
    private static final String FILE1 = "org/dcm4chex/archive/hsm/spi/test1.txt"; // NON-NLS

    private File file1;
    private File file2;
    private TarService tarService;
    private String tempDir;
    private File destDir;
    private File tarFile;
    private FileInfo fileInfo1;
    private FileInfo fileInfo2;
    private List<FileInfo> files;

    @BeforeMethod
    public void setUp() throws Exception {
        file1 = HsmUtils.classpathResource(FILE1); // NON-NLS
        file2 = HsmUtils.classpathResource(FILE2); // NON-NLS

        tarService = new TarService();
        tempDir = HsmUtils.classpathResource(".").getCanonicalPath(); // NON-NLS
        destDir = new File(tempDir, "destinationDir"); // NON-NLS
        destDir.mkdirs();

        fileInfo1 = TestUtils.newFileInfo(1,
                FILE1,
                tempDir,
                "1.33.44.55",
                file1.length(),
                "1.55.44.1",
                new String(md5Digest(file1)),
                0);
        fileInfo2 = TestUtils.newFileInfo(2,
                FILE2,
                tempDir,
                "1.33.44.55",
                file2.length(),
                "1.55.44.2",
                new String(md5Digest(file2)),
                0);

        files = new ArrayList<FileInfo>(){{
            add(fileInfo1);
            add(fileInfo2);
        }};
        tarFile = tarService.pack(destDir.getCanonicalPath(), files);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        TestUtils.cleanup(tarFile, tempDir);
        TestUtils.cleanup(new File(destDir, FILE1), tempDir);
        TestUtils.cleanup(new File(destDir, TarService.MD5_SUM), tempDir);
        TestUtils.cleanup(new File(destDir, FILE2), tempDir);
    }

    @Test
    public void canPackGivenFiles() throws Exception {
        assertTrue(tarFile.exists(), "Didn't create expected tar file.");
        assertTrue(tarFile.length() > 0, "Didn't create expected tar file.");
        assertEquals(tarFile.getCanonicalPath(), new File(destDir, "org/dcm4chex/archive/hsm/spi-test1.txt.tar").getCanonicalPath()); // NON-NLS

        TarInputStream tis = null;
        try {
            tis = new TarInputStream(new FileInputStream(tarFile));
            TarEntry nextEntry = tis.getNextEntry();

            assertTrue(nextEntry != null, "Tar file didn't contain any entries.");
            //noinspection ConstantConditions
            assertEquals(nextEntry.getName(), TarService.MD5_SUM);
            assertEquals(tis.getNextEntry().getName(), fileInfo1.fileID.replaceAll(File.separator, "/"));
            assertEquals(tis.getNextEntry().getName(), fileInfo2.fileID.replaceAll(File.separator, "/"));
        } finally {
            if(tis != null) tis.close();
        }
    }

    @Test
    public void canUnpackTarFile() throws Exception {
        tarService.unpack(tarFile, destDir.getCanonicalPath());
        assertTrue(new File(destDir, FILE1).exists());
        assertTrue(new File(destDir, FILE2).exists());
    }

    @Test
    public void unpackReplacesExistingFiles() throws Exception {
        File newFile = new File(destDir, FILE1);
        newFile.getParentFile().mkdirs();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(file1);
            fos = new FileOutputStream(newFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
        } finally {
            if (fis != null) fis.close();
            if (fos != null) fos.close();
        }
        String file1Md5 = new String(md5Digest(file1));
        String newFileMd5 = new String(md5Digest(newFile));
        assertEquals(newFileMd5, file1Md5);

        tarService.unpack(tarFile, destDir.getCanonicalPath());

        File exFile1 = new File(destDir, FILE1);
        File exFile2 = new File(destDir, FILE2);

        assertTrue(exFile1.exists());
        assertTrue(exFile2.exists());
        assertEquals(new String(md5Digest(exFile1)), file1Md5);
    }

    @Test(expectedExceptions = {FailedDigestCheckException.class})
    public void unpackChecksMd5Sums() throws Exception {
        fileInfo2.md5 = "123456789012345678901234567890ce"; // NON-NLS
        tarService.pack(destDir.getCanonicalPath(), files);
        try {
            tarService.unpack(tarFile, destDir.getCanonicalPath());
        } catch (FailedDigestCheckException e) {
            assertFalse(new File(destDir, FILE1).exists());
            File exFile2 = new File(destDir, FILE2);
            assertFalse(exFile2.exists());
            assertFalse(exFile2.getParentFile().exists());
            assertEquals(e.getMessage(),
                    MessageFormat.format(TarService.FAILED_DIGEST_CHECK,
                            "MD5", // NON-NLS
                            FILE2,
                            fileInfo2.md5,
                            new String(md5Digest(file2)))); // NON-NLS
            throw e;
        }
    }

    @Test(expectedExceptions = {FailedDigestCheckException.class})
    public void unpackThrowsExceptionIfUnexpectedTarEntry() throws Exception {
        fileInfo2.md5 = ""; // NON-NLS
        tarService.pack(destDir.getCanonicalPath(), files);
        try {
            tarService.unpack(tarFile, destDir.getCanonicalPath());
        } catch (FailedDigestCheckException e) {
            assertFalse(new File(destDir, FILE1).exists());
            assertFalse(new File(destDir, FILE2).exists());
            assertEquals(e.getMessage(),
                    MessageFormat.format(TarService.UNEXPECTED_TAR_ENTRY, FILE2));
            throw e;
        }
    }

    @Test
    public void canUnpackTarFileWithoutMd5Check() throws Exception {
        tarService.setCheckMd5(false);
        tarService.unpack(tarFile, destDir.getCanonicalPath());
        assertTrue(new File(destDir, FILE1).exists());
        assertTrue(new File(destDir, FILE2).exists());
        assertTrue(new File(destDir, TarService.MD5_SUM).exists());
    }

    private char[] md5Digest(File file) throws NoSuchAlgorithmException, IOException {
        DigestInputStream dis = null;
        try {
            dis = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5")); // NON-NLS
            byte[] buf = new byte[32];
            while (dis.read(buf) > 0) {
            }
            return MD5Utils.toHexChars(dis.getMessageDigest().digest());
        } finally {
            if (dis != null) dis.close();
        }
    }
}
