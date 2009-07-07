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

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.dcm4chee.xero.search.AEProperties;
import org.testng.annotations.Test;

public class InstanceFileLocatorFactoryTest
{
      
   @Test
   public void getObjectName_IdentifiesMBeanWithLocateInstance() throws Exception
   {
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      MBeanServerConnection connection = createMock(MBeanServerConnection.class);
      String expectedName = InstanceFileLocatorFactory.EJB_NAME_DEFAULT+InstanceFileLocatorFactory.IDC2_NAME;
      MBeanOperationInfo[] operations = new MBeanOperationInfo[] { new MBeanOperationInfo("locateInstance",null,null,null,1)};
      MBeanInfo info = new MBeanInfo(expectedName,null,null,null,operations,null);
      expect(connection.getMBeanInfo(isA(ObjectName.class))).andStubReturn(info);
      replay(connection);

      String name = factory.getObjectName(connection,InstanceFileLocatorFactory.EJB_NAME_DEFAULT);
      assertEquals(name,expectedName);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void getObjectName_RejectsMBeansWithoutLocateInstance() throws Exception
   {
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      MBeanServerConnection connection = createMock(MBeanServerConnection.class);
      String expectedName = InstanceFileLocatorFactory.IDC2_NAME;
      MBeanOperationInfo[] operations = new MBeanOperationInfo[] { new MBeanOperationInfo("NotTheRightMethod",null,null,null,1)};
      MBeanInfo info = new MBeanInfo(expectedName,null,null,null,operations,null);
      expect(connection.getMBeanInfo(isA(ObjectName.class))).andStubReturn(info);
      replay(connection);

      String name = factory.getObjectName(connection,InstanceFileLocatorFactory.EJB_NAME_DEFAULT);
      assertEquals(name,expectedName);
   }
   
   @Test
   public void isDirectlyAccessible_ReturnsTrue_WhenLocalHostHasNoDefinedEJBPort() throws Exception
   {
      Map<String,Object> config = new HashMap<String,Object>();
      config.put(AEProperties.AE_HOST_KEY, "localhost");
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      assertTrue(factory.isDirectlyAccessible(config));
   }
   
   @Test
   public void isDirectlyAccessible_ReturnsFalse_WhenLocalHostHasEJBPortDefined() throws Exception
   {
      Map<String,Object> config = new HashMap<String,Object>();
      config.put(AEProperties.AE_HOST_KEY, "localhost");
      config.put(AEProperties.EJB_PORT, 1099);
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      assertFalse(factory.isDirectlyAccessible(config));
   }
   
   @Test
   public void isDirectlyAccessible_ReturnsFalseFor_MARLIN() throws Exception
   {
      Map<String,Object> config = new HashMap<String,Object>();
      config.put("host", "marlin"); // default host value...
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      assertFalse(factory.isDirectlyAccessible(config));
   }
   
   @Test
   public void isDirectlyAccessible_DefaultAE_MustBeDirectlyAccessible() throws Exception
   {
      Map<String,Object> config = AEProperties.getInstance().getDefaultAE();
      InstanceFileLocatorFactory factory = new InstanceFileLocatorFactory();
      assertTrue(factory.isDirectlyAccessible(config));
   }
}
