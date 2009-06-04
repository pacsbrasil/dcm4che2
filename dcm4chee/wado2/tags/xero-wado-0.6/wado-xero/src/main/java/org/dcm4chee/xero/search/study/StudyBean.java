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

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.metadata.filter.CacheItem;
import org.dcm4chee.xero.search.LocalModel;
import org.dcm4chee.xero.search.ResultFromDicom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Study representation that can initialize itself from the DICOM object and knows about macros and parent/child
 * relationships.
 * @author bwallace
 */
@XmlRootElement(namespace = "http://www.dcm4chee.org/xero/search/study/", name = "study")
public class StudyBean extends StudyType implements Study, CacheItem, LocalModel<String>, ResultFromDicom {
   private static Logger log = LoggerFactory.getLogger(StudyBean.class);

   @XmlTransient
   Map<Object, Object> children;

   @XmlTransient
   StudyBean originalStudy;

   @XmlTransient
   MacroItems macroItems;

   public StudyBean() {
	  this(new HashMap<Object, Object>());
   }

   /** Create a new study bean object from the given data */
   public StudyBean(Map<Object, Object> children, DicomObject data) {
	  this(children);
	  initAttributes(data);
	  addResult(data);
   }

   /** Create a new study bean object, with no data in it */
   public StudyBean(Map<Object, Object> children) {
	  if (children == null)
		 throw new IllegalArgumentException("Children must not be null.");
	  this.children = children;
   }

   /**
     * Create a study instance by copying attributes and children (shallow
     * children copy, no update to grand children's containment in children
     * map.) Does not update the children map.
     * 
     * @param study
     *            to copy from.
     */
   public StudyBean(Map<Object, Object> children, StudyBean study) {
	  this(children);
	  this.originalStudy = study;
	  setAccessionNumber(study.getAccessionNumber());
	  setInstanceAvailability(study.getInstanceAvailability());
	  setModalitiesInStudy(study.getModalitiesInStudy());
	  setNumberOfStudyRelatedInstances(study.getNumberOfStudyRelatedInstances());
	  setNumberOfStudyRelatedSeries(study.getNumberOfStudyRelatedSeries());
	  setReferringPhysicianName(study.getReferringPhysicianName());
	  setStudyDateTime(study.getStudyDateTime());
	  setStudyDescription(study.getStudyDescription());
	  setStudyID(study.getStudyID());
	  setStudyUID(study.getStudyUID());
	  setStudyStatusID(study.getStudyStatusID());
	  getSeries().addAll(study.getSeries());
   }

   /** Gets additional attributes and child elements defined in other objects.
    * Shares the macro items with any parent items that have one, so as to only
    * update local information once for all child clones. */
   public MacroItems getMacroItems() {
	  if (macroItems == null) {
		 if( this.originalStudy!=null ) 
			this.macroItems = this.originalStudy.getMacroItems();
		 else macroItems = new MacroItems();
	  }
	  return macroItems;
   }

   /**
     * Initialize the attributes for this study bean object from the dicom
     * object provided.
     * 
     * @param data
     */
   protected void initAttributes(DicomObject data) {
	  setAccessionNumber(data.getString(Tag.AccessionNumber));
	  setInstanceAvailability(data.getString(Tag.InstanceAvailability));
	  setModalitiesInStudy(commaSeparate(data.getStrings(Tag.ModalitiesInStudy)));
	  setNumberOfStudyRelatedInstances(data.getInt(Tag.NumberOfStudyRelatedInstances));
	  setNumberOfStudyRelatedSeries(data.getInt(Tag.NumberOfStudyRelatedSeries));
	  setReferringPhysicianName(PatientBean.excludeZeroEnd(data.getString(Tag.ReferringPhysicianName)));

	  Date date = null;
	  try {
		 date = data.getDate(Tag.StudyDate, Tag.StudyTime);
	  } catch (NumberFormatException nfe) {
		 log.warn("Illegal study date or time:" + nfe);
	  }
	  if (date != null) {
		 GregorianCalendar cal = new GregorianCalendar();
		 cal.setTime(date);
		 setStudyDateTime(PatientBean.datatypeFactory.newXMLGregorianCalendar(cal));
	  }

	  setStudyDescription(data.getString(Tag.StudyDescription));
	  setStudyID(data.getString(Tag.StudyID));
	  setStudyUID(data.getString(Tag.StudyInstanceUID));
	  setStudyStatusID(data.getString(Tag.StudyStatusIDRET));
   }

   /** Turn an array of strings into a comma separated string. */
   public static String commaSeparate(String[] strings) {
	  if (strings == null)
		 return null;
	  if (strings.length == 0)
		 return null;
	  if (strings.length == 1)
		 return strings[0];
	  StringBuffer ret = new StringBuffer(strings[0]);
	  for (int i = 1; i < strings.length; i++) {
		 ret.append(',').append(strings[i]);
	  }
	  return ret.toString();
   }

