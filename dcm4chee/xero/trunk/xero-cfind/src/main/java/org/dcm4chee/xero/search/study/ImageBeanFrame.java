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

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

/** This object is used for a temporary, single frame representation of a multi-frame object.
 * @author bwallace
 */
public class ImageBeanFrame extends ImageBean {
   @XmlTransient
   int frame;
   @XmlTransient
   ImageBeanMultiFrame parent;
   
   /** Create a single frame object for a specific frame of a multi-frame object.
    * If there is no multi-frame object instantiated, just pass null, and this will work as a regular
    * image bean, albeit with the frame number set.
    */
   public ImageBeanFrame(ImageBeanMultiFrame image, int frame) {
	  this.parent = image;
	  this.frame = frame;
	  this.sopInstanceUID = image.getSOPInstanceUID();
	  this.instanceNumber = image.getInstanceNumber();
	  this.gspsUID = image.getGspsUID();
	  this.children = image.children;
	  if (image.position != null)
		 this.position = image.position + frame - 1;
	  this.rows = image.rows;
	  this.columns = image.columns;
   }
   
   /** Create a single frame object with the given sop instance and frame number */
   public ImageBeanFrame(String objectUID, int frame) {
	  this.parent = null;
	  this.frame = frame;
	  this.sopInstanceUID = objectUID;
   }
   
   public ImageBeanFrame() {
	  // Should only be used by JAXB.
   }

   /** Return the frame number */
   public Integer getFrame() {
	  return frame;
   }
   
   /** Sets the frame number */
   public void setFrame(Integer frame) {
	  this.frame = frame;
   }
   
   /** Return the id for this element, in this case the SOP Instance UID */
   @Override
   public String getId() {
	 return getSOPInstanceUID()+","+frame;
   }

   /** Gets all the attributes from the parent, multi-frame object, and secondly from this specific object */
   @Override
   public Map<QName, String> getOtherAttributes() {
	  if( parent==null ) return super.getOtherAttributes();
	  Map<QName,String> ret = null;
	  if( parent.macroItems!=null ) {
		    ret = parent.getMacroItems().getAnyAttributes();
	  }
	  if( parent.frameItems[frame-1]!=null ) {
		 if( ret==null ) ret = parent.frameItems[frame-1].getAnyAttributes();
		 else {
			Map<QName,String> ret2 = parent.frameItems[frame-1].getAnyAttributes();
			if( ret2!=null ) ret.putAll(ret2);
		 }
	  }
	  return ret;
   }

   /** Gets all the elements first from the parent root object, and secondly from this specific object */
   @Override
   public List<Object> getOtherElements() {
	  if( parent==null ) return super.getOtherElements();
	  List<Object> ret = null;
	  if( parent.macroItems!=null ) {
		    ret = parent.getMacroItems().getOtherElements();
	  }
	  if( parent.frameItems[frame-1]!=null ) {
		 if( ret==null ) ret = parent.frameItems[frame-1].getOtherElements();
		 else {
			List<Object> ret2 = parent.frameItems[frame-1].getOtherElements();
			if( ret2!=null ) ret.addAll(ret2);
		 }
	  }
	  return ret;
   }

   /** Gets the macro items for THIS frame - does not include the parent items */
   @Override
   public MacroItems getMacroItems() {
	  if( parent==null ) return super.getMacroItems();
	  return parent.getFrameMacroItems(frame);
   }

   
}
