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

import org.dcm4che.net.PresContext;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class PresContextImpl implements PresContext {

    private final byte pcid;
    private final int result;
    private final String asuid;
    private final List tsuids;

    PresContextImpl(byte pcid, int result, String asuid, String[] tsuids) {
        this.pcid = pcid;
        this.result = result;
        this.asuid = asuid;
        this.tsuids = new ArrayList(Arrays.asList(tsuids));
    }
    
    PresContextImpl(DataInputStream din, int len) throws IOException {
        this.pcid = din.readByte();
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
                    if (asuid != null) {
                        throw new IOException(
                                "More than one Abstract Syntax sub-item in"
                                + " Presentation Context");
                    }
                    asuid = AAssociateRQACImpl.readASCII(din, uidlen);
                    break;
                case 0x40:
                    tsuids.add(AAssociateRQACImpl.readASCII(din, uidlen));
                    break;
                default:
                    throw new IOException(
                            "Illegal sub-item type=" + uidtype  + ", len= "
                            + uidlen + " in Presentation Context");
            }
            remain -= 4 + uidlen;
        }
        this.asuid = asuid;
        if (remain < 0) {
            throw new IOException("Presentation item length: " + len
                + " mismatch length of sub-items");
        }
    }
    
    void writeTo(int type, DataOutputStream dout) throws IOException {
        dout.write(type);
        dout.write(0);
        dout.writeShort(length());
        dout.write(getID());
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

    public final byte getID() {
        return pcid;
    }    
    
    public final int getResult() {
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
}
