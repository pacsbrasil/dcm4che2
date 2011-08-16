package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.hsm.module.dicey.FileIOTimeOut;

public class FileIOTimeOut {
    private static final Logger log = Logger.getLogger(FileIOTimeOut.class);

    public static void copy(File source, File destination, int timeOut)
    throws IOException {
        int KB = 1024;
        int MB = KB * KB;
        String logoutput = "";

        try {
            long t1 = System.currentTimeMillis();

            TransferThread ioThread;
            ioThread = new TransferThread(source, destination, source.length(),
                    1024 * 1024 * 4 /* 4 MB chunks */);			
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
            logoutput = "Copied " + source + " -> " + destination + " ";
            totalSize = destination.length();
            if (totalSize > MB) {
                logoutput = logoutput + (totalSize / MB);
                logoutput = logoutput + ("MiB");
            } else {
                logoutput = logoutput + (totalSize / KB);
                logoutput = logoutput + ("KiB");
            }
            logoutput = logoutput
            + (" in " + Float.valueOf((t2 - t1) / 1000f) + " seconds");
            logoutput = logoutput
            + (" [" + Float.valueOf(( totalSize / 1024f / 1024f ) / (( t2 - t1  ) / 1000f) ) + "MiB/s]");
            log.info(logoutput);

        } catch (Exception e) {
            throw new IOException("FileCopy Not successfull",e);
        }
    }

}
