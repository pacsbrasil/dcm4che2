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

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class DimseWriterImpl {

    private final FsmImpl fsm;
    private PDataTFImpl pDataTF = null;
    private int pcid;
    private boolean cmd;
    
//    PDataTFOutputStream pDataTFout = null;

    /** Creates a new instance of PDataTFWriteAdapter */
    public DimseWriterImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public synchronized void write(Dimse dimse) throws IOException {
        int pcid = dimse.pcid();
        String tsUID = fsm.getTransferSyntaxUID(pcid);
        if (tsUID == null) {
            throw new IllegalStateException();
        }
        if (pDataTF == null) {
            pDataTF = new PDataTFImpl(fsm.getMaxLength());
        }
        pDataTF.openPDV(pcid, cmd = true);
        OutputStream out = new PDataTFOutputStream();
        Command c = dimse.getCommand();
        try {
            c.write(out);
        } finally {
            out.close();
        }
        if (c.hasDataset()) {
            pDataTF.openPDV(pcid, cmd = false);
            out = new PDataTFOutputStream();
            try {
                dimse.writeTo(out, tsUID);
            } finally {
                out.close();
            }                
        }
        flush();
    }
    
    public void flush() throws IOException {
        boolean open = pDataTF.isOpenPDV();
        if (open) {
            pDataTF.closePDV(false);
        }
        if (!pDataTF.isEmpty()) {
            fsm.write(pDataTF);
        }
        pDataTF = new PDataTFImpl(fsm.getMaxLength()); 
        if (open) {
            pDataTF.openPDV(pcid, cmd);
        }
    }
        
    private void closeStream() throws IOException {
        pDataTF.closePDV(true);
        if (!cmd) {
            flush();
        }
    }

    private class PDataTFOutputStream extends OutputStream {
        public synchronized void write(int b) throws IOException {
            if (pDataTF.free() == 0) {
                flush();
            }
            pDataTF.write(b);
        }
        public synchronized void write(byte b[], int off, int len)
                throws IOException {
            if (len == 0) {
                return;
            }
            int n = 0;
            for (;;) {
                int c = Math.min(pDataTF.free(), len - n);
                pDataTF.write(b, off + n, c);
                n += c;
                if (n == len) {
                    return;
                }
                flush();
            }
        }
        public synchronized void close() throws IOException {
            closeStream();
        }
    }
}
