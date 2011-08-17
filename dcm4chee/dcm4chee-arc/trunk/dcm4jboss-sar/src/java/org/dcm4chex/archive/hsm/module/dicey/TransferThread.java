package org.dcm4chex.archive.hsm.module.dicey;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class TransferThread extends Thread {
    public File source;
    public File destination;
    private long lengthInBytes;
    public long chunckSizeInBytes;
    public boolean verbose;
    volatile FileChannel outputChannel;
    volatile FileChannel inputChannel;
    volatile boolean stop = false;

    private static final Logger log = Logger.getLogger(TransferThread.class);

    public TransferThread(File inFile, File outFile, int i) {
        this.source = inFile;
        this.destination = outFile;
        this.chunckSizeInBytes = i;
    }

    public void run() {
        log.debug("Opening streams");
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        this.lengthInBytes = source.length();
        try {
            try {
                fileInputStream = new FileInputStream(source);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new IOException("Opening input Stream Failed");
            }
            try {
                fileOutputStream = new FileOutputStream(destination);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                throw new IOException("Opening output Stream Failed");
            }
            log.debug("Opening channels");
            inputChannel = fileInputStream.getChannel();
            outputChannel = fileOutputStream.getChannel();
            long overallBytesTransfered = 0L;

            try {
                while ( stop == false ) {
                    long bytesToTransfer = Math.min(chunckSizeInBytes,
                            lengthInBytes - overallBytesTransfered);
                    long bytesTransfered = 0;
                    log.debug("Transfer bytes: " + bytesToTransfer);
                    try {
                        bytesTransfered = inputChannel.transferTo(
                                overallBytesTransfered, bytesToTransfer,
                                outputChannel);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IOException("Copy of chunks Failed");
                    }
                    overallBytesTransfered += bytesTransfered;
                    if (overallBytesTransfered == lengthInBytes) stop=true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Copy Failed");

            } finally {
                fileInputStream.close();
                fileOutputStream.close();
                inputChannel.close();
                outputChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            interrupt();
        }
    }
}
