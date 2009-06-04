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

import static org.testng.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dcm4chee.xero.dicom.DicomURLHandler;
import org.testng.annotations.Test;


public class CMoveSettingsTest
{

   /**
    * Test method for {@link org.dcm4chee.xero.wado.cmove.CMoveSettings#CMoveSettings(java.net.URL)}.
    * @throws MalformedURLException 
    */
   @Test
   public void testCMoveSettings_DicomURLConstructor() throws MalformedURLException
   {
      DicomURLHandler handler = new DicomURLHandler();
      URL dicomURL = handler.createURL("dicom://cmoveType@localhost");
      CMoveSettings settings = new CMoveSettings(dicomURL);
      assertEquals(settings.getDestinationAET(),"CMOVE-RECEIVE");
      assertEquals(settings.getDestinationPath(),"d:/cache/cmove");
   }

   /**
    * Test method for {@link org.dcm4chee.xero.wado.cmove.CMoveSettings#CMoveSettings(java.lang.String)}.
    */
   @Test
   public void testCMoveSettings_AEPathConstructor()
   {
      CMoveSettings settings = new CMoveSettings("cmoveType");
      assertEquals(settings.getDestinationAET(),"CMOVE-RECEIVE");
      assertEquals(settings.getDestinationPath(),"d:/cache/cmove");
   }

   /**
    * Test method for {@link org.dcm4chee.xero.wado.cmove.CMoveSettings#getDestinationPath()}.
    */
   @Test
   public void testGetDestinationPath()
   {
      String expected = "D:\\cache";
      Map<String,Object> map = new HashMap<String,Object>();
      map.put(CMoveSettings.DESTINATION_PATH,expected);
      CMoveSettings settings = new CMoveSettings(map);
      assertEquals(settings.getDestinationPath(),expected);
   }
   
   @Test
   public void testGetDestinationPath_DefaultIfNotDefined()
   {
      Map<String,Object> map = new HashMap<String,Object>();
      CMoveSettings settings = new CMoveSettings(map);
      assertEquals(settings.getDestinationPath(),CMoveSettings.DEFAULT_DESTINATION_PATH);
   }

   /**
    * Test method for {@link org.dcm4chee.xero.wado.cmove.CMoveSettings#getDestinationAET()}.
    */
   @Test
   public void testGetDestinationAET()
   {
      String expected = "MyDesintationAET";
      Map<String,Object> map = new HashMap<String,Object>();
      map.put(CMoveSettings.DESTINATION_AET,expected);
      CMoveSettings settings = new CMoveSettings(map);
      assertEquals(settings.getDestinationAET(),expected);
   }
   
   @Test
   public void testGetDestinationAET_DefaultIfNotDefined()
   {
      Map<String,Object> map = new HashMap<String,Object>();
      CMoveSettings settings = new CMoveSettings(map);
      assertEquals(settings.getDestinationAET(),CMoveSettings.DEFAULT_DESTINATION_AET);
   }

}
