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
import java.io.IOException;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.media.DirBuilderFactory;
import org.dcm4che.media.DirReader;
import org.dcm4che.media.DirRecord;
import org.dcm4chex.cdw.common.AbstractMediaWriterService;
import org.dcm4chex.cdw.common.ExecutionStatusInfo;
import org.dcm4chex.cdw.common.MediaCreationException;
import org.dcm4chex.cdw.common.MediaCreationRequest;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class CDRecordService extends AbstractMediaWriterService {

    private static final DirBuilderFactory dbf = DirBuilderFactory.getInstance();
    
    protected void handle(MediaCreationRequest r, Dataset attrs) throws MediaCreationException {
        
        File dicomdir = new File(r.getFilesetDir(), "DICOMDIR");
        try {
            DirReader reader = dbf.newDirReader(dicomdir);
            try {
                DirRecord patRec = reader.getFirstRecord();
                while (patRec != null) {
                    Dataset patData = patRec.getDataset();
                    log.info("Patient: " + patData.getString(Tags.PatientName));
                    DirRecord studyRec = patRec.getFirstChild();
                    while (studyRec != null) {
                        Dataset studyData = studyRec.getDataset();
                        log.info("Study: " + studyData.getDateTime(Tags.StudyDate, Tags.StudyTime) + " - " + studyData.getString(Tags.StudyDescription));
                        studyRec = studyRec.getNextSibling();
                    }
                    patRec = patRec.getNextSibling();
                }
            } finally {
                try { reader.close(); } catch (IOException e) {}
            }
        } catch (IOException e) {
            throw new MediaCreationException(ExecutionStatusInfo.PROC_FAILURE, e);
        }
        //TODO
        try {
            Thread.sleep(100000L);
        } catch (InterruptedException e1) {
        }
    }
}
