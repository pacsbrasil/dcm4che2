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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 03.12.2003
 */
final class ForwardTask implements Runnable, DimseListener {

    private static final String[] NATIVE_TS =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final int PCID = 1;

    private final Logger log;
    private final Collection storedStudiesInfo;
    private final String destAETs;
    private final ActiveAssociation moveAssoc;
    private final int priority;

    public ForwardTask(
        Logger log,
        String callingAET,
        AEData moveSCP,
        Collection storedStudiesInfo,
        String destAETs,
        int priority)
        throws UnknownHostException, IOException {
        this.log = log;
        this.storedStudiesInfo = storedStudiesInfo;
        this.destAETs = destAETs;
        this.moveAssoc = openAssociation(callingAET, moveSCP);
        this.priority = priority;
    }

    private ActiveAssociation openAssociation(
        String callingAET,
        AEData moveSCP)
        throws UnknownHostException, IOException {
        Association a = StoreScp.af.newRequestor(createSocket(moveSCP));
        AAssociateRQ rq = StoreScp.af.newAAssociateRQ();
        rq.setCalledAET(moveSCP.getTitle());
        rq.setCallingAET(callingAET);
        rq.addPresContext(
            StoreScp.af.newPresContext(
                PCID,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                NATIVE_TS));
        PDU ac = a.connect(rq);
        if (!(ac instanceof AAssociateAC)) {
            throw new IOException("Association not accepted by " + moveSCP);
        }
        ActiveAssociation aa = StoreScp.af.newActiveAssociation(a, null);
        aa.start();
        if (a.getAcceptedTransferSyntaxUID(PCID) == null) {
            try {
                aa.release(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            throw new IOException(
                "Presentation Context for Retrieve rejected by " + moveSCP);
        }
        return aa;
    }

    private Socket createSocket(AEData moveSCP)
        throws UnknownHostException, IOException {
        return new Socket(moveSCP.getHostName(), moveSCP.getPort());
    }

    public void run() {
        for (Iterator it = storedStudiesInfo.iterator(); it.hasNext();) {
            Dataset scn = (Dataset) it.next();
            final String studyIUID = scn.getString(Tags.StudyInstanceUID);
            DcmElement refSeriesSeq = scn.get(Tags.RefSeriesSeq);
            for (int i = 0, n = refSeriesSeq.vm(); i < n; ++i) {
                Dataset refSeries = refSeriesSeq.getItem(i);
                final String serIUID =
                    refSeries.getString(Tags.SeriesInstanceUID);
                DcmElement refSOPSeq = refSeries.get(Tags.RefImageSeq);
                final String[] sopIUIDs = new String[refSOPSeq.vm()];
                for (int j = 0, m = refSOPSeq.vm(); j < m; ++j) {
                    sopIUIDs[j] =
                        refSOPSeq.getItem(j).getString(Tags.RefSOPInstanceUID);
                }
                StringTokenizer stok = new StringTokenizer(destAETs, "\\");
                while (stok.hasMoreTokens()) {
                    doMove(studyIUID, serIUID, sopIUIDs, stok.nextToken());
                }
            }
        }
    }

    private void doMove(
        String studyIUID,
        String seriesIUID,
        String[] sopIUIDs,
        String moveDest) {
        Command cmd = StoreScp.dof.newCommand();
        cmd.initCMoveRQ(
            moveAssoc.getAssociation().nextMsgID(),
            UIDs.StudyRootQueryRetrieveInformationModelMOVE,
            priority,
            moveDest);
        Dataset ds = StoreScp.dof.newDataset();
        ds.putCS(Tags.QueryRetrieveLevel, "IMAGE");
        ds.putUI(Tags.StudyInstanceUID, studyIUID);
        ds.putUI(Tags.SeriesInstanceUID, seriesIUID);
        ds.putUI(Tags.SOPInstanceUID, sopIUIDs);
        try {
            moveAssoc.invoke(StoreScp.af.newDimse(PCID, cmd, ds), this);
        } catch (Exception e) {
            log.error(
                "Failed to invoke move for forwarding "
                    + sopIUIDs.length
                    + " objects",
                e);
        }
    }

    public void dimseReceived(Association assoc, Dimse dimse) {
        // TODO Auto-generated method stub

    }
}
