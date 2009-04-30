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
 * Dave Smith & Laura Peters, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Laura Peters <laura.peters@agfa.com>
 * David Smith <david.smith@agfa.com>
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chex.archive.ejb.interfaces.FileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Generates URLs for file locations.
 * 
 * @author lpeters, dsmith1547
 */
public class FileLocationURLFilter implements Filter<URL> {
	public static Logger log = LoggerFactory.getLogger(FileLocationURLFilter.class);
	
	public static final String RELATIVE_FILE_PREFIX = "../server/default/";
	public static final String FILE_PREFIX = "filePrefix";
	public static final String FILE_DTO = "FileDTO";

	public URL filter(FilterItem<URL> filterItem, Map<String,Object> params) {
		FileDTO dto = (FileDTO)params.get(FILE_DTO);
		if (dto == null) 
			throw new IllegalArgumentException("No dto passed in params");
		URL url = null;
		String directoryPath = dto.getDirectoryPath().replace("\\", "/");
		String filePath = dto.getFilePath().replace("\\", "/"); 
		try {
		   String ae = FilterUtil.getString(params, "ae","local");
		   String filePrefix = getPrefixForAE(ae);
		   if(filePrefix != null)
		      directoryPath = filePrefix;
		   
		   String fullyQualifiedPath = combinePath(directoryPath,filePath,'/'); 
		   
		   // AC:  Not sure whether any sort of differentiation matters at this point...
	      int colon = fullyQualifiedPath.indexOf(":");
         if (colon == -1) {
            if (fullyQualifiedPath.charAt(0) == '/')
               url = new URL("file://" + fullyQualifiedPath);
            else 
               url = new URL("file:" + RELATIVE_FILE_PREFIX + fullyQualifiedPath);
         }
         else if (colon == 1) {
            url = new URL("file:///" + fullyQualifiedPath);
         }
		      
		    // Single letter drive paths are fine, as is no url indicator type

		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Unable to compose URL for file path" + filePath, e);
		}
		if ( log.isDebugEnabled() )
			log.debug("Constructed url: " + url);
		if( url!=null || filterItem==null ) return url;
		return filterItem.callNextFilter(params);
	}

   /**
    * Combine two parts of a path and ensure that only a single '/' is used
    * between the fields.
    */
   private String combinePath(String directoryPath, String filePath, char separator)
   {
      char lastDirectoryChar = directoryPath.charAt(directoryPath.length()-1);
      char firstFileChar = filePath.charAt(0);
      
      StringBuilder sb = new StringBuilder(directoryPath);
      if(lastDirectoryChar == separator && firstFileChar == separator)
         sb.deleteCharAt(sb.length()-1);
      else if(lastDirectoryChar != separator && firstFileChar != separator)
         sb.append(separator);
      
      return sb.append(filePath).toString();
   }

   /**
    * Determine the prefix to use for the particular AE that is being accessed based
    * on the appropriate configuration file.
    * @return 
    */
   private String getPrefixForAE(String ae)
   {
      Map<String,Object> aeMap = AEProperties.getInstance().getAE(ae);
      return FilterUtil.getString(aeMap, FILE_PREFIX);
   }
}
