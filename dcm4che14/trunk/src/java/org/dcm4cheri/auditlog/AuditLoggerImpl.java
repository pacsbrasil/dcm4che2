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
package org.dcm4cheri.auditlog;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Category;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.MediaDescription;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.HostNameUtils;
import org.dcm4che.util.SyslogWriter;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      August 22, 2002
 * @version    $Revision$ $Date$
 */
class AuditLoggerImpl implements AuditLogger
{

    // Constants -----------------------------------------------------
    private final Category log;

    // Variables -----------------------------------------------------
    private final SyslogWriter writer = new SyslogWriter();

    private boolean actorStartStop = true;
    private boolean instancesStored = true;
    private boolean beginStoringInstances = true;
    private boolean instancesSent = true;
    private boolean dicomQuery = true;
    private boolean securityAlert = true;
    private boolean userAuthenticated = true;
    private boolean actorConfig = true;
    private boolean logExport = true;

    // Constructors --------------------------------------------------
    AuditLoggerImpl(Category log)
    {
        this.log = log;
    }

    // Methods -------------------------------------------------------
    /**
     *  Sets the syslogHost attribute of the AuditLoggerImpl object
     *
     * @param  syslogHost                The new syslogHost value
     * @exception  UnknownHostException  Description of the Exception
     */
    public void setSyslogHost(String syslogHost)
        throws UnknownHostException
    {
        writer.setSyslogHost(syslogHost);
    }


    /**
     *  Gets the syslogHost attribute of the AuditLoggerImpl object
     *
     * @return    The syslogHost value
     */
    public String getSyslogHost()
    {
        return writer.getSyslogHost();
    }


    /**
     *  Sets the syslogPort attribute of the AuditLoggerImpl object
     *
     * @param  syslogPort  The new syslogPort value
     */
    public void setSyslogPort(int syslogPort)
    {
        writer.setSyslogPort(syslogPort);
    }


    /**
     *  Gets the syslogPort attribute of the AuditLoggerImpl object
     *
     * @return    The syslogPort value
     */
    public int getSyslogPort()
    {
        return writer.getSyslogPort();
    }


    /**
     *  Gets the facility attribute of the AuditLoggerImpl object
     *
     * @return    The facility value
     */
    public String getFacility()
    {
        return writer.getFacilityAsString();
    }


    /**
     *  Sets the facility attribute of the AuditLoggerImpl object
     *
     * @param  facility  The new facility value
     */
    public void setFacility(String facility)
    {
        writer.setFacility(facility);
    }


