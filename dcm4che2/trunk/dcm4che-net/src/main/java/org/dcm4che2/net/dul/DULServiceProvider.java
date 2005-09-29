/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.dul;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.codec.DULProtocolViolationException;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PDataTF;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 20, 2005
 *
 */
public class DULServiceProvider {
    
    static final Logger log = Logger.getLogger(DULServiceProvider.class);
    
    public static final State STA1 = new Sta1();
    public static final State STA2 = new Sta2();
    public static final State STA3 = new Sta3();
    public static final State STA4 = new Sta4();
    public static final State STA5 = new Sta5();
    public static final State STA6 = new Sta6();
    public static final State STA7 = new Sta7();
    public static final State STA8 = new Sta8();
    public static final State STA9 = new Sta9();
    public static final State STA10 = new Sta10();
    public static final State STA11 = new Sta11();
    public static final State STA12 = new Sta12();
    public static final State STA13 = new Sta13();
    
    private static Timer artim = new Timer("ARTIM", true);
    private TimerTask artimTask = null;
    
    private final boolean acceptor;
    private final DULServiceUser user;
    private final ProtocolSession session;
    private State state;
    private long associationRequestTimeout = 10000L;
    private long socketCloseDelay = 100L;

    DULServiceProvider(DULServiceUser user, ProtocolSession session,
            boolean acceptor) {
        this.user = user;
        this.session = session;
        this.acceptor = acceptor;
        setState(acceptor ? STA2 : STA4);
    }

    public final boolean isAcceptor() {
        return acceptor;
    }
    
