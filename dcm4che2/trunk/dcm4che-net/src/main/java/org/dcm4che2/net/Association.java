/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.TransferSyntax;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.codec.DULProtocolViolationException;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PDataTF;
import org.dcm4che2.net.pdu.PresentationContext;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 20, 2005
 */
public class Association
{

    private static final Logger log = Logger.getLogger(Association.class);
    private static final int DATA = 0;
    private static final int COMMAND = 1;
    private static final int PENDING = 0;
    private static final int LAST = 2;

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

    private final boolean requestor;
    private final Executor executor;
    private final AssociationHandler handler;
    private final ProtocolSession session;
    
    private State state;
    private long associationRequestTimeout = 10000L;
    private long socketCloseDelay = 100L;

    private PipedOutputStream pipedOut;
    private PipedInputStream pipedIn;
    
    private AAssociateRQ associateRQ;
    private AAssociateAC associateAC;
    private AAbort abort;
    private int pdvLen = 2;
    private int pdvPcid = -1;
    private int pdvMch = 0;
    private int limitOutgoingPDULength = 0x10000;
    private boolean packPDV = true;

    Association(Executor executor, AssociationHandler listener,
            boolean requestor, ProtocolSession session)
    {
        this.executor = executor;
        this.handler = listener;
        this.requestor = requestor;
        this.session = session;
        setState(requestor ? STA4 : STA2);
    }

    public final AssociationHandler getHandler()
    {
        return handler;
    }

    public final boolean isRequestor()
    {
        return requestor;
    }

    public final long getAssociationRequestTimeout()
    {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(
            long associationRequestTimeout)
    {
        this.associationRequestTimeout = associationRequestTimeout;
    }

    public final long getSocketCloseDelay()
    {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(long socketCloseDelay)
    {
        this.socketCloseDelay = socketCloseDelay;
    }

    public final State getState()
    {
        return state;
    }
    
    public int getMaxOutgoingPDULength() {
        try
        {
            return (requestor ? associateAC : associateRQ).getMaxPDULength() & ~1;
        } catch (NullPointerException e)
        {
            throw new IllegalStateException(state.toString());
        }
    }

    public void write(PDU pdu)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Sending: " + pdu);
        }
        state.write(this, pdu);
    }
    