    /**
     *  Description of the Method
     *
     * @param  actorName  Description of the Parameter
     * @param  action     Description of the Parameter
     * @param  user       Description of the Parameter
     */
    public void logActorStartStop(String actorName, String action, User user)
    {
        if (!actorStartStop) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newActorStartStop(actorName, action, user,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  rnode                      Description of the Parameter
     * @param  instanceActionDescription  Description of the Parameter
     */
    public void logInstancesStored(RemoteNode rnode,
            InstancesAction instanceActionDescription)
    {
        if (!instancesStored) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newInstancesStored(rnode, instanceActionDescription,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  rnode                      Description of the Parameter
     * @param  instanceActionDescription  Description of the Parameter
     */
    public void logBeginStoringInstances(RemoteNode rnode,
            InstancesAction instanceActionDescription)
    {
        if (!beginStoringInstances) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newBeginStoringInstances(rnode, instanceActionDescription,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  rnode                      Description of the Parameter
     * @param  instanceActionDescription  Description of the Parameter
     */
    public void logInstancesSent(RemoteNode rnode,
            InstancesAction instanceActionDescription)
    {
        if (!instancesSent) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newInstancesSent(rnode, instanceActionDescription,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  keys       Description of the Parameter
     * @param  requestor  Description of the Parameter
     * @param  cuid       Description of the Parameter
     */
    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid)
    {
        if (!dicomQuery) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newDicomQuery(keys, requestor, cuid,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  alertType    Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  description  Description of the Parameter
     */
    public void logSecurityAlert(String alertType, User user,
            String description)
    {
        if (!securityAlert) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newSecurityAlert(alertType, user, description,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  localUserName  Description of the Parameter
     * @param  action         Description of the Parameter
     */
    public void logUserAuthenticated(String localUserName, String action)
    {
        if (!userAuthenticated) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newUserAuthenticated(localUserName, action,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  description  Description of the Parameter
     * @param  user         Description of the Parameter
     * @param  configType   Description of the Parameter
     */
    public void logActorConfig(String description, User user,
            String configType)
    {
        if (!actorConfig) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newActorConfig(description, user, configType,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }


    /**
     * Getter for property actorStartStop.
     *
     * @return    Value of property actorStartStop.
     */
    public boolean isLogActorStartStop()
    {
        return actorStartStop;
    }


    /**
     * Setter for property actorStartStop.
     *
     * @param  actorStartStop  New value of property actorStartStop.
     */
    public void setLogActorStartStop(boolean actorStartStop)
    {
        this.actorStartStop = actorStartStop;
    }


    /**
     * Getter for property instancesStored.
     *
     * @return    Value of property instancesStored.
     */
    public boolean isLogInstancesStored()
    {
        return instancesStored;
    }


    /**
     * setLogter for property instancesStored.
     *
     * @param  instancesStored  New value of property instancesStored.
     */
    public void setLogInstancesStored(boolean instancesStored)
    {
        this.instancesStored = instancesStored;
    }


    /**
     * Getter for property beginStoringInstances.
     *
     * @return    Value of property beginStoringInstances.
     */
    public boolean isLogBeginStoringInstances()
    {
        return beginStoringInstances;
    }


    /**
     * Setter for property beginStoringInstances.
     *
     * @param  beginStoringInstances  New value of property beginStoringInstances.
     */
    public void setLogBeginStoringInstances(boolean beginStoringInstances)
    {
        this.beginStoringInstances = beginStoringInstances;
    }


    /**
     * Getter for property instancesSent.
     *
     * @return    Value of property instancesSent.
     */
    public boolean isLogInstancesSent()
    {
        return instancesSent;
    }


    /**
     * Setter for property instancesSent.
     *
     * @param  instancesSent  New value of property instancesSent.
     */
    public void setLogInstancesSent(boolean instancesSent)
    {
        this.instancesSent = instancesSent;
    }


    /**
     * Getter for property dicomQuery.
     *
     * @return    Value of property dicomQuery.
     */
    public boolean isLogDicomQuery()
    {
        return dicomQuery;
    }


    /**
     * Setter for property dicomQuery.
     *
     * @param  dicomQuery  New value of property dicomQuery.
     */
    public void setLogDicomQuery(boolean dicomQuery)
    {
        this.dicomQuery = dicomQuery;
    }


    /**
     * Getter for property securityAlert.
     *
     * @return    Value of property securityAlert.
     */
    public boolean isLogSecurityAlert()
    {
        return securityAlert;
    }


    /**
     * Setter for property securityAlert.
     *
     * @param  securityAlert  New value of property securityAlert.
     */
    public void setLogSecurityAlert(boolean securityAlert)
    {
        this.securityAlert = securityAlert;
    }


    /**
     * Getter for property userAuthenticated.
     *
     * @return    Value of property userAuthenticated.
     */
    public boolean isLogUserAuthenticated()
    {
        return userAuthenticated;
    }


    /**
     * Setter for property userAuthenticated.
     *
     * @param  userAuthenticated  New value of property userAuthenticated.
     */
    public void setLogUserAuthenticated(boolean userAuthenticated)
    {
        this.userAuthenticated = userAuthenticated;
    }


    /**
     * Getter for property actorConfig.
     *
     * @return    Value of property actorConfig.
     */
    public boolean isLogActorConfig()
    {
        return actorConfig;
    }


    /**
     * Setter for property actorConfig.
     *
     * @param  actorConfig  New value of property actorConfig.
     */
    public void setLogActorConfig(boolean actorConfig)
    {
        this.actorConfig = actorConfig;
    }


    /**
     *  Gets the logExport attribute of the AuditLoggerImpl object
     *
     * @return    The logExport value
     */
    public boolean isLogExport()
    {
        return logExport;
    }


    /**
     *  Sets the logExport attribute of the AuditLoggerImpl object
     *
     * @param  logExport  The new logExport value
     */
    public void setLogExport(boolean logExport)
    {
        this.logExport = logExport;
    }


    /**
     *  Description of the Method
     *
     * @param  media  Description of the Parameter
     * @param  user   Description of the Parameter
     */
    public void logExport(MediaDescription media, User user)
    {
        if (!logExport) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                    IHEYr4.newExport(media, user,
                    HostNameUtils.getLocalHostName(), millis).toString(),
                    millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
}

