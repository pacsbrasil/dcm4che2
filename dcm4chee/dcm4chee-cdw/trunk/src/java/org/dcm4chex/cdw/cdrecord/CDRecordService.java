/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4chex.cdw.common.AbstractCDWriterService;
import org.dcm4chex.cdw.common.ExecutionStatus;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;


/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractCDWriterService {

    protected void process(String iuid, int retry) {
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        Dataset mcrq;
        try {
            mcrq = spoolDir.readDatasetFrom(f);
        } catch (FileNotFoundException e) {
            return; // was canceled
        } catch (IOException e) {
            return;
        }
        mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.CREATING);
        mcrq.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            spoolDir.writeDatasetTo(mcrq, f);
        } catch (IOException ignore) {
        }
        //TODO
        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e1) {
        }
        
        spoolDir.deleteMediaLayouts(iuid);
        spoolDir.deleteRefInstances(mcrq);
        mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.DONE);
        mcrq.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            spoolDir.writeDatasetTo(mcrq, f);
        } catch (IOException ignore) {
        }
    }
}