    public void write(int pcid, DicomObject command, DataWriter dataWriter)
    throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Sending DIMSE[pcid=" + pcid + "]");
            log.debug(command);
        }
        PresentationContext pc;
        try
        {
            pc = associateAC.getPresentationContext(pcid);
        } catch (NullPointerException e)
        {
            throw new IllegalStateException(state.toString());
        }
        if (pc == null)
            throw new IllegalStateException("No Presentation State with id - " + pcid);
        if (!pc.isAccepted())
            throw new IllegalStateException("Presentation State not accepted - " + pc);
        
        int maxPduLen = getOutgoingPDULength();
        ByteBuffer buf = ByteBuffer.allocate(maxPduLen);
        PDVOutputStream out = new PDVOutputStream(pcid, COMMAND, buf, maxPduLen);
        DicomOutputStream dos = new DicomOutputStream(out);
        dos.writeCommand(command);
        dos.close();
        if (dataWriter != null) {
            if (!packPDV) 
                writePDataTF(buf);
            out = new PDVOutputStream(pcid, DATA, buf, maxPduLen);
            dataWriter.writeTo(out, TransferSyntax.valueOf(pc.getTransferSyntax()));
            out.close();            
        }
        writePDataTF(buf);
    }

    private void writePDataTF(ByteBuffer buf)
    {
        buf.flip();
        write(new PDataTF(buf));
        buf.clear();
    }
   
    private int getOutgoingPDULength()
    {
        int len = getMaxOutgoingPDULength();
        return (len > 0 && len < limitOutgoingPDULength) ? len 
                : limitOutgoingPDULength ;
    }

    private synchronized void setState(State state)
    {
        if (this.state == state)
            return;

        this.state = state;
        if (log.isDebugEnabled())
            log.debug("Enter State: " + state);
        notifyAll();
    }

    private void startARTIM(long delay)
    {
        stopARTIM();
        if (log.isDebugEnabled())
        {
            log.debug("Start ARTIM: " + (delay / 1000f) + "s");
        }
        artimTask = new TimerTask()
        {
            public void run()
            {
                artimExpired();
            }
        };
        artim.schedule(artimTask, delay);
    }

    private void stopARTIM()
    {
        if (artimTask != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Stop ARTIM");
            }
            artimTask.cancel();
            artimTask = null;
        }
    }

    void opened()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Opened: " + this);
        }
        handler.onOpened(this);
        if (!requestor)
        {
            startARTIM(associationRequestTimeout);
        }
    }

    void received(PDU pdu)
    throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Received: " + pdu);
        }
        state.received(this, pdu);
    }

    void sent(PDU pdu)
    {
//        if (log.isDebugEnabled())
//        {
//            log.debug("Sent: " + pdu);
//        }
    }

    void artimExpired()
    {
        if (log.isDebugEnabled())
        {
            log.debug("ARTIM expired: " + this);
        }
        state.artimExpired(this);

    }

    void closed() {
        if (log.isDebugEnabled())
        {
            log.debug("Closed: " + this);
        }
        state.closed(this);
        if (pipedOut != null)
            closePipedOut(); // wait until parsePDVs exits
        setState(STA1);
        handler.onClosed(this);
    }

    private synchronized void closePipedOut()
    {
        try
        {
            pipedOut.close();
            while (pipedOut != null) // wait until parsePDVs exits
                wait();
        } catch (Exception e)
        {
            log.warn("closing PDV stream throws " + e, e);
        }
    }

    void exception(Throwable cause)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Exception: " + cause);
        }
        state.exception(this, cause);
    }

    void idle(IdleStatus status)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Send A-ASSOCIATE-RQ-PDU. Next state is Sta5.
     * 
     * @param associateRQ
     */
    private void ae2(AAssociateRQ associateRQ)
    {
        this.associateRQ = associateRQ;
        session.write(associateRQ);
        setState(STA5);
    }

    /**
     * Issue A-ASSOCIATE confirmation (accept) primitive. Next state is Sta6
     * 
     * @param associateAC
     * @throws IOException
     */
    private void ae3(AAssociateAC associateAC)
    {
        this.associateAC = associateAC;
        setState(STA6);
        handler.onAAssociateAC(this, associateAC);
    }

    /**
     * Issue A-ASSOCIATE confirmation (reject) primitive and close transport
     * connection. Next state is Sta1.
     * 
     * @param associateRJ
     */
    private void ae4(AAssociateRJ associateRJ)
    {
        handler.onAAssociateRJ(this, associateRJ);
        session.close();
//        setState(STA1);
    }

    /**
     * Stop ARTIM timer and if A-ASSOCIATE-RQ acceptable by service-provider: -
     * issue A-ASSOCIATE indication primitive Next state is Sta3 otherwise: -
     * issue A-ASSOCIATE-RJ-PDU and start ARTIM timer Next state is Sta13
     * 
     * @param associateRQ
     * @throws IOException 
     */
    private void ae6(AAssociateRQ associateRQ)
    {
        this.associateRQ = associateRQ;
        stopARTIM();
        AAssociateRJ rj = acceptable(associateRQ);
        if (rj != null)
        {
            session.write(rj);
            startARTIM(socketCloseDelay);
            setState(STA13);
        } else
        {
            setState(STA3);
            handler.onAAssociateRQ(this, associateRQ);
        }
    }

    private AAssociateRJ acceptable(AAssociateRQ associateRQ)
    {
        if ((associateRQ.getProtocolVersion() & 1) == 0)
            return AAssociateRJ.protocolVersionNotSupported();
        else
            return null;
    }

    /**
     * Send A-ASSOCIATE-AC PDU Next state is Sta6
     * 
     * @param associateAC
     * @throws IOException
     */
    private void ae7(AAssociateAC associateAC)
    {
        this.associateAC = associateAC;
        session.write(associateAC);
        setState(STA6);
    }

    /**
     * Send A-ASSOCIATE-RJ PDU and start ARTIM timer Next state is Sta13
     * 
     * @param associateRJ
     */
    private void ae8(AAssociateRJ associateRJ)
    {
        session.write(associateRJ);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * Send P-DATA-TF PDU Next state is Sta6
     * 
     * @param dataTF
     */
    private void dt1(PDataTF dataTF)
    {
        session.write(dataTF);
    }

    /**
     * Send P-DATA indication primitive Next state is Sta6
     * 
     * @param dataTF
     */
    private void dt2(PDataTF dataTF)
    {
        onPDataTF(dataTF);
        setState(STA6);
    }

    /**
     * Send A-RELEASE-RQ PDU Next state is Sta7
     * 
     * @param releaseRQ
     */
    private void ar1(AReleaseRQ releaseRQ)
    {
        setState(STA7);
        session.write(releaseRQ);
    }

    /**
     * Issue A-RELEASE indication primitive Next state is Sta8
     * 
     * @param releaseRQ
     * @throws IOException 
     */
    private void ar2(AReleaseRQ releaseRQ)
    {
        setState(STA8);
        handler.onAReleaseRQ(this, releaseRQ);
    }

    /**
     * Issue A-RELEASE confirmation primitive, and close transport connection.
     * Next state is Sta1
     * 
     * @param releaseRP
     */
    private void ar3(AReleaseRP releaseRP)
    {
//        setState(STA1);
        handler.onAReleaseRP(this, releaseRP);
        session.close();
    }

    /**
     * Issue A-RELEASE-RP PDU and start ARTIM timer. Next state is Sta13
     */
    private void ar4(AReleaseRP releaseRP)
    {
        session.write(releaseRP);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * Stop ARTIM timer Next state is Sta1
     */
    private void ar5()
    {
        stopARTIM();
//        setState(STA1);
    }

    /**
     * Issue P-DATA indication Next state is Sta7
     * 
     * @param dataTF
     * @throws DULProtocolViolationException
     */
    private void ar6(PDataTF dataTF)
    {
        onPDataTF(dataTF);
        setState(STA7);
    }

    private void onPDataTF(PDataTF dataTF)
    {
        ByteBuffer buf = dataTF.getByteBuffer();
        java.nio.ByteBuffer nioBuf = buf.buf();
        int offset = nioBuf.position();
        int length = nioBuf.limit() - offset;
        if (!nioBuf.hasArray())
        {
            ByteBuffer heapBuf = ByteBuffer.allocate(length, false);
            heapBuf.put(buf);
            heapBuf.flip();
            nioBuf = heapBuf.buf();
            offset = 0;
        }
        try
        {
            if (pipedOut == null)
            {
                pipedOut = new PipedOutputStream();
                pipedIn = new PipedInputStream(pipedOut);
                executor.execute(new Runnable(){
                    public void run()
                    {
                        parsePDVs();                        
                    }});
            }
            pipedOut.write(nioBuf.array(), offset, length);
        } catch (IOException e)
        {
            log.warn("reading P-DATA-TF throws i/o exception", e);
            write(AAbort.reasonNotSpecified());
        }
    }
    
    /**
     * Issue P-DATA-TF PDU Next state is Sta8
     * 
     * @param dataTF
     */
    private void ar7(PDataTF dataTF)
    {
        session.write(dataTF);
        setState(STA8);
    }

    /**
     * Issue A-RELEASE indication (release collision): 
     * - if association-requestor, next state is Sta9
     * - if not, next state is Sta10
     * 
     * @param releaseRP
     * @throws IOException 
     */
    private void ar8(AReleaseRQ releaseRQ)
    throws IOException
    {
        setState(requestor ? STA9 : STA10);
        handler.onAReleaseRQ(this, releaseRQ);
    }

    /**
     * Send A-RELEASE-RP PDU Next state is Sta11.
     */
    private void ar9(AReleaseRP releaseRP)
    {
        session.write(releaseRP);
        setState(STA11);
    }

    /**
     * Issue A-RELEASE confirmation primitive. Next state is Sta12
     * 
     * @param releaseRP
     */
    private void ar10(AReleaseRP releaseRP)
    {
        setState(STA12);
        handler.onAReleaseRP(this, releaseRP);
    }

    /**
     * Send A-ABORT PDU (service-user source) and start (or restart if already
     * started) ARTIM timer; Next state is Sta13
     * 
     * @param abort
     */
    private void aa1(AAbort abort)
    {
        this.abort = abort;
        session.write(abort);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-user source) and
     * start (or restart if already started) ARTIM timer; Next state is Sta13. -
     * otherwise stop ARTIM timer if running, Close transport connection; Next
     * state is Sta1.
     * 
     * @param cause
     */
    private void aa1(Throwable cause)
    {
        if (cause instanceof DULProtocolViolationException)
        {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa1(AAbort.fromServiceProvider(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else
        {
            aa1(AAbort.reasonNotSpecified());
        }
    }

    /**
     * Stop ARTIM timer if running. Close transport connection Next state is
     * Sta1
     */
    private void aa2()
    {
        stopARTIM();
        session.close();
//        setState(STA1);
    }

    /**
     * If (service-user inititated abort) - issue A-ABORT indication and close
     * transport connection otherwise (service-provider inititated abort): -
     * issue A-P-ABORT indication and close transport connection Next state is
     * Sta1
     * 
     * @param abort
     */
    private void aa3(AAbort abort)
    {
        this.abort = abort;
        handler.onAbort(this, abort);
        session.close();
//        setState(STA1);
    }

    /**
     * Issue A-P-ABORT indication primitive, Next state is Sta1.
     * 
     * @param abort
     */
    private void aa4(AAbort abort)
    {
        this.abort = abort;
        handler.onAbort(this, abort);
//        setState(STA1);
    }

    /**
     * Stop ARTIM timer, Next state is Sta1.
     */
    private void aa5()
    {
        stopARTIM();
//        setState(STA1);
    }

    /**
     * Ignore PDU, Next state is Sta13.
     */
    private void aa6()
    {
        setState(STA13);
    }

    /**
     * Send A-ABORT PDU, Next state is Sta13
     * 
     * @param abort
     */
    private void aa7(AAbort abort)
    {
        this.abort = abort;
        session.write(abort);
        setState(STA13);
    }

    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-user source);
     * Next state is Sta13. Otherwise, stop ARTIM timer if running, Close
     * transport connection; Next state is Sta1.
     * 
     * @param cause
     */
    private void aa7(Throwable cause)
    {
        if (cause instanceof DULProtocolViolationException)
        {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa7(AAbort.fromServiceProvider(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else
        {
            aa7(AAbort.reasonNotSpecified());
        }
    }

    /**
     * Send A-ABORT PDU (service-provider source-), issue an A-P-ABORT
     * indication, and start ARTIM timer; Next state is Sta13
     * 
     * @param abort
     */
    private void aa8(AAbort abort)
    {
        this.abort = abort;
        session.write(abort);
        handler.onAbort(this, abort);
        startARTIM(socketCloseDelay);
        setState(STA13);
    }

    /**
     * If cause is no i/o exception, send A-ABORT PDU (service-provider source),
     * issue an A-P-ABORT indication, and start ARTIM timer; Next state is
     * Sta13. Otherwise, issue A-P-ABORT indication primitive; Next state is
     * Sta1.
     * 
     * @param cause
     */
    private void aa8(Throwable cause)
    {
        if (cause instanceof DULProtocolViolationException)
        {
            DULProtocolViolationException e = (DULProtocolViolationException) cause;
            aa8(AAbort.fromServiceProvider(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa4(AAbort.reasonNotSpecified());
        } else
        {
            aa8(AAbort.reasonNotSpecified());
        }
    }

    public static abstract class State
    {

        protected final String name;

        protected State(String name)
        {
            this.name = name;
        }

        public String toString()
        {
            return name;
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AAbort)
                as.aa1((AAbort) pdu);
            else
                throw new IllegalStateException(name);
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AAbort)
                as.aa3((AAbort) pdu);
            else
                as.aa8(AAbort.unexpectedPDU());
        }

        protected void exception(Association as, Throwable cause)
        {
            as.aa8(cause);
        }

        protected void closed(Association as)
        {
            as.aa4(AAbort.reasonNotSpecified());
        }

        protected void artimExpired(Association as)
        {
            as.aa2();
        }
    }

    private static class Sta1 extends State
    {

        Sta1()
        {
            super("Sta1 - Idle");
        }

        @Override
        protected void write(Association as, PDU pdu)
        {
            if (!(pdu instanceof AAbort))
                throw new IllegalStateException(name);
        }
    }

    private static class Sta2 extends State
    {

        Sta2()
        {
            super(
                    "Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU)");
        }

        @Override
        protected void write(Association as, PDU pdu)
        {
            throw new IllegalStateException(name);
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateRQ)
                as.ae6((AAssociateRQ) pdu);
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                as.aa1(AAbort.unexpectedPDU());
        }

        @Override
        protected void exception(Association provider, Throwable cause)
        {
            provider.aa1(cause);
        }

        @Override
        protected void closed(Association provider)
        {
            provider.aa5();
        }

    }

    private static class Sta3 extends State
    {

        Sta3()
        {
            super("Sta3 - Awaiting local A-ASSOCIATE response primitive");
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateAC)
                as.ae7((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                as.ae8((AAssociateRJ) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta4 extends State
    {

        Sta4()
        {
            super("Sta4 - Awaiting transport connection opening to complete.");
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateRQ)
                as.ae2((AAssociateRQ) pdu);
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                throw new IllegalStateException(name);
        }
    }

    private static class Sta5 extends State
    {

        Sta5()
        {
            super("Sta5 - Awaiting A-ASSOCIATE-AC or A-ASSOCIATE-RJ PDU");
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateAC)
                as.ae3((AAssociateAC) pdu);
            else if (pdu instanceof AAssociateRJ)
                as.ae4((AAssociateRJ) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta6 extends State
    {

        Sta6()
        {
            super("Sta6 - Association established and ready for data transfer");
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof PDataTF)
                as.dt1((PDataTF) pdu);
            else if (pdu instanceof AReleaseRQ)
                as.ar1((AReleaseRQ) pdu);
            else
                super.write(as, pdu);

        }

        @Override
        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof PDataTF)
                as.dt2((PDataTF) pdu);
            else if (pdu instanceof AReleaseRQ)
                as.ar2((AReleaseRQ) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta7 extends State
    {

        Sta7()
        {
            super("Sta7 - Awaiting A-RELEASE-RP PDU");
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof PDataTF)
                as.ar6((PDataTF) pdu);
            else if (pdu instanceof AReleaseRP)
                as.ar3((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta8 extends State
    {

        public Sta8()
        {
            super("Sta8 - Awaiting local A-RELEASE response primitive");
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof PDataTF)
                as.ar7((PDataTF) pdu);
            else if (pdu instanceof AReleaseRP)
                as.ar4((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta9 extends State
    {

        public Sta9()
        {
            super(
                    "Sta9 - Release collision requestor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar9((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta10 extends State
    {

        public Sta10()
        {
            super(
                    "Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar10((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta11 extends State
    {

        public Sta11()
        {
            super(
                    "Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU");
        }

        @Override
        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar3((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }
    }

    private static class Sta12 extends State
    {

        public Sta12()
        {
            super(
                    "Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive");
        }

        @Override
        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar4((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
    }

    private static class Sta13 extends State
    {

        public Sta13()
        {
            super("Sta13 - Awaiting Transport Connection Close Indication");
        }

        @Override
        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateRQ)
                as.aa7(AAbort.unexpectedPDU());
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                as.aa6();
        }

        @Override
        protected void write(Association as, PDU pdu)
        {
            if (!(pdu instanceof AAbort))
                throw new IllegalStateException(name);
        }

        @Override
        protected void exception(Association as, Throwable cause)
        {
            as.aa7(cause);
        }

        @Override
        protected void closed(Association as)
        {
            as.ar5();
        }
    }

    private static class PDVHeader
    {
        private final int length;
        private final int pcid;
        private final int msh;
        
        public PDVHeader(int length, int pcid, int msh)
        {
            this.length = length;
            this.pcid = pcid;
            this.msh = msh;
        }
        
        public final int getLength()
        {
            return length;
        }
        
        public final int getPCID()
        {
            return pcid;
        }
        
        public final boolean isCommand()
        {
            return (msh & COMMAND) != 0;
        }
        
        public final boolean isLast()
        {
            return (msh & LAST) != 0;
        }
        
    }
    
    void parsePDVs()
    {
        if (log.isDebugEnabled())
            log.debug("" + this + ": Enter parsePDVs");
        try
        {
            PDVHeader pdv;
            while ((pdv = readPDVHeader()) != null)
                nextDIMSE(pdv);
        } catch (DULProtocolViolationException e)
        {
            log.warn(e.getMessage(), e);
            write(AAbort.fromServiceProvider(e.getReason()));
        } catch (IOException e)
        {
           log.warn("Parsing PDVs throws i/o exception - ", e);
           write(AAbort.reasonNotSpecified());
        }
        if (log.isDebugEnabled())
            log.debug("" + this + ": Exit parsePDVs");
        synchronized (this)
        {
            pipedIn = null;
            pipedOut = null;
            notifyAll();
        }
    }

    public PDVHeader readPDVHeader() throws IOException
    {
        final int b1 = pipedIn.read();
        final int b2 = pipedIn.read();
        final int b3 = pipedIn.read();
        final int b4 = pipedIn.read();
        final int pcid = pipedIn.read();
        final int mch = pipedIn.read();
        if ((b1 | b2 | b3 | b4 | pcid | mch) < 0)
            return null;
        
        final int length = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        if (length < 2) {
            log.warn("Invalid PDV item length: " + length);
            write(AAbort.fromServiceProvider(AAbort.INVALID_PDU_PARAMETER_VALUE));
            return null;                
        }
        return new PDVHeader(length, pcid, mch);
    }
 
    private boolean nextDIMSE(PDVHeader pdv)
    throws DULProtocolViolationException, IOException
    {
        if (!pdv.isCommand())
            throw new DULProtocolViolationException(
                    AAbort.UNEXPECTED_PDU_PARAMETER,
                    "Expected Command PDV but received - " + pdv);
        
        final int pcid = pdv.getPCID();
        PresentationContext pc = associateAC.getPresentationContext(pcid);
        if (pc == null)
            throw new DULProtocolViolationException(
                    AAbort.UNEXPECTED_PDU_PARAMETER,
                    "No Presentation Context with given ID - " + pdv);
        
        if (!pc.isAccepted())
            throw new DULProtocolViolationException(
                    AAbort.UNEXPECTED_PDU_PARAMETER,
                    "No accepted Presentation Context with given ID - " + pdv);
        
        BasicDicomObject cmd = new BasicDicomObject();
        PDVInputStream inCmd = new PDVInputStream(pdv);
        DicomInputStream din = 
            new DicomInputStream(inCmd, TransferSyntax.ImplicitVRLittleEndian);
        din.readDicomObject(cmd, -1);
        if (log.isDebugEnabled()) {
            log.debug("Receiving DIMSE[pcid=" + pcid + "]");
            log.debug(cmd);
        }
        PDVInputStream inData = null;
        if (cmd.getInt(Tag.DataSetType) != 0x101) {
            PDVHeader pdv2 = readPDVHeader();
            if (pdv2.isCommand())
                throw new DULProtocolViolationException(
                        AAbort.UNEXPECTED_PDU_PARAMETER,
                        "Expected Data PDV but received - " + pdv2);
            
            if (pdv2.getPCID() != pcid)
                throw new DULProtocolViolationException(
                        AAbort.UNEXPECTED_PDU_PARAMETER,
                        "Expected pcid = " + pcid + " but received " + pdv2);
                
            inData = new PDVInputStream(pdv2);
        }
        handler.onDIMSE(this, pcid, cmd, inData);
        return true;
    }

    private class PDVInputStream extends InputStream
    {
        private PDVHeader pdv;
        private int available;

        public PDVInputStream(PDVHeader pdv)
        {
            this.pdv = pdv;
            this.available = pdv.getLength() - 2;
        }

        private boolean isEOF() throws IOException
        {
            while (available == 0) {
                if (!nextPDV())
                    return true;
            }
            return false;
        }
        
        private boolean nextPDV() throws IOException
        {
            if (pdv.isLast())
                return false;
            
            PDVHeader tmp = readPDVHeader();
            
            if (tmp == null) {
                log.warn("Unexpected end of PDV Input Stream");
                write(AAbort.reasonNotSpecified());
                return false;
            }
            
            if (tmp.getPCID() != pdv.getPCID()
                    || tmp.isCommand() != pdv.isCommand()) {
                log.warn("Unexpected " +  tmp + " - does not match previous " + pdv);
                write(AAbort.fromServiceProvider(AAbort.UNEXPECTED_PDU_PARAMETER));
                return false;
            }
            
            pdv = tmp;
            available = pdv.getLength() - 2;
            return true;
        }

        @Override
        public int read() throws IOException
        {
            if (isEOF())
                return -1;
            
            --available;
            return pipedIn.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (isEOF())
                return -1;
            
            final int read = pipedIn.read(b, off, Math.min(len, available));
            if (read > 0)
                available -= read;
            
            return read;
        }

        @Override
        public int available() throws IOException
        {
             return available;
        }

        @Override
        public long skip(long n) throws IOException
        {
            if (n <= 0 || isEOF())
                return 0;
            
            final long skipped = pipedIn.skip(Math.min(n, available));
            available -= skipped;
           
            return skipped;
        }

    }

    private void throwAbortException() throws IOException
    {
        if (abort != null)
        {
            throw new AbortException(abort);
        }
    }    
    
    private class PDVOutputStream extends OutputStream
    {
        private final int pcid;
        private final ByteBuffer buf;
        private final int maxPduLen;
        private int command;
        private int pdvPos;
        private int pdvLen;
        private int free;

        public PDVOutputStream(int pcid, int command, ByteBuffer buf, int maxPduLen)
        {
            this.pcid = pcid;
            this.command = command;
            this.buf = buf;
            this.maxPduLen = maxPduLen;
            init();
        }

        private void init()
        {
            pdvPos = buf.position();
            pdvLen = 2;
            free = maxPduLen - pdvPos - 6;
            buf.position(pdvPos + 6);
        }

        private void writePDVHeader(int last)
        {
            int prev = buf.position();
            buf.position(pdvPos);
            buf.put((byte) (pdvLen >> 24));
            buf.put((byte) (pdvLen >> 16));
            buf.put((byte) (pdvLen >> 8));
            buf.put((byte) pdvLen);
            buf.put((byte) pcid);
            buf.put((byte) (command | last));
            buf.position(prev);
        }

        @Override
        public void write(int b) throws IOException
        {
            throwAbortException();
            if (free == 0)
                flushPDataTF();
            buf.put((byte) b);
            --free;
            ++pdvLen;            
        }

        @Override
        public void write(byte[] b, int off, int len)
        throws IOException
        {
            throwAbortException();
            int pos = off;
            int remaining = len;
            while (remaining > 0)
            {
                if (free == 0)
                    flushPDataTF();
                int write = Math.min(remaining, free);
                buf.put(b, pos, write);
                pos += write;
                free -= write;
                pdvLen += write;
                remaining -= write;
            }
        }

        private void flushPDataTF() throws IOException
        {
            throwAbortException();
            writePDVHeader(PENDING);
            writePDataTF(buf);
            init();
        }

        @Override
        public void close() throws IOException
        {
            throwAbortException();
            writePDVHeader(LAST);
        }

    }

}
