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

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dcm4chee.xero.search.filter.MBeanConnectionManager;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Unit tests for the MBeanFileLocationResolver.
 * <p>
 * TODO: Pass the host name as an argument.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class MBeanInstanceFileLocatorTest
{
   private Context context;
   private MBeanServerConnection connection;
   
   @BeforeMethod
   public void setupContext() throws NamingException
   {
      this.connection = createMock(MBeanServerConnection.class);
      this.context = createMock(Context.class);
      expect(context.lookup("jmx/invoker/RMIAdaptor")).andStubReturn(connection);
      replay(context);
   }
   
   @Test(expectedExceptions=IllegalArgumentException.class)
   public void constructor_MustRejectNullInputs() throws Exception
   {
      new MBeanInstanceFileLocator(null,InstanceFileLocatorFactory.IDC1_NAME);
   }
   
   
   @Test
   public void locateInstance_MustInvokeMBean() throws Exception
   {
      ObjectName name = new ObjectName("dcm4chee.archive:service=FileSystemMgt");
      String expectedUID = "1.2.3.4.5.6";
      Object[] expectedArgs = new Object[] {expectedUID};
      String[] expectedSig = new String[]{"java.lang.String"};
      File expectedFile = new File("C:\\cache\\file.dcm");

      expect(connection.invoke(eq(name), eq("locateInstance"),aryEq(expectedArgs) , aryEq(expectedSig) ))
         .andStubReturn(expectedFile);
      replay(connection);
      
      MBeanInstanceFileLocator locator = new MBeanInstanceFileLocator(context,InstanceFileLocatorFactory.IDC1_NAME);
      Object location = locator.locateInstance(expectedUID);
      assertEquals(location,expectedFile);
      verify(connection);
   }

}
