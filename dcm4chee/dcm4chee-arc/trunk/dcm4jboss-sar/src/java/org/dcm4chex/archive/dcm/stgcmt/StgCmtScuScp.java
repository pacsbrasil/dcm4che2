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

/**
 * @author <a href="mailto:gunter@tiani.com">Gunter Zeilinger</a>
 * @version $Revision$ $Date$
 * @since Jan 5, 2005
 */
class StgCmtScuScp extends DcmServiceBase {

    final StgCmtScuScpService service;

    public StgCmtScuScp(StgCmtScuScpService service) {
        this.service = service;
        // TODO Auto-generated constructor stub
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
        Association a = assoc.getAssociation();
        a.putProperty("StgCmtCmd-" + cmd.getMessageID(), new StgCmtCmd(service,
                a, data));
        return null;
    }

    private void check(Command cmd) throws DcmServiceException {
    }

    protected void doAfterRsp(ActiveAssociation assoc, Dimse rsp) {
        Association a = assoc.getAssociation();
        Command cmd = rsp.getCommand();
        String key = "StgCmtCmd-" + cmd.getMessageIDToBeingRespondedTo();
        final StgCmtCmd stgCmtCmd = (StgCmtCmd) a.getProperty(key);
        if (stgCmtCmd != null) {
            new Thread(new Runnable() {
                public void run() {
                    stgCmtCmd.execute();
                }
            });
        }
    }
}
