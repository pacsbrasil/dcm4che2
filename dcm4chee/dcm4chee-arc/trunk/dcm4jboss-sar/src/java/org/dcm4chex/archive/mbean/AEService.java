/*                                                                           *
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG                             *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4chex.archive.mbean;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.ObjectName;

import org.dcm4chex.archive.ejb.interfaces.AEManager;
import org.dcm4chex.archive.ejb.interfaces.AEManagerHome;
import org.dcm4chex.archive.ejb.jdbc.AEData;
import org.dcm4chex.archive.util.EJBHomeFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      July 24, 2002
 * @version    $Revision$ $Date$
 */
public class AEService extends ServiceMBeanSupport
{
    private ObjectName auditLogName;
    private ObjectName echoServiceName;
    
    private boolean dontSaveIP = true;
    private int[] portNumbers;


    public ObjectName getAuditLoggerName() {
        return auditLogName;
    }

    public void setAuditLoggerName(ObjectName auditLogName) {
        this.auditLogName = auditLogName;
    }
    
	/**
	 * @return Returns the echoServiceName.
	 */
	public ObjectName getEchoServiceName() {
		return echoServiceName;
	}
	/**
	 * @param echoServiceName The echoServiceName to set.
	 */
	public void setEchoServiceName(ObjectName echoServiceName) {
		this.echoServiceName = echoServiceName;
	}
	/**
	 * @return Returns the autoConfig.
	 */
	public boolean isDontSaveIP() {
		return dontSaveIP;
	}
	/**
	 * @param dontSaveIP The dontSaveIP to set.
	 */
	public void setDontSaveIP(boolean dontSaveIP) {
		this.dontSaveIP = dontSaveIP;
	}
	/**
	 * @return Returns the portNumbers.
	 */
	public String getPortNumbers() {
		if ( portNumbers == null || portNumbers.length < 1 ) return "NONE";
		int len = portNumbers.length;
		String first = String.valueOf(portNumbers[0]);
		if ( len == 1 ) return first;
		StringBuffer sb = new StringBuffer(first);
		for ( int i=1 ; i < len ; i++ )
			sb.append(",").append(portNumbers[i]);
		return sb.toString();
	}
	/**
	 * @param portNumbers The portNumbers to set.
	 */
	public void setPortNumbers(String ports) {
		if ( ports == null || "NONE".equalsIgnoreCase(ports) ) {
			portNumbers = null;
		} else {
			StringTokenizer st = new StringTokenizer(ports, ",");
			portNumbers = new int[st.countTokens()];
			for ( int i=0 ; st.hasMoreTokens() ; i++ ) {
				portNumbers[i] = Integer.parseInt(st.nextToken());
			}
		}
	}

	
	public String getAEs()
        throws RemoteException, Exception
    {
        Collection c = lookupAEManager().getAes();
        StringBuffer sb = new StringBuffer();
        AEData ae;
        for (Iterator iter = c.iterator() ; iter.hasNext() ;) {
        	ae = (AEData) iter.next();
            sb.append( ae.toString() ).append(" cipher:").append(ae.getCipherSuitesAsString()).append("\r\n");
        }
        return sb.toString();
    }
	
	public List listAEs() throws RemoteException, Exception {
		return lookupAEManager().getAes();
	}
	
    
    public AEData getAE( String title ) throws RemoteException, Exception {
    	return lookupAEManager().getAeByTitle( title );
    }

    public AEData getAE( String title, String host ) throws RemoteException, Exception {
    	return getAE( title, host == null ? null : InetAddress.getByName(host) );
    }
    
    public AEData getAE( String title, InetAddress addr ) throws RemoteException, Exception {
    	AEManager aeManager = lookupAEManager();
    	AEData ae = aeManager.getAeByTitle( title );
    	if ( ae != null || portNumbers==null || addr == null ) return ae;
		
		String aeHost = addr.getHostName();
		for ( int i = 0 ; i < portNumbers.length ; i++ ) {
			ae = new AEData( -1, title, aeHost, portNumbers[i], null );
			if ( echo(ae) ) {
				if ( dontSaveIP ) {
					if ( !aeHost.equals(addr.getHostAddress()))
						aeManager.newAE(ae);
				} else {
					aeManager.newAE(ae);
				}
	            logActorConfig("Add new auto-configured AE " + ae);
				return ae;
			}
		}
		return null;
    }


	/**
     * Adds (replace) a new AE Title.
     * 
     * @param aet 		Application Entity Title
     * @param host		Hostname or IP addr.
     * @param port		port number
     * @param cipher	String with cypher(s) to create a secure connection (seperated with ',') or null
     * @throws Exception
     * @throws RemoteException
     */
    public void addAE(String title, String host, int port, String cipher)
        throws RemoteException, Exception
    {
    	host = InetAddress.getByName(host).getCanonicalHostName();
    	
        AEManager aeManager = lookupAEManager();
        AEData aeOld = aeManager.getAeByTitle( title );
        if ( aeOld == null ) {
        	AEData aeNew = new AEData(-1,title,host,port,cipher);
        	aeManager.newAE( aeNew );
            logActorConfig("Add AE " + aeNew +" cipher:"+aeNew.getCipherSuitesAsString());
        } else {
        	AEData aeNew = new AEData(aeOld.getPk(),title,host,port,cipher);
        	aeManager.updateAE( aeNew );
            logActorConfig("Modify AE " + aeOld +" -> "+aeNew);
        }
    }


    public void removeAE(String titles)
        throws Exception
    {
        StringTokenizer st = new StringTokenizer(titles, " ,;\t\r\n");
        AEData ae;
        AEManager aeManager = lookupAEManager();
        while (st.hasMoreTokens()) {
            ae = aeManager.getAeByTitle( st.nextToken() );
            aeManager.removeAE(ae.getPk());
            logActorConfig("Remove AE " + ae);
        }
    }

    private void logActorConfig(String desc)
    {
		log.info(desc);
        if (auditLogName == null) {
            return;
        }
        try {
            server.invoke(auditLogName, "logActorConfig",
                    new Object[]{
                        desc,
                        "NetWorking"
                    },
                    new String[]{
                    String.class.getName(),
                    String.class.getName(),
                    });
        } catch (Exception e) {
            log.warn("Failed to log ActorConfig:", e);
        }
    }
    
    private boolean echo(AEData ae) {
        try {
            Boolean result = (Boolean) server.invoke(this.echoServiceName, "checkEcho",
                    new Object[]{ae},
                    new String[]{AEData.class.getName()});
            return result.booleanValue();
        } catch (Exception e) {
            log.warn("Failed to use echo service:", e);
            return false;
        }
    	
    }

	protected AEManager lookupAEManager() throws Exception
	{
		AEManagerHome home =
			(AEManagerHome) EJBHomeFactory.getFactory().lookup(
					AEManagerHome.class,
					AEManagerHome.JNDI_NAME);
		return home.create();
	}			
}

