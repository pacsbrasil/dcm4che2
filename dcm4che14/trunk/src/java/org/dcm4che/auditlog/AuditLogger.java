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

package org.dcm4che.auditlog;

import org.dcm4che.data.Dataset;
import java.net.UnknownHostException;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 * @since September 1, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public interface AuditLogger {
    
    // Constants -----------------------------------------------------
    
    // Methods -------------------------------------------------------
    void setSyslogHost(String syslogHost) throws UnknownHostException;
    
    String getSyslogHost();
    
    void setSyslogPort(int syslogPort);
    
    int getSyslogPort();
    
    String getFacility();
    
    void setFacility(String facility);
    
    boolean isActorStartStopEnabled();

    void setActorStartStopEnabled(boolean enable);
    
    boolean isInstancesStoredEnabled();
    
    void setInstancesStoredEnabled(boolean enable);
    
    boolean isBeginStoringInstancesEnabled();

    void setBeginStoringInstancesEnabled(boolean enable);
    
    boolean isInstancesSentEnabled();
    
    void setInstancesSentEnabled(boolean enable);
    
    boolean isDicomQueryEnabled();

    void setDicomQueryEnabled(boolean enable);
    
    boolean isSecurityAlertEnabled();
    
    void setSecurityAlertEnabled(boolean enable);
    
    boolean isUserAuthenticatedEnabled();
    
    void setUserAuthenticatedEnabled(boolean enable);
    
    boolean isActorConfigEnabled();
    
    void setActorConfigEnabled(boolean enable);
    
    void logActorStartStop(String actorName, String action, User user);

    void logInstancesStored(RemoteNode rNode, InstancesAction action);
    
    void logBeginStoringInstances(RemoteNode rNode, InstancesAction action);

    void logInstancesSent(RemoteNode rNode, InstancesAction action);
    
    void logDicomQuery(Dataset keys, RemoteNode requestor, String cuid);

    void logSecurityAlert(String alertType, User user, String description);
    
    void logUserAuthenticated(String localUserName, String action);
    
    void logActorConfig(String description, User user, String configType);
}
