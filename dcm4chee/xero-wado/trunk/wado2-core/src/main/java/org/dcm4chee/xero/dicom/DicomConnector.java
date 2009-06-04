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

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4chee.xero.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles the details of connecting two DICOM AEs together.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DicomConnector
{
   private static Logger log = LoggerFactory.getLogger(DicomConnector.class);
   
   private Executor executor = Executors.newCachedThreadPool(new NamedThreadFactory("XERO_DICOM "));

   /**
    * Connect two Network AEs with one of the indicated SOP Class UIDs
    * @param localAE 
    * @param remoteAE
    * @param sopClassUIDs
    * @throws InterruptedException 
    * @throws IOException 
    * @throws ConfigurationException 
    */
   public Association connect(NetworkApplicationEntity localAE, NetworkApplicationEntity remoteAE) throws ConfigurationException, IOException, InterruptedException
   {
      if(localAE == null || remoteAE == null)
         throw new IllegalArgumentException("Remote and Local AEs must be defined to create a DICOM association.");
      
      configureTLS(remoteAE, localAE);
      return localAE.connect(remoteAE, executor);
   }
   
   
   
   /**
    * Configure the remote connection to use the same TLS setup of the local connection.
    * We assume that the local connection is a slave to the remote connection configuration.
    */
   private void configureTLS(NetworkApplicationEntity remoteAE, NetworkApplicationEntity localAE)
   {
      NetworkConnection localConn = localAE.getNetworkConnection()[0];
      NetworkConnection remoteConn = remoteAE.getNetworkConnection()[0];
      
      // Ensure we are connecting with the correct protocol.
      if(localConn.isTLS())
      {
         String[] protocols = localConn.getTlsProtocol();
         String[] ciphers = localConn.getTlsCipherSuite();

         log.info("TLS Security enabled. protocols: {}, ciphers: {}", protocols, ciphers);
            
         remoteConn.setTlsProtocol(protocols);
         remoteConn.setTlsCipherSuite(ciphers);
      }
      
   }
   
   /**
    * Utility method that will release the indicated association if it is not null.
    * @param association
    */
   public void release(Association association, boolean waitForRSP)
   {
      if(association != null) {
         try { 
            association.release(waitForRSP);
         } 
         catch(InterruptedException e){
            log.warn("Unable to release association from {} to {}",association.getCallingAET(),association.getCalledAET());
         }
      }
   }

}
