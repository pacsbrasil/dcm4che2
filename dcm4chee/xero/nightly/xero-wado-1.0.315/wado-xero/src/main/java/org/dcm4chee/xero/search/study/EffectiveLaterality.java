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

import org.dcm4che2.data.DicomObject;

/**
 * Effective laterality reflects the best guess of the laterality of an image
 * from the DICOM tags that have been provided.
 * <p>
 * DICOM is a messy standard, and can require some creative interpretation of
 * poorly formed Objects.  To determine the laterality (leftie / rightie) of an
 * image the Image Laterality should be used, but in cases where it is not 
 * present the patient orientation must be checked.
 * @author Andrew Cowan (amidx)
 */
public class EffectiveLaterality
{
   /**
    * Read the patient orientation and image laterality fields from the DICOM 
    * instance.  These values are then combined to determine the effective 
    * orientation of the patient with respect to the image.
    */
   public Laterality parseEffectiveLaterality(DicomObject data)
   {
      Laterality laterality = Laterality.parseImageLaterality(data);
      if(laterality == Laterality.UNKNOWN)
      {
         Orientation orientation = parseEffectiveOrientation(data);
         laterality = mapOrientationToLaterality(orientation);
      }
      
      return laterality;
   }
   
   /**
    * Read the effective LEFT/RIGHT orientation from the DICOM element with the proper
    * transform based on the POSTERIOR or ANTERIOR positioning.
    */
   protected Orientation parseEffectiveOrientation(DicomObject data)
   {
      List<Orientation> orientations = Orientation.parsePatientOrientation(data);
      
      Orientation effective = Orientation.UNKNOWN;
      if(orientations.contains(Orientation.LEFT))
         effective = Orientation.LEFT;
      else if(orientations.contains(Orientation.RIGHT))
         effective = Orientation.RIGHT;
      
      if(orientations.contains(Orientation.POSTERIOR))
         effective = Orientation.flip(effective);
      
      return effective;
   }
   
   protected Laterality mapOrientationToLaterality(Orientation orientation)
   {
      Laterality laterality;
      switch(orientation)
      {
      case LEFT:
         laterality = Laterality.LEFT;
         break;
      case RIGHT:
         laterality = Laterality.RIGHT;
         break;
       default:
          laterality = Laterality.UNKNOWN;
      }
      return laterality;
   }
}
