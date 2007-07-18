package org.dcm4chee.xero.wado;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.CacheItemImpl;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a dicom reader, usable for reading dicom objects and returning a "DicomObject"
 * instance.  By default it only returns header data.  Be very careful what you read when
 * considering reading image data - a large multi-frame may not fit into 32 bits worth of memory.
 * 
 * @author bwallace
 * 
 */
public class DicomFilter implements Filter<DicomObject> {
	private static final Logger log = LoggerFactory.getLogger(DicomFilter.class);
	
	public static final String DICOM_FULL_READ = " DICOM_FULL_READ";

	private static final String DICOM_OBJECT_HEADER_KEY = " DICOM_OBJECT_HEADER";
	private static final String DICOM_OBJECT_KEY = " DICOM_OBJECT";


	private static final StopTagInputHandler stopHandler = new StopTagInputHandler(
			Tag.PixelData);

	/** Return either the params cached header, or read it from the file location
	 * as appropriate.
	 * @param filterItem - call the fileLocation filter to get the location.
	 * @param
	 */
	public DicomObject filter(FilterItem filterItem, Map<String, Object> params) {
		String fullReadStr = (String) params.get(DICOM_FULL_READ);
		boolean fullRead = false;
		if( fullReadStr!=null ) fullRead = Boolean.parseBoolean(fullReadStr);
		String key = fullRead ? DICOM_OBJECT_KEY : DICOM_OBJECT_HEADER_KEY;
		DicomObject ret = (DicomObject) params.get(key);
		if (ret != null)
			return ret;
		URL location = (URL) filterItem.callNamedFilter("fileLocation",
				params);
		if (location == null)
			return null;
		try {
			DicomInputStream dis = new DicomInputStream(location.openStream());
			if( !fullRead ) dis.setHandler(stopHandler);
			ret = dis.readDicomObject();
			params.put(key, ret);
			params.put(CacheItemImpl.CACHE_SIZE, Long.toString(ret.size()*10) );
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
