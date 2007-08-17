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

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.dcm4chee.xero.display.DisplayMode.ApplyLevel;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageBeanFrame;
import org.dcm4chee.xero.search.study.Macro;
import org.dcm4chee.xero.search.study.PatientBean;
import org.dcm4chee.xero.search.study.PatientIdentifier;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.jboss.seam.annotations.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StudyModel {
   static Logger log = LoggerFactory.getLogger(StudyModel.class);

   /**
     * This variable is used to control what is being modified by the current
     * action, and thus what is returned by getPatient/Study/Series/Image
     * methods.
     */
   @In(value = "ActionStudyLevel", required = false)
   protected StudyLevel actionStudyLevel;

   /** This parent is used for studies with unknown parents.  If null, then an exception is thrown
    * on trying to create a study without defining the parent.  It is preferable to always have the
    * patient ID, but for some EPR applications, only the study UID's are used.
    */
   protected PatientBean emptyPatient;
   
   /**
     * This variable is used to control what is being displayed, and thus what
     * needs to be rendered in order to display the current study.
     */
   protected StudyLevel displayStudyLevel;

   protected ResultsBean results = new ResultsBean();

   static JAXBContext context;
   static {
	  try {
		 context = JAXBContext.newInstance("org.dcm4chee.xero.search.study");
	  } catch (JAXBException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
	  }
   }

   Map<Object, Object> children = results.getChildren();

   /** This method is used for testing to setup both display and action variables */
   void setStudyLevel(StudyLevel studyLevel) {
	  this.actionStudyLevel = studyLevel;
	  this.displayStudyLevel = studyLevel;
   }
   /**
     * Returns a string representation of the changes for the given patient.
     * Returns the empty string if that patient has no local changes. The
     * assumption is that the se prefix is the study result namespace.
     * 
     * @param patientId
     * @return XML representing the patient id local study changes
     */
   public String getPatientXml() {
	  List<String> uids = displayStudyLevel.getStudyUIDs();
	  PatientBean patient = null;
	  PatientIdentifier patientIdentifier = displayStudyLevel.getPatientIdentifier();
	  if (patientIdentifier != null) {
		 patient = (PatientBean) children.get(patientIdentifier);
	  }
	  if (uids != null && uids.size() > 0) {
		 if( patient!=null ) {
			// Start with a copy of the patient bean, but with no children.
			patient = new PatientBean(null,patient);
			patient.getStudy().clear();
		 }
		 else {
			patient = new PatientBean();
		 }
		 for (String uid : uids) {
			StudyBean study = (StudyBean) children.get(uid);
			if (study != null) {
			   patient.getStudy().add(study);
			}
		 }
		 if (patient.getStudy().size() == 0)
			return "";
	  } else if (patientIdentifier != null) {
		 if (patient == null)
			return "";
		 if (clearEmpty(patient))
			return "";
	  } else {
		 throw new IllegalArgumentException("Either a patient ID or a set of Study UID's must be provided.");
	  }
	  try {
		 Marshaller m = context.createMarshaller();
		 StringWriter sw = new StringWriter();
		 m.marshal(patient, sw);
		 String ret = sw.toString();
		 int xmlIndicator = ret.indexOf("?>");
		 if (xmlIndicator >= 0)
			ret = ret.substring(xmlIndicator + 2);
		 log.info("Patient xml is " + ret);
		 return ret;
	  } catch (JAXBException e) {
		 throw new RuntimeException(e);
	  }
   }

   /**
     * Gets the patient object that the current ACTION is operating on, creating
     * a new one if necessary for the current objects.
     */
   public PatientBean getPatient() {
	  if (actionStudyLevel == null || actionStudyLevel.getPatientIdentifier() == null) {
		 if( this.emptyPatient!=null ) return null;
		 throw new IllegalArgumentException("Patient viewed must have a patient identifier.");
	  }
	  PatientIdentifier patientId = actionStudyLevel.getPatientIdentifier();
	  PatientBean ret = (PatientBean) children.get(patientId);
	  if (ret != null)
		 return ret;
	  ret = new PatientBean(children);
	  ret.setId(patientId);
	  results.getPatient().add(ret);
	  children.put(ret.getId(), ret);
	  return ret;
   }

   /** Returns the StudyType object for the given UID */
   public StudyBean getStudy() {
	  if (actionStudyLevel == null)
		 throw new IllegalArgumentException("Action StudyLevel information must be supplied.");
	  String studyUid = actionStudyLevel.getStudyUID();
	  if (studyUid == null)
		 throw new IllegalArgumentException("Study UID must be supplied.");
	  StudyBean ret = (StudyBean) children.get(studyUid);
	  if (ret != null)
		 return ret;
	  PatientBean patient = getPatient();
	  if( patient==null ) patient = this.emptyPatient;
	  ret = new StudyBean(children);
	  ret.setStudyInstanceUID(studyUid);
	  patient.getStudy().add(ret);
	  children.put(ret.getId(), ret);
	  return ret;
   }

   /**
     * Returns the series type for the given uids
     */
   public SeriesBean getSeries() {
	  if (actionStudyLevel == null)
		 throw new IllegalArgumentException("Action StudyLevel information must be supplied.");
	  String seriesUid = actionStudyLevel.getSeriesUID();
	  if (seriesUid == null)
		 throw new IllegalArgumentException("Series UID must be supplied.");
	  SeriesBean ret = (SeriesBean) children.get(seriesUid);
	  if (ret != null)
		 return ret;
	  StudyBean study = getStudy();
	  ret = new SeriesBean(children);
	  ret.setSeriesInstanceUID(seriesUid);
	  study.getSeries().add(ret);
	  children.put(ret.getId(), ret);
	  return ret;
   }

   /**
     * Returns the image type of the given UIDs
     */
   public ImageBean getImage() {
	  if (actionStudyLevel == null)
		 throw new IllegalArgumentException("StudyLevel information must be supplied.");
	  String objectUid = actionStudyLevel.getObjectUID();
	  if (objectUid == null)
		 throw new IllegalArgumentException("Object UID must be supplied.");
	  String id = objectUid;
	  Integer frame = actionStudyLevel.getFrame();
	  if (frame != null && frame != 0)
		 id = id + "," + frame;
	  ImageBean ret = (ImageBean) children.get(id);
	  if (ret != null)
		 return ret;
	  SeriesBean series = getSeries();
	  if (frame != null && frame != 0) {
		 ret = new ImageBeanFrame(objectUid, frame);
	  } else {
		 ret = new ImageBean();
	     ret.setSOPInstanceUID(objectUid);
	  }
	  series.getDicomObject().add(ret);
	  children.put(ret.getId(), ret);
	  return ret;
   }

   public ResultsBean getResults() {
	  return results;
   }

   /**
     * This method goes through the current patient information and through
     * lower levels, and removes any elements that contain no children or
     * attributes other than the id attribute. This is done depth first in order
     * to allow clearing parents of items that only contain empty children.
     */
   public boolean clearEmpty(PatientBean patient) {
	  boolean isEmpty = patient.clearEmpty();
	  if (isEmpty) {
		 results.getPatient().remove(patient);
		 children.remove(new PatientIdentifier(patient.getPatientIdentifier()));
		 return true;
	  }
	  return false;
   }

   /**
     * Applies the given macro to the selected object.
     */
   public void apply(ApplyLevel applyLevel, Macro macro) {
	  if( applyLevel.equals(ApplyLevel.STUDY )) {
		 getStudy().addMacro(macro);
	  }
	  else if (applyLevel.equals(ApplyLevel.SERIES)) {
		 getSeries().addMacro(macro);
	  } else if( applyLevel.equals(ApplyLevel.IMAGE)) {
		 getImage().addMacro(macro);
	  }
	  else {
		 log.warn("Apply level not implemented:"+applyLevel);
	  }
   }

   public StudyLevel getDisplayStudyLevel() {
	  return displayStudyLevel;
   }

}
