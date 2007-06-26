package org.dcm4chee.xero.wado;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/**
 * This class finds the location of the given dicom object - returns a URL.
 * @author bwallace
 *
 */
public class DicomFileLocationFilter implements Filter<URL> {

	public static final String DICOM_FILE_LOCATION = " DICOM_FILE_LOCATION";
	
	protected static final String wadoRequired[] = new String[]{
		"studyUID",
		"seriesUID", 
		"objectUID",
	};

	/** Figure out the location of the file, as referenced by SOP instance UID. */
	public URL filter(FilterItem filterItem, Map<String, Object> params) {
		URL ret = (URL) params.get(DICOM_FILE_LOCATION);
		if( ret!=null ) return ret;
		try {
			return new URL(createWadoUrl(params));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/** Figures out the URL to use for the WADO request
	 * @todo change the host to be configurable. */
	protected String createWadoUrl(Map<String, ?> args) {
		StringBuffer ret = new StringBuffer(
				"http://localhost:8080/wado?requestType=WADO&contentType=application%2Fdicom");
		for(String key : wadoRequired ) {
			Object value = args.get(key);
			String strValue = (value==null ? null : value.toString().trim());
			if( strValue==null || strValue.equals("")) {
				throw new IllegalArgumentException("Required value "+key+" is missing for request.");
			}
			ret.append("&").append(key).append("=").append(strValue);
		}
		return ret.toString();
	}

	@MetaData
	public int getPriority() {
		return -1;
	}
}
