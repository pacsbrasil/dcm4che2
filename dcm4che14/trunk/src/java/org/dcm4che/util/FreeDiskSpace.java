/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4che.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 26.11.2004
 */
public abstract class FreeDiskSpace {

    private static final long KB = 1024L;
    private static FreeDiskSpace impl;
    static {
        final String os = System.getProperty("os.name");
        // TODO implementation for other OS
        impl = new ExecuteDF();
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: FreeDiskSpace DIR[...]");
            return;
        }
        try {        
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i] + " - "
                        + getFreeDiskSpace(new File(args[i])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static long getFreeDiskSpace(File f) throws IOException  {
        if (impl == null)
            throw new UnsupportedOperationException();
        return impl.getFreeDiskSpaceInternal(f);
    }
    
    protected abstract long getFreeDiskSpaceInternal(File f) throws IOException;


    private static int indexOfLastDigit(byte[] buf, int fromIndex) {
        for (int i = fromIndex; i >= 0; --i)
            if (buf[i] >= 48 && buf[i] <= 57)
                return i;
        return -1;
    }

    private static int indexOfLastNonDigit(byte[] buf, int fromIndex) {
        for (int i = fromIndex; i >= 0; --i)
            if (buf[i] < 48 || buf[i] > 57)
                return i;
        return -1;
    }
    
    private static class MyByteArrayOutputStream extends ByteArrayOutputStream {
        public final byte[] exposeBuffer() {
            return buf;
        }
    }
    
    private static final class ExecuteDF extends FreeDiskSpace {

        protected long getFreeDiskSpaceInternal(File f) throws IOException {
            String[] cmd = { "df", "-Pk", f.getAbsolutePath() };
            MyByteArrayOutputStream stdout = new MyByteArrayOutputStream();            
            Executer ex = new Executer(cmd, stdout, null);
            try {
                int exit = ex.waitFor();
                if (exit != 0)
                    throw new IOException("df returns with exit code: " + exit);
            } catch (InterruptedException e) {
                throw new IOException("waiting for return of df interrupted: " + e);
            }
            final byte[] buf = stdout.exposeBuffer();
            
            int r = indexOfLastDigit(buf, buf.length - 1);
            r = indexOfLastNonDigit(buf, r - 1);
            r = indexOfLastDigit(buf, r - 1);
            int l = indexOfLastNonDigit(buf, r - 1);
            try {
                return Long.parseLong(new String(buf, l + 1, r - l)) * KB;
            } catch (Exception e) {
                throw new IOException("failed to parse df result: "
                        + stdout.toString());
            }
        }
    }
}
