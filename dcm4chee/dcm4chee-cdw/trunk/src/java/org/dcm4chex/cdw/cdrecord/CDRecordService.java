/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.dcm4che.data.Dataset;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

    private static final String TAO = "tao"; 
    private static final String DAO = "dao"; 
    private static final String SAO = "sao"; 
    private static final String RAW = "raw"; 
    private static final String RAW96R = "raw96r"; 
    private static final String RAW96P = "raw96p"; 
    private static final String RAW16 = "raw16"; 
    private static final String[] WRITE_MODES = {
            TAO, DAO, SAO, RAW, RAW96R, RAW96P, RAW16
    };
    private static final String DATA = "data"; 
    private static final String MODE2 = "mode2"; 
    private static final String XA = "xa"; 
    private static final String XA1 = "xa1"; 
    private static final String XA2 = "xa2"; 
    private static final String XAMIX = "xamix"; 
    private static final String[] TRACK_TYPES = {
            DATA, MODE2, XA, XA1, XA2, XAMIX
    };
    
    private final File logFile;
    
    private String device = "0,0";

    private int writeSpeed = 24;

    private String writeMode = "tao";

    private String trackType = "data";

    private boolean padding = false;    

    private boolean multiSession = false;    

    private boolean simulate = false;

    private boolean logEnabled = true;

    public CDRecordService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        File logdir = new File(homedir, "log");
        logFile = new File(logdir, "cdrecord.log");
    }

    public final boolean isPadding() {
        return padding;
    }
    
    public final void setPadding(boolean padding) {
        this.padding = padding;
    }
    
    public final boolean isLogEnabled() {
        return logEnabled;
    }

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public final boolean isMultiSession() {
        return multiSession;
    }

    public final void setMultiSession(boolean multiSession) {
        this.multiSession = multiSession;
    }

    public final boolean isSimulate() {
        return simulate;
    }

    public final void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public final String getTrackType() {
        return trackType;
    }

    public final void setTrackType(String trackType) {
        if (Arrays.asList(TRACK_TYPES).indexOf(trackType) == -1)
            throw new IllegalArgumentException("trackType:" + trackType);
        this.trackType = trackType;
    }

    public final String getWriteMode() {
        return writeMode;
    }

    public final void setWriteMode(String writeMode) {
        if (Arrays.asList(WRITE_MODES).indexOf(writeMode) == -1)
            throw new IllegalArgumentException("writeMode:" + writeMode);
        this.writeMode = writeMode;
    }

    public final int getWriteSpeed() {
        return writeSpeed;
    }

    public final void setWriteSpeed(int writeSpeed) {
        if (writeSpeed < 0)
            throw new IllegalArgumentException("writeSpeed:" + writeSpeed);
        this.writeSpeed = writeSpeed;
    }

    public final String getDevice() {
        return device;
    }

    public final void setDevice(String device) {
        this.device = device;
    }

    protected void handle(MediaCreationRequest r, Dataset attrs)
            throws MediaCreationException {
        try {
            cdrecord(r.getIsoImageFile());
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    public void cdrecord(File isoImageFile) throws IOException {
        int exitCode;
        try {
            ArrayList cmd = new ArrayList();
            cmd.add("cdrecord");
            cmd.add("dev=" + device);
            cmd.add("speed=" + writeSpeed);
            if (simulate) cmd.add("-dummy");
            cmd.add("-" + writeMode);
            if (multiSession) cmd.add("-multi");
            if (!logEnabled) cmd.add("-s");
            cmd.add(padding ? "-pad" : "-nopad");
            cmd.add("-" + trackType);
            cmd.add(isoImageFile.getAbsolutePath());
            String[] cmdarray = (String[]) cmd.toArray(new String[cmd.size()]);
            if (log.isDebugEnabled())
                    log.debug("invoke: " + StringUtils.toString(cmdarray, ' '));
            Process pr = Runtime.getRuntime().exec(cmdarray);
            if (logEnabled) {
                BufferedWriter logout = new BufferedWriter(new FileWriter(
                        logFile));
                try {
	                BufferedReader procout = new BufferedReader(new InputStreamReader(
                        pr.getInputStream()));
                String line;
                while ((line = procout.readLine()) != null) {
                    logout.write(line);
                    logout.newLine();
                }            
                } finally {
                	try { logout.close(); } catch (IOException ignore) {}
                }
            }
            exitCode = pr.waitFor();
            if (log.isDebugEnabled())
                    log.debug("finished: " + StringUtils.toString(cmdarray, ' '));
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        if (exitCode != 0) { throw new IOException("cdrecord " + isoImageFile
                + " returns " + exitCode); }
    }

}
