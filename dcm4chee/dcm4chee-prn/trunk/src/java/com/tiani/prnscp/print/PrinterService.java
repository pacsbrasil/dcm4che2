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

package com.tiani.prnscp.print;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;

import org.jboss.system.ServiceMBeanSupport;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

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
public class PrinterService
   extends ServiceMBeanSupport
   implements PrinterServiceMBean, NotificationListener, Runnable {
   
   // Constants -----------------------------------------------------
   private static final String[] CODE_STRING = {
      null, "NORMAL", "WARNING", "FAILURE"
   };
   
   // Attributes ----------------------------------------------------
   private ObjectName printerCalibration;
   private ObjectName printerConfiguration;
   private PrinterCalibrationService calibrationService;
   private PrinterConfigurationService configurationService;
   
   private long notifCount = 0;
   private LinkedList highPriorQueue = new LinkedList();
   private LinkedList medPriorQueue = new LinkedList();
   private LinkedList lowPriorQueue = new LinkedList();
   private Object queueMonitor = new Object();
   private Thread scheduler;
   
   private int status = NORMAL;
   private String statusInfo = "NORMAL";
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
   throws MalformedObjectNameException {
      return name == null
      ? new ObjectName("dcm4chex:service=Printer")
      : name;
   }
   
   public String getName() {
      return "Printer";
   }
   
   public PrinterCalibrationService getCalibrationService() {
      return calibrationService;
   }
   
   public PrinterConfigurationService getConfigurationService() {
      return configurationService;
   }
   
   // PrinterMBean implementation -----------------------------------
   
   /** Getter for property printerCalibration.
    * @return Value of property printerCalibration.
    */
   public ObjectName getPrinterCalibration() {
      return printerCalibration;
   }
   
   /** Setter for property printerCalibration.
    * @param printerCalibration New value of property printerCalibration.
    */
   public void setPrinterCalibration(ObjectName printerCalibration) {
      this.printerCalibration = printerCalibration;
   }
   
   /** Getter for property printerConfiguration.
    * @return Value of property printerConfiguration.
    */
   public ObjectName getPrinterConfiguration() {
      return printerConfiguration;
   }
   
   /** Setter for property printerConfiguration.
    * @param printerConfiguration New value of property printerConfiguration.
    */
   public void setPrinterConfiguration(ObjectName printerConfiguration) {
      this.printerConfiguration = printerConfiguration;
   }
   
   /** Getter for property status.
    * @return Value of property status.
    */
   public int getStatusID() {
      return status;
   }
   
   /** Getter for property statusInfo.
    * @return Value of property statusInfo.
    */
   public String getStatusInfo() {
      return statusInfo;
   }
   
   /** Getter for string value for property status.
    * @return String value of property status.
    */
   public String getStatus() {
      return CODE_STRING[status];
   }
   
   
   // ServiceMBeanSupport overrides ------------------------------------
   public void startService()
   throws Exception {
      calibrationService = (PrinterCalibrationService)
         server.getAttribute(printerCalibration, "Service");
      configurationService = (PrinterConfigurationService)
         server.getAttribute(printerConfiguration, "Service");

      scheduler = new Thread(this);
      scheduler.start();
   }
   
   public void stopService()
   throws Exception {
      Thread tmp = scheduler;
      scheduler = null;
      tmp.interrupt();
   }
   
   // NotificationListener implementation -----------------------------------
   public void handleNotification(Notification notif, Object obj) {
      log.info("Scheduling job - " + new File(notif.getMessage()).getName());
      Dataset sessionAttr = (Dataset)notif.getUserData();
      String prior = sessionAttr.getString(Tags.PrintPriority);
      synchronized (queueMonitor) {
         if ("LOW".equals(prior)) {
            lowPriorQueue.add(notif);
         } else if ("HIGH".equals(prior)) {
            highPriorQueue.add(notif);
         } else {
            medPriorQueue.add(notif);
         }
         queueMonitor.notify();
      }
   }
   
   // Runnable implementation -----------------------------------
   public void run() {
      log.info("Scheduler Started");
      while (scheduler != null) {
         try {
            Notification notif;
            synchronized (queueMonitor) {
               while ((notif = nextNotification()) == null) {
                  queueMonitor.wait();
               }
            }
            processNotification(notif);
         } catch (InterruptedException ignore) {
         }
      }
      log.info("Scheduler Stopped");
   }
   
   private Notification nextNotification() {
      if (!highPriorQueue.isEmpty()) {
         return (Notification) highPriorQueue.removeFirst();
      }
      if (!medPriorQueue.isEmpty()) {
         return (Notification) medPriorQueue.removeFirst();
      }
      if (!lowPriorQueue.isEmpty()) {
         return (Notification) lowPriorQueue.removeFirst();
      }
      return null;
   }
   
   private void processNotification(Notification notif) {
      String job = notif.getMessage();
      File jobDir = new File(job);
      String jobID = new File(job).getName();
      log.info("Start processing job - " + jobID);
      sendNotification(
         new Notification(NOTIF_PRINTING, this, ++notifCount, job));
      Dataset sessionAttr = (Dataset)notif.getUserData();
      try {
         doPrint(jobDir, (Dataset)notif.getUserData());
         log.info("Finished processing job - " + jobID);
         sendNotification(
            new Notification(NOTIF_DONE, this, ++notifCount, job));
      } catch (Exception e) {
         log.error("Failed processing job - " + jobID, e);
         sendNotification(
            new Notification(NOTIF_FAILURE, this, ++notifCount, job));
      }
   }
   
   private void doPrint(File jobDir, Dataset sessionAttr)
      throws Exception
   {
      if (!jobDir.exists()) {
         throw new RuntimeException("Missing job dir - " + jobDir);
      }
      File rootDir = jobDir.getParentFile().getParentFile();
      File hcDir = new File(rootDir, "HC");
      if (!hcDir.exists()) {
         throw new RuntimeException("Missing hardcopy dir - " + hcDir);
      }

      File[] spFiles = jobDir.listFiles();
      Arrays.sort(spFiles,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o1).lastModified()
                          - ((File)o2).lastModified());
            }
         });
      // simulate Print Process
      try {
         Thread.sleep(10000);
      } catch (InterruptedException ignore) {}
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
