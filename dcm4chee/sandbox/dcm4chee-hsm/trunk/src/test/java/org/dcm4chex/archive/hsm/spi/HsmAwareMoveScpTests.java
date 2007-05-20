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

import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DimseListener;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.interfaces.AEDTO;
import org.dcm4chex.archive.dcm.qrscp.MoveTask;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.easymock.EasyMock.*;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.Serializable;

/**
 * @author Fuad Ibrahimov
 * @since Feb 9, 2007
 */
public class HsmAwareMoveScpTests {
    private static final String TEST_MOVE_DEST = "TEST_MOVE_DEST"; // NON-NLS
    private static final String TEST_AE = "TEST_AE"; // NON-NLS
    private static final String LOCALHOST = "localhost"; // NON-NLS
    private static final int PORT = 4006;
    private static final String CIPHER_SUITES = "";
    private static final int PK = 1;

    private static final String FILE2 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!de/diagnoseklinik_muenchen/test2.txt"; // NON-NLS
    private static final String FILE1 = "/2007/2/15/12/3E376689/6AA7F267-D9E55A2B.tar!de/diagnoseklinik_muenchen/test1.txt"; // NON-NLS
    private static final int MESSAGE_ID = 23;
    private HsmAwareQueryRetrieveScpService service;
    private HsmAwareMoveScp moveScp;
    private ActiveAssociation activeAssosciation;
    private Association association;
    private Command command;
    private Dataset ds;
    private AEDTO aeData;
    private static final String HSM_HOME = "tar:/home/hsm"; // NON-NLS
    private FileInfo[][] localFileInfos;
    private FileInfo[][] archivedFileInfos;
    private FileInfo[][] archivedAndLocalFiles;
    private MBeanServer mockMbeanServer;
    private TimeProvider timeProvider;

    @BeforeMethod
    public void setUp() throws Exception {
        String classPathDir = HsmUtils.classpathResource(".").getCanonicalPath();
        FileInfo fileInfo1 = TestUtils.newFileInfo(1,
                FILE1,
                classPathDir,
                "1.33.44.55",
                0,
                "1.55.44.3",
                "123456789012345678901234567890ad", 0); // NON-NLS
        FileInfo fileInfo2 = TestUtils.newFileInfo(2,
                FILE2,
                classPathDir,
                "1.33.44.55",
                0,
                "1.55.44.3",
                "123456789012345678901234567890ad", 0); // NON-NLS

        localFileInfos = new FileInfo[2][2];

        localFileInfos[0][0] = fileInfo1;
        localFileInfos[0][1] = fileInfo2;
        localFileInfos[1][0] = fileInfo1;
        localFileInfos[1][1] = fileInfo2;

        archivedFileInfos = new FileInfo[2][2];
        FileInfo archivedFileInfo1 = TestUtils.newFileInfo(3,
                FILE1,
                HSM_HOME,
                "1.33.44.55",
                0,
                "1.55.1.3",
                "123456789012345678901234567890ad", 2); // NON-NLS
        FileInfo archivedFileInfo2 = TestUtils.newFileInfo(4,
                FILE2,
                HSM_HOME,
                "1.33.44.55",
                0,
                "1.55.2.3",
                "123456789012345678901234567890ad", 2); // NON-NLS
        FileInfo archivedFileInfo3 = TestUtils.newFileInfo(5,
                FILE1,
                HSM_HOME,
                "1.33.44.55",
                0,
                "1.55.33.3",
                "123456789012345678901234567890ad", 2); // NON-NLS
        FileInfo archivedFileInfo4 = TestUtils.newFileInfo(6,
                FILE2,
                HSM_HOME,
                "1.33.44.55",
                0,
                "1.55.55.3",
                "123456789012345678901234567890ad", 2); // NON-NLS
        archivedFileInfos[0][0] = archivedFileInfo1;
        archivedFileInfos[0][1] = archivedFileInfo2;
        archivedFileInfos[1][0] = archivedFileInfo3;
        archivedFileInfos[1][1] = archivedFileInfo4;

        archivedAndLocalFiles = new FileInfo[2][4];
        archivedAndLocalFiles[0][0] = archivedFileInfo1;
        archivedAndLocalFiles[0][1] = archivedFileInfo2;
        archivedAndLocalFiles[0][2] = fileInfo1;
        archivedAndLocalFiles[0][3] = fileInfo2;
        archivedAndLocalFiles[1][0] = archivedFileInfo1;
        archivedAndLocalFiles[1][1] = archivedFileInfo2;
        archivedAndLocalFiles[1][2] = fileInfo1;
        archivedAndLocalFiles[1][3] = fileInfo2;

        service = new HsmAwareQueryRetrieveScpService();
        service.setCalledAETs(TEST_AE);
        moveScp = new HsmAwareMoveScp(service);


        activeAssosciation = createMock(ActiveAssociation.class);
        association = createMock(Association.class);
        expect(activeAssosciation.getAssociation()).andReturn(association).times(2);
        expect(association.getCallingAET()).andReturn(TEST_MOVE_DEST);
        expect(association.getCalledAET()).andReturn(TEST_AE);
        activeAssosciation.addCancelListener(eq(MESSAGE_ID),
                isA(DimseListener.class));
        expectLastCall().times(1);

        replay(association);
        replay(activeAssosciation);

        command = createMock(Command.class);

        expect(command.getInt(1792, 0)).andReturn(0);
        expect(command.getMessageID()).andReturn(MESSAGE_ID);
        replay(command);

        ds = createMock(Dataset.class);
        aeData = new AEDTO(PK,
                TEST_AE,
                LOCALHOST,
                PORT,
                CIPHER_SUITES);


        mockMbeanServer = createMock(MBeanServer.class);
        service.preRegister(mockMbeanServer,
                ObjectName.getInstance("dcm4chee.archive:service=HsmCopyService")); // NON-NLS
        service.setJmsServiceName(ObjectName.getInstance("dcm4chee.archive:service=JMS")); // NON-NLS
        service.setJmsPriority(0);
        timeProvider = new TimeProvider() {
            public long now() {
                return 0;
            }
        };
        service.setTimeProvider(timeProvider);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        verify(command);
        verify(association);
        verify(activeAssosciation);
    }


