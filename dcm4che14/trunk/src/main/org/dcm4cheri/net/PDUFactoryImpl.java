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

package org.dcm4cheri.net;

import org.dcm4che.net.*;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class PDUFactoryImpl extends PDUFactory {
    
    public PDUFactoryImpl() {
    }

    public AAssociateRQ newAAssociateRQ() {
        return new AAssociateRQImpl();
    }
    
    public AAssociateAC newAAssociateAC() {
        return new AAssociateACImpl();
    }
    
    public AAssociateRJ newAAssociateRJ(int result, int source, int reason) {
        return new AAssociateRJImpl(result, source, reason);
    }
    
    public PDataTF newPDataTF(int maxLength) {
        return new PDataTFImpl(maxLength);
    }
        
    public AReleaseRQ newAReleaseRQ() {
        return new AReleaseRQImpl();
    }
    
    public AReleaseRP newAReleaseRP() {
        return new AReleaseRPImpl();
    }

    public AAbort newAAbort(int source, int reason) {
        return new AAbortImpl(source, reason);
    }
    
    public PresContext newPresContext(byte id, String asuid, String[] tsuids) {
        return new PresContextImpl(id, 0, asuid, tsuids);
    }
    
    public PresContext newPresContext(byte id, int result, String tsuid) {
        return new PresContextImpl(id, result, null, new String[]{ tsuid } );
    }
    
    public AsyncOpsWindow newAsyncOpsWindow(
            int maxOpsInvoked, int maxOpsPerfomed) {
        return new AsyncOpsWindowImpl(maxOpsInvoked, maxOpsPerfomed);
    }
    
    public RoleSelection newRoleSelection(String uid, boolean scu, boolean scp)
    {
        return new RoleSelectionImpl(uid, scu, scp);
    }
    
    public ExtNegotiation newExtNegotiation(String uid, byte[] info) {
        return new ExtNegotiationImpl(uid, info);
    }
    
    public UnparsedPDU readFrom(InputStream in) throws IOException {
        return new UnparsedPDUImpl(in);
    }

    public PDU parse(UnparsedPDU raw) throws PDUParseException {
        switch (raw.type()) {
            case 1:
                return new AAssociateRQImpl(raw);
            case 2:
                return new AAssociateACImpl(raw);
            case 3:
                return new AAssociateRJImpl(raw);
            case 4:
                return new PDataTFImpl(raw);
            case 5:
                return new AReleaseRQImpl(raw);
            case 6:
                return new AReleaseRPImpl(raw);
            case 7:
                return new AAbortImpl(raw);
            default:
                throw new PDUParseException("Illegal PDU-type: " + raw);
        }
    }
}
