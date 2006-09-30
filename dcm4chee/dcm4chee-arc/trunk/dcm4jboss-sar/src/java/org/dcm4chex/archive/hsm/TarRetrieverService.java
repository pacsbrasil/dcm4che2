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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.commons.compress.tar.TarEntry;
import org.apache.commons.compress.tar.TarInputStream;
import org.dcm4che.util.MD5Utils;
import org.dcm4chex.archive.util.FileSystemUtils;
import org.dcm4chex.archive.util.FileUtils;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Mar 13, 2006
 */
public class TarRetrieverService extends ServiceMBeanSupport {

    private static final int MIN_LRUCACHE_SIZE = 10;

    private static final Comparator DIR_INFO_CMP = new Comparator() {
        public int compare(Object o1, Object o2) {
            long l = ((File) o1).lastModified() - ((File) o2).lastModified();
            return l < 0 ? -1 : l > 0 ? 1 : 0;
        }
    };

    private File cacheRoot;

    private File absCacheRoot;

    private long minFreeDiskSpace;

    private long prefFreeDiskSpace;

    private int bufferSize = 8192;

    private int lruCacheSize = 20;

    private ArrayList lruDirs = new ArrayList(lruCacheSize);

    private boolean checkMD5 = true;

    public boolean isCheckMD5() {
        return checkMD5;
    }

    public void setCheckMD5(boolean checkMD5) {
        this.checkMD5 = checkMD5;
    }

    public final int getLRUCacheSize() {
        return lruCacheSize;
    }

    public final void setLRUCacheSize(int cacheSize) {
        if (cacheSize < MIN_LRUCACHE_SIZE)
            throw new IllegalArgumentException("chacheSize: " + cacheSize);
        this.lruCacheSize = cacheSize;
        int size = lruDirs.size();
        while (size > cacheSize)
            lruDirs.remove(--size);
    }

    public final int getBufferSize() {
        return bufferSize;
    }

    public final void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public final String getCacheRoot() {
        return cacheRoot.getPath();
    }

    public final void setCacheRoot(String cacheRoot) {
        this.cacheRoot = new File(cacheRoot);
        this.absCacheRoot = FileUtils.resolve(this.cacheRoot);
    }

    public final String getMinFreeDiskSpace() {
        return FileUtils.formatSize(minFreeDiskSpace);
    }

    public final void setMinFreeDiskSpace(String s) {
        this.minFreeDiskSpace = FileUtils.parseSize(s, 0);
    }

    public final String getPreferredFreeDiskSpace() {
        return FileUtils.formatSize(prefFreeDiskSpace);
    }

    public final void setPreferredFreeDiskSpace(String s) {
        this.prefFreeDiskSpace = FileUtils.parseSize(s, 0);
    }

    public File retrieveFileFromTAR(String taruri) throws IOException,
            VerifyTarException {
        if (!taruri.startsWith("tar:")) {
            throw new IllegalArgumentException("Not a tar URI: " + taruri);
        }
        int tarEnd = taruri.indexOf('!');
        if (tarEnd == -1)
            throw new IllegalArgumentException("Missing ! in " + taruri);
        int dirEnd = taruri.lastIndexOf('/', tarEnd);
        if (dirEnd == -1)
            throw new IllegalArgumentException("Missing / before ! in " + taruri);
        String dpath = taruri.substring(4, dirEnd).replace('/',
                File.separatorChar);
        File cacheDir = new File(absCacheRoot, dpath);
        String fpath = taruri.substring(tarEnd + 1).replace('/',
                File.separatorChar);
        File f = new File(cacheDir, fpath);
        if (!f.exists()) {
            fetchTar(taruri.substring(4, tarEnd), cacheDir);
        }
        File p = f.getParentFile();
        if (lruDirs.remove(p))
            if (log.isDebugEnabled())
                log.debug("Remove from list of LRU directories: " + p);
        p.setLastModified(System.currentTimeMillis());
        return f;
    }

