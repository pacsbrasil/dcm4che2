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
import java.util.logging.*;
import java.net.Socket;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class FsmImpl {
    private static final String CLASSNAME = "org.dcm4cheri.net.FsmImpl";
    private static final Logger log = Logger.getLogger(CLASSNAME);
    
    private final AssociationImpl assoc;
    private final boolean requestor;
    private final Socket s;
    private final InputStream in;
    private final OutputStream out;
    private final AssociationListener listener;
    private final static Timer timer = new Timer(true);
    private int tcpCloseTimeout = 500;
    private AAssociateRQ rq = null;
    private AAssociateAC ac = null;
    private AAssociateRJ rj = null;
    private AAbort aa = null;

    /** Creates a new instance of DcmULServiceImpl */
    public FsmImpl(AssociationImpl assoc, Socket s, boolean requestor, AssociationListener listener) throws IOException {
        this.assoc = assoc;
        this.requestor = requestor;
        this.s = s;
        this.in = s.getInputStream();
        this.out = s.getOutputStream();
        this.listener = listener;
        changeState(requestor ? STA4 : STA2);
    }    
   
    final Socket socket() {
        return s;
    }

    final boolean isRequestor() {
        return requestor;
    }

    final void setTCPCloseTimeout(int tcpCloseTimeout) {
        if (tcpCloseTimeout < 0) {
            throw new IllegalArgumentException(
                    "tcpCloseTimeout:" + tcpCloseTimeout);
        }
        this.tcpCloseTimeout = tcpCloseTimeout;
    }
    
    final int getTCPCloseTimeout() {
        return tcpCloseTimeout;
    }

    public AssociationState getState() {
        return state;
    }
    
    final int getMaxLength() {
        if (ac == null || rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return requestor ? ac.getMaxLength() : rq.getMaxLength();
    }
    
    final String getAcceptedTransferSyntaxUID(int pcid) {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        PresContext pc = ac.getPresContext(pcid);
        if (pc == null || pc.result() != PresContext.ACCEPTANCE) {
            return null;
        }
        return pc.getTransferSyntaxUID();
    }
    
    final PresContext getAcceptedPresContext(String asuid, String tsuid) {
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        for (Iterator it = rq.iteratePresContext(); it.hasNext();) {
            PresContext rqpc = (PresContext)it.next();
            if (asuid.equals(rqpc.getAbstractSyntaxUID())) {
                PresContext acpc = ac.getPresContext(rqpc.pcid());
                if (acpc != null && acpc.result() == PresContext.ACCEPTANCE
                        && tsuid.equals(acpc.getTransferSyntaxUID())) {
                    return acpc;
                }
            }
        }
        return null;
    }
    
    private synchronized void changeState(State state) {
        if (this.state != state) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("Enter " + state.toString());
            }
            State prev = this.state;
            this.state = state;
            if (listener != null) {
                listener.stateChanged(
                        new AssociationEvent(assoc, prev, state));
            }
            state.entry();
        }
    }

    public PDU read(int timeout) throws PDUException, IOException {
        synchronized (in) {
            s.setSoTimeout(timeout);
            UnparsedPDUImpl raw = null;
            try {
                raw = new UnparsedPDUImpl(in);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            return state.parse(raw);
        }
     }
  
    public void write(AAssociateRQ rq) throws IOException {
        synchronized (out) { state.write(rq); }
        this.rq = rq;
    }
    
    public void write(AAssociateAC ac) throws IOException {
        synchronized (out) { state.write(ac); }
        this.ac = ac;
    }
    
    public void write(AAssociateRJ rj) throws IOException {
        synchronized (out) { state.write(rj); }
    }
    
    public void write(PDataTF data) throws IOException {
        synchronized (out) { state.write(data); }
    }
    
    public void write(AReleaseRQ rq) throws IOException {
        synchronized (out) { state.write(rq); }
    }
    
    public void write(AReleaseRP rp) throws IOException {
        synchronized (out) { state.write(rp); }
    }
    
    public void write(AAbort abort) throws IOException {
        synchronized (out) { state.write(abort); }
    }
    
    private abstract class State implements AssociationState {
        
        private final int type;
        
        State(int type) {
            this.type = type;
        }
        
        public final int getType() {
            return type;
        }

        public boolean isOpen() {
            return false;
        }

        public boolean canWritePDataTF() {
            return false;
        }

        public boolean canReadPDataTF() {
            return false;
        }

        void entry() {
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1: case 2: case 3: case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }

        void write(AAssociateRQ rq) throws IOException {
            throw new IllegalStateException();
        }

        void write(AAssociateAC ac) throws IOException {
            throw new IllegalStateException();
        }

        void write(AAssociateRJ rj) throws IOException {
            throw new IllegalStateException();
        }

        void write(PDataTF data) throws IOException {
            throw new IllegalStateException();
        }

        void write(AReleaseRQ rq) throws IOException {
            throw new IllegalStateException();
        }

        void write(AReleaseRP rp) throws IOException {
            throw new IllegalStateException();
        }

        void write(AAbort abort) throws IOException {
            try {
                abort.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA13);
        }

    }
    
    private final State STA1 = new State(AssociationState.IDLE) {
        public String toString() {
            return "Sta 1 - Idle";
        }
        void entry() {
            try { s.close(); } catch (IOException ignore) {}
        }
        void write(AAbort abort) throws IOException {
        }
    };
    private State state = STA1;
    
    private final State STA2 =
            new State(AssociationState.AWAITING_READ_ASS_RQ) {
        public String toString() {
            return "Sta 2 - Transport connection open"
                    + " (Awaiting A-ASSOCIATE-RQ PDU)";
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1:
                        rq = AAssociateRQImpl.parse(raw);
                        changeState(STA3);
                        return rq;
                    case 2: case 3: case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }
    };
    
    private final State STA3 =
            new State(AssociationState.AWAITING_WRITE_ASS_RP) {
        public String toString() {
            return "Sta 3 - Awaiting local A-ASSOCIATE response primitive";
        }

        void write(AAssociateAC ac) throws IOException {
            try {
                ac.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA6);
        }
        
        void write(AAssociateRJ rj) throws IOException {
            try {
                rj.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA13);
        }
    };
    
    private final State STA4 =
            new State(AssociationState.AWAITING_WRITE_ASS_RQ) {
        public String toString() {
            return "Sta 4 - Awaiting transport connection opening to complete";
        }

        void write(AAssociateRQ rq) throws IOException {
            try {
                rq.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA5);
        }

        void write(AAbort abort) throws IOException {
            changeState(STA1);
        }
    };
    
    private final State STA5 =
            new State(AssociationState.AWAITING_READ_ASS_RP) {
        public String toString() {
            return "Sta 5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU";
        }
        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 2: 
                        ac = AAssociateACImpl.parse(raw);
                        changeState(STA6);
                        return ac;
                    case 3:
                        rj = AAssociateRJImpl.parse(raw);
                        changeState(STA13);
                        return rj;
                    case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }
    };

    private final State STA6 =
            new State(AssociationState.ASSOCIATION_ESTABLISHED) {
        public String toString() {
            return "Sta 6 - Association established and ready for data transfer";
        }

        public boolean isOpen() {
            return true;
        }

        public boolean canWritePDataTF() {
            return true;
        }

        public boolean canReadPDataTF() {
            return true;
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1: case 2: case 3:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 4: 
                        return PDataTFImpl.parse(raw);
                    case 5:
                        PDU pdu = AReleaseRQImpl.parse(raw);
                        changeState(STA8);
                        return pdu;
                    case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }

        void write(PDataTF tf) throws IOException {
            try {
                tf.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
        }

        void write(AReleaseRQ rq) throws IOException {
            try {
                rq.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA7);
        }
    };

    private final State STA7 =
            new State(AssociationState.AWAITING_READ_REL_RP) {
        public String toString() {
            return "Sta 7 - Awaiting A-RELEASE-RP PDU";
        }

        public boolean canReadPDataTF() {
            return true;
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1: case 2: case 3:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 4: 
                        return PDataTFImpl.parse(raw);
                    case 5:
                        PDU pdu = AReleaseRQImpl.parse(raw);
                        changeState(requestor ? STA9 : STA10);
                        return pdu;
                    case 6:
                        pdu = AReleaseRPImpl.parse(raw);
                        changeState(STA1);
                        return pdu;
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }
    };

    private final State STA8 =
            new State(AssociationState.AWAITING_WRITE_REL_RP) {
        public String toString() {
            return "Sta 8 - Awaiting local A-RELEASE response primitive";
        }

        public boolean canWritePDataTF() {
            return true;
        }

        void write(PDataTF tf) throws IOException {
            try {
                tf.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
        }

        void write(AReleaseRP rp) throws IOException {
            try {
                rp.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA13);
        }
    };

    private final State STA9 =
            new State(AssociationState.RCRS_AWAITING_WRITE_REL_RP) {
        public String toString() {
            return "Sta 9 - Release collision requestor side;"
                    + " awaiting A-RELEASE response";
        }

        void write(AReleaseRP rp) throws IOException {
            try {
                rp.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA11);
        }
    };

    private final State STA10 =
            new State(AssociationState.RCAS_AWAITING_READ_REL_RP) {
        public String toString() {
            return "Sta 10 - Release collision acceptor side;"
                    + " awaiting A-RELEASE response";
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1: case 2: case 3: case 4: case 5:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 6:
                        PDU pdu = AReleaseRPImpl.parse(raw);
                        changeState(STA12);
                        return pdu;
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }
    };

    private final State STA11 =
            new State(AssociationState.RCRS_AWAITING_READ_REL_RP) {
        public String toString() {
            return "Sta 11 - Release collision requestor side;"
                    + " awaiting A-RELEASE-RP PDU";
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1: case 2: case 3: case 4: case 5:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 6:
                        PDU pdu = AReleaseRPImpl.parse(raw);
                        changeState(STA1);
                        return pdu;
                    case 7:
                        aa = AAbortImpl.parse(raw);
                        changeState(STA1);
                        return aa;
                    default:
                        throw new PDUException("Unrecognized " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNRECOGNIZED_PDU));
                }
            } catch (PDUException ule) {
                try { write(ule.getAAbort()); } catch (Exception ignore) {};
                throw ule;
            }
        }
     };

    private final State STA12 =
            new State(AssociationState.RCAS_AWAITING_WRITE_REL_RP){
        public String toString() {
            return "Sta 12 - Release collision acceptor side;"
                    + " awaiting A-RELEASE-RP PDU";
        }
        void write(AReleaseRP rp) throws IOException {
            try {
                rp.writeTo(out);
            } catch (IOException e) {
                changeState(STA1);
                throw e;
            }
            changeState(STA13);
        }
    };

    private final State STA13 =
            new State(AssociationState.ASSOCIATION_TERMINATING) {
        public String toString() {
            return "Sta 13 - Awaiting Transport Connection Close Indication";
        }
        void entry() {
            timer.schedule(
                    new TimerTask() {
                        public void run() { 
                            changeState(STA1); 
                        }
                    },
                    tcpCloseTimeout);                    
        }
    };

}
