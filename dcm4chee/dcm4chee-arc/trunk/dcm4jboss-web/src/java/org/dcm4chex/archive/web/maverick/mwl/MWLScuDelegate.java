/*
 * Created on 29.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.dcm4chex.archive.web.maverick.mwl;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author franz.willer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MWLScuDelegate {
	   private static ObjectName mwlScuServiceName = null;
		private static MBeanServer server;
		
	    private static Logger log = Logger.getLogger( MWLScuDelegate.class.getName() );

	    /** 
	     * Iinitialize the MWLScu service delegator.
	     * <p>
	     * Set the name of the MwlScuService MBean with the servlet config param 'mwlScuServiceName'.
	     * 
	     * @param config The ServletConfig object.
	     */
		public void init( ServletConfig config ) {
	        if (server != null) return;
	        server = MBeanServerLocator.locate();
	        String s = config.getInitParameter("mwlScuServiceName");
	        try {
	        	mwlScuServiceName = new ObjectName(s);
				
			} catch (Exception e) {
				log.error( "Exception in init! ",e );
			}
	       
	    }

		public Logger getLogger() {
			return log;
		}
		
		/**
		 * Makes the MBean call to get the list of worklist entries for given filter (ds).
		 * 
		 * @param ds	The WADO request.
		 * 
		 * @return The list of worklist entries ( Each item in the list is a Dataset of one scheduled procedure step).
		 */
		public List findMWLEntries( Dataset ds ) {
			List resp = null;
			try {
		        Object o = server.invoke(mwlScuServiceName,
		                "findMWLEntries",
	                    new Object[]{ ds },
						new String[]{ Dataset.class.getName() });
		        resp = (List) o;
			} catch ( Exception x ) {
				log.error( "Exception occured in getMWLEntries: "+x.getMessage(), x );
			}
	        return resp;
		}
		
		/**
		 * Checks if the MwlScpAET is local.
		 * <p>
		 * This means, that the MWLSCP is in the same container.
		 * <p>
		 * If it runs in the same container, the query can be done directly without a CFIND.
		 * Also we can allow deletion of MWLEntries.
		 * 
		 * @return true if the MWLSCP runs in the same container.
		 */
		public boolean isLocal() {
			try {
		        Object o = server.invoke(mwlScuServiceName,
		                "getMwlScpAET",
	                    null,
						null );
		        String resp = (String) o;
		        if ( resp == null ) return false;
		        return ( "local".equalsIgnoreCase( resp ) );
			} catch ( Exception x ) {
				log.error( "Exception occured in isLocal: "+x.getMessage(), x );
			}
			return false;
		}
		
		/**
		 * Deletes an MWL entry with given id.
		 * <p>
		 * This method should only be called if isLocal() returns true!
		 * 
		 * @param spsID The ID of the MWLEntry (Scheduled Procedure Step ID)
		 * @return
		 */
		public boolean deleteMWLEntry( String spsID ) {
			try {
		        Object o = server.invoke(mwlScuServiceName,
		                "deleteMWLEntry",
	                    new Object[]{ spsID },
						new String[]{ String.class.getName() });
		        return ((Boolean) o).booleanValue();
			} catch ( Exception x ) {
				log.error( "Exception occured in deleteMWLEntry: "+x.getMessage(), x );
			}
			return false;
		}

}
