/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
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
package org.dcm4che.auditlog;
import java.net.UnknownHostException;

import org.dcm4che.data.Dataset;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      September 1, 2002
 * @version    $Revision$ $Date$
 */
public interface AuditLogger
{

    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static String START = "Start";
    /**  Description of the Field */
    public final static String STOP = "Stop";
    /**  Description of the Field */
    public final static String SECURITY = "Security";
    /**  Description of the Field */
    public final static String NETWORKING = "Networking";

    // Methods -------------------------------------------------------
    /**
     *  Sets the syslogHost attribute of the AuditLogger object
     *
     * @param  syslogHost                The new syslogHost value
     * @exception  UnknownHostException  Description of the Exception
     */
    public void setSyslogHost(String syslogHost)
        throws UnknownHostException;


    /**
     *  Gets the syslogHost attribute of the AuditLogger object
     *
     * @return    The syslogHost value
     */
    public String getSyslogHost();


    /**
     *  Sets the syslogPort attribute of the AuditLogger object
     *
     * @param  syslogPort  The new syslogPort value
     */
    public void setSyslogPort(int syslogPort);


    /**
     *  Gets the syslogPort attribute of the AuditLogger object
     *
     * @return    The syslogPort value
     */
    public int getSyslogPort();


    /**
     *  Gets the facility attribute of the AuditLogger object
     *
     * @return    The facility value
     */
    public String getFacility();


    /**
     *  Sets the facility attribute of the AuditLogger object
     *
     * @param  facility  The new facility value
     */
    public void setFacility(String facility);


    /**
     *  Gets the logActorStartStop attribute of the AuditLogger object
     *
     * @return    The logActorStartStop value
     */
    public boolean isLogActorStartStop();


    /**
     *  Sets the logActorStartStop attribute of the AuditLogger object
     *
     * @param  enable  The new logActorStartStop value
     */
    public void setLogActorStartStop(boolean enable);


    /**
     *  Gets the logInstancesStored attribute of the AuditLogger object
     *
     * @return    The logInstancesStored value
     */
    public boolean isLogInstancesStored();


    /**
     *  Sets the logInstancesStored attribute of the AuditLogger object
     *
     * @param  enable  The new logInstancesStored value
     */
    public void setLogInstancesStored(boolean enable);


    /**
     *  Gets the logBeginStoringInstances attribute of the AuditLogger object
     *
     * @return    The logBeginStoringInstances value
     */
    public boolean isLogBeginStoringInstances();


    /**
     *  Sets the logBeginStoringInstances attribute of the AuditLogger object
     *
     * @param  enable  The new logBeginStoringInstances value
     */
    public void setLogBeginStoringInstances(boolean enable);


    /**
     *  Gets the logInstancesSent attribute of the AuditLogger object
     *
     * @return    The logInstancesSent value
     */
    public boolean isLogInstancesSent();


    /**
     *  Sets the logInstancesSent attribute of the AuditLogger object
     *
     * @param  enable  The new logInstancesSent value
     */
    public void setLogInstancesSent(boolean enable);


    /**
     *  Gets the logDicomQuery attribute of the AuditLogger object
     *
     * @return    The logDicomQuery value
     */
    public boolean isLogDicomQuery();


    /**
     *  Sets the logDicomQuery attribute of the AuditLogger object
     *
     * @param  enable  The new logDicomQuery value
     */
    public void setLogDicomQuery(boolean enable);


    /**
     *  Gets the logSecurityAlert attribute of the AuditLogger object
     *
     * @return    The logSecurityAlert value
     */
    public boolean isLogSecurityAlert();


    /**
     *  Sets the logSecurityAlert attribute of the AuditLogger object
     *
     * @param  enable  The new logSecurityAlert value
     */
    public void setLogSecurityAlert(boolean enable);


    /**
     *  Gets the logUserAuthenticated attribute of the AuditLogger object
     *
     * @return    The logUserAuthenticated value
     */
    public boolean isLogUserAuthenticated();


    /**
     *  Sets the logUserAuthenticated attribute of the AuditLogger object
     *
     * @param  enable  The new logUserAuthenticated value
     */
    public void setLogUserAuthenticated(boolean enable);


    /**
     *  Gets the logActorConfig attribute of the AuditLogger object
     *
     * @return    The logActorConfig value
     */
    public boolean isLogActorConfig();


    /**
     *  Sets the logActorConfig attribute of the AuditLogger object
     *
     * @param  enable  The new logActorConfig value
     */
    public void setLogActorConfig(boolean enable);


    /**
     *  Gets the logExport attribute of the AuditLogger object
     *
     * @return    The logExport value
     */
    public boolean isLogExport();


    /**
     *  Sets the logExport attribute of the AuditLogger object
     *
     * @param  enable  The new logExport value
     */
    public void setLogExport(boolean enable);


    /**
     *  Description of the Method
     *
     * @param  actorName  Description of the Parameter
     * @param  action     Description of the Parameter
     * @param  user       Description of the Parameter
     */
    public void logActorStartStop(String actorName, String action, User user);


    /**
     *  Description of the Method
     *
     * @param  rNode   Description of the Parameter
     * @param  action  Description of the Parameter
     */
    public void logInstancesStored(RemoteNode rNode, InstancesAction action);


    /**
     *  Description of the Method
     *
     * @param  rNode   Description of the Parameter
     * @param  action  Description of the Parameter
     */
    public void logBeginStoringInstances(RemoteNode rNode, InstancesAction action);


    /**
     *  Description of the Method
     *
     * @param  rNode   Description of the Parameter
     * @param  action  Description of the Parameter
     */
    public void logInstancesSent(RemoteNode rNode, InstancesAction action);


    /**
     *  Description of the Method
     *
     * @param  keys       Description of the Parameter
     * @param  requestor  Description of the Parameter
     * @param  cuid       Description of the Parameter
     */
    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid);


    /**
     *  Description of the Method
     *
     * @param  alertType    Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  description  Description of the Parameter
     */
    public void logSecurityAlert(String alertType, User user, String description);


    /**
     *  Description of the Method
     *
     * @param  localUserName  Description of the Parameter
     * @param  action         Description of the Parameter
     */
    public void logUserAuthenticated(String localUserName, String action);


    /**
     *  Description of the Method
     *
     * @param  description  Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  configType   Description of the Parameter
     */
    public void logActorConfig(String description, User user, String configType);


    /**
     *  Description of the Method
     *
     * @param  media  Description of the Parameter
     * @param  user   Description of the Parameter
     */
    public void logExport(MediaDescription media, User user);
}

