/* $Id$
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
import org.dcm4chex.archive.ejb.jdbc.AECmd;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.archive.ejb.jdbc.RetrieveCmd;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 31.08.2003
 */
public class MoveScp extends DcmServiceBase
{
    private final Logger log;
    private final DataSourceFactory dsf;

    public MoveScp(Logger log, DataSourceFactory dsf)
    {
        this.log = log;
        this.dsf = dsf;
    }

    public void c_move(ActiveAssociation assoc, Dimse rq) throws IOException
    {
        Command rqCmd = rq.getCommand();
        String dest = rqCmd.getString(Tags.MoveDestination);
        Dataset rqData = rq.getDataset();
        try
        {
            AEData aeData = queryAEData(dest);
            FileInfo[][] fileInfos = queryFileInfos(rqData);
            new Thread(
                new MoveTask(
                    log,
                    assoc,
                    rq.pcid(),
                    rqCmd,
                    fileInfos,
                    aeData,
                    dest))
                .start();
        } catch (DcmServiceException e)
        {
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

    private FileInfo[][] queryFileInfos(Dataset rqData)
        throws DcmServiceException
    {
        FileInfo[][] fileInfos = null;
        try
        {
            RetrieveCmd retrieveCmd =
                RetrieveCmd.create(dsf.getDataSource(), rqData);
            fileInfos = retrieveCmd.execute();
        } catch (Exception e)
        {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        return fileInfos;
    }

    private AEData queryAEData(String dest) throws DcmServiceException
    {
        AEData aeData = null;
        try
        {
            AECmd aeCmd = new AECmd(dsf.getDataSource(), dest);
            aeData = aeCmd.execute();
        } catch (Exception e)
        {
            log.error("Query DB failed:", e);
            throw new DcmServiceException(Status.ProcessingFailure, e);
        }
        if (aeData == null)
        {
            throw new DcmServiceException(Status.MoveDestinationUnknown, dest);
        }
        return aeData;
    }
}
