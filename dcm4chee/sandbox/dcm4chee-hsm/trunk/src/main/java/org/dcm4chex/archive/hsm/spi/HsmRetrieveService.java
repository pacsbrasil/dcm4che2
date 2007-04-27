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

import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.mbean.FileSystemMgtService;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.dcm.movescu.MoveOrder;
import org.dcm4chex.archive.ejb.interfaces.Storage;
import org.dcm4chex.archive.ejb.interfaces.StorageHome;
import org.dcm4chex.archive.ejb.interfaces.FileSystemDTO;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.HomeFactoryException;
import org.dcm4chex.archive.util.FileUtils;
import org.dcm4chex.archive.hsm.spi.utils.StringUtils;
import org.dcm4chex.archive.hsm.spi.utils.HsmUtils;
import org.dcm4chex.archive.common.FileStatus;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.ServiceMBean;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

/**
 * @author Fuad Ibrahimov
 * @version $Id$
 * @since Feb 14, 2007
 */
public class HsmRetrieveService extends ServiceMBeanSupport implements MessageListener {

    private final static Log logger = LogFactory.getLog(HsmRetrieveService.class);

    private static final String START_PROCESSING = "About to start processing [{0}]"; // NON-NLS
    private static final String FINISHED_PROCESSING = "Finished processing [{0}]"; // NON-NLS
    private static final String ERROR_DURING_PROCESSING_MESSAGE = "Error during processing the message [{0}]"; // NON-NLS
    private static final String GIVE_UP_TO_PROCESS = "Give up to process [{0}]"; // NON-NLS
    private static final String FAILED_TO_PROCESS_SCHEDULING_RETRY = "Failed to process [{0}]. Scheduling retry."; // NON-NLS
    private static final String FAILED_TO_RETRIEVE_SCHEDULING_RETRY = "Failed to retrieve [{0}]. Scheduling retry."; // NON-NLS
    private static final String FAILED_TO_SCHEDULE_MOVESCU_ORDER = "Failed to schedule MOVE-SCU order [{0}]"; // NON-NLS
    private static final String HSM_RETRIEVE = "HsmRetrieve"; // NON-NLS
    private static final String RETRIEVE = "retrieve"; // NON-NLS
    private static final String MOVE_SCU = "MoveScu"; // NON-NLS
    private static final String UNPACK = "unpack"; // NON-NLS
    private static final String SELECT_STORAGE_FILE_SYSTEM = "selectStorageFileSystem"; // NON-NLS


    private JMSDelegate jmsDelegate = new JMSDelegate(this);

    private String hsmRetrieveQueueName = HSM_RETRIEVE;

    private String moveScuQueueName = MOVE_SCU;

    private int concurrency;

    private RetryIntervalls retryIntervalls = new RetryIntervalls();

    private Storage storage;
    private ObjectName hsmClientName;
    private ObjectName tarServiceName;
    private ObjectName fileSystemMgtName;

    private File tempDir;


