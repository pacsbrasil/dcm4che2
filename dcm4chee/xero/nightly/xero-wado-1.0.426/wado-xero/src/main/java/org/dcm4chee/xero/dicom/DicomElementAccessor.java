// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.dicom;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.wado.DicomFilter;

/**
 * Wrapper class that can automatically fall back to retrieving DICOM headers
 * when the requested attribute is not available.
 * 
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DicomElementAccessor
{
   private final Filter<DicomObject> dicomFullHeader;
   private final String aeTitle;
   
   /**
    * Create an accessor that will retrieve the full DICOM header when the 
    * C-FIND response is not sufficient.
    */
   public DicomElementAccessor(Filter<DicomObject> dicomFullHeader, String aeTitle)
   {
      this.dicomFullHeader = dicomFullHeader;
      this.aeTitle = aeTitle;
   }
   
   /**
    * Create a trivial accessor that does not retrieve the full dicom header.
    */
   public DicomElementAccessor()
   {
      this(null,null);
   }
   
   public boolean contains(ImageBean image,int tag)
   {
      DicomElement element = getElement(image, tag);
      return element != null;
   }
   
   /**
    * Gets attempts to access the attribute from the passed object, and 
    * falls back to the full DICOM header if it is not available.
    * @param dcm DicomObject to read from
    * @param tag DICOM tag value.
    * @return the value of this attribute or -1 if it is not defined.
    */
   public int getInt(ImageBean image, int tag)
   {
      DicomElement element = getElement(image, tag);
      return element == null ? -1 : element.getInt(true);
   }
   
   /**
    * Get the DICOM element for the ImageBean from the C-FIND or if necessary
    * by header retrieval.
    */
   public DicomElement getElement(ImageBean image, int tag)
   {
      if(image == null || image.getCfindHeader() == null)
         return null;
      
      DicomElement dicomElement = image.getCfindHeader().get(tag);
      if(dicomElement == null && dicomFullHeader != null)
      {
         DicomObject dcm = DicomFilter.callInstanceFilter(dicomFullHeader, image ,aeTitle);
         dicomElement = dcm.get(tag);
      }
      
      return dicomElement;
   }
}
