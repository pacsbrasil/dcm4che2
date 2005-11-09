/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4chex.archive.notif;

import java.io.Serializable;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Nov 7, 2005
 */
public class SeriesStored implements Serializable {
	
	private static final long serialVersionUID = 3256442495306118960L;

	private String callingAET;

	private String calledAET;
	
	private String patientID;

	private String patientName;

	private String retrieveAET;

	private String accessionNumber;
	
	private String fsPath;
	
	private final Dataset ian;
	
	public SeriesStored() {
		ian = DcmObjectFactory.getInstance().newDataset();
		ian.putSQ(Tags.RefPPSSeq);
		DcmElement sq = ian.putSQ(Tags.RefSeriesSeq);
		sq.addNewItem().putSQ(Tags.RefSOPSeq);
	}

	public final Dataset getInstanceAvailabilityNotification() {
		return ian;
	}
	
	public final String getCalledAET() {
		return calledAET;
	}

	public final void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
	}

	public final String getCallingAET() {
		return callingAET;
	}

	public final void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}

	public final String getRetrieveAET() {
		return retrieveAET;
	}

	public final void setRetrieveAET(String retrieveAET) {
		this.retrieveAET = retrieveAET;
	}

	public final String getPatientID() {
		return patientID;
	}

	public final void setPatientID(String patientID) {
		this.patientID = patientID;
	}

	public final String getPatientName() {
		return patientName;
	}

	public final void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public final String getAccessionNumber() {
		return accessionNumber;
	}

	public final void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public final String getFileSystemPath() {
		return fsPath;
	}

	public final void setFileSystemPath(String fsPath) {
		this.fsPath = fsPath;
	}

	private Dataset getSeries() {
		return ian.getItem(Tags.RefSeriesSeq);
	}

	public String getStudyInstanceUID() {
		return ian.getString(Tags.StudyInstanceUID);
	}

	public void setStudyInstanceUID(String iuid) {
		ian.putUI(Tags.StudyInstanceUID, iuid);
	}

	public String getSeriesInstanceUID() {
		return  getSeries().getString(Tags.SeriesInstanceUID);
	}

	public void setSeriesInstanceUID(String iuid) {
		getSeries().putUI(Tags.SeriesInstanceUID, iuid);
	}

	public int getNumberOfInstances() {
		return getSeries().vm(Tags.RefSOPSeq);
	}

	public String getPPSInstanceUID() {
		return getPPS(Tags.RefSOPInstanceUID);
	}

	public String getPPSClassUID() {
		return getPPS(Tags.RefSOPClassUID);
	}

	private String getPPS(int tag) {
		Dataset pps = ian.getItem(Tags.RefPPSSeq);
		return pps != null ? pps.getString(tag) : null;
	}

	public void setRefPPS(String instanceUID, String classUID) {
		DcmElement sq = ian.putSQ(Tags.RefPPSSeq);
		Dataset item = sq.addNewItem();
		item.putUI(Tags.RefSOPInstanceUID, instanceUID);
		item.putUI(Tags.RefSOPClassUID, classUID);
		item.putSQ(Tags.PerformedWorkitemCodeSeq);
	}

	public DcmElement getRefSOPSeq() {
		return getSeries().get(Tags.RefSOPSeq);
	}
	
	public void addRefSOP(String instanceUID, String classUID) {
		Dataset item = getRefSOPSeq().addNewItem();
		item.putAE(Tags.RetrieveAET, retrieveAET);
		item.putCS(Tags.InstanceAvailability, "ONLINE");
		item.putUI(Tags.RefSOPInstanceUID, instanceUID);
		item.putUI(Tags.RefSOPClassUID, classUID);
	}
	
	
}
