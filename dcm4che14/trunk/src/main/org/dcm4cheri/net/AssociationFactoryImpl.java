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
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public final class AssociationFactoryImpl extends AssociationFactory {
    
    public AssociationFactoryImpl() {
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
        return AReleaseRQImpl.getInstance();
    }
    
    public AReleaseRP newAReleaseRP() {
        return AReleaseRPImpl.getInstance();
    }

    public AAbort newAAbort(int source, int reason) {
        return new AAbortImpl(source, reason);
    }
    
    public PresContext newPresContext(int pcid, String asuid, String[] tsuids) {
        return new PresContextImpl(0x020, pcid, 0, asuid, tsuids);
    }
    
    public PresContext newPresContext(int pcid, int result, String tsuid) {
        return new PresContextImpl(0x021, pcid, result, null,
                new String[]{ tsuid } );
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
    
    public PDU readFrom(InputStream in)
            throws IOException, PDUException {
        UnparsedPDUImpl raw = new UnparsedPDUImpl(in);
        switch (raw.type()) {
            case 1:
                return AAssociateRQImpl.parse(raw);
            case 2:
                return AAssociateACImpl.parse(raw);
            case 3:
                return AAssociateRJImpl.parse(raw);
            case 4:
                return PDataTFImpl.parse(raw);
            case 5:
                return AReleaseRQImpl.parse(raw);
            case 6:
                return AReleaseRPImpl.parse(raw);
            case 7:
                return AAbortImpl.parse(raw);
            default:
                throw new PDUException("Unrecognized " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.UNRECOGNIZED_PDU));
        }
    }
    
    public Association newRequestor(Socket s, AssociationListener l)
            throws IOException {
        return new AssociationImpl(s, true, l);
    }
    
    public Association newAcceptor(Socket s, AssociationListener l)
            throws IOException {
        return new AssociationImpl(s, false, l);
    }
    
    public Dimse newDimse(int pcid, Command cmd) {
        return new DimseImpl(pcid, cmd, null, null);
    }
    
    public Dimse newDimse(int pcid, Command cmd, Dataset ds) {
        return new DimseImpl(pcid, cmd, ds, null);
    }
    
    public Dimse newDimse(int pcid, Command cmd, DataSource src) {
        return new DimseImpl(pcid, cmd, null, src);
    }
}
