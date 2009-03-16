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

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.dcm4che2.data.UID;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
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
   
   public static final String LOCAL_AE_NAME = "local";
   
   private static final String[] NATIVE_LE_TS = {
      UID.ImplicitVRLittleEndian,
      UID.ExplicitVRLittleEndian  };
   

   /**
    * Create the named AE instance from the local configuration files.
    * @param aePath <aeTitle>[@hostName][:port]
    * @throws IllegalArgumentException if the AE is not configured
    * @return A new ApplicationEntity instance.
    */
   public NetworkApplicationEntity getAE(String aePath)
   {
      // TODO: Add caching of NetworkAEs
      
      Map<String,Object> settings = AEProperties.getInstance().getAE(aePath);
      if(settings==null)
         throw new IllegalArgumentException("Unknown AE path: "+aePath);
      
      log.debug("Creating a new AE for {}",aePath);
      NetworkApplicationEntity ae = new NetworkApplicationEntity();
      NetworkConnection connection = new NetworkConnection();
      Device device = new Device("XERO");
      
      configureAE(ae,settings);
      configureNetworkConnection(connection,settings);
      
      ae.setNetworkConnection(connection);
      device.setNetworkApplicationEntity(ae);
      device.setNetworkConnection(connection);
      
      return ae;
   }
   
   /**
    * Create a new AE and configure the supported sop class uids.
    * @param aePath <aeTitle>[@hostName][:port]
    * @param sopClassUIDs SOP class UIDs for this AEs capabilities.
    * @throws IllegalArgumentException if the AE is not configured
    * @return Valid AE instance.
    */
   public NetworkApplicationEntity getAE(String aePath, Collection<String> sopClassUIDs)
   {
      NetworkApplicationEntity ae = getAE(aePath);
      configureTransferCapabilities(ae,sopClassUIDs);
      return ae;
   }
   
   
   /**
    * Configure the TransferCapabilities of the indicated AE.
    */
   private void configureTransferCapabilities(NetworkApplicationEntity ae, Collection<String> sopClassUIDs)
   {
      int idx = 0;
      TransferCapability[] transferCapabilities = new TransferCapability[sopClassUIDs.size()];
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
    * </ul>
    */
   private void configureNetworkConnection(NetworkConnection connection, Map<String, Object> settings)
   {
      connection.setHostname((String)settings.get(AEProperties.AE_HOST_KEY));
      connection.setPort((Integer)settings.get(AEProperties.AE_PORT_KEY));
      
      // Make these configurable later on.
      connection.setTcpNoDelay(true);
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
   private void configureAE(NetworkApplicationEntity ae, Map<String, Object> settings)
   {
      ae.setAETitle((String)settings.get(AEProperties.AE_TITLE_KEY));  
      
      // Make configurable...
      ae.setPackPDV(true);
      ae.setMaxOpsInvoked(1);
   }


   /**
    * Parse the indicated URL and return the associated AE.
    * <p>
    * The aeTitle will be used to lookup a locally configured 
    * @param url
    * @return
    */
   public NetworkApplicationEntity getAE(URL dicomURL)
   {
      String aeTitle = DicomURLHandler.parseAETitle(dicomURL);
      return getAE(aeTitle);
   }
   
   public NetworkApplicationEntity getLocalAE()
   {
      NetworkApplicationEntity localAE = getAE(LOCAL_AE_NAME);
      localAE.setAssociationInitiator(true); // make configurable?
      return localAE;
   }

}
