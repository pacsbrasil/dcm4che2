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

package org.dcm4cheri.data;

import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmHandler;
import org.dcm4che.dict.VRs;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;

/** Defines behavior of <code>Command</code> container objects.
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 * @see "DICOM Part 7: Message Exchange, 6.3.1 Command Set Structure"
 */
final class CommandImpl extends DcmObjectImpl
        implements org.dcm4che.data.Command {
    
    protected DcmElement set(DcmElement newElem) {
        if ((newElem.tag() & 0xFFFF0000) != 0x00000000)
            throw new IllegalArgumentException(newElem.toString());
        
        if (newElem.getByteBuffer().order() != ByteOrder.LITTLE_ENDIAN)
            throw new IllegalArgumentException(
                    newElem.getByteBuffer().toString());

        return super.set(newElem);
    }
    
    public int length() {
        return grLen() + 12;
    }
    
    private int grLen() {
        int len = 0;
        for (int i = 0, n = list.size(); i < n; ++i)
            len += ((DcmElement)list.get(i)).length() + 8;

        return len;
    }
    
    public void write(DcmHandler handler) throws IOException {
        handler.setDcmDecodeParam(DcmDecodeParam.IVR_LE);
        write(0x00000000, grLen(), handler);
    }

    public void write(OutputStream out) throws IOException {
        write(new DcmStreamHandlerImpl(out));
    }
}

