/*
 * JMXBrowser.java
 *
 * Created on January 16, 2003, 3:13 PM
 */

/**
 *
 * @author  jforaci
 */
package com.tiani.prnscp.client;

import java.net.InetAddress;
import java.util.Hashtable;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;

public class GetAvailablePrinters {
   public static void main(String[] args) throws Exception {
      String serviceName = "dcm4chex:service=Printer,aet=TIANI_PRINT";
      if (args.length > 0) {
         serviceName = "dcm4chex:service=Printer,aet=" + args[0];
      }
      String serviceHost = InetAddress.getLocalHost().getHostName();
      Hashtable env = new Hashtable();
      env.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
      env.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      env.put("java.naming.provider.url", "localhost");
      InitialContext ic = new InitialContext(env);
      RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx:" + serviceHost + ":rmi");
      String[] printers = (String[]) server.getAttribute(
         new ObjectName(serviceName), "AvailablePrinters");
      System.out.println("Available Printers:");
      for (int i = 0; i < printers.length; ++i) {
         System.out.println(printers[i]);
      }
   }
}
