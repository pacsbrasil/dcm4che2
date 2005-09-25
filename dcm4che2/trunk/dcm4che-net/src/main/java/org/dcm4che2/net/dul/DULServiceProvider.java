/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.dul;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.mina.protocol.ProtocolSession;
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

    public DULServiceProvider(DULServiceUser user, ProtocolSession session,
            boolean acceptor) {
        this.user = user;
        this.session = session;
        this.acceptor = acceptor;
        this.state = STA1;
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
    
    private synchronized void setState(State state) {
        this.state = state;
        if (log.isDebugEnabled())
            log.debug("Enter State: " + state);
        notifyAll();
    }

    private void startARTIM(long delay) {
        artimTask = new TimerTask() {        
            public void run() {
                artimExpired();        
            }        
        };
        artim.schedule(artimTask, delay);
    }

    private void stopARTIM() {
        if (artimTask != null) {
            artimTask.cancel();
            artimTask = null;
        }
    }
    
    void opened() {
        if (acceptor) {
            ae5();
        } else {
            ae1();           
        }
    }

    void received(PDU pdu) {
        state.received(pdu, this);        
    }


    void artimExpired() {
        state.artimExpired(this);
        
    }
    
    public void write(PDU pdu) {
        state.write(pdu, this);        
    }

    private void ae1() {
        setState(STA4);
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
        user.confirm(associateAC, this);        
    }

    /**
     * Issue A-ASSOCIATE confirmation (reject) primitive and close transport
     * connection. Next state is Sta1.
     * @param associateRJ
     */
    private void ae4(AAssociateRJ associateRJ) {
        setState(STA1);
        user.confirm(associateRJ, this);        
        session.close();
    }

    /**
     * Issue Transport connection response primitive already done;
     * start ARTIM timer. Next state is Sta2
     */
    private void ae5() {
        setState(STA2);
        startARTIM(associationRequestTimeout);
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
            user.indicate(associateRQ);
        }        
    }

    private AAssociateRJ acceptable(AAssociateRQ associateRQ) {
        if ((associateRQ.getProtocolVersion() & 1) == 0)
            return AAssociateRJ.protocolVersionNotSupported();
        else
            return null;
    }

    private void ae7(AAssociateAC associateAC) {
        // TODO Auto-generated method stub
        
    }

    private void ae8(AAssociateRJ associateRJ) {
        // TODO Auto-generated method stub
        
    }

    private void ar1(AReleaseRQ releaseRQ) {
        session.write(releaseRQ);
        setState(STA7);
    }

    private void ar3(AReleaseRP releaseRP) {
        setState(STA1);
        user.confirm(releaseRP, this);        
        session.close();
    }

    private void aa1() {
        // TODO Auto-generated method stub
        
    }

    private void aa2() {
        // TODO Auto-generated method stub
        
    }

    private void aa3(AAbort abort) {
        // TODO Auto-generated method stub
        
    }

    private void aa8() {
        // TODO Auto-generated method stub
        
    }

    private void dt1(PDataTF dataTF) {
        // TODO Auto-generated method stub
        
    }

    public static abstract class State {
        
        private final String name;
        
        protected State(String name) {
            this.name = name;
        }
        
        public String toString() {
            return name;
        }
        
        protected void write(PDU pdu, DULServiceProvider service) {
            throw new IllegalStateException(toString());        
        }
        
        protected abstract void received(PDU pdu, DULServiceProvider service);

        protected void artimExpired(DULServiceProvider service) {
            //NOOP
        }
    }
    
    private static class Sta1 extends State {

        Sta1() {
            super("Sta1 - Idle");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // should not happen!
            service.aa2();            
        }

    }

    private static class Sta2 extends State {

        Sta2() {
            super("Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU)");
        }

        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAssociateRQ)
                service.ae6((AAssociateRQ) pdu);
            else if (pdu instanceof AAbort)
                service.aa2();
            else
                service.aa1();
        }
        
        protected void artimExpired(DULServiceProvider service) {
            service.aa2();
        }

    }
    
    private static class Sta3 extends State {

        Sta3() {
            super("Sta4 - Transport connection open (Awaiting local ASSOCIATE request primitive)");
        }
        
        protected void write(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAssociateRQ)
                service.ae2((AAssociateRQ) pdu);
            else
                throw new IllegalStateException(toString());
            
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAbort)
                service.aa3((AAbort) pdu);
            else
                service.aa8();
        }
    }

    private static class Sta4 extends State {

        Sta4() {
            super("Sta4 - Transport connection open (Awaiting local ASSOCIATE request primitive)");
        }
        
        protected void write(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAssociateRQ)
                service.ae2((AAssociateRQ) pdu);
            else
                throw new IllegalStateException(toString());
            
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAbort)
                service.aa3((AAbort) pdu);
            else
                service.aa8();
        }
    }

    private static class Sta5 extends State {

        Sta5() {
            super("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU");
        }
        
        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAssociateAC)
                service.ae3((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                service.ae4((AAssociateRJ) pdu);
            else  if (pdu instanceof AAbort)
                service.aa3((AAbort) pdu);
            else
                service.aa8();
        }

    }

    private static class Sta6 extends State {

        Sta6() {
            super("Sta6 - Association established and ready for data transfer");
        }
                
        protected void write(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof PDataTF)
                service.dt1((PDataTF) pdu);
            else if (pdu instanceof AReleaseRQ)
                service.ar1((AReleaseRQ) pdu);
            else
                throw new IllegalStateException(toString());
            
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AAssociateAC)
                service.ae3((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                service.ae4((AAssociateRJ) pdu);
            else  if (pdu instanceof AAbort)
                service.aa3((AAbort) pdu);
            else
                service.aa8();
        }

    }

    private static class Sta7 extends State {

        Sta7() {
            super("Sta7 - Awaiting A-RELEASE-RP PDU");
        }
                
        protected void received(PDU pdu, DULServiceProvider service) {
            if (pdu instanceof AReleaseRP)
                service.ar3((AReleaseRP) pdu);
            else
                service.aa8();
        }
    }

    private static class Sta8 extends State {

        public Sta8() {
            super("Sta8 - Awaiting local A-RELEASE response primitive");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

    private static class Sta9 extends State {

        public Sta9() {
            super("Sta9 - Release collision requestor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

    private static class Sta10 extends State {

        public Sta10() {
            super("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

    private static class Sta11 extends State {

        public Sta11() {
            super("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

    private static class Sta12 extends State {

        public Sta12() {
            super("Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

    private static class Sta13 extends State {

        public Sta13() {
            super("Sta13 - Awaiting Transport Connection Close Indication");
        }

        @Override
        protected void received(PDU pdu, DULServiceProvider service) {
            // TODO Auto-generated method stub
            
        }

    }

}
