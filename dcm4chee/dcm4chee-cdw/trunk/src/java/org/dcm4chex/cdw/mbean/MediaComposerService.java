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
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
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

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);
    
    private final File confdir;

    private final File dispappDir;

    private final File aprofileDir;
    
    private String fileSetDescriptorFilename = "README.TXT";

    private String charsetOfFileSetDescriptorFile = "ISO_IR 100";

    private ApplicationProfiles applicationProfiles = null;
    
    private boolean keepSpoolFiles = false;
    
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
        confdir = new File(ServerConfigLocator.locate().getServerHomeDir(), "conf");
        dispappDir = new File(confdir, "dispapp");
        aprofileDir = new File(confdir, "aprofile");
    }
    
    public final boolean isKeepSpoolFiles() {
        return keepSpoolFiles;
    }

    public final void setKeepSpoolFiles(boolean keepSpoolFiles) {
        this.keepSpoolFiles = keepSpoolFiles;
    }

    public final String getCharsetOfFileSetDescriptorFile() {
        return charsetOfFileSetDescriptorFile;
    }

    public final void setCharsetOfFileSetDescriptorFile(
            String charsetOfFileSetDescriptorFile) {
        this.charsetOfFileSetDescriptorFile = charsetOfFileSetDescriptorFile;
    }

    public final String getFileSetDescriptorFilename() {
        return fileSetDescriptorFilename;
    }

    public final void setFileSetDescriptorFilename(String fname) {
        this.fileSetDescriptorFilename = fname;
    }

    public final boolean isMakeIsoImage() {
        return makeIsoImage;
    }

    public final void setMakeIsoImage(boolean makeIsoImage) {
        this.makeIsoImage = makeIsoImage;
    }

    public final ObjectName getSpoolDirName() {
        return spoolDir.getSpoolDirName();
    }

    public final void setSpoolDirName(ObjectName spoolDirName) {
        spoolDir.setSpoolDirName(spoolDirName);
    }

    final SpoolDirDelegate getSpoolDir() {
        return spoolDir;
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
                FilesetBuilder builder = new FilesetBuilder(this, rq, attrs);
                builder.build();
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
            if (cleanup && !keepSpoolFiles) {
                if (attrs != null) spoolDir.deleteRefInstances(attrs);
                rq.cleanFiles(log);
            }
        }
    }
    
    public final String getApplicationProfiles() {
        return applicationProfiles != null 
        	? applicationProfiles.toString()
        	: null;
    }

    final File getFileSetDescriptorFile() {
        return new File(confdir, fileSetDescriptorFilename);
    }

    final File getDisplayApplicationDir() {
        return dispappDir;
    }
}
