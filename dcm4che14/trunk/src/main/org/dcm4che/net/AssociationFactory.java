/*$Id$*/
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

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author  <a href="mailto:gunter.zeilinger@tiani.com">gunter zeilinger</a>
 * @version 1.0.0
 */
public abstract class AssociationFactory {

    public static AssociationFactory getInstance() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String name = System.getProperty("dcm4che.net.AssociationFactory",
                "org.dcm4cheri.net.AssociationFactoryImpl");
        try {
            return (AssociationFactory)loader.loadClass(name).newInstance();
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationError("class not found: " + name, ex); 
        } catch (InstantiationException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        } catch (IllegalAccessException ex) {
            throw new ConfigurationError("could not instantiate: " + name, ex); 
        }
    }

    static class ConfigurationError extends Error {
        ConfigurationError(String msg, Exception x) {
            super(msg,x);
        }
    }
    
    protected AssociationFactory() {
    }
    
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

    public abstract PDU readFrom(InputStream in, byte[] buf) throws IOException;

    public abstract Association newRequestor(Socket s, AssociationListener l)
            throws IOException;
    
    public abstract Association newAcceptor(Socket s, AssociationListener l)
            throws IOException;
    
    public abstract Dimse newDimse(int pcid, Command cmd);
    
    public abstract Dimse newDimse(int pcid, Command cmd, Dataset ds);
    
    public abstract Dimse newDimse(int pcid, Command cmd, DataSource src);
}