    public final long getAssociationRequestTimeout() {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(long associationRequestTimeout) {
        this.associationRequestTimeout = associationRequestTimeout;
    }

    public final long getSocketCloseDelay() {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(long socketCloseDelay) {
        this.socketCloseDelay = socketCloseDelay;
    }

    public final State getState() {
        return state;
    }

    public void write(PDU pdu) {
        if (log.isDebugEnabled()) {
            log.debug("Sending: " + pdu);
        }
        state.write(this, pdu);        
    }
    
    private synchronized void setState(State state) {
        if (this.state == state)
            return;
        
        this.state = state;
        if (log.isDebugEnabled())
            log.debug("Enter State: " + state);
        notifyAll();
    }

    private void startARTIM(long delay) {
        stopARTIM();
        if (log.isDebugEnabled()) {
            log.debug("Start ARTIM: " + (delay/1000f) + "s");
        }
        artimTask = new TimerTask() {        
            public void run() {
                artimExpired();        
            }        
        };
        artim.schedule(artimTask, delay);
    }

    private void stopARTIM() {
        if (artimTask != null) {
            if (log.isDebugEnabled()) {
                log.debug("Stop ARTIM");
            }
            artimTask.cancel();
            artimTask = null;
        }
    }
    
    void opened() {
        if (log.isDebugEnabled()) {
            log.debug("Opened: " + this);
        }
        user.onOpened(this);
        if (acceptor) {
            startARTIM(associationRequestTimeout);
        }
    }

    void received(PDU pdu) {
        if (log.isDebugEnabled()) {
            log.debug("Received: " + pdu);
        }
        state.received(this, pdu);        
    }

    void sent(PDU pdu) {
//        if (log.isDebugEnabled()) {
//            log.debug("Sent: " + pdu);
//        }
    }

    void artimExpired() {
        if (log.isDebugEnabled()) {
            log.debug("ARTIM expired: " + this);
        }
        state.artimExpired(this);
        
    }
    

    void closed() {
        if (log.isDebugEnabled()) {
            log.debug("Closed: " + this);
        }
        state.closed(this);
    }

    void exception(Throwable cause) {
        if (log.isDebugEnabled()) {
            log.debug("Exception: " + cause);
        }
        state.exception(this, cause);
    }

    void idle(IdleStatus status) {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Send A-ASSOCIATE-RQ-PDU. Next state is Sta5.
     * @param associateRQ
     */
    private void ae2(AAssociateRQ associateRQ) {
        session.write(associateRQ);
        setState(STA5);
    }

    /**
     * Issue A-ASSOCIATE confirmation (accept) primitive.
     * Next state is Sta6
     * @param associateAC
     */
    private void ae3(AAssociateAC associateAC) {
        setState(STA6);
        user.onAAssociateAC(this, associateAC);        
    }

    /**
     * Issue A-ASSOCIATE confirmation (reject) primitive and close transport
     * connection. Next state is Sta1.
     * @param associateRJ
     */
    private void ae4(AAssociateRJ associateRJ) {
        user.onAAssociateRJ(this, associateRJ);        
        session.close();
        setState(STA1);
    }

    /**
     * Stop ARTIM timer and if A-ASSOCIATE-RQ acceptable by service-provider:
     * - issue A-ASSOCIATE indication primitive
     *   Next state is Sta3
     * otherwise:
     * - issue A-ASSOCIATE-RJ-PDU and start ARTIM timer
     *   Next state is Sta13
     * @param associateRQ
     */
    private void ae6(AAssociateRQ associateRQ) {
        stopARTIM();
        AAssociateRJ rj = acceptable(associateRQ);
        if (rj != null) {
            session.write(rj);
            startARTIM(socketCloseDelay);
            setState(STA13);
        } else {
            setState(STA3);
            user.onAAssociateRQ(this, associateRQ);
        }        
    }

    private AAssociateRJ acceptable(AAssociateRQ associateRQ) {
        if ((associateRQ.getProtocolVersion() & 1) == 0)
            return AAssociateRJ.protocolVersionNotSupported();
        else
            return null;
    }

    /**
     * Send A-ASSOCIATE-AC PDU
     * Next state is Sta6
     * @param associateAC
     */
    private void ae7(AAssociateAC associateAC) {
        session.write(associateAC);
        setState(STA6);
    }

    /**
     * Send A-ASSOCIATE-RJ PDU and start ARTIM timer
     * Next state is Sta13
     * @param associateRJ
     */
    private void ae8(AAssociateRJ associateRJ) {
        session.write(associateRJ);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * Send P-DATA-TF PDU
     * Next state is Sta6
     * @param dataTF
     */
    private void dt1(PDataTF dataTF) {
        session.write(dataTF);
    }

    /**
     * Send P-DATA indication primitive
     * Next state is Sta6
     * @param dataTF
     */
    private void dt2(PDataTF dataTF) {
        onPDataTF(dataTF);
        setState(STA6);
    }

    /**
     * Send A-RELEASE-RQ PDU
     * Next state is Sta7
     * @param releaseRQ
     */
    private void ar1(AReleaseRQ releaseRQ) {
        session.write(releaseRQ);
        setState(STA7);
    }
    
    /**
     * Issue A-RELEASE indication primitive
     * Next state is Sta8
     * @param releaseRQ
     */
    private void ar2(AReleaseRQ releaseRQ) {
        setState(STA8);
        user.onAReleaseRQ(this, releaseRQ);        
    }

    /**
     * Issue A-RELEASE confirmation primitive, and close transport connection.
     * Next state is Sta1
     * @param releaseRP
     */
    private void ar3(AReleaseRP releaseRP) {
        setState(STA1);
        user.onAReleaseRP(this, releaseRP);        
        session.close();
    }

    /**
     * Issue A-RELEASE-RP PDU and start ARTIM timer.
     * Next state is Sta13
     */
    private void ar4(AReleaseRP releaseRP) {
        session.write(releaseRP);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * Stop ARTIM timer
     * Next state is Sta1
     */
    private void ar5() {
        stopARTIM();
        setState(STA1);
    }

    /**
     * Issue P-DATA indication
     * Next state is Sta7
     * @param dataTF
     */
    private void ar6(PDataTF dataTF) {
        onPDataTF(dataTF);        
        setState(STA7);
    }
    
    private void onPDataTF(PDataTF dataTF) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Issue P-DATA-TF PDU
     * Next state is Sta8
     * @param dataTF
     */
    private void ar7(PDataTF dataTF) {
        session.write(dataTF);
        setState(STA8);
    }

    /**
     * Issue A-RELEASE indication (release collision):
     * - if association-requestor, next state is Sta9
     * - if not, next state is Sta10
     * @param releaseRP
     */
    private void ar8(AReleaseRQ releaseRQ) {
        setState(acceptor ? STA10 : STA9);
        user.onAReleaseRQ(this, releaseRQ);        
    }
    
    /**
     * Send A-RELEASE-RP PDU
     * Next state is Sta11.
     */
    private void ar9(AReleaseRP releaseRP) {
        session.write(releaseRP);
        setState(STA11);
    }
    
    /**
     * Issue A-RELEASE confirmation primitive.
     * Next state is Sta12
     * @param releaseRP
     */
    private void ar10(AReleaseRP releaseRP) {
        setState(STA12);
        user.onAReleaseRP(this, releaseRP);        
    }
    
    /**
     * Send A-ABORT PDU (service-user source) and 
     * start (or restart if already started) ARTIM timer;
     * Next state is Sta13
     * @param abort
     */
    private void aa1(AAbort abort) {
        session.write(abort);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-user source) and 
     * start (or restart if already started) ARTIM timer; Next state is Sta13.
     * - otherwise stop ARTIM timer if running, Close transport connection;
     * Next state is Sta1.
     * @param cause
     */
    private void aa1(Throwable cause) {
        if (cause instanceof DULProtocolViolationException) {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa1(AAbort.fromServiceProvider(e.getReason()));                            
        } else if (cause instanceof IOException) {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else {
            aa1(AAbort.reasonNotSpecified());                            
        }
    }
    
    /**
     * Stop ARTIM timer if running. Close transport connection
     * Next state is Sta1
     */
    private void aa2() {
        stopARTIM();
        session.close();
        setState(STA1);
    }

    /**
     * If (service-user inititated abort)
     * - issue A-ABORT indication and close transport connection
     * otherwise (service-provider inititated abort):
     * - issue A-P-ABORT indication and close transport connection
     * Next state is Sta1
     * @param abort
     */
    private void aa3(AAbort abort) {
        user.onAbort(abort);
        session.close();
        setState(STA1);
    }

    /**
     * Issue A-P-ABORT indication primitive, Next state is Sta1.
     * @param abort
     */
    private void aa4(AAbort abort) {
        user.onAbort(abort);
        setState(STA1);
    }
    
    /**
     * Stop ARTIM timer, Next state is Sta1.
     */
    private void aa5() {
        stopARTIM();        
        setState(STA1);
    }
    
    /**
     * Ignore PDU, Next state is Sta13.
     */
    private void aa6() {
        setState(STA13);
    }

    /**
     * Send A-ABORT PDU, Next state is Sta13
     * @param abort
     */
    private void aa7(AAbort abort) {
        session.write(abort);
        setState(STA13);
    }
    
    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-user source);
     * Next state is Sta13.
     * Otherwise, stop ARTIM timer if running, Close transport connection;
     * Next state is Sta1.
     * @param cause
     */
    private void aa7(Throwable cause) {
        if (cause instanceof DULProtocolViolationException) {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa7(AAbort.fromServiceProvider(e.getReason()));                            
        } else if (cause instanceof IOException) {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else {
            aa7(AAbort.reasonNotSpecified());                            
        }
    }
    
    /**
     * Send A-ABORT PDU (service-provider source-), 
     * issue an A-P-ABORT indication, and start ARTIM timer;
     * Next state is Sta13
     * @param abort
     */
    private void aa8(AAbort abort) {
        session.write(abort);
        user.onAbort(abort);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }


    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-provider source-), 
     * issue an A-P-ABORT indication, and start ARTIM timer; Next state is Sta13.
     * Otherwise, issue A-P-ABORT indication primitive; Next state is Sta1.
     * @param cause
     */
    private void aa8(Throwable cause) {
        if (cause instanceof DULProtocolViolationException) {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa8(AAbort.fromServiceProvider(e.getReason()));                            
        } else if (cause instanceof IOException) {
            // do NOT try to send AAbort in case of I/O exception
            aa4(AAbort.reasonNotSpecified());
        } else {
            aa8(AAbort.reasonNotSpecified());                            
        }
    }
   
    public static abstract class State {
        
        protected final String name;
        
        protected State(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
        
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAbort)
                as.aa1((AAbort) pdu);
            else
                throw new IllegalStateException(name);
        }
        
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAbort)
                as.aa3((AAbort) pdu);
            else
                as.aa8(AAbort.unexpectedPDU());
        }

        protected void exception(DULServiceProvider as, Throwable cause) {
            as.aa8(cause);
        }

        protected void closed(DULServiceProvider as) {
            as.aa4(AAbort.reasonNotSpecified());
        }

        protected void artimExpired(DULServiceProvider as) {
            as.aa2();
        }
    }
    
    private static class Sta1 extends State {

        Sta1() {
            super("Sta1 - Idle");
        }

        @Override
        protected void write(DULServiceProvider as, PDU pdu) {
            throw new IllegalStateException(name);
        }        
    }

    private static class Sta2 extends State {

        Sta2() {
            super("Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU)");
        }

        @Override
        protected void write(DULServiceProvider as, PDU pdu) {
            throw new IllegalStateException(name);
        }
        
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAssociateRQ)
                as.ae6((AAssociateRQ) pdu);
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                as.aa1(AAbort.unexpectedPDU());
        }
        
        @Override
        protected void exception(DULServiceProvider provider, Throwable cause) {
            provider.aa1(cause);
        }

        @Override
        protected void closed(DULServiceProvider provider) {
            provider.aa5();            
        }

    }
    
