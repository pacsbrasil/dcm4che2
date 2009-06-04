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
package org.dcm4chee.xero.search;

import static org.testng.Assert.*;

import java.util.Map;

import org.testng.annotations.Test;


/**
 *
 * @author Andrew Cowan (amidx)
 */
public class AEPropertiesTest
{
   private AEProperties ae = AEProperties.getInstance();
   /**
    * Test method for {@link org.dcm4chee.xero.search.AEProperties#getAE(java.lang.String)}.
    */
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testGetAE_ShouldThrowIllegalArgument_WhenColonInName()
   {
      ae.getAE("invalid:name");
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testGetAE_ShouldThrowIllegalArgument_WhenBackslashInName()
   {
      ae.getAE("invalid\\name");
   }

   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testGetAE_ShouldThrowIllegalArgument_WhenForwardSlashInName()
   {
      ae.getAE("invalid/name");
   }
   
   @Test
   public void testGetAE_ShouldReturnNull_WhenUnknownAERequested()
   {
      assertNull(ae.getAE("unknownAE"));
   }
   
   @Test
   public void testGetAE_ShouldReturnMap_WhenKnownAERequested()
   {
      Map<String,Object> settings = ae.getAE("cmoveLocal");
      assertNotNull(settings);
      assertEquals(settings.get("type"),"cmove");
      assertEquals(settings.get("host"),"localhost");
      assertEquals(settings.get("aeport"),new Integer(11119));
      assertEquals(settings.get("title"),"XERO-REQUEST");
      
   }
   
   @Test
   public void getDefaultAE_TypeShouldBeIDC2()
   {
      Map<String,Object> settings = ae.getDefaultAE();
      assertEquals(settings.get("type"),"idc2");
   }
   
   @Test
   public void getAE_TypeShouldDefaultToIDC2()
   {
      Map<String,Object> settings = ae.getAE("noType");
      assertEquals(settings.get("type"),"idc2");
   }
   
   @Test
   public void getAE_NullAEMapShouldBeReturned_WhenHostnameIsNotSpecified()
   {
      Map<String,Object> settings = ae.getAE("NoHost");
      assertNull(settings);
   }
   
   @Test
   public void getAE_EJBPort_HasNoDefaultValueDefined()
   {
      Map<String,Object> settings = ae.getAE("noType");
      assertNotNull(settings);
      assertNull(settings.get(AEProperties.EJB_PORT));
   }
   
   @Test
   public void getLocalAE_EJBPort_HasNoDefaultValueDefined()
   {
      Map<String,Object> settings = ae.getDefaultAE();
      assertNotNull(settings);
      assertNull(settings.get(AEProperties.EJB_PORT));
   }
}
