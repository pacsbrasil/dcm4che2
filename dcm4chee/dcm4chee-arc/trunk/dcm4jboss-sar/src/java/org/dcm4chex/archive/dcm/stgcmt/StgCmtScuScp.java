/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.dcm.stgcmt;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.JMSException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.archive.util.JMSDelegate;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 * @since Jan 5, 2005
 */
class StgCmtScuScp extends DcmServiceBase {

    private final StgCmtScuScpService service;
    private final Logger log;

    public StgCmtScuScp(StgCmtScuScpService service) {
        this.service = service;
        this.log = service.getLog();
    }

    protected Dataset doNAction(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        Command cmd = rq.getCommand();
        Dataset data = rq.getDataset();
        service.logDataset("StgCmt Request:\n", data);
        if (!UIDs.StorageCommitmentPushModelSOPInstance.equals(cmd
                .getRequestedSOPInstanceUID())) {
            throw new DcmServiceException(Status.NoSuchObjectInstance);
        }
        if (cmd.getInt(Tags.ActionTypeID, -1) != 1) {
            throw new DcmServiceException(Status.NoSuchActionType);
        }
        if (data.vm(Tags.TransactionUID) <= 0) { throw new DcmServiceException(
                Status.MissingAttributeValue,
                "Missing Transaction UID (0008,1195) in Action Information"); }
        if (data.vm(Tags.RefSOPSeq) <= 0) { throw new DcmServiceException(
                Status.MissingAttributeValue,
                "Missing Referenced SOP Sequence (0008,1199) in Action Information"); }
        final Association a = assoc.getAssociation();
        final String aet = a.getCallingAET();
        try {
            AEData aeData = new AECmd(aet).execute();
            if (aeData == null) { throw new DcmServiceException(
                    Status.ProcessingFailure, "Failed to resolve AET:" + aet); }
            FileInfo[][] fileInfos = RetrieveCmd.create(data.get(Tags.RefSOPSeq)).execute();
            StgCmtOrder order = new StgCmtOrder(data, aeData, a.getCalledAET(), fileInfos);
            JMSDelegate.queue(StgCmtScuScpService.QUEUE, order, 0, 0);
        } catch (SQLException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        } catch (JMSException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return null;
    }
    
    protected Dataset doNEventReport(ActiveAssociation assoc, Dimse rq,
            Command rspCmd) throws IOException, DcmServiceException {
        // TODO Auto-generated method stub
        return super.doNEventReport(assoc, rq, rspCmd);
    }
}
