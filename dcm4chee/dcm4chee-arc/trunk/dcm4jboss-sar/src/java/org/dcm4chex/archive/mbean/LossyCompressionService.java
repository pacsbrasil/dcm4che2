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
 * Portions created by the Initial Developer are Copyright (C) 2006-2008
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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
package org.dcm4chex.archive.mbean;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.archive.codec.CompressCmd;
import org.dcm4chex.archive.codec.DecompressCmd;
import org.dcm4chex.archive.common.FileStatus;
import org.dcm4chex.archive.common.PrivateTags;
import org.dcm4chex.archive.config.RetryIntervalls;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2;
import org.dcm4chex.archive.ejb.interfaces.FileSystemMgt2Home;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Nov 11, 2009
 */
public class LossyCompressionService extends ServiceMBeanSupport {

    private static final String COMPRESS_FILE_FORMAT = 
        "File Compression Ratio: {0,number,#.##} : 1\n"
        + " Pixel Compression Ratio: {1,number,#.##} : 1";

    private static final String COMPRESS_SERIES_FORMAT =
        "{0} images compressed\n"
        + "File Compression Ratio: {1,number,#.##}/{2,number,#.##}/{3,number,#.##} : 1\n"
        + "Pixel Compression Ratio: {4,number,#.##}/{5,number,#.##}/{6,number,#.##} : 1";

    private static final UIDGenerator uidGenerator = UIDGenerator.getInstance();

    private ObjectName storeScpServiceName;

    private final SchedulerDelegate scheduler = new SchedulerDelegate(this);

    private long taskInterval = 0L;

    private String timerIDCheckFilesToCompress;

    private Integer listenerID;

    private int disabledStartHour;

    private int disabledEndHour;

    private int limitNumberOfFilesPerTask;

    private int bufferSize = 8192;

    private File tmpDir;

    private String fsGroupID;

    private String sourceAET;

    private String seriesDescription;

    private List<CompressionRule> compressionRuleList =
            new ArrayList<CompressionRule>();

    private boolean isRunning;