    private void fetchTar(String path, File cacheDir)
            throws IOException, VerifyTarException {
        if (absCacheRoot.mkdirs()) {
            log.warn("M-WRITE " + absCacheRoot);
        }

        int count = 0;
        long totalSize = 0;
        long free = FileSystemUtils.freeSpace(absCacheRoot.getPath());
        File tarFile = FileUtils.toFile(path);
        long fsize = tarFile.length();
        long toDelete = fsize + minFreeDiskSpace - free;
        if (toDelete > 0)
            free += free(toDelete);
        byte[] buf = new byte[bufferSize];
        TarInputStream tar = new TarInputStream(new FileInputStream(tarFile));
        InputStream in = tar;
        try {
            TarEntry entry = tar.getNextEntry();
            if (entry == null)
                throw new IOException("No entries in " + path);
            String entryName = entry.getName();
            if (!"MD5SUM".equals(entryName))
                throw new IOException("Missing MD5SUM entry in " + path);

            MessageDigest digest = null;
            HashMap md5sums = new HashMap();
            if (checkMD5) {
                BufferedReader lineReader = new BufferedReader(
                        new InputStreamReader(tar));
                String line;
                while ((line = lineReader.readLine()) != null) {
                    md5sums.put(line.substring(34), 
                            MD5Utils.toBytes(line.substring(0, 32)));
                }
                try {
                    digest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

            while ((entry = tar.getNextEntry()) != null) {
                entryName = entry.getName();
                // Retrieve saved MD5 checksum
                byte[] md5sum = null;
                if (checkMD5) {
                    md5sum = (byte[]) md5sums.remove(entryName);
                    if (md5sum == null)
                        throw new VerifyTarException("Unexpected TAR entry: "
                                + entryName + " in " + path);
                    digest.reset();
                    in = new DigestInputStream(tar, digest);
                }

                File f = new File(cacheDir, 
                        entryName.replace('/', File.separatorChar));
                File dir = f.getParentFile();
                if (dir.mkdirs()) {
                    log.info("M-WRITE " + dir);
                }

                // Write the stream to file
                FileOutputStream out = new FileOutputStream(f);
                boolean cleanup = true;
                try {
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    cleanup = false;
                } finally {
                    try {
                        out.close();
                    } catch (Exception ignore) {
                    }
                    ;
                    if (cleanup)
                        f.delete();
                }

                // Verify MD5
                if (checkMD5) {
                    if (!Arrays.equals(digest.digest(), md5sum)) {
                        f.delete();
                        throw new VerifyTarException(
                                "Failed MD5 check of TAR entry: " + entryName
                                        + " in " + path);
                    } else
                        log.info("MD5 check is successful for " + entryName
                                + " in " + path);
                }

                log.info("M-WRITE " + f);
                free -= f.length();
                count++;
                totalSize += f.length();
            }
        } finally {
            tar.close();
        }
        toDelete = prefFreeDiskSpace - free;
        if (toDelete > 0) {
            freeNonBlocking(toDelete);
        }
    }

    private void freeNonBlocking(final long toDelete) {
        new Thread(new Runnable() {

            public void run() {
                free(toDelete);
            }
        }).start();
    }

    public long free(long size) {
        log.info("Start deleting LRU directories of at least " + size
                + " bytes from TAR cache");
        long deleted = 0;
        while (deleted < size) {
            if (lruDirs.isEmpty()) {
                log.debug("Start scanning cache for building list of LRU directories");
                scanDirs(absCacheRoot);
                log.debug("Finished scanning cache for building list of LRU directories");
            }
            File d = (File) lruDirs.remove(0);
            if (log.isDebugEnabled()) {
                log.debug("Remove from list of LRU directories: " + d);
            }
            deleted += deleteDir(d);
        }
        log.info("Finished deleting LRU directories with " + deleted
                + " bytes from TAR cache");
        return deleted;
    }

    private long deleteDir(File d) {
        long deleted = 0;
        String[] ss = d.list();
        for (int i = 0; i < ss.length; i++) {
            File f = new File(d, ss[i]);
            deleted += f.length();
            f.delete();
            log.info("M-DELETE " + f);
        }
        File p = d.getParentFile();
        while (d.delete()) {
            log.info("M-DELETE " + d);
            d = p;
            p = d.getParentFile();
        }
        return deleted;
    }

    private void scanDirs(File d) {
        String[] ss = d.list();
        if (ss != null) {
            for (int i = 0; i < ss.length; i++) {
                File f = new File(d, ss[i]);
                if (f.isDirectory()) {
                    scanDirs(f);
                } else {
                    addLRUDir(d);
                    return;
                }
            }
        }
    }

    private void addLRUDir(File d) {
        int size = lruDirs.size();
        int index = 0;
        if (size > 0) {
            if (size >= lruCacheSize) {
                File last = (File) lruDirs.get(size - 1);
                if (last.lastModified() < d.lastModified())
                    return;
                lruDirs.remove(size - 1);
                if (log.isDebugEnabled())
                    log.debug("Remove from list of LRU directories: " + last);
            }
            index = Collections.binarySearch(lruDirs, d, DIR_INFO_CMP);
            if (index < 0) {
                index = -(index + 1);
            }
        }
        if (log.isDebugEnabled())
            log.debug("Insert to list of LRU directories at position [" + index
                    + "]: " + d);
        lruDirs.add(index, d);
    }

}
