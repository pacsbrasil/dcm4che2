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
import org.dcm4che.data.*;

import java.io.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class DimseReaderImpl {
    
    private static final DcmObjectFactory dcmObjFact =
            DcmObjectFactory.getInstance();
    private final FsmImpl fsm;
    private int timeout = 0;
    
    private PDataTF pDataTF = null;
    private PDataTF.PDV pdv = null;
//    private PDataTFInputStream pDataTFin = null;

    /** Creates a new instance of DimseReader */
    public DimseReaderImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public synchronized Dimse read(int timeout) throws IOException {
        this.timeout = timeout;
        if (!nextPDV()) {
            return null;
        }
        if (!pdv.cmd()) {
            abort("Command PDV expected, but received " + pdv);
        }
        int pcid = pdv.pcid();
        String tsUID = fsm.getTransferSyntaxUID(pcid);
        if (tsUID == null) {
            abort("No Presentation Context negotiated with pcid:" + pcid);
        }
        InputStream in = new PDataTFInputStream(pdv.getInputStream());
        Command cmd = dcmObjFact.newCommand();
        boolean rq = false;
        boolean ds = false;
        try {
            cmd.read(in);
            rq = cmd.isRequest();
            ds = cmd.hasDataset();
        } catch (IllegalArgumentException e) { // very lousy Exception Handling 
            abort(e.getMessage());
        } catch (DcmValueException e) {
            abort(e.getMessage());
        } finally {
            in.close();
        }
        in = null;
        if (ds) {
            if (!nextPDV()) {
                throw new EOFException(
                        "Association released during receive of DIMSE");
            }
            if (pdv.cmd()) {
                abort("Data PDV expected, but received " + pdv);
            }
            if (pcid != pdv.pcid()) {
                abort("Mismatch between Command PDV pcid: " + pcid
                        + " and " + pdv);
            }
            in = new PDataTFInputStream(pdv.getInputStream());
        }
        Dimse retval = new DimseImpl(pcid, tsUID, cmd, in);
        if (!rq) {
            retval.getDataset();
        }
        return retval;
    }
    
    private InputStream nextStream() throws IOException {
        if (pdv != null && pdv.last()) {
            return null;
        }
        PDataTF.PDV prevPDV = pdv;        
        if (!nextPDV()) {
            throw new EOFException(
                    "Association released during receive of DIMSE");
        }
        return pdv.getInputStream();
    }
    
    private boolean nextPDV() throws IOException {
        PDataTF.PDV prevPDV = pdv;
        while (pDataTF == null || (pdv = pDataTF.readPDV()) == null) {
            if (!nextPDataTF()) {
                return false;
            }
        }
        if (prevPDV != null && !prevPDV.last()
                && (prevPDV.cmd() != pdv.cmd()
                || prevPDV.pcid() != pdv.pcid())) {
            abort("Mismatch of following PDVs: " + prevPDV + " -> " + pdv);
        }
        return true;
    }
    
    private void abort(String msg) throws IOException {
        AAbort aa = new AAbortImpl(AAbort.SERVICE_USER, 0);
        fsm.write(aa);
        throw new PDUException(msg, aa);
    }

    private boolean nextPDataTF() throws IOException {
        PDU pdu = fsm.read(timeout);
        if (pdu instanceof PDataTF) {
            pDataTF = (PDataTF)pdu;
            return true;
        }
        if (pdu instanceof AReleaseRQ) {
            fsm.write(AReleaseRPImpl.getInstance());
            return false;
        }
        throw new PDUException("Received " + pdu, (AAbort)pdu);
    }                

//    public InputStream openInputStream(int timeout) throws IOException {
//        if (pDataTFin != null) {
//            pDataTFin.close();
//        }
//        this.timeout = timeout;
//        return nextPDV()
//                ? new PDataTFInputStream(pdv.getInputStream())
//                : null;
//    }

    private class PDataTFInputStream extends InputStream {
        private InputStream in;

        PDataTFInputStream(InputStream in) {
            this.in = in;
        }

        public int available() throws IOException {
            if(in == null) {
                return 0; // no way to signal EOF from available()
            }
            return in.available();
        }
        
        public int read() throws IOException {
            if (in == null) {
                return -1;
            }
            int c = in.read();
            if (c == -1) {
                in = nextStream();
                return read();
            }
            return c;
        }
        
        public int read(byte b[], int off, int len) throws IOException {
            if (in == null) {
                return -1;
            } else if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0) ||
                       ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int n = in.read(b, off, len);
            if (n <= 0) {
                in = nextStream();
                return read(b, off, len);
            }
            return n;
        }
        
        public void close() throws IOException {
            while (in != null) {
                in = nextStream();
            }
        }
    }
}