    public void onMessage(Message message) {
        logger.info(MessageFormat.format(START_PROCESSING, message));
        try {
            HsmRetrieveOrder order = (HsmRetrieveOrder) ((ObjectMessage) message).getObject();
            logger.info(MessageFormat.format(START_PROCESSING, order));
            try {
                process(order, message.getJMSPriority());
                logger.info(MessageFormat.format(FINISHED_PROCESSING, message));
            } catch (Exception e) {
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = retryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    logger.error(MessageFormat.format(GIVE_UP_TO_PROCESS, order), e);
                } else {
                    logger.warn(MessageFormat.format(FAILED_TO_PROCESS_SCHEDULING_RETRY, order), e);
                    order.setThrowable(e);
                    scheduleHsmOrder(order, System.currentTimeMillis() + delay, message.getJMSPriority());
                }
            }
        } catch (Throwable t) {
            logger.error(MessageFormat.format(ERROR_DURING_PROCESSING_MESSAGE, message), t);
        }
    }

    private void process(HsmRetrieveOrder order, int jmsPriority) throws Exception {
        Map<String, List<HsmFile>> hsmFiles = order.getHsmFiles();
        for (Map.Entry<String, List<HsmFile>> entry : hsmFiles.entrySet()) {
            List<HsmFile> files = entry.getValue();
            try {
                String destination = getCurrentStorageDirPath();
                for (HsmFile hsmFile : files) {
                    retrieve(hsmFile);
                    File tarFile = new File(tempDir, hsmFile.getTarPath().replaceAll("/", File.separator));
                    unpack(tarFile, destination);
                    storage.storeFiles(hsmFile.getEntries(), destination, FileStatus.DEFAULT);
                }
            } catch (Exception e) {
                // TODO - cleanup temp files
                if (hsmFiles.size() == 1) throw e;
                logger.warn(MessageFormat.format(FAILED_TO_RETRIEVE_SCHEDULING_RETRY, files), e);
                scheduleNewHsmOrder(entry.getKey(), files, order.getDestination(), jmsPriority);
            }
            scheduleMoveScuOrder(entry.getKey(), order.getDestination(), jmsPriority);
        }
    }

    private void retrieve(HsmFile file) throws Exception {
        String fsName = HsmUtils.resolveFileSpacePath(file.getFileSpaceName());
        String filePath = fullPath(fsName, file.getTarPath());
        server.invoke(hsmClientName,
                RETRIEVE,
                new Object[]{fsName, filePath, tempDir},
                new String[]{String.class.getName(), String.class.getName(), File.class.getName()});

    }

    private String fullPath(String fileSpaceName, String filePath) {
        return fileSpaceName.replaceAll("/", File.separator) + separatorFor(fileSpaceName) + filePath.replaceAll("/",
                File.separator);
    }

    private String separatorFor(String fileSpaceName) {
        return fileSpaceName.endsWith("") ? "" : File.separator;
    }

    private String getCurrentStorageDirPath() throws Exception {
        return ((FileSystemDTO) server.invoke(fileSystemMgtName,
                SELECT_STORAGE_FILE_SYSTEM,
                new Object[]{},
                new String[]{})).getDirectoryPath();
    }

    private void unpack(File tarFile, String destination) throws Exception {
        server.invoke(tarServiceName,
                UNPACK,
                new Object[]{tarFile, destination},
                new String[]{File.class.getName(), String.class.getName()});
    }

    private void scheduleMoveScuOrder(String seriesIuid, String destination, int jmsPriority) {
        MoveOrder order = new MoveOrder(null, destination, jmsPriority, null, null, new String[]{seriesIuid});
        try {
            jmsDelegate.queue(moveScuQueueName, order, jmsPriority, System.currentTimeMillis());
        } catch (Exception e) {
            logger.error(MessageFormat.format(FAILED_TO_SCHEDULE_MOVESCU_ORDER, order));
        }
    }

    private void scheduleHsmOrder(HsmRetrieveOrder order, long scheduledTime, int jmsPriority) {
        try {
            jmsDelegate.queue(hsmRetrieveQueueName, order, jmsPriority, scheduledTime);
        } catch (Exception e) {
            logger.error(MessageFormat.format(FAILED_TO_SCHEDULE_MOVESCU_ORDER, order));
        }
    }

    private void scheduleNewHsmOrder(String seriesIuid, List<HsmFile> files, String destination, int jmsPriority) {
        Map<String, List<HsmFile>> newOrderFiles = new HashMap<String, List<HsmFile>>(1);
        newOrderFiles.put(seriesIuid, files);
        scheduleHsmOrder(new HsmRetrieveOrder(newOrderFiles, destination), System.currentTimeMillis(), jmsPriority);
    }

    protected void startService() throws Exception {
        super.startService();
        jmsDelegate.startListening(hsmRetrieveQueueName, this, concurrency);
        this.storage = getStorageHome().create();
    }


    private StorageHome getStorageHome() throws HomeFactoryException {
        return (StorageHome) EJBHomeFactory.getFactory().lookup(StorageHome.class, StorageHome.JNDI_NAME);
    }

    public final String getRetryIntervalls() {
        return retryIntervalls.toString();
    }

    public final void setRetryIntervalls(String s) {
        this.retryIntervalls = new RetryIntervalls(s);
    }

    public void setHsmRetrieveQueueName(String hsmRetrieveQueueName) {
        this.hsmRetrieveQueueName = hsmRetrieveQueueName;
    }

    public String getHsmRetrieveQueueName() {
        return hsmRetrieveQueueName;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public String getMoveScuQueueName() {
        return moveScuQueueName;
    }

    public void setMoveScuQueueName(String moveScuQueueName) {
        this.moveScuQueueName = moveScuQueueName;
    }

    public void setConcurrency(int concurrency) throws Exception {
        if (concurrency <= 0)
            throw new IllegalArgumentException("Concurrency: " + concurrency);
        if (this.concurrency != concurrency) {
            final boolean restart = getState() == ServiceMBean.STARTED;
            if (restart)
                stop();
            this.concurrency = concurrency;
            if (restart)
                start();
        }
    }

    public void setHsmClientName(ObjectName hsmClientName) {
        this.hsmClientName = hsmClientName;
    }

    public ObjectName getHsmClientName() {
        return hsmClientName;
    }

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public ObjectName getTarServiceName() {
        return tarServiceName;
    }

    public void setTarServiceName(ObjectName tarServiceName) {
        this.tarServiceName = tarServiceName;
    }

    public ObjectName getFileSystemMgtName() {
        return fileSystemMgtName;
    }

    public void setFileSystemMgtName(ObjectName fileSystemMgtName) {
        this.fileSystemMgtName = fileSystemMgtName;
    }

    public void setTempDir(String tempDir) throws IOException {
        if (!StringUtils.hasText(tempDir))
            throw new IllegalArgumentException("tempDir must be not null and not blank, but was <" + tempDir + ">");
        this.tempDir = FileUtils.toFile(tempDir);
        validateOrMkTempDir();
    }

    public String getTempDir() throws IOException {
        return this.tempDir.getCanonicalPath();
    }

    private void validateOrMkTempDir() throws IOException {
        if (!this.tempDir.exists()) {
            this.tempDir.mkdirs();
        } else if (!this.tempDir.isDirectory()) {
            throw new IllegalArgumentException("Provided tempDir doesn't point to a directory: <" + this.tempDir.getCanonicalPath() + ">");
        }
    }
}