    private final NotificationListener delayedCompressionListener = new NotificationListener() {
        public void handleNotification(Notification notif, Object handback) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if (isDisabled(hour)) {
                if (log.isDebugEnabled())
                    log.debug("trigger ignored in time between "
                            + disabledStartHour + " and " + disabledEndHour
                            + " !");
            } else {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            checkForFilesToCompress();
                        } catch (Exception e) {
                            log.error("Delayed compression failed!", e);
                        }
                    }
                }).start();
            }
        }
    };

    public final void setStoreScpServiceName(ObjectName storeScpServiceName) {
        this.storeScpServiceName = storeScpServiceName;
    }

    public final ObjectName getStoreScpServiceName() {
        return storeScpServiceName;
    }

    public ObjectName getSchedulerServiceName() {
        return scheduler.getSchedulerServiceName();
    }

    public void setSchedulerServiceName(ObjectName schedulerServiceName) {
        scheduler.setSchedulerServiceName(schedulerServiceName);
    }

    public final String getFileSystemGroupID() {
        return fsGroupID;
    }

    public String getTimerIDCheckFilesToCompress() {
        return timerIDCheckFilesToCompress;
    }

    public void setTimerIDCheckFilesToCompress(
            String timerIDCheckFilesToCompress) {
        this.timerIDCheckFilesToCompress = timerIDCheckFilesToCompress;
    }

    public final void setFileSystemGroupID(String fsGroupID) {
        this.fsGroupID = fsGroupID.trim();
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
            scheduler.stopScheduler(timerIDCheckFilesToCompress, listenerID,
                    delayedCompressionListener);
            listenerID = scheduler.startScheduler(timerIDCheckFilesToCompress,
                    taskInterval, delayedCompressionListener);
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }

    public final void setCompressionRules(String rules) {
        this.compressionRuleList.clear();
        if (rules == null || rules.trim().length() < 1)
            return;
        StringTokenizer st = new StringTokenizer(rules, ",;\n\r\t ");
        while (st.hasMoreTokens()) {
            compressionRuleList.add(new CompressionRule(st.nextToken()));
        }
    }

    public final String getCompressionRules() {
        StringBuffer sb = new StringBuffer();
        Iterator iter = this.compressionRuleList.iterator();
        while (iter.hasNext()) {
            sb.append(((CompressionRule) iter.next())).append("\r\n");
        }
        return sb.toString();
    }

    public int getLimitNumberOfFilesPerTask() {
        return limitNumberOfFilesPerTask;
    }

    public void setLimitNumberOfFilesPerTask(int limit) {
        this.limitNumberOfFilesPerTask = limit;
    }

    public final String getSourceAET() {
        return sourceAET;
    }

    public final void setSourceAET(String sourceAET) {
        this.sourceAET = sourceAET.trim();
    }

    public final String getSeriesDescription() {
        return seriesDescription;
    }

    public final void setSeriesDescription(String seriesDescription) {
        this.seriesDescription = seriesDescription.trim();
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final void setTempDir(String dirPath) {
        tmpDir = new File(dirPath);
    }

    public final String getTempDir() {
        return tmpDir.toString();
    }

    public String compressFileJPEGLossy(String inFilePath, String outFilePath,
            float compressionQuality, float estimatedCompressionRatio,
            boolean newSOPInstanceUID, boolean newSeriesInstanceUID)
            throws Exception {
        File inFile = new File(inFilePath.trim());
        File outFile = new File(outFilePath.trim());
        float[] actualCompressionRatio = new float[1];
        CompressCmd.compressFileJPEGLossy(inFile, outFile, compressionQuality,
                estimatedCompressionRatio, actualCompressionRatio , 
                newSOPInstanceUID ? uidGenerator.createUID() : null,
                newSeriesInstanceUID ? uidGenerator.createUID() : null,
                new byte[bufferSize], null);
        return MessageFormat.format(COMPRESS_FILE_FORMAT,
                (float) inFile.length() / outFile.length(),
                actualCompressionRatio[0]);
    }

    private boolean isUncompressed(String tsuid) {
        return tsuid.equals(UIDs.ExplicitVRLittleEndian)
                || tsuid.equals(UIDs.ExplicitVRBigEndian)
                || tsuid.equals(UIDs.ImplicitVRLittleEndian);
    }

    private boolean isLossyCompressed(String tsuid) {
        return tsuid.equals(UIDs.JPEGBaseline)
                || tsuid.equals(UIDs.JPEGExtended)
                || tsuid.equals(UIDs.JPEGLSLossy)
                || tsuid.equals(UIDs.JPEG2000Lossy);
    }

    public String compressSeriesJPEGLossy(String seriesIUID,
            float compressionQuality, float estimatedCompressionRatio,
            boolean archive) throws Exception {
        FileSystemMgt2 fsmgt = newFileSystemMgt();
        FileDTO[] fileDTOs = fsmgt.getFilesOfSeriesOnFileSystemGroup(
                seriesIUID.trim(), fsGroupID);
        byte[] buffer = new byte[bufferSize];
        float[] pixelCompressionRatio = new float[1];
        float fileCompressionRatio;
        float minFileCompressionRatio = Float.MAX_VALUE;
        float maxFileCompressionRatio = Float.MIN_VALUE;
        float sumFileCompressionRatio = 0.f;
        float minPixelCompressionRatio = Float.MAX_VALUE;;
        float maxPixelCompressionRatio = Float.MIN_VALUE;
        float sumPixelCompressionRatio = 0.f;
        int count = 0;
        String suid = uidGenerator.createUID();
        for (int i = 0; i < fileDTOs.length; i++) {
            FileDTO fileDTO = fileDTOs[i];
            String tsuid = fileDTO.getFileTsuid();
            if (isLossyCompressed(tsuid))
                continue;

            File srcFile = FileUtils.toFile(fileDTO.getDirectoryPath(),
                    fileDTO.getFilePath());
            File destFile = FileUtils.createNewFile(srcFile.getParentFile(),
                    (int) Long.parseLong(srcFile.getName(), 16) + 1);
            File uncFile = null;
            try {
                if (!isUncompressed(tsuid)) {
                    File absTmpDir = FileUtils.resolve(tmpDir);
                    if (absTmpDir.mkdirs())
                        log.info("Create directory for decompressed files");
                    uncFile = new File(absTmpDir,
                            fileDTO.getFilePath().replace('/', '-') + ".dcm");
                    DecompressCmd.decompressFile(srcFile, uncFile,
                            UIDs.ExplicitVRLittleEndian, VRs.OW, buffer);
                    srcFile = uncFile;
                }
                String iuid = uidGenerator.createUID();
                Dataset ds = DcmObjectFactory.getInstance().newDataset();
                byte[] md5 = CompressCmd.compressFileJPEGLossy(srcFile,
                        destFile, compressionQuality, estimatedCompressionRatio,
                        pixelCompressionRatio , iuid, suid, buffer, ds);
                fileCompressionRatio =
                    (float) srcFile.length() / destFile.length();
                if (minFileCompressionRatio > fileCompressionRatio)
                    minFileCompressionRatio = fileCompressionRatio;
                if (maxFileCompressionRatio < fileCompressionRatio)
                    maxFileCompressionRatio = fileCompressionRatio;
                sumFileCompressionRatio += fileCompressionRatio;
                if (minPixelCompressionRatio > pixelCompressionRatio[0])
                    minPixelCompressionRatio = pixelCompressionRatio[0];
                if (maxPixelCompressionRatio < pixelCompressionRatio[0])
                    maxPixelCompressionRatio = pixelCompressionRatio[0];
                sumPixelCompressionRatio += pixelCompressionRatio[0];
                count++;
                if (archive) {
                    File baseDir = FileUtils.toFile(fileDTO.getDirectoryPath());
                    int baseDirPathLength = baseDir.getPath().length();
                    String destFilePath = destFile.getPath()
                            .substring(baseDirPathLength + 1)
                            .replace(File.separatorChar, '/');
                    fileDTO.setPk(0);
                    fileDTO.setSopInstanceUID(iuid);
                    fileDTO.setFilePath(destFilePath);
                    fileDTO.setFileSize((int) destFile.length());
                    fileDTO.setFileMd5(md5);
                    fileDTO.setFileTsuid(
                            ds.getFileMetaInfo().getTransferSyntaxUID());
                    updateSeriesDescription(ds);
                    ds.setPrivateCreatorID(PrivateTags.CreatorID);
                    ds.putAE(PrivateTags.CallingAET, sourceAET);
                    importFile(fileDTO, ds, suid, i == fileDTOs.length - 1);
                    destFile = null;
                }
            } finally {
                if (uncFile != null)
                    FileUtils.delete(uncFile, false);
                if (destFile != null)
                    FileUtils.delete(destFile, false);
            }
        }
        return count == 0 ? "No images compressed"
                : MessageFormat.format(COMPRESS_SERIES_FORMAT,
                        count,
                        minFileCompressionRatio,
                        sumFileCompressionRatio / count,
                        maxFileCompressionRatio,
                        minPixelCompressionRatio,
                        sumPixelCompressionRatio / count,
                        maxPixelCompressionRatio);
    }

    private void updateSeriesDescription(Dataset ds) {
        if ("{}".equals(seriesDescription))
            return;
        String s = seriesDescription.replace("{}",
                ds.getString(Tags.SeriesDescription, ""));
        if (s.length() > 64)
            s = s.substring(0, 64);
        ds.putLO(Tags.SeriesDescription, s);
    }

    private void importFile(FileDTO fileDTO, Dataset ds, String prevseriuid,
            boolean last) throws Exception {
        server.invoke(this.getStoreScpServiceName(), "importFile", new Object[] {
                fileDTO, ds, prevseriuid, new Boolean(last) }, new String[] {
                FileDTO.class.getName(), Dataset.class.getName(),
                String.class.getName(), boolean.class.getName() });
    }

    private boolean isDisabled(int hour) {
        if (disabledEndHour == -1)
            return false;
        boolean sameday = disabledStartHour <= disabledEndHour;
        boolean inside = hour >= disabledStartHour && hour < disabledEndHour;
        return sameday ? inside : !inside;
    }

    protected void startService() throws Exception {
        listenerID = scheduler.startScheduler(timerIDCheckFilesToCompress,
                taskInterval, delayedCompressionListener);
    }

    protected void stopService() throws Exception {
        scheduler.stopScheduler(timerIDCheckFilesToCompress, listenerID,
                delayedCompressionListener);
        super.stopService();
    }

    private FileSystemMgt2 newFileSystemMgt() {
        try {
            FileSystemMgt2Home home = (FileSystemMgt2Home) EJBHomeFactory
                    .getFactory().lookup(FileSystemMgt2Home.class,
                            FileSystemMgt2Home.JNDI_NAME);
            return home.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access File System Mgt EJB:",
                    e);
        }
    }

    private static final class CompressionRule {
        final String cuid;
        final String bodyPart;
        final String srcAET;
        final long delay;
        final float quality;
        final float ratio;
        
        public CompressionRule(String s) {
            String[] a = StringUtils.split(s, ':');
            if (a.length != 6)
                throw new IllegalArgumentException(s);
            uidOf(a[0]);
            cuid = a[0];
            bodyPart = "*".equals(a[1]) || "".equals(a[1]) ? null : a[1];
            srcAET = a[2];
            delay = RetryIntervalls.parseInterval(a[3]);
            quality = Float.parseFloat(a[4]);
            ratio = Float.parseFloat(a[5]);
        }

        public String toString() {
            return cuid
                    + ':' + (bodyPart == null ? "*" : bodyPart)
                    + ':' + srcAET
                    + ':' + RetryIntervalls.formatInterval(delay)
                    + ':' + quality
                    + ':' + ratio;
        }

    }

    private static String uidOf(String nameOrUID) {
        return Character.isDigit(nameOrUID.charAt(0)) ? nameOrUID 
                : UIDs.forName(nameOrUID);
    }

    public void checkForFilesToCompress() throws FinderException, IOException {
        synchronized(this) {
            if (isRunning) {
                log.info("checkForFilesToCompress is already running!");
                return;
            }
            isRunning = true;
        }
        try {
            log.info("Check For Files To Lossy Compress");
            int limit = limitNumberOfFilesPerTask;
            byte[] buffer = null;
            FileSystemMgt2 fsMgt = newFileSystemMgt();
            for (CompressionRule rule : compressionRuleList) {
                FileDTO[] files = fsMgt.findFilesToLossyCompress(
                            fsGroupID, uidOf(rule.cuid), rule.bodyPart,
                            rule.srcAET, new Timestamp(
                                    System.currentTimeMillis() - rule.delay),
                            limit);
                for (FileDTO fileDTO : files) {
                    if (buffer == null)
                        buffer = new byte[bufferSize];
                    doCompress(fsMgt, fileDTO, rule, buffer);
                    if (--limit <= 0)
                        break;
                }
                if (limit <= 0)
                    break;
            }
        } finally {
            isRunning = false;
        }
    }

    private void doCompress(FileSystemMgt2 fsMgt, FileDTO fileDTO,
            CompressionRule rule, byte[] buffer) {
        String tsuid = fileDTO.getFileTsuid();
        File srcFile = FileUtils.toFile(fileDTO.getDirectoryPath(),
                fileDTO.getFilePath());
        File destFile = null;
        File uncFile = null;
        try {
            destFile = FileUtils.createNewFile(srcFile.getParentFile(),
                    (int) Long.parseLong(srcFile.getName(), 16) + 1);
            if (!isUncompressed(tsuid)) {
                File absTmpDir = FileUtils.resolve(tmpDir);
                if (absTmpDir.mkdirs())
                    log.info("Create directory for decompressed files");
                uncFile = new File(absTmpDir,
                        fileDTO.getFilePath().replace('/', '-') + ".dcm");
                DecompressCmd.decompressFile(srcFile, uncFile,
                        UIDs.ExplicitVRLittleEndian, VRs.OW, buffer);
                srcFile = uncFile;
            }
            Dataset ds = DcmObjectFactory.getInstance().newDataset();
            byte[] md5 = CompressCmd.compressFileJPEGLossy(srcFile, destFile,
                    rule.quality, rule.ratio, null , null, null, buffer, ds);
            File baseDir = FileUtils.toFile(fileDTO.getDirectoryPath());
            int baseDirPathLength = baseDir.getPath().length();
            String destFilePath = destFile.getPath()
                    .substring(baseDirPathLength + 1)
                    .replace(File.separatorChar, '/');
            if (log.isDebugEnabled())
                log.debug("replace File " + srcFile + " with " + destFile);
            fsMgt.replaceFileAndCoerceAttributes(fileDTO.getPk(),
                    destFilePath, ds.getFileMetaInfo().getTransferSyntaxUID(),
                    destFile.length(), md5, FileStatus.DEFAULT, ds);
            destFile = null;
        } catch (Exception e) {
            log.error("Lossy Compression of " + fileDTO + " failed:", e);
            try {
                fsMgt.setFileStatus(fileDTO.getPk(),
                                FileStatus.COMPRESS_FAILED);
            } catch (Exception x1) {
                log.error("Failed to set FAILED_TO_COMPRESS for file "
                        + srcFile);
            }
        } finally {
            if (uncFile != null)
                FileUtils.delete(uncFile, false);
            if (destFile != null)
                FileUtils.delete(destFile, false);
        }
    }
}
