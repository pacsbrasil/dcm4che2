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

import com.tiani.prnscp.print.PrintJobNotification;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.ServerConfigLocator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.TextMessage;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Iterator;

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
   implements PrintScpServiceMBean, NotificationListener {
   
   // Constants -----------------------------------------------------   
   private final String[] LITTLE_ENDIAN_TS = {
        UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian
   };
   private final String[] ONLY_DEFAULT_TS = {
        UIDs.ImplicitVRLittleEndian
   };
   public static final NotificationFilter NOTIF_FILTER = new NotificationFilter() {
      public boolean isNotificationEnabled(Notification n) {
         return n.getType().equals(PrintJobNotification.TYPE);
      }
   };
   
   // Attributes ----------------------------------------------------
   private QueueConnection queueConn;
   private QueueSession queueSession;
   private QueueSender queueSend;
   private String queueName;
   private String spoolDirectory;
   private File spoolDir;
   private FilmSessionService filmSessionService = new FilmSessionService(this);
   private FilmBoxService filmBoxService = new FilmBoxService(this);
   private ImageBoxService imageBoxService = new ImageBoxService(this);
         
   private ObjectName printer;
   private ObjectName dcmServer;
   private DcmHandler dcmHandler;
   private AcceptorPolicy policy;
   private DcmServiceRegistry services;
   private String[] ts_uids = LITTLE_ENDIAN_TS;
   private int jobCounter = 0;
   
   // Static --------------------------------------------------------
   static final DcmObjectFactory dof = DcmObjectFactory.getInstance();
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws MalformedObjectNameException {
      this.server = server;
      return name == null
      ? new ObjectName("dcm4chex:service=PrintSCP")
      : name;
   }
   
   public String getName() {
      return "PrintSCP";
   }
   
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
   
   /** Getter for property printer.
    * @return Value of property printer.
    */
   public ObjectName getPrinter() {
      return printer;
   }
   
   /** Setter for property printer.
    * @param printer New value of property printer.
    */
   public void setPrinter(ObjectName printer) {
      this.printer = printer;
   }
     
   /** Getter for property queueName.
    * @return Value of property queueName.
    */
   public String getQueueName() {
      return queueName;
   }
   
   /** Setter for property queueName.
    * @param queueName New value of property queueName.
    */
   public void setQueueName(String queueName) {
      if (queueName.length() == 0) {
         throw new IllegalArgumentException();
      }
      this.queueName = queueName;
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
   
   // NotificationListener implementation -----------------------------
   public void handleNotification(Notification n, Object handback) {
      PrintJobNotification pjn = (PrintJobNotification) n;
      log.info("handleNotification - eventID:" + pjn.getEventID());
      switch (pjn.getEventID()) {
         case PrintJobNotification.FAILURE:
         case PrintJobNotification.DONE:
            deleteJob(new File(pjn.getMessage()));
            break;
      }
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
      
      server.addNotificationListener(printer, this, NOTIF_FILTER, null);
      
      Context iniCtx = new InitialContext();
      QueueConnectionFactory qcf = 
         (QueueConnectionFactory) iniCtx.lookup("ConnectionFactory");
      queueConn = qcf.createQueueConnection();
      queueSession = 
         queueConn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);      
      Queue que = (Queue) iniCtx.lookup("queue/" + queueName);
      queueSend = queueSession.createSender(que);
      queueConn.start();

      dcmHandler = (DcmHandler)server.getAttribute(dcmServer, "DcmHandler");
      services = dcmHandler.getDcmServiceRegistry();
      services.bind(UIDs.BasicFilmSession, filmSessionService);
      services.bind(UIDs.BasicFilmBoxSOP, filmBoxService);
      services.bind(UIDs.BasicColorImageBox, imageBoxService);
      services.bind(UIDs.BasicGrayscaleImageBox, imageBoxService);
      services.bind(UIDs.Printer, printerService);
      policy = dcmHandler.getAcceptorPolicy();
      policy.putPresContext(UIDs.BasicGrayscalePrintManagement, ts_uids);
      policy.putPresContext(UIDs.BasicColorPrintManagement, ts_uids);
   }
   
   public void stopService()
      throws Exception
   {
      policy.putPresContext(UIDs.BasicGrayscalePrintManagement, null);
      policy.putPresContext(UIDs.BasicColorPrintManagement, null);
      policy.putPresContext(UIDs.PresentationLUT, null);
      policy = null;
      services.unbind(UIDs.BasicFilmSession);
      services.unbind(UIDs.BasicFilmBoxSOP);
      services.unbind(UIDs.BasicColorImageBox);
      services.unbind(UIDs.BasicGrayscaleImageBox);
      services.unbind(UIDs.Printer);
      services.unbind(UIDs.PresentationLUT);
      services = null;
      dcmHandler = null;

      queueSend.close();
      queueSend = null;
      queueConn.stop();
      queueSession.close();
      queueSession = null;
      queueConn.close();
      queueConn = null;
      
      server.removeNotificationListener(printer, this);
      
      cleardir(spoolDir);
   }
   
   
   
   // Package protected ---------------------------------------------
 
   
   FilmSession getFilmSession(ActiveAssociation as) {
      return (FilmSession) as.getAssociation().getProperty("FilmSession");
   }
   
   File getSessionSpoolDir(String uid) {
      return new File(spoolDir, uid);
   }
   
   void initSessionSpoolDir(File dir) throws DcmServiceException {
      if (!dir.mkdir() || !lockSessionSpoolDir(dir)
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

   void unlockSessionSpoolDir(File dir) {
      new File(dir, SPOOL_SESSION_LOCK_SUFFIX).delete();
      if (countJobsInSession(dir) == 0) {
         deltree(dir);
      }
   }

   private boolean isSessionSpoolDirLocked(File dir) {
      return new File(dir, SPOOL_SESSION_LOCK_SUFFIX).exists();
   }

   private int countJobsInSession(File dir) {
      return new File(dir, SPOOL_JOB_DIR_SUFFIX).list().length;
   }
   
   private void purgeSessionSpoolDir(File dir) {
      if (!isSessionSpoolDirLocked(dir) && countJobsInSession(dir) == 0) {
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

   int countImageBoxes(String format)
      throws DcmServiceException
   {
      if (format == null) {
         throw new DcmServiceException(Status.MissingAttribute);
      }
      try {
         Integer n = (Integer) server.invoke(printer, "countImageBoxes", 
                        new Object[] { format },
                        new String[] { "java.lang.String" });
         return n.intValue();
      } catch (IllegalArgumentException e) {
         throw new DcmServiceException(Status.InvalidAttributeValue, e);
      } catch (Exception e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
   }
   
   void createPrintJob(FilmSession session, boolean all)
      throws DcmServiceException
   {
      String jobID = "J-" + ++jobCounter;
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
         try {
            Dataset ds = session.getDataset();
            TextMessage msg = queueSession.createTextMessage(jobdir.getPath());
            msg.setIntProperty("NumberOfCopies", ds.getInt(Tags.NumberOfCopies, 1));  
            msg.setStringProperty("MediumType", ds.getString(Tags.MediumType)); 
            msg.setStringProperty("FilmDestination", ds.getString(Tags.FilmDestination)); 
            queueSend.send(msg);
         } catch (JMSException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
         } catch (DcmValueException e) {
            throw new DcmServiceException(Status.ProcessingFailure, e);
         }
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
      if (!deltree(job)) {
         log.warn("Failed to delete job - " + job.getName());
      }
      purgeSessionSpoolDir(job.getParentFile().getParentFile());
   }
   
      
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private void storePrint(File job, FilmSession session, FilmBox filmBox) 
      throws DcmServiceException
   {
      try {
         Dataset storedPrint = filmBox.createStoredPrint(session);
         File f = File.createTempFile("SP-","",job);
         OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
         try {
            storedPrint.writeFile(out, null);
         } finally {
            try { out.close(); } catch (IOException ignore) {}
         }
      } catch (DcmValueException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      } catch (IOException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
   }
   

   // Inner classes -------------------------------------------------      
   private DcmServiceBase printerService = new DcmServiceBase(){
      protected Dataset doNGet(ActiveAssociation as, Dimse rq, Command rspCmd)
         throws IOException, DcmServiceException
      {
         Dataset result = dof.newDataset();
         try {
            result.putCS(Tags.PrinterStatus,
               (String)server.getAttribute(printer, "Status"));
            result.putCS(Tags.PrinterStatusInfo,
               (String)server.getAttribute(printer, "StatusInfo"));
         } catch (Exception e) {
            log.error("Failed to access printer status", e);
            throw new DcmServiceException(Status.ProcessingFailure, 
               "Failed to access printer status");
         }
         return result;
      }      
   };
}
