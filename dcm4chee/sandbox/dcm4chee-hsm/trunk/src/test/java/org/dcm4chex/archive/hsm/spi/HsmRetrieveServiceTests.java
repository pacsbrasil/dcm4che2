package org.dcm4chex.archive.hsm.spi;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.dcm.movescu.MoveOrder;
import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import javax.management.*;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since May 3, 2007
 */
public class HsmRetrieveServiceTests {

    private static final String FILE2 = "org/dcm4chex/archive/hsm/spi/test2.txt"; // NON-NLS
    private static final String FILE1 = "org/dcm4chex/archive/hsm/spi/test1.txt"; // NON-NLS

    private HsmRetrieveService hsmRetrieveService = new HsmRetrieveService();
    private Storage mockStorage;

    private FileInfo fileInfo1;
    private FileInfo fileInfo2;
    private FileInfo[][] fileInfos;
    private static final String TAR_FILE = "TAR_FILE.tar"; // NON-NLS
    private MBeanServer mockMbeanServer;
    private String currentFileSystem;
    private String hsmFileSystem;
    private static final String SERIES_IUID = "1.33.44.55";   // NON-NLS
    private static final String DEST_AET = "TEST_AET";
    private TimeProvider mockTimeProvider;
    private File classPathDir;

    @BeforeMethod
    public void setUp() throws Exception {
        mockStorage = createMock(Storage.class);
        mockMbeanServer = createMock(MBeanServer.class);
        hsmRetrieveService.preRegister(mockMbeanServer,
                ObjectName.getInstance("dcm4chee.archive:service=HsmCopyService")); // NON-NLS
        hsmRetrieveService.setTarServiceName(ObjectName.getInstance("dcm4chee.archive:service=TarService")); // NON-NLS
        hsmRetrieveService.setHsmClientName(ObjectName.getInstance("dcm4chee.archive:service=HsmClient")); // NON-NLS
        hsmRetrieveService.setFileSystemMgtName(ObjectName.getInstance("dcm4chee.archive:service=FileSystemMgt")); // NON-NLS
        hsmRetrieveService.setJmsServiceName(ObjectName.getInstance("dcm4chee.archive:service=JMS")); // NON-NLS
        hsmRetrieveService.setStorage(mockStorage);

        classPathDir = HsmUtils.classpathResource(".");
        hsmFileSystem = new File(classPathDir, "hsm").getCanonicalPath();  // NON-NLS
        currentFileSystem = new File(classPathDir, "current").getCanonicalPath(); // NON-NLS

        hsmRetrieveService.setTempDir(classPathDir.getCanonicalPath());

        fileInfo1 = TestUtils.newFileInfo(1,
                TAR_FILE + "!" + FILE1,
                hsmFileSystem,
                SERIES_IUID,
                10,
                "1.55.44.3",
                "123456789012345678901234567890ad",  // NON-NLS
                FileStatus.DEFAULT);
        fileInfo2 = TestUtils.newFileInfo(2,
                TAR_FILE + "!" + FILE2,
                hsmFileSystem,
                SERIES_IUID,
                10,
                "1.55.44.3",
                "123456789012345678901234567890ad", // NON-NLS
                FileStatus.DEFAULT);

        fileInfos = new FileInfo[1][2];
        fileInfos[0][0] = fileInfo1;
        fileInfos[0][1] = fileInfo2;

        mockTimeProvider = new TimeProvider() {
            public long now() {
                return 0;
            }
        };
        hsmRetrieveService.setTimeProvider(mockTimeProvider);
    }

    @Test
    public void recognizesTarFilesAndUnpacksThem() throws Exception {
        expectSelectStorageSystemCall();
        expectRetrieveFile(systemDependentFilePath(TAR_FILE));
        expectUnpackTar();
        expectMoveScuCall();
        expectUpdateDb(new ArrayList<FileInfo>(){{add(fileInfo1); add(fileInfo2);}});

        replay(mockMbeanServer);
        replay(mockStorage);

        ObjectMessage mockMessage = createMock(ObjectMessage.class);
        expect(mockMessage.getObject()).andReturn(new HsmRetrieveOrder(fileInfos, DEST_AET)); // NON-NLS
        expect(mockMessage.getJMSPriority()).andReturn(0);
        replay(mockMessage);
        hsmRetrieveService.onMessage(mockMessage);

        verify(mockStorage);
        verify(mockMbeanServer);
    }

