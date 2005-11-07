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
import java.util.ArrayList;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since Nov 7, 2005
 */
public class SeriesStored implements Serializable {
	
	private static final long serialVersionUID = 3690755090114032947L;

	private String callingAET;

	private String calledAET;
	
	private String retrieveAET;
	
	private String patientID;

	private String patientName;

	private String accessionNumber;
	
	private String studyIUID;

	private String seriesIUID;

	private String modality;
	
	private String ppsCUID;

	private String ppsIUID;
	
	private String fsPath;

	private final ArrayList iuids = new ArrayList();

	private final ArrayList cuids = new ArrayList();

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

	public final String getFileSystemPath() {
		return fsPath;
	}

	public final void setFileSystemPath(String fsPath) {
		this.fsPath = fsPath;
	}

	public final String getModality() {
		return modality;
	}

	public final void setModality(String modality) {
		this.modality = modality;
	}

	public final String getRefPpsSOPInstanceUID(String iuid) {
		return ppsIUID;
	}

	public final void setRefPpsSOPInstanceUID(String iuid) {
		ppsIUID = iuid;
	}

	public final String getRefPpsSOPClassUID(String cuid) {
		return ppsCUID;
	}

	public final void setRefPpsSOPClassUID(String cuid) {
		ppsCUID = cuid;
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

	public final String getSeriesInstanceUID() {
		return seriesIUID;
	}

	public final void setSeriesInstanceUID(String seriesIUID) {
		this.seriesIUID = seriesIUID;
	}

	public final String getStudyInstanceUID() {
		return studyIUID;
	}

	public final void setStudyInstanceUID(String studyIUID) {
		this.studyIUID = studyIUID;
	}
	
	public void addSOP(String iuid, String cuid) {
		iuids.add(iuid);
		cuids.add(cuid);
	}
	
	public String getSOPInstanceUID(int index) {
		return (String) iuids.get(index);
	}

	public String getSOPClassUID(int index) {
		return (String) cuids.get(index);
	}
	
	public int getNumberOfInstances() {
		return iuids.size();
	}

}
