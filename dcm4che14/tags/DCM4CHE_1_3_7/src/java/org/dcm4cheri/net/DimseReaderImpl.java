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

import org.dcm4cheri.util.LF_ThreadPool;

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
    private Command cmd = null;
    private byte[] buf = null;
    private LF_ThreadPool pool = null;

    /** Creates a new instance of DimseReader */
    public DimseReaderImpl(FsmImpl fsm) {
        this.fsm = fsm;
    }
    
    public void setThreadPool(LF_ThreadPool pool) {
       this.pool = pool;
    }
        
    public synchronized Dimse read(int timeout) throws IOException {
        this.timeout = timeout;
        try {
            if (!nextPDV()) {
                return null;
            }
        } catch (EOFException e) {
            FsmImpl.log.warn("Socket closed on open association:" + fsm.socket());
            return null;
        }
        if (!pdv.cmd()) {
            abort("Command PDV expected, but received " + pdv);
        }
        int pcid = pdv.pcid();
        String tsUID = fsm.getAcceptedTransferSyntaxUID(pcid);
        if (tsUID == null) {
            abort("No Presentation Context negotiated with pcid:" + pcid);
        }
        InputStream in = new PDataTFInputStream(pdv.getInputStream());
        cmd = dcmObjFact.newCommand();
        boolean ds = false;
        try {
            cmd.read(in);
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
        } else { // no Dataset
           // if no Data Fragment
            forkNextReadNext();
        }
        DimseImpl retval = new DimseImpl(pcid, tsUID, cmd, in);
        fsm.fireReceived(retval);
        return retval;
    }

    private void forkNextReadNext() {
       if (pool == null)
          return;
       if (cmd.isRequest()) {
          switch (cmd.getCommandField()) {
             case Command.C_GET_RQ:
             case Command.C_FIND_RQ:
             case Command.C_MOVE_RQ:
             case Command.C_CANCEL_RQ:
                break;
             default:
                // no need for extra thread in syncron mode
                if (fsm.getMaxOpsPerformed() == 1)
                   return;
          }
       }
       pool.promoteNewLeader();
    }

    private InputStream nextStream() throws IOException {
        if (pdv != null && pdv.last()) {
           // if last Data Fragment
           if (!pdv.cmd()) {
              forkNextReadNext();
           }
           return null;
        }
        if (!nextPDV()) {
            throw new EOFException(
                    "Association released during receive of DIMSE");
        }
        return pdv.getInputStream();
    }
    
    private boolean nextPDV() throws IOException {
        boolean hasPrev = pdv != null && !pdv.last();
        boolean prevCmd = hasPrev && pdv.cmd();
        int prevPcid = hasPrev ? pdv.pcid() : 0;
        while (pDataTF == null || (pdv = pDataTF.readPDV()) == null) {
            if (!nextPDataTF()) {
                return false;
            }
        }
        if (hasPrev && (prevCmd != pdv.cmd() || prevPcid != pdv.pcid())) {
            abort("Mismatch of following PDVs: " + pdv);
        }
        return true;
    }
    
    private void abort(String msg) throws IOException {
        AAbort aa = new AAbortImpl(AAbort.SERVICE_USER, 0);
        fsm.write(aa);
        throw new PDUException(msg, aa);
    }

    private boolean nextPDataTF() throws IOException {
        if (buf == null) {
            buf = new byte[fsm.getReadMaxLength() + 6];
        }
        PDU pdu = fsm.read(timeout, buf);
        if (pdu instanceof PDataTF) {
            pDataTF = (PDataTF)pdu;
            return true;
        }
        if (pdu instanceof AReleaseRP) {
            return false;
        }
        if (pdu instanceof AReleaseRQ) {
            fsm.write(AReleaseRPImpl.getInstance());
            return false;
        }
        throw new PDUException("Received " + pdu, (AAbort)pdu);
    }                

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
