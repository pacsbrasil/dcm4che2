/*
 * Created on 29.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mcmc;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MCMScuDelegate {
	   private static ObjectName mcmScuServiceName = null;
		private static MBeanServer server;
		
	    private static Logger log = Logger.getLogger( MCMScuDelegate.class.getName() );

	    /** 
	     * Iinitialize the WADO service delegator.
	     * <p>
	     * Set the name of the WADOService MBean with the servlet config param 'wadoServiceName'.
	     * 
	     * @param config The ServletConfig object.
	     */
		public void init( ServletConfig config ) {
	        if (server != null) return;
	        server = MBeanServerLocator.locate();
	        String s = config.getInitParameter("mcmScuServiceName");
	        try {
	        	mcmScuServiceName = new ObjectName(s);
				
			} catch (Exception e) {
				log.error( "Exception in init! ",e );
			}
	       
	    }

		public Logger getLogger() {
			return log;
		}
		
		/**
		 * Makes the MBean call to get the WADO response object for given WADO request.
		 * 
		 * @param reqVO	The WADO request.
		 * 
		 * @return The WADO response object.
		 */
		public String getMediaCreationStatus() {
			String resp = null;
			try {
		        Object o = server.invoke(mcmScuServiceName,
		                "getMediaCreationStatus",
		                null,
		                null );
		        resp = (String) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in getMediaCreationStatus: "+x.getMessage(), x );
			}
	        return resp;
		}
		
		/**
		 * Checks if Media Creation SCP is available.
		 * <p>
		 * Checks move destination AET and MCM_SCP AET.
		 * 
		 * @return true if available.
		 */
		public boolean checkMcmScpAvail() {
			String resp = "";
			try {
		        Object o = server.invoke(mcmScuServiceName,
		                "checkMcmScpAvail",
		                null,
		                null );
		        resp = (String) o;
			} catch ( Exception x ) {
				if ( log.isDebugEnabled() ) log.debug( "Exception occured in checkMcmScpAvail: "+x.getMessage(), x );
			}
			return "OK".equals( resp );
			
		}

}
