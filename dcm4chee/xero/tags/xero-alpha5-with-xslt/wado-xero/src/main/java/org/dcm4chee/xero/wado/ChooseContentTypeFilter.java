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

import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.servlet.ServletResponseItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chooses the type of the response to be supplied for a given object type.
 * Selection criteria is:
 * 	 1. If contentType is specified, use the type to lookup a filter.
 * 		image/* - use the image encoding rules.
 *   2. If no contentType is specified, grab the header data and try to read the
 *      raw file.  Use the modality to decide the response type.
 *      PR - if a frame number is also specified, then return an image frame, otherwise as HTML
 *      SR - return as HTML
 *      KO - return as HTML
 *      ECG - don't know
 * @author bwallace
 *
 */
public class ChooseContentTypeFilter implements Filter<ServletResponseItem> {
   private static final Logger log = LoggerFactory.getLogger(ChooseContentTypeFilter.class);

   public ServletResponseItem filter(FilterItem<ServletResponseItem> filterItem, Map<String, Object> params) {
	  String contentType = (String) params.get("contentType");
	  if( contentType==null ) {
		 log.info("No content type found, looking for modality of the image to determine contentType");
		 DicomObject dobj = DicomFilter.filterDicomObject(filterItem, params, null);
		 if( dobj==null ) {
			return null;
		 }
		 String modality = dobj.getString(Tag.Modality);
		 if( modality==null ) {
			log.info("Couldn't find modality for object, not returning any result.");
			return null;
		 }
		 if( modality.equalsIgnoreCase("SR") || modality.equalsIgnoreCase("KO") 
			   || modality.equalsIgnoreCase("ECG") ) {
			contentType = "text/html";
		 }
		 else if( modality.equalsIgnoreCase("PR") ) {
			if( params.containsKey("frameNumber") ) contentType="image/jpeg";
			else contentType="text/html";
		 }
	  }
	  if( contentType==null ) {
		  return (ServletResponseItem) filterItem.callNextFilter(params);
	  }
	  log.info("Search on content type "+contentType);
	  if( filterItem.contains(contentType) ) {
		 return (ServletResponseItem) filterItem.callNamedFilter(contentType,params);
	  }
	  int slashPos = contentType.indexOf("/");
	  if( slashPos>0 ) {
		 String mainType = contentType.substring(0,slashPos);
		 log.info("Looking for primary type "+mainType);
		 if( filterItem.contains(mainType) ) {
			return (ServletResponseItem) filterItem.callNamedFilter(mainType,params);
		 }
	  }
	  log.warn("WADO Filter not found for contentType="+contentType);
	  return filterItem.callNextFilter(params);
   }

}
