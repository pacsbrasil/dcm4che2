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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.management.ObjectName;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.ConfigurationException;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.JMSDelegate;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;
import org.dcm4chex.cdw.common.SpoolDirDelegate;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 25.06.2004
 *
 */
public class MediaComposerService extends ServiceMBeanSupport {

    private SpoolDirDelegate spoolDir = new SpoolDirDelegate(this);

    private DirRecordFactory dirRecordFactory = new DirRecordFactory(
            "resource:dicomdir-records.xml");

    private final File xmlFile;
    
    private final File mergeDir;

    private final File mergeDirViewer;

    private final File mergeDirWeb;
    
    private String fileSetDescriptorFile = "README.TXT";

    private String charsetOfFileSetDescriptorFile = "ISO_IR 100";

    private boolean keepSpoolFiles = false;

    private boolean createIcon = true;

    private int iconWidth = 64;

    private int iconHeight = 64;

    private int jpegWidth = 512;

    private int jpegHeight = 512;

    private boolean makeIsoImage = true;

    private boolean logXml = false;
    
    private final ImageReader imageReader;

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
        ServerConfig config = ServerConfigLocator.locate();
        File homedir = config.getServerHomeDir();
        xmlFile = new File(homedir, "log" + File.separatorChar + "dicomdir.xml");
        File datadir = config.getServerDataDir();
        checkExists(mergeDir = new File(datadir, "mergedir"));
        checkExists(mergeDirViewer = new File(datadir, "mergedir-viewer"));
        checkExists(mergeDirWeb = new File(datadir, "mergedir-web"));
        Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
        if (!it.hasNext())
                throw new ConfigurationException("DICOM Image Reader not found");
        imageReader = (ImageReader) it.next();
    }

    private void checkExists(File file) {
        if (!file.exists())
                throw new ConfigurationException("missing " + file);
    }

    final File getMergeDir() {
        return mergeDir;
    }

    final File getMergeDirWeb() {
        return mergeDirWeb;
    }
    
    final File getMergeDirViewer() {
        return mergeDirViewer;
    }

    final DirRecordFactory getDirRecordFactory() {
        return dirRecordFactory;
    }

    final ImageReader getImageReader() {
        return imageReader;
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

    public final String getFileSetDescriptorFile() {
        return fileSetDescriptorFile;
    }

    public final void setFileSetDescriptorFile(String fname) {
        checkExists(new File(mergeDir, fname));
        checkExists(new File(mergeDirViewer, fname));
        this.fileSetDescriptorFile = fname;
    }

    public final int getIconHeight() {
        return iconHeight;
    }

    public final void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public final int getIconWidth() {
        return iconWidth;
    }

    public final void setIconWidth(int iconWidth) {
        this.iconWidth = iconWidth;
    }

    public final boolean isCreateIcon() {
        return createIcon;
    }

    public final void setCreateIcon(boolean includeIcon) {
        this.createIcon = includeIcon;
    }

    public final int getJpegHeight() {
        return jpegHeight;
    }

    public final void setJpegHeight(int jpegHeight) {
        this.jpegHeight = jpegHeight;
    }

    public final int getJpegWidth() {
        return jpegWidth;
    }

    public final void setJpegWidth(int jpegWidth) {
        this.jpegWidth = jpegWidth;
    }

    public final boolean isMakeIsoImage() {
        return makeIsoImage;
    }

    public final void setMakeIsoImage(boolean makeIsoImage) {
        this.makeIsoImage = makeIsoImage;
    }

    public final boolean isLogXml() {
        return logXml;
    }
    public final void setLogXml(boolean logXml) {
        this.logXml = logXml;
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
                WebBuilder webBuilder = new WebBuilder(this, rq, attrs);
                if (logXml)
                    webBuilder.toXML(xmlFile);
                webBuilder.createIndex();
                if (builder.isWeb())
                    webBuilder.createWeb();
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
}
