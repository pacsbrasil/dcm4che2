/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.scp;

import com.tiani.prnscp.print.PrinterServiceMBean;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 3, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public class PrintScpService
   extends ServiceMBeanSupport
   implements PrintScpServiceMBean {
   
   // Constants -----------------------------------------------------   
   private final String[] LITTLE_ENDIAN_TS = {
        UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian
   };
   private final String[] ONLY_DEFAULT_TS = {
        UIDs.ImplicitVRLittleEndian
   };
   
   // Attributes ----------------------------------------------------
   private String spoolDirectory;
   private File spoolDir;
   private boolean keepSpoolFiles = false;
   private FilmSessionService filmSessionService = new FilmSessionService(this);
   private FilmBoxService filmBoxService = new FilmBoxService(this);
   private ImageBoxService imageBoxService = new ImageBoxService(this);

   private HashMap printerMap = new HashMap();
   private ObjectName dcmServer;
   private DcmHandler dcmHandler;
   private String[] ts_uids = LITTLE_ENDIAN_TS;
   private int numCreatedJobs = 0;
   private int numStoredPrints = 0;
            
   // Static --------------------------------------------------------
   static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   static final AssociationFactory asf = AssociationFactory.getInstance();
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // PrintScpMBean implementation ----------------------------------
   
   /** Getter for property dcmServer.
    * @return Value of property dcmServer.
    */
   public ObjectName getDcmServer() {
      return dcmServer;
   }
   
   /** Setter for property dcmServer.
    * @param dcmServer New value of property dcmServer.
    */
   public void setDcmServer(ObjectName dcmServer) {
      this.dcmServer = dcmServer;
   }
           
   /** Getter for property spoolDirPath.
    * @return Value of property spoolDirPath.
    */
   public String getSpoolDirectory() {
      return spoolDirectory;
   }
   
   /** Setter for property spoolDirPath.
    * @param spoolDirPath New value of property spoolDirPath.
    */
   public void setSpoolDirectory(String spoolDirectory) {
      this.spoolDirectory = spoolDirectory;
   }
   
   /** Getter for property keepSpoolFiles.
    * @return Value of property keepSpoolFiles.
    */
   public boolean isKeepSpoolFiles() {
      return keepSpoolFiles;
   }   

   /** Setter for property keepSpoolFiles.
    * @param keepSpoolFiles New value of property keepSpoolFiles.
    */
   public void setKeepSpoolFiles(boolean keepSpoolFiles) {
      this.keepSpoolFiles = keepSpoolFiles;
   }
   
   /** Getter for property numCreatedJobs.
    * @return Value of property numCreatedJobs.
    */
   public int getNumCreatedJobs() {
      return numCreatedJobs;
   }
   
   /** Getter for property numStoredPrints.
    * @return Value of property numStoredPrints.
    */
   public int getNumStoredPrints() {
      return numStoredPrints;
   }
      
   // NotificationListener implementation -----------------------------
   private final NotificationListener printingListener =
      new NotificationListener() {
         public void handleNotification(Notification n, Object handback) {
         }
      };
      
   private final NotificationListener doneListener =
      new NotificationListener() {
         public void handleNotification(Notification n, Object handback) {
           deleteJob(new File(n.getMessage()));
         }
      };

   private final NotificationListener failureListener =
      new NotificationListener() {
         public void handleNotification(Notification n, Object handback) {
           deleteJob(new File(n.getMessage()));
         }
      };
   
   private NotificationFilter makeNotificationFilter(String type) {
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(type);
      return filter;
   }

   private NotificationFilter makeNotificationFilter(String type1, String type2) {
      NotificationFilterSupport filter = new NotificationFilterSupport();
      filter.enableType(type1);
      filter.enableType(type2);
      return filter;
   }
   
   // ServiceMBeanSupport overrides -----------------------------------
   public void startService()
   throws Exception
   {
      spoolDir = new File(spoolDirectory);
      if (!spoolDir.isAbsolute()) {
         File systemHomeDir = ServerConfigLocator.locate().getServerHomeDir();
         spoolDir = new File(systemHomeDir, spoolDirectory);
      }
      if (!spoolDir.exists()) {
         log.info("Creating spool directory - " + spoolDir.getCanonicalPath());
         spoolDir.mkdirs();
      }
      if (!spoolDir.isDirectory() || !spoolDir.canWrite()) {
         throw new IOException("No writeable spool directory - " + spoolDir);
      }
      cleardir(spoolDir);
      queryPrinters();
      dcmHandler = (DcmHandler)server.getAttribute(dcmServer, "DcmHandler");
      try {
         addNotificationListeners();
         bindServices();
         enableServices();
      } catch (Exception e) {
         try { stopService(); } catch (Exception ignore) {} 
         throw e;
      }      
   }
   
   private void queryPrinters()
      throws Exception 
   {
      Set printerNames = server.queryNames(
         new ObjectName("dcm4chex:service=Printer,*"), null);
      printerMap.clear();
      for (Iterator it = printerNames.iterator(); it.hasNext();) {
         ObjectName printer = (ObjectName) it.next();
         String aet = printer.getKeyProperty("aet");
         if (aet == null || aet.length() == 0) {
            throw new IllegalArgumentException(
               "Missing aet property in printer object name - " + printer);
         }
         if (printerMap.containsKey(aet)) {
            throw new IllegalArgumentException(
               "Duplicate printer aet - " + aet);
         }
         printerMap.put(aet, printer);
      }
   }

   private void addNotificationListeners()
      throws Exception
   {
      for (Iterator it = printerMap.values().iterator(); it.hasNext();) {
         ObjectName printer = (ObjectName) it.next();
         server.addNotificationListener(getServiceName(), printer,
            makeNotificationFilter(
               PrinterServiceMBean.NOTIF_SCHEDULE_COLOR,
               PrinterServiceMBean.NOTIF_SCHEDULE_GRAY),
            null);
         server.addNotificationListener(printer, printingListener, 
            makeNotificationFilter(PrinterServiceMBean.NOTIF_PRINTING), null);
         server.addNotificationListener(printer, doneListener, 
            makeNotificationFilter(PrinterServiceMBean.NOTIF_DONE), null);
         server.addNotificationListener(printer, failureListener, 
            makeNotificationFilter(PrinterServiceMBean.NOTIF_FAILURE), null);
      }
   }

   private void removeNotificationListeners()
      throws Exception
   {
      for (Iterator it = printerMap.values().iterator(); it.hasNext();) {
         ObjectName printer = (ObjectName) it.next();
         server.removeNotificationListener(getServiceName(), printer);
         server.removeNotificationListener(printer, printingListener);
         server.removeNotificationListener(printer, doneListener);
         server.removeNotificationListener(printer, failureListener);
      }
   }
   
   private void bindServices() {
      DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
      services.bind(UIDs.BasicFilmSession, filmSessionService);
      services.bind(UIDs.BasicFilmBoxSOP, filmBoxService);
      services.bind(UIDs.BasicColorImageBox, imageBoxService);
      services.bind(UIDs.BasicGrayscaleImageBox, imageBoxService);
      services.bind(UIDs.Printer, printerService);
      services.bind(UIDs.PresentationLUT, plutService);
   }
   
   private void unbindServices() {
      DcmServiceRegistry services = dcmHandler.getDcmServiceRegistry();
      services.unbind(UIDs.BasicFilmSession);
      services.unbind(UIDs.BasicFilmBoxSOP);
      services.unbind(UIDs.BasicColorImageBox);
      services.unbind(UIDs.BasicGrayscaleImageBox);
      services.unbind(UIDs.Printer);
      services.unbind(UIDs.PresentationLUT);
   }

   
   private void enableServices()
      throws Exception
   {
      AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
      for (Iterator it = printerMap.keySet().iterator(); it.hasNext();) {
         String aet = (String) it.next();
         policy.putPolicyForCalledAET(aet, makeAcceptorPolicy(aet));
         log.info("Enabled Print Service with AET: " + aet);
      }
   }
   
   private AcceptorPolicy makeAcceptorPolicy(String aet)
      throws Exception
   {
      AcceptorPolicy policy = asf.newAcceptorPolicy();
      if (getBooleanPrinterAttribute(aet, "SupportsGrayscale")) {
         policy.putPresContext(UIDs.BasicGrayscalePrintManagement, ts_uids);
         if (getBooleanPrinterAttribute(aet, "SupportsPresentationLUT")) {
            policy.putPresContext(UIDs.PresentationLUT, ts_uids);
         }
      }
      if (getBooleanPrinterAttribute(aet, "SupportsColor")) {
         policy.putPresContext(UIDs.BasicColorPrintManagement, ts_uids);
      }
//      if (getBooleanPrinterAttribute(aet, "SupportsAnnotationBox")) {
//         policy.putPresContext(UIDs.BasicAnnotationBox, ts_uids);
//      }
      return policy;
   }

   private void disableServices() {
      AcceptorPolicy policy = dcmHandler.getAcceptorPolicy();
      for (Iterator it = printerMap.keySet().iterator(); it.hasNext();) {
         String aet = (String) it.next();
         policy.putPolicyForCalledAET(aet, null);
         log.info("Disabled Print Service with AET: " + aet);
      }
   }
   
   public void stopService()
      throws Exception
   {
      disableServices();
      unbindServices();
      removeNotificationListeners();
      dcmHandler = null;
      if (!keepSpoolFiles) {
         cleardir(spoolDir);
      }
   }      
   
   // Package protected ---------------------------------------------
   
   String checkAttributeValue(String aet, String test, String val, boolean type1)
      throws DcmServiceException
   {
      if (val == null) {
         if (type1) {
            throw new DcmServiceException(Status.MissingAttributeValue);
         }
         return null;
      }
      try {
         if (invokeBooleanOnPrinter(aet, test,
               new Object[]{ val },
               new String[]{ String.class.getName() }))
         {
            return val;
         }
      } catch (Exception e) {
         log.error("Failed to checkAttributeValue " + test, e);
         throw new DcmServiceException(Status.ProcessingFailure);
      }
      throw new DcmServiceException(Status.InvalidAttributeValue);
   }

   String checkImageDisplayFormat(String aet, String val, String orientation)
      throws DcmServiceException
   {
      if (val == null) {
         throw new DcmServiceException(Status.MissingAttributeValue);
      }
      try {
         if (orientation == null) {
            orientation = 
               (String) getPrinterAttribute(aet, "DefaultFilmOrientation");
         }
         if (invokeBooleanOnPrinter(aet, "isSupportsDisplayFormat",
               new Object[]{ val, orientation },
               new String[]{ String.class.getName(), String.class.getName() }))
         {
            return val;
         }
      } catch (Exception e) {
         log.error("Failed to checkImageDisplayFormat:", e);
         throw new DcmServiceException(Status.ProcessingFailure);
      }
      throw new DcmServiceException(Status.InvalidAttributeValue);
   }

   private Object getPrinterAttribute(String aet, String attribute)
      throws Exception
   {
      ObjectName printer = (ObjectName) printerMap.get(aet);
      if (printer == null) {
         throw new IllegalArgumentException("No printer with aet: " + aet);
      }
      return server.getAttribute(printer, attribute);
   }

   private boolean getBooleanPrinterAttribute(String aet, String attribute)
      throws Exception
   {
      Boolean b = (Boolean) getPrinterAttribute(aet, attribute);
      return b.booleanValue();
   }

   private int getIntPrinterAttribute(String aet, String attribute)
      throws Exception
   {
      Integer i = (Integer) getPrinterAttribute(aet, attribute);
      return i.intValue();
   }
   
   boolean invokeBooleanOnPrinter(String aet, String methode,
         Object[] arg, String[] type)
      throws Exception
   {
      ObjectName printer = (ObjectName) printerMap.get(aet);
      if (printer == null) {
         throw new IllegalArgumentException("No printer with aet: " + aet);
      }
      Boolean b = (Boolean) server.invoke(printer, methode, arg, type);
      return b.booleanValue();
   }
       
   FilmSession getFilmSession(ActiveAssociation as) {
      return (FilmSession) as.getAssociation().getProperty("FilmSession");
   }

   HashMap getPresentationLUTs(ActiveAssociation as) {
      Association a = as.getAssociation();
      HashMap result = (HashMap) a.getProperty("PresentationLUTs");
      if (result == null) {
         a.putProperty("PresentationLUTs", result = new HashMap());
      }
      return result;
   }
   
   File getSessionSpoolDir(Association a, String uid) {
      File dir = new File(spoolDir, a.getCalledAET());
      dir = new File(dir, a.getCallingAET());
      return new File(dir, uid); 
   }
   
   void initSessionSpoolDir(File dir) throws DcmServiceException {
      log.info("Create Spool Directory for Film Session[uid="
         + dir.getName() + "]");
      if (!dir.mkdirs() || !lockSessionSpoolDir(dir)
         || !new File(dir, SPOOL_HARDCOPY_DIR_SUFFIX).mkdir()
         || !new File(dir, SPOOL_JOB_DIR_SUFFIX).mkdir())
      {
         deltree(dir);
         throw new DcmServiceException(Status.ProcessingFailure,
            "Failed to initalize spool directory: " + dir);
      }
   }

   private boolean lockSessionSpoolDir(File dir) {
      try {
         new File(dir, SPOOL_SESSION_LOCK_SUFFIX).createNewFile();
         return true;
      } catch (IOException e) {
         return false;
      }
   }

   private int countJobsInSession(File dir) {
      return new File(dir, SPOOL_JOB_DIR_SUFFIX).list().length;
   }
   
   void purgeSessionSpoolDir(File dir, boolean unlock) {
      File lock = new File(dir, SPOOL_SESSION_LOCK_SUFFIX);
      if (unlock) {
         lock.delete();
      } else if (lock.exists()) {
         return;
      }
      if (!keepSpoolFiles && countJobsInSession(dir) == 0) {
         log.info("Delete Spool Directory for Film Session[uid="
            + dir.getName() + "]");
         deltree(dir);
      }
   }
     
   void cleardir(File dir) {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; ++i) {
         deltree(files[i]);
      }
   }
   
   boolean deltree(File dir) {
      if (dir.isDirectory()) {
         cleardir(dir);
      }
      return dir.delete();
   }

   void createPrintJob(FilmSession session, boolean all)
      throws DcmServiceException
   {
      String jobID = "J-" + ++numCreatedJobs;
      File jobdir = new File(new File(session.dir(), SPOOL_JOB_DIR_SUFFIX), jobID);
      if (!jobdir.mkdir()) {
         throw new DcmServiceException(Status.ProcessingFailure,
            "Failed write access to spool directory: " + spoolDir);
      }
      log.info("Create job: " + jobID);
      try {
         if (all) {
            Iterator it = session.getFilmBoxes().values().iterator();
            while (it.hasNext()) {
               storePrint(jobdir, session, (FilmBox) it.next());
            }
         } else {
            storePrint(jobdir, session, session.getCurrentFilmBox());
         }
         Notification notif = new Notification(
            session.isColor()
               ? PrinterServiceMBean.NOTIF_SCHEDULE_COLOR
               : PrinterServiceMBean.NOTIF_SCHEDULE_GRAY, 
               this, numCreatedJobs, jobdir.getPath());
         Dataset sessionAttr = dof.newDataset();
         sessionAttr.putAll(session.getAttributes());
         notif.setUserData(sessionAttr);
         sendNotification(notif);
      } catch (DcmServiceException e) {
         deltree(jobdir);
         throw e;
      }
   }
   
   void deleteJob(File job) {
      log.info("Deleting job - " + job.getName());
      if (!job.exists()) {
         log.warn("No such job - " + job.getName());
         return;
      }
      if (keepSpoolFiles) {
         return;
      }
      if (!deltree(job)) {
         log.warn("Failed to delete job - " + job.getName());
      }
      purgeSessionSpoolDir(job.getParentFile().getParentFile(), false);
   }
   
      
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private void storePrint(File job, FilmSession session, FilmBox filmBox) 
      throws DcmServiceException
   {
      String spID = "SP-" + ++numStoredPrints;
      try {
         Dataset storedPrint = filmBox.createStoredPrint(session);
         File f = new File(job, spID);
         OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
         try {
            storedPrint.writeFile(out, null);
         } finally {
            try { out.close(); } catch (IOException ignore) {}
         }
      } catch (IOException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
   }
      
   // Inner classes -------------------------------------------------      
   private DcmServiceBase plutService = new DcmServiceBase(){
      protected Dataset doNCreate(ActiveAssociation as, Dimse rq, Command rspCmd)
         throws IOException, DcmServiceException 
      {
         try {
            Dataset ds = rq.getDataset(); // read out dataset
            String uid = rspCmd.getAffectedSOPInstanceUID();            
            log.info("Creating Presentation LUT[uid=" + uid + "]");         
            HashMap pluts = getPresentationLUTs(as);
            if (pluts.get(uid) != null) {
               throw new DcmServiceException(Status.DuplicateSOPInstance);
            }
            // add SOP Instane UID for use as Presentation LUT Content Seq Item
            // in Stored Print Object
            ds.putUI(Tags.SOPInstanceUID, uid);
            pluts.put(uid, ds);
            log.info("Created Presentation LUT[uid=" + uid + "]");         
            return null;
         } catch (DcmServiceException e) {
            log.warn("Failed to create Presentation LUT SOP Instance", e);
            throw e;
         }
      }
      
      protected Dataset doNDelete(ActiveAssociation as, Dimse rq, Command rspCmd)
         throws IOException, DcmServiceException 
      {
         try {
            String uid = rq.getCommand().getRequestedSOPInstanceUID();
            HashMap pluts = getPresentationLUTs(as);
            if (pluts.get(uid) == null) {
               throw new DcmServiceException(Status.NoSuchObjectInstance);
            }
            pluts.remove(uid);
            return null;
         } catch (DcmServiceException e) {
            log.warn("Failed to delete Presentation LUT SOP Instance", e);
            throw e;
         }
      }
   };

   private DcmServiceBase printerService = new DcmServiceBase(){
      protected Dataset doNGet(ActiveAssociation as, Dimse rq, Command rspCmd)
         throws IOException, DcmServiceException
      {
         Dataset result = dof.newDataset();
         try {
            String aet = as.getAssociation().getCalledAET();
            result.putCS(Tags.PrinterStatus,
               (String) getPrinterAttribute(aet, "Status"));
            result.putCS(Tags.PrinterStatusInfo,
               (String) getPrinterAttribute(aet, "StatusInfo"));
            result.putDA(Tags.DateOfLastCalibration,
               (String) getPrinterAttribute(aet, "DateOfLastCalibration"));
            result.putTM(Tags.TimeOfLastCalibration,
               (String) getPrinterAttribute(aet, "TimeOfLastCalibration"));
         } catch (Exception e) {
            log.error("Failed to access printer status", e);
            throw new DcmServiceException(Status.ProcessingFailure, 
               "Failed to access printer status");
         }
         return result;
      }      
   };
}
