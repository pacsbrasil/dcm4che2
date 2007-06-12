package org.dcm4chee.xero.wado;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a dicom header reader, usable for reading image headers, SR
 * objects, GSPS and other related objects.
 * 
 * @author bwallace
 * 
 */
public class DicomHeaderFilter implements Filter<DicomObject> {
	public static final Logger log = LoggerFactory.getLogger(DicomHeaderFilter.class);

	private static final String DICOM_OBJECT_HEADER_KEY = " DICOM_OBJECT_HEADER";

	private static final StopTagInputHandler stopHandler = new StopTagInputHandler(
			Tag.PixelData);

	/** Return either the params cached header, or read it from the file location
	 * as appropriate.
	 * @param filterItem - call the fileLocation filter to get the location.
	 * @param
	 */
	public DicomObject filter(FilterItem filterItem, Map<String, Object> params) {
		DicomObject ret = (DicomObject) params.get(DICOM_OBJECT_HEADER_KEY);
		if (ret != null)
			return ret;
		File location = (File) filterItem.callNamedFilter("fileLocation",
				params);
		if (location == null)
			return null;
		try {
			DicomInputStream dis = new DicomInputStream(location);
			dis.setHandler(stopHandler);
			ret = dis.readDicomObject();
			params.put(DICOM_OBJECT_HEADER_KEY, ret);			
		} catch (IOException e) {
			log.warn("Can't read sop instance "+params.get("objectUID"));
		}
		// This might happen if the instance UID under request comes from another
		// system and needs to be read in another way.
		if( ret==null ) return (DicomObject) filterItem.callNextFilter(params);
		return ret;
	}
	
	/** Get the default priority for the dicom header filter. */
	@MetaData
	public int getPriority() {
		return 25;
	}

}
