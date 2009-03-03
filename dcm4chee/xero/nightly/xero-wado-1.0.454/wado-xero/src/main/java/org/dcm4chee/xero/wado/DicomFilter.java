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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.stream.ImageInputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.study.DicomObjectInterface;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.StudyBean;
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

	/**
	 * Return either the params cached header, or read it from the file location
	 * as appropriate. Returns a dicom image reader that should be synchronized
	 * on before reading images, AND while using the dicom object.
	 * 
	 * @param filterItem -
	 *           call the fileLocation filter to get the location.
	 * @param
	 */
	public DicomImageReader filter(FilterItem<DicomImageReader> filterItem, Map<String, Object> params) {
		URL location = fileLocation.filter(null, params);
		if (location == null)
			return null;
		long start = System.nanoTime();
		try {
			// Creating it directly rather than iterating over the ImageIO list
			// means that multiple instances
			// can be in memory at once, which is rather handy for a stand-alone
			// WAR that can
			// use either a provided version of the library or an included version.
			DicomImageReader reader = (DicomImageReader) dicomImageReaderSpi.createReaderInstance();
			ImageInputStream in = new ReopenableImageInputStream(location);
			reader.setInput(in);
			// Size is from visualvm size of object + sub-objects, roughly
			params.put(MemoryCacheFilter.CACHE_SIZE, 20480);
			// Makes this a bit more thread safe if the header has been read, then
			// it will
			// be safe to get the stream meta-data.
			reader.getStreamMetadata();
			log.info("Time to open " + params.get("objectUID") + " read meta-data "
			      + nanoTimeToString(System.nanoTime() - start));
			return reader;
		} catch (IOException e) {
			log.warn("Can't read sop instance " + params.get("objectUID") + " at " + location,e);
		}
		// This might happen if the instance UID under request comes from
		// another system and needs to be read in another way.
		return filterItem.callNextFilter(params);
	}

	/** Get the default priority for the dicom header filter. */
	@MetaData
	public static int getPriority() {
		return 25;
	}

	/**
	 * Make a call to fetch a UID from the same AE and with header update on or
	 * off as setup in the request, but not necessarily the same study or series.
	 * 
	 * @param filter
	 * @param params
	 * @param uid
	 * @return
	 */
	public static DicomObject callInstanceFilter(Filter<DicomObject> filter, Map<String, Object> params, String uid) {
		Map<String, Object> newParams = new HashMap<String, Object>();
		// This request is used for filters where the request is for some other
		// objects, and the
		// UID is required.
		newParams.put("objectUID", uid);
		if( params!=null ) {
			if (params.containsKey(DicomUpdateFilter.UPDATE_HEADER)) {
				newParams.put(DicomUpdateFilter.UPDATE_HEADER, "TRUE");
			}
			String ae = (String) params.get(WadoParams.AE);
			if(ae!=null ) newParams.put(WadoParams.AE,ae);
		}
		DicomObject ret = filter.filter(null, newParams);
		return ret;
	}

   /**
    * Retrieve the DicomObject that represents the full DICOM header for the indicated
    * DicomObjectType instance.
    */
   public static DicomObject callInstanceFilter(Filter<DicomObject> filter, DicomObjectInterface dot,String aeTitle)
   {
      Map<String, Object> params = new HashMap<String, Object>();
      
      if(aeTitle != null)
         params.put(WadoParams.AE, aeTitle);
      
      params.put(WadoParams.OBJECT_UID, dot.getObjectUID());      
      SeriesBean seriesBean =  dot.getSeriesBean();;
      if(seriesBean != null)
      {
         params.put("seriesUID", seriesBean.getSeriesUID());
         StudyBean studyBean = seriesBean.getStudyBean();
         if(studyBean != null)
         {
            params.put("studyUID", studyBean.getStudyUID());
         }
            
      }
      return filter.filter(null,params);
   }

   private Filter<URL> fileLocation;
   
   public Filter<URL> getFileLocation() {
   	return fileLocation;
   }

	/**
	 * Sets the file location filter, that knows how to find files.
	 * @param fileLocation
	 */
	@MetaData(out="${ref:fileLocation}")
	public void setFileLocation(Filter<URL> fileLocation) {
   	this.fileLocation = fileLocation;
   }

}
