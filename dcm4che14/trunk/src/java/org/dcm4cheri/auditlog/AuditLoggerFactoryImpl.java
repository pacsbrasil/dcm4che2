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

package org.dcm4cheri.auditlog;

import org.dcm4che.auditlog.AuditLogger;
import org.dcm4che.auditlog.AuditLoggerFactory;
import org.dcm4che.auditlog.InstancesAction;
import org.dcm4che.auditlog.Patient;
import org.dcm4che.auditlog.RemoteNode;
import org.dcm4che.auditlog.User;
import org.dcm4che.auditlog.ArrService;
import org.dcm4che.auditlog.ArrInputException;
import java.net.Socket;

public class AuditLoggerFactoryImpl extends AuditLoggerFactory {
        
    // Constants -----------------------------------------------------
    
    // Constructor ---------------------------------------------------
    
    // Public --------------------------------------------------------
    public AuditLogger newAuditLogger() {
        return new AuditLoggerImpl();
    }
    
    public Patient newPatient(String id, String name) {
        return new PatientImpl(id, name);
    }
    
    public RemoteNode newRemoteNode(String ip, String hname, String aet) {
        return new RemoteNodeImpl(ip, hname, aet);
    }
    
    public RemoteNode newRemoteNode(Socket socket, String aet) {
        return new RemoteNodeImpl(socket, aet);
    }

    
    public User newLocalUser(String name) {
        return new LocalUserImpl(name);
    }
    
    public User newRemoteUser(RemoteNode rnode) {
        return new RemoteUserImpl(rnode);
    }
    
    public InstancesAction newInstancesAction(String action, String suid,
            Patient patient) {
        return new InstancesActionImpl(action, suid, patient);
    }

    public ArrService newArrService(boolean validating) {
    	try {
		return new ArrServiceImpl(validating);
	}
	catch (ArrInputException e) {
		return null;
	}
    }
}
