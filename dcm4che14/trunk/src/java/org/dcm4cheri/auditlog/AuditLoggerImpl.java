/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
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
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.data.Dataset;
import org.dcm4che.util.SyslogWriter;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since August 22, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class AuditLoggerImpl implements AuditLogger {
    
    // Constants -----------------------------------------------------
    private static final Logger log = Logger.getLogger(AuditLoggerImpl.class);    
            
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
    
    // Constructors --------------------------------------------------
    
    // Methods -------------------------------------------------------
    public void setSyslogHost(String syslogHost) throws UnknownHostException {
        writer.setSyslogHost(syslogHost);
    }
    
    public String getSyslogHost() {
        return writer.getSyslogHost();
    }
    
    public void setSyslogPort(int syslogPort) {
        writer.setSyslogPort(syslogPort);
    }
    
    public int getSyslogPort() {
        return writer.getSyslogPort();
    }
    
    public String getFacility() {
        return writer.getFacilityAsString();
    }
    
    public void setFacility(String facility) {
        writer.setFacility(facility);
    }

    public void logActorStartStop(String actorName, String action, User user) {
        if (!actorStartStop) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newActorStartStop(actorName, action, user,
                    writer.getLocalHostName(), millis).toString(),
                 millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesStored(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesStored) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newInstancesStored(rnode, instanceActionDescription,
                    writer.getLocalHostName(), millis).toString(),
                 millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
    
    public void logBeginStoringInstances(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!beginStoringInstances) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newBeginStoringInstances(rnode, instanceActionDescription,
                    writer.getLocalHostName(), millis).toString(),
                 millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logInstancesSent(RemoteNode rnode,
            InstancesAction instanceActionDescription) {
        if (!instancesSent) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newInstancesSent(rnode, instanceActionDescription,
                    writer.getLocalHostName(), millis).toString(),
                 millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
    
    public void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid) {
        if (!dicomQuery) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newDicomQuery(keys, requestor, cuid,
                    writer.getLocalHostName(), millis).toString(),
                 millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logSecurityAlert(String alertType, User user,
            String description) {
        if (!securityAlert) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newSecurityAlert(alertType, user, description,
                    writer.getLocalHostName(), millis).toString(),
                millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }

    public void logUserAuthenticated(String localUserName, String action) {
        if (!userAuthenticated) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newUserAuthenticated(localUserName, action,
                    writer.getLocalHostName(), millis).toString(),
                millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }
    
    public void logActorConfig(String description, User user,
            String configType) {
        if (!actorConfig) {
            return;
        }
        try {
            long millis = System.currentTimeMillis();
            writer.write(SyslogWriter.LOG_INFO,
                IHEYr4.newActorConfig(description, user, configType,
                    writer.getLocalHostName(), millis).toString(),
                millis);
        } catch (IOException e) {
            log.error("Could not write to syslog:", e);
        }
    }    
    
    /** Getter for property actorStartStop.
     * @return Value of property actorStartStop.
     */
    public boolean isLogActorStartStop() {
        return actorStartStop;
    }
    
    /** Setter for property actorStartStop.
     * @param actorStartStop New value of property actorStartStop.
     */
    public void setLogActorStartStop(boolean actorStartStop) {
        this.actorStartStop = actorStartStop;
    }
    
    /** Getter for property instancesStored.
     * @return Value of property instancesStored.
     */
    public boolean isLogInstancesStored() {
        return instancesStored;
    }
    
    /** setLogter for property instancesStored.
     * @param instancesStored New value of property instancesStored.
     */
    public void setLogInstancesStored(boolean instancesStored) {
        this.instancesStored = instancesStored;
    }
    
    /** Getter for property beginStoringInstances.
     * @return Value of property beginStoringInstances.
     */
    public boolean isLogBeginStoringInstances() {
        return beginStoringInstances;
    }
    
    /** Setter for property beginStoringInstances.
     * @param beginStoringInstances New value of property beginStoringInstances.
     */
    public void setLogBeginStoringInstances(boolean beginStoringInstances) {
        this.beginStoringInstances = beginStoringInstances;
    }
    
    /** Getter for property instancesSent.
     * @return Value of property instancesSent.
     */
    public boolean isLogInstancesSent() {
        return instancesSent;
    }
    
    /** Setter for property instancesSent.
     * @param instancesSent New value of property instancesSent.
     */
    public void setLogInstancesSent(boolean instancesSent) {
        this.instancesSent = instancesSent;
    }
    
    /** Getter for property dicomQuery.
     * @return Value of property dicomQuery.
     */
    public boolean isLogDicomQuery() {
        return dicomQuery;
    }
    
    /** Setter for property dicomQuery.
     * @param dicomQuery New value of property dicomQuery.
     */
    public void setLogDicomQuery(boolean dicomQuery) {
        this.dicomQuery = dicomQuery;
    }
    
    /** Getter for property securityAlert.
     * @return Value of property securityAlert.
     */
    public boolean isLogSecurityAlert() {
        return securityAlert;
    }
    
    /** Setter for property securityAlert.
     * @param securityAlert New value of property securityAlert.
     */
    public void setLogSecurityAlert(boolean securityAlert) {
        this.securityAlert = securityAlert;
    }
    
    /** Getter for property userAuthenticated.
     * @return Value of property userAuthenticated.
     */
    public boolean isLogUserAuthenticated() {
        return userAuthenticated;
    }
    
    /** Setter for property userAuthenticated.
     * @param userAuthenticated New value of property userAuthenticated.
     */
    public void setLogUserAuthenticated(boolean userAuthenticated) {
        this.userAuthenticated = userAuthenticated;
    }
    
    /** Getter for property actorConfig.
     * @return Value of property actorConfig.
     */
    public boolean isLogActorConfig() {
        return actorConfig;
    }
    
    /** Setter for property actorConfig.
     * @param actorConfig New value of property actorConfig.
     */
    public void setLogActorConfig(boolean actorConfig) {
        this.actorConfig = actorConfig;
    }
    
}
