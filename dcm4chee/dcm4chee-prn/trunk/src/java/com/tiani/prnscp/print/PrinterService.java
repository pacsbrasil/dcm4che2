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

import org.jboss.system.ServiceMBeanSupport;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.QueueReceiver;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.io.File;
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
public class PrinterService
   extends ServiceMBeanSupport
   implements MessageListener, PrinterServiceMBean {
   
   // Constants -----------------------------------------------------
   private static final String[] CODE_STRING = {
      null, "NORMAL", "WARNING", "FAILURE"
   };
   
   // Attributes ----------------------------------------------------
   
   private QueueConnection conn;
   private QueueSession session;
   private String queueName;
   private long notifCount = 0;
   
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
   
   // PrinterMBean implementation -----------------------------------
   
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
   
   // MessageListener implementation -----------------------------------
   
   public void onMessage(Message msg) {
      String job;
      int numberOfCopies;
      String mediumType;
      String filmDestination;
      try {
         job = ((TextMessage) msg).getText();
         numberOfCopies = msg.getIntProperty("NumberOfCopies"); 
         mediumType = msg.getStringProperty("MediumType"); 
         filmDestination = msg.getStringProperty("FilmDestination");          
      } catch (JMSException e) {
         log.error("Failed to read JMS messsage:", e);
         return;
      }
      
      log.info("Start processing job - " + job);
      sendNotification(
         new PrintJobNotification(this, ++notifCount, job,
            PrintJobNotification.PRINTING));
      try {
         File jobDir = new File(job);
         if (!jobDir.exists()) {
            throw new RuntimeException("Missing job dir - " + job);
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
         // TO DO
         Thread.sleep(10000); // simulate Print Process
         
         log.info("Finished processing job - " + job);
         sendNotification(
            new PrintJobNotification(this, ++notifCount, job,
               PrintJobNotification.DONE));
      } catch (Exception e) {
         log.error("Failed processing job - " + job, e);
         sendNotification(
            new PrintJobNotification(this, ++notifCount, job,
               PrintJobNotification.FAILURE));
      }
   }
   
   // ServiceMBeanSupport overrides ------------------------------------
   public void startService()
      throws Exception
   {
      Context iniCtx = new InitialContext();
      QueueConnectionFactory qcf = 
         (QueueConnectionFactory) iniCtx.lookup("ConnectionFactory");
      conn = qcf.createQueueConnection();
      session = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);      
      Queue que = (Queue) iniCtx.lookup("queue/" + queueName);
      QueueReceiver rcv = session.createReceiver(que);
      rcv.setMessageListener(this);
      conn.start();
   }

   public void stopService()
      throws Exception
   {
      conn.stop();
      session.close();
      session = null;
      conn.close();
      conn = null; 
   }
      
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
