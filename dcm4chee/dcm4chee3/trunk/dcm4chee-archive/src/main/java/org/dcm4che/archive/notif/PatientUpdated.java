/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che.archive.notif;

import java.io.Serializable;

/**
 * @author franz.willer@gwi-ag.com
 * @version $Revision: 1.1 $ $Date: 2007/06/12 21:03:20 $
 * @since Nov 9, 2005
 */
public class PatientUpdated implements Serializable {
	
	private static final long serialVersionUID = 3689069551690199604L;

	private String patientID;

	private String description;

	private String retrieveAET;

	public PatientUpdated(String patientID, String description, String retrieveAET) {
		this.patientID = patientID;
		this.description = description;
		this.retrieveAET = retrieveAET;
		
	}

	/**
	 * @return Returns the patientID.
	 */
	public String getPatientID() {
		return patientID;
	}
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return Returns the retrieveAET.
	 */
	public String getRetrieveAET() {
		return retrieveAET;
	}
}
