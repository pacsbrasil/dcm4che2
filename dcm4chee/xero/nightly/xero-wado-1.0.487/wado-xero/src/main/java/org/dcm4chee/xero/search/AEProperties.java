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
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a properties provider for DICOM AEs. Initialize the default properties to connect to the localhost.
 * <p>
 * Definition files must be resources in the classpath and be of the form:  
 * <p>
 * <i> ae-{aePath}.properties</i>
 * <br>
 * Where aePath is of the form <aeTitle>[@hostName][:port] i.e. DCM4CHEE@localhost:104
 * @author smohan
 * 
 */
public class AEProperties {
   private static final Logger log = LoggerFactory.getLogger(AEProperties.class);
   
   public static final String AE_TITLE_KEY = "title";
   public static final String AE_PORT_KEY = "aeport";
   public static final String AE_HOST_KEY = "host";
   
   private static final String FILE_NAME_PREPEND = "ae-";
   private static final String FILE_NAME_EXT = ".properties";

   private static final Pattern validFileNamePattern = Pattern.compile("[a-z0-9_@]+",Pattern.CASE_INSENSITIVE);
   
   private static final AEProperties aeProperties = new AEProperties();

   /** The key to use for a particular ae */
   public static final String AE="ae";

   /** Set the local ae title for this object - NOT necessarily the 
    * ae properties file name, but the DICOM name
    */
   public static final String LOCAL_TITLE = "localTitle";

   /** Set this property to control which issuer is the default one */
   public static final String DEFAULT_ISSUER = "defaultIssuer";
   
   private Map<String, Object> defaultProperties = null;

   private ConcurrentHashMap<String, Map<String, Object>> remoteProperties = new ConcurrentHashMap<String, Map<String, Object>>();


   ClassLoader cl = Thread.currentThread().getContextClassLoader();

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
      loadRemoteProperty("local");
      defaultProperties = remoteProperties.get("local");
      if( defaultProperties!=null ) return;
      log.info("Loading local properties from default.");
      Map<String, Object> temp = new HashMap<String, Object>();
      temp.put(AE_HOST_KEY, "localhost");
      temp.put(AE_PORT_KEY, 11112);
      temp.put(AE_TITLE_KEY, "DCM4CHEE");
      temp.put(LOCAL_TITLE, "XERO");
      defaultProperties = Collections.unmodifiableMap(temp);
   }

   /**
    * loads the property for the given ae name.
    * 
    * @param aePath
    */
   @SuppressWarnings("unchecked")
   private void loadRemoteProperty(String aePath) {
      String propName = FILE_NAME_PREPEND + aePath + FILE_NAME_EXT;
      InputStream is = cl.getResourceAsStream(propName);
      Properties props = new Properties();
      if (is != null) {
         try {
            props.load(is);

            String hostname = props.getProperty(AE_HOST_KEY);
            String aeport = props.getProperty(AE_PORT_KEY);
            if( aeport==null ) aeport = "11112";
            String ejbport = props.getProperty("ejbport");
            if( ejbport==null ) ejbport = "1099";
            String title = props.getProperty(AE_TITLE_KEY);
            if( title==null ) props.put(AE_TITLE_KEY,"DCM4CHEE");
            String localTitle = props.getProperty(LOCAL_TITLE);
            if( localTitle==null ) props.put(LOCAL_TITLE,"XERO");
             
            if (hostname != null ) {
               Map mprops = props;
               Map<String, Object> map = (Map<String,Object>) mprops;
               map.put(AE_PORT_KEY, Integer.parseInt(aeport));
               map.put("ejbport", Integer.parseInt(ejbport));

               remoteProperties
                     .putIfAbsent(aePath, map);

            } else {
               log.error("The host must be specified in ae properties file "+aePath);
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
      } else {
         log.warn("Unable to find ae property file {}", propName);
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

      if (!remoteProperties.contains(name))
      {
         loadRemoteProperty(name);
      }

      if(!validFileNamePattern.matcher(name).matches())
         throw new IllegalArgumentException("Invalid AE path:  Contains illegal filesystem characters");


      return remoteProperties.get(name);
   }
   
   /**
    * Gets the AE object from the parameters.  Throws a runtime exception if the AE
    * isn't found and the AE is specified.
    */
   public static Map<String,Object> getAE(Map<String,Object> params) {
      String aes = (String) params.get(AE);
      if( aes==null ) return aeProperties.getDefaultAE();
      Map<String,Object> ret = aeProperties.getAE(aes);
      if( ret==null ) throw new RuntimeException("Unknown AE specified:"+aes);
      return ret;
   }
}
