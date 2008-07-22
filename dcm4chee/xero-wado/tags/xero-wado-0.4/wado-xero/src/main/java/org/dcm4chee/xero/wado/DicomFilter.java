/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.dcm4chee.xero.wado;

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.filter.FileLocationMgtFilter;
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
public class DicomFilter implements Filter<DicomImageReader> {
   private static final Logger log = LoggerFactory.getLogger(DicomFilter.class);
   
   DicomImageReaderSpi dicomImageReaderSpi = new DicomImageReaderSpi();

   public static String PREFERRED_DICOM_READER = "org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader";

   private static final String DICOM_OBJECT_HEADER_KEY = " DICOM_OBJECT_HEADER";

   /**
     * Return either the params cached header, or read it from the file location
     * as appropriate. Returns a dicom image reader that should be synchronized
     * on before reading images, AND while using the dicom object.
     * 
     * @param filterItem -
     *            call the fileLocation filter to get the location.
     * @param
     */
   public DicomImageReader filter(FilterItem<DicomImageReader> filterItem, Map<String, Object> params) {
	  DicomImageReader ret = (DicomImageReader) params.get(DICOM_OBJECT_HEADER_KEY);
	  if (ret != null)
		 return ret;
	  URL location = FileLocationMgtFilter.filterURL(filterItem, params, null);
	  if (location == null)
		 return null;
	  long start = System.nanoTime();
	  try {
		 // Creating it directly rather than iterating over the ImageIO list means that multiple instances
		 // can be in memory at once, which is rather handy for a stand-alone WAR that can
		 // use either a provided version of the library or an included version.
		 DicomImageReader reader = (DicomImageReader) dicomImageReaderSpi.createReaderInstance();
		 ImageInputStream in = new ReopenableImageInputStream(location);
		 reader.setInput(in);
		 // We don't have any reliable size information right now.   
		 params.put(MemoryCacheFilter.CACHE_SIZE, 2048);
		 // Makes this a bit more thread safe if the header has been read
		 reader.getStreamMetadata();
		 log.info("Time to open "+params.get("objectUID")+" read meta-data "+nanoTimeToString(System.nanoTime() - start));
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
   public static int getPriority() {
	  return 25;
   }

   /**
    * Get just the image relevant attributes for this object, eg Bit Stored, LUT's etc - things required
    * for actual display of the object.
    * @param filterItem
    * @param params
    * @param uid
    * @return
    */
   public static DicomObject filterImageDicomObject(FilterItem filterItem, Map<String,Object> params, String uid) {
	  // When the filterDicomObject returns something else, then this can be updated to use the dicom object
	  // from the DicomImageReader or from some other location.
	  return filterDicomObject(filterItem,params,uid);
   }
   /**
     * Returns the complete DICOM header.  Use this for returning the the client or for complete access
     * to all dicom attributes.
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
     * Returns the DICOM image reader that allows frames and overlays to be read from the given object.
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
