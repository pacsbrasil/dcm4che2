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

import java.util.Collection;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.search.macro.FlipRotateMacro;

/**
 * Utility that will check a mammo image and determine whether a flip is 
 * required to show it in the proper orientation for the viewer.
 * @author Andrew Cowan (amidx)
 */
public class MammoImageFlipper
{

   /**
    * Determine if a flip is required to show the Mammo image properly.
    * The C-FIND header will be used to determine the image orientation.
    */
   public boolean isFlipRequired(ImageBean image)
   {
      if(image == null)
         return false;
      
      DicomObject dcm = image.getCfindHeader();
      return isFlipRequired(dcm);
   }
   
   public boolean isFlipRequired(DicomObject dicom)
   {
      List<Orientation> orientations = Orientation.parsePatientOrientation(dicom);
      Laterality laterality = Laterality.parseImageLaterality(dicom);
      return isFlipRequired(laterality, orientations);
   }
   
   protected boolean isFlipRequired(Laterality laterality, Collection<Orientation> orientations)
   {
      if(laterality == null || orientations == null)
         return false;
      
      boolean isAnterior = orientations.contains(Orientation.ANTERIOR);
      boolean isLeft = laterality == Laterality.LEFT;
      
      return  isLeft ^ isAnterior;
   }
   
   public boolean applyFlipIfNecessary(ImageBean image)
   {
      boolean shouldFlip = isFlipRequired(image);
      if(shouldFlip)
         flip(image);
      
      return shouldFlip;
   }
   
   public void flip(ImageBean image)
   {
      image.getMacroItems().addMacro(new FlipRotateMacro(0,true));
   }
}
