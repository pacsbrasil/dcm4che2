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
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.AEProperties;
import org.dcm4chee.xero.search.filter.EJBServiceLocator;


/**
 * Factory that hides the details of how to bind an instance file locator.  Instances
 * are cached internally to reduce the cost of subsequent calls.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class InstanceFileLocatorFactory
{
   public static final String IDC2_NAME = "dcm4chee.archive:service=QueryRetrieveScp";
   public static final String IDC1_NAME = "dcm4chee.archive:service=FileSystemMgt";

   private Map<String,MBeanInstanceFileLocator> titleToLocator = new HashMap<String, MBeanInstanceFileLocator>();
   
   
   /**
    * Get an instance file locator for the indicated host.
    * <p>
    * This method will check the AE type to determine how to access it.
    */
   public MBeanInstanceFileLocator getInstanceFileLocator(String aeName) 
      throws NamingException, JMException, IOException
   {
      if(!titleToLocator.containsKey(aeName))
      {
         titleToLocator.put(aeName, createInstanceFileLocator(aeName));
      }
      
      return titleToLocator.get(aeName);
   }

   /**
    * Create a new instance of the instance file locator
    */
   protected MBeanInstanceFileLocator createInstanceFileLocator(String aeTitle) 
      throws NamingException, JMException, IOException
   {
      Map<String,Object> config = AEProperties.getInstance().getAE(aeTitle);
      String objectName = getObjectName(config);
      Context context = createInitialContext(config);
      return new MBeanInstanceFileLocator(context,objectName);
   }

   /**
    * Read the object name from the AE configuration
    */
   protected String getObjectName(Map<String,Object> config)
   {
      String objectName = IDC1_NAME;
      String type = FilterUtil.getString(config, "type");
      if("idc2".equals(type))
         objectName = IDC2_NAME;
      
      return objectName;
   }
   
   /**
    * Create an initial context to the server defined in the passed configuration Map
    */
   protected Context createInitialContext(Map<String, Object> config) throws NamingException
   {
      String host = FilterUtil.getString(config, AEProperties.AE_HOST_KEY);
      int port = FilterUtil.getInt(config, AEProperties.EJB_PORT);
      
      return EJBServiceLocator.getInitialContext(host, Integer.toString(port));
   }
}
