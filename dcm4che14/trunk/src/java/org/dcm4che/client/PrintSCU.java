/*
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.client;

import java.io.File;
import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.util.UIDGenerator;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since Jun 21, 2003
 * @version $Revision$ $Date$
 *
 */
public class PrintSCU {
	
	static final AssociationFactory assocFact =
		AssociationFactory.getInstance();
	static final DcmObjectFactory dcmFact =
		DcmObjectFactory.getInstance();
	static final String[] TS_NO_EXPLICIT_VR = {
		UIDs.ImplicitVRLittleEndian
	};
	static final String[] TS_EXPLICIT_VR = {
		UIDs.ExplicitVRLittleEndian,
		UIDs.ImplicitVRLittleEndian
	};
	static final UIDGenerator UID_GEN =
		UIDGenerator.getInstance();
		
	private final AssociationRequestor requestor;
	private final PropertyChangeListener closeListener =
		new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {				
				requestor.removePropertyChangeListener(
					AssociationRequestor.CONNECTED, this);
				curFilmSessionIUID = null;
				curFilmBoxIUID = null;
				curPLUT_IUID = null;
				curFilmBox = null;
			}
		};

	private boolean negotiateColorPrint = true;
	private boolean negotiateGrayscalePrint = true;
	private boolean negotiatePLUT = true;	
	private boolean negotiateAnnotation = true;
	private String[] tsuids = TS_EXPLICIT_VR;
	private boolean createRQwithIUID = false;
	private boolean autoRefPLUT = true;
	
	private int pcidColorPrint = 0;
	private int pcidGrayscalePrint = 0;
	private int pcidPLUT = 0;
	private int pcidAnnotation = 0;
	private int pcidPrint = 0;

	private String curFilmSessionIUID;
	private String curFilmBoxIUID;
	private String curPLUT_IUID;
	private Dataset curFilmBox;
	private byte[] buffer = new byte[4096];
	
	public PrintSCU(AssociationRequestor requestor) {
		this.requestor = requestor;
		updatePresContexts();
	}
	
	private void updatePresContexts() {
		updateGrayscalePrintPresContext();
		updateColorPrintPresContext();
		updatePLUTPresContext();
		updateAnnotationPresContext();
	}

	private void updateGrayscalePrintPresContext() {
		if (negotiateGrayscalePrint) {
			if (pcidGrayscalePrint == 0) {
				pcidGrayscalePrint = requestor.addPresContext(
					UIDs.BasicGrayscalePrintManagement, tsuids);				
			}
		} else {
			if (pcidGrayscalePrint != 0) {
				requestor.removePresContext(pcidGrayscalePrint);
				pcidGrayscalePrint = 0;
			}
		}		
	}

	private void updateColorPrintPresContext() {
		if (negotiateColorPrint) {
			if (pcidColorPrint == 0) {
				pcidColorPrint = requestor.addPresContext(
						UIDs.BasicColorPrintManagement, tsuids);				
			}
		} else {
			if (pcidColorPrint != 0) {
				requestor.removePresContext(pcidColorPrint);
				pcidColorPrint = 0;
			}
		}
	}
	
	private void updatePLUTPresContext() {
		if (negotiatePLUT) {
			if (pcidPLUT == 0) {
                pcidPLUT = requestor.addPresContext( 
						UIDs.PresentationLUT, tsuids);				
			}
		} else {
			if (pcidPLUT != 0) {
				requestor.removePresContext(pcidPLUT);
				pcidPLUT = 0;
			}
		}
	}
	
	private void updateAnnotationPresContext() {
		if (negotiateAnnotation) {
			if (pcidAnnotation == 0) {
				requestor.addPresContext(
						UIDs.BasicAnnotationBox, tsuids);				
			}
		} else {
			if (pcidAnnotation != 0) {
				requestor.removePresContext(pcidAnnotation);
				pcidAnnotation = 0;
			}
		}
	}


	/**
	 * @return
	 */
	public boolean isNegotiateGrayscalePrint() {
		return negotiateGrayscalePrint;
	}

	/**
	 * @param negotiateGrayscalePrint
	 */
	public void setNegotiateGrayscalePrint(boolean negotiateGrayscalePrint) {
		this.negotiateGrayscalePrint = negotiateGrayscalePrint;
		updateGrayscalePrintPresContext();
	}

	/**
	 * @return
	 */
	public boolean isNegotiateColorPrint() {
		return negotiateColorPrint;
	}

	/**
	 * @param negotiateColorPrint
	 */
	public void setNegotiateColorPrint(boolean negotiateColorPrint) {
		this.negotiateColorPrint = negotiateColorPrint;
		updateColorPrintPresContext();
	}

	/**
	 * @return
	 */
	public boolean isNegotiatePLUT() {
		return negotiatePLUT;
	}

	/**
	 * @param negotiatePLUT
	 */
	public void setNegotiatePLUT(boolean negotiatePLUT) {
		this.negotiatePLUT = negotiatePLUT;
		updatePLUTPresContext();
	}

	/**
	 * @return
	 */
	public boolean isNegotiateAnnotation() {
		return negotiateAnnotation;
	}

	/**
	 * @param negotiateAnnotation
	 */
	public void setNegotiateAnnotation(boolean negotiateAnnotation) {
		this.negotiateAnnotation = negotiateAnnotation;
		updateAnnotationPresContext();
	}

	/**
	 * @return
	 */
	public boolean isCreateRQwithIUID() {
		return createRQwithIUID;
	}

	/**
	 * @param createRQwithIUID
	 */
	public void setCreateRQwithIUID(boolean createRQwithIUID) {
		this.createRQwithIUID = createRQwithIUID;
	}

	/**
	 * @return
	 */
	public boolean isAutoRefPLUT() {
		return autoRefPLUT;
	}

	/**
	 * @param createRQwithIUID
	 */
	public void setAutoRefPLUT(boolean autoRefPLUT) {
		this.autoRefPLUT = autoRefPLUT;
	}

	/**
	 * @return
	 */
	public AssociationRequestor getRequestor() {
		return requestor;
	}

	/**
	 * @return
	 */
	public boolean isNegotiateExplicitVR() {
		return tsuids == TS_EXPLICIT_VR;
	}

	/**
	 * @param negotiateExplicitVR
	 */
	public void setNegotiateExplicitVR(boolean negotiateExplicitVR) {
		if (isNegotiateExplicitVR() == negotiateExplicitVR) {
			return;
		}
		this.tsuids = negotiateExplicitVR
			? TS_EXPLICIT_VR
			: TS_NO_EXPLICIT_VR;
		removePresContexts();
		updatePresContexts();
	}
	
	private void removePresContexts() {
		if (pcidColorPrint != 0) {
			requestor.removePresContext(pcidColorPrint);
			pcidColorPrint = 0;
		}		
		if (pcidGrayscalePrint != 0) {
			requestor.removePresContext(pcidGrayscalePrint);
			pcidGrayscalePrint = 0;
		}		
		if (pcidPLUT != 0) {
			requestor.removePresContext(pcidPLUT);
			pcidPLUT = 0;
		}		
		if (pcidAnnotation != 0) {
			requestor.removePresContext(pcidAnnotation);
			pcidAnnotation = 0;
		}		
	}

	public boolean isGrayscalePrintEnabled() {
		return isEnabled(pcidGrayscalePrint);
	}
	
	private boolean isEnabled(int pcid) {
		return pcid != 0 && requestor.isConnected()
			&& requestor.getAcceptedTransferSyntaxUID(pcid) != null;
	}

	public boolean isColorPrintEnabled() {
		return isEnabled(pcidColorPrint);
	}
	
	public boolean isPLUTEnabled() {
		return isEnabled(pcidPLUT);
	}
	
	public boolean isAnnotationEnabled() {
		return isEnabled(pcidAnnotation);
	}
	

	private static int[] NO_ERROR_STATI = {
		Status.Success,
		Status.AttributeValueOutOfRange,
		Status.MinMaxDensityOutOfRange,
		Status.MemoryAllocationNotSupported
	};
	
	private int checkStatus(Command rsp) throws DcmServiceException {
		int status = rsp.getStatus();
		for (int i = 0; i < NO_ERROR_STATI.length; i++) {
			if (status == NO_ERROR_STATI[i]) {
				return status;
			}
		}
		throw new DcmServiceException(status,
		rsp.getString(Tags.ErrorComment));
	}
	
	
	public String createPLUT(Dataset attr) throws InterruptedException, IOException, DcmServiceException {
		String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
		int msgid = requestor.nextMsgID();
		Command nCreateRQ = dcmFact.newCommand();
		nCreateRQ.initNCreateRQ(msgid, UIDs.PresentationLUT, iuid);
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPLUT, nCreateRQ, attr);
		Command nCreateRSP = rsp.getCommand();
		int status = checkStatus(nCreateRSP);
		curPLUT_IUID = checkIUID(iuid, nCreateRSP);
		return iuid;
	}	

	public String createPLUT(String shape) throws InterruptedException, IOException, DcmServiceException {
		Dataset plut = dcmFact.newDataset();
		plut.putCS(Tags.PresentationLUTShape, shape);
		return createPLUT(plut);
	}

	private String checkIUID(String iuid, Command nCreateRSP) throws DcmServiceException {
		if (iuid == null) {
			iuid = nCreateRSP.getAffectedSOPInstanceUID();
			if (iuid == null) {
				throw new DcmServiceException(-1, "Missing Affected SOP Instance UID in N-CREATE-RSP" );
			}
		}
		return iuid;
	}

	public int deletePLUT(String iuid) throws InterruptedException, IOException, DcmServiceException {
		int msgid = requestor.nextMsgID();
		Command nDeleteRQ = dcmFact.newCommand();
		nDeleteRQ.initNDeleteRQ(msgid, UIDs.PresentationLUT, iuid);
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPLUT, nDeleteRQ);
		return checkStatus(rsp.getCommand());
	}
	
	public int createFilmSession(Dataset attr, boolean color) throws InterruptedException, IOException, DcmServiceException {
		pcidPrint = color
			? pcidColorPrint
			: pcidGrayscalePrint;
		String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
		int msgid = requestor.nextMsgID();
		Command nCreateRQ = dcmFact.newCommand();
		nCreateRQ.initNCreateRQ(msgid, UIDs.BasicFilmSession, iuid);
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPrint, nCreateRQ, attr);
		Command nCreateRSP = rsp.getCommand();
		int status = checkStatus(nCreateRSP);
		curFilmSessionIUID = checkIUID(iuid, nCreateRSP);
		return status;
	}
	
	private Dataset makeRefSOP(String cuid, String iuid) {
		Dataset refSOP = dcmFact.newDataset();
		refSOP.putUI(Tags.RefSOPClassUID, cuid);
		refSOP.putUI(Tags.RefSOPInstanceUID, iuid);
		return refSOP;
	}

	private void checkSession() {
		if (curFilmSessionIUID == null) {
			throw new IllegalStateException("No current Film Session");
		}
	}

	public int deleteFilmSession() throws InterruptedException, IOException, DcmServiceException {
		checkSession();
		int msgid = requestor.nextMsgID();
		Command nDeleteRQ = dcmFact.newCommand();
		nDeleteRQ.initNDeleteRQ(msgid,
			UIDs.BasicFilmSession, curFilmSessionIUID);
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPrint, nDeleteRQ);
		Command nDeleteRSP = rsp.getCommand();
		curFilmSessionIUID = null;
		curFilmBoxIUID = null;
		curFilmBox = null;
		return checkStatus(nDeleteRSP);
	}

	public int createFilmBox(Dataset attr) throws InterruptedException, IOException, DcmServiceException {
		checkSession();
		String iuid = createRQwithIUID ? UID_GEN.createUID() : null;
		int msgid = requestor.nextMsgID();
		Command nCreateRQ = dcmFact.newCommand();
		nCreateRQ.initNCreateRQ(msgid, UIDs.BasicFilmBoxSOP, iuid);
		attr.putSQ(Tags.RefFilmSessionSeq).addItem(
			makeRefSOP(UIDs.BasicFilmSession, curFilmSessionIUID));;
		if (autoRefPLUT && curPLUT_IUID != null
				&& attr.vm(Tags.RefPresentationLUTSeq) == -1) {
			attr.putSQ(Tags.RefPresentationLUTSeq).addItem(
				makeRefSOP(UIDs.PresentationLUT, curPLUT_IUID));
		}
		Dimse rsp = null;
		try {		
			rsp = requestor.invokeAndWaitForRSP(
				pcidPrint, nCreateRQ, attr);
		} finally {
			attr.remove(Tags.RefFilmSessionSeq);
		}
		Command nCreateRSP = rsp.getCommand();
		int status = checkStatus(nCreateRSP);
		curFilmBoxIUID = checkIUID(iuid, nCreateRSP);
		curFilmBox = rsp.getDataset();
		if (curFilmBox == null) {
			throw new DcmServiceException(-1, "Missing Attribute List in N-CREATE-RSP" );
		}
		requestor.addPropertyChangeListener(
			AssociationRequestor.CONNECTED, closeListener);
		return status;
	}

	private void checkFilmBox() {
		if (curFilmBox == null) {
			throw new IllegalStateException("No current Film Box");
		}
	}

	public int deleteFilmBox() throws InterruptedException, IOException, DcmServiceException {
		checkFilmBox();
		int msgid = requestor.nextMsgID();
		Command nDeleteRQ = dcmFact.newCommand();
		nDeleteRQ.initNDeleteRQ(msgid,
			UIDs.BasicFilmBoxSOP, curFilmBoxIUID);
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPrint, nDeleteRQ);
		Command nDeleteRSP = rsp.getCommand();
		curFilmBoxIUID = null;
		curFilmBox = null;
		return checkStatus(nDeleteRSP);
	}
	
	public int countImageBoxes() {
		checkFilmBox();
		return curFilmBox.vm(Tags.RefImageBoxSeq);
	}

	public int countAnnotationBoxes() {
		checkFilmBox();
		return curFilmBox.vm(Tags.RefBasicAnnotationBoxSeq);
	}
	
	public int setImageBox(int index, File file, Dataset attr) throws InterruptedException, IOException, DcmServiceException {
		if (index < 0 || index >= countImageBoxes()) {
			throw new IndexOutOfBoundsException(
				"index:" + index + ", count:" + countImageBoxes());
		}
		Dataset imageBox = dcmFact.newDataset();
		if (attr != null) {
			imageBox.putAll(attr);
		}
		imageBox.putUS(Tags.ImagePositionOnFilm, index+1);
			
		Dataset refImageBox = curFilmBox.getItem(Tags.RefImageBoxSeq, index);
		int msgid = requestor.nextMsgID();
		Command nSetRQ = dcmFact.newCommand();
		nSetRQ.initNSetRQ(msgid,
			refImageBox.getString(Tags.RefSOPClassUID),
			refImageBox.getString(Tags.RefSOPInstanceUID));
			
		Dimse rsp = requestor.invokeAndWaitForRSP(
			pcidPrint, nSetRQ, new PrintSCUDataSource(this, imageBox, file));
		return checkStatus(rsp.getCommand());
	}
		
	boolean isColorPrint() {
		return pcidPrint == pcidColorPrint;
	}
	
	byte[] getBuffer() {
		return buffer;		
	}
    
    public int printFilmBox()
        throws InterruptedException, IOException, DcmServiceException
    {
        checkFilmBox();
        int msgid = requestor.nextMsgID();
        Command nActionRQ = dcmFact.newCommand();
        nActionRQ.initNActionRQ(msgid,
            UIDs.BasicFilmBoxSOP, curFilmBoxIUID, 1);
        Dimse rsp = requestor.invokeAndWaitForRSP(
            pcidPrint, nActionRQ);
        Command nActionRSP = rsp.getCommand();
        return checkStatus(nActionRSP);
    }

    public int printFilmSession()
        throws InterruptedException, IOException, DcmServiceException
    {
        checkSession();
        int msgid = requestor.nextMsgID();
        Command nActionRQ = dcmFact.newCommand();
        nActionRQ.initNActionRQ(msgid,
            UIDs.BasicFilmSession, curFilmSessionIUID, 1);
        Dimse rsp = requestor.invokeAndWaitForRSP(
            pcidPrint, nActionRQ);
        Command nActionRSP = rsp.getCommand();
        return checkStatus(nActionRSP);
    }
}
