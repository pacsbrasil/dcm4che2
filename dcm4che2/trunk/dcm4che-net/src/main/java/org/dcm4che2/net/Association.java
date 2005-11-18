/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

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
import org.dcm4che2.util.IntHashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 20, 2005
 */
public class Association
{

    private static final Logger log = LoggerFactory.getLogger(Association.class);
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

    private static Timer artim = new Timer(true);

    private TimerTask artimTask = null;

    private final boolean requestor;
    private final Executor executor;
    private AssociationHandler handler;
    private final ProtocolSession session;
    
    private State state;
    private long associationRequestTimeout = 0;
    private long associationAcceptTimeout = 0;
    private long releaseResponseTimeout = 0;
    private long socketCloseDelay = 100L;
    private boolean packPDV = true;

    private ByteBufferChannel readDataChannel = new ByteBufferChannel();
    private Thread readDataThread;
    
    private AAssociateRQ associateRQ;
    private AAssociateAC associateAC;
    private AAssociateRJ associateRJ;
    private AAbort abort;
    private int messageID = 0;
    private IntHashtable rspHandlerForMsgId = new IntHashtable();
    private IntHashtable cancelHandlerForMsgId = new IntHashtable();
    
    private int maxSendPDULength = 0x100000;
    private int sendPDULength;
    private int maxOpsInvoked;

    Association(Executor executor, AssociationHandler handler,
            boolean requestor, ProtocolSession session)
    {
        this.executor = executor;
        this.handler = handler;
        this.requestor = requestor;
        this.session = session;
        setState(requestor ? STA4 : STA2);
    }

    public final AssociationHandler getHandler()
    {
        return handler;
    }

    public final InetSocketAddress getRemoteAddress()
    {
        return (InetSocketAddress) session.getRemoteAddress();
    }
    
    public void addAssociationHandlerFilter(AssociationHandlerFilter af)
    {
        if (af.getHandler() != handler)
            throw new IllegalArgumentException(
                    "Filter does not match current Association Handler");
        this.handler = af;        
    }

    public final boolean isRequestor()
    {
        return requestor;
    }

