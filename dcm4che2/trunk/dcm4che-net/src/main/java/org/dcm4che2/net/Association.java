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
import org.dcm4che2.data.VR;
import org.dcm4che2.net.pdu.AAbortException;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJException;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
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
    static int nextSerialNo = 0;
    private final int serialNo = ++nextSerialNo;
    private final NetworkConnection connector;
    private final AssociationReaper reaper;
    private NetworkApplicationEntity ae;
    private Socket socket;
    private boolean requestor = false;
    private InputStream in;
    private OutputStream out;
    private PDUEncoder encoder;
    private PDUDecoder decoder;
    private State state;
    private String name = "Association(" + serialNo + ")";

    private AAssociateRQ associateRQ;
    private AAssociateAC associateAC;
    private IOException exception;
    
    private int maxOpsInvoked;
    private int maxPDULength;

    private int msgID = 0;
    private int performing = 0;
    private boolean closed = false;
    private IntHashtable rspHandlerForMsgId = new IntHashtable();
    private IntHashtable cancelHandlerForMsgId = new IntHashtable();
    private HashMap acceptedPCs = new HashMap();
    private HashMap scuTCs = new HashMap();
    private HashMap scpTCs = new HashMap();
    private long idleTimeout = Long.MAX_VALUE;

    
    private Association(Socket socket, NetworkConnection connector, boolean requestor)
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
        log.info(requestor ? "{} initiated {}" : "{} accepted {}", name, socket);
    }
    
    public String toString()
    {
        return name;
    }
    
    static Association request(Socket socket, NetworkConnection connector, NetworkApplicationEntity ae) 
    throws IOException
    {
        Association a = new Association(socket, connector, true);
        a.setApplicationEntity(ae);
        a.setState(State.STA4);
        return a;
    }

    static Association accept(Socket socket, NetworkConnection connector) 
    throws IOException
    {
        Association a = new Association(socket, connector, false);
        a.setState(State.STA2);
        a.startARTIM(connector.getRequestTimeout());
        return a;
    }
    
    final void setApplicationEntity(NetworkApplicationEntity ae)
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
    
    public final boolean isRequestor()
    {
        return requestor;
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

        synchronized (this)
        {
            log.debug("{} enter state: {}", this, state);
            this.state = state;
            notifyAll();
        }
    }
    
    private void processAC()
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
                log.warn("{}: A-ASSOCIATE-AC contains not offered Presentation Context: {}", name, pc);
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
        for (Iterator iter = acceptedPCs.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry e = (Map.Entry) iter.next();
            String asuid = (String) e.getKey();
            Map ts2pc = (Map) e.getValue();
            String[] tsuids = (String[]) ts2pc.keySet().toArray(new String[ts2pc.size()]);
            String cuid = asuid; // TODO support of Meta SOP Classes
            if (isSCUFor(cuid))
                scuTCs.put(cuid, new TransferCapability(cuid, tsuids, TransferCapability.SCU));
            if (isSCPFor(cuid))
                scpTCs.put(cuid, new TransferCapability(cuid, tsuids, TransferCapability.SCP));            
        }
    }

    private boolean isSCPFor(String cuid)
    {
        RoleSelection rolsel = associateAC.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return !requestor;
        return requestor ? rolsel.isSCP() : rolsel.isSCU();
    }

    private boolean isSCUFor(String cuid)
    {
        RoleSelection rolsel = associateAC.getRoleSelectionFor(cuid);
        if (rolsel == null)
            return requestor;
        return requestor ? rolsel.isSCU() : rolsel.isSCP();
    }

    public String getCallingAET()
    {
        return associateRQ != null ? associateRQ.getCallingAET() : null;
    }

    public String getCalledAET()
    {
        return associateRQ != null ? associateRQ.getCalledAET() : null;
    }

    public String getRemoteAET()
    {
        return requestor ? getCalledAET() : getCallingAET();
    }

    public String getLocalAET()
    {
        return requestor ? getCallingAET() : getCalledAET();
    }
    
    public TransferCapability getTransferCapabilityAsSCP(String cuid)
    {
        return (TransferCapability) scpTCs.get(cuid);
    }
    
    public TransferCapability getTransferCapabilityAsSCU(String cuid)
    {
        return (TransferCapability) scuTCs.get(cuid);
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
        if (ae != null)
            ae.removeFromPool(this);
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
    
    private PresentationContext pcFor(String cuid, String tsuid) 
    throws NoPresentationContextException
    {
        Map ts2pc = (Map) acceptedPCs.get(cuid);
        if (ts2pc == null)
            throw new NoPresentationContextException("Abstract Syntax "
                    + UIDDictionary.getDictionary().prompt(cuid)
                    + " not supported");
        if (tsuid == null)
            return (PresentationContext) ts2pc.values().iterator().next();
        PresentationContext pc = (PresentationContext) ts2pc.get(tsuid);
        if (pc == null)
            throw new NoPresentationContextException("Abstract Syntax "
                    + UIDDictionary.getDictionary().prompt(cuid)
                    + " with Transfer Syntax "
                    + UIDDictionary.getDictionary().prompt(tsuid)
                    + " not supported");
        return pc;
    }
    
    public void cstore(String cuid, String iuid, int priority,
            DataWriter data, String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject cstorerq = CommandUtils.newCStoreRQ(
                ++msgID, cuid, iuid, priority);
        invoke(pc.getPCID(), cstorerq, data, rspHandler);
    }
    
    public DimseRSP cstore(String cuid, String iuid, int priority,
            DataWriter data, String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        cstore(cuid, iuid, priority, data, tsuid, rsp);
        return rsp;
    }

    public void cfind(String cuid, int priority,
            DicomObject data, String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject cfindrq = CommandUtils.newCFindRQ(
                ++msgID, cuid, priority);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cfind(String cuid, int priority,
            DicomObject data, String tsuid, int autoCancel)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        rsp.setAutoCancel(autoCancel);
        cfind(cuid, priority, data, tsuid, rsp);
        return rsp;
    }
    
    public void cget(String cuid, int priority,
            DicomObject data, String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject cfindrq = CommandUtils.newCGetRQ(
                ++msgID, cuid, priority);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cget(String cuid, int priority,
            DicomObject data, String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        cget(cuid, priority, data, tsuid, rsp);
        return rsp;
    }
    
    public void cmove(String cuid, int priority, DicomObject data,
            String tsuid, String destination, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject cfindrq = CommandUtils.newCMoveRQ(
                ++msgID, cuid, priority, destination);
        invoke(pc.getPCID(), cfindrq, new DataWriterAdapter(data), rspHandler);
    }
    
    public DimseRSP cmove(String cuid, int priority, DicomObject data,
            String tsuid, String destination)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        cmove(cuid, priority, data, tsuid, destination, rsp);
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
        FutureDimseRSP rsp = new FutureDimseRSP();
        PresentationContext pc = pcFor(cuid, null);
        DicomObject cechorq = CommandUtils.newCEchoRQ(++msgID, cuid);
        invoke(pc.getPCID(), cechorq, null, rsp);
        return rsp;
    }

    public void nevent(String cuid, String iuid, int eventTypeId,
            DicomObject attrs, String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject neventrq = CommandUtils.newNEventReportRQ(
                ++msgID, cuid, iuid, eventTypeId, attrs);
        invoke(pc.getPCID(), neventrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nevent(String cuid, String iuid, int eventTypeId,
            DicomObject attrs, String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        nevent(cuid, iuid, eventTypeId, attrs, tsuid, rsp);
        return rsp;
    }    

    public void nget(String cuid, String iuid, DicomObject attrs,
            String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject ngetrq = CommandUtils.newNGetRQ(++msgID, cuid, iuid, attrs);
        invoke(pc.getPCID(), ngetrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nget(String cuid, String iuid, DicomObject attrs,
            String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        nget(cuid, iuid, attrs, tsuid, rsp);
        return rsp;
    }    
    
    public void nset(String cuid, String iuid, DicomObject attrs,
            String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject nsetrq = CommandUtils.newNSetRQ(++msgID, cuid, iuid);
        invoke(pc.getPCID(), nsetrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP nset(String cuid, String iuid, DicomObject attrs,
            String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        nset(cuid, iuid, attrs, tsuid, rsp);
        return rsp;
    }    

    public void naction(String cuid, String iuid, int actionTypeId,
            DicomObject attrs, String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject nactionrq = CommandUtils.newNActionRQ(
                ++msgID, cuid, iuid, actionTypeId, attrs);
        invoke(pc.getPCID(), nactionrq, new DataWriterAdapter(attrs), rspHandler);
    }

    public DimseRSP naction(String cuid, String iuid, int actionTypeId,
            DicomObject attrs, String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        naction(cuid, iuid, actionTypeId, attrs, tsuid, rsp);
        return rsp;
    }    
    
    public void ncreate(String cuid, String iuid, DicomObject attrs,
            String tsuid, DimseRSPHandler rspHandler)
    throws IOException, InterruptedException
    {
        PresentationContext pc = pcFor(cuid, tsuid);
        DicomObject ncreaterq = CommandUtils.newNCreateRQ(++msgID, cuid, iuid);
        invoke(pc.getPCID(), ncreaterq, new DataWriterAdapter(attrs), rspHandler);
    }
    
    public DimseRSP ncreate(String cuid, String iuid, DicomObject attrs,
            String tsuid)
    throws IOException, InterruptedException
    {
        FutureDimseRSP rsp = new FutureDimseRSP();
        ncreate(cuid, iuid, attrs, tsuid, rsp);
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
        FutureDimseRSP rsp = new FutureDimseRSP();
        ndelete(cuid, iuid, rsp);
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
        encoder.writeDIMSE(pcid, cmd, data, pc.getTransferSyntax());      
    }
    
    void cancel(int pcid, int msgid)
    throws IOException
    {
        DicomObject cmd = CommandUtils.newCCancelRQ(msgid);
        encoder.writeDIMSE(pcid, cmd, null, null);      
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
        
        DataWriter writer = null;
        int datasetType = CommandUtils.NO_DATASET;
        if (data != null)
        {
            writer = new DataWriterAdapter(data);
            datasetType = CommandUtils.getWithDatasetType();
        }
        cmd.putInt(Tag.DataSetType, VR.US, datasetType);
        encoder.writeDIMSE(pcid, cmd, writer, pc.getTransferSyntax());
        if (!CommandUtils.isPending(cmd))
        {
            updateIdleTimeout();
            decPerforming();
        }
    }

    void onCancelRQ(DicomObject cmd) throws IOException
    {
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        DimseRSP handler = removeCancelRQHandler(msgId);
        if (handler != null)
        {
            handler.cancel(this);
        }        
    }
    
    public void registerCancelRQHandler(DicomObject cmd, DimseRSP handler)
    {
        synchronized (cancelHandlerForMsgId)
        {
            cancelHandlerForMsgId.put(cmd.getInt(Tag.MessageID), handler);
        }
    }
    
    private DimseRSP removeCancelRQHandler(int msgId)
    {
        synchronized (cancelHandlerForMsgId)
        {
            return (DimseRSP) cancelHandlerForMsgId.remove(msgId);
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
        try
        {
            out.close();
        }
        catch (IOException e)
        {
            log.warn("I/O error during close of socket output stream", e);
        }
        try
        {
            in.close();
        }
        catch (IOException e)
        {
            log.warn("I/O error during close of socket input stream", e);
        }
        if (!closed)
        {
            log.info("{}: close {}", name, socket);
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                log.warn("I/O error during close of socket", e);
            }
            closed  = true;
            onClosed();
        }
    }

    private void onClosed()
    {
        if (ae != null)
            ae.removeFromPool(this);
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
        return maxPDULength;
    }

    boolean isPackPDV()
    {
        return ae.isPackPDV();
    }

    private void startARTIM(int timeout) throws IOException
    {
        if (log.isDebugEnabled())
            log.debug(name + ": start ARTIM " + timeout + "ms");
        socket.setSoTimeout(timeout);        
    }
    
    private void stopARTIM() throws IOException
    {
        log.debug("{}: stop ARTIM", name);
        socket.setSoTimeout(0);        
    }

    void receivedAssociateRQ(AAssociateRQ rq) throws IOException
    {
        log.info("{} >> {}", name, rq);
        state.receivedAssociateRQ(this, rq);        
    }

    void receivedAssociateAC(AAssociateAC ac) throws IOException
    {
        log.info("{} >> {}", name, ac);
        state.receivedAssociateAC(this, ac);
    }

    void receivedAssociateRJ(AAssociateRJException rj) throws IOException
    {
        log.info("{} >> {}", name, rj);
        state.receivedAssociateRJ(this, rj);        
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
        log.info("{} >> A-RELEASE-RQ", name);
        state.receivedReleaseRQ(this);        
    }
   
    void receivedReleaseRP() throws IOException
    {
        log.info("{} >> A-RELEASE-RP", name);
        state.receivedReleaseRP(this);        
    }

    void receivedAbort(AAbortException aa)
    {
        log.info("{}: >> {}", name, aa);
        exception = aa;
        setState(State.STA1);
        ae.removeFromPool(this);
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
            state.sendAssociateRQ(this, rq);
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
        if (ae != null)
            ae.removeFromPool(this);
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
        name = rq.getCalledAET() + '(' + serialNo + ")";
        setState(State.STA5);
        encoder.write(rq);
    }

    void onAAssociateRQ(AAssociateRQ rq) throws IOException
    {
        associateRQ = rq;
        name = rq.getCallingAET() + '(' + serialNo + ")";
        stopARTIM();
        setState(State.STA3);
        try
        {
            if ((rq.getProtocolVersion() & 1) == 0)
                throw new AAssociateRJException(
                        AAssociateRJException.RESULT_REJECTED_PERMANENT,
                        AAssociateRJException.SOURCE_SERVICE_PROVIDER_ACSE,
                        AAssociateRJException.REASON_PROTOCOL_VERSION_NOT_SUPPORTED);
            if (!rq.getApplicationContext().equals(UID.DICOMApplicationContextName))
                throw new AAssociateRJException(
                        AAssociateRJException.RESULT_REJECTED_PERMANENT,
                        AAssociateRJException.SOURCE_SERVICE_USER,
                        AAssociateRJException.REASON_APP_CTX_NAME_NOT_SUPPORTED);
            NetworkApplicationEntity ae = connector.getDevice().getNetworkApplicationEntity(rq.getCalledAET());
            if (ae == null)
                throw new AAssociateRJException(
                        AAssociateRJException.RESULT_REJECTED_PERMANENT,
                        AAssociateRJException.SOURCE_SERVICE_USER,
                        AAssociateRJException.REASON_CALLED_AET_NOT_RECOGNIZED);
            setApplicationEntity(ae);
            ae.negotiate(this, rq);
            associateAC = ae.negotiate(this, rq);
            processAC();
            maxOpsInvoked = associateAC.getMaxOpsPerformed();
            maxPDULength = Math.min(rq.getMaxPDULength(), ae.getMaxPDULengthSend());
            setState(State.STA6);
            encoder.write(associateAC);
            updateIdleTimeout();
            reaper.register(this);
            ae.addToPool(this);
        }
        catch (AAssociateRJException e)
        {
            setState(State.STA13);
            encoder.write(e);
        }        
    }

    void onAssociateAC(AAssociateAC ac) throws IOException
    {
        associateAC = ac;
        stopARTIM();
        processAC();
        maxOpsInvoked = associateAC.getMaxOpsInvoked();
        maxPDULength = Math.min(associateAC.getMaxPDULength(), ae.getMaxPDULengthSend());
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
        log.info("{} << A-RELEASE-RP", name);
        setState(State.STA13);                
        encoder.writeAReleaseRP();
    }

    void onReleaseRQ() throws IOException
    {
        setState(State.STA8);
        if (ae != null)
            ae.removeFromPool(this);
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
