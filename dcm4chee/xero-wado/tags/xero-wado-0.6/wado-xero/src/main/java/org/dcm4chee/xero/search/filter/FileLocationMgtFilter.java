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
package org.dcm4chee.xero.search.filter;

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter knows how to get the raw file location URL, from the file system
 * management name instance.
 * 
 * @author bwallace
 * 
 */
public class FileLocationMgtFilter implements Filter<URL> {
   private static final Logger log = LoggerFactory.getLogger(FileLocationMgtFilter.class);

   private ObjectName fileSystemMgtName;

   private static MBeanServer server;

   public FileLocationMgtFilter() {
	  try {
		 fileSystemMgtName = new ObjectName("dcm4chee.archive:service=FileSystemMgt");
	  } catch (MalformedObjectNameException e) {
		 e.printStackTrace();
		 fileSystemMgtName = null;
	  } catch (NullPointerException e) {
		 fileSystemMgtName = null;
		 e.printStackTrace();
	  }
	  server = MBeanServerLocator.locate();
   }

   /**
     * Returns the DICOM file for given arguments.
     * <p>
     * Use the FileSystemMgtService MBean to localize the DICOM file.
     * 
     * @param studyUID
     *            Unique identifier of the study.
     * @param seriesUID
     *            Unique identifier of the series.
     * @param instanceUID
     *            Unique identifier of the instance.
     * 
     * @return The File object or null if not found.
     * 
     * @throws IOException
     */
   protected File getDICOMFile(String instanceUID) throws IOException {
	  Object dicomObject = null;
	  try {
		 dicomObject = server.invoke(fileSystemMgtName, "locateInstance", new Object[] { instanceUID }, new String[] { String.class
			   .getName() });

	  } catch (Exception e) {
		 log.error("Failed to get DICOM file", e);
	  }
	  if (dicomObject == null)
		 return null; // not found!
	  if (dicomObject instanceof File)
		 return (File) dicomObject; // We have the File!
	  if (dicomObject instanceof String) {
		 throw new RuntimeException("Need redirection?:" + (String) dicomObject);
	  }
	  return null;
   }

   /** Get the URL of the local file - may not be updated for DB changes etc */
   public URL filter(FilterItem<URL> filterItem, Map<String, Object> params) {
	  if (fileSystemMgtName == null || server == null)
		 return filterItem.callNextFilter(params);
	  long start = System.nanoTime();
	  String objectUID = (String) params.get("objectUID");
	  File f;
	  try {
		 f = getDICOMFile(objectUID);
		 if (f == null) {
			log.warn("File not found in local online cache.");
			return filterItem.callNextFilter(params);
		 }
		 URL url = f.toURI().toURL();
		 int size = url.toString().length()*2 + 64;
		 params.put(MemoryCacheFilter.CACHE_SIZE, size);
		 log.info("Time to read "+objectUID+" file location="+nanoTimeToString(System.nanoTime() - start)+" size of URL="+size);
		 return url;
	  } catch (RuntimeException e) {
		 throw e;
	  } catch (Exception e) {
		 log.warn("Caught exception getting dicom file location:" + e, e);
		 return filterItem.callNextFilter(params);
	  }
   }

   /** Returns the URL of the local file for the given image bean */
   public static URL findImageBeanUrl(DicomObjectType dot, FilterItem<?> filterItem, Map<String, Object> params) {
	  Map<String, Object> newParams = new HashMap<String, Object>();
	  newParams.put("objectUID", dot.getObjectUID());
	  if ("true".equalsIgnoreCase((String) params.get(MemoryCacheFilter.NO_CACHE))) {
		 newParams.put("no-cache", "true");
	  }
	  URL location = (URL) filterItem.callNamedFilter("fileLocation", newParams);
	  return location;
   }

   /** Finds the location of the given object by calling the fileLocation filter. */
   public static URL filterURL(FilterItem<?> filterItem, Map<String, Object> params, String uid) {
	  Map<String, Object> newParams;
	  if (uid == null) {
		 // This case is used for filters where the request is directly for as single instance 
		 // object.
		 newParams = params;
	  } else {
		 // This request is used for filters where the request is for some other objects, and the
		 // UID is required.
		 newParams = new HashMap<String, Object>();
		 if( "true".equalsIgnoreCase((String) params.get(MemoryCacheFilter.NO_CACHE))) {
			newParams.put("no-cache", "true");
		 }
		 newParams.put("objectUID", uid);
	  }
	  Object ret = filterItem.callNamedFilter("fileLocation", newParams);
	  return (URL) ret;
   }

}
