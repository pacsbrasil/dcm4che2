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
package org.dcm4chee.xero.search.study;

import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.dcm4chee.xero.search.LocalModel;
import org.dcm4chee.xero.search.ResultFromDicom;

/**
 * Defines the series bean, along with intializers from the dicom object.
 * @author bwallace
 *
 */
public class SeriesBean extends SeriesType implements Series, ResultFromDicom, CacheItem, LocalModel<String> {
   static Logger log = LoggerFactory.getLogger(SeriesBean.class);

   /** Used to define the DICOM macro tables included in this object */
   @XmlTransient
   MacroItems macroItems;

   @XmlTransient
   protected StudyBean parent;

   @XmlTransient
   private Integer numberOfSeriesRelatedInstances;

   public SeriesBean(StudyBean parent) {
	  this.parent = parent;
   }

   /**
     * Construct a series bean object from another series type object. Does NOT
     * fill in the child map at this time.
     */
   public SeriesBean(StudyBean newParent, SeriesType series) {
	  this.parent = newParent;
	  setModality(series.getModality());
	  setSeriesDescription(series.getSeriesDescription());
	  setSeriesInstanceUID(series.getSeriesInstanceUID());
	  setSeriesNumber(series.getSeriesNumber());
	  setViewable(series.getViewable());
	  getDicomObject().addAll(series.getDicomObject());
   }

   /**
     * This creates a series beans with no attributes or preset values.
     * 
     */
   public SeriesBean() {
   }

   /**
     * Create a series bean from the given instance data.
     * 
     * @param data
     *            is the Dicom object to copy series and possibly image level
     *            data from.
     */
   public SeriesBean(StudyBean parent, DicomObject data) {
	  this.parent = parent;;
	  initAttributes(data);
	  addResult(data);
   }

   /** Initialize the series level attributes */
   protected void initAttributes(DicomObject data) {
	  setModality(data.getString(Tag.Modality));
	  setSeriesDescription(data.getString(Tag.SeriesDescription));
	  setSeriesInstanceUID(data.getString(Tag.SeriesInstanceUID));
	  setNumberOfSeriesRelatedInstances(data.getInt(Tag.NumberOfSeriesRelatedInstances));
	  try {
		 setSeriesNumber(data.getInt(Tag.SeriesNumber));
	  } catch (NumberFormatException nfe) {
		 log.warn("Series number was incorrectly formatted - sometimes happens for SR data:" + nfe);
	  }
   }

   /** Add any image level information to this series. */
   public void addResult(DicomObject data) {
	  String objectUID = data.getString(Tag.SOPInstanceUID);
	  DicomElement instances = data.get(ImageSearch.InstanceSeq);
	  if (objectUID == null && instances==null)
		 return;
	  String key = ImageBean.key(objectUID);
	  if (getChildren().containsKey(key)) {
		 log.debug("Series " + getSeriesInstanceUID() + " already contains a child " + key);
	  } else if( instances!=null && instances.countItems()>0 ) {
// TODO Remove this code or fix it to match whatever VMF code is final.
		 for(int i=0, n=instances.countItems(); i<n; i++) {
			DicomObject child = instances.getDicomObject(i);
			addResult(child);
		 }
	  } else {
		 DicomObjectType dobj = createChildByModality(data);
		 LocalModel<?> localModel = (LocalModel<?>) dobj;
		 if (dobj == null) {
			log.warn("No object created for child " + objectUID + " of modality " + modality);
			return;
		 }
		 getChildren().put(localModel.getId(), dobj);
		 getDicomObject().add(dobj);
	  }
   }

   /** Create different types of children based on the modality of the series */
   protected DicomObjectType createChildByModality(DicomObject data) {
	  if( modality.equals("ECG") ) {
		 log.warn("Unsupported modality "+modality);
		 return null;
	  }
	  if (modality.equals("SR")) {
		 return new ReportBean(data);
	  }
	  if (modality.equals("KO")) {
		 return new KeyObjectBean(this,data);
	  }
	  if (modality.equals("PR")) {
		 return new GspsBean(data);
	  }
	  int frameCount = data.getInt(Tag.NumberOfFrames);
	  if (frameCount > 1) {
		 log.debug("Creating a multi-frame image bean on NumberOfFrames=",frameCount);
		 return new ImageBeanMultiFrame(this,data);
	  } else
		 log.debug("Creating a single image bean.");
		 return new ImageBean(this,data);
   }

   /** Figure out how many bytes this consumes */
   public long getSize() {
	  // Some amount of space for this item, plus some for all the other
	  // images under this item.
	  return 128 + 256 * getDicomObject().size();
   }

   /** Gets the number of DICOM objects associated with this series */
   public Integer getNumberOfSeriesRelatedInstances() {
	  return numberOfSeriesRelatedInstances;
   }

   /** Sets the number of DICOM objects associated with this series */
   public void setNumberOfSeriesRelatedInstances(Integer value) {
	  this.numberOfSeriesRelatedInstances = value;
   }

   /**
     * Clears children that are empty (have not interesting content, and
     * returnes true if this object can be cleared (has not more children and no
     * cusotized data.
     */
   public boolean clearEmpty() {
	  boolean emptyChildren = ResultsBean.clearEmpty(getChildren(), getDicomObject());
	  return emptyChildren && (macroItems == null || macroItems.clearEmpty());
   }

   /** Get the ID for this object, in this case the series instance UID */
   public String getId() {
	  return key(getSeriesInstanceUID());
   }
   
   public static String key(String seriesUid) {
	  return "series://"+seriesUid;
   }

   /** Gets additional attributes and child elements defined in other objects */
   public MacroItems getMacroItems() {
	  if (macroItems == null)
		 macroItems = new MacroItems();
	  return macroItems;
   }

   /** Adds the given macro item, AND clears it from any children elements */
   public void addMacro(Macro macro) {
	  clearMacro(macro.getClass());
	  getMacroItems().addMacro(macro);
   }

   /** Clears the macro from this class, and any children of this class. */
   public void clearMacro(Class<? extends Macro> clazz) {
	  if (macroItems != null) {
		 Macro item = macroItems.findMacro(clazz);
		 if (item != null)
			macroItems.removeMacro(item);
	  }
	  for (DicomObjectType dot : getDicomObject()) {
		 if (dot instanceof ImageBean) {
			ImageBean image = (ImageBean) dot;
			image.clearMacro(clazz);
		 }
	  }
   }
   
   /** Returns the children map */
   public Map<Object, Object> getChildren() {
	  if( parent==null ) return null;
	  return parent.children;
   }
   
   /** Returns the original parent study containing this object, in which this is
    * registered as a child. May not return the path thorugh which this series was accessed
    * if this series is multiply accessible.
    */
   public StudyBean getStudyBean() {
	  return parent;
   }

   /** Get the attributes from the macro items that are included in this object. */
   @XmlAnyAttribute
   public Map<QName, String> getAnyAttributes() {
	  if (macroItems == null)
		 return null;
	  return macroItems.getAnyAttributes();
   }
}
