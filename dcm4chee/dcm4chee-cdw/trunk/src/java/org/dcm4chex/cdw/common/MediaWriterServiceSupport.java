/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.jms.JMSException;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.Executer;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 29.10.2004
 *
 */
public abstract class MediaWriterServiceSupport extends
        AbstractMediaWriterService {

    static final int MIN_RETRY_INTERVAL = 10;

    protected String executable;

    protected String burncmd;

    protected String drivePath;

    protected File driveDir;

    protected int writeSpeed = -1;

    protected boolean multiSession = false;

    protected boolean appendEnabled = false;

    protected boolean simulate = false;

    protected boolean eject = true;

    protected boolean autoLoad = false;

    protected int numberOfRetries = 0;

    protected int retryInterval = 60;

    protected boolean verify = false;

    protected boolean mount = false;

    protected boolean logEnabled = false;

    protected int mountTime = 10;

    protected int pauseTime = 10;

    protected File logFile;

    private final LabelPrintDelegate labelPrint = new LabelPrintDelegate(this);

    public final ObjectName getLabelPrintName() {
        return labelPrint.getLabelPrintName();
    }

    public final void setLabelPrintName(ObjectName labelPrintName) {
        labelPrint.setLabelPrintName(labelPrintName);
    }

    public final int getNumberOfRetries() {
        return numberOfRetries;
    }

    public final void setNumberOfRetries(int numberOfRetries) {
        if (numberOfRetries < 0)
                throw new IllegalArgumentException("numberOfRetries:"
                        + numberOfRetries);
        this.numberOfRetries = numberOfRetries;
    }

    public final int getRetryInterval() {
        return retryInterval;
    }

    public final void setRetryInterval(int retryInterval) {
        if (retryInterval < MIN_RETRY_INTERVAL)
                throw new IllegalArgumentException("retryInterval:"
                        + retryInterval);
        this.retryInterval = retryInterval;
    }

    public final boolean isVerify() {
        return verify;
    }

    public final void setVerify(boolean verify) {
        this.verify = verify;
    }

    public String getKeepLabelFiles() {
        return labelPrint.getKeepLabelFiles();
    }

    public void setKeepLabelFiles(String s) {
        labelPrint.setKeepLabelFiles(s);
    }

    public boolean isPrintLabel() {
        return labelPrint.isPrintLabel();
    }

    public void setPrintLabel(boolean printLabel) {
        labelPrint.setPrintLabel(printLabel);
    }

    public final int getMountTime() {
        return mountTime;
    }

    public final void setMountTime(int mountTime) {
        this.mountTime = mountTime;
    }

    public final int getPauseTime() {
        return pauseTime;
    }

    public final void setPauseTime(int pauseTime) {
        if (pauseTime < 0)
                throw new IllegalArgumentException("pauseTime: " + pauseTime);
        this.pauseTime = pauseTime;
    }

    public final boolean isAppendEnabled() {
        return appendEnabled;
    }

    public final void setAppendEnabled(boolean appendEnabled) {
        this.appendEnabled = appendEnabled;
    }

    public final boolean isSimulate() {
        return simulate;
    }

    public final void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public final boolean isEject() {
        return eject;
    }

    public final void setEject(boolean eject) {
        this.eject = eject;
    }

    public final boolean isAutoLoad() {
        return autoLoad;
    }

    public final void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    public final String getExecutable() {
        return executable;
    }

    public final void setExecutable(String command) {
        this.executable = command;
        int begin = command.lastIndexOf(File.separatorChar);
        int end = command.lastIndexOf('.');
        this.burncmd = (end == -1) ? command.substring(begin + 1) : command
                .substring(begin + 1, end);
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        this.logFile = new File(homedir, "log" + File.separatorChar + burncmd
                + ".log");
    }

    public final String getDriveLetterOrMountDirectory() {
        return drivePath;
    }

    public void setDriveLetterOrMountDirectory(String drivePath) {
        boolean mount = drivePath.charAt(0) == '/';
        if (!mount && drivePath.charAt(1) != ':')
                throw new IllegalArgumentException("drivePath: " + drivePath);
        this.drivePath = drivePath;
        this.driveDir = new File(drivePath);
        this.mount = mount;
    }

    public final int getWriteSpeed() {
        return writeSpeed;
    }

    public final void setWriteSpeed(int writeSpeed) {
        if (writeSpeed < -1)
                throw new IllegalArgumentException("writeSpeed:" + writeSpeed);
        this.writeSpeed = writeSpeed;
    }

    public final boolean isMultiSession() {
        return multiSession;
    }

    public final void setMultiSession(boolean multiSession) {
        this.multiSession = multiSession;
    }

    public final boolean isLogEnabled() {
        return logEnabled;
    }

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public abstract boolean checkDrive() throws MediaCreationException;

    public abstract boolean checkDisk() throws MediaCreationException;

    public abstract boolean hasTOC() throws MediaCreationException;

    protected abstract String[] makeBurnCmd(File isoImageFile);

    protected abstract String[] makeLoadCmd();

    protected abstract String[] makeEjectCmd();

    protected boolean handle(MediaCreationRequest rq, Dataset attrs)
            throws MediaCreationException, IOException {
        if (!checkDrive())
                throw new MediaCreationException(
                        ExecutionStatusInfo.CHECK_MCD_SRV, "Drive Check failed");
        while (rq.getRemainingCopies() > 0) {
            if (autoLoad) load();
            if (!checkDisk()) {
                eject();
                retry(rq, attrs, "No or Wrong Media");
                return false;
            }
            if (!appendEnabled && hasTOC()) {
                eject();
                retry(rq, attrs, "Media not empty");
                return false;
            }
            try {
                log.info("Burning " + rq);
                burn(rq.getIsoImageFile());
                log.info("Burned " + rq);
                if (verify) {
                    load();
                    if (mountTime > 0) {
                        try {
                            Thread.sleep(mountTime * 1000L);
                        } catch (InterruptedException e) {
                            log.warn("Mount Time was interrupted:", e);
                        }
                    }
                    log.info("Verifying " + rq);
                    verify(rq.getFilesetDir());
                    log.info("Verified " + rq);
                    if (eject) eject();
                }
            } catch (MediaCreationException e) {
                eject();
                throw e;
            }

            labelPrint.print(rq);

            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return true;
            }
            rq.copyDone(attrs, log);
            try {
                Thread.sleep(pauseTime * 1000L);
            } catch (InterruptedException e) {
                log.warn("Pause before next burn was interrupted:", e);
            }
        }
        log.info("Finished Creating Media for " + rq);
        return true;
    }

    private void retry(MediaCreationRequest rq, Dataset attrs, String msg)
            throws MediaCreationException, IOException {
        if (rq.getRetries() >= numberOfRetries)
                throw new MediaCreationException(
                        ExecutionStatusInfo.OUT_OF_SUPPLIES, msg);
        log.warn(msg + " - retry in " + retryInterval + "s.");
        rq.setRetries(rq.getRetries() + 1);
        attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
        attrs.putCS(Tags.ExecutionStatusInfo,
                ExecutionStatusInfo.OUT_OF_SUPPLIES);
        rq.writeAttributes(attrs, log);
        try {
            JMSDelegate.queue(rq.getMediaWriterName(),
                    "Schedule Writing Media for " + rq,
                    log,
                    rq,
                    System.currentTimeMillis() + retryInterval * 1000L);
        } catch (JMSException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

    private void verify(File fsDir) throws MediaCreationException {
        try {
            if (!MD5Utils.verify(driveDir, fsDir, log)) { throw new MediaCreationException(
                    ExecutionStatusInfo.PROC_FAILURE, "Verification failed!"); }
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    "Verification failed:", e);
        }
    }

    public void burn(File isoImageFile) throws MediaCreationException {
        int exitCode;
        OutputStream logout = null;
        try {
            String[] cmdarray = makeBurnCmd(isoImageFile);
            if (logEnabled)
                    logout = new BufferedOutputStream(new FileOutputStream(
                            logFile));
            exitCode = new Executer(cmdarray, logout, null).waitFor();
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } finally {
            if (logout != null) try {
                logout.close();
            } catch (IOException ignore) {
            }
        }
        if (exitCode != 0) { throw new MediaCreationException(
                ExecutionStatusInfo.MCD_FAILURE, burncmd + " " + isoImageFile
                        + " returns " + exitCode); }
    }

    public void load() {
        exec(makeLoadCmd(), "Failed to load Media:");
    }

    public void eject() {
        exec(makeEjectCmd(), "Failed to eject Media:");
    }

    public void mount() throws MediaCreationException {
        String[] cmdArray = { "mount", drivePath};
        int exit = 0;
        try {
            exit = new Executer(cmdArray).waitFor();
        } catch (Exception e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    "mount " + drivePath + " failed:", e);
        }
        if (exit != 0)
                throw new MediaCreationException(
                        ExecutionStatusInfo.PROC_FAILURE, "mount " + drivePath
                                + " failed: exit(" + exit + ")");
    }

    public boolean umount() {
        String[] cmdArray = { "umount", "-l", drivePath};
        String warning = "Failed to unmount " + drivePath + ":";
        return exec(cmdArray, warning);
    }

    private boolean exec(String[] cmd, String warning) {
        try {
            int exit = new Executer(cmd, null, null).waitFor();
            if (exit == 0) return true;
            log.warn(warning + " exit(" + exit + ")");
        } catch (Exception e) {
            log.warn(warning, e);
        }
        return false;
    }
    
    protected boolean check(String[] cmdarray, String match)
            throws MediaCreationException {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Executer ex = new Executer(cmdarray, stdout, null);
            int exit = ex.waitFor();
            String result = stdout.toString();
            if (log.isDebugEnabled())
                    log.debug(burncmd + " stdout: " + result);
            return exit == 0 && result.indexOf(match) != -1;
        } catch (InterruptedException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE,
                    e);
        }
    }

}