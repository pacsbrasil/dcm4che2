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
package org.dcm4chee.xero.display;

import java.util.List;

import org.dcm4chee.xero.search.study.Macro;
import org.dcm4chee.xero.search.study.PatientBean;
import org.dcm4chee.xero.search.study.PatientIdentifier;
import org.dcm4chee.xero.search.study.StudyBean;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The conversational study model defines what patient, study, series and
 * image(s) are currently being viewed. A request can update these values
 * implicitly by providing new values in the request URL, (eg if the user is
 * manually providing the values), or it can remember them from the last
 * request.
 * 
 * The session study model will use the conversational study model information
 * to allow it to display the same children as this object does.
 * 
 * @author bwallace
 * 
 */
@Name("ConversationStudyModel")
@Scope(ScopeType.CONVERSATION)
public class ConversationStudyModel extends StudyModel {
   static Logger log = LoggerFactory.getLogger(ConversationStudyModel.class);

   /**
     * Contains the name of the current frame layout TODO make this dependent on
     * what target is current. TODO check the setting of this value to ensure it
     * is safe. (SECURITY concern)
     */
   protected String layout = "/xero/image/layout.xml";


   public ConversationStudyModel() {
	  displayStudyLevel = new StudyLevel();
	  emptyPatient = new PatientBean();
   }

   /**
     * This will be called only if the user has supplied at least 1 property in
     * the dipslay study level, that is, study, series, image UID's or frame. In
     * general, they should supply all the parent information, but that isn't
     * necessarily reliable. TODO make this dependent on the current target.
     */
   @In(value = "DisplayStudyLevel", required = false)
   public void setDisplayStudyLevel(StudyLevel studyLevel) {
	  if( studyLevel==null ) return;
	  log.info("Study level set to " + studyLevel);
	  if (studyLevel.getPatientIdentifier() != null || studyLevel.getStudyUIDs() != null) {
		 // completely re-sets the current display study level - either will
         // work fine.
		 this.displayStudyLevel = studyLevel;
		 if( studyLevel.getSeriesUID() !=null ) {
			this.actionStudyLevel = studyLevel;
			
			StudyBean study = getStudy();
			Macro oldMacro = study.getMacroItems().findMacro(NavigateMacro.class);
			if( oldMacro!=null ) study.getMacroItems().removeMacro(oldMacro);
			study.getMacroItems().addMacro(new NavigateMacro(studyLevel.getSeriesUID()));
		 }
		 return;
	  }
	  log.warn("Display study level not set at study or patient level - wont navigate successfully.");
   }

   public String getLayout() {
	  return layout;
   }

   public void setLayout(String layout) {
	  this.layout = layout;
   }

   /**
     * Returns a generated URL to retrieve the study level information for the
     * currently displaying studies. A study level URL is generated by
     * preference, but a patient level can be generated if the study level one
     * isn't found.
     * 
     * @return Relative URL for xero for the currently displaying studies.
     */
   public String getUrl() {
	  List<String> uids = displayStudyLevel.getStudyUIDs();
	  StringBuffer url = new StringBuffer("/xero/study/study.xml");
	  PatientIdentifier patientIdentifier = displayStudyLevel.getPatientIdentifier();
	  boolean first = true;
	  if (uids != null && uids.size() > 0) {
		 for (String uid : uids) {
			if (first) {
			   url.append("?StudyInstanceUID=");
			   first = false;
			} else
			   url.append("&StudyInstanceUID=");
			url.append(uid);
		 }
	  } else if (patientIdentifier != null) {
		 // TODO - check the context to see if there are selected studies to be viewed instead of just
		 // querying against the entire patient ID....
		 url.append("?PatientID=").append(patientIdentifier);
	  } else {
		 throw new IllegalArgumentException("Nether patient ID nor study UID's provided.");
	  }
	  return url.toString();
   }
}