    @Test
    public void createNewMoveTaskCreatesNormalMoveTaskIfFilesWereNotArchived() throws Exception {
        MoveTask moveTask = moveScp.createMoveTask(service,
                activeAssosciation,
                0,
                command,
                ds,
                localFileInfos,
                aeData,
                TEST_MOVE_DEST);
        assertTrue(moveTask.getClass().isAssignableFrom(MoveTask.class), "Wrong class. Must be MoveTask.");
    }

    @Test
    public void createNewMoveTaskCreatesDelayedAndEmptyMoveTaskIfFilesWereArchived() throws Exception {
        HsmRetrieveOrder retrieveOrder = new HsmRetrieveOrder(archivedFileInfos, TEST_MOVE_DEST);
        expect(mockMbeanServer.invoke(eq(service.getJmsServiceName()),
                eq(HsmAwareQueryRetrieveScpService.QUEUE),
                aryEq(new Object[]{HsmAwareQueryRetrieveScpService.HSM_RETRIEVE, retrieveOrder, service.getJmsPriority(), timeProvider.now()}),
                aryEq(new String[]{String.class.getName(), Serializable.class.getName(), int.class.getName(), long.class.getName()}))).andReturn(null);
        replay(mockMbeanServer);
        MoveTask moveTask = moveScp.createMoveTask(service,
                activeAssosciation,
                0,
                command,
                ds,
                archivedFileInfos,
                aeData,
                TEST_MOVE_DEST);
        assertTrue(moveTask instanceof HsmAwareMoveScp.EmptyMoveTask, "Wrong class. Must be EmptyMoveTask.");
        verify(mockMbeanServer);
    }

    @Test
    public void createNewMoveTaskCreatesDelayedAndNormalMoveTaskIfSomeFilesWereArchived() throws Exception {
        expect(mockMbeanServer.invoke(eq(service.getJmsServiceName()),
                eq(HsmAwareQueryRetrieveScpService.QUEUE),
                aryEq(new Object[]{HsmAwareQueryRetrieveScpService.HSM_RETRIEVE, new HsmRetrieveOrder(archivedFileInfos, TEST_MOVE_DEST), service.getJmsPriority(), timeProvider.now()}),
                aryEq(new String[]{String.class.getName(), Serializable.class.getName(), int.class.getName(), long.class.getName()}))).andReturn(null);
        replay(mockMbeanServer);
        MoveTask moveTask = moveScp.createMoveTask(service,
                activeAssosciation,
                0,
                command,
                ds,
                archivedAndLocalFiles,
                aeData,
                TEST_MOVE_DEST);
        assertTrue(moveTask.getClass().isAssignableFrom(MoveTask.class), "Wrong class. Must be MoveTask.");
        verify(mockMbeanServer);
    }
}
