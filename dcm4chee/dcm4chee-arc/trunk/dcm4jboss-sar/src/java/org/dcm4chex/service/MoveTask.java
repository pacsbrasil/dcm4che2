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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;
import org.dcm4che.net.PDU;
import org.dcm4chex.archive.ejb.jdbc.FileInfo;
import org.dcm4chex.service.util.LocalHost;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision$
 * @since 16.09.2003
 */
class MoveTask implements Runnable, DimseListener {

	private static final String[] NATIVE_TS =
		{ UIDs.ExplicitVRLittleEndian, UIDs.ImplicitVRLittleEndian };
	private static final List NATIVE_TS_AS_LIST = Arrays.asList(NATIVE_TS);

	private static final AssociationFactory af =
		AssociationFactory.getInstance();
	private static final DcmObjectFactory df = DcmObjectFactory.getInstance();

	private final DeviceInfo deviceInfo;
	private final String moveDest;
	private final NetworkAEInfo aeInfo;
	private final int movePcid;
	private final Command moveRspCmd;
	private ActiveAssociation moveAssoc;
	private final LinkedHashMap fileInfo = new LinkedHashMap();
	private final HashMap pcInfo = new HashMap();
	private final ArrayList failedIUIDs = new ArrayList();
	private int warnings = 0;
	private int completed = 0;
	private boolean canceled = false;
	private ActiveAssociation storeAssoc;

	public MoveTask(
		ActiveAssociation moveAssoc,
		int movePcid,
		Command moveRspCmd,
		FileInfo[] fileInfo,
		DeviceInfo deviceInfo,
		String moveDest)
		throws DcmServiceException {
		this.moveAssoc = moveAssoc;
		this.movePcid = movePcid;
		this.moveRspCmd = moveRspCmd;
		this.deviceInfo = deviceInfo;
		this.moveDest = moveDest;
		this.aeInfo = deviceInfo.getNetworkAE(moveDest);
		if (aeInfo == null) {
			throw new DcmServiceException(Status.ProcessingFailure);
		}
		for (int i = 0; i < fileInfo.length; i++) {
			putFileInfo(fileInfo[i]);
		}
		openAssociation();
		moveAssoc.addCancelListener(
			moveRspCmd.getMessageIDToBeingRespondedTo(),
			this);
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
		} catch (IOException e) {
		}
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
		ArrayList fileInfoForIUID = (ArrayList) fileInfo.get(info.sopIUID);
		if (fileInfoForIUID == null) {
			fileInfo.put(info.sopIUID, fileInfoForIUID = new ArrayList());
		}
		fileInfoForIUID.add(info);
		HashMap fileInfoForCUID = (HashMap) pcInfo.get(info.sopCUID);
		if (fileInfoForCUID == null) {
			pcInfo.put(info.sopCUID, fileInfoForCUID = new HashMap());
		}
		fileInfoForCUID.put(info.tsUID, info);
	}

	/* Implementation of CancelListener
	 */
	public void dimseReceived(Association assoc, Dimse dimse) {
		canceled = true;
	}

	public void run() {
		Iterator it = fileInfo.entrySet().iterator();
		while (!canceled && it.hasNext()) {
			if (moveAssoc != null) {
				notifyMovePending();
			}
			Map.Entry entry = (Entry) it.next();
			String iuid = (String) entry.getKey();
			try {
				Object[] fileAndTs =
					selectFileAndTs((ArrayList) entry.getValue());
				doMove((FileInfo) fileAndTs[0], (String) fileAndTs[1]);
			} catch (Exception e) {
				failedIUIDs.add(iuid);
			}
			it.remove();
		}
		try {
			storeAssoc.release(true);
		} catch (Exception ignore) {
		}
		if (moveAssoc != null) {
			notifyMoveFinished();
		}
	}

	private void notifyMovePending() {
		prepareMoveRsp(Status.Pending);
		notifyMoveSCU(null);
	}

	private void notifyMoveSCU(Dataset ds) {
		if (moveAssoc != null) {
			try {
				moveAssoc.getAssociation().write(
					af.newDimse(movePcid, moveRspCmd, ds));
			} catch (IOException e) {
				moveAssoc = null;
			}
		}
	}

	private void prepareMoveRsp(int status) {
		if (status == Status.Cancel) {
			moveRspCmd.remove(Tags.NumberOfRemainingSubOperations);
		} else {
			moveRspCmd.putUS(
				Tags.NumberOfRemainingSubOperations,
				fileInfo.size());
		}
		moveRspCmd.putUS(Tags.NumberOfCompletedSubOperations, completed);
		moveRspCmd.putUS(Tags.NumberOfWarningSubOperations, warnings);
		moveRspCmd.putUS(Tags.NumberOfFailedSubOperations, failedIUIDs.size());
		moveRspCmd.putUS(Tags.Status, status);
	}

	private void notifyMoveFinished() {
		prepareMoveRsp(
			canceled
				? Status.Cancel
				: !failedIUIDs.isEmpty()
				? Status.SubOpsOneOrMoreFailures
				: Status.Success);
		Dataset ds = null;
		if (!failedIUIDs.isEmpty()) {
			ds = df.newDataset();
			ds.putUI(
				Tags.FailedSOPInstanceUIDList,
				(String[]) failedIUIDs.toArray(new String[failedIUIDs.size()]));
		}
		notifyMoveSCU(ds);
	}

	private Object[] selectFileAndTs(ArrayList files) throws IOException {
		FileInfo file = (FileInfo) files.get(0);
		// there must be one entry!
		List acceptedTs =
			storeAssoc.getAssociation().listAcceptedPresContext(file.sopCUID);
		if (acceptedTs.isEmpty()) {
			throw new IOException(
				"No support of SOP Class "
					+ file.sopCUID
					+ " by "
					+ aeInfo.getAETitle());
		}
		// prefer smallest file size
		Collections.sort(files, new Comparator() {
			public int compare(Object o1, Object o2) {
				long diffSize = ((FileInfo) o1).size - ((FileInfo) o2).size;
				return diffSize < 0 ? -1 : diffSize > 0 ? 1 : 0;
			}
		});
		int tsIndex = -1;
		for (Iterator it = files.iterator(); it.hasNext();) {
			file = (FileInfo) it.next();
			if (!LocalHost.getHostName().equalsIgnoreCase(file.host)) {
				break;
			}
			String[] compatibleTs = compatibleTs(file.tsUID);
			for (int i = 0; i < compatibleTs.length; i++) {
				if (acceptedTs.contains(compatibleTs[i])) {
					return new Object[] { file, compatibleTs[i] };
				}
			}
		}
		throw new IOException(
			"No support of appropriate TranferSyntax for SOP Class "
				+ file.sopCUID
				+ " by "
				+ aeInfo.getAETitle());
	}

	private String[] compatibleTs(String ts) {
		return (NATIVE_TS_AS_LIST.indexOf(ts) == -1) ? new String[] { ts }
		: NATIVE_TS;
	}

	private void doMove(FileInfo info, String ts) throws IOException {
		InputStream in =
			new BufferedInputStream(new FileInputStream(info.toFile()));
		try {

		} finally {
			try {
				in.close();
			} catch (IOException ignore) {
			}
		}
	}

}
