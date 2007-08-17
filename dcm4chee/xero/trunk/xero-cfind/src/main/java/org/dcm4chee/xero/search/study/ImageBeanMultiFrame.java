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

import javax.xml.bind.annotation.XmlTransient;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * This class is another instance of the image bean, but with a frames sub-object.  It is modified and
 * optimized to handle storage of sub-objects within it, and is thus handled different from the default
 * image bean object.
 * @author bwallace
 */
public class ImageBeanMultiFrame extends ImageBean {
   @XmlTransient
   MacroItems[] frameItems;

   /** Construct this object, complete with child frames. */
   public ImageBeanMultiFrame(DicomObject dcmObj) {
	  super(dcmObj);
   }
   
   
   /** Read additional attributes from the dicom object */   
   @Override
   protected void initAttributes(DicomObject data) {
	  super.initAttributes(data);
	  frameItems = new MacroItems[data.getInt(Tag.NumberOfFrames)];
   }



   /** Return the number of frames in this instance */
   public int getNumberOfFrames() {
	  return frameItems.length;
   }
   
   /** Get the n'th image bean - the is a temporary object, don't make changes to it and expect them
    * to stick.
    * @param posn is a 1 based index to retrieve the given frame information.
    */
   public ImageBean getImageFrame(int posn) {
	  if( posn < 1 || posn>getNumberOfFrames() ) throw new IllegalArgumentException("A position between 1 and the number of frames must be requested.");
	  ImageBean ret = new ImageBeanFrame(this,posn);
	  return ret;
   }
   
   /** Gets the image frame macro item */
   public MacroItems getFrameMacroItems(int posn) {
	  if( posn < 1 || posn>getNumberOfFrames() ) throw new IllegalArgumentException("A position between 1 and the number of frames must be requested.");
	  if( frameItems[posn-1]==null ) frameItems[posn-1] = new MacroItems();
	  return frameItems[posn-1];
   }
   
   public void clearMacro(Class<? extends Macro> clazz) {
	  if( this.macroItems!=null ) super.clearMacro(clazz);
	  for(int i=0; i<frameItems.length; i++ ) {
		 if( frameItems[i]!=null ) {
			Macro m = frameItems[i].findMacro(clazz);
			if( m!=null ) frameItems[i].removeMacro(m);
		 }
	  }
   }
}
