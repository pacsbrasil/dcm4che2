/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2001,2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>*
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

import org.dcm4che.Implementation;
import java.net.Socket;

public abstract class AuditLoggerFactory {
    // Constants -----------------------------------------------------
    
    // Constructor ---------------------------------------------------
    public static AuditLoggerFactory getInstance() {
        return (AuditLoggerFactory)Implementation.findFactory(
        "dcm4che.auditlog.AuditLoggerFactory");
    }
    
    // Public --------------------------------------------------------
    public abstract AuditLogger newAuditLogger();
    
    public abstract Patient newPatient(String id, String name);
    
    public abstract RemoteNode newRemoteNode(
                                String ip, String hname, String aet);
    
    public abstract RemoteNode newRemoteNode(Socket socket, String aet);
    
    public abstract User newLocalUser(String name);
    
    public abstract User newRemoteUser(RemoteNode rnode);
    
    public abstract InstancesAction newInstancesAction(String action,
            String suid, Patient patient);
}
