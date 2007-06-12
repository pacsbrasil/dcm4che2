package org.dcm4chee.xero.wado;

import java.io.File;
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

	/** Figure out the location of the file, as referenced by SOP instance UID. */
	public URL filter(FilterItem filterItem, Map<String, Object> params) {
		URL ret = (URL) params.get(DICOM_FILE_LOCATION);
		if( ret==null ) {
			throw new UnsupportedOperationException("The reading of file locations hasn't yet been implemented.");
		}
		return ret;
	}
	
	@MetaData
	public int getPriority() {
		return -1;
	}
}
