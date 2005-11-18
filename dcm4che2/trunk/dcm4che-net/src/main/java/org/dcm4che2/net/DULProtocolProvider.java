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

import java.io.InputStream;
import java.net.SocketException;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.SessionConfig;
import org.apache.mina.io.socket.SocketSessionConfig;
import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolEncoder;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.codec.DULProtocolDecoder;
import org.dcm4che2.net.codec.DULProtocolEncoder;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 22, 2005
 */
class DULProtocolProvider
implements ProtocolProvider, ProtocolHandler, AssociationHandler
{

    static final Logger log = LoggerFactory.getLogger(DULProtocolProvider.class);
    private static final ProtocolCodecFactory CODEC_FACTORY = 
            new ProtocolCodecFactory()
            {
        
                public ProtocolEncoder newEncoder()
                {
                    return new DULProtocolEncoder();
                }
        
                public ProtocolDecoder newDecoder()
                {
                    return new DULProtocolDecoder();
                }
            };
    
    protected final DicomServiceRegistry serviceRegistry;
    protected final Executor executor;
    protected final boolean requestor;
    protected final AssociationConfig assocConfig = new AssociationConfig();
            
    public DULProtocolProvider(DicomServiceRegistry serviceRegistry,
            Executor executor, boolean requestor)
    {
        this.executor = executor;
        this.serviceRegistry = serviceRegistry;
        this.requestor = requestor;
//        this.handler = new DULProtocolHandler(executor, listener, requestor);
    }

    public ProtocolCodecFactory getCodecFactory()
    {
        return CODEC_FACTORY;
    }

    public ProtocolHandler getHandler()
    {
        return this;
    }


    public final AssociationConfig getAssocConfig()
    {
        return assocConfig;
    }
    
    public void sessionCreated(ProtocolSession session) throws Exception
    {
        Association service = new Association(executor, this, requestor,
                session);
        session.setAttachment(service);
        configureAssociation(service);
        configureSession(session);
    }

    private void configureSession(ProtocolSession session)
    {
        SessionConfig config = session.getConfig();
        if (assocConfig.getIdleTime() > 0)
            config.setIdleTime(IdleStatus.BOTH_IDLE, assocConfig.getIdleTime());
        if (assocConfig.getWriteTimeout() > 0)
            config.setWriteTimeout(assocConfig.getWriteTimeout());
        if (config instanceof SocketSessionConfig)
            configSocketSession((SocketSessionConfig) config);            
    }

    private void configSocketSession(SocketSessionConfig config)
    {
        if (assocConfig.getSessionReceiveBufferSize() > 0)
            config.setSessionReceiveBufferSize(assocConfig.getSessionReceiveBufferSize());
        if (assocConfig.getSocketReceiveBufferSize() > 0)
        {
            try
            {
                config.setReceiveBufferSize(assocConfig.getSocketReceiveBufferSize());
            } catch (SocketException e)
            {
                log.info("Failed to set ReceivedBufferSize="
                        + assocConfig.getSocketReceiveBufferSize(), e);
            }
        }
        if (assocConfig.getSocketSendBufferSize() > 0)
        {
            try
            {
                config.setSendBufferSize(assocConfig.getSocketSendBufferSize());
            } catch (SocketException e)
            {
                log.info("Failed to set SendBufferSize=" + assocConfig.getSocketSendBufferSize(), e);
            }
        }
        if (assocConfig.getSoLinger() > 0)
        {
            try
            {
                config.setSoLinger(true, assocConfig.getSoLinger());
            } catch (SocketException e)
            {
                log.info("Failed to enable SoLinger=" + assocConfig.getSoLinger(), e);
            }
        }
        if (assocConfig.getTrafficClass() > 0)
        {
            try
            {
                config.setTrafficClass(assocConfig.getTrafficClass());
            } catch (SocketException e)
            {
                log.info("Failed to set TrafficClass=" + assocConfig.getTrafficClass(), e);
            }
        }
        try
        {
            config.setTcpNoDelay(assocConfig.getTcpNoDelay());
        } catch (SocketException e)
        {
            log.info("Failed to set TcpNoDelay=" + assocConfig.getTcpNoDelay(), e);
        }
        try
        {
            config.setReuseAddress(assocConfig.getReuseAddress());
        } catch (SocketException e)
        {
            log.info("Failed to set ReuseAddress=" + assocConfig.getReuseAddress(), e);
        }
        try
        {
            config.setKeepAlive(assocConfig.getSocketKeepAlive());
        } catch (SocketException e)
        {
            log.info("Failed to set KeepAlive=" + assocConfig.getSocketKeepAlive(), e);
        }
        try
        {
            config.setOOBInline(assocConfig.getSocketOobInline());
        } catch (SocketException e)
        {
            log.info("Failed to set OOBInline=" + assocConfig.getSocketOobInline(), e);
        }
    }

    private void configureAssociation(Association a)
    {
        a.setAssociationRequestTimeout(assocConfig.getAssociationRequestTimeout());
        a.setAssociationAcceptTimeout(assocConfig.getAssociationAcceptTimeout());
        a.setReleaseResponseTimeout(assocConfig.getReleaseResponseTimeout());
        a.setPackPDV(assocConfig.getPackPDV());
    }

    public void sessionOpened(ProtocolSession session) throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.opened();
    }

    public void sessionClosed(ProtocolSession session) throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.closed();
    }

    public void sessionIdle(ProtocolSession session, IdleStatus status)
            throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.idle(status);
    }

    public void exceptionCaught(ProtocolSession session, Throwable cause)
            throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.exception(cause);
    }

    public void messageReceived(ProtocolSession session, Object message)
            throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.received((PDU) message);
    }

    public void messageSent(ProtocolSession session, Object message)
            throws Exception
    {
        Association provider = (Association) session.getAttachment();
        provider.sent((PDU) message);
    }

    public void onAReleaseRQ(Association as, AReleaseRQ rq)
    {        
        synchronized (as)
        {
            // wait for receiving A-RELEASE RP in case of release collision
            while (as.getState() == Association.STA10)
                try
                {
                    as.wait();
                } catch (InterruptedException e)
                {
                }
        }
        synchronized (this)
        {
            // finish onDIMSE, before sending release response
            as.write(new AReleaseRP());
        }
    }

    public synchronized void onDIMSE(Association as, int pcid,
            DicomObject cmd, InputStream dataStream)
    {
        if (CommandFactory.isResponse(cmd))
            as.onDimseRSP(pcid, cmd, dataStream);
        else if (CommandFactory.isCancelRQ(cmd))
            as.onCancelRQ(pcid, cmd, dataStream);
        else
            serviceRegistry.process(as, pcid, cmd, dataStream);
    }

    public void onOpened(Association as)
    {
    }

    public void onAAssociateRQ(Association as, AAssociateRQ rq)
    {
    }

    public void onAAssociateAC(Association as, AAssociateAC ac)
    {
    }

    public void onAAssociateRJ(Association as, AAssociateRJ rj)
    {
    }

    public void onAReleaseRP(Association as, AReleaseRP rp)
    {
    }

    public void onAbort(Association as, AAbort abort)
    {
    }

    public void onClosed(Association association)
    {
    }
}
