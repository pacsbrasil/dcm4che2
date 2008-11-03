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

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4chee.xero.search.LocalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Represents a viewable image DICOM object.  There are a few different types of image beans - 
 * multi-frames, frames and regular (this one).  The multi-frame objects refer to many child frames,
 * but the child frames don't have individual object definitions, whereas the frame object includes
 * the parent multi-frame information and is a reference to a specific frame.  This object ONLY
 * works for single-frame objects (and not for multi-frames containing just 1 image). 
 * @author bwallace
 *
 */
@XmlRootElement
public class ImageBean extends ImageType implements Image, LocalModel<String>, MacroMixIn {
   private static final Logger log = LoggerFactory.getLogger(ImageBean.class);

   @XmlTransient
   protected SeriesBean seriesBean;

   /** Used to define the DICOM macro tables included in this object */
   @XmlTransient
   MacroItems macroItems;

   /**
     * Create an empty image bean object.  
     * @deprecated Not recommended for use except through JAXB deserialization.
     */
   protected ImageBean() {
   };

   /**
    * Create an iamge bean with a specified parent.
    * @param series
    */
   public ImageBean(SeriesBean series) {
	  this.seriesBean = series;
   }
   
   /**
     * Create an image bean from the given dicom data
     * 
     * @param data
     *            to use for the DICOM information
     */
   public ImageBean(SeriesBean seriesBean, DicomObject data) {
	  this(seriesBean);
	  initAttributes(data);
   }

   /** Make a clone of this image into the given image, or create a new one if necessary.
    * Copies any macros by reference.
    */
   public ImageBean clone(ImageBean image) {
	  if( image==null ) image = new ImageBean(seriesBean);
	  image.columns = this.columns;
	  image.rows = this.rows;
	  image.objectUID = this.objectUID;
	  image.instanceNumber = this.instanceNumber;
	  image.kobUID = this.kobUID;
	  image.macroItems = getMacroItems();
	  return image;
   }
   
   /**
     * Initialize the image level attributes by copying the DicomObject's image
     * level data for Columns, Rows, SOP Instance UID and Instance Number.
     * 
     * @param data
     *            to copy image level data into this from.
     */
   protected void initAttributes(DicomObject data) {
	  setColumns(data.getInt(Tag.Columns));
	  setRows(data.getInt(Tag.Rows));
	  // setSOPClassUID(data.getString(Tag.SOPClassUID));
	  setObjectUID(data.getString(Tag.SOPInstanceUID));
	  setInstanceNumber(data.getInt(Tag.InstanceNumber));
   }

   /**
     * Indicate if there are any interesting children, clearing any empty ones
     * first. Note that size is used to drive emptiness for all size related
     * attributes, as is WindowCenter for all window level related attributes.
     */
   public boolean clearEmpty() {
	  return getGspsUID() == null
			&& (macroItems == null || macroItems.clearEmpty());
   }

   /** Get the series that this originally belonged to. */
   public SeriesBean getSeriesBean()
   {
	  return seriesBean;
   }
   
   public void setSeriesBean(SeriesBean series) {
	  this.seriesBean = series;
   }
   
   /** Return the id for this element, in this case the SOP Instance UID */
   public String getId() {
	 return key(getObjectUID());
   }
   
   /** Return the key for the given object */
   public static String key(String objectUid) {
	  return objectUid;
   }

   /** Gets additional attributes and child elements defined in other objects */
   public MacroItems getMacroItems() {
	  if (macroItems == null)
		 macroItems = new MacroItems();
	  return macroItems;
   }
   
   /** Get the attributes from the macro items that are included in this object. */
   @XmlAnyAttribute
   public Map<QName, String> getOtherAttributes() {
	  Map<QName, String> ret = null;
	  if (macroItems != null)
		 ret = macroItems.getAnyAttributes();
	  if( log.isDebugEnabled() ) log.debug("Getting other attributes="+ret);
	  return ret;
   }
   
   @XmlAnyElement(lax=true)
   public List<Object> getOtherElements() {
	  if( this.macroItems==null ) return null;
	  return this.macroItems.getOtherElements();
   }

   /** Adds the given macro to this objects set of macro items */
   public void addMacro(Macro m) {
	  clearMacro(m.getClass());
	  getMacroItems().addMacro(m);
   }
   
   /** Clears the macro from this class, and any children of this class.
    * This uses getMacroItems rather than direct access to the variable in order to work with both
    * frame and non-framed images.  It still needs extra work for the multi-frame single object, however. */
   public void clearMacro(Class<? extends Macro> clazz) {
	  Macro item = getMacroItems().findMacro(clazz);
	  if (item != null)
		 getMacroItems().removeMacro(item);
   }

   /**
     * Gets the number of frames in this object - always 1 for non-multi frame
     * objects
     */
   public int getNumberOfFrames() {
	  return 1;
   }
   
   public MacroItems getFrameMacroItems(int posn) {
	  if( posn!=1 ) throw new IllegalArgumentException("Frame number must be 1 for single-frame getFrameMacroItems");
	  return getMacroItems();
   }
   
   /** The frame number for image beans is always null - only ImageBeanFrame has a frame number */
   @XmlAttribute
   public Integer getFrame() {
	  return null;
   }
}
