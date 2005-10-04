/*
 * Created on 29.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.ae;

import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AEDelegate {
	   private static ObjectName echoServiceName = null;
	   private static ObjectName aeServiceName = null;
	   private static MBeanServer server;
	   private List aes;
		
	    private static Logger log = Logger.getLogger( AEDelegate.class.getName() );

	    /** 
	     * Iinitialize the Echo service delegator.
	     * <p>
	     * Set the name of the EchoService MBean with the servlet config param 'echoServiceName'.
	     * 
	     * @param config The ServletConfig object.
	     */
		public void init( ServletConfig config ) {
	        if (server != null) return;
	        server = MBeanServerLocator.locate();
	        try {
	        	echoServiceName = new ObjectName(config.getInitParameter("echoServiceName"));
	        	aeServiceName = new ObjectName(config.getInitParameter("aeServiceName"));
			} catch (Exception e) {
				log.error( "Exception in init! ",e );
			}
	       
	    }

		public Logger getLogger() {
			return log;
		}
//AE Service	
		/**
		 * Return list of all configured AE
		 */
		public List getAEs() {
			try {
		        aes = (List) server.invoke(aeServiceName,
		                "listAEs",
		                null,
		                null );
		        return aes;
			} catch ( Exception x ) {
				log.error( "Exception occured in getAEs: "+x.getMessage(), x );
				return null;
			}
		}

		/**
		 * @param title
		 * @return
		 * @throws ReflectionException
		 * @throws MBeanException
		 * @throws InstanceNotFoundException
		 */
		public AEData getAE(String title) throws InstanceNotFoundException, MBeanException, ReflectionException {
			return (AEData) server.invoke(aeServiceName,
	                "getAE",
	                new Object[] {title},
	                new String[] { String.class.getName() } );
		}
		
		/**
		 * @param newAE
		 * @throws ReflectionException
		 * @throws MBeanException
		 * @throws InstanceNotFoundException
		 */
		public void updateAE(String aet, String host, int port, String ciphers) throws InstanceNotFoundException, MBeanException, ReflectionException {
	        server.invoke(aeServiceName,
	                "addAE",
	                new Object[]{ aet, host, new Integer(port), ciphers },
	                new String[]{ String.class.getName(), String.class.getName(), int.class.getName(), String.class.getName()} );
		}
	
		/**
		 * @param title
		 * @return
		 * @throws ReflectionException
		 * @throws MBeanException
		 * @throws InstanceNotFoundException
		 */
		public AEData delAE(String title) throws InstanceNotFoundException, MBeanException, ReflectionException {
			return (AEData) server.invoke(aeServiceName,
	                "removeAE",
	                new Object[] {title},
	                new String[] { String.class.getName() } );
		}
		
//ECHO Service		
		/**
		 * Makes the MBean call to echo an AE configuration.
		 * 
		 * 
		 * @return An info string for status of echo.
		 */
		public String echo( AEData aeData, int nrOfTests ) {
			if ( log.isDebugEnabled() ) log.debug("Send echo to "+aeData);
			String resp = null;
			try {
		        Object o = server.invoke(echoServiceName,
		                "echo",
		                new Object[]{ aeData, new Integer( nrOfTests ) },
		                new String[]{ AEData.class.getName(), Integer.class.getName() } );
		        resp = (String) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in echoAE: "+x.getMessage(), x );
			}
			if ( log.isDebugEnabled() ) log.debug("echo response for "+aeData+":"+resp);
	        return resp;
		}

		/**
		 * Makes the MBean call to echo an AET (AE config for given title).
		 * 
		 * 
		 * @return An info string for status of echo.
		 */
		public String echo( String aet, int nrOfTests ) {
			String resp = null;
			try {
		        Object o = server.invoke(echoServiceName,
		                "echo",
		                new Object[]{ aet, new Integer( nrOfTests ) },
		                new String[]{ String.class.getName(), Integer.class.getName() } );
		        resp = (String) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in echo (AET="+aet+"): "+x.getMessage(), x );
			}
	        return resp;
		}

		/**
		 * Makes the MBean call to echoe an AE configuration.
		 * 
		 * 
		 * @return An info string for status of echo.
		 */
		public String[] echoAll( AEData aeData ) {
			String[] resp = null;
			try {
		        Object o = server.invoke(echoServiceName,
		                "echoAll",
		                null,
		                null );
		        resp = (String[]) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in echo ("+aeData+"): "+x.getMessage(), x );
			}
	        return resp;
		}
}
