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

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PresContextImpl implements PresContext {
    private final int type;
    private final int pcid;
    private final int result;
    private final String asuid;
    private final List tsuids;

    PresContextImpl(int type, int pcid, int result, String asuid,
            String[] tsuids) {
        if ((pcid | 1) == 0 || (pcid & ~0xff) != 0) {
            throw new IllegalArgumentException("pcid=" + pcid);
        }         
        this.type = type;
        this.pcid = pcid;
        this.result = result;
        this.asuid = asuid;
        this.tsuids = new ArrayList(Arrays.asList(tsuids));
    }
    
    PresContextImpl(int type, DataInputStream din, int len)
            throws IOException, DcmULServiceException {
        this.type = type;
        this.pcid = din.readUnsignedByte();
        din.readUnsignedByte();
        this.result = din.readUnsignedByte();
        din.readUnsignedByte();
        int remain = len - 4;
        String asuid = null;
        this.tsuids = new LinkedList();
        while (remain > 0) {
            int uidtype = din.readUnsignedByte();
            din.readUnsignedByte();
            int uidlen = din.readUnsignedShort();
            switch (uidtype) {
                case 0x30:
                    if (type == 0x21 || asuid != null) {
                        throw new DcmULServiceException(
                                "Unexpected Abstract Syntax sub-item in"
                                + " Presentation Context",
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                            AAbort.UNEXPECTED_PDU_PARAMETER));
                    }
                    asuid = AAssociateRQACImpl.readASCII(din, uidlen);
                    break;
                case 0x40:
                    if (type == 0x21 && !tsuids.isEmpty()) {
                        throw new DcmULServiceException(
                                "Unexpected Transfer Syntax sub-item in"
                                + " Presentation Context",
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                            AAbort.UNEXPECTED_PDU_PARAMETER));
                    }
                    tsuids.add(AAssociateRQACImpl.readASCII(din, uidlen));
                    break;
                default:
                    throw new DcmULServiceException(
                            "unrecognized item type "
                                    + Integer.toHexString(uidtype) + 'H',
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU_PARAMETER));
            }
            remain -= 4 + uidlen;
        }
        this.asuid = asuid;
        if (remain < 0) {
            throw new DcmULServiceException("Presentation item length: " + len
                + " mismatch length of sub-items",
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
    }
    
    void writeTo(DataOutputStream dout) throws IOException {
        dout.write(type);
        dout.write(0);
        dout.writeShort(length());
        dout.write(pcid);
        dout.write(0);
        dout.write(result);
        dout.write(0);
        if (asuid != null) {
            dout.write(0x30);
            dout.write(0);
            dout.writeShort(asuid.length());
            dout.writeBytes(asuid);
        }
        for (Iterator it = tsuids.iterator(); it.hasNext();) {
            String tsuid = (String)it.next();
            dout.write(0x40);
            dout.write(0);
            dout.writeShort(tsuid.length());
            dout.writeBytes(tsuid);
        }            
    }
    
    final int length() {
        int retval = 4;
        if (asuid != null) {
            retval += 4 + asuid.length();
        }
        for (Iterator it = tsuids.iterator(); it.hasNext();) {
            retval += 4 + ((String)it.next()).length();
        }
        return retval;
    }

    final int type() {
        return type;
    }    

    public final int pcid() {
        return pcid;
    }    
    
    public final int result() {
        return result;
    }    
    
    public final String getAbstractSyntaxUID() {
        return asuid;
    }    
    
    public final List getTransferSyntaxUIDs() {
        return Collections.unmodifiableList(tsuids);
    }    

    public final String getTransferSyntaxUID() {
        return (String)tsuids.get(0);
    }

    private String resultAsString() {
        switch (result()) {
            case ACCEPTANCE:
                return "0 - acceptance";
            case USER_REJECTION:
                return "1 - user-rejection";
            case NO_REASON_GIVEN:
                return "2 - no-reason-given";
            case ABSTRACT_SYNTAX_NOT_SUPPORTED:
                return "3 - abstract-syntax-not-supported";
            case TRANSFER_SYNTAXES_NOT_SUPPORTED:
                return "4 - transfer-syntaxes-not-supported";
            default:
                return String.valueOf(result());
        }
    }
}
