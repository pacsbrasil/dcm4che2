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
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.net.pdu.AAbortException;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJException;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AAssociateRQAC;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.util.IntHashtable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision$ $Date$
 * @since Nov 25, 2005
 *
 */
public class Association implements Runnable
{
    static Logger log = LoggerFactory.getLogger(Association.class);

    private final Connector connector;
    private final AssociationReaper reaper;
    private ApplicationEntity ae;
    private Socket socket;
    private boolean requestor = false;
    private InputStream in;
    private OutputStream out;
    private PDUEncoder encoder;
    private PDUDecoder decoder;
    private State state;

    private AAssociateRQ associateRQ;
    private AAssociateAC associateAC;
    private IOException exception;
    
    private int maxOpsInvoked;

    private int msgID = 0;
    private int performing = 0;
    private boolean closed = false;
    private IntHashtable rspHandlerForMsgId = new IntHashtable();
    private IntHashtable cancelHandlerForMsgId = new IntHashtable();
    private HashMap acceptedPCs = new HashMap();
    private long idleTimeout = Long.MAX_VALUE;

    
    private Association(Socket socket, Connector connector, boolean requestor)
    throws IOException
    {
        if (socket == null)
            throw new NullPointerException("socket");
        if (connector == null)
            throw new NullPointerException("connector");
        this.connector = connector;
        this.reaper = connector.getDevice().getAssociationReaper();
        this.socket = socket;
        this.requestor = requestor;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.encoder = new PDUEncoder(this, out);
        this.state = State.STA1;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Association[");
        if (associateRQ != null) {
            if (requestor)
            {
                sb.append(associateRQ.getCallingAET());
                sb.append(">>");
                sb.append(associateRQ.getCalledAET());
            }
            else
            {
                sb.append(associateRQ.getCalledAET());                
                sb.append("<<");
                sb.append(associateRQ.getCallingAET());
            }
            sb.append(", ");
       }
       sb.append(socket).append("]");
       return sb.toString();
    }
    
    static Association request(Socket socket, Connector connector, ApplicationEntity ae) 
    throws IOException
    {
        Association a = new Association(socket, connector, true);
        a.setApplicationEntity(ae);
        a.setState(State.STA4);
        return a;
    }

    static Association accept(Socket socket, Connector connector) 
    throws IOException
    {
        Association a = new Association(socket, connector, false);
        a.setState(State.STA2);
        a.startARTIM(connector.getRequestTimeout());
        return a;
    }
    
    final void setApplicationEntity(ApplicationEntity ae)
    {
        this.ae = ae;
    }
    
    final AAssociateAC getAssociateAC()
    {
        return associateAC;
    }

    final AAssociateRQ getAssociateRQ()
    {
        return associateRQ;
    }
        
    final IOException getException()
    {
        return exception;
    }
    
    void checkException() throws IOException
    {
        if (exception != null)
            throw exception;
    }
    
    public final boolean isReadyForDataTransfer()
    {
        return state.isReadyForDataTransfer();
    }

    private boolean isReadyForDataReceive()
    {
        return state.isReadyForDataReceive();
    }
    
    private boolean isReadyForDataSend()
    {
        return state.isReadyForDataSend();
    }
    
    void setState(State state)
    {
        if (this.state == state)
            return;

        boolean wasReadyForDataReceive = isReadyForDataReceive();
        synchronized (this)
        {
            this.state = state;
            log.debug("Enter State: {}", state);
            notifyAll();
        }
        if (wasReadyForDataReceive && !isReadyForDataReceive())
            fireEndOfData();
    }
    
    private void fireEndOfData()
    {
        // TODO Auto-generated method stub
        
    }

    private Map acceptedPC(String asuid)
    {
        return (Map) acceptedPCs.get(asuid);
    }
    
    private void checkAAAC()
    {
        Collection c = associateAC.getPresentationContexts();
        for (Iterator iter = c.iterator(); iter.hasNext();)
        {
            PresentationContext pc = (PresentationContext) iter.next();
            if (!pc.isAccepted())
                continue;
            PresentationContext pcrq = 
                associateRQ.getPresentationContext(pc.getPCID());
            if (pcrq == null)
            {
                log.warn("A-ASSOCIATE-AC contains not offered Presentation Context: " + pc);
                continue;
            }
            String as = pcrq.getAbstractSyntax();
            Map ts2pc = (Map) acceptedPCs.get(as);
            if (ts2pc == null)
            {
                ts2pc = new HashMap();
                acceptedPCs.put(as, ts2pc);
            }
            ts2pc.put(pc.getTransferSyntax(), pc);
        }
    }

    public AAssociateAC negotiate(AAssociateRQ rq)
    throws IOException, InterruptedException
    {
        sendAssociateRQ(rq);        
        synchronized (this)
        {
            while (state == State.STA5)
                wait();
        }
        checkException();
        if (state != State.STA6)
        {
            throw new RuntimeException("unexpected state: " + state);
        }        
        return associateAC;        
    }
    
    void pabort(int reason)
    {
        abort(new AAbortException(AAbortException.UL_SERIVE_PROVIDER, reason));        
    }

    public void release(boolean waitForRSP) throws InterruptedException
    {
        if (waitForRSP)
            waitForDimseRSP();
        
        sendReleaseRQ();
        synchronized (this)
        {
            while (state != State.STA1)
                wait();
        }
    }

    public void waitForDimseRSP() throws InterruptedException
    {
        synchronized (rspHandlerForMsgId)
        {
            while (!rspHandlerForMsgId.isEmpty() && isReadyForDataReceive())
                rspHandlerForMsgId.wait();
        }
    }

    public void abort()
    {
        abort(new AAbortException());       
    }
    
    private PresentationContext pcFor(String cuid, String[] tsuids) 
    throws NoPresentationContextException
    {
        Map ts2pc = acceptedPC(cuid);
        if (ts2pc == null)
            throw new NoPresentationContextException("Abstract Syntax "
                    + UIDDictionary.getDictionary().prompt(cuid)
                    + " not supported");
        if (tsuids == null)
            return (PresentationContext) ts2pc.values().iterator().next();
        for (int i = 0; i < tsuids.length; i++)
        {
            PresentationContext pc = (PresentationContext) ts2pc.get(tsuids[i]);
            if (pc != null)
                return pc;
        }
        throw new NoPresentationContextException("Abstract Syntax "
                + UIDDictionary.getDictionary().prompt(cuid)
                + " with Transfer Syntax "
                + UIDDictionary.getDictionary().prompt(tsuids[0])
                + " not supported");
    }
    
    public void cstore(String cuid, String iuid, int priority,
            DataWriter data, String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject cstorerq = CommandUtils.newCStoreRQ(
                ++msgID, cuid, iuid, priority);
        invoke(pc.getPCID(), cstorerq, data, rspHandler);
    }
    
    public DimseRSP cstore(String cuid, String iuid, int priority,
            DataWriter data, String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        cstore(cuid, iuid, priority, data, tsuids, rsp.getHandler());
        return rsp;
    }

    public void cfind(String cuid, int priority,
            DicomObject data, String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject cfindrq = CommandUtils.newCFindRQ(
                ++msgID, cuid, priority);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cfind(String cuid, int priority,
            DicomObject data, String[] tsuids, int autoCancel)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        rsp.setAutoCancel(autoCancel);
        cfind(cuid, priority, data, tsuids, rsp.getHandler());
        return rsp;
    }
    
    public void cget(String cuid, int priority,
            DicomObject data, String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject cfindrq = CommandUtils.newCGetRQ(
                ++msgID, cuid, priority);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cget(String cuid, int priority,
            DicomObject data, String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        cget(cuid, priority, data, tsuids, rsp.getHandler());
        return rsp;
    }
    
    public void cmove(String cuid, int priority, DicomObject data,
            String[] tsuids, String destination, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject cfindrq = CommandUtils.newCMoveRQ(
                ++msgID, cuid, priority, destination);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cmove(String cuid, int priority, DicomObject data,
            String[] tsuids, String destination)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        cmove(cuid, priority, data, tsuids, destination, rsp.getHandler());
        return rsp;
    }
    
    public DimseRSP cecho()
    throws IOException, InterruptedException
    {
        return cecho(UID.VerificationSOPClass);
    }
    
    public DimseRSP cecho(String cuid)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        PresentationContext pc = pcFor(cuid, null);
        DicomObject cechorq = CommandUtils.newCEchoRQ(++msgID, cuid);
        invoke(pc.getPCID(), cechorq, null, rsp.getHandler());
        return rsp;
    }

    public void nevent(String cuid, String iuid, int eventTypeId,
            DicomObject attrs, String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject neventrq = CommandUtils.newNEventReportRQ(
                ++msgID, cuid, iuid, eventTypeId, attrs);
        invoke(pc.getPCID(), neventrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nevent(String cuid, String iuid, int eventTypeId,
            DicomObject attrs, String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        nevent(cuid, iuid, eventTypeId, attrs, tsuids, rsp.getHandler());
        return rsp;
    }    

    public void nget(String cuid, String iuid, DicomObject attrs,
            String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject ngetrq = CommandUtils.newNGetRQ(++msgID, cuid, iuid, attrs);
        invoke(pc.getPCID(), ngetrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nget(String cuid, String iuid, DicomObject attrs,
            String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        nget(cuid, iuid, attrs, tsuids, rsp.getHandler());
        return rsp;
    }    
    
    public void nset(String cuid, String iuid, DicomObject attrs,
            String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject nsetrq = CommandUtils.newNSetRQ(++msgID, cuid, iuid);
        invoke(pc.getPCID(), nsetrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nset(String cuid, String iuid, DicomObject attrs,
            String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        nset(cuid, iuid, attrs, tsuids, rsp.getHandler());
        return rsp;
    }    

    public void naction(String cuid, String iuid, int actionTypeId,
            DicomObject attrs, String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject nactionrq = CommandUtils.newNActionRQ(
                ++msgID, cuid, iuid, actionTypeId, attrs);
        invoke(pc.getPCID(), nactionrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP naction(String cuid, String iuid, int actionTypeId,
            DicomObject attrs, String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        naction(cuid, iuid, actionTypeId, attrs, tsuids, rsp.getHandler());
        return rsp;
    }    
    
    public void ncreate(String cuid, String iuid, DicomObject attrs,
            String[] tsuids, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuids);
        DicomObject ncreaterq = CommandUtils.newNCreateRQ(++msgID, cuid, iuid);
        invoke(pc.getPCID(), ncreaterq, new DataWriterAdapter(attrs), rspHandler);
    }
    
    public DimseRSP ncreate(String cuid, String iuid, DicomObject attrs,
            String[] tsuids)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        ncreate(cuid, iuid, attrs, tsuids, rsp.getHandler());
        return rsp;
    }
    
    public void ndelete(String cuid, String iuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, null);
        DicomObject nsetrq = CommandUtils.newNDeleteRQ(++msgID, cuid, iuid);
        invoke(pc.getPCID(), nsetrq, null, rspHandler);
    }

    public DimseRSP ndelete(String cuid, String iuid)
    throws IOException, InterruptedException
    {
        DimseRSP rsp = new DimseRSP();
        ndelete(cuid, iuid, rsp.getHandler());
        return rsp;
    }   
    
    void invoke(int pcid, DicomObject cmd, DataWriter data, 
            DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        if (CommandUtils.isResponse(cmd))
            throw new IllegalArgumentException("cmd:\n" + cmd);
        checkException();
        if (!isReadyForDataTransfer())
            throw new IllegalStateException(state.toString());
        PresentationContext pc = associateAC.getPresentationContext(pcid);
        if (pc == null)
            throw new IllegalStateException("No Presentation State with id - " + pcid);
        if (!pc.isAccepted())
            throw new IllegalStateException("Presentation State not accepted - " + pc);
        rspHandler.setPcid(pcid);
        rspHandler.setMsgId(cmd.getInt(Tag.MessageID));
        rspHandler.setTimeout(System.currentTimeMillis() 
                + (cmd.getInt(Tag.CommandField) == CommandUtils.C_MOVE_RQ ?
                        ae.getMoveRspTimeout() : ae.getDimseRspTimeout()));
        addDimseRSPHandler(cmd.getInt(Tag.MessageID), rspHandler);
        log.debug("Sending DIMSE-RQ[pcid={}]:\n{}", new Integer(pcid),  cmd);
        encoder.writeDIMSE(pcid, cmd, data, pc.getTransferSyntax());      
    }
    
    void cancel(int pcid, int msgid)
    throws IOException
    {
        log.debug("Sending C-CANCEL-RQ");
        encoder.writeDIMSE(pcid, CommandUtils.newCCancelRQ(msgid), null, null);      
    }

    public void writeDimseRSP(int pcid, DicomObject cmd)
    throws IOException
    {
        writeDimseRSP(pcid, cmd, null);
    }
    
    public void writeDimseRSP(int pcid, DicomObject cmd, DicomObject data)
    throws IOException
    {
        if (!CommandUtils.isResponse(cmd))
            throw new IllegalArgumentException("cmd:\n" + cmd);
        PresentationContext pc = associateAC.getPresentationContext(pcid);
        if (pc == null)
            throw new IllegalStateException("No Presentation State with id - " + pcid);
        if (!pc.isAccepted())
            throw new IllegalStateException("Presentation State not accepted - " + pc);
        
        log.debug("Sending DIMSE-RSP[pcid={}]:\n{}", new Integer(pcid),  cmd);
        DataWriter writer = data != null ? new DataWriterAdapter(data) : null;
        encoder.writeDIMSE(pcid, cmd, writer, pc.getTransferSyntax());
        if (!CommandUtils.isPending(cmd))
        {
            updateIdleTimeout();
            decPerforming();
        }
    }

    void onCancelRQ(DicomObject cmd)
    {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        CancelRQHandler handler = removeCancelRQHandler(msgId);
        if (handler != null)
        {
            handler.cancel(this);
        }
        
    }
    
    public void registerCancelRQHandler(DicomObject cmd, CancelRQHandler handler)
    {
        synchronized (cancelHandlerForMsgId)
        {
            cancelHandlerForMsgId.put(cmd.getInt(Tag.MessageID), handler);
        }
    }
    
    private CancelRQHandler removeCancelRQHandler(int msgId)
    {
        synchronized (cancelHandlerForMsgId)
        {
            return (CancelRQHandler) cancelHandlerForMsgId.remove(msgId);
        }
    }

    void onDimseRSP(DicomObject cmd, DicomObject data)
    throws IOException
    {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        DimseRSPHandler rspHandler = getDimseRSPHandler(msgId);
        if (rspHandler == null)
        {
            log.warn("unexpected message ID in DIMSE RSP:\n{}", cmd);
            throw new AAbortException();
        }
        try
        {
            rspHandler.onDimseRSP(this, cmd, data);
        }
        finally
        {
            if (!CommandUtils.isPending(cmd))
            {
                updateIdleTimeout();
                removeDimseRSPHandler(msgId);
            }
            else
            {
                rspHandler.setTimeout(System.currentTimeMillis() 
                        + (cmd.getInt(Tag.CommandField) == CommandUtils.C_MOVE_RSP ?
                                ae.getMoveRspTimeout() : ae.getDimseRspTimeout()));
                
            }
        }
    }

    private void addDimseRSPHandler(int msgId, DimseRSPHandler rspHandler)
    throws InterruptedException
    {
        synchronized (rspHandlerForMsgId)
        {
            if (maxOpsInvoked > 0)
                while (rspHandlerForMsgId.size() >= maxOpsInvoked)
                    rspHandlerForMsgId.wait();
            if (isReadyForDataReceive())
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

    public void run()
    {
        try
        {
            this.decoder = new PDUDecoder(this, in);
            while (!(state == State.STA1 || state == State.STA13))
                decoder.nextPDU();
        }
        catch (AAbortException aa)
        {
            abort(aa);
        }
        catch (Throwable e)
        {
            setState(State.STA1);
        }
        finally
        {
            closeSocket();
        }        
    }
    
    private void closeSocket()
    {
        if (state != State.STA1)
        {
            try
            {
                Thread.sleep(connector.getSocketCloseDelay());
            }
            catch (InterruptedException e)
            {
                log.warn("Interrupted Socket Close Delay", e);
            }
            setState(State.STA1);
        }
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                log.warn("I/O error during close of socket output stream", e);
            }
            out = null;
            encoder = null;
        }
        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                log.warn("I/O error during close of socket input stream", e);
            }
            in = null;
            decoder = null;
        }
        try
        {
            socket.close();
        }
        catch (IOException e)
        {
            log.warn("I/O error during close of socket", e);
        }
        if (!closed)
            onClosed();
    }

    private void onClosed()
    {
        closed  = true;
        reaper.unregister(this);
        synchronized (rspHandlerForMsgId)
        {
            rspHandlerForMsgId.accept(new IntHashtable.Visitor(){
                
                public boolean visit(int key, Object value)
                {
                    ((DimseRSPHandler)value).onClosed(Association.this);
                    return true;
                }});
            rspHandlerForMsgId.clear();
            rspHandlerForMsgId.notifyAll();
        }
//        if (ae != null)
//            ae.onClosed(this);
        
    }

    int getMaxPDULengthSend()
    {
        AAssociateRQAC rqac = requestor 
                ? (AAssociateRQAC) associateAC 
                : (AAssociateRQAC) associateRQ;
        return Math.min(rqac.getMaxPDULength(), ae.getMaxPDULengthSend());
    }

    boolean isPackPDV()
    {
        return ae.isPackPDV();
    }

    private void startARTIM(int timeout) throws IOException
    {
        socket.setSoTimeout(timeout);        
    }
    
    private void stopARTIM() throws IOException
    {
        socket.setSoTimeout(0);        
    }

    void receivedAssociateRQ(AAssociateRQ rq) throws IOException
    {
        state.received(this, rq);        
    }

    void receivedAssociateAC(AAssociateAC ac) throws IOException
    {
        state.received(this, ac);
    }

    void receivedAssociateRJ(AAssociateRJException rj) throws IOException
    {
        state.received(this, rj);        
    }

    void receivedPDataTF() throws IOException
    {
        state.receivedPDataTF(this);        
    }

    void onPDataTF() throws IOException
    {
        decoder.decodeDIMSE();        
    }
    
    void receivedReleaseRQ() throws IOException
    {
        state.receivedReleaseRQ(this);        
    }
   
    void receivedReleaseRP() throws IOException
    {
        state.receivedReleaseRP(this);        
    }

    void receivedAbort(AAbortException aa)
    {
        exception = aa;
        setState(State.STA1);
    }

    void onDimseRQ(int pcid, DicomObject cmd, PDVInputStream data, String tsuid)
    throws IOException
    {
        incPerforming();
        ae.perform(this, pcid, cmd, data, tsuid);
    }

    private synchronized void incPerforming()
    {
        ++performing;
    }

    private synchronized void decPerforming()
    {
        --performing;
        notifyAll();
    }
    
    void sendPDataTF() throws IOException
    {
        try
        {
            state.sendPDataTF(this);
        }
        catch (IOException e)
        {
            closeSocket();
            throw e;
        }        
    }

    void writePDataTF() throws IOException
    {
        encoder.writePDataTF();
    }

    void sendAssociateRQ(AAssociateRQ rq) throws IOException
    {
        try
        {
            state.send(this, rq);
            startARTIM(connector.getAcceptTimeout());
        }
        catch (IOException e)
        {
            closeSocket();
            throw e;
        }
    }
        
    void sendReleaseRQ()
    {
        try
        {
            state.sendReleaseRQ(this);
        }
        catch (IOException e)
        {
            closeSocket();
        }
    }
    
    void abort(AAbortException aa)
    {
        state.abort(this, aa);       
    }

    void writeAbort(AAbortException aa)
    {
        exception = aa;
        try
        {
            setState(State.STA13);
            encoder.write(aa);
        }
        catch (Throwable e)
        {
            setState(State.STA1);
        }
        closeSocket();
    }
    
    void unexpectedPDU(String name) throws AAbortException
    {
        log.warn("received unexpected " + name + " in state: " + state);
        throw new AAbortException(AAbortException.UL_SERIVE_PROVIDER, 
                AAbortException.UNEXPECTED_PDU);
    }

    void illegalStateForSending(String name) throws IOException
    {
        log.warn("unable to send " + name + " in state: " + state);
        checkException();
        throw new AAbortException();
    }
    
    void writeAssociationRQ(AAssociateRQ rq) throws IOException
    {
        associateRQ = rq;
        setState(State.STA5);
        encoder.write(rq);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException
    {
        associateRQ = rq;
        stopARTIM();
        setState(State.STA3);
        try
        {
            associateAC = connector.getDevice().negotiate(this, rq);
            checkAAAC();
            setState(State.STA6);
            encoder.write(associateAC);
            updateIdleTimeout();
            reaper.register(this);
        }
        catch (AAssociateRJException e)
        {
            setState(State.STA13);
            encoder.write(e);
        }        
    }

    void onAssociateAC(AAssociateAC ac) throws IOException
    {
        stopARTIM();
        associateAC = ac;
        checkAAAC();
        setState(State.STA6);
        updateIdleTimeout();
        reaper.register(this);
    }

    private void updateIdleTimeout()
    {
        idleTimeout = System.currentTimeMillis() + ae.getIdleTimeout();        
    }

    void onAssociateRJ(AAssociateRJException rj) throws IOException
    {
        stopARTIM();
        exception = rj;
        setState(State.STA1);        
    }

    void writeReleaseRQ() throws IOException
    {
        setState(State.STA7);                
        encoder.writeAReleaseRQ();
    }

    void onReleaseRP() throws IOException
    {
        stopARTIM();
        setState(State.STA1);        
    }

    void onCollisionReleaseRP() throws IOException
    {
        stopARTIM();
//        setState(State.STA12);        
        setState(State.STA13);                
        encoder.writeAReleaseRP();
    }

    void onReleaseRQ() throws IOException
    {
        setState(State.STA8);
        waitForPerformingOps();
        setState(State.STA13);                
        encoder.writeAReleaseRP();
    }

    private synchronized void waitForPerformingOps()
    {
         while (performing > 0 && isReadyForDataReceive())
            try { wait(); }
            catch (InterruptedException e) {}
    }

    void onCollisionReleaseRQ() throws IOException
    {
        if (requestor)
        {
//            setState(State.STA9);
            setState(State.STA11);                
            encoder.writeAReleaseRP();
        }
        else
        {
            setState(State.STA10);
        }
    }

    void checkIdle(final long now)
    {
        if (performing > 0)
            return;
        if (rspHandlerForMsgId.isEmpty())
        {
            if (now > idleTimeout)
                try
                {
                    release(false);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
        }
        else
        {
            rspHandlerForMsgId.accept(new IntHashtable.Visitor(){

                public boolean visit(int key, Object value)
                {
                    DimseRSPHandler rspHandler = (DimseRSPHandler) value;
                    if (now < rspHandler.getTimeout())
                        return true;
                    Association.this.abort();                        
                    return false;
                }});
        }
    }

}
