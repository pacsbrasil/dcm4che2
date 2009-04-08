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
package org.dcm4chee.xero.dicom;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.testng.annotations.Test;


public class ApplicationEntityProviderTest
{
   private ApplicationEntityProvider aes = new ApplicationEntityProvider();
   
   
   /**
    * @param ae
    */
   private void assertLocalPropertiesRead(NetworkApplicationEntity ae)
   {
      assertEquals(ae.getAETitle(),"DCM4CHEE");
      assertEquals(ae.getNetworkConnection().length,1);
      
      NetworkConnection con = ae.getNetworkConnection()[0];
      assertEquals(con.getPort(),11112);
      assertEquals(con.getHostname(),"localhost");    
   }

   private void assertAllSettingsRead(NetworkApplicationEntity ae)
   {
      assertEquals(ae.getAETitle(),"testTitle");
      assertEquals(ae.getNetworkConnection().length,1);
      
      NetworkConnection con = ae.getNetworkConnection()[0];
      assertEquals(con.getPort(),105);
      assertEquals(con.getHostname(),"testHost");
   }
   
   /**
    * Test method for {@link org.dcm4chee.xero.dicom.ApplicationEntityProvider#getAE(java.lang.String)}.
    * @throws IOException 
    */
   @Test
   public void testGetAE_FromAETitle_AllSettingsAreLoaded() throws IOException
   {
      NetworkApplicationEntity ae = aes.getAE("allSettings");
      assertAllSettingsRead(ae);
   }

   
   /**
    * Test method for {@link org.dcm4chee.xero.dicom.ApplicationEntityProvider#getAE(java.net.URL)}.
    * @throws IOException 
    */
   @Test
   public void testGetAE_FromDicomURL_ShouldReturnValuesForCorrectAE() throws IOException
   {
      DicomURLHandler handler = new DicomURLHandler();
      URL url = handler.createURL("dicom://allSettings@marlin:105/?seriesUID=1.2.3.4.5");
      NetworkApplicationEntity ae = aes.getAE(url);
      assertAllSettingsRead(ae);
   }

   /**
    * Test method for {@link org.dcm4chee.xero.dicom.ApplicationEntityProvider#getLocalAE()}.
    * @throws IOException 
    */
   @Test
   public void testGetLocalAE_ShouldLoadLocalProperties() throws IOException
   {
      NetworkApplicationEntity ae = aes.getLocalAE("uknownAE");
      assertLocalPropertiesRead(ae);
   }

   @Test(expectedExceptions=IllegalArgumentException.class)
   public void testGetAE_ShouldThrowIllegalArgument_WhenAEIsUnknown() throws IOException
   {
      aes.getAE("unknownAEPath");
   }
   
   @Test
   public void getAE_DeviceNameIsRead() throws IOException
   {
      NetworkApplicationEntity ae = aes.getAE("allSettings");
      assertEquals(ae.getDevice().getDeviceName(),"myTestDevice");
   }


   
   @Test
   public void getLocalAE_ShouldReturnTheAESpecificedInLocalTitle() throws IOException
   {
      // allSettings is pointing at cmoveLocal as it's local AE
      NetworkApplicationEntity localAE = aes.getLocalAE("allSettings");
      assertEquals(localAE.getAETitle(),"XERO-REQUEST");
      assertEquals(localAE.getNetworkConnection()[0].getPort(),11119);
      assertEquals(localAE.getNetworkConnection()[0].getHostname(), "localhost");
   }
   
   @Test
   public void getLocalAE_ShouldReturnDefaultAEWhenNoLocalAEIsSpecified() throws IOException
   {
      // allSettings is pointing at cmoveLocal as it's local AE
      NetworkApplicationEntity localAE = aes.getLocalAE("cmoveType");
      assertEquals(localAE.getAETitle(),"XERO");
      assertEquals(localAE.getNetworkConnection()[0].getPort(),11116);
      assertEquals(localAE.getNetworkConnection()[0].getHostname(), "marlin");
   }
   
   @Test
   public void getAE_TlsEncryptionConfiguredOnConnection() throws IOException
   {
      MockKeyStoreLoader loader = new MockKeyStoreLoader();
      ApplicationEntityProvider provider = new ApplicationEntityProvider(loader);
      
      try
      {
         NetworkApplicationEntity ae = provider.getAE("tls");
         NetworkConnection conn = ae.getDevice().getNetworkConnection()[0];
         assertTrue(conn.isTLS());
      }
      catch(IOException ie)
      {
         // Can't work with a 
         assertTrue(ie.getCause() instanceof GeneralSecurityException);
      }
      
      
      assertTrue(loader.fileNames.contains("D:/dev/xero/keystore"));
      assertTrue(loader.fileNames.contains("D:/dev/xero/truststore"));
      assertTrue(loader.passwords.contains("ks_secret"));
      assertTrue(loader.passwords.contains("ts_secret"));
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void getAE_TlsPropertyMustBeKnownValue() throws IOException
   {
      aes.getAE("badTLS");
   }
   
   public class MockKeyStoreLoader implements KeyStoreLoader
   {
      public List<String> fileNames = new ArrayList<String>();
      public List<String> passwords = new ArrayList<String>();
      
      public MockKeyStoreLoader()
      {
         
      }

      @Override
      public KeyStore loadKeyStore(String fileName, String password) throws GeneralSecurityException, IOException
      {
         fileNames.add(fileName);
         passwords.add(new String(password));
         
         String type = KeyStore.getDefaultType();
         return KeyStore.getInstance(type);
      }
      
   }
   
}
