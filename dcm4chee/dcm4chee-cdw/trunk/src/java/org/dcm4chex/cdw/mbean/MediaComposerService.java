/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.mbean;

import java.io.File;
import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.util.UIDGenerator;
import org.dcm4cheri.util.StringUtils;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.FileUtils;
import org.dcm4chex.cdw.common.Flag;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 25.06.2004
 *
 */
public class MediaComposerService extends ServiceMBeanSupport {

    private static final UIDGenerator uidgen = UIDGenerator.getInstance();

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private final File readmeFile;

    private final File viewerDir;

    private String viewerDirOnMedia = "VIEWER";

    private boolean makeIsoImage = true;

    private final MessageListener listener = new MessageListener() {

        public void onMessage(Message msg) {
            ObjectMessage objmsg = (ObjectMessage) msg;
            try {
                MediaComposerService.this.process((MediaCreationRequest) objmsg
                        .getObject());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    public MediaComposerService() {
        File homedir = ServerConfigLocator.locate().getServerHomeDir();
        File confdir = new File(homedir, "conf");
        readmeFile = new File(confdir, "README.TXT");
        viewerDir = new File(confdir, "VIEWER");
    }

    public final boolean isMakeIsoImage() {
        return makeIsoImage;
    }

    public final void setMakeIsoImage(boolean makeIsoImage) {
        this.makeIsoImage = makeIsoImage;
    }

    public final String getViewerDirOnMedia() {
        return viewerDirOnMedia;
    }

    public final void setViewerDirOnMedia(String viewerDirOnMedia) {
        this.viewerDirOnMedia = viewerDirOnMedia;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    protected void startService() throws Exception {
        JMSDelegate.getInstance().setMediaComposerListener(listener);
    }

    protected void stopService() throws Exception {
        JMSDelegate.getInstance().setMediaComposerListener(null);
    }

    protected void process(MediaCreationRequest rq) {
        boolean cleanup = true;
        Dataset attrs = null;
        try {
            log.info("Start processing " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            attrs = rq.readAttributes(log);
            attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
            attrs.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.BUILDING);
            rq.writeAttributes(attrs, log);
            try {
                buildFileset(rq, attrs);
                // TODO split fileset on several media
                if (rq.isCanceled()) {
                    log.info("" + rq + " was canceled");
                    return;
                }
                attrs = rq.readAttributes(log);
                String status = attrs.getString(Tags.ExecutionStatus);
                if (ExecutionStatus.FAILURE.equals(status)) {
                    log.info("" + rq + " already failed");
                    return;
                }
                if (rq.getVolsetSeqno() == 1) {
                    attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
                    attrs.putCS(Tags.ExecutionStatusInfo,
                            makeIsoImage ? ExecutionStatusInfo.QUEUED_MKISOFS
                                    : ExecutionStatusInfo.QUEUED);
                    rq.writeAttributes(attrs, log);
                }
                try {
                    if (makeIsoImage)
                        JMSDelegate.getInstance().queueForMakeIsoImage(log, rq);
                    else
                        JMSDelegate.getInstance().queueForMediaWriter(log, rq);
                    cleanup = false;
                } catch (JMSException e) {
                    throw new MediaCreationException(
                            ExecutionStatusInfo.PROC_FAILURE, e);
                }
            } catch (MediaCreationException e) {
                if (rq.isCanceled()) {
                    log.info("" + rq + " was canceled");
                    return;
                }
                log.error("Failed to process " + rq, e);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
                rq.writeAttributes(attrs, log);
            }
        } catch (IOException e) {
            // error already logged
        } finally {
            if (cleanup) {
                if (attrs != null) spoolDir.deleteRefInstances(attrs);
                rq.cleanFiles(log);
            }
        }
    }

    private File makeRootDir(String uid) throws IOException {
        if (uid == null) return null;
        File dir = spoolDir.getMediaFilesetRootDir(uid);
        if (dir.exists()) return null;
        if (!dir.mkdir()) throw new IOException("Failed to mkdir " + dir);
        return dir;
    }

    private void buildFileset(MediaCreationRequest rq, Dataset attrs)
            throws MediaCreationException {
        try {
            String fsuid = attrs.getString(Tags.StorageMediaFileSetUID);
            File rootDir;
            while ((rootDir = makeRootDir(fsuid)) == null)
                fsuid = uidgen.createUID();

            rq.setFilesetDir(rootDir);
            rq.setFilesetID(attrs.getString(Tags.StorageMediaFileSetID));

            makeSymLink(readmeFile, new File(rootDir, "README.TXT"));
            final boolean preserve = Flag
                    .isYes(attrs
                            .getString(Tags.PreserveCompositeInstancesAfterMediaCreation));
            DcmElement refSOPs = attrs.get(Tags.RefSOPSeq);
            for (int i = 0, n = refSOPs.vm(); i < n; ++i) {
                Dataset item = refSOPs.getItem(i);
                String iuid = item.getString(Tags.RefSOPInstanceUID);
                File src = spoolDir.getInstanceFile(iuid);
                Dataset ds = FileUtils.readDataset(src, log);
                String[] fileIDs = makeFileIDs(ds);
                File dest = new File(rootDir, StringUtils.toString(fileIDs,
                        File.separatorChar));
                File parent = dest.getParentFile();
                if (!parent.exists() && !parent.mkdirs())
                        throw new IOException("Failed to mkdirs " + parent);
                if (preserve)
                    makeSymLink(src, dest);
                else
                    move(src, dest);
            }
            if (Flag.isYes(attrs.getString(Tags.IncludeDisplayApplication)))
                    makeSymLink(viewerDir, new File(rootDir, viewerDirOnMedia));

        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE);
        }
    }

    private String[] makeFileIDs(Dataset ds) {
        return new String[] { "DICOM",
                toHex(ds.getString(Tags.PatientID, "").hashCode()),
                toHex(ds.getString(Tags.StudyInstanceUID, "").hashCode()),
                toHex(ds.getString(Tags.SeriesInstanceUID, "").hashCode()),
                toHex(ds.getString(Tags.SOPInstanceUID, "").hashCode()),};
    }

    // only for Tests
    private static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String toHex(int val) {
        char[] ch8 = new char[8];
        for (int i = 8; --i >= 0; val >>= 4) {
            ch8[i] = HEX_DIGIT[val & 0xf];
        }
        return String.valueOf(ch8);
    }

    private void move(File src, File dst) throws IOException {
        if (log.isDebugEnabled()) log.debug("M-MOVE " + src + " => " + dst);
        if (!src.renameTo(dst))
                throw new IOException("M-MOVE " + src + " => " + dst
                        + " failed!");
    }

    private void makeSymLink(File src, File dst) throws IOException {
        String[] cmd = new String[] { "ln", "-s", src.getAbsolutePath(),
                dst.getAbsolutePath()};
        if (log.isDebugEnabled()) log.debug("M-LINK " + src + " => " + dst);
        int exitCode;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            exitCode = p.waitFor();
        } catch (Exception e) {
            throw new IOException("M-LINK " + src + " => " + dst + " failed!"
                    + e);
        }
        if (exitCode != 0) { throw new IOException("M-LINK " + src + " => "
                + dst + " failed!" + " failed! Exit Code: " + exitCode); }
    }
}
