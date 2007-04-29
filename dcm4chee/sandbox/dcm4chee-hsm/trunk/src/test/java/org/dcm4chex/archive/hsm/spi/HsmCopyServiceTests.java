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

import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.hsm.FileCopyOrder;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.dcm4che.data.Dataset;
import static org.easymock.EasyMock.*;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Fuad Ibrahimov
 * @since Jan 18, 2007
 */
public class HsmCopyServiceTests {
    private static final String FILE2 = "org/dcm4chex/archive/hsm/spi/test2.txt"; // NON-NLS
    private static final String FILE1 = "org/dcm4chex/archive/hsm/spi/test1.txt"; // NON-NLS

    private HsmCopyService hsmCopyService;
    private Storage mockStorage;

    private FileInfo fileInfo1;
    private FileInfo fileInfo2;
    private List<FileInfo> fileInfos;
    private static final String TAR_FILE = "TAR_FILE.tar"; // NON-NLS
    private String destination;
    private File tarFile;
    private String destinationFileSystem;
    private MBeanServer mockMbeanServer;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        hsmCopyService = new HsmCopyService();
        mockStorage = createMock(Storage.class);
        hsmCopyService.setStorage(mockStorage);
        mockMbeanServer = createMock(MBeanServer.class);
        hsmCopyService.preRegister(mockMbeanServer,
                ObjectName.getInstance("dcm4chee.archive:service=HsmCopyService")); // NON-NLS
        hsmCopyService.setTarServiceName(ObjectName.getInstance("dcm4chee.archive:service=TarService")); // NON-NLS
        hsmCopyService.setHsmClientName(ObjectName.getInstance("dcm4chee.archive:service=HsmClient")); // NON-NLS
        hsmCopyService.setFileStatus("ARCHIVED"); // NON-NLS


        File file1 = HsmUtils.classpathResource(FILE1);
        File file2 = HsmUtils.classpathResource(FILE2);

        String classPathDir = HsmUtils.classpathResource(".").getCanonicalPath();
        fileInfo1 = TestUtils.newFileInfo(1,
                FILE1,
                classPathDir,
                "1.33.44.55",
                file1.length(),
                "1.55.44.3",
                "123456789012345678901234567890ad",
                FileStatus.DEFAULT); // NON-NLS
        fileInfo2 = TestUtils.newFileInfo(2,
                FILE2,
                classPathDir,
                "1.33.44.55",
                file2.length(),
                "1.55.44.3",
                "123456789012345678901234567890ad",
                FileStatus.DEFAULT); // NON-NLS

        fileInfos = new ArrayList<FileInfo>();
        fileInfos.add(fileInfo1);
        fileInfos.add(fileInfo2);
        File tempDir = new File(HsmUtils.classpathResource("."), "temp"); // NON-NLS
        tempDir.mkdirs();
        tarFile = new File(tempDir, TAR_FILE);
        tarFile.createNewFile();
        destination = HsmUtils.classpathResource(".").getCanonicalPath();
        destinationFileSystem = "tar:" + destination; // NON-NLS 
    }

    @Test
    public void processPacksAndArchivesImages() throws Exception {
        expect(mockMbeanServer.invoke(eq(hsmCopyService.getTarServiceName()),
                eq(HsmCopyService.PACK),
                aryEq(new Object[]{destination, fileInfos}),
                aryEq(new String[]{String.class.getName(), List.class.getName()}))).andReturn(tarFile);
        expect(mockMbeanServer.invoke(eq(hsmCopyService.getHsmClientName()),
                eq(HsmCopyService.ARCHIVE),
                aryEq(new Object[]{destination, tarFile}),
                aryEq(new String[]{String.class.getName(), File.class.getName()}))).andReturn(null);
        String filePath1 = tarFile.getCanonicalPath().replace(destination, "") + "!" + fileInfo1.fileID;
        String filePath2 = tarFile.getCanonicalPath().replace(destination, "") + "!" + fileInfo2.fileID;

        mockStorage.storeFiles(eq(fileInfos), eq(destinationFileSystem), eq(FileStatus.ARCHIVED));


        replay(mockMbeanServer);
        replay(mockStorage);

        hsmCopyService.process(new TestFileCopyOrder(destinationFileSystem, fileInfos));

        verify(mockMbeanServer);
        verify(mockStorage);

        File parent = tarFile.getParentFile();
        while (!destination.equals(parent.getCanonicalPath())) {
            assertFalse(parent.exists(), "Parent dirs were not deleted after archive command.");
            parent = parent.getParentFile();
        }

        assertFalse(tarFile.exists(), "Tar file was not deleted after archive command");

        assertEquals(fileInfo1.fileID, filePath1);
        assertEquals(fileInfo2.fileID, filePath2);
    }

    @Test
    public void removesPrefixesFromFileSystemName() throws Exception {
        destinationFileSystem = "hsm:" + destination; // NON-NLS
        processPacksAndArchivesImages();
    }

    private class TestFileCopyOrder extends FileCopyOrder {

        private List fileInfos;

        public TestFileCopyOrder(String fileSystem, List fileInfos) {
            super(null, fileSystem, null);
            this.fileInfos = fileInfos;
        }

        public TestFileCopyOrder(Dataset ian, String dstFsPath, String retrieveAET, String fileSystem) {
            super(ian, fileSystem, retrieveAET);
        }

        public List getFileInfos() throws Exception {
            return fileInfos;
        }

    }
}
