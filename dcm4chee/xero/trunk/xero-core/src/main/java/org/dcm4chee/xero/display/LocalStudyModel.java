package org.dcm4chee.xero.display;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.dcm4chee.xero.display.DisplayMode.ApplyLevel;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.Macro;
import org.dcm4chee.xero.search.study.PatientBean;
import org.dcm4chee.xero.search.study.PatientIdentifier;
import org.dcm4chee.xero.search.study.ResultsBean;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.search.study.StudyBean;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("LocalStudyModel")
@Scope(ScopeType.SESSION)
public class LocalStudyModel {
   static Logger log = LoggerFactory.getLogger(LocalStudyModel.class);

   @In("PatientViewed")
   protected PatientViewed patientViewed;

   @In("StudyLevel")
   protected StudyLevel studyLevel;

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

   /**
     * Returns a string representation of the changes for the given patient.
     * Returns the empty string if that patient has no local changes. The
     * assumption is that the se prefix is the study result namespace.
     * 
     * @param patientId
     * @return XML representing the patient id local study changes
     */
   public String getPatientXml() {
	  PatientIdentifier pid = patientViewed.getId();
	  // If no customizations for this item, return immediately.
	  if (pid != null && !children.containsKey(pid))
		 return "";
	  if (clearEmpty())
		 return "";
	  try {
		 // TODO - figure out how to get a complete list of recognized
            // classes
		 // so that serialization works correctly.
		 JAXBContext context = JAXBContext.newInstance(PatientBean.class);
		 Marshaller m = context.createMarshaller();
		 StringWriter sw = new StringWriter();
		 m.marshal(getPatient(), sw);
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
     * Gets the customizations for the study specific data - this is worthwhile
     * from the point of view of displaying the object, since it minimizes how
     * much customized data is found.
     * 
     * @return
     */
   public String getStudyXml() {
	  String studyUid = studyLevel.getStudyUID();
	  if (studyUid == null)
		 throw new IllegalArgumentException("No Study UID provided.");
	  // If no customizations for this item, return immediately.
	  StudyBean study = (StudyBean) children.get(studyUid);
	  if (study == null)
		 return "";
	  if (study.clearEmpty())
		 return "";
	  try {
		 Marshaller m = context.createMarshaller();
		 StringWriter sw = new StringWriter();
		 m.marshal(getStudy(), sw);
		 String ret = sw.toString();
		 int xmlIndicator = ret.indexOf("?>");
		 if (xmlIndicator >= 0)
			ret = ret.substring(xmlIndicator + 2);
		 log.info("Study xml is " + ret);
		 return ret;
	  } catch (JAXBException e) {
		 throw new RuntimeException(e);
	  }
   }

   public PatientViewed getPatientViewed() {
	  return patientViewed;
   }

   public void setPatientViewed(PatientViewed patientViewed) {
	  this.patientViewed = patientViewed;
   }

   public StudyLevel getStudyLevel() {
	  return studyLevel;
   }

   public void setStudyLevel(StudyLevel studyLevel) {
	  this.studyLevel = studyLevel;
   }

   /**
     * Gets the patient object, creating a new one if necessary for the current
     * objects
     */
   public PatientBean getPatient() {
	  if (patientViewed == null || patientViewed.getPatientIdentifier() == null) {
		 throw new IllegalArgumentException("Patient viewed must have a patient identifier.");
	  }
	  PatientIdentifier patientId = patientViewed.getId();
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
	  if (studyLevel == null)
		 throw new IllegalArgumentException("StudyLevel information must be supplied.");
	  String studyUid = studyLevel.getStudyUID();
	  if (studyUid == null)
		 throw new IllegalArgumentException("Study UID must be supplied.");
	  StudyBean ret = (StudyBean) children.get(studyUid);
	  if (ret != null)
		 return ret;
	  PatientBean patient = getPatient();
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
	  if (studyLevel == null)
		 throw new IllegalArgumentException("StudyLevel information must be supplied.");
	  String seriesUid = studyLevel.getSeriesUID();
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
	  if (studyLevel == null)
		 throw new IllegalArgumentException("StudyLevel information must be supplied.");
	  String objectUid = studyLevel.getObjectUID();
	  if (objectUid == null)
		 throw new IllegalArgumentException("Object UID must be supplied.");
	  String id = objectUid;
	  if( studyLevel.getFrame()!=null && studyLevel.getFrame()!=0 ) id = id+","+studyLevel.getFrame();
	  ImageBean ret = (ImageBean) children.get(id);
	  if (ret != null)
		 return ret;
	  SeriesBean series = getSeries();
 	  ret = new ImageBean();
	  ret.setSOPInstanceUID(objectUid);
	  ret.setFrame(studyLevel.getFrame());
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
   public boolean clearEmpty() {
	  PatientBean patient = getPatient();
	  boolean isEmpty = patient.clearEmpty();
	  if (isEmpty) {
		 results.getPatient().remove(patient);
		 children.remove(new PatientIdentifier(patient.getPatientIdentifier()));
		 return true;
	  }
	  return false;
   }

   /** Applies the given macro to the selected object 
    * TODO implement this for frame, study and patient levels as well.
    */
   public void apply(ApplyLevel applyLevel, Macro macro) {
	  if( applyLevel.equals(ApplyLevel.SERIES)) {
		 getSeries().addMacro(macro);
	  }
	  else {
		 getImage().getMacroItems().addMacro(macro);
	  }
   }

}
