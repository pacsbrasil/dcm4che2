/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.jboss.logging.Logger;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 28.06.2004
 */
public class FileUtils {

    
private static final int BUF_SIZE = 512;
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private FileUtils() {
    }

    public static Dataset readDataset(File f, Logger log) throws IOException {
        if (log.isDebugEnabled()) log.debug("M-READ " + f);
        Dataset ds = dof.newDataset();
        try {
            ds.readFile(f, FileFormat.DICOM_FILE, Tags.PixelData);
        } catch (IOException e) {
            log.error("Failed: M-READ " + f, e);
            throw e;
        }
        return ds;
    }

    public static void writeDataset(Dataset ds, File f, Logger log)
            throws IOException {
        if (log.isDebugEnabled()) log.debug("M-UPDATE " + f);
        try {
            ds.writeFile(f, null);
        } catch (IOException e) {
            log.error("Failed M-UPDATE " + f);
            throw e;
        }
    }

    public static boolean delete(File f, Logger log) {
        if (!f.exists()) return false;
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++)
                delete(files[i], log);
        }
        log.debug("M-DELETE " + f);
        boolean success = f.delete();
        if (!success) log.warn("Failed M-DELETE " + f);
        return success;
    }

    public static void purgeDir(File d, Logger log) {
        if (d.isDirectory() && d.list().length == 0) {
            log.debug("M-DELETE " + d);
            if (!d.delete())
                log.warn("Failed M-DELETE " + d);
            else
                purgeDir(d.getParentFile(), log);
        }
    }

    public static File makeMD5File(File f) {
        return new File(f.getParent(), f.getName() + ".MD5");
    }

    public static char[] toHexChars(byte[] bs) {
        char[] cbuf = new char[bs.length * 2];
        toHexChars(bs, cbuf);
        return cbuf;
    }

    private static final char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static void toHexChars(byte[] bs, char[] cbuf) {
        for (int i = 0, j = 0; i < bs.length; i++, j++, j++) {
            cbuf[j] = HEX_DIGIT[(bs[i] >>> 4) & 0xf];
            cbuf[j + 1] = HEX_DIGIT[bs[i] & 0xf];
        }
    }

    public static void md5sum(File fileOrDir, char[] cbuf, MessageDigest digest, byte[] bbuf)
            throws IOException {
        digest.reset();
        InputStream in = new DigestInputStream(
                new FileInputStream(fileOrDir), digest);
        try {
            while (in.read(bbuf) != -1)
                ;
        } finally {
            in.close();
        }
        toHexChars(digest.digest(), cbuf);
    }

    public static boolean verify(File driveDir, File fsDir, Logger log)
            throws IOException {
        File md5sums = new File(driveDir, "MD5_SUMS");
        return md5sums.exists() ? verifyMd5Sums(md5sums, log, new byte[BUF_SIZE])
                : equals(driveDir, fsDir, log, new byte[BUF_SIZE], new byte[BUF_SIZE]);
    }

    private static boolean equals(File dst, File src, Logger log, byte[] srcBuf, byte[] dstBuf)
            throws IOException {
        if (src.isDirectory()) {
            String[] ss = src.list();
            for (int i = 0; i < ss.length; i++) {
            	String s = ss[i];
            	if (s.endsWith(".lnk")) continue; 
                if (!(equals(new File(dst, s), new File(src, s), log, srcBuf, dstBuf)))
                        return false;
            }
        } else {
            if (!dst.isFile()) {
                log.warn("File " + dst + " missing");
                return false;
            }
            log.debug("check " + dst + " = " + src);
            final long srcLen = src.length();
            final long dstLen = dst.length();
            if (dstLen != srcLen) {
                log.warn("File " + dst + " has wrong length");
                return false;
            }
            DataInputStream dstIn = new DataInputStream(new FileInputStream(dst));
            try {
                InputStream srcIn = new FileInputStream(src);
                try {
                    int len;
                    while ((len = srcIn.read(srcBuf)) != -1) {
                        dstIn.readFully(dstBuf, 0, len);
                        if (!equals(dstBuf, srcBuf, len)) {
                            log.warn("File " + dst + " corrupted");
                            return false;
                        }
                    }
                } finally {
                    srcIn.close();
                }
            } finally {
                dstIn.close();
            }
        }
        return true;
    }

    private static boolean equals(byte[] dstBuf, byte[] srcBuf, int len) {
        for (int i = 0; i < len; i++)
            if (dstBuf[i] != srcBuf[i]) return false; 
        return true;
    }

    private static boolean verifyMd5Sums(File md5sums, Logger log, byte[] bbuf)
            throws IOException {
        String base = md5sums.getParentFile().toURI().toString();
        BufferedReader md5sumsIn = new BufferedReader(new FileReader(md5sums));
        try {
            final char[] cbuf = new char[32];
            String line;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            while ((line = md5sumsIn.readLine()) != null) {
                if (line.length() < 33) continue;
                File f = new File(new URI(base + line.substring(32).trim()));
                log.debug("md5sum " + f);
                md5sum(f, cbuf, digest, bbuf);
                if (!Arrays.equals(cbuf, line.substring(0, 32).toCharArray())) {
                    log.warn("File " + f + " corrupted");
                    return false;
                }
            }
        } catch (URISyntaxException e) {
            log.warn("File " + md5sums + " corrupted");
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new ConfigurationException(e);
        } finally {
            md5sumsIn.close();
        }
        return true;
    }
}