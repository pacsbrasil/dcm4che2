/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.dcm4chex.cdw.common.FileUtils;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class SpoolDirService extends ServiceMBeanSupport {

    private static final long MIN_HWM = 10000000L;

    private static final long MS_PER_MINUTE = 60000L;

    private static final long MS_PER_HOUR = MS_PER_MINUTE * 60;

    private static final long MS_PER_DAY = MS_PER_HOUR * 24;

    private String archiveDirPath;

    private String filesetDirPath;

    private String requestDirPath;

    private File archiveDir;

    private File filesetDir;

    private File requestDir;

    private long archiveDiskUsage = 0L;

    private long archiveHighWaterMark = MIN_HWM;

    private long aduRefreshTime;

    private long aduRefreshInterval = MS_PER_HOUR;

    private long filesetDiskUsage = 0L;

    private long filesetHighWaterMark = MIN_HWM;

    private long fsduRefreshTime;

    private long fsduRefreshInterval = MS_PER_HOUR;

    private long purgeMediaCreationRequestsAfter = MS_PER_DAY;

    private long purgePreservedInstancesAfter = MS_PER_DAY;

    private long purgeTemporaryFilesAfter = MS_PER_HOUR;

    private int numberOfArchiveBuckets = 37;

    public final String getArchiveDirPath() {
        return archiveDirPath;
    }

    public void setArchiveDirPath(String path) {
        File dir = resolve(new File(path));
        checkDir(dir);
        this.archiveDirPath = path;
        this.archiveDir = dir;
        aduRefreshTime = 0L;
    }

    private File resolve(File dir) {
        if (dir.isAbsolute()) return dir;
        File dataDir = ServerConfigLocator.locate().getServerHomeDir();
        return new File(dataDir, dir.getPath());
    }

    private void checkDir(File dir) {
        if (dir.mkdirs()) log.debug("M-WRITE " + dir);
        if (!dir.isDirectory() || !dir.canWrite())
                throw new IllegalArgumentException(dir
                        + " is not a writable directory!");
    }

    public final String getFilesetDirPath() {
        return filesetDirPath;
    }

    public void setFilesetDirPath(String path) {
        File dir = resolve(new File(path));
        checkDir(dir);
        this.filesetDirPath = path;
        this.filesetDir = dir;
        fsduRefreshTime = 0L;
    }

    public final String getRequestDirPath() {
        return requestDirPath;
    }

    public void setRequestDirPath(String path) {
        File dir = resolve(new File(path));
        checkDir(dir);
        this.requestDirPath = path;
        this.requestDir = dir;
    }

    public final boolean isArchiveHighWater() {
        return archiveDiskUsage > archiveHighWaterMark;
    }

    public final String getArchiveHighWaterMark() {
        return FileUtils.formatSize(archiveHighWaterMark);
    }

    public final void setArchiveHighWaterMark(String str) {
        this.archiveHighWaterMark = FileUtils.parseSize(str, MIN_HWM);
    }

    public final String getArchiveDiskUsage() {
        return FileUtils.formatSize(archiveDiskUsage);
    }

    public final boolean isFilesetHighWater() {
        return filesetDiskUsage > filesetHighWaterMark;
    }

    public final String getFilesetHighWaterMark() {
        return FileUtils.formatSize(filesetHighWaterMark);
    }

    public final void setFilesetHighWaterMark(String str) {
        this.filesetHighWaterMark = FileUtils.parseSize(str, MIN_HWM);
    }

    public final String getFilesetDiskUsage() {
        return FileUtils.formatSize(filesetDiskUsage);
    }

    public void refreshArchiveDiskUsage() {
        log.info("Calculating Archive Disk Usage");
        archiveDiskUsage = 0L;
        register(archiveDir);
        log.info("Calculated Archive Disk Usage: " + getArchiveDiskUsage());
        aduRefreshTime = System.currentTimeMillis();
    }

    public void refreshFilesetDiskUsage() {
        log.info("Calculating Fileset Disk Usage");
        filesetDiskUsage = 0L;
        register(filesetDir);
        log.info("Calculated Fileset Disk Usage: " + getFilesetDiskUsage());
        fsduRefreshTime = System.currentTimeMillis();
    }

    public void register(File f) {
        if (!f.exists()) return;
        if (f.isDirectory()) {
            String[] ss = f.list();
            for (int i = 0; i < ss.length; i++)
                register(new File(f, ss[i]));
        } else {
            final String fpath = f.getPath();
            if (fpath.startsWith(archiveDir.getPath()))
                archiveDiskUsage += f.length();
            else if (fpath.startsWith(filesetDir.getPath()))
                    filesetDiskUsage += f.length();
        }
    }

    public final int getNumberOfArchiveBuckets() {
        return numberOfArchiveBuckets;
    }

    public final void setNumberOfArchiveBuckets(int numberOfArchiveBuckets) {
        if (numberOfArchiveBuckets < 1 || numberOfArchiveBuckets > 1000)
                throw new IllegalArgumentException("numberOfArchiveBuckets:"
                        + numberOfArchiveBuckets + " is not between 1 and 1000");
        this.numberOfArchiveBuckets = numberOfArchiveBuckets;
    }

    public final String getArchiveDiskUsageRefreshInterval() {
        return timeAsString(aduRefreshInterval);
    }

    public final void setArchiveDiskUsageRefreshInterval(String refreshInterval) {
        this.aduRefreshInterval = timeFromString(refreshInterval);
    }

    public final String getFilesetDiskUsageRefreshInterval() {
        return timeAsString(fsduRefreshInterval);
    }

    public final void setFilesetDiskUsageRefreshInterval(String refreshInterval) {
        this.fsduRefreshInterval = timeFromString(refreshInterval);
    }

    public final String getPurgePreservedInstancesAfter() {
        return timeAsString(purgePreservedInstancesAfter);
    }

    public final void setPurgePreservedInstancesAfter(String s) {
        this.purgePreservedInstancesAfter = timeFromString(s);
    }

    public final String getPurgeMediaCreationRequestsAfter() {
        return timeAsString(purgeMediaCreationRequestsAfter);
    }

    public final void setPurgeMediaCreationRequestsAfter(String s) {
        this.purgeMediaCreationRequestsAfter = timeFromString(s);
    }

    public final String getPurgeTemporaryFilesAfter() {
        return timeAsString(purgeTemporaryFilesAfter);
    }

    public final void setPurgeTemporaryFilesAfter(String s) {
        this.purgeTemporaryFilesAfter = timeFromString(s);
    }

    public File getInstanceFile(String iuid) {
        final int i = (iuid.hashCode() & 0x7FFFFFFF) % numberOfArchiveBuckets;
        File bucket = new File(archiveDir, String.valueOf(i));
        if (bucket.mkdirs()) log.debug("Success: M-WRITE " + bucket);
        return new File(bucket, iuid);
    }

    public File getMediaCreationRequestFile(String iuid) {
        return new File(requestDir, iuid);
    }

    public File getMediaFilesetRootDir(String iuid) {
        return new File(filesetDir, iuid);
    }

    public void purge() {
        purgeExpiredMediaCreationRequests();
        purgeExpiredPreservedInstances();
        purgeResidualTemporaryFiles();
        final long now = System.currentTimeMillis();
        if (now > aduRefreshTime + aduRefreshInterval)
                refreshArchiveDiskUsage();
        if (now > fsduRefreshTime + fsduRefreshInterval)
                refreshFilesetDiskUsage();
    }

    public void purgeExpiredMediaCreationRequests() {
        if (purgePreservedInstancesAfter == 0) return;
        deleteFilesModifiedBefore(requestDir, System.currentTimeMillis()
                - purgeMediaCreationRequestsAfter);
    }

    public void purgeExpiredPreservedInstances() {
        if (purgePreservedInstancesAfter == 0) return;
        String[] ss = archiveDir.list();
        if (ss == null) return;
        final long modifiedBefore = System.currentTimeMillis()
                - purgePreservedInstancesAfter;
        for (int i = 0; i < ss.length; i++) {
            deleteFilesModifiedBefore(new File(archiveDir, ss[i]),
                    modifiedBefore);
        }
    }

    public void purgeResidualTemporaryFiles() {
        if (purgePreservedInstancesAfter == 0) return;
        deleteFilesModifiedBefore(filesetDir, System.currentTimeMillis()
                - purgeTemporaryFilesAfter);
    }

    private void deleteFilesModifiedBefore(File subdir, long modifiedBefore) {
        String[] ss = subdir.list();
        if (ss == null) return;
        for (int i = 0, n = ss.length; i < n; i++) {
            File f = new File(subdir, ss[i]);
            if (f.lastModified() <= modifiedBefore) {
                delete(f);
            }
        }
    }

    public boolean deleteInstanceFile(String iuid) {
        return delete(getInstanceFile(iuid));
    }

    public boolean delete(File f) {
        if (!f.exists()) return false;
        long length = 0L;
        if (f.isDirectory()) {
            String[] ss = f.list();
            for (int i = 0; i < ss.length; i++)
                delete(new File(f, ss[i]));
        } else {
            length = f.length();
        }
        log.debug("Success: M-DELETE " + f);
        boolean success = f.delete();
        if (success) {
            final String fpath = f.getPath();
            if (fpath.startsWith(archiveDir.getPath()))
                archiveDiskUsage -= length;
            else if (fpath.startsWith(filesetDir.getPath()))
                    filesetDiskUsage -= length;
        } else
            log.warn("Failed: M-DELETE " + f);
        return success;
    }

    public boolean copy(File src, File dest, byte[] b) {
        if (src.isDirectory()) {
            String[] ss = src.list();
            for (int i = 0; i < ss.length; i++)
                if (!copy(new File(src, ss[i]), new File(dest, ss[i]), b))
                        return false;
            return true;
        }
        delete(dest);
        dest.getParentFile().mkdirs();
        try {
            FileInputStream fis = new FileInputStream(src);
            try {
                FileOutputStream fos = new FileOutputStream(dest);
                try {
                    int read;
//                    byte[] b = (byte[]) buffer;
                    while ((read = fis.read(b)) != -1)
                        fos.write(b, 0, read);
                } finally {
                    try {
                        fos.close();
                    } catch (IOException ignore) {
                    }
                    register(dest);
                }
            } finally {
                fis.close();
            }
            if (log.isDebugEnabled())
                    log.debug("Success: M-COPY " + src + " -> " + dest);
            return true;
        } catch (IOException e) {
            log.error("Failed: M-COPY " + src + " -> " + dest, e);
            return false;
        }
    }

    public boolean move(File src, File dest) {
        if (!src.exists()) return false;
        if (src.isDirectory()) {
            String[] ss = src.list();
            for (int i = 0; i < ss.length; i++)
                if (!move(new File(src, ss[i]), new File(dest, ss[i])))
                        return false;
            return true;
        }
        delete(dest);
        dest.getParentFile().mkdirs();
        if (src.renameTo(dest)) {
            final long len = dest.length();
            final String sPath = src.getPath();
            if (sPath.startsWith(archiveDir.getPath()))
                archiveDiskUsage -= len;
            else if (sPath.startsWith(filesetDir.getPath()))
                    filesetDiskUsage -= len;

            final String dPath = dest.getPath();
            if (dPath.startsWith(filesetDir.getPath()))
                filesetDiskUsage += len;
            else if (dPath.startsWith(archiveDir.getPath()))
                    archiveDiskUsage += len;
            if (log.isDebugEnabled())
                    log.debug("Success: M-MOVE " + src + " -> " + dest);
            return true;
        }
        log.error("Failed: M-MOVE " + src + " -> " + dest);
        return false;
    }

    static String timeAsString(long ms) {
        if (ms == 0) return "0";
        if (ms % MS_PER_DAY == 0) return "" + (ms / MS_PER_DAY) + 'd';
        if (ms % MS_PER_HOUR == 0) return "" + (ms / MS_PER_HOUR) + 'h';
        if (ms % MS_PER_MINUTE == 0) return "" + (ms / MS_PER_MINUTE) + 'm';
        if (ms % 1000 == 0) return "" + (ms / 1000) + 's';
        return "" + ms + "ms";
    }

    static long timeFromString(String str) {
        String s = str.trim().toLowerCase();
        try {
            if (s.endsWith("d"))
                    return Long.parseLong(s.substring(0, s.length() - 1))
                            * MS_PER_DAY;
            if (s.endsWith("h"))
                    return Long.parseLong(s.substring(0, s.length() - 1))
                            * MS_PER_HOUR;
            if (s.endsWith("m"))
                    return Long.parseLong(s.substring(0, s.length() - 1))
                            * MS_PER_MINUTE;
            if (s.endsWith("s"))
                    return Long.parseLong(s.substring(0, s.length() - 1)) * 1000;
            if (s.endsWith("ms"))
                    return Long.parseLong(s.substring(0, s.length() - 2));
            if (Long.parseLong(s) == 0L) return 0L;
        } catch (NumberFormatException e) {
        }
        throw new IllegalArgumentException(str);
    }
}