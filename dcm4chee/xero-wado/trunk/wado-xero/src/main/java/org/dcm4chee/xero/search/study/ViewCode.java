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

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Class that encapsulates the parsing and access of ViewCode values in a DICOM file. The actual
 * 
 * @author Andrew Cowan (amidx)
 */
public class ViewCode
{
   private static Map<String,String> valueToDescription = createValueToDescription();

   private String value;


   public ViewCode(DicomObject dicom)
   {
      initAttributes(dicom);
   }

   private static Map<String, String> createValueToDescription()
   {
      Map<String,String> valueToDescription = new HashMap<String,String>();
      valueToDescription.put(null, "");
      valueToDescription.put("R-10226", "MLO"); // medio-lateral oblique
      valueToDescription.put("R-10242", "CC"); // cranio-caudal
      valueToDescription.put("R-10224", "ML"); // medial-lateral
      return valueToDescription;
   }

   public static boolean containsViewCodeSequence(DicomObject dcm)
   {
      return dcm != null && dcm.contains(Tag.ViewCodeSequence);
   }

   /**
    * Load the sequence from the database.
    * 
    * @param dicom
    */
   protected void initAttributes(DicomObject dicom)
   {
      // TODO: Do we want to provide an isAvailable() method?
      if (dicom == null)
         return; // Leave a blank object if there is no sequence.

      DicomObject vcs = dicom.getNestedDicomObject(Tag.ViewCodeSequence);
      String codeValue = vcs == null ? null : vcs.getString(Tag.CodeValue);
      setCodeValue(codeValue);
   }

   /**
    * DICOM Code Value field
    */
   public String getCodeValue()
   {
      return value;
   }

   /**
    * @param value
    *           the value to set
    */
   public void setCodeValue(String value)
   {
      this.value = value;
   }


   /**
    * Based on the internal code value.
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ViewCode)
         return equals((ViewCode) obj);
      else
         return false;
   }

   private boolean equals(ViewCode viewCode)
   {
      if (value == null)
         return viewCode.getCodeValue() == null;
      else
         return value.equals(viewCode.getCodeValue());
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return value.hashCode();
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "ViewCode(value=" + value + ")";
   }

   /**
    * @return
    */
   public String getDescription()
   {
      String description = valueToDescription.get(value);
      if(description == null)
         description = value;
     
      return description;
   }
}
