/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.common;

import java.io.IOException;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public abstract class AbstractMediaWriterService extends ServiceMBeanSupport {

    private final MessageListener listener = new MessageListener() {

        public void onMessage(Message msg) {
            ObjectMessage objmsg = (ObjectMessage) msg;
            try {
                AbstractMediaWriterService.this
                        .process((MediaCreationRequest) objmsg.getObject());
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

    };

    protected void startService() throws Exception {
        JMSDelegate.getInstance().setMediaWriterListener(listener);
    }

    protected void stopService() throws Exception {
        JMSDelegate.getInstance().setMediaWriterListener(null);
    }

    protected void process(MediaCreationRequest rq) {
        try {
            log.info("Start processing " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
            Dataset attrs = rq.readAttributes(log);
            String status = attrs.getString(Tags.ExecutionStatus);
            if (ExecutionStatus.FAILURE.equals(status)) {
                log.info("" + rq + " already failed");
                return;
            }
            if (!ExecutionStatus.CREATING.equals(status)) {
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.CREATING);
                attrs.putCS(Tags.ExecutionStatusInfo,
                        ExecutionStatusInfo.NORMAL);
                rq.writeAttributes(attrs, log);
            }
            try {
                handle(rq, attrs);
                log.info("Finished processing " + rq);
                DcmElement sq = attrs.get(Tags.RefStorageMediaSeq);
                if (sq == null) sq = attrs.putSQ(Tags.RefStorageMediaSeq);
                Dataset item = sq.addNewItem();
                item.putUI(Tags.StorageMediaFileSetID, rq.getFilesetID());
                item.putUI(Tags.StorageMediaFileSetUID, rq.getFilesetDir()
                        .getName());
                attrs.putUS(Tags.TotalNumberOfPiecesOfMediaCreated, rq
                        .getVolsetSeqno()
                        * rq.getNumberOfCopies());
                if (rq.getVolsetSeqno() == rq.getVolsetSize()) {
                    attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.DONE);
                    attrs.putCS(Tags.ExecutionStatusInfo,
                            ExecutionStatusInfo.NORMAL);
                }
            } catch (MediaCreationException e) {
                log.error("Failed to process " + rq, e);
                attrs.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
                attrs.putCS(Tags.ExecutionStatusInfo, e.getStatusInfo());
            }
            rq.writeAttributes(attrs, log);
        } catch (IOException e) {
            // error already logged
        } finally {
            rq.cleanFiles(log);
        }

    }

    protected abstract void handle(MediaCreationRequest r, Dataset attrs)
            throws MediaCreationException;
}
