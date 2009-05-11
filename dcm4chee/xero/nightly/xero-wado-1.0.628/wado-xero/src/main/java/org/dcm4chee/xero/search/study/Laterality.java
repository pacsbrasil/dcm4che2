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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Enumeration that represents the laterality of a DICOM image. 
 * @author Andrew Cowan (amidx)
 */
public enum Laterality
{
   LEFT('L'),
   RIGHT('R'),
   BOTH('B'),
   UNPAIRED('U'),
   UNKNOWN(' '); // Get rid of UNKNOWN and use NULL?
   
   private final char dicomCode;


   private Laterality(char dicomCode)
   {
      this.dicomCode = dicomCode;
   }
   
   public char getDicomCode()
   {
      return dicomCode;
   }
   
   public static Laterality parseLaterality(char code)
   {
      Laterality laterality;
      
      switch(code)
      {
      case 'L':
         laterality = LEFT;
         break;
      case 'R':
         laterality = RIGHT;
         break;
      case 'B':
         laterality = BOTH;
         break;
      case 'U':
         laterality = UNPAIRED;
         break;
      default:
         laterality = UNKNOWN;
      }
      
      return laterality;
   }
  
   
   /**
    * Parse the image laterality from DICOM element (0020,0062)
    * @param dicom
    * @return
    */
   public static Laterality parseImageLaterality(DicomObject dicom)
   {
      if(!containsImageLaterality(dicom))
         return UNKNOWN;
      
      Laterality laterality;
      
      String code = dicom.getString(Tag.ImageLaterality);
      if(code == null || code.length() == 0)
         laterality = UNKNOWN;
      else
         laterality = parseLaterality(code.charAt(0));
      
      assert code == null || code.length() == 1 : "Laterality must be a single character code"; 
      
      return laterality;
   }


   /**
    * Flip to the opposite laterality.
    */
   public static Laterality flip(Laterality laterality)
   {
      Laterality flipped;
      switch(laterality)
      {
      case LEFT:
         flipped = RIGHT;
         break;
      case RIGHT:
         flipped = LEFT;
      case BOTH:
      case UNPAIRED:
      case UNKNOWN:
      default:
         flipped = laterality;
      }
      
      return flipped;
   }

   /**
    * Determine if the indicated DICOM object contains the image laterality field.
    */
   public static boolean containsImageLaterality(DicomObject dicom)
   {
      if(dicom == null)
         return false;
      
      return dicom.contains(Tag.ImageLaterality);
   }
   
}