    @Test
    public void canRetrieveNotPackedFiles() throws Exception {
        fileInfos[0][0].fileID = FILE1;
        fileInfos[0][1].fileID = FILE2;
        File fileOnHsm1 = HsmUtils.toFile(fileInfos[0][0].basedir, fileInfos[0][0].fileID);
        File fileOnHsm2 = HsmUtils.toFile(fileInfos[0][1].basedir, fileInfos[0][1].fileID);
        File retrievedFile1 = null;
        File retrievedFile2 = null;
        try {
            HsmUtils.copy(HsmUtils.toFile(classPathDir.getCanonicalPath(), fileInfos[0][0].fileID), fileOnHsm1, 8192);
            HsmUtils.copy(HsmUtils.toFile(classPathDir.getCanonicalPath(), fileInfos[0][1].fileID), fileOnHsm2, 8192);

            expectSelectStorageSystemCall();
            expectRetrieveFile(systemDependentFilePath(FILE1));
            expectUpdateDb(new ArrayList<FileInfo>(){{add(fileInfo1);}});
            expectRetrieveFile(systemDependentFilePath(FILE2));
            expectUpdateDb(new ArrayList<FileInfo>(){{add(fileInfo2);}});
            expectMoveScuCall();

            replay(mockMbeanServer);
            replay(mockStorage);

            ObjectMessage mockMessage = createMock(ObjectMessage.class);
            expect(mockMessage.getObject()).andReturn(new HsmRetrieveOrder(fileInfos, DEST_AET)); // NON-NLS
            expect(mockMessage.getJMSPriority()).andReturn(0);
            replay(mockMessage);
            hsmRetrieveService.onMessage(mockMessage);

            verify(mockStorage);
            verify(mockMbeanServer);

            retrievedFile1 = HsmUtils.toFile(currentFileSystem, fileInfos[0][0].fileID);
            retrievedFile2 = HsmUtils.toFile(currentFileSystem, fileInfos[0][1].fileID);

            assertTrue(retrievedFile1.exists(), "Didn't copy the first file to the current storage dir");
            assertTrue(retrievedFile2.exists(), "Didn't copy the second file to the current storage dir");


        } finally {
            if(fileOnHsm1.exists()) {
                fileOnHsm1.delete();
                HsmUtils.deleteParentsTill(fileOnHsm1, hsmFileSystem);
            }
            if(fileOnHsm2.exists()) {
                fileOnHsm2.delete();
                HsmUtils.deleteParentsTill(fileOnHsm2, hsmFileSystem);
            }
            if(retrievedFile1 != null && retrievedFile1.exists()) {
                retrievedFile1.delete();
                HsmUtils.deleteParentsTill(retrievedFile1, currentFileSystem);
            }
            if(retrievedFile2 != null && retrievedFile2.exists()) {
                retrievedFile2.delete();
                HsmUtils.deleteParentsTill(retrievedFile2, currentFileSystem);
            }
        }
    }

    private void expectUpdateDb(ArrayList<FileInfo> files) throws javax.ejb.FinderException, javax.ejb.CreateException, java.rmi.RemoteException {
        mockStorage.storeFiles(eq(files), eq(currentFileSystem), eq(
                FileStatus.DEFAULT));
        expectLastCall().times(1);
    }

    private void expectMoveScuCall() throws InstanceNotFoundException, MBeanException, ReflectionException {
        MoveOrder moveOrder = new MoveOrder(null, DEST_AET, 0, null, null, new String[]{SERIES_IUID});
        expect(mockMbeanServer.invoke(eq(hsmRetrieveService.getJmsServiceName()),
                eq("queue"), // NON-NLS
                aryEq(new Object[]{hsmRetrieveService.getMoveScuQueueName(), moveOrder, 0, mockTimeProvider.now()}),
                aryEq(new String[]{String.class.getName(), Serializable.class.getName(), int.class.getName(), long.class.getName()}))).andReturn(null);
    }

    private void expectUnpackTar() throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        expect(mockMbeanServer.invoke(eq(hsmRetrieveService.getTarServiceName()),
                eq(HsmRetrieveService.UNPACK),
                aryEq(new Object[]{FileUtils.toFile(hsmRetrieveService.getTempDir(), TAR_FILE), currentFileSystem}),
                aryEq(new String[]{File.class.getName(), String.class.getName()}))).andReturn(null);
    }

    private void expectRetrieveFile(String file) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        expect(mockMbeanServer.invoke(eq(hsmRetrieveService.getHsmClientName()),
                eq(HsmRetrieveService.RETRIEVE),
                aryEq(new Object[]{hsmFileSystem, file, FileUtils.toFile(hsmRetrieveService.getTempDir())}),
                aryEq(new String[]{String.class.getName(), String.class.getName(), File.class.getName()}))).andReturn(null);
    }

    private void expectSelectStorageSystemCall() throws InstanceNotFoundException, MBeanException, ReflectionException {
        FileSystemDTO fileSystemDTO = new FileSystemDTO();
        fileSystemDTO.setDirectoryPath(currentFileSystem);
        expect(mockMbeanServer.invoke(eq(hsmRetrieveService.getFileSystemMgtName()),
                eq(HsmRetrieveService.SELECT_STORAGE_FILE_SYSTEM),
                aryEq(new Object[]{}),
                aryEq(new String[]{}))).andReturn(fileSystemDTO);
    }

    private String systemDependentFilePath(String filePath) {
        return hsmFileSystem.replaceAll("/", File.separator) + (hsmFileSystem.endsWith("/") ? "" : File.separator) + filePath.replaceAll("/", File.separator);
    }

    @AfterMethod
    public void tearDown() {
        File hsmFS = FileUtils.toFile(hsmFileSystem);
        if(hsmFS.exists()) hsmFS.delete();
        File currentFS = FileUtils.toFile(currentFileSystem);
        if(currentFS.exists()) currentFS.delete();
    }
}