   /** Add any series and sub-series information to this study object */
   public void addResult(DicomObject data) {
	  String seriesUID = data.getString(Tag.SeriesInstanceUID);
	  log.debug("Adding information to study seriesUID=" + seriesUID);
	  if (seriesUID != null) {
		 log.debug("Adding child to study " + seriesUID);
		 String key = SeriesBean.key(seriesUID);
		 if (children.containsKey(key)) {
			((SeriesBean) children.get(key)).addResult(data);
		 } else {
			SeriesBean child = new SeriesBean(this, data);
			children.put(child.getId(), child);
			getSeries().add(child);
		 }
	  } else
		 log.debug("Study " + studyUID + " does not contain a series information.");
   }

   /** Figure out how many bytes this consumes */
   public long getSize() {
	  // Some amount of space for this item
	  long ret = 128;
	  for (SeriesType series : getSeries()) {
		 ret += ((CacheItem) series).getSize();
	  }
	  return ret;
   }

   /** Return true if there are no series children and no customized elements */
   public boolean clearEmpty() {
	  boolean seriesEmpty = ResultsBean.clearEmpty(children, getSeries());
	  return seriesEmpty && getGspsLabel() == null && (macroItems == null || macroItems.clearEmpty());
   }

   public String getId() {
	  return key(getStudyUID());
   }
   
   public static String key(String studyUid) {
	  return "study://"+studyUid;
   }

   /**
     * Returns the original study that this clone is based on - or returns this
     * if this is the original.
     */
   public StudyBean getOriginalStudy() {
	  if (originalStudy != null)
		 return originalStudy;
	  return this;
   }

   /** Gets a child by UID */
   public Object getChildById(Object uid) {
	  if (children != null)
		 return children.get(uid);
	  return null;
   }
   
   @XmlAttribute(name = "StudyDateF")
   public String getStudyDateFormatted() {
	  if( studyDateTime==null ) return null;
	  GregorianCalendar gc = studyDateTime.toGregorianCalendar();
	  Date time = gc.getTime();
	  DateFormat df = DateFormat.getDateTimeInstance();
	  return df.format(time);
   }


   /** Get the attributes from the macro items that are included in this object. */
   @XmlAnyAttribute
   public Map<QName, String> getAnyAttributes() {
	  if (macroItems == null)
		 return null;
	  return macroItems.getAnyAttributes();
   }
   
   /** Adds the given macro item, AND clears it from any children elements */
   public void addMacro(Macro macro) {
	  clearMacro(macro.getClass());
	  getMacroItems().addMacro(macro);
   }

   public void clearMacro(Class<? extends Macro> clazz) {
	  if( macroItems!=null ) {
		 Macro m = getMacroItems().findMacro(clazz);
		 if( m!=null ) getMacroItems().removeMacro(m);
	  }
	  for (SeriesType seriesT : getSeries()) {
		SeriesBean seriesB = (SeriesBean) seriesT;
		seriesB.clearMacro(clazz);
	  }
   }
   
   /**
    * Searches the leaf nodes for the latest item matching the given modality.
    * @param key
    * @param modality
    * @return
    */
   public DicomObjectType searchStudy(String key, String modality) {
	  SearchableDicomObject ret = null;
	  for (SeriesType series : getSeries()) {
		 if (! series.getModality().equals(modality) )
			continue;
		 for (DicomObjectType dot : series.getDicomObject()) {
			if( !(dot instanceof SearchableDicomObject) )
			   continue;
			SearchableDicomObject sdo = (SearchableDicomObject) dot;
			if( betterMatch(key,sdo,ret) ) {
			   ret = sdo;
			}
		 }
	  }
	  return (DicomObjectType) ret;
   }
   
   /**
    * Figures out whether sdo1 is a better match than sdo2 or not
    * Currently only handles * or an exact match.
    * @TODO Add better position based matching that will match multiple items.
    * @param key
    * @param sdo1
    * @param sdo2
    * @return
    */
   public static boolean betterMatch(String key, SearchableDicomObject sdo1, SearchableDicomObject sdo2) {
	  boolean sdo1Match = key.equals("*") | key.equals(sdo1.getContentName()) || key.equals(sdo1.getObjectUID());
	  if( !sdo1Match ) return false;
	  if( sdo2==null ) return true;
	  Date d1 = sdo1.getContentDate();
	  Date d2 = sdo2.getContentDate();
	  int cmp=0;
	  if( d1!=null && d2!=null ) cmp = d1.compareTo(d2);
	  if( cmp!=0 ) return cmp>0;
	  cmp = sdo1.getContentName().compareTo(sdo2.getContentName());
	  if( cmp!=0 ) return cmp>0;
	  cmp = sdo1.getObjectUID().compareTo(sdo2.getObjectUID());
	  return cmp>0;
   }
}
