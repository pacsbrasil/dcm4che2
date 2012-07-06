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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
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
package org.dcm4chex.archive.hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.dcm4chex.archive.common.BaseJmsOrder;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.ejb.interfaces.MD5;
import org.dcm4chex.archive.mbean.JMSDelegate;
import org.dcm4chex.archive.mbean.SchedulerDelegate;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author franz.willer@gmail.com
 * @version $Revision: $
 * @since June 28, 2012
 */
public class HSMMigrateService extends ServiceMBeanSupport implements MessageListener {

    private static final String NONE = "NONE";
    
    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private String timerIDHSMMigrate;
    
    private boolean isRunning;

    private long taskInterval = 0L;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private boolean lastPksFirst;
    
    private int targetFileStatus;

    private String srcFilesystem;
    private String targetFilesystem;
    
    private Integer listenerID;

    private ObjectName hsmModuleServicename;
    
    private RetryIntervalls failedRetryIntervalls = new RetryIntervalls();
    private RetryIntervalls statusRetryIntervalls = new RetryIntervalls();
    private String queueName;
    private JMSDelegate jmsDelegate = new JMSDelegate(this);

    private boolean verifyTar;
    private byte[] buf = new byte[8192];
    
    private final NotificationListener timerListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            if (targetFilesystem == null || srcFilesystem == null) {
                log.debug("HSM Migration service disabled (srcFilesystems or targetFilesystem are NONE)!");
                return;
            }
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("HSM Migration service disabled in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else { 
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            migrate();
                        } catch (Exception e) {
                            log.error("HSM Migration task failed!", e);
                        }
                    }
                }).start();
            }
        }

    };

    public final String getSourceFileSystem() {
        return srcFilesystem == null ? NONE : srcFilesystem;
    }

    public final void setSourceFileSystem(String s) {
        srcFilesystem = NONE.equals(s) ? null : s;
    }
    
    public final String getTargetFileSystem() {
        return targetFilesystem == null ? NONE : targetFilesystem;
    }

    public final void setTargetFileSystem(String s) {
        targetFilesystem = NONE.equals(s) ? null : s;
    }

    public final String getTargetFileStatus() {
        return FileStatus.toString(targetFileStatus);
    }

    public final void setTargetFileStatus(String status) {
        targetFileStatus = FileStatus.toInt(status);
    }

    public boolean isVerifyTar() {
        return verifyTar;
    }

    public void setVerifyTar(boolean verifyTar) {
        this.verifyTar = verifyTar;
    }

    public final String getFailedRetryIntervalls() {
        return failedRetryIntervalls.toString();
    }

    public final void setFailedRetryIntervalls(String s) {
        this.failedRetryIntervalls = new RetryIntervalls(s);
    }

    public final String getStatusRetryIntervalls() {
        return statusRetryIntervalls.toString();
    }

    public final void setStatusRetryIntervalls(String s) {
        this.statusRetryIntervalls = new RetryIntervalls(s);
    }

    public String getTimerIDHSMMigrate() {
        return timerIDHSMMigrate;
    }

    public void setTimerIDHSMMigrate(String timerID) {
        this.timerIDHSMMigrate = timerID;
    }
 
    public final String getTaskInterval() {
        String s = RetryIntervalls.formatIntervalZeroAsNever(taskInterval);
        return (disabledEndHour == -1) ? s : s + "!" + disabledStartHour + "-"
                + disabledEndHour;
    }

    public void setTaskInterval(String interval) throws Exception {
        long oldInterval = taskInterval;
        int pos = interval.indexOf('!');
        if (pos == -1) {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval);
            disabledEndHour = -1;
        } else {
            taskInterval = RetryIntervalls.parseIntervalOrNever(interval
                    .substring(0, pos));
            int pos1 = interval.indexOf('-', pos);
            disabledStartHour = Integer.parseInt(interval.substring(pos + 1,
                    pos1));
            disabledEndHour = Integer.parseInt(interval.substring(pos1 + 1));
        }
        if (getState() == STARTED && oldInterval != taskInterval) {
            scheduler.stopScheduler(timerIDHSMMigrate, listenerID,
                    timerListener);
            listenerID = scheduler.startScheduler(timerIDHSMMigrate,
                    taskInterval, timerListener);
        }
    }

    public boolean isLastPksFirst() {
        return lastPksFirst;
    }

    public void setLastPksFirst(boolean b) {
        lastPksFirst = b;
    }

    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }

    public final ObjectName getHSMModuleServicename() {
        return hsmModuleServicename;
    }

    public final void setHSMModuleServicename(ObjectName name) {
        this.hsmModuleServicename = name;
    }

    public final ObjectName getJmsServiceName() {
        return jmsDelegate.getJmsServiceName();
    }

    public final void setJmsServiceName(ObjectName jmsServiceName) {
        jmsDelegate.setJmsServiceName(jmsServiceName);
    }
    
    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1)
            return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour;
        return sameday ? inside : !inside;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public final String getQueueName() {
        return queueName;
    }

    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    protected void startService() throws Exception {
        jmsDelegate.startListening(queueName, this, 1);
        listenerID = scheduler.startScheduler(timerIDHSMMigrate,
                taskInterval, timerListener);
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler(timerIDHSMMigrate, listenerID,
                timerListener);
        jmsDelegate.stopListening(queueName);
    }

    public int migrate() throws Exception  {
        if (srcFilesystem == null || this.targetFilesystem == null) {
            return 0;
        }
        synchronized(this) {
            if (isRunning) {
                log.info("HSM Migration service is already running!");
                return -1;
            }
            isRunning = true;
        }
        try {
            log.info("Start HSM Migration!");
            int count = 0;
            FileSystemMgt2 mgr = newFileSystemMgt();
            @SuppressWarnings("unchecked")
            Collection<String> tarFiles = mgr
                .findTarFilenamesToMigrate(srcFilesystem, lastPksFirst, this.limitNumberOfFilesPerTask);
            log.info("Found "+tarFiles.size()+" files to migrate on filesystem "+srcFilesystem);
            String srcTarFilename;
            for (Iterator<String> it = tarFiles.iterator() ; it.hasNext() ;) {
                srcTarFilename = it.next();
                if (srcTarFilename.endsWith("!"))
                    srcTarFilename = srcTarFilename.substring(0, srcTarFilename.length()-1);
                log.info("Migrate tar file: "+srcTarFilename);
                try {
                    count += migrateTarFile(srcFilesystem, srcTarFilename, this.targetFilesystem, mgr);
                } catch (Exception x) {
                    log.error("Copy tar file failed!", x);
                    mgr.setFilestatusOfFilesOfTarFile(srcFilesystem, srcTarFilename, FileStatus.MIGRATION_FAILED);
                    long delay = this.failedRetryIntervalls.getIntervall(1);
                    if (delay != -1) {
                        this.schedule(new MigrationOrder(srcFilesystem, srcTarFilename, targetFilesystem, null), System.currentTimeMillis()+delay);
                    }
                }
            }
            return count;
        } finally {
            isRunning = false;
        }
    }
    
    private int migrateTarFile(String srcFsId, String srcTarFilename, String targetFsId, FileSystemMgt2 mgr) throws Exception {
        File src = fetchTarFile(srcFsId, srcTarFilename);
        if (verifyTar) {
            try {
                List<FileDTO> missingFiles = verifyTar(src, mgr.getFilesOfTarFile(srcFsId, srcTarFilename), true);
                for (int i = 0, len = missingFiles.size() ; i < len ; i++) {
                    mgr.setFileStatus(missingFiles.get(i).getPk(), FileStatus.MD5_CHECK_FAILED);
                }
            } catch (Exception x) {
                log.error(x.getMessage()+" Set file status of source tar file entities to MD5_CHECK_FAILED and skip migration of "+srcTarFilename);
                mgr.setFilestatusOfFilesOfTarFile(srcFsId, srcTarFilename, FileStatus.MD5_CHECK_FAILED);
                fetchTarFileFinished(srcFsId, srcTarFilename, src);
                return 0;
            }
        }
        int nrOfFiles = 0;
        String targetTarFilename = srcTarFilename;
        File target = null;
        try {
            target = prepareHSMFile(targetFsId, srcTarFilename);
            if(!target.exists()) {
                if (!target.getParentFile().exists()) {
                    log.info("M-CREATE Directory "+target);
                    target.getParentFile().mkdirs();
                }
                log.info("M-CREATE "+target);
                target.createNewFile();
            }
            if (target.length() > 0) {
                log.warn("Target tar file "+target+" already exists! Skip migration with assumption that this file is already migrated and set file status of source to MIGRATED.");
                mgr.setFilestatusOfFilesOfTarFile(srcFsId, srcTarFilename, FileStatus.MIGRATED);
                return 0;
            } else {
                FileChannel srcCh = null;
                FileChannel destCh = null;
                try {
                    srcCh = new FileInputStream(src).getChannel();
                    destCh = new FileOutputStream(target).getChannel();
                    destCh.transferFrom(srcCh, 0, srcCh.size());
                } finally {
                  if(srcCh != null)
                      srcCh.close();
                  if(destCh != null)
                      destCh.close();
                }
            }
            targetTarFilename = storeHSMFile(target, targetFsId, srcTarFilename);
            nrOfFiles = mgr.migrateFilesOfTarFile(srcFsId, srcTarFilename, targetFsId, targetTarFilename, targetFileStatus);
            if (verifyTar) {
                verifyTar(fetchTarFile(targetFsId, targetTarFilename), mgr.getFilesOfTarFile(targetFsId, targetTarFilename), false);
            }
        } catch (Exception x) {
            if (target != null) {
                this.failedHSMFile(target, targetFsId, targetTarFilename);
                target.delete();
                mgr.deleteFilesOfInvalidTarFile(targetFsId, targetTarFilename);
            }
            throw x;
        } finally {
            fetchTarFileFinished(srcFsId, srcTarFilename, src);
        }
        this.schedule(new MigrationOrder(srcFilesystem, srcTarFilename, targetFilesystem, targetTarFilename), 0);
        return nrOfFiles;
    }

    private List<FileDTO> verifyTar(File file, FileDTO[] files, boolean markMissingFiles) throws IOException, VerifyTarException {
        Map<String, byte[]> entries = VerifyTar.verify(file, buf);
        if (entries == null) 
            throw new VerifyTarException("Verify tar failed! Tar file has no entries.");
        List<FileDTO> missingFiles = new ArrayList<FileDTO>();
        for (int i = 0 ; i < files.length ; i++) {
            String filePath = files[i].getFilePath();
            String filepathInTar = files[i].getFilePath().substring(filePath.indexOf('!')+1);
            byte[] md5 = entries.get(filepathInTar);
            if (md5 == null) {
                String msg = "Verify tar failed! "+filepathInTar+" not found in tar file "+file;
                if (markMissingFiles) {
                    log.error(msg+". Set file status to MD5_CHECK_FAILED");
                    missingFiles.add(files[i]);
                } else {
                    throw new VerifyTarException(msg);
                }
            }
            if (!Arrays.equals(files[i].getFileMd5(), md5)) {
                String msg = "Verify tar failed! Different MD5 of file "+filepathInTar+" and file entity! ("+
                              MD5.toString(md5)+" vs. "+files[i].getMd5String()+ ")";
                if (markMissingFiles) {
                    log.error(msg+". Set file status to MD5_CHECK_FAILED");
                    missingFiles.add(files[i]);
                } else {
                    throw new VerifyTarException(msg);
                }
            }
        }
        if (missingFiles.size() == files.length)
            throw new VerifyTarException("Verify tar failed! No file entity found in tar file.");
        return missingFiles;
    }

    protected boolean schedule(BaseJmsOrder order, long scheduledTime) {
        try {
            log.info("Scheduling " + order + (scheduledTime == 0 ? 
                    " now" : " at "+new Date(scheduledTime)) );
            jmsDelegate.queue(queueName, order, Message.DEFAULT_PRIORITY,
                    scheduledTime);
            return true;
        } catch (Exception e) {
            log.error("Failed to schedule " + order, e);
            return false;
        }
    }
    
    public void onMessage(Message message) {
        ObjectMessage om = (ObjectMessage) message;
        try {
            MigrationOrder order = (MigrationOrder) om.getObject();
            log.info("Start processing " + order);
            try {
                if (order.getTargetTarFilename() == null) {
                    migrateTarFile(order.getSrcFsId(), order.getSrcTarFilename(), order.getTargetFsId(), newFileSystemMgt());
                } else {
                    processStatusCheck(order);
                }
                log.info("Finished processing " + order);
            } catch (Exception e) {
                order.setThrowable(e);
                final int failureCount = order.getFailureCount() + 1;
                order.setFailureCount(failureCount);
                final long delay = order.getTargetTarFilename() == null ? 
                        failedRetryIntervalls.getIntervall(failureCount): this.statusRetryIntervalls.getIntervall(failureCount);
                if (delay == -1L) {
                    log.error("Give up to process " + order, e);
                    jmsDelegate.fail(queueName,order);
                } else {
                    log.warn("Failed to process " + order
                            + ". Scheduling retry.", e);
                    schedule(order, System.currentTimeMillis() + delay);
                }
            }
        } catch (Throwable e) {
            log.error("unexpected error during processing message: " + message, e);
        }
     }
    
    private void processStatusCheck(MigrationOrder order) throws Exception {
        Integer status = queryStatus(order.getTargetFsId(), order.getTargetTarFilename(), null);
        if (status == null) {
            log.info("No status change of target tar file "+order.getTargetTarFilename());
            return;
        }
        log.debug("Status of target tar file("+order.getTargetTarFilename()+"):"+FileStatus.toString(status)+" ("+status+")");
        if (status == FileStatus.ARCHIVED) {
            FileSystemMgt2 mgr = newFileSystemMgt();
            mgr.setFilestatusOfFilesOfTarFile(order.getTargetFsId(), order.getTargetTarFilename(), status);
            log.debug("##### delete file entities of source tar file");
            mgr.deleteFilesOfInvalidTarFile(order.getSrcFsId(), order.getSrcTarFilename());
        } else {
            throw new Exception("Not Archived! current status:"+FileStatus.toString(status));
        }
    }

    private File fetchTarFile(String fsID, String tarPath) throws IOException {
        try {
            return (File) server.invoke(hsmModuleServicename, "fetchHSMFile", new Object[]{fsID, tarPath}, 
                new String[]{String.class.getName(),String.class.getName()});
        } catch (Exception x) {
            log.error("Fetch of HSMFile failed! fsID:"+fsID+" tarPath:"+tarPath, x);
            IOException iox = new IOException("Fetch of HSMFile failed!");
            iox.initCause(x);
            throw iox;
        }
    }
    private void fetchTarFileFinished(String fsID, String tarPath, File tarFile) {
        try {
            server.invoke(hsmModuleServicename, "fetchHSMFileFinished", new Object[]{fsID, tarPath, tarFile}, 
                        new String[]{String.class.getName(),String.class.getName(),File.class.getName()});
        } catch (Exception x) {
            log.warn("fetchHSMFileFinished failed! fsID:"+fsID+" tarPath:"+tarPath+" tarFile:"+tarFile, x);
        }
    }
    private File prepareHSMFile(String fsID, String filePath) throws InstanceNotFoundException, MBeanException, ReflectionException {
        return (File) server.invoke(hsmModuleServicename, "prepareHSMFile", new Object[]{fsID, filePath}, 
               new String[]{String.class.getName(),String.class.getName()});
    }

    private String storeHSMFile(File file, String fsID, String filePath) throws InstanceNotFoundException, MBeanException,
              ReflectionException {
        return (String) server.invoke(hsmModuleServicename, "storeHSMFile", 
               new Object[]{file, fsID, filePath}, 
               new String[]{File.class.getName(),String.class.getName(),String.class.getName()});
    }

    private void failedHSMFile(File file, String fsID, String filePath) throws InstanceNotFoundException, MBeanException,
              ReflectionException {
        server.invoke(hsmModuleServicename, "failedHSMFile", new Object[]{file, fsID, filePath}, 
            new String[]{File.class.getName(),String.class.getName(),String.class.getName()});
    }
    private Integer queryStatus(String fsID, String filePath, String userInfo) throws InstanceNotFoundException, MBeanException,
            ReflectionException {
        return (Integer) server.invoke(hsmModuleServicename, "queryStatus", new Object[]{fsID, filePath, userInfo}, 
                new String[]{String.class.getName(),String.class.getName(),String.class.getName()});
    }

    private FileSystemMgt2 newFileSystemMgt() throws Exception {
        return ((FileSystemMgt2Home) EJBHomeFactory.getFactory().lookup(
                FileSystemMgt2Home.class, FileSystemMgt2Home.JNDI_NAME)).create();
    }

}
