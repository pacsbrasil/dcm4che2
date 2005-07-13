/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.web.maverick.model;


import org.dcm4che.dict.Tags;

/**
 * @author gunter.zeilinger@tiani.com
 * @version $Revision$ $Date$
 * @since 05.10.2004
 *
 */
public class StudyFilterModel extends AbstractModel {

	private String name; //hold orig input! in dataset append an asterix!
    public StudyFilterModel() {
    }

    public final String getPatientID() {
        return ds.getString(Tags.PatientID);
    }

    public final void setPatientID(String patientID) {
        ds.putLO(Tags.PatientID, patientID);
    }

    public final String getPatientName() {
        return name;
    }

    /**
     * Set patient name filter value.
     * <p>
     * Use auto wildcard match to get all patient beginning with given string.
     * <p>
     * This feature is only used if <code>patientName</code> doesn't already 
     * contain a wildcard caracter ('?' or '*')! 
     * 
     * @param patientName
     */
    public final void setPatientName(String patientName) {
    	name = patientName;
    	if ( patientName != null && 
    		 patientName.length() > 0 && 
			 patientName.indexOf('*') == -1 &&
			 patientName.indexOf('?') == -1) patientName+="*";
        ds.putPN(Tags.PatientName, patientName);
    }

    public final String getAccessionNumber() {
        return ds.getString(Tags.AccessionNumber);
    }

    public final void setAccessionNumber(String s) {
        ds.putSH(Tags.AccessionNumber, s);
    }

    public final String getStudyDateRange() {
        return getDateRange(Tags.StudyDate);
    }

    public final void setStudyDateRange(String s) {
        setDateRange(Tags.StudyDate, s);
    }

    public final String getStudyDescription() {
        return ds.getString(Tags.StudyDescription);
    }

    public final void setStudyDescription(String s) {
        ds.putLO(Tags.StudyDescription, s);
    }

    public final String getStudyID() {
        return ds.getString(Tags.StudyID);
    }

    public final void setStudyID(String s) {
        ds.putSH(Tags.StudyID, s);
    }
    
    public final void setStudyUID(String s) {
        ds.putUI(Tags.StudyInstanceUID, s);
    }

    public final String getModality() {
        return ds.getString(Tags.ModalitiesInStudy);
    }

    public final void setModality(String s) {
        ds.putCS(Tags.ModalitiesInStudy, s);
    }
    /**
     * Returns -1 because pk isnt use here.
     */
    public int getPk() {
    	return -1;
    }

}