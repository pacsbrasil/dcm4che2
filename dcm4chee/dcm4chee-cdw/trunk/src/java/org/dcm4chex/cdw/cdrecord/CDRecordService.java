/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationRequest;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

    protected void process(MediaCreationRequest rq) {
        try {
            log.info("Start processing " + rq);
            if (rq.isCanceled()) {
                log.info("" + rq + " was canceled");
                return;
            }
             
            Dataset attrs = rq.readAttributes(log);
            rq.updateStatus(ExecutionStatus.CREATING, ExecutionStatusInfo.NORMAL, log);
            //TODO
            try {
                Thread.sleep(100000L);
            } catch (InterruptedException e1) {
            }
            rq.updateStatus(ExecutionStatus.DONE, ExecutionStatusInfo.NORMAL, log);
            log.info("Finished processing " + rq);
        } catch (IOException e) {
            log.error("Failed to process " + rq, e);
            try {
                rq.updateStatus(ExecutionStatus.FAILURE, ExecutionStatusInfo.PROC_FAILURE, log);
            } catch (IOException e1) {
            }
        } finally {
            rq.cleanFiles(log);
        }
    }
}
