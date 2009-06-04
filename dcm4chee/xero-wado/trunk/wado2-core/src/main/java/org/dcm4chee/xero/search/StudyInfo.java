package org.dcm4chee.xero.search;

import java.net.URL;
import java.util.HashMap;

import org.dcm4che2.data.DicomObject;

/** This is a regular hashmap with some added functions to enable easy access to 
 * some types of study cached information.
 * @author bwallace
 *
 */
@SuppressWarnings("serial")
public class StudyInfo extends HashMap<String,Object> {

	public StudyInfo(String studyUid) {
		this.put("studyUID", studyUid);
	}

	/** Indicate if the given action on this study has been audited yet. */
	public synchronized boolean isAudited(String userkey, String action) {
		return this.containsKey("audit://"+userkey+"/"+action);
	}
	
	/** Sets the given user key as having been audited */
	public synchronized void putAudited(String userkey,String action) {
		this.put("audit://"+userkey+"/"+action,Boolean.TRUE);
	}
	
	/** Finds the image location, if any */
	public synchronized URL getImageUrl(String sop) {
		URL ret = (URL) this.get("sop://"+sop);
		return ret;
	}
	
	/** Puts the image URL.
	 */
	public synchronized void putImageUrl(String sop, URL url) {
		this.put("sop://"+sop, url);
	}
	
	/** Gets the DicomObject for the given series, if loaded.  Ignores a series UID of 1
	 * as that is used to indicate unknown.
	 */
	public synchronized DicomObject getSeriesHeader(String seriesUid) {
		if( seriesUid.equals("1") ) return null;
		return (DicomObject) this.get("series://"+seriesUid);
	}
	
	/** Sets the DicomObject for the given series, as required */
	public synchronized void putSeriesHeader(String seriesUid, DicomObject series) {
		this.put("series://"+seriesUid, series);
	}

	public synchronized Object getStudyUID() {
		return (String) get("studyUID");
	}
}
