/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

import java.io.File;

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

    private static final String CACHE = "cache";

    private static final String REQUEST = "request";

    private static final String MEDIA = "media";

    private String directoryPath;

    private File dir;

    private long deleteMediaCreationRequestsAfter = MS_PER_DAY;

    private long deletePreservedInstancesAfter = MS_PER_DAY;

    public final String getDirectoryPath() {
        return directoryPath;
    }

    public final void setDirectoryPath(String directoryPath) {
        File d = new File(directoryPath);
        if (!d.isAbsolute()) {
            File dataDir = ServerConfigLocator.locate().getServerDataDir();
            d = new File(dataDir, directoryPath);
        }
        init(d, CACHE);
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

    public final String getDeletePreservedInstancesAfter() {
        return timeAsString(deletePreservedInstancesAfter);
    }

    public final void setDeletePreservedInstancesAfter(String s) {
        this.deletePreservedInstancesAfter = timeFromString(s);
    }

    public final String getDeleteMediaCreationRequestsAfter() {
        return timeAsString(deleteMediaCreationRequestsAfter);
    }

    public final void setDeleteMediaCreationRequestsAfter(String s) {
        this.deleteMediaCreationRequestsAfter = timeFromString(s);
    }

    public File getInstanceFile(String iuid) {
        return new File(new File(dir, CACHE), iuid);
    }

    public File getMediaCreationRequestFile(String iuid) {
        return new File(new File(dir, REQUEST), iuid);
    }

    public File getMediaLayoutsRoot(String iuid) {
        return new File(new File(dir, MEDIA), iuid);
    }

    public void deleteExpiredFiles() {
        deleteExpiredMediaCreationRequests();
        deleteExpiredPreservedInstances();
    }

    public void deleteExpiredMediaCreationRequests() {
        if (deleteMediaCreationRequestsAfter == 0) return;
        long modifiedBefore = System.currentTimeMillis()
                - deleteMediaCreationRequestsAfter;
        delete(new File(dir, REQUEST), modifiedBefore, true);
    }

    public void deleteExpiredPreservedInstances() {
        if (deletePreservedInstancesAfter == 0) return;
        long modifiedBefore = System.currentTimeMillis() - deletePreservedInstancesAfter;
        delete(new File(dir, MEDIA), modifiedBefore, true);
        delete(new File(dir, CACHE), modifiedBefore, true);
    }

    private boolean delete(File fileOrDirectory, long modifiedBefore,
            boolean keepRoot) {
        if (!fileOrDirectory.exists()) return false;
        if (fileOrDirectory.isDirectory()) {
            boolean emptyDir = true;
            File[] files = fileOrDirectory.listFiles();
            for (int i = 0; i < files.length; i++)
                emptyDir = delete(files[i], modifiedBefore, false) && emptyDir;
            if (keepRoot || !emptyDir) return false;
        }
        if (fileOrDirectory.lastModified() > modifiedBefore) return false;
        String prompt = "M-DELETE " + fileOrDirectory;
        log.info(prompt);
        boolean success = fileOrDirectory.delete();
        if (!success) log.error("Failed to " + prompt);
        return success;
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
