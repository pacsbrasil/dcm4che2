/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw.cdrecord;

import org.dcm4che.data.Dataset;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

    protected void handle(MediaCreationRequest r, Dataset attrs) throws MediaCreationException {
        //TODO
        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e1) {
        }
    }
}
