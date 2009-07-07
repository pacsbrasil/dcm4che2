// ***** BEGIN LICENSE BLOCK *****
// Version: MPL 1.1/GPL 2.0/LGPL 2.1
// 
// The contents of this file are subject to the Mozilla Public License Version 
// 1.1 (the "License"); you may not use this file except in compliance with 
// the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
// 
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
// for the specific language governing rights and limitations under the
// License.
// 
// The Original Code is part of dcm4che, an implementation of DICOM(TM) in Java(TM), hosted at http://sourceforge.net/projects/dcm4che
//  
// The Initial Developer of the Original Code is Agfa Healthcare.
// Portions created by the Initial Developer are Copyright (C) 2009 the Initial Developer. All Rights Reserved.
// 
// Contributor(s):
// Andrew Cowan <andrew.cowan@agfa.com>
// 
// Alternatively, the contents of this file may be used under the terms of
// either the GNU General Public License Version 2 or later (the "GPL"), or
// the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
// in which case the provisions of the GPL or the LGPL are applicable instead
// of those above. If you wish to allow use of your version of this file only
// under the terms of either the GPL or the LGPL, and not to allow others to
// use your version of this file under the terms of the MPL, indicate your
// decision by deleting the provisions above and replace them with the notice
// and other provisions required by the GPL or the LGPL. If you do not delete
// the provisions above, a recipient may use your version of this file under
// the terms of any one of the MPL, the GPL or the LGPL.
// 
// ***** END LICENSE BLOCK *****
package org.dcm4chee.xero.location;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;

import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.search.filter.EJBServiceLocator;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory that hides the details of how to bind an instance file locator.  Instances
 * are cached internally to reduce the cost of subsequent calls.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class InstanceFileLocatorFactory
{
   public static final Logger log = LoggerFactory.getLogger(InstanceFileLocatorFactory.class);
    
   public static final String EJBNAME = "ejbname";

   public static final String EJB_NAME_DEFAULT = "dcm4chee.archive";

   public static final String IDC2_NAME = ":service=QueryRetrieveScp";
   public static final String IDC1_NAME = ":service=FileSystemMgt";
   
   /** Names of the DCM4CHEE services to connect to in order */
   private static final String[] OBJECT_NAMES = new String[] {IDC2_NAME,IDC1_NAME};
   
   /** It's not expensive to make multiple locators, so we just make sure that we only cache one */
   private ConcurrentMap<String,MBeanInstanceFileLocator> titleToLocator = new ConcurrentHashMap<String, MBeanInstanceFileLocator>();
   
   
   /**
    * Get an instance file locator for the indicated host.
    * <p>
    * This method will check the AE type to determine how to access it.
    */
   public MBeanInstanceFileLocator getInstanceFileLocator(String aeName) 
      throws NamingException, JMException, IOException
   {
      MBeanInstanceFileLocator locator = titleToLocator.get(aeName);
      if(locator == null)
      {
         locator = createInstanceFileLocator(aeName);
         titleToLocator.putIfAbsent(aeName, locator );
      }
      
      return locator;
   }

   /**
    * Create a new instance of the instance file locator
    */
   protected MBeanInstanceFileLocator createInstanceFileLocator(String aeTitle) 
      throws NamingException, JMException, IOException
   {
      Map<String,Object> config = AEProperties.getInstance().getAE(aeTitle);
      MBeanServerConnection server = createMBeanServerConnection(config);
      String ejbname = FilterUtil.getString(config,EJBNAME, EJB_NAME_DEFAULT);
      String objectName = getObjectName(server,ejbname);
      
      log.info("Created a file instance locator for {} at {}",aeTitle,objectName);
      return new MBeanInstanceFileLocator(server,objectName);
   }
   
   
   /**
    * Determine the name of the object to invoke.
    * @throws NamingException 
    */
   protected String getObjectName(MBeanServerConnection connection, String ejbname) throws NamingException
   {
      for(String name : OBJECT_NAMES)
      {
         try
         {
            String fullName = ejbname+name;
            ObjectName objectName = new ObjectName(fullName);
            MBeanInfo info = connection.getMBeanInfo(objectName);
            if(containsLocateInstance(info))
               return fullName;
         }
         catch (Exception e)
         {
            log.warn("Unable to access the MBean Operation Information",e);
         }
      }
      
      throw new IllegalArgumentException("Unable to bind the locateInstance call on server MBean");
   }
   
   /**
    * Determine if the locateInstance call is contained in this MBean.
    */
   private boolean containsLocateInstance(MBeanInfo mbeanInfo)
   {
      MBeanOperationInfo[] operations = mbeanInfo.getOperations();
      for(MBeanOperationInfo opInfo : operations)
      {
         if("locateInstance".equals(opInfo.getName()))
            return true;
      }
      return false;
   }

   /**
    * Create an MBeanServerConnection based on the indicated configuration.
    * @throws NamingException 
    */
   protected MBeanServerConnection createMBeanServerConnection(Map<String, Object> config) 
      throws NamingException
   {
      MBeanServerConnection connection;
      
      if(isDirectlyAccessible(config))
         connection = MBeanServerLocator.locate();
      else
         connection = createRemoteConnection(config);
      
      return connection;
   }
   
   /**
    * Create an initial context to the server defined in the passed configuration Map
    */
   protected MBeanServerConnection createRemoteConnection(Map<String, Object> config) throws NamingException
   {
      Context context = EJBServiceLocator.getInitialContext(config);
      MBeanServerConnection connection = (MBeanServerConnection) context.lookup("jmx/invoker/RMIAdaptor");
      return connection;
   }

   /**
    * Determine if the AE configuration allows us to connect directly to the 
    * MBean registry.  To connect directly the DCM4CHEE must be running on 
    * the same machine with no EBJ port defined.
    */
   protected boolean isDirectlyAccessible(Map<String, Object> config)
   {
      String host = (String)config.get(AEProperties.AE_HOST_KEY);
      Object port = config.get(AEProperties.EJB_PORT);
      return port == null && "localhost".equalsIgnoreCase(host);
   }
}
