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

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Map;

import org.dcm4che2.data.UID;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that abstracts the logic of loading Application Entities (AE) for the
 * client application.
 * <P>
 * TODO: Consider using an identifier formatted as <i>aeTitle@hostName</i> to uniquely identify stations.
 * @author Andrew Cowan (amidx)
 */
public class ApplicationEntityProvider
{
   private static final Logger log = LoggerFactory.getLogger(ApplicationEntityProvider.class);
   
   
   private static final String[] NATIVE_LE_TS = {
      UID.ImplicitVRLittleEndian,
      UID.ExplicitVRLittleEndian  };

   private static final String DEFAULT_LOCAL_AE_NAME = "local";
   
   private final KeyStoreLoader keyStoreLoader;
   
   
   public ApplicationEntityProvider(KeyStoreLoader keyStoreLoader)
   {
      this.keyStoreLoader = keyStoreLoader;
   }
   
   public ApplicationEntityProvider()
   {
      this(new SimpleKeyStoreLoader());
   }

   /**
    * Create a new AE and configure the supported sop class uids.
    * @param aePath <aeTitle>[@hostName][:port]
    * @param sopClassUIDs SOP class UIDs for this AEs capabilities.
    * @throws IllegalArgumentException if the AE is not configured
    * @return Valid AE instance.
    */
   public NetworkApplicationEntity getAE(String aePath, String... sopClassUIDs)
      throws IOException
   {
      Map<String,Object> settings = AEProperties.getInstance().getAE(aePath);
      if(settings==null)
         throw new IllegalArgumentException("Unknown AE path: "+aePath);
      
      log.debug("Creating a new AE for {}",aePath);

      NetworkApplicationEntity ae = createAE(settings);
      NetworkConnection connection = createNetworkConnection(settings);
      Device device = createDevice(settings);
      
      ae.setNetworkConnection(connection);
      device.setNetworkApplicationEntity(ae);
      device.setNetworkConnection(connection);
      
      configureTransferCapabilities(ae,sopClassUIDs);
      return ae;
   }
   
   
   /**
    * Configure the TransferCapabilities of the indicated AE.
    */
   private void configureTransferCapabilities(NetworkApplicationEntity ae, String[] sopClassUIDs)
   {
      int idx = 0;
      TransferCapability[] transferCapabilities = new TransferCapability[sopClassUIDs.length];
      for(String cuid : sopClassUIDs)
      {
         TransferCapability tc = new TransferCapability(cuid,NATIVE_LE_TS,TransferCapability.SCU);
         transferCapabilities[idx++] = tc;
      }
      ae.setTransferCapability(transferCapabilities);
   }


   /**
    * Configure the network connection for the AE from the properties file.
    * <p>
    * The configured values are
    * <ul>
    * <li>AE host
    * <li>AE port
    * <li>TcpNoDelay = true (default)
    * <li>tls
    * </ul>
    */
   private NetworkConnection createNetworkConnection( Map<String, Object> settings)
   {
      NetworkConnection connection = new NetworkConnection();
      
      connection.setHostname((String)settings.get(AEProperties.AE_HOST_KEY));
      connection.setPort((Integer)settings.get(AEProperties.AE_PORT_KEY));
     
      String tls = (String)settings.get("tls");
      if(tls != null)
      {
         if("AES".equalsIgnoreCase(tls))
            connection.setTlsAES_128_CBC();
         else if("3DES".equalsIgnoreCase(tls))
            connection.setTls3DES_EDE_CBC();
         else if("NULL".equalsIgnoreCase(tls))
            connection.setTlsWithoutEncyrption();
         else
            throw new IllegalArgumentException("Unable to configure TLS encryption of type "+tls);
      }
     
      // Make these configurable later on.
      connection.setTcpNoDelay(true);
      
      return connection;
   }


   /**
    * Configure the AE settings based on the properties file values.  
    * <p>
    * The current default settings are:
    * <ul>
    * <li>PackPDV = true
    * <li>MaxOpsInvoked = 1
    * </ul>
    */
   private NetworkApplicationEntity createAE( Map<String, Object> settings)
   {
      NetworkApplicationEntity ae = new NetworkApplicationEntity();
      ae.setAETitle((String)settings.get(AEProperties.AE_TITLE_KEY));  
      
      // Make configurable...
      ae.setPackPDV(true);
      ae.setMaxOpsInvoked(1);
      
      return ae;
   }
   
   /**
    * Create a new network device that is configured from the client 
    * settings.
    */
   private Device createDevice(Map<String, Object> settings)
      throws IOException
   {  
      String name = FilterUtil.getString(settings, "name","XERO");
      Device device = new Device(name);
      
      String keyStoreFile = FilterUtil.getString(settings, "keystore");
      String keyStorePassword = FilterUtil.getString(settings, "keystorepw");
      String trustStoreFile = FilterUtil.getString(settings, "truststore");
      String trustStorePassword  = FilterUtil.getString(settings, "truststorepw");      
      String keyPassword = FilterUtil.getString(settings, "keypw");    

      try
      {
         KeyStore keyStore = keyStoreLoader.loadKeyStore(
               keyStoreFile, 
               keyStorePassword);

         KeyStore trustStore = keyStoreLoader.loadKeyStore(
               trustStoreFile, 
               trustStorePassword);

         if(keyStore!=null && trustStore!=null)
         {
            log.debug("Configuring device {} with truststore and keystore",name);
            
            String keyOrStorePassword = keyPassword != null ? keyPassword : keyStorePassword;
            if(keyOrStorePassword == null)
               throw new IllegalArgumentException("Key password is undefined");
            device.initTLS(
                  keyStore, 
                  keyOrStorePassword.toCharArray(), 
                  trustStore);   
         }
      }
      catch(GeneralSecurityException gse)
      {
         throw new IOException("Unable to configure TLS for device "+name,gse);
      }
         
      return device;
   }


   /**
    * Parse the indicated URL and return the associated AE.
    * <p>
    * The aeTitle will be used to lookup a locally configured 
    * @param url
    * @return
    */
   public NetworkApplicationEntity getAE(URL dicomURL)
      throws IOException
   {
      String aeTitle = DicomURLHandler.parseAETitle(dicomURL);
      return getAE(aeTitle);
   }
   
   /**
    * Get the local AE that has been configured to connect to the indicated 
    * remote AE title.
    */
   public NetworkApplicationEntity getLocalAE(String remoteAE, String... sopClassUIDs)
      throws IOException
   {
      Map<String,Object> p = AEProperties.getInstance().getAE(remoteAE);
      String localTitle = (String)p.get(AEProperties.LOCAL_TITLE);
      
      NetworkApplicationEntity localAE = null;
      try
      {
         localAE = getAE(localTitle,sopClassUIDs);
      }
      catch(IllegalArgumentException ex)
      {
         log.debug("Unable to load AE "+remoteAE+".  Default AE will be used instead.");
         localAE = getAE(DEFAULT_LOCAL_AE_NAME, sopClassUIDs);
      }
      
      localAE.setAssociationInitiator(true); // make configurable?
      return localAE;
   }
}
