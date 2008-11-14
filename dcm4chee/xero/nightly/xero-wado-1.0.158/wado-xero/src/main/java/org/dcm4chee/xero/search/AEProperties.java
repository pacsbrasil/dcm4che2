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
package org.dcm4chee.xero.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a properties provider for <tt>DiconCFindFilter</tt>. Initialise the default properties to connect to the localhost.
 * 
 * @author smohan
 * 
 */
public class AEProperties {

   private static final AEProperties aeProperties = new AEProperties();

   private static String FILE_NAME_PREPEND = "ae-";

   private static String FILE_NAME_EXT = ".properties";

   private Map<String, Object> defaultProperties = null;

   private ConcurrentHashMap<String, Map<String, Object>> remoteProperties = new ConcurrentHashMap<String, Map<String, Object>>();

   static Logger log = LoggerFactory.getLogger(AEProperties.class);

   /**
    * force initialization through this class
    */
   private AEProperties() {
      initLocalProperties();
   }

   /**
    * 
    * @return instance of this class
    */
   public static AEProperties getInstance() {
      return aeProperties;
   }

   /**
    * populate default ae properties.
    */
   private void initLocalProperties() {
      Map<String, Object> temp = new HashMap<String, Object>();
      temp.put("host", "localhost");
      temp.put("aeport", 11112);
      temp.put("ejbport", 1099);
      temp.put("title", "DCM4CHEE");
      temp.put("localTitle", "XERO");
      defaultProperties = Collections.unmodifiableMap(temp);
   }

   /**
    * loads the property for the given ae name.
    * 
    * @param name
    */
   private void loadRemoteProperty(String name) {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is = cl.getResourceAsStream(FILE_NAME_PREPEND + name
            + FILE_NAME_EXT);
      Properties props = new Properties();
      if (is != null) {
         try {
            props.load(is);

            String hostname = props.getProperty("host");
            String aeport = props.getProperty("aeport");
            String ejbport = props.getProperty("ejbport");
            String title = props.getProperty("title");
            String localTitle = props.getProperty("localTitle");

            if (hostname != null && aeport != null && ejbport != null
                  && title != null && localTitle != null) {

               Map<String, Object> map = new HashMap<String, Object>();
               map.put("host", hostname);
               map.put("aeport", Integer.parseInt(aeport));
               map.put("ejbport", Integer.parseInt(ejbport));
               map.put("title", title);
               map.put("localTitle", localTitle);

               remoteProperties
                     .putIfAbsent(name, map);

            } else {
               log.error("insufficient ae values");
            }
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

   /**
    * @return default property set.
    */
   public Map<String, Object> getDefaultAE() {
      return defaultProperties;
   }

   /**
    * 
    * @param ae
    *           name. Locate and load the properties from the file. If value is
    *           'local' return default properties.
    * @return properties for the given ae name. Will return <tt>null</tt> if
    *         unable to determine the property set.
    */
   public Map<String, Object> getAE(String name) {
      if (name.equals("local")) {
         return getDefaultAE();
      }

      if (remoteProperties.contains(name)) {
         return remoteProperties.get(name);
      }

      if (!remoteProperties.contains(name) && name.matches("[a-zA-Z_]+")) {
         loadRemoteProperty(name);
      }

      return remoteProperties.get(name);
   }
}
