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
import org.dcm4che.util.UIDGenerator;

import org.jboss.system.ServiceMBeanSupport;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Comparator;

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
   private File spoolDir;
   private FilmSessionService filmSessionService = new FilmSessionService(this);
   private FilmBoxService filmBoxService = new FilmBoxService(this);
   private ImageBoxService imageBoxService = new ImageBoxService(this);
         
   private DcmObjectFactory dof = DcmObjectFactory.getInstance();
   private UIDGenerator uidgen = UIDGenerator.getInstance();
   private ObjectName printer;
   private ObjectName dcmServer;
   private DcmHandler dcmHandler;
   private AcceptorPolicy policy;
   private DcmServiceRegistry services;
   private String[] ts_uids = LITTLE_ENDIAN_TS;
   
   // Static --------------------------------------------------------
   
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
      return spoolDir.getPath();
   }
   
   /** Setter for property spoolDirPath.
    * @param spoolDirPath New value of property spoolDirPath.
    */
   public void setSpoolDirectory(String spoolDirectory) throws Exception {
      File tmp = new File(spoolDirectory);
      if (!tmp.exists()) {
         log.info("Create spool directory - " + tmp);
         tmp.mkdirs();
      }
      if (!tmp.isDirectory() || !tmp.canWrite()) {
         throw new IOException("No writeable spool directory - " + tmp);
      }
      spoolDir = tmp;
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
   }
   
   // Package protected ---------------------------------------------
   File getFilmSessionRootDir(String suid) {
      return new File(spoolDir, suid);
   }

   File getFilmSessionDir(String suid) {
      return new File(getFilmSessionRootDir(suid), "SESSION");
   }
   
   File getAttrFile(File dir) {
      return new File(dir, "ATTR");
   }
      
   File getFilmBoxDir(String suid, String fuid) {
      return new File(getFilmSessionDir(suid), fuid);
   }

   File getImageBoxFile(String suid, String fuid, String iuid) {
      return new File(getFilmBoxDir(suid, fuid), iuid);
   }

   File getHardCopyDir(String suid) {
      return new File(getFilmSessionRootDir(suid), "HC");
   }

   File getJobRootDir(String suid) {
      return new File(getFilmSessionRootDir(suid), "JOBS");
   }

   File getJobDir(String suid, String juid) {
      return new File(getJobRootDir(suid), juid);
   }
   
   void createFilmSession(String suid, Dataset ds)
      throws DcmServiceException
   {
      File dir = getFilmSessionRootDir(suid);
      if (dir.exists()) {
         throw new DcmServiceException(Status.DuplicateSOPInstance);
      }
      dir.mkdir();
      getJobRootDir(suid).mkdir();
      getHardCopyDir(suid).mkdir();
      File session = getFilmSessionDir(suid);
      session.mkdir();
      try {
         if (log.isDebugEnabled()) {
            log.debug("Store Session - " + getAttrFile(session));
         }
         writeDataset(getAttrFile(session), ds);
      } catch (DcmServiceException e) {
         rmdir(dir);
         throw e;
      }
   }
   
   void deleteFilmSession(String suid) {
      if (getJobRootDir(suid).list().length == 0) {
         rmdir(getFilmSessionRootDir(suid));
      } else {
         rmdir(getFilmSessionDir(suid));
      }      
   }
   
   void writeDataset(File f, Dataset data) throws  DcmServiceException {
      try {
         OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
         try {
            data.writeDataset(out,  DcmEncodeParam.EVR_LE);
         } finally {
            try { out.close(); } catch (IOException ignore) {}
         }
      } catch (IOException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
   }
   
   Dataset readDataset(File f) throws  DcmServiceException {
      Dataset data = dof.newDataset();
      try {
         InputStream in = new BufferedInputStream(new FileInputStream(f));
         try {
            data.readDataset(in, DcmEncodeParam.EVR_LE, -1);
         } finally {
            try { in.close(); } catch (IOException ignore) {}
         }
      } catch (IOException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
      return data;
   }   
   
   boolean rmdir(File dir) {
      if (dir.isDirectory()) {
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; ++i) {
            rmdir(files[i]);
         }
      }
      return dir.delete();
   }
   
   void createJob(String suid, String fuid) 
      throws DcmServiceException
   {
      File job = newJobDir(suid);
      log.info("Create job - " + job);
      createStoredPrint(job, suid, getFilmBoxDir(suid, fuid));
      sendJob(suid, job);
   }
      
   void createJob(String suid)
      throws DcmServiceException
   {
      File job = newJobDir(suid);
      log.info("Create job - " + job);
      File[] films = getFilmSessionDir(suid).listFiles();
      Arrays.sort(films,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o1).lastModified()
                          - ((File)o2).lastModified());
            }
         });
      for (int i = 1; i < films.length; ++i) {
         createStoredPrint(job, suid, films[i]);
      }
      sendJob(suid, job);
   }

   void deleteJob(File job) {
      log.info("Deleting job - " + job);
      if (!job.exists()) {
         log.warn("No such job - " + job);
         return;
      }
      if (!rmdir(job)) {
         log.warn("Failed to delete job - " + job);
      }
      File jobRootDir = job.getParentFile();
      File sessionRootDir = jobRootDir.getParentFile();
      File sessionDir = new File(sessionRootDir, "SESSION");
      if (jobRootDir.list().length == 0 && !sessionDir.exists()) {
         log.info("Purge Session - " + sessionRootDir);
         rmdir(sessionRootDir);
      }
   }
   
      
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private File newJobDir(String suid) {
      File dir = getJobDir(suid, uidgen.createUID());
      dir.mkdir();
      return dir;
   }
   
   private void sendJob(String suid, File job)
      throws DcmServiceException
   {
      try {
         Dataset session = readDataset(getAttrFile(getFilmSessionDir(suid)));
         TextMessage msg = queueSession.createTextMessage(job.getPath());
         msg.setIntProperty("NumberOfCopies", session.getInt(Tags.NumberOfCopies, 1));  
         msg.setStringProperty("MediumType", session.getString(Tags.MediumType)); 
         msg.setStringProperty("FilmDestination", session.getString(Tags.FilmDestination)); 
         queueSend.send(msg);
      } catch (JMSException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      } catch (DcmValueException e) {
         throw new DcmServiceException(Status.ProcessingFailure, e);
      }
   }
   
   private void createStoredPrint(File job, String suid, File filmBoxDir) 
      throws DcmServiceException
   {
      try {
         String cuid = UIDs.StoredPrintStorage;
         String iuid = uidgen.createUID();
         Dataset sp = dof.newDataset();
         sp.setFileMetaInfo(
            dof.newFileMetaInfo(cuid, iuid, UIDs.ExplicitVRLittleEndian));
         sp.putUI(Tags.SOPClassUID, cuid);
         sp.putUI(Tags.SOPInstanceUID, iuid);
         sp.putDA(Tags.StudyDate);
         sp.putTM(Tags.StudyTime);
         sp.putSH(Tags.AccessionNumber);
         sp.putCS(Tags.Modality, "STORED_PRINT");
         sp.putLO(Tags.Manufacturer, "TIANI MEDGRAPH AG");
         sp.putPN(Tags.PatientName);
         sp.putLO(Tags.PatientID);
         sp.putDA(Tags.PatientBirthDate);
         sp.putCS(Tags.PatientSex);
         sp.putUI(Tags.StudyInstanceUID, suid);
         sp.putUI(Tags.SeriesInstanceUID, uidgen.createUID());
         sp.putSH(Tags.StudyID);
         sp.putIS(Tags.SeriesNumber);
         sp.putIS(Tags.InstanceNumber);


         if (log.isDebugEnabled()) {
            log.debug("Read FilmBox - " + getAttrFile(filmBoxDir));
         }
         Dataset filmbox = readDataset(getAttrFile(filmBoxDir));
         filmbox.remove(Tags.RefFilmSessionSeq);
         DcmElement refImageBoxSeq = filmbox.remove(Tags.RefImageBoxSeq);
         String iboxCUID = refImageBoxSeq.getItem().getString(Tags.RefSOPClassUID);      
         DcmElement capabilities = sp.putSQ(Tags.PrintManagementCapabilitiesSeq);
         capabilities.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.BasicFilmSession);
         capabilities.addNewItem().putUI(Tags.RefSOPClassUID, UIDs.BasicFilmBoxSOP);
         capabilities.addNewItem().putUI(Tags.RefSOPClassUID, iboxCUID);
         capabilities.addNewItem().putUI(Tags.RefSOPClassUID, 
            iboxCUID.equals(UIDs.BasicGrayscaleImageBox)
               ? UIDs.HardcopyGrayscaleImageStorage
               : UIDs.HardcopyColorImageStorage);

         sp.putSQ(Tags.PrinterCharacteristicsSeq);
         sp.putSQ(Tags.FilmBoxContentSeq).addItem(filmbox);

         File[] iboxfiles = filmBoxDir.listFiles();
         Arrays.sort(iboxfiles,
            new Comparator() {
               public int compare(Object o1, Object o2) {
                  return (int)(((File)o1).lastModified()
                             - ((File)o2).lastModified());
               }
            });
         Dataset[] imgboxes = new Dataset[refImageBoxSeq.vm()];
         for (int i = iboxfiles.length - 1; i > 0; --i) {
            if (log.isDebugEnabled()) {
               log.debug("Read ImageBox - " + iboxfiles[i]);
            }
            Dataset imgbox = readDataset(iboxfiles[i]);
            int ipos = imgbox.getInt(Tags.ImagePositionOnFilm, -1);
            if (ipos <= 0 || ipos > imgboxes.length) {
               throw new RuntimeException("ipos: " + ipos);
            }
            if (imgboxes[ipos-1] == null) {
               imgboxes[ipos-1] = imgbox;
            }
         }

         DcmElement contentSeq = sp.putSQ(Tags.ImageBoxContentSeq);
         for (int i = 0; i < imgboxes.length; ++i) {
            if (imgboxes[i] != null && imgboxes[i].contains(Tags.RefImageSeq)) {
               contentSeq.addItem(imgboxes[i]);
            }
         }

         File f = new File(job, iuid);
         OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
         try {
            sp.writeFile(out, null);
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
