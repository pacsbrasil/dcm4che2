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

import org.dcm4che.net.UnparsedPDU;
import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class UnparsedPDUImpl implements UnparsedPDU {
    
    static final int MAX_LENGTH = 1048576; // 1 MB
    private final byte[] buf;
    private final int len;
    
    /** Creates a new instance of RawPDU */
    public UnparsedPDUImpl(InputStream in) throws IOException {
        byte[] h = new byte[6];
        readFully(in, h, 0, 6);
        this.len = ((h[2] & 0xff) << 24)
                | ((h[3] & 0xff) << 16)
                | ((h[4] & 0xff) << 8)
                | ((h[5] & 0xff) << 0);
        if (len > MAX_LENGTH) {
            throw new IOException("PDU length exceeds supported maximum: "
                    + len);
        }
        this.buf = new byte[6 + len];
        System.arraycopy(h, 0, buf, 0, 6);
        readFully(in, buf, 6, len);
    }

    public final int type() {
        return buf[0] & 0xFF;
    }

    public final int length() {
        return len;
    }

    public final byte[] buffer() {
        return buf;
    }
    
    public String toString() {
        return "PDU[type=" + (buf[0] & 0xFF)
                + ", length=" + (len & 0xFFFFFFFFL)
                + "]";
    }

    static void readFully(InputStream in, byte b[], int off, int len)
            throws IOException {
	int n = 0;
	while (n < len) {
	    int count = in.read(b, n + off, len - n);
	    if (count < 0)
		throw new EOFException();
	    n += count;
	}
    }
}
