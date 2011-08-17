package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.hsm.module.dicey.FileIOTimeOut;
import org.dcm4chex.archive.hsm.module.dicey.TransferThread;

public class FileIOTimeOut {
    private static final Logger log = Logger.getLogger(FileIOTimeOut.class);
    private static final int KB = 1024;
    private static final int MB = KB * KB;

    public static void copy(File source, File destination, int timeOut)
    throws IOException {
        StringBuilder logoutput = new StringBuilder();

        try {
            long t1 = System.currentTimeMillis();

            TransferThread ioThread = new TransferThread(source, destination, MB * 4);			
            ioThread.start();
            log.debug("Thread started");
            int MAX_SECONDS = timeOut; // Max number of seconds
            int counter = 0; // current second
            long totalSize = 0L;
            totalSize = destination.length();


            while ( ! ioThread.stop || ioThread.getState() != Thread.State.TERMINATED ) { // if not
                // already
                // terminated
                log.debug("Timeout seconds: " + counter);
                if (counter >= MAX_SECONDS) {
                    // Output filesize did not change for n seconds, therefore
                    // interrupt thread
                    log.debug("TimeOut reached");	
                    ioThread.inputChannel.close();
                    ioThread.outputChannel.close();
                    ioThread.stop = true;
                    ioThread.interrupt();				

                    throw new IOException("TimeOut reached");
                } else {
                    try {
                        Thread.sleep(1000);
                        if (destination.length() > totalSize) {
                            totalSize = destination.length();
                            counter = 0;
                            log.debug("Size:" + totalSize);
                        } else
                            log.debug("Size not changed:" + totalSize);
                    } catch (InterruptedException e) {
                        throw new IOException("Thread Aborted");
                    }
                }
                counter++;
            }

            long t2 = System.currentTimeMillis();
            logoutput.append("Copied ").append(source).append(" -> ").append(destination).append(' ');
            totalSize = destination.length();
            if (totalSize > MB) {
                logoutput.append(totalSize / MB).append("MiB");
            } else {
                logoutput.append(totalSize / KB).append("KiB");
            }
            logoutput.append(" in ").append((t2 - t1) / 1000f).append(" seconds")
            .append(" [").append((totalSize / (float)MB ) / ((t2 - t1) / 1000f)).append("MiB/s]");
            log.info(logoutput.toString());
        } catch (Exception e) {
            throw new IOException("FileCopy Not successfull",e);
        }
    }

}
