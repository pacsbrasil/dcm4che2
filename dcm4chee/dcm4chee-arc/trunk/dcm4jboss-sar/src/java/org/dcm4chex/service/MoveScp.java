/*
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.service;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.dcm4chex.config.NetworkAEInfo;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 31.08.2003
 */
public class MoveScp extends DcmServiceBase {
    private final MoveScpService scp;
    private final Logger log;

    public MoveScp(MoveScpService scp) {
        this.scp = scp;
        this.log = scp.getLog();
    }

    public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException {
        Command rqCmd = rq.getCommand();
        String dest = rqCmd.getString(Tags.MoveDestination);
        Dataset rqData = rq.getDataset();
        try {
            NetworkAEInfo aeInfo = getNetworkAEInfo(dest);
            FileInfo[] fileInfo;
            try {
                RetrieveCmd retrieveCmd =
                    RetrieveCmd.create(scp.getDataSource(), rqData);
                new Thread(
                    new MoveTask(
                        log,
                        assoc,
                        rq.pcid(),
                        rqCmd,
                        retrieveCmd.execute(),
                        aeInfo,
                        dest))
                    .start();
            } catch (Exception e) {
                log.error("Query DB failed:", e);
                throw new DcmServiceException(Status.ProcessingFailure, e);
            }
        } catch (DcmServiceException e) {
            Command rspCmd = objFact.newCommand();
            rspCmd.initCMoveRSP(
                rqCmd.getMessageID(),
                rqCmd.getAffectedSOPClassUID(),
                e.getStatus());
            e.writeTo(rspCmd);
            Dimse rsp = fact.newDimse(rq.pcid(), rspCmd);
            assoc.getAssociation().write(rsp);
        }
    }

    private NetworkAEInfo getNetworkAEInfo(String dest)
        throws DcmServiceException {
            NetworkAEInfo aeInfo = scp.findNetworkAE(dest);
            if (aeInfo == null) {
                log.warn("Unkown Move Destination - " + dest);
                throw new DcmServiceException(Status.MoveDestinationUnknown);
            }
            
            if (aeInfo.getNetworkConnections().isEmpty()) {
                log.error(
                    "No configured connection of Move Destination - " + dest);
                throw new DcmServiceException(
                    Status.UnableToPerformSuboperations);
            }
            return aeInfo;
    }

}
