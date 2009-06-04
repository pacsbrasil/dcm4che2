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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;


/**
 * Orientation of the Patient based on PS 3.17-2008 Annex A.
 *  
 * @author Andrew Cowan (amidx)
 */
public enum Orientation
{
   LEFT,
   RIGHT,
   POSTERIOR,
   ANTERIOR,
   HEAD,
   FEET,
   UNKNOWN;
   
   /**
    * Parse in the patient orientation from DICOM element (0020,0020)
    */
   public static List<Orientation> parsePatientOrientation(DicomObject dicom)
   {
      if(!containsPatientOrientation(dicom))
         return Collections.emptyList();
      
      String[] poCodes = dicom.getStrings(Tag.PatientOrientation);     
      List<Orientation> orientations = new ArrayList<Orientation>();
      for(String code : poCodes)
      {
         // Codes should be single character.  Some modalities mess this up
         for(int i=0;i<code.length();i++)
            orientations.add(Orientation.parseDicomCode(code.charAt(i)));
      }
      
      assert !(orientations.contains(Orientation.LEFT) && orientations.contains(Orientation.RIGHT));
      assert !(orientations.contains(Orientation.POSTERIOR) && orientations.contains(Orientation.ANTERIOR));
      assert !(orientations.contains(Orientation.HEAD) && orientations.contains(Orientation.FEET));

      return orientations;
   }
   
   /**
    * Does this DICOM object contain the Patient Orientation codes?
    */
   public static boolean containsPatientOrientation(DicomObject dicom)
   {
      if(dicom == null)
         return false;
      
      return dicom.contains(Tag.PatientOrientation);
   }
   
   /**
    * Parse the indicated patient orientation squence and output a corrected orientation
    */
   public static Orientation parseDicomCode(String code)
   {
      if(code == null || code.length() != 1)
         return UNKNOWN;

      return parseDicomCode(code.charAt(0));
   }
   
   public static Orientation parseDicomCode(char code)
   {
      Orientation orientation;
      switch(code)
      {
      case 'L':
         orientation = LEFT;
         break;
      case 'R':
         orientation = RIGHT;
         break;
      case 'P':
         orientation = POSTERIOR;
         break;
      case 'A':
         orientation = ANTERIOR;
         break;
      case 'H':
         orientation = HEAD;
         break;
      case 'F':
         orientation = FEET;
         break;
      default:
         orientation = UNKNOWN;
      }
      return orientation;
   }
   

   /**
    * Flip the orientation to the opposite value.
    */
   public static Orientation flip(Orientation orientation)
   {
      if(orientation == null) return null;
      else if(orientation == LEFT) return RIGHT;
      else if(orientation == RIGHT) return LEFT;
      else if(orientation == POSTERIOR) return ANTERIOR;
      else if(orientation == ANTERIOR) return POSTERIOR;
      else if(orientation == HEAD) return FEET;
      else if(orientation == FEET) return HEAD;
      else return UNKNOWN;
   }

}
