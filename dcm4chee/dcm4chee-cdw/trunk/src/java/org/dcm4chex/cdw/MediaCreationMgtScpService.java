/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.cdw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import javax.jms.JMSException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmService;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 * @author gunter.zeilinter@tiani.com
 * @version $Revision$ $Date$
 * @since 22.06.2004
 *
 */
public class MediaCreationMgtScpService extends AbstractScpService {

    private static final String HIGH = "HIGH";

    private static final String MED = "MED";

    private static final String LOW = "LOW";

    private static final String[] MC_PRIORITY = { LOW, MED, HIGH};

    private static final int[] JMS_PRIORITY = { 3, 4, 5};

    private static final int INITIATE = 1;

    private static final int CANCEL = 2;

    private static final String[] CUIDS = { UIDs.MediaCreationManagementSOPClass};

    private boolean persistentDeliveryMode = true;

    private String requestPriority = MED;

    private int numberOfCopies = 1;

    private int maxNumberOfCopies = 50;
    
    private boolean labelUsingInformationExtractedFromInstances = true;

    private String labelText = "";

    private String labelStyleSelection = "";
    
    private boolean allowMediaSplitting = false;
    
    private boolean allowLossyCompression = false;
    
    private String includeNonDICOMObjects = "NO";
    
    private boolean includeDisplayApplication = false;
    
    private boolean preserveInstances = false;
    
    private final DcmService service = new DcmServiceBase() {

        protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNCreate(assoc, rq, rspCmd);
        }

        protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNAction(assoc, rq, rspCmd);
        }

        protected Dataset doNGet(ActiveAssociation assoc, Dimse rq,
                Command rspCmd) throws IOException, DcmServiceException {
            return MediaCreationMgtScpService.this.doNGet(assoc, rq, rspCmd);
        }

    };

    protected void bindDcmServices() {
        bindDcmServices(CUIDS, service);
    }

    protected void unbindDcmServices() {
        unbindDcmServices(CUIDS);
    }

    protected void updatePresContexts() {
        putPresContexts(CUIDS, getTransferSyntaxes());
    }

    protected void removePresContexts() {
        putPresContexts(CUIDS, null);
    }

    private Dataset doNCreate(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset ds = rq.getDataset();
        if (log.isDebugEnabled()) log.debug("N-Create Attributes:\n" + ds);
        checkCreateAttributes(ds, rspCmd);
        String iuid = rspCmd.getAffectedSOPInstanceUID();
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        if (f.exists())
                throw new DcmServiceException(Status.DuplicateSOPInstance);
        String cuid = rqCmd.getAffectedSOPClassUID();
        String tsuid = rq.getTransferSyntaxUID();
        FileMetaInfo fmi = dof.newFileMetaInfo(cuid, iuid, tsuid);
        ds.setFileMetaInfo(fmi);
        ds.putUI(Tags.SOPInstanceUID, iuid);
        ds.putUI(Tags.SOPClassUID, cuid);
        ds.putCS(Tags.ExecutionStatus, ExecutionStatus.IDLE);
        ds.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.NORMAL);
        try {
            spoolDir.writeDatasetTo(ds, f);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        return ds;
    }

    private void checkCreateAttributes(Dataset ds, Command rspCmd) {
        // TODO Auto-generated method stub

    }

    private Dataset doNAction(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset actionInfo = rq.getDataset();
        if (log.isDebugEnabled())
                log.debug("N-Action Information:\n" + actionInfo);
        String iuid = rqCmd.getAffectedSOPInstanceUID();
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        Dataset mcrq;
        try {
            mcrq = spoolDir.readDatasetFrom(f);
        } catch (FileNotFoundException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        final String status = mcrq.getString(Tags.ExecutionStatus,
                ExecutionStatus.IDLE);
        final int actionID = rqCmd.getInt(Tags.ActionTypeID, -1);
        switch (actionID) {
        case INITIATE:
            if (!ExecutionStatus.IDLE.equals(status))
                    throw new DcmServiceException(
                            Status.DuplicateInitiateMediaCreation);
            checkActionInfo(actionInfo, rspCmd);
            
            createDisk1(iuid, mcrq);

            String priority = actionInfo != null ? actionInfo.getString(
                    Tags.RequestPriority, requestPriority) : requestPriority;
            int copies = actionInfo != null ? actionInfo.getInt(
                    Tags.NumberOfCopies, numberOfCopies) : numberOfCopies;
            mcrq.putUS(Tags.NumberOfCopies, copies);
            mcrq.putCS(Tags.RequestPriority, priority);
            mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.PENDING);
            mcrq.putCS(Tags.ExecutionStatusInfo, ExecutionStatusInfo.QUEUED);
            try {
                spoolDir.writeDatasetTo(mcrq, f);
            } catch (IOException e) {
                throw new DcmServiceException(Status.ProcessingFailure);
            }
            try {                
                JMSDelegate.getInstance().queuePending(iuid,
                        persistentDeliveryMode, toJMSPriority(priority), 0, 0L);
                return null;
            } catch (JMSException e1) {
                mcrq.putCS(Tags.ExecutionStatusInfo,
                        ExecutionStatusInfo.PROC_FAILURE);
            }
            mcrq.putCS(Tags.ExecutionStatus, ExecutionStatus.FAILURE);
            try {
                spoolDir.writeDatasetTo(mcrq, f);
            } catch (IOException ignore) {
            }
            throw new DcmServiceException(Status.ProcessingFailure);
        case CANCEL:
            if (ExecutionStatus.CREATING.equals(status))
                    throw new DcmServiceException(
                            Status.MediaCreationRequestAlreadyInProgress);
            if (ExecutionStatus.DONE.equals(status)
                    || ExecutionStatus.FAILURE.equals(status))
                    throw new DcmServiceException(
                            Status.MediaCreationRequestAlreadyCompleted);
            if (!spoolDir.delete(f))
                    throw new DcmServiceException(
                            Status.CancellationDeniedForUnspecifiedReason);
            spoolDir.deleteMediaLayouts(iuid);
            spoolDir.deleteRefInstances(mcrq);
            break;
        default:
            throw new DcmServiceException(Status.NoSuchActionType, "actionID:"
                    + actionID);
        }
        return null;
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

    private void createDisk1(String iuid, Dataset mcrq) {
        File[] instances = spoolDir.getRefInstanceFiles(mcrq);
        File disk1 = new File(spoolDir.getMediaLayoutsRoot(iuid), "DISK1");
        File dicom = new File(disk1, "DICOM");
        log.info("M-WRITE " + dicom);
        dicom.mkdirs();
        HashSet used = new HashSet();
        for (int i = 0; i < instances.length; i++) {
	        File src = instances[i];
	        int hash = src.getName().hashCode();
	        while (!used.add(new Integer(hash))) ++hash;
	        File dst = new File(dicom, toHex(hash));
	        link(src, dst);
        }
    }

    private void link(File src, File dst) {
        String cmd = "ln -s " + src.getAbsolutePath() + " " + dst.getAbsolutePath();
        log.debug(cmd);
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param actionInfo
     * @param rspCmd
     */
    private void checkActionInfo(Dataset actionInfo, Command rspCmd) {
        // TODO Auto-generated method stub

    }

    private static int toJMSPriority(String priority) {
        for (int i = 0; i < MC_PRIORITY.length; i++)
            if (MC_PRIORITY[i].equals(priority)) return JMS_PRIORITY[i];
        throw new IllegalArgumentException("priority:" + priority);
    }

    private Dataset doNGet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
            throws IOException, DcmServiceException {
        Command rqCmd = rq.getCommand();
        Dataset ds = rq.getDataset(); // should be null!
        String iuid = rqCmd.getAffectedSOPInstanceUID();
        File f = spoolDir.getMediaCreationRequestFile(iuid);
        Dataset mcrq;
        log.info("M-READ " + f);
        try {
            mcrq = spoolDir.readDatasetFrom(f);
        } catch (FileNotFoundException e) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        } catch (IOException e) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        final int[] filter = rqCmd.getTags(Tags.AttributeIdentifierList);
        return filter != null ? ds.subSet(filter) : ds;
    }
}
