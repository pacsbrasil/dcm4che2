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

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class AAssociateRJImpl implements AAssociateRJ {

    private final byte[] buf;
    
    AAssociateRJImpl(UnparsedPDU raw) throws PDUParseException {
        if (raw.length() != 4) {
            throw new PDUParseException("Illegal PDU : " + raw);
        }
        this.buf = raw.buffer();
    }

    AAssociateRJImpl(int result, int source, int reason) {
        this.buf = new byte[]{ 3, 0, 0, 0, 0, 4, 0,
                (byte)result,
                (byte)source,
                (byte)reason
        };
    }

    public final int getResult() {
        return buf[7] & 0xff;
    }
    
    public final int getSource() {
        return buf[8] & 0xff;
    }
    
    public final int getReason() {
        return buf[9] & 0xff;
    }
    
    public void writeTo(OutputStream out) throws IOException {
        out.write(buf);
        out.flush();
    }
}
