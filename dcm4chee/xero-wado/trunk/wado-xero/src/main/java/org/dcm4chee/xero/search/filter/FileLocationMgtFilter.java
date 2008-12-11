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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.ObjectNotFoundException;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
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
   private static final Logger log = LoggerFactory
         .getLogger(FileLocationMgtFilter.class);

   boolean tryNext = false;
   
   /**
    * Returns the DICOM file <tt>URL</tt> for the given arguments.
    * <p>
    * Use the FileSystemMgt SessionBean to localize the DICOM file.
    * 
    * @param instanceUID
    *           Unique identifier of the instance.
    * 
    * @return The file URL or null if not found.
    * 
    * @throws IOException
    */
   protected URL getDICOMFileURL(String host, String port, String instanceUID) throws IOException {
      URL url = null;
      try {
          FileDTO[] dtos = FileSystemMgtResolver.getDTOs(host, port, instanceUID);
            tryNext = false;
            if (dtos == null || dtos.length == 0)
                return null; // not found!

            for (FileDTO dto : dtos) {
                if (dto.getDirectoryPath().startsWith("ftp://")
                        || dto.getDirectoryPath().startsWith("mvfhsm://"))
                    url = new URL(dto.getDirectoryPath() + "/" + dto.getFilePath());
                else
                    url = new URL("file:///" + dto.getDirectoryPath().replace("\\", "/") + "/" + dto.getFilePath());
                break;
            }

            log.info("URL: " + url);
      }
      catch(ObjectNotFoundException e) {
         log.warn("Object not available");
      }
      catch (Exception e) {
         log.error("Failed to get DICOM file URL, unknown reason:", e);
         tryNext = true;
      }
      return url;
   }

   /** Get the URL of the local file - may not be updated for DB changes etc */
   public URL filter(FilterItem<URL> filterItem, Map<String, Object> params) {
      Map<String,Object> aeMap = AEProperties.getAE(params);
      String type = (String) aeMap.get("type");
      if( type!=null && !type.equals("fileLocationMgt") ) {
         URL ret = filterItem.callNextFilter(params);
         log.debug("AE filter type {}, calling next filter, returned {}", type,ret);
         return ret;
      }
      log.debug("AE filter type {}", type);
      long start = System.nanoTime();
      
       
      if (params.get("ae") != null && AEProperties.getInstance().getAE((String)params.get("ae")) != null)   {
    	  String ae = (String)params.get("ae");
          aeMap = AEProperties.getInstance().getAE(ae);
      } else {
    	  aeMap = AEProperties.getInstance().getDefaultAE();
      }
      
      
      // This is only for dcm4chee fileLocation searches (This is the default, so null is okay).
      String pacsType = (String) aeMap.get("pacsType");
      if( pacsType != null && !pacsType.equalsIgnoreCase("dcm4chee")) {
    	  return filterItem.callNextFilter(params);
      }
      
      String objectUID = (String) params.get("objectUID");
      try {
         URL url = null;
         String hostname = (String)aeMap.get("host");
         Integer port = (Integer)aeMap.get("ejbport");

         String portStr = null;
         if ( port != null )
             portStr = port.toString();
             
         url = getDICOMFileURL(hostname, portStr, objectUID);
 
         if (url == null) {
            if( ! tryNext ) return null; 
            log.warn("File not found in local online cache.");
            return filterItem.callNextFilter(params);
         }

         int size = url.toString().length() * 2 + 64;
         params.put(MemoryCacheFilter.CACHE_SIZE, size);
         log.info("Time to read " + objectUID + " file location="
               + nanoTimeToString(System.nanoTime() - start) + " size of URL="
               + size);
         return url;
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         log.warn("Caught exception getting dicom file location:" + e, e);
         return filterItem.callNextFilter(params);
      }
   }

   /** Returns the URL of the local file for the given image bean */
   public static URL findImageBeanUrl(DicomObjectType dot, Filter<URL> filter,
         Map<String, Object> params) {
      Map<String, Object> newParams = new HashMap<String, Object>();
      newParams.put("objectUID", dot.getObjectUID());
      String queryStr = "objectUID='"+dot.getObjectUID()+"'";
      
      String ae = (String) params.get("ae");
      if(ae!=null ) {
    	  newParams.put("ae",ae);
    	  queryStr += ";ae="+ae;
      }
      
      newParams.put("queryStr",queryStr);
      
      String studyUid = (String) params.get("studyUID");
      if( studyUid != null ) newParams.put("studyUID", studyUid);
      
      String seriesUid = (String) params.get("seriesUID");
      if( seriesUid != null ) newParams.put("seriesUID", seriesUid);
      
      if ("true".equalsIgnoreCase((String) params
            .get(MemoryCacheFilter.NO_CACHE))) {
         newParams.put("no-cache", "true");
      }
      URL location = filter.filter(null, newParams);
      return location;
   }

   /**
    * Finds the location of the given object by calling the fileLocation filter.
    */
   public static URL filterURL(Filter<URL> filter, Map<String, Object> params,
         String uid) {
      Map<String, Object> newParams = new HashMap<String, Object>();

      // This request is used for filters where the request is for some other
      // objects, and the
      // UID is required.
      if ("true".equalsIgnoreCase((String) params
            .get(MemoryCacheFilter.NO_CACHE))) {
         newParams.put("no-cache", "true");
      }
      newParams.put("objectUID", uid);
      URL ret = filter.filter(null, newParams);
      return ret;
   }

   private Filter<URL> fileLocation;

   public Filter<URL> getFileLocation() {	   
      return fileLocation;
   }

   /**
    * Sets the file location filter, that knows how to find files.
    * 
    * @param fileLocation
    */
   @MetaData(out = "${ref:fileLocation}")
   public void setFileLocation(Filter<URL> fileLocation) {
      this.fileLocation = fileLocation;
   }

}
