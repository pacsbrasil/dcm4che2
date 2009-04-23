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
package org.dcm4chee.xero.dicom;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.wado.cmove.DicomURLStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating, parsing and manipulating DICOM URLs
 * @author Andrew Cowan (amidx)
 */
public class DicomURLHandler // Manipulator?
{
   private static Logger log = LoggerFactory.getLogger(DicomURLHandler.class);
   private final URLStreamHandler handler;

   public DicomURLHandler(URLStreamHandler handler)
   {
      this.handler = handler;
   }
   
   public DicomURLHandler()
   {
      this(new DicomURLStreamHandler());
   }

   /**
    * Create a new URL from the indicated URL string, with the appropriate
    * URLStreamHandler. 
    * @throws MalformedURLException
    */
   public URL createURL(String urlStr) throws MalformedURLException
   {
      return new URL(null,urlStr,handler);
   }
   
   /**
    * Create a dicom:// URL from the filter parameters.
    * @throws MalformedURLException 
    */
   public URL createURL(Map<String, Object> filterParams) throws MalformedURLException
   {
      Map<String,Object> ae = AEProperties.getAE(filterParams);

      String name = (String) ae.get(AEProperties.AE_PROPERTY_NAME);
      String host = (String)ae.get(AEProperties.AE_HOST_KEY);
      int port = (Integer)ae.get(AEProperties.AE_PORT_KEY);
      
      String aeAndHost = String.format("%s@%s", name,host);
      String queryStr = extractQueryStr(filterParams);
      
      return new URL("dicom",aeAndHost,port,"/"+queryStr,handler);
   }

   private String extractQueryStr(Map<String, Object> filterParams)
   {
      String objectUID = FilterUtil.getString(filterParams, "objectUID");
      String seriesUID = FilterUtil.getString(filterParams, "seriesUID");
      String studyUID = FilterUtil.getString(filterParams, "studyUID");
      
      StringBuilder sb = new StringBuilder();
      
      if(objectUID != null)
         appendQueryParam(sb,"objectUID",objectUID);
      
      if(seriesUID!=null)
         appendQueryParam(sb,"seriesUID",seriesUID);
      
      if(studyUID!=null)
         appendQueryParam(sb,"studyUID",studyUID);
      
      // Put a ? at the front of the query string if necessary
      if(sb.length() > 0 && sb.charAt(0) == '&')
         sb.setCharAt(0, '?');
      
      return sb.toString();
      }
      
   
   /**
    * Append a new parameter to a query String:
    * <p>
    * &paramName=paramValue
    */
   private void appendQueryParam(StringBuilder sb,String paramName,String paramValue)
      {
         sb.append('&');
      sb.append(paramName);
      sb.append('=');
      sb.append(paramValue);
   }

   /** Figures out whtat query level to use based on what is available in the query string */
   public static String chooseLevel(boolean hasStudy, boolean hasSeries, boolean hasImage, String level) {
	   if( !(hasStudy||hasSeries||hasImage) ) throw new IllegalArgumentException("No UID specified - not retrievable.");
	   if(!hasStudy) log.warn("Shouldn't query without STUDY.");
	   if( level.equals("STUDY") ) {
		   if( hasStudy ) return level;
		   log.warn("Can't query at study level because no study UID specified.");
		   if( hasSeries ) return "SERIES";
		   return "IMAGE";
	   }
	   if( level.equals("SERIES") ) {
		   if( hasSeries ) return level;
		   log.warn("Trying to query at the SERIES level, but no SERIES UID specified.");
		   if( hasStudy ) return "STUDY";
		   return "IMAGE";
	   }
	   if( hasImage ) {
		   if( !hasSeries ) log.warn("No series level UID provided - not necessarily safe to retrieve at image level");
		   return level;
	   }
	   if( hasStudy ) return "STUDY";
	   return "SERIES";
   }


   /**
    * Create a DICOM query object based on the arguments of the indicated DICOM URL.
    */
   public DicomObject createDicomQuery(URL dicomURL, String level)
   {
      Map<String,String> parameters = parseQueryParameters(dicomURL);
      
      boolean hasStudy = parameters.containsKey("studyUID");
      boolean hasSeries = parameters.containsKey("seriesUID");
      boolean hasObject =parameters.containsKey("objectUID"); 
      level = chooseLevel(hasStudy, hasSeries, hasObject, level);

      // VR of null will lookup the VR of the Tag.
      DicomObject dcm = new BasicDicomObject();
      if(hasStudy)
         dcm.putString(Tag.StudyInstanceUID, null, parameters.get("studyUID"));
      
      if (!level.equals("STUDY")) {
			if (hasSeries)
				dcm.putString(Tag.SeriesInstanceUID, null, parameters
						.get("seriesUID"));

			if (hasObject && !level.equals("SERIES"))
				dcm.putString(Tag.SOPInstanceUID, null, parameters
						.get("objectUID"));
		}
      dcm.putString(Tag.QueryRetrieveLevel, VR.CS, level);

      return dcm;
   }

   /**
    * Parse the AE title out of the DICOM URL or null if none is defined.
    */
   public static String parseAETitle(URL dicomURL)
   {
      if(dicomURL == null)
         return null;
      
      String[] aeAndHost = dicomURL.getAuthority().split("@");
      if(aeAndHost.length == 2)
         return aeAndHost[0];
      else
         return null;
   }
   
   public static Map<String,String> parseQueryParameters(URL dicomURL)
   {
      String queryStr = dicomURL.getQuery();
      if(queryStr == null || queryStr.trim().length()==0)
         return Collections.emptyMap();
      
      // ?studyUID={}&seriesUID={}&objectUID={}
      Map<String,String> parameters = new HashMap<String,String>();

      // Trim leading '?'
      for(String paramStr : queryStr.split("&"))
      {
         String[] keyAndValue = paramStr.split("=");
         if(keyAndValue.length == 2)
         {
            parameters.put(keyAndValue[0], keyAndValue[1]);
         }
      }
      
      return parameters;
   }
   
}
