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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import org.dcm4chee.xero.search.filter.EJBServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

/**
 * Class that is able to connect to a remote server and retrieve the file
 * location of a particular sop instance UID.  The internal implementation 
 * is based on invoking the generic MBean interfaces of the file management
 * service of DCM4CHEE.  
 * <p>
 * The reflective invocation of MBeans was introduced to remove direct dependencies 
 * on the DCM4CHEE EJB interfaces.  
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class MBeanInstanceFileLocator
{
   private static Logger log = LoggerFactory.getLogger(MBeanInstanceFileLocator.class);
   
   private static String[] signature = new String[]{"java.lang.String"};
   
   private MBeanServerConnection connection;
   private ObjectName name;

   /**
    * Create a new copy of the MBeanInstanceFileLocator which is pointed at the
    * server indicated by the context and the object defined by the objectName. 
    * @param context JNDI context for the JBoss server to connect to.
    * @param objectName Name of the MBean that contains the locateInstance call.  
    */
   public MBeanInstanceFileLocator(MBeanServerConnection connection, String objectName)
      throws NamingException,JMException,IOException
   {
      if(connection == null) 
         throw new IllegalArgumentException("Context is required to bind MBean References.");
      
      this.connection = connection;
      this.name = new ObjectName(objectName);
   }

   /**
    * Return either a File location of the instance or the host on which 
    * the instance can be found.
    * @throws IOException 
    * @throws ReflectionException 
    * @throws MBeanException 
    * @throws InstanceNotFoundException 
    */
   public Object locateInstance(String sopInstanceUID) throws JMException, IOException
   {
      try 
      {
         Object[] params = new Object[] {sopInstanceUID};
         return connection.invoke(name, "locateInstance", params, signature);
      }
      catch(JMException e)
      {
         if(log.isDebugEnabled())
         {
            if(e.getCause() instanceof InstanceNotFoundException)
            {
               Set<ObjectName> names = this.connection.queryNames(null, null);
               log.debug("Could not find FileSystemMgt MBean.  Registered MBeans are {}",names);
            }
            
            MBeanInfo beanInfo = connection.getMBeanInfo(name);
            List<MBeanOperationInfo> operationInfo = Arrays.asList(beanInfo.getOperations());
            log.debug("Unable to invoke FileSystemMgt.locateInstance.  Available operations are {} ",operationInfo);
         }
         
         throw e;
      }
   }
}
