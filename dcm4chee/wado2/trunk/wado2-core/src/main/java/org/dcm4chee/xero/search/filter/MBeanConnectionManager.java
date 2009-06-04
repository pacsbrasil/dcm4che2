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
 * Sebastian Mohan, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sebastian Mohan <sebastian.mohan@agfa.com>
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
package org.dcm4chee.xero.search.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Establishes connection to the MBean server for the given host and port.
 * Defaults to localhost and port 8999.
 * 
 * @author smohan
 * 
 */
public class MBeanConnectionManager {
   private static String PROPERTIES_FILE = "org/dcm4chee/xero/mbean-connection.properties";

   private static MBeanConnectionManager connectionManager = null;

   private String host = "localhost";

   private String port = "8999";

   private MBeanServerConnection connection = null;

   private MBeanConnectionManager() {
   }

   /**
    * Returns singleton instance of the MBeanConnectionManager. Initialize with
    * default host and port if no properties specified
    * 
    * @return Connection to the MBean server
    * @throws IOException
    *            if unable to establish connection.
    */
   public static synchronized MBeanConnectionManager getConnectionManager() {
      if (connectionManager == null) {
         connectionManager = new MBeanConnectionManager();
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         InputStream is = cl.getResourceAsStream(PROPERTIES_FILE);
         Properties props = new Properties();
         if (is != null) {
            try {
               props.load(is);
               connectionManager.host = props.getProperty("host");
               connectionManager.port = props.getProperty("port");
            } catch (Exception e) {
               // Do nothing
            } finally {
               if (is != null) {
                  try {
                     is.close();
                  } catch (IOException e) {
                     // Do nothing
                  }
               }
            }
         }
      }
      return connectionManager;
   }

   /**
    * Returns the MBean server connection for the assigned host and the port.
    * 
    * @return Connection to the MBean server
    * @throws IOException
    *            if unable to establish connection.
    */
   public synchronized MBeanServerConnection getMBeanServerConnection() throws IOException {
      if (connection == null) {
         JMXServiceURL serviceURL = new JMXServiceURL("rmi", "", 0,
               "/jndi/rmi://" + host + ":" + port + "/jmxrmi");
         JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
         connection = connector.getMBeanServerConnection();

      }
      return connection;
   }
}
