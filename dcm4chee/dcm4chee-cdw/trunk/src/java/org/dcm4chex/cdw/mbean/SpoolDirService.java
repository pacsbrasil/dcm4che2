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

    private static final long MS_PER_MINUTE = 60000L;

    private static final long MS_PER_HOUR = MS_PER_MINUTE * 60;

    private static final long MS_PER_DAY = MS_PER_HOUR * 24;

    private static final String ARCHIVE = "archive";

    private static final String REQUEST = "request";

    private static final String MEDIA = "media";

    private String directoryPath;

    private File dir;

    private long purgeMediaCreationRequestsAfter = MS_PER_DAY;

    private long purgePreservedInstancesAfter = MS_PER_DAY;

    private long purgeTemporaryFilesAfter = MS_PER_HOUR;

    private int numberOfArchiveBuckets = 37;

    public final String getDirectoryPath() {
        return directoryPath;
    }

    public final void setDirectoryPath(String directoryPath) {
        File d = new File(directoryPath);
        if (!d.isAbsolute()) {
            File dataDir = ServerConfigLocator.locate().getServerHomeDir();
            d = new File(dataDir, directoryPath);
        }
        init(d, ARCHIVE);
        init(d, REQUEST);
        init(d, MEDIA);
        this.dir = d;
        this.directoryPath = directoryPath;
    }

    private void init(File d, String sub) {
        File dir = new File(d, sub);
        if (dir.mkdirs()) log.warn("M-WRITE " + dir);
        if (!dir.isDirectory() || !dir.canWrite())
                throw new IllegalArgumentException("" + dir
                        + " is not a writable directory!");
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
        File bucket = new File(new File(dir, ARCHIVE), String.valueOf(i));
        if (bucket.mkdirs()) log.debug("M-WRITE " + bucket);
        return new File(bucket, iuid);
    }

    public File getMediaCreationRequestFile(String iuid) {
        return new File(new File(dir, REQUEST), iuid);
    }

    public File getMediaFilesetRootDir(String iuid) {
        return new File(new File(dir, MEDIA), iuid);
    }

    public void purge() {
        purgeExpiredMediaCreationRequests();
        purgeExpiredPreservedInstances();
        purgeResidualTemporaryFiles();
    }

    public void purgeExpiredMediaCreationRequests() {
        if (purgePreservedInstancesAfter == 0) return;
        deleteFilesModifiedBefore(new File(dir, REQUEST), System
                .currentTimeMillis()
                - purgeMediaCreationRequestsAfter);
    }

    public void purgeExpiredPreservedInstances() {
        if (purgePreservedInstancesAfter == 0) return;
        File subdir = new File(dir, ARCHIVE);
        String[] ss = subdir.list();
        if (ss == null) return;
        final long modifiedBefore = System.currentTimeMillis()
                - purgePreservedInstancesAfter;
        for (int i = 0; i < ss.length; i++)
            deleteFilesModifiedBefore(new File(subdir, ss[i]), modifiedBefore);
    }

    public void purgeResidualTemporaryFiles() {
        if (purgePreservedInstancesAfter == 0) return;
        deleteFilesModifiedBefore(new File(dir, MEDIA), System
                .currentTimeMillis()
                - purgeTemporaryFilesAfter);
    }

    private void deleteFilesModifiedBefore(File subdir, long modifiedBefore) {
        String[] ss = subdir.list();
        if (ss == null) return;
        for (int i = 0, n = ss.length; i < n; i++) {
            File f = new File(subdir, ss[i]);
            if (f.lastModified() <= modifiedBefore) {
                FileUtils.delete(f, log);
            }
        }
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