    private static class Sta3 extends State {

        Sta3() {
            super("Sta3 - Awaiting local A-ASSOCIATE response primitive");
        }
        
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAssociateAC)
                as.ae7((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                as.ae8((AAssociateRJ) pdu);
            else 
                super.write(as, pdu);            
        }
    }

    private static class Sta4 extends State {

        Sta4() {
            super("Sta4 - Awaiting transport connection opening to complete.");
        }
        
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAssociateRQ)
                as.ae2((AAssociateRQ) pdu);
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                throw new IllegalStateException(name);            
        }
    }

    private static class Sta5 extends State {

        Sta5() {
            super("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU");
        }
        
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAssociateAC)
                as.ae3((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                as.ae4((AAssociateRJ) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta6 extends State {

        Sta6() {
            super("Sta6 - Association established and ready for data transfer");
        }
                
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof PDataTF)
                as.dt1((PDataTF) pdu);
            else if (pdu instanceof AReleaseRQ)
                as.ar1((AReleaseRQ) pdu);
            else
                super.write(as, pdu);
            
        }

        @Override
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof PDataTF)
                as.dt2((PDataTF) pdu);
            else if (pdu instanceof AReleaseRQ)
                as.ar2((AReleaseRQ) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta7 extends State {

        Sta7() {
            super("Sta7 - Awaiting A-RELEASE-RP PDU");
        }
                
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof PDataTF)
                as.ar6((PDataTF) pdu);
            else if (pdu instanceof AReleaseRP)
                as.ar3((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta8 extends State {

        public Sta8() {
            super("Sta8 - Awaiting local A-RELEASE response primitive");
        }

        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof PDataTF)
                as.ar7((PDataTF) pdu);
            else if (pdu instanceof AReleaseRP)
                as.ar4((AReleaseRP) pdu);
            else
                super.write(as, pdu);            
        }
    }

    private static class Sta9 extends State {

        public Sta9() {
            super("Sta9 - Release collision requestor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AReleaseRP)
                as.ar9((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta10 extends State {

        public Sta10() {
            super("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AReleaseRP)
                as.ar10((AReleaseRP) pdu);
            else
                super.received(as, pdu);        }
    }

    private static class Sta11 extends State {

        public Sta11() {
            super("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AReleaseRP)
                as.ar3((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta12 extends State {

        public Sta12() {
            super("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void write(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AReleaseRP)
                as.ar4((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta13 extends State {

        public Sta13() {
            super("Sta13 - Awaiting Transport Connection Close Indication");
        }

        @Override
        protected void received(DULServiceProvider as, PDU pdu) {
            if (pdu instanceof AAssociateRQ)
                as.aa7(AAbort.unexpectedPDU());
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                as.aa6();
        }

        @Override
        protected void write(DULServiceProvider as, PDU pdu) {
            throw new IllegalStateException(name);
        }

        @Override
        protected void exception(DULServiceProvider as, Throwable cause) {
            as.aa7(cause);            
        }

        @Override
        protected void closed(DULServiceProvider as) {
            as.ar5();            
        }
    }





}
