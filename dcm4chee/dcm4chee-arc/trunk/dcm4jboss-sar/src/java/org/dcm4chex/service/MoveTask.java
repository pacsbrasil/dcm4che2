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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dcm4che.conf.DeviceInfo;
import org.dcm4che.conf.NetworkAEInfo;
import org.dcm4che.conf.NetworkConnectionInfo;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DataSource;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PresContext;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.jboss.logging.Logger;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 16.09.2003
 */
class MoveTask implements Runnable {

    private static final String[] NATIVE_TS =
        { UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
    private static final List NATIVE_TS_AS_LIST = Arrays.asList(NATIVE_TS);

    private static final AssociationFactory af =
        AssociationFactory.getInstance();
    private static final DcmObjectFactory of = DcmObjectFactory.getInstance();

    private static int defaultBufferSize = 2048;

    private final Logger log;
    private final byte[] buffer = new byte[defaultBufferSize];
    private final DeviceInfo deviceInfo;
    private final String moveDest;
    private final NetworkAEInfo aeInfo;
    private final int movePcid;
    private final Command moveRqCmd;
    private final String moveOriginatorAET;
	private final String retrieveAET;
    private ActiveAssociation moveAssoc;
    private final LinkedHashMap fileInfoMap = new LinkedHashMap();
    private final HashMap pcInfo = new HashMap();
    private final ArrayList failedIUIDs = new ArrayList();
    private int warnings = 0;
    private int completed = 0;
    private int remaining;
    private boolean canceled = false;
    private ActiveAssociation storeAssoc;

    public MoveTask(
        Logger log,
        ActiveAssociation moveAssoc,
        int movePcid,
        Command moveRqCmd,
        FileInfo[] fileInfo,
        DeviceInfo deviceInfo,
        String moveDest)
        throws DcmServiceException {
        this.log = log;
        this.moveAssoc = moveAssoc;
        this.movePcid = movePcid;
        this.moveRqCmd = moveRqCmd;
        this.deviceInfo = deviceInfo;
        this.moveDest = moveDest;
        this.moveOriginatorAET = moveAssoc.getAssociation().getCallingAET();
		this.retrieveAET = moveAssoc.getAssociation().getCalledAET();
        this.aeInfo = deviceInfo.getNetworkAE(moveDest);
        if (aeInfo == null) {
            throw new DcmServiceException(Status.ProcessingFailure);
        }
        for (int i = 0; i < fileInfo.length; i++) {
            putFileInfo(fileInfo[i]);
        }
        if (fileInfo.length > 0) {
            notifyMovePending();
            openAssociation();
            moveAssoc
                .addCancelListener(
                    moveRqCmd.getMessageID(),
                    new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    canceled = true;
                }
            });
        }
    }

    private void openAssociation() throws DcmServiceException {
        try {
            Association a = af.newRequestor(createSocket());
            PDU ac = a.connect(createAAssociateRQ());
            if (ac instanceof AAssociateAC) {
                storeAssoc = af.newActiveAssociation(a, null);
                storeAssoc.start();
                return;
            }
        } catch (IOException e) {}
        throw new DcmServiceException(
            Status.UnableToPerformSuboperations,
            "Connecting " + aeInfo.getAETitle() + " failed!");

    }

    private AAssociateRQ createAAssociateRQ() {
        AAssociateRQ rq = af.newAAssociateRQ();
        rq.setCalledAET(moveDest);
        rq.setCallingAET(moveAssoc.getAssociation().getCalledAET());
        HashMap fileInfoForCUID;
        FileInfo fileInfo = null;
        for (Iterator it = pcInfo.values().iterator(); it.hasNext();) {
            fileInfoForCUID = (HashMap) it.next();
            for (Iterator it2 = fileInfoForCUID.values().iterator();
                it2.hasNext();
                ) {
                fileInfo = (FileInfo) it2.next();
                if (!NATIVE_TS_AS_LIST.contains(fileInfo.tsUID)) {
                    rq.addPresContext(
                        af.newPresContext(
                            rq.nextPCID(),
                            fileInfo.sopCUID,
                            new String[] { fileInfo.tsUID }));
                }
            }
            rq.addPresContext(
                af.newPresContext(rq.nextPCID(), fileInfo.sopCUID, NATIVE_TS));
        }
        return rq;
    }

    private Socket createSocket()
        throws UnknownHostException, DcmServiceException, IOException {
        if (!aeInfo.isInstalled(deviceInfo)) {
            throw new DcmServiceException(
                Status.UnableToPerformSuboperations,
                aeInfo.getAETitle() + " not installed!");
        }
        NetworkConnectionInfo[] nc = aeInfo.getNetworkConnection();
        for (int i = 0; i < nc.length; i++) {
            if (nc[i].isInstalled(deviceInfo) && nc[i].isListening()) {
                return createSocket(nc[i]);
            }
        }
        throw new DcmServiceException(
            Status.UnableToPerformSuboperations,
            "No server installed at " + aeInfo.getAETitle() + "!");
    }

    private Socket createSocket(NetworkConnectionInfo info)
        throws DcmServiceException, UnknownHostException, IOException {
        if (info.isTLS()) {
            throw new DcmServiceException(
                Status.UnableToPerformSuboperations,
                "dicom-tls not yet supported");
        }
        return new Socket(info.getHostname(), info.getPort());
    }

    private void putFileInfo(FileInfo info) {
        ArrayList fileInfoForIUID = (ArrayList) fileInfoMap.get(info.sopIUID);
        if (fileInfoForIUID == null) {
            fileInfoMap.put(info.sopIUID, fileInfoForIUID = new ArrayList());
        }
        fileInfoForIUID.add(info);
        HashMap fileInfoForCUID = (HashMap) pcInfo.get(info.sopCUID);
        if (fileInfoForCUID == null) {
            pcInfo.put(info.sopCUID, fileInfoForCUID = new HashMap());
        }
        fileInfoForCUID.put(info.tsUID, info);
    }

    public void run() {
        Iterator it = fileInfoMap.entrySet().iterator();
        while (!canceled && it.hasNext()) {
            Map.Entry entry = (Entry) it.next();
            final String iuid = (String) entry.getKey();
            DimseListener storeScpListener = new DimseListener() {
                public void dimseReceived(Association assoc, Dimse dimse) {
                    switch (dimse.getCommand().getStatus()) {
                        case Status.Success :
                            ++completed;
                            break;
                        case Status.CoercionOfDataElements :
                        case Status.DataSetDoesNotMatchSOPClassWarning :
                        case Status.ElementsDiscarded :
                            ++warnings;
                            break;
                        default :
                            failedIUIDs.add(iuid);
                            break;
                    }
                    if (!canceled && remaining() > 0) {
                        notifyMovePending();
                    }
                }
            };
            try {
                Object[] fileAndPresCtx =
                    selectFileAndPresCtx((ArrayList) entry.getValue());
                FileInfo info = (FileInfo) fileAndPresCtx[0];
                PresContext presCtx = (PresContext) fileAndPresCtx[1];
                storeAssoc.invoke(
                    makeCStoreRQ(info, presCtx.pcid()),
                    storeScpListener);
            } catch (Exception e) {
                log.error("Failed to move " + iuid, e);
                failedIUIDs.add(iuid);
            }
        }
        try {
            storeAssoc.release(true);
        } catch (Exception ignore) {}
        notifyMoveFinished();
    }

    private Dimse makeCStoreRQ(FileInfo info, int pcid) {
        Association assoc = storeAssoc.getAssociation();
        Command storeRqCmd = of.newCommand();
        storeRqCmd.initCStoreRQ(
            assoc.nextMsgID(),
            info.sopCUID,
            info.sopIUID,
            moveRqCmd.getInt(Tags.Priority, Command.MEDIUM));
        storeRqCmd.putUS(
            Tags.MoveOriginatorMessageID,
            moveRqCmd.getMessageID());
        storeRqCmd.putAE(
            Tags.MoveOriginatorAET,
            moveOriginatorAET);
        DataSource ds = new FileDataSource(info, buffer);
        return af.newDimse(pcid, storeRqCmd, ds);
    }

    private int remaining() {
        return fileInfoMap.size() - failedIUIDs.size() - warnings - completed;
    }

    private void notifyMovePending() {
        notifyMoveSCU(Status.Pending, null);
    }

    private void notifyMoveFinished() {
        final int status =
            canceled
                ? Status.Cancel
                : !failedIUIDs.isEmpty()
                ? Status.SubOpsOneOrMoreFailures
                : Status.Success;
        Dataset ds = null;
        if (!failedIUIDs.isEmpty()) {
            ds = of.newDataset();
            ds.putUI(
                Tags.FailedSOPInstanceUIDList,
                (String[]) failedIUIDs.toArray(new String[failedIUIDs.size()]));
        }
        notifyMoveSCU(status, ds);
    }

    private void notifyMoveSCU(int status, Dataset ds) {
        if (moveAssoc != null) {
            try {
                moveAssoc.getAssociation().write(
                    af.newDimse(movePcid, makeMoveRsp(status), ds));
            } catch (Exception e) {
                log.info("Failed to send Move RSP to Move Originator:", e);
                moveAssoc = null;
            }
        }
    }

    private Command makeMoveRsp(int status) {
        Command rspCmd = of.newCommand();
        rspCmd.initCMoveRSP(
            moveRqCmd.getMessageID(),
            moveRqCmd.getAffectedSOPClassUID(),
            status);
        if (status == Status.Cancel) {
            rspCmd.remove(Tags.NumberOfRemainingSubOperations);
        } else {
            rspCmd.putUS(Tags.NumberOfRemainingSubOperations, remaining());
        }
        rspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed);
        rspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings);
        rspCmd.putUS(Tags.NumberOfFailedSubOperations, failedIUIDs.size());
        rspCmd.putUS(Tags.Status, status);
        return rspCmd;
    }

    private Object[] selectFileAndPresCtx(ArrayList files) throws IOException {
        // prefer smallest file size
        Collections.sort(files, new Comparator() {
            public int compare(Object o1, Object o2) {
                long diffSize = ((FileInfo) o1).size - ((FileInfo) o2).size;
                return diffSize < 0 ? -1 : diffSize > 0 ? 1 : 0;
            }
        });
        int tsIndex = -1;
        FileInfo info = null;
        Association assoc = storeAssoc.getAssociation();
        for (Iterator it = files.iterator(); it.hasNext();) {
            info = (FileInfo) it.next();
            String[] compatibleTs = compatibleTs(info.tsUID);
            for (int i = 0; i < compatibleTs.length; i++) {
                PresContext pc =
                    assoc.getAcceptedPresContext(info.sopCUID, compatibleTs[i]);
                if (pc != null) {
                    //TODO implement forward MoveRQ to AE at other host
                    if (retrieveAET.equalsIgnoreCase(info.retrieveAET)) {
                        return new Object[] { info, pc };
                    }
                }
            }
        }
        throw new IOException(
            "No appropriate Presentation Context for SOP Class "
                + info.sopCUID
                + " accepted by "
                + aeInfo.getAETitle());
    }

    private String[] compatibleTs(String ts) {
        return (NATIVE_TS_AS_LIST.indexOf(ts) == -1) ? new String[] { ts }
        : NATIVE_TS;
    }
}
