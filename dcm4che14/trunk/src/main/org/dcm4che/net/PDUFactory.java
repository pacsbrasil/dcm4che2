/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public abstract class PDUFactory {

    public static PDUFactory getInstance() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String name = System.getProperty("dcm4che.net.PDUFactory",
                "org.dcm4cheri.net.PDUFactoryImpl");
        try {
            return (PDUFactory)loader.loadClass(name).newInstance();
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
    
    protected PDUFactory() {
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
            byte id, String asuid, String[] tsuids);
    public abstract PresContext newPresContext(
            byte id, int result, String tsuid);
    public abstract AsyncOpsWindow newAsyncOpsWindow(
            int maxOpsInvoked, int maxOpsPerfomed);    
    public abstract RoleSelection newRoleSelection(String uid,
            boolean scu, boolean scp);    
    public abstract ExtNegotiation newExtNegotiation(String uid, byte[] info);    

    public abstract UnparsedPDU readFrom(InputStream in) throws IOException;

    public abstract PDU parse(UnparsedPDU pdu) throws PDUParseException;
}
