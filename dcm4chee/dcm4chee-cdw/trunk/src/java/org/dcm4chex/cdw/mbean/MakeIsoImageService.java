/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 25.06.2004
 *
 */
public class MakeIsoImageService extends ServiceMBeanSupport {

    private static final String _ISO = ".iso";

    private static final String _SORT = ".sort";

    private int isoLevel = 1;

    private boolean rockRidge = false;

    private boolean joliet = false;

    private boolean volsetInfoEnabled = false;

    private boolean logEnabled = false;

    private final File logFile;

    private boolean sortEnabled = false;

    private final File sortFile;

    private final MessageListener listener = new MessageListener() {

        public void onMessage(Message msg) {
            ObjectMessage objmsg = (ObjectMessage) msg;
            try {
                MakeIsoImageService.this.process((MediaCreationRequest) objmsg
                        .getObject());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    public MakeIsoImageService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        File confdir = new File(homedir, "conf");
        sortFile = new File(confdir, "mkisofs.sort");
        File logdir = new File(homedir, "log");
        logFile = new File(logdir, "mkisofs.log");
    }

    public final boolean isVolsetInfoEnabled() {
        return volsetInfoEnabled;
    }

    public final void setVolsetInfoEnabled(boolean volsetInfoEnabled) {
        this.volsetInfoEnabled = volsetInfoEnabled;
    }

    public final int getIsoLevel() {
        return isoLevel;
    }

    public final void setIsoLevel(int isoLevel) {
        this.isoLevel = isoLevel;
    }

    public final boolean isJoliet() {
        return joliet;
    }

    public final void setJoliet(boolean joliet) {
        this.joliet = joliet;
    }

    public final boolean isLogEnabled() {
        return logEnabled;
    }

    public final void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public final boolean isRockRidge() {
        return rockRidge;
    }

    public final void setRockRidge(boolean rockRidge) {
        this.rockRidge = rockRidge;
    }

    public final boolean isSortEnabled() {
        return sortEnabled;
    }

    public final void setSortEnabled(boolean sortEnabled) {
        this.sortEnabled = sortEnabled;
    }

    public void makeIsoImage(File srcDir, File isoImageFile, String volId,
            String volsetId, int volsetSeqno, int volsetSize)
            throws IOException {
        File tmpSortFile = null;
        ArrayList cmd = new ArrayList();
        cmd.add("mkisofs");
        cmd.add("-f"); // follow symbolic links
        cmd.add("-iso-level");
        cmd.add(String.valueOf(isoLevel));
        if (rockRidge) cmd.add("-r");
        if (joliet) cmd.add("-J");
        if (volId != null && volId.length() != 0) {
            cmd.add("-V");
            cmd.add(volId);
        }
        if (volsetInfoEnabled) {
            if (volsetId.length() != 0) {
                cmd.add("-volset");
                cmd.add(volsetId);
            }
            cmd.add("-volset-seqno");
            cmd.add(String.valueOf(volsetSeqno));
            cmd.add("-volset-size");
            cmd.add(String.valueOf(volsetSize));
        }
        if (logEnabled) {
            cmd.add("-log-file");
            cmd.add(logFile.getAbsolutePath());
        } else {
            cmd.add("-q");
        }
        if (sortEnabled) {
            tmpSortFile = makeSortFile(srcDir);
            cmd.add("-sort");
            cmd.add(tmpSortFile.getAbsolutePath());
        }
        cmd.add("-o");
        cmd.add(isoImageFile.getAbsolutePath());
        cmd.add(srcDir.getAbsolutePath());
        String[] a = (String[]) cmd.toArray(new String[cmd.size()]);
        if (log.isDebugEnabled())
                log.debug("invoke: " + StringUtils.toString(a, ' '));
        int exitCode;
        try {
            Process p = Runtime.getRuntime().exec(a);
            exitCode = p.waitFor();
            if (log.isDebugEnabled())
                    log.debug("finished: " + StringUtils.toString(a, ' '));
        } catch (Exception e) {
            throw new IOException("mkisofs " + srcDir + " failed:" + e);
        } finally {
            if (tmpSortFile != null) tmpSortFile.delete();
        }
        if (exitCode != 0) { throw new IOException("mkisofs " + srcDir
                + " returns " + exitCode); }
    }

    private File makeSortFile(File srcDir) throws IOException {
        File tmpSortFile = new File(srcDir.getParent(), srcDir.getName()
                + _SORT);
        String prefix = srcDir.getAbsolutePath() + File.separatorChar;
        BufferedReader r = new BufferedReader(new FileReader(sortFile));
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(tmpSortFile));
            try {
                String line;
                while ((line = r.readLine()) != null) {
                    w.write(prefix);
                    w.write(line);
                    w.newLine();
                }
            } finally {
                try {
                    w.close();
                } catch (IOException e) {
                }
            }
        } finally {
            try {
                r.close();
            } catch (IOException e) {
            }
        }
        return tmpSortFile;
    }

    protected void startService() throws Exception {
        JMSDelegate.getInstance().setMakeIsoImageListener(listener);
    }

    protected void stopService() throws Exception {
        JMSDelegate.getInstance().setMakeIsoImageListener(null);
    }

    private void process(MediaCreationRequest rq) {
        boolean cleanup = true;
        try {
            log.info("Start processing " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }

            rq.setIsoImageFile(new File(rq.getFilesetDir().getParent(), rq
                    .getFilesetDir().getName()
                    + _ISO));
            try {
                makeIsoImage(rq.getFilesetDir(), rq.getIsoImageFile(), rq
                        .getFilesetID(), rq.getVolsetID(), rq.getVolsetSeqno(),
                        rq.getVolsetSize());

                log.info("Forwarding " + rq + " to Media Writer");
                JMSDelegate.getInstance().queueForMediaWriter(rq);
                cleanup = false;
                return;
            } catch (IOException e) {
                log.error("Failed to process " + rq, e);
            } catch (JMSException e) {
                log.error("Failed to forward " + rq + " to Media Writer", e);
            }
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            try {
                rq.updateStatus(ExecutionStatus.FAILURE,
                        ExecutionStatusInfo.PROC_FAILURE, log);
            } catch (IOException e) {
                log.error("Failed to process " + rq, e);
            }
        } finally {
            if (cleanup) rq.cleanFiles(log);
        }
    }
}
