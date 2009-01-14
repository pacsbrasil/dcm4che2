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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.dcm4che2.data.UID;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.xero.search.DicomCFindFilter;
import org.dcm4chee.xero.util.NamedThreadFactory;

/**
 * AE connections wrapper class
 * <p>
 * Moved from {@link DicomCFindFilter}
 * @author smohan
 * @author Andrew Cowan (amidx)
 */
public class AEConnection   {   
   
   private static final String[] NATIVE_LE_TS = {
      UID.ImplicitVRLittleEndian,
      UID.ExplicitVRLittleEndian  };
   
   private Executor executor = Executors.newCachedThreadPool(new NamedThreadFactory("XERO_QUERY "));
   private Device device = new Device("XERO");
   
   private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();
   private NetworkConnection remoteConn = new NetworkConnection();
   private NetworkApplicationEntity ae = new NetworkApplicationEntity();
   private NetworkConnection conn = new NetworkConnection();
   
   private String remoteHost = null;
   
   private AEConnection(String[] cuids)    {
      remoteAE.setInstalled(true);
      remoteAE.setAssociationAcceptor(true);
      remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });
      device.setNetworkApplicationEntity(ae);
      device.setNetworkConnection(conn);
      ae.setNetworkConnection(conn);
      ae.setAssociationInitiator(true);
      
      ae.setPackPDV(true);
      conn.setTcpNoDelay(true);
      ae.setMaxOpsInvoked(1);

      configureTransferCapability(cuids);
   }
   
   /**
    * @param aeProps
    */
   public AEConnection(String[] cuids, AESettings settings)    {
      this(cuids);
      remoteConn.setHostname(settings.getHostName());
      remoteConn.setPort(settings.getPort());
      remoteAE.setAETitle(settings.getRemoteTitle());
      ae.setAETitle(settings.getLocalTitle());
      remoteHost = settings.getHostName();
   }

   
   /**
    * @return <tt>Executor</tt>
    */
   public Executor getExecutor() {
      return executor;
   }


   /**
    * @return <tt>NetworkApplicationEntity</tt>
    */
   public NetworkApplicationEntity getAE() {
      return ae;
   }


   /**
    * @return <tt>NetworkConnection</tt>
    */
   public NetworkConnection getConnection() {
      return conn;
   }


   /**
    * @return <tt>NetworkApplicationEntity</tt>
    */
   public NetworkApplicationEntity getRemoteAE() {
      return remoteAE;
   }

   
   /**
    * @return remote host name
    */
   public String getRemoteHost()   {
      return remoteHost;
   }
   
   protected boolean getSemanticPersonNameMatching()   {
      return false;
    }
   

   protected boolean getRelationQR()   {
     return false;
   }

   /**
    * Configure the transfer capabilities to request - based on the type of the object being requested from.
    */
   private void configureTransferCapability(String[] cuids)
   {
       TransferCapability[] tc = new TransferCapability[cuids.length];
       int i=0;
       for(String cuid : cuids ) {
           tc[i++] = mkFindTC(cuid,NATIVE_LE_TS);
       }
       ae.setTransferCapability(tc);
   }
   
   /** Make a find transfer capability object for use in requesting the transfer capabilities. */
   private TransferCapability mkFindTC(String cuid, String[] ts) {
       ExtQueryTransferCapability tc = new ExtQueryTransferCapability(cuid,
               ts, TransferCapability.SCU);
       tc.setExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES, getRelationQR());
       tc.setExtInfoBoolean(ExtQueryTransferCapability.DATE_TIME_MATCHING, false);
       tc.setExtInfoBoolean(ExtQueryTransferCapability.FUZZY_SEMANTIC_PN_MATCHING,
               getSemanticPersonNameMatching());
       return tc;
   }
   
}