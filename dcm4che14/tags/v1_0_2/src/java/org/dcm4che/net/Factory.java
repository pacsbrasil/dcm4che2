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

package org.dcm4che.net;

import org.dcm4che.Implementation;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$ $Date$
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
public abstract class Factory {
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   public static Factory getInstance() {
      return (Factory)Implementation.findFactory(
            "dcm4che.net.Factory");
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public abstract AAssociateRQ newAAssociateRQ();
   
   public abstract AAssociateAC newAAssociateAC();
   
   public abstract AAssociateRJ newAAssociateRJ(
         int result, int source, int reason);
   
   public abstract PDataTF newPDataTF(int maxLength);
   
   public abstract AReleaseRQ newAReleaseRQ();
   
   public abstract AReleaseRP newAReleaseRP();
   
   public abstract AAbort newAAbort(int source, int reason);
   
   public abstract PresContext newPresContext(
         int pcid, String asuid, String[] tsuids);
   
   public abstract PresContext newPresContext(
         int pcid, int result, String tsuid);
   
   public abstract AsyncOpsWindow newAsyncOpsWindow(
         int maxOpsInvoked, int maxOpsPerfomed);
   
   public abstract RoleSelection newRoleSelection(String uid,
         boolean scu, boolean scp);
   
   public abstract ExtNegotiation newExtNegotiation(String uid, byte[] info);
   
   public abstract PDU readFrom(InputStream in, byte[] buf)
   throws IOException;
   
   public abstract Association newRequestor(Socket s)
   throws IOException;
   
   public abstract Association newAcceptor(Socket s)
   throws IOException;
   
   public abstract ActiveAssociation newActiveAssociation(Association assoc,
         DcmServiceRegistry services);

   public abstract Dimse newDimse(int pcid, Command cmd);
   
   public abstract Dimse newDimse(int pcid, Command cmd, Dataset ds);
   
   public abstract Dimse newDimse(int pcid, Command cmd, DataSource src);
   
   public abstract AcceptorPolicy newAcceptorPolicy();
   
   public abstract DcmServiceRegistry newDcmServiceRegistry();
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
