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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.TransferCapability;
import org.dcm4chee.xero.dicom.ApplicationEntityProvider;
import org.dcm4chee.xero.dicom.DicomCommandChecker;
import org.dcm4chee.xero.dicom.DicomConnector;
import org.dcm4chee.xero.dicom.DicomException;
import org.dcm4chee.xero.dicom.DicomURLHandler;
import org.dcm4chee.xero.dicom.FilesystemWatcher;
import org.dcm4chee.xero.dicom.SOPClassUIDs;
import org.dcm4chee.xero.dicom.TransferCapabilitySelector;
import org.dcm4chee.xero.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* URL connection which will parse in the DICOM information from the URI 
* and return a URL connection that will be able to retrieve the data.
* <p>
* For consistency sake the DICOM URI syntax that is used in the Agility
* project is used to represent the DICOM locations:
* <p>
* dicom://<AE Title>@<host>:<port>/&uid=<study instance UID>[& level=STUDY]
* @author Andrew Cowan (amidx)
*/
public class CMoveURLConnection extends URLConnection
{
   private static Logger log = LoggerFactory.getLogger(CMoveURLConnection.class);
   
   private static final String DEFAULT_QUERY_LEVEL = "IMAGE"; //"SERIES";

   private static DicomConnector dicomConnector = new DicomConnector();
   
   private DicomURLHandler urlHandler;
   private TransferCapabilitySelector tcs;
   private ApplicationEntityProvider aes;

   private FilesystemWatcher fileSystemWatcher;
   private Future<InputStream> futureInputStream;
   
   private DicomCommandChecker responseChecker;
   private CMoveSettings settings;
   
   public CMoveURLConnection(URL dicomURL)
   {
      super(dicomURL);
      
      this.settings = new CMoveSettings(dicomURL);
      this.aes = new ApplicationEntityProvider();
      this.tcs = new TransferCapabilitySelector();
      this.fileSystemWatcher = new FilesystemWatcher();
      this.urlHandler = new DicomURLHandler();
      this.responseChecker = new DicomCommandChecker();
   }
   
   /**
    * Perform a C-MOVE on the remote location.
    * The DICOM response will not be checked until the {@link #getInputStream()} method is inovked.
    * @see java.net.URLConnection#connect()
    */
   @Override
   public void connect() throws IOException
   {
      String fileName = DicomURLHandler.parseQueryParameters(getURL()).get("objectUID");
      File file = new File(settings.getDestinationPath() ,fileName);
      
      try
      {
         if(! file.exists())
         {
            log.info("DICOM file is not yet available.  C-MOVE will be initiated for "+fileName);
            DimseRSP dicomResponse = cmove(); // Might want to put something in the file system to indicate series level queries
            
            // Exception will be thrown if the response is an error code.
            while(dicomResponse.next())
               responseChecker.isSuccess(dicomResponse.getCommand());
            
            log.debug("C-MOVE successful");
         }
         
         // Create the input stream 
         log.info("{} to open file {}",file.exists() ? "Starting" : "Waiting", file.toString());
         futureInputStream = fileSystemWatcher.openWhenAvailable(file);
      }
      catch(IOException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new IOException("C-MOVE failed for "+getURL()+", error="+e);
      }
      finally
      {
         // TODO: Check result and if it's not successful, then stop polling file.
      }
      
      
   }

   /**
    * @throws IOException
    */
   private DimseRSP cmove() throws IOException
   {
      NetworkApplicationEntity remoteAE = aes.getAE(getURL());
      NetworkApplicationEntity localAE = aes.getLocalAE(remoteAE.getAETitle(),SOPClassUIDs.CMove);

      Association association = null;
      try
      {
         log.info("Performing a C-MOVE from {} to {}",remoteAE.getAETitle(),settings.getDestinationAET());
         association = dicomConnector.connect(localAE, remoteAE);
         
         String transferSyntaxUID;
         TransferCapability capability = tcs.selectTransferCapability(association,SOPClassUIDs.CMove);
         if(capability == null || (transferSyntaxUID = tcs.selectBestTransferSyntaxUID(capability)) == null)
            throw new DicomException("Could not negotiate a transfer capability between "+localAE.getAETitle()+" and "+remoteAE.getAETitle());
         log.debug("Transfer syntax of {} was selected.",transferSyntaxUID);
         
         // Query at a series level to avoid excessive requests.
         DicomObject query = urlHandler.createDicomQuery(getURL());
         query.putString(Tag.QueryRetrieveLevel, VR.CS, DEFAULT_QUERY_LEVEL);
         
         // Set query level from transfer capability
         
         DimseRSP result = association.cmove(
               capability.getSopClass(),
               CommandUtils.HIGH,
               query,
               transferSyntaxUID,
               settings.getDestinationAET());

         return result;

      }
      catch(InterruptedException e)
      {
         log.debug("Interrupted during a C-MOVE from AE {}",remoteAE.getAETitle());
         
         //mark the state or throw an exception???
         throw new IOException("Interrupted: "+e);
      }
      catch (ConfigurationException e)
      {
         throw new IOException("DICOM configuration problems: "+e);
      }
      finally 
      {
         dicomConnector.release(association, true);
      }
   }
   
   
   @Override
   public InputStream getInputStream() 
      throws IOException
   {
      if(futureInputStream == null)
         throw new IOException("Unable to read DICOM file for "+getURL());
      
      int msTimeout = 30 * 1000;
      try
      {
         return futureInputStream.get(msTimeout,TimeUnit.MILLISECONDS);
      }
      catch(InterruptedException e)
      {
         throw new IOException("Thread interrupted before file was available "+e);
      }
      catch(ExecutionException e)
      {
         throw new IOException("Unable to open stream to file: "+e.getCause());
      }
      catch(TimeoutException e)
      {
         throw new SocketTimeoutException("Waited for a total of "+msTimeout+" ms");
      }
      finally
      {
         // Make sure that we stop listening for the file.
         if(!futureInputStream.isDone())
            futureInputStream.cancel(true);
      }
   }

}
