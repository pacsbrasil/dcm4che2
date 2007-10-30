package org.dcm4chee.xero.wado;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.CacheItemImpl;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a dicom reader, usable for reading dicom objects and returning
 * a "DicomObject" instance. By default it only returns header data. Be very
 * careful what you read when considering reading image data - a large
 * multi-frame may not fit into 32 bits worth of memory.
 * 
 * @author bwallace
 * 
 */
public class DicomFilter implements Filter<Object> {
   private static final Logger log = LoggerFactory.getLogger(DicomFilter.class);

   public static String PREFERRED_DICOM_READER = "org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader";

   private static final String DICOM_OBJECT_HEADER_KEY = " DICOM_OBJECT_HEADER";

   /**
     * Return either the params cached header, or read it from the file location
     * as appropriate. Returns a dicom image reader that should be synchronized
     * on before reading images. The reader should probably be cached rather
     * than creating a new one each time.
     * 
     * @param filterItem -
     *            call the fileLocation filter to get the location.
     * @param
     */
   public Object filter(FilterItem filterItem, Map<String, Object> params) {
	  DicomImageReader ret = (DicomImageReader) params.get(DICOM_OBJECT_HEADER_KEY);
	  if (ret != null)
		 return ret;
	  URL location = (URL) filterItem.callNamedFilter("fileLocation", params);
	  if (location == null)
		 return null;
	  try {
		 Iterator it = ImageIO.getImageReadersByFormatName("DICOM");
		 if (!it.hasNext())
			throw new UnsupportedOperationException("The DICOM image I/O filter must be available to read images.");
		 log.info("Found DICOM image reader - trying to read image now.");
		 ImageReader reader = (ImageReader) it.next();
		 while (it.hasNext() && !PREFERRED_DICOM_READER.equals(reader.getClass().getName())) {
			reader = (ImageReader) it.next();
		 }
		 if (!PREFERRED_DICOM_READER.equals(reader.getClass().getName())) {
			throw new UnsupportedOperationException("Couldn't find image reader class " + PREFERRED_DICOM_READER);
		 }
		 String surl = location.toString();
		 ImageInputStream in;
		 if (surl.startsWith("file:")) {
			String fileName = location.getFile();
			log.info("Reading DICOM image from local cache file " + surl);
			in = new FileImageInputStream(new File(fileName));
		 } else {
			// TODO change to FileCacheInputStream once we can configure the
			// location.
			log.info("Reading DICOM image from remote WADO url:" + surl);
			in = new MemoryCacheImageInputStream(location.openStream());
		 }
		 reader.setInput(in);
		 DicomObject dobj = ((DicomStreamMetaData) reader.getStreamMetadata()).getDicomObject();
		 log.info("Dicom object "+dobj.getString(Tag.SOPInstanceUID)+ " size is "+dobj.size());
		 params.put(CacheItemImpl.CACHE_SIZE, Long.toString(1024+dobj.size()));
		 return reader;
	  } catch (IOException e) {
		 log.warn("Can't read sop instance " + params.get("objectUID") + " at " + location + " exception:" + e);
	  }
	  // This might happen if the instance UID under request comes from
	  // another
	  // system and needs to be read in another way.
	  if (ret == null)
		 return filterItem.callNextFilter(params);
	  return ret;
   }

   /** Get the default priority for the dicom header filter. */
   @MetaData
   public int getPriority() {
	  return 25;
   }

   /**
     * Returns the DICOM header read from a filter named "dicom".
     * 
     * @param filterItem
     * @param params
     * @param uid -
     *            can be null, if so, then the uid will be read from the
     *            existing params.
     * @return
     */
   public static DicomObject filterDicomObject(FilterItem filterItem, Map<String, Object> params, String uid) {
	  // This might come from a different series or even study, so don't
	  // assume anything here.
	  Object ret = callInstanceFilter(filterItem, params, uid);
	  if (ret == null)
		 return null;
	  if (ret instanceof DicomObject)
		 return (DicomObject) ret;
	  if (ret instanceof DicomImageReader) {
		 DicomStreamMetaData streamData;
		 try {
			streamData = (DicomStreamMetaData) ((DicomImageReader) ret).getStreamMetadata();
		 } catch (IOException e) {
			log.error("Unable to reader dicom header:" + e);
			return null;
		 }
		 DicomObject ds = streamData.getDicomObject();
		 return ds;
	  }
	  log.warn("Unknown return type " + ret.getClass().getName());
	  return null;
   }

   private static Object callInstanceFilter(FilterItem filterItem, Map<String, Object> params, String uid) {
	  Map<String, Object> newParams;
	  if (uid == null) {
		 // This case is used for filters where the request is directly for as single instance 
		 // object.
		 newParams = params;
	  } else {
		 // This request is used for filters where the request is for some other objects, and the
		 // UID is required.
		 newParams = new HashMap<String, Object>();
		 newParams.put("objectUID", uid);
	  }
	  Object ret = filterItem.callNamedFilter("dicom", newParams);
	  return ret;
   }

   /**
     * Returns the DICOM image reader from a filter named "dicom".
     * 
     * @param filterItem
     * @param params
     * @param uid
     * @return
     */
   public static DicomImageReader filterDicomImageReader(FilterItem filterItem, Map<String, Object> params, String uid) {
	  // This might come from a different series or even study, so don't
	  // assume anything here.
	  Object ret = callInstanceFilter(filterItem, params, uid);
	  if (ret == null)
		 return null;
	  if (ret instanceof DicomObject)
		 return null;
	  if (ret instanceof DicomImageReader) {
		 return (DicomImageReader) ret;
	  }
	  log.warn("Unknown return type " + ret.getClass().getName());
	  return null;
   }
}