    public final long getAssociationRequestTimeout()
    {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(long timeout)
    {
        this.associationRequestTimeout = timeout;
    }

    public final long getAssociationAcceptTimeout()
    {
        return associationAcceptTimeout;
    }

    public final void setAssociationAcceptTimeout(long timeout)
    {
        this.associationAcceptTimeout = timeout;
    }

    public final long getReleaseResponseTimeout()
    {
        return releaseResponseTimeout;
    }

    public final void setReleaseResponseTimeout(long releaseResponseTimeout)
    {
        this.releaseResponseTimeout = releaseResponseTimeout;
    }

    public final long getSocketCloseDelay()
    {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(long socketCloseDelay)
    {
        this.socketCloseDelay = socketCloseDelay;
    }

    public final int getMaxSendPDULength()
    {
        return maxSendPDULength;
    }

    public final void setMaxSendPDULength(int bufferSize)
    {
        this.maxSendPDULength = bufferSize;
    }

    public final boolean isPackPDV()
    {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    public final State getState()
    {
        return state;
    }
    
    public final AAbort getAbort()
    {
        return abort;
    }
    
    public final AAssociateRJ getAssociateRJ()
    {
        return associateRJ;
    }
    
    public final AAssociateAC getAssociateAC()
    {
        return associateAC;
    }

    public final AAssociateRQ getAssociateRQ()
    {
        return associateRQ;
    }

    public String getTransferSyntax(int pcid)
    {
        try
        {
            PresentationContext pc = associateAC.getPresentationContext(pcid);
            return pc.isAccepted() ? pc.getTransferSyntax() : null;
        } catch (NullPointerException e)
        {
            throw new IllegalStateException(state.toString());
        }
    }
    
    public int nextMessageID() {
        return ++messageID;
    }

    public void write(PDU pdu)
    {
        log.debug("Sending: {}", pdu);
        state.write(this, pdu);
    }
    
    public void abort(int reason) 
    {
        if (abort != null) // already aborted
            write(new AAbort(reason));
    }

    public void abort() 
    {
        if (abort != null) // already aborted
            write(new AAbort());
    }
    
   
    public void write(int pcid, DicomObject command, DataWriter dataWriter)
    throws IOException
    {
        log.debug("Sending DIMSE[pcid={}]:\n{}", new Integer(pcid),  command);
        
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
        
        ByteBuffer buf = ByteBuffer.allocate(sendPDULength);
        PDVOutputStream out = new PDVOutputStream(pcid, COMMAND, buf, sendPDULength);
        DicomOutputStream dos = new DicomOutputStream(out);
        try
        {
            dos.writeCommand(command);
            dos.close();
        } catch (IOException e)
        {
            // should never happen!
            log.error("Failed to encode Command into PDV", e);
            write(new AAbort(AAbort.REASON_NOT_SPECIFIED));
            throw e;
        }
        if (dataWriter != null) {
            if (!packPDV) 
                writePDataTF(buf);
            out = new PDVOutputStream(pcid, DATA, buf, sendPDULength);
            try
            {
                dataWriter.writeTo(out, TransferSyntax.valueOf(pc
                        .getTransferSyntax()));
                out.close(); 
            } catch (IOException e)
            {
                log.error("Failed to encode Data into PDVs", e);
                write(new AAbort(AAbort.REASON_NOT_SPECIFIED));
                throw e;
            }            
        }
        writePDataTF(buf);
    }

    private void writePDataTF(ByteBuffer buf)
    {
        buf.flip();
        write(new PDataTF(buf));
        buf.clear();
    }
   
    private synchronized void setState(State state)
    {
        if (this.state == state)
            return;

        this.state = state;
        log.debug("Enter State: {}", state);
        notifyAll();
    }

    private void startARTIM(long delay)
    {
        stopARTIM();
        if (delay <= 0)
            return;
        
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
        if (readDataThread != null)
            readDataThread.interrupt();
        setState(STA1);
        handler.onClosed(this);
        rspHandlerForMsgId.accept(new IntHashtable.Visitor(){

            public boolean visit(int key, Object value)
            {
                ((DimseRSPHandler) value).onClosed(Association.this);
                return true;
            }});
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
        if (state == STA6)
        {
            log.info("Release idle association");
            write(new AReleaseRQ());
        }
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
        startARTIM(associationAcceptTimeout);
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
        this.maxOpsInvoked = associateAC.getMaxOpsInvoked();
        this.sendPDULength = toSendPDULength(associateAC.getMaxPDULength());
        stopARTIM();
        setState(STA6);
        handler.onAAssociateAC(this, associateAC);
    }

    private int toSendPDULength(int len)
    {
        return len == 0 || len > maxSendPDULength ? maxSendPDULength : len;
    }

    /**
     * Issue A-ASSOCIATE confirmation (reject) primitive and close transport
     * connection. Next state is Sta1.
     * 
     * @param associateRJ
     */
    private void ae4(AAssociateRJ associateRJ)
    {
        this.associateRJ = associateRJ;
        stopARTIM();
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
        this.sendPDULength = toSendPDULength(associateRQ.getMaxPDULength());
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
            return new AAssociateRJ(
                    AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
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
        this.maxOpsInvoked = associateAC.getMaxOpsPerformed();
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
        session.write(releaseRQ);
        startARTIM(releaseResponseTimeout);
        setState(STA7);
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
        stopARTIM();
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
        if (readDataThread == null)
            executor.execute(new Runnable(){
                public void run()
                {
                    readData();                        
                }});
        readDataChannel.put(dataTF.getByteBuffer());
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
     */
    private void ar8(AReleaseRQ releaseRQ)
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
        stopARTIM();
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
            aa1(new AAbort(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else
        {
            aa1(new AAbort(AAbort.REASON_NOT_SPECIFIED));
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
            aa7(new AAbort(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa2();
        } else
        {
            aa7(new AAbort(AAbort.REASON_NOT_SPECIFIED));
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
            aa8(new AAbort(e.getReason()));
        } else if (cause instanceof IOException)
        {
            // do NOT try to send AAbort in case of I/O exception
            aa4(new AAbort(AAbort.REASON_NOT_SPECIFIED));
        } else
        {
            aa8(new AAbort(AAbort.REASON_NOT_SPECIFIED));
        }
    }

    void releaseResponseTimeoutExpired()
    {
        log.warn("Timeout for receiving A-RELEASE-RP expired - abort");
        aa8(new AAbort(AAbort.REASON_NOT_SPECIFIED));
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
                as.aa8(new AAbort(AAbort.UNEXPECTED_PDU));
        }

        protected void exception(Association as, Throwable cause)
        {
            as.aa8(cause);
        }

        protected void closed(Association as)
        {
            as.aa4(new AAbort(AAbort.REASON_NOT_SPECIFIED));
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
            super("Sta2 - Transport connection open (Awaiting A-ASSOCIATE-RQ PDU)");
        }

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
                as.aa1(new AAbort(AAbort.UNEXPECTED_PDU));
        }

        protected void exception(Association provider, Throwable cause)
        {
            provider.aa1(cause);
        }

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

        protected void artimExpired(Association as)
        {
            log.warn("Timeout for receiving A-ASSOCIATE-AC expired - abort");
            as.aa8(new AAbort(AAbort.REASON_NOT_SPECIFIED));
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
            else if (pdu instanceof AReleaseRQ)
                as.ar8((AReleaseRQ) pdu);
            else
                super.received(as, pdu);
        }
        
        protected void artimExpired(Association as)
        {
            as.releaseResponseTimeoutExpired();
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
            super("Sta9 - Release collision requestor side; awaiting A-RELEASE response primitive");
        }

        protected void write(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar9((AReleaseRP) pdu);
            else
                super.write(as, pdu);
        }
                
        protected void artimExpired(Association as)
        {
            as.releaseResponseTimeoutExpired();
        }
    }

    private static class Sta10 extends State
    {

        public Sta10()
        {
            super("Sta10 - Release collision acceptor side; awaiting A-RELEASE-RP PDU");
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar10((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }

        protected void artimExpired(Association as)
        {
            as.releaseResponseTimeoutExpired();
        }
    }

    private static class Sta11 extends State
    {

        public Sta11()
        {
            super("Sta11 - Release collision requestor side; awaiting A-RELEASE-RP PDU");
        }

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AReleaseRP)
                as.ar3((AReleaseRP) pdu);
            else
                super.received(as, pdu);
        }

        protected void artimExpired(Association as)
        {
            as.releaseResponseTimeoutExpired();
        }
    }

    private static class Sta12 extends State
    {

        public Sta12()
        {
            super(
                    "Sta12 - Release collision acceptor side; awaiting A-RELEASE response primitive");
        }

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

        protected void received(Association as, PDU pdu)
        {
            if (pdu instanceof AAssociateRQ)
                as.aa7(new AAbort(AAbort.UNEXPECTED_PDU));
            else if (pdu instanceof AAbort)
                as.aa2();
            else
                as.aa6();
        }

        protected void write(Association as, PDU pdu)
        {
            if (!(pdu instanceof AAbort))
                throw new IllegalStateException(name);
        }

        protected void exception(Association as, Throwable cause)
        {
            as.aa7(cause);
        }

        protected void closed(Association as)
        {
            as.ar5();
        }
    }
    
    void readData()
    {
        if (readDataThread != null)
            return;
        
        readDataThread = Thread.currentThread(); 
        if (log.isDebugEnabled())
            log.debug("" + this + ": Enter readData");
        while (nextDIMSE())
            ;
        if (log.isDebugEnabled())
            log.debug("" + this + ": Exit readData");
    }

    private boolean nextDIMSE()
    {
        if (log.isDebugEnabled())
            log.debug("Waiting for next DIMSE");
        
        final ByteBuffer dataTF = readDataChannel.get();
        if (dataTF == null)
            return false;
        
        PDVInputStream cmdStream = new PDVInputStream(dataTF, COMMAND, -1);
        final int pcid = cmdStream.getPCID();
        
        PresentationContext pc = associateAC.getPresentationContext(pcid);
        if (pc == null)
        {
            log.warn("No Presentation Context with given ID - " + pcid);
            abort(AAbort.UNEXPECTED_PDU_PARAMETER);
            return false;
        }
        
        if (!pc.isAccepted())
        {
            log.warn("No accepted Presentation Context with given ID - " + pcid);
            abort(AAbort.UNEXPECTED_PDU_PARAMETER);
            return false;
        }
        
        DicomObject cmd = readDicomObject(cmdStream,
                TransferSyntax.ImplicitVRLittleEndian);
        
        if (cmd == null)
            return false;
        
        log.debug("Command:\n{}", cmd);
        if (CommandFactory.hasDataset(cmd))
        {
            PDVInputStream dataStream =
                new PDVInputStream(cmdStream.getDataTF(), DATA, pcid);
            handler.onDIMSE(this, pcid, cmd, dataStream);
            try
            {
                long skipped = dataStream.skipAll();
                if (log.isDebugEnabled() && skipped > 0)
                    log.debug("" + skipped + " bytes of DIMSE data not consumed by handler.onDIMSE().");                               
            }
            catch (IOException e)
            {
                // already handled by PDVInputStream
            }
        }
        else
        {
            handler.onDIMSE(this, pcid, cmd, null);
        }
        return true;
    }

    DicomObject readDicomObject(InputStream pdvStream,
            TransferSyntax ts)
    {
        try {
            DicomInputStream din = new DicomInputStream(pdvStream, ts);
            DicomObject dcmobj = new BasicDicomObject();
            din.readDicomObject(dcmobj, -1);
            return dcmobj;
        } catch (IOException e)
        {
            log.warn("Read Dicom Object throws i/o exepction:", e);
            abort();
            return null;
        }
    }

    class ByteBufferChannel
    {
        private ByteBuffer buffer;    
        
        synchronized public void put(ByteBuffer buffer)
        {
            try
            {
                while (this.buffer != null)
                    wait();
                this.buffer = buffer;
                notify();
            }
            catch (InterruptedException e)
            {
                Association.log.warn("Take over of P_DATA_TF to read thread interrupted:", e);
                Association.this.abort(AAbort.REASON_NOT_SPECIFIED);
            }
        }
        
        synchronized ByteBuffer get()
        {
            try
            {
                while (buffer == null)
                    wait();
            }
            catch (InterruptedException e)
            {
                return null;
            }
            ByteBuffer tmp = buffer;
            buffer = null;
            notify();
            return tmp;
        }
        
    }
    
    private class PDVInputStream extends InputStream
    {
        private int pcid;
        private int mch;
        private int available;
        private ByteBuffer dataTF;

        public PDVInputStream(ByteBuffer dataTF, int command, int pcid)
        {
            this.dataTF = dataTF;
            nextPDV(command, pcid);
        }

        public final int getPCID()
        {
            return pcid;
        }
        
        public final ByteBuffer getDataTF()
        {
            return dataTF;
        }
        
        private boolean isEOF() throws IOException
        {
            while (available == 0) {
                if ((mch & LAST) != 0)
                    return true;
                nextPDV(mch & COMMAND, pcid);
                throwAbortException();
            }
            return false;
        }
        
        private boolean nextPDV(int command, int expectPCID)
        {
            if (!dataTF.hasRemaining())
            {
                dataTF = readDataChannel.get();
                if (dataTF == null)
                {
                    if (expectPCID != -1)
                    {
                        log.warn("Unexpected End of PDV Stream");
                        abort(AAbort.REASON_NOT_SPECIFIED);
                    }
                    return false;
                }
            }
            this.available = dataTF.getInt() - 2;
            this.pcid = dataTF.get() & 0xff;
            this.mch = dataTF.get() & 0xff;
            if (log.isDebugEnabled())
                log.debug("Parsed PDV[len = " + available
                        + ", pcid = " + pcid + ", mch = " + mch + "]");
            if (this.available < 0)
            {
                log.warn("Invalid PDV item length: " + (available + 2));
                abort(AAbort.INVALID_PDU_PARAMETER_VALUE);
                return false;
            }
            if ((mch & COMMAND) != command)
            {
                log.warn(command == 0 ? "Expected Data but received Command PDV"
                                : "Expected Command but received Data PDV");
                abort(AAbort.INVALID_PDU_PARAMETER_VALUE);
                return false;
            }
            if (expectPCID != -1 && expectPCID != pcid)
            {
                log.warn("Expected PCID: " + expectPCID + " but received: " + pcid);
                abort(AAbort.INVALID_PDU_PARAMETER_VALUE);
                return false;
            }
            return true;
        }
     
        public int read() throws IOException
        {
            if (isEOF())
                return -1;
            
            --available;
            return dataTF.get() & 0xff;
        }

        public int read(byte[] b, int off, int len) throws IOException
        {
            if (isEOF())
                return -1;
            
            int read = Math.min(len, available);
            dataTF.get(b, off, read);
            available -= read;
            return read;
        }

        public int available() throws IOException
        {
             return available;
        }

        public long skip(long n) throws IOException
        {
            if (n <= 0 || isEOF())
                return 0;

            int skipped = (int) Math.min(n, available);
            dataTF.position(dataTF.position() + skipped);
            available -= skipped;
            return skipped;
        }


        private long skipAll() throws IOException
        {
            long n = 0;            
            while (!isEOF())
            {
                n += available;
                dataTF.position(dataTF.position() + available);
                available = 0;
            }
            return n;
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

        public void write(int b) throws IOException
        {
            throwAbortException();
            if (free == 0)
                flushPDataTF();
            buf.put((byte) b);
            --free;
            ++pdvLen;            
        }

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

        public void close() throws IOException
        {
            throwAbortException();
            writePDVHeader(LAST);
        }

    }

    public void invoke(int pcid, DicomObject cmd, DicomObject data, 
            DimseRSPHandler rspHandler)
    throws IOException
    {
        invoke(pcid, cmd, new DataWriterAdapter(data), rspHandler);
    }
    
    public void invoke(int pcid, DicomObject cmd, DimseRSPHandler rspHandler)
    throws IOException
    {
        invoke(pcid, cmd, (DataWriter) null, rspHandler);
    }

    public void invoke(int pcid, DicomObject cmd, DataWriter data, 
            DimseRSPHandler rspHandler)
    throws IOException
    {
        addDimseRSPHandler(cmd.getInt(Tag.MessageID), rspHandler);
        write(pcid, cmd, data);
    }
    
    void onDimseRSP(int pcid, DicomObject cmd, InputStream dataStream)
    {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        rspHandler.onDimseRSP(this, pcid, cmd, dataStream);
        if (!CommandFactory.isPending(cmd))
            removeDimseRSPHandler(msgId);
    }

    private void addDimseRSPHandler(int msgId, DimseRSPHandler rspHandler)
    {
        synchronized (rspHandlerForMsgId)
        {
            if (maxOpsInvoked > 0)
                while (rspHandlerForMsgId.size() >= maxOpsInvoked)
                {
                    try
                    {
                        rspHandlerForMsgId.wait();
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            rspHandlerForMsgId.put(msgId, rspHandler);
        }
    }

    private DimseRSPHandler removeDimseRSPHandler(int msgId)
    {
        synchronized (rspHandlerForMsgId)
        {
            DimseRSPHandler tmp = 
                    (DimseRSPHandler) rspHandlerForMsgId.remove(msgId);
            rspHandlerForMsgId.notifyAll();
            return tmp;
        }
    }

    private DimseRSPHandler getDimseRSPHandler(int msgId)
    {
        synchronized (rspHandlerForMsgId)
        {
            return (DimseRSPHandler) rspHandlerForMsgId.get(msgId);
        }
    }

    void onCancelRQ(int pcid, DicomObject cmd, InputStream dataStream)
    {
        // TODO Auto-generated method stub
        
    }

    public DimseRSP invoke(int pcid, DicomObject cmd, DicomObject data)
    throws IOException
    {
        return invoke(pcid, cmd, new DataWriterAdapter(data));
    }
    
    public DimseRSP invoke(int pcid, DicomObject cmd)
    throws IOException
    {
        return invoke(pcid, cmd, (DataWriter) null);
    }
    
    public DimseRSP invoke(int pcid, DicomObject cmd, DataWriter dw)
    throws IOException
    {
        DimseRSP rsp = new DimseRSP();
        invoke(pcid, cmd, dw, rsp);
        return rsp;
    }

    public void release()
    {
        write(new AReleaseRQ());
    }

    public boolean waitForDimseRSP(long timeout)
    {
        if (!rspHandlerForMsgId.isEmpty()) {
            synchronized (rspHandlerForMsgId)
            {
                while (!rspHandlerForMsgId.isEmpty())
                {
                    try
                    {
                        rspHandlerForMsgId.wait(timeout);
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return rspHandlerForMsgId.isEmpty();
    }

    public void waitForDimseRSP()
    {
        if (!rspHandlerForMsgId.isEmpty()) {
            synchronized (rspHandlerForMsgId)
            {
                while (!rspHandlerForMsgId.isEmpty())
                {
                    try
                    {
                        rspHandlerForMsgId.wait();
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

}
