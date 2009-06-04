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
package org.dcm4chee.xero.wado.cmove;

import java.net.URL;
import java.util.Map;

import org.dcm4chee.xero.dicom.DicomURLHandler;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;

/**
 * Accessor class that extracts the C-MOVE specific settings from the AE properties
 * file for the convenience of developers.
 * @author Andrew Cowan (amidx)
 */
public class CMoveSettings
{
   public static final String DESTINATION_PATH = "destinationPath";
   public static final String DESTINATION_AET = "destinationAET";
 
   public static final String DEFAULT_DESTINATION_PATH = System.getProperty("user.home") + "/XeroDicomCache";
   public static final String DEFAULT_DESTINATION_AET = "XERO-RECEIVE";
   
   private final Map<String, Object> aeSettings;

   /**
    * Load the C-MOVE settings for the indicated DICOM URL
    */
   public CMoveSettings(URL dicomURL)
   {
      this(DicomURLHandler.parseAETitle(dicomURL));
   }
   
   /**
    * Load the C-MOVE settings for the indicated aePath
    */
   public CMoveSettings(String aePath)
   { 
      this.aeSettings = AEProperties.getInstance().getAE(aePath);
      if( this.aeSettings==null ) 
          throw new NullPointerException("No ae settings for "+aePath);      
   }

   /**
    * Load the C-MOVE settings from the indicated property map.
    */
   public CMoveSettings(Map<String, Object> aeSettings)
   {
      if( aeSettings==null ) throw new NullPointerException("Null AE provided.");
      this.aeSettings = aeSettings;
   }

   /**
    * Get the cache directory where files will be 
    */
   public String getDestinationPath()
   {
      return FilterUtil.getString(aeSettings, DESTINATION_PATH,DEFAULT_DESTINATION_PATH);
   }
   
   /**
    * Get the destination AE title for the C-MOVE 
    */
   public String getDestinationAET()
   {
      return FilterUtil.getString(aeSettings, DESTINATION_AET,DEFAULT_DESTINATION_AET);
   }
   
}
