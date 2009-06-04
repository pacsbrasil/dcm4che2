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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.search.macro.FileLocationMacro;
import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.MacroMixIn;
import org.dcm4chee.xero.search.study.PatientType;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesType;
import org.dcm4chee.xero.search.study.StudyType;
import org.dcm4chee.xero.wado.WadoParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Optionally adds the file location information to the results, per image */
public class AddFileLocationFilter  implements Filter<ResultsBean> {
   private static final Logger log = LoggerFactory.getLogger(AddFileLocationFilter.class);

   public static String FILE_LOCATION="fileLocation";
   
   /** Update any image beans with min/max pixel range information */
   public ResultsBean filter(FilterItem<ResultsBean> filterItem, Map<String, Object> params) {
	  ResultsBean ret = filterItem.callNextFilter(params);
	  log.debug("Add file location filter called.");
	  if( ret==null || ! ("true".equalsIgnoreCase((String) params.get(FILE_LOCATION) )))
		 return ret;
	  log.info("Adding file location to dicom objects.");
	  Map<String,Object> subParams = new HashMap<String,Object>();
	  String ae = (String) params.get(WadoParams.AE);
	  if( ae!=null )subParams.put(WadoParams.AE, ae);
	  for (PatientType pt : ret.getPatient()) {
		 for (StudyType st : pt.getStudy()) {
		    subParams.put(WadoParams.STUDY_UID, st.getStudyUID());
			for (SeriesType set : st.getSeries()) {
			   subParams.put(WadoParams.SERIES_UID, set.getSeriesUID());
			   for (DicomObjectType dot : set.getDicomObject()) {
				  if( ! (dot instanceof MacroMixIn) ) continue;
				  String objectUID = dot.getObjectUID();
				  MacroMixIn mmi = (MacroMixIn) dot;
				  if( mmi.getMacroItems().findMacro(FileLocationMacro.class)!=null ) continue;
				  subParams.put(WadoParams.OBJECT_UID, objectUID);
				  URL url = fileLocation.filter(null,subParams);
				  if( url==null ) {
				     log.warn("Unable to add file location for objectUID {}", objectUID);
				     continue;
				  }
				  log.info("Adding location {} to {}", url, objectUID);
				  mmi.getMacroItems().addMacro(new FileLocationMacro(url.toString()));
			   }
			}
		 }
	  }
	  log.info("Adding file location info done.");
	  return ret;
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
