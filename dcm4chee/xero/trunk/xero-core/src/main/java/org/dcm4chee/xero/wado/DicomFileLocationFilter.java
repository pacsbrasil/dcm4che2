package org.dcm4chee.xero.wado;

import java.io.File;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;

/**
 * This class finds the location of the given dicom FILE.
 * @author bwallace
 *
 */
public class DicomFileLocationFilter implements Filter<File> {

	public static final String DICOM_FILE_LOCATION = " DICOM_FILE_LOCATION";

	/** Figure out the location of the file, as referenced by SOP instance UID. */
	public File filter(FilterItem filterItem, Map<String, Object> params) {
		File ret = (File) params.get(DICOM_FILE_LOCATION);
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
