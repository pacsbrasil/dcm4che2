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

import org.dcm4che.net.AAbort;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRJ;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationListener;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDataTF;
import org.dcm4che.net.PDU;
import org.dcm4che.net.PDUException;
import org.dcm4che.net.PresContext;
import org.dcm4che.net.AReleaseRQ;
import org.dcm4che.net.AReleaseRP;
import org.dcm4che.net.AsyncOpsWindow;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.Socket;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class FsmImpl {    
    static final Logger log = Logger.getLogger("dcm4che.Association");

    private final AssociationImpl assoc;
    private final boolean requestor;
    private final Socket s;
    private final InputStream in;
    private final OutputStream out;
    private final static Timer timer = new Timer(true);
    private int tcpCloseTimeout = 500;
    private AAssociateRQ rq = null;
    private AAssociateAC ac = null;
    private AAssociateRJ rj = null;
    private AAbort aa = null;
    private AssociationListener assocListener = null;

    /** Creates a new instance of DcmULServiceImpl */
    public FsmImpl(AssociationImpl assoc, Socket s, boolean requestor)
    throws IOException {
        this.assoc = assoc;
        this.requestor = requestor;
        this.s = s;
        this.in = s.getInputStream();
        this.out = s.getOutputStream();
        changeState(requestor ? STA4 : STA2);
    }    
   
   public synchronized void addAssociationListener(AssociationListener l) {
     assocListener = Multicaster.add(assocListener, l);
   }

   public synchronized void removeAssociationListener(AssociationListener l) {
     assocListener = Multicaster.remove(assocListener, l);
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

    public int getState() {
        return state.getType();
    }
    
    final int getWriteMaxLength() {
        if (ac == null || rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return requestor ? ac.getMaxLength() : rq.getMaxLength();
    }
    
    final int getReadMaxLength() {
        if (ac == null || rq == null) {
            throw new IllegalStateException(state.toString());
        }
        return requestor ? rq.getMaxLength() : ac.getMaxLength();
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
        for (Iterator it = rq.listPresContext().iterator(); it.hasNext();) {
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
    
    int getMaxOpsInvoked() {    
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        AsyncOpsWindow aow = ac.getAsyncOpsWindow();
        if (aow == null)
           return 1;
        return requestor ? aow.getMaxOpsInvoked() : aow.getMaxOpsPerformed();
    }
    
    int getMaxOpsPerformed() {    
        if (ac == null) {
            throw new IllegalStateException(state.toString());
        }
        AsyncOpsWindow aow = ac.getAsyncOpsWindow();
        if (aow == null)
           return 1;
        return requestor ? aow.getMaxOpsPerformed() : aow.getMaxOpsInvoked();
    }
    
    private synchronized void changeState(State state) {
        if (this.state != state) {
            State prev = this.state;
            this.state = state;
            state.entry();
            if (log.isLoggable(Level.INFO)) {
                log.info("" + s.getInetAddress() + ": " + state);
            }
        }
    }

    public PDU read(int timeout, byte[] buf) throws IOException {
       try {
          synchronized (in) {
             s.setSoTimeout(timeout);
             UnparsedPDUImpl raw = null;
             try {
                raw = new UnparsedPDUImpl(in, buf);
             } catch (IOException e) {
                changeState(STA1);
                throw e;
             }
             return state.parse(raw);
          }
       } catch (IOException ioe) {
           if (assocListener != null) assocListener.error(assoc, ioe);
           throw ioe;
       }
    }
  
    public void write(AAssociateRQ rq) throws IOException {
       fireWrite(rq);
       try {
          synchronized (out) { state.write(rq); }
       } catch (IOException ioe) {
           if (assocListener != null) assocListener.error(assoc, ioe);
           throw ioe;
       }
       this.rq = rq;
    }
    
    public void write(AAssociateAC ac) throws IOException {
       fireWrite(ac);
       try {
          synchronized (out) { state.write(ac); }
       } catch (IOException ioe) {
           if (assocListener != null) assocListener.error(assoc, ioe);
           throw ioe;
       }
       this.ac = ac;
    }
    
    public void write(AAssociateRJ rj) throws IOException {
       fireWrite(rj);
       try {
          synchronized (out) { state.write(rj); }
       } catch (IOException ioe) {
          if (assocListener != null) assocListener.error(assoc, ioe);
          throw ioe;
       }
    }
    
    public void write(PDataTF data) throws IOException {
       fireWrite(data);
       try {
          synchronized (out) { state.write(data); }
       } catch (IOException ioe) {
          if (assocListener != null) assocListener.error(assoc, ioe);
          throw ioe;
       }
    }
    
    public void write(AReleaseRQ rq) throws IOException {
       fireWrite(rq);
       try {
          synchronized (out) { state.write(rq); }
       } catch (IOException ioe) {
          if (assocListener != null) assocListener.error(assoc, ioe);
          throw ioe;
       }
    }
    
    public void write(AReleaseRP rp) throws IOException {
       fireWrite(rp);
       try {
          synchronized (out) { state.write(rp); }
       } catch (IOException ioe) {
          if (assocListener != null) assocListener.error(assoc, ioe);
          throw ioe;
       }
    }
    
    public void write(AAbort abort) throws IOException {
       fireWrite(abort);
       try {
          synchronized (out) { state.write(abort); }
       } catch (IOException ioe) {
          if (assocListener != null) assocListener.error(assoc, ioe);
          throw ioe;
       }
    }
    
    void fireReceived(Dimse dimse) {
       if (log.isLoggable(Level.INFO)) {
          log.info("" + s.getInetAddress() + " >> " + dimse);
       }
       if (assocListener != null) assocListener.received(assoc, dimse);
    }
    
    void fireWrite(Dimse dimse) {
       if (log.isLoggable(Level.INFO)) {
          log.info("" + s.getInetAddress() + " << " + dimse);
       }
       if (assocListener != null) assocListener.write(assoc, dimse);
    }

    private void fireWrite(PDU pdu) {
       if (pdu instanceof PDataTF) {
          if (log.isLoggable(Level.FINE)) {
             log.fine("" + s.getInetAddress() + " << " + pdu);
          }
       } else {
          if (log.isLoggable(Level.INFO)) {
             log.info("" + s.getInetAddress() + " << " + pdu);
          }
       }
       if (assocListener != null) assocListener.write(assoc, pdu);       
    }

    private PDU fireReceived(PDU pdu) {
       if (pdu instanceof PDataTF) {
          if (log.isLoggable(Level.FINE)) {
             log.fine("" + s.getInetAddress() + " >> " + pdu);
          }
       } else {
          if (log.isLoggable(Level.INFO)) {
             log.info("" + s.getInetAddress() + " >> " + pdu);
          }
       }
       if (assocListener != null) assocListener.received(assoc, pdu);
       return pdu;
    }
    
    private abstract class State {
        
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
                    case 1: case 2: case 3:
                    case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
    
    private final State STA1 = new State(Association.IDLE) {
        public String toString() {
            return "Sta 1 - Idle";
        }
        void entry() {
            if (assocListener != null) assocListener.close(assoc);
            if (log.isLoggable(Level.INFO))
               log.info("" + s.getInetAddress() + ": Closing connection");
            try { in.close(); } catch (IOException ignore) {}
            try { out.close(); } catch (IOException ignore) {}
            try { s.close(); } catch (IOException ignore) {}
        }
        void write(AAbort abort) throws IOException {
        }
    };
    private State state = STA1;
    
    private final State STA2 =
            new State(Association.AWAITING_READ_ASS_RQ) {
        public String toString() {
            return "Sta 2 - Transport connection open"
                    + " (Awaiting A-ASSOCIATE-RQ PDU)";
        }

        PDU parse(UnparsedPDUImpl raw) throws PDUException {
            try {
                switch (raw.type()) {
                    case 1:
                        fireReceived(rq = AAssociateRQImpl.parse(raw));
                        changeState(STA3);
                        return rq;
                    case 2: case 3: case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.AWAITING_WRITE_ASS_RP) {
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
            new State(Association.AWAITING_WRITE_ASS_RQ) {
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
            new State(Association.AWAITING_READ_ASS_RP) {
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
                        fireReceived(ac = AAssociateACImpl.parse(raw));
                        changeState(STA6);
                        return ac;
                    case 3:
                        fireReceived(rj = AAssociateRJImpl.parse(raw));
                        changeState(STA13);
                        return rj;
                    case 4: case 5: case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.ASSOCIATION_ESTABLISHED) {
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
                        return fireReceived(PDataTFImpl.parse(raw));
                    case 5:
                        PDU pdu = fireReceived(AReleaseRQImpl.parse(raw));
                        changeState(STA8);
                        return pdu;
                    case 6:
                        throw new PDUException("Unexpected " + raw,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.UNEXPECTED_PDU));
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.AWAITING_READ_REL_RP) {
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
                        return fireReceived(PDataTFImpl.parse(raw));
                    case 5:
                        PDU pdu = fireReceived(AReleaseRQImpl.parse(raw));
                        changeState(requestor ? STA9 : STA10);
                        return pdu;
                    case 6:
                        fireReceived(pdu = AReleaseRPImpl.parse(raw));
                        changeState(STA1);
                        return pdu;
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.AWAITING_WRITE_REL_RP) {
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
            new State(Association.RCRS_AWAITING_WRITE_REL_RP) {
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
            new State(Association.RCAS_AWAITING_READ_REL_RP) {
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
                        PDU pdu = fireReceived(AReleaseRPImpl.parse(raw));
                        changeState(STA12);
                        return pdu;
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.RCRS_AWAITING_READ_REL_RP) {
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
                        PDU pdu = fireReceived(AReleaseRPImpl.parse(raw));
                        changeState(STA1);
                        return pdu;
                    case 7:
                        fireReceived(aa = AAbortImpl.parse(raw));
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
            new State(Association.RCAS_AWAITING_WRITE_REL_RP){
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
            new State(Association.ASSOCIATION_TERMINATING) {
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
