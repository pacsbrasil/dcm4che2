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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmObjectFactory;
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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.Message;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
      return spoolDir.getAbsolutePath();
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
   public void handleNotification(Notification notification, Object handback) {
   }

   // ServiceMBeanSupport overrides -----------------------------------
   public void startService()
   throws Exception
   {
      server.addNotificationListener(printer, this, null, null);
      
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
   File getFilmSessionDir(String suid) {
      return new File(spoolDir, suid);
   }

   File getFilmBoxDir(String suid, String fuid) {
      return new File(new File(getFilmSessionDir(suid), "FILMS"), fuid);
   }

   File getHardCopyDir(String suid) {
      return new File(getFilmSessionDir(suid), "HC");
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
   
   boolean rmdir(File dir) {
      if (dir.isDirectory()) {
         File[] files = dir.listFiles();
         for (int i = 0; i < files.length; ++i) {
            rmdir(files[i]);
         }
      }
      return dir.delete();
   }
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------

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
