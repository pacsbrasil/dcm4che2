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

import java.net.SocketException;

import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.SessionConfig;
import org.apache.mina.io.socket.SocketSessionConfig;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 22, 2005
 */
public class DULProtocolHandler implements ProtocolHandler
{

    static final Logger log = Logger.getLogger(DULProtocolHandler.class);

    private final Executor executor;
    private final AssociationHandler listener;
    private final boolean requestor;
    private long associationRequestTimeout = 10000L;
    private long associationAcceptTimeout = 10000L;
    private long releaseResponseTimeout = 10000L;
    private long socketCloseDelay = 100L;
    private int pdvPipeBufferSize = 1024;
    private boolean packPDV = true;
    private int idleTime = 0;
    private int writeTimeout = 0;
    private int receiveBufferSize;
    private int sendBufferSize;
    private int sessionReceiveBufferSize;
    private int soLinger = -1;
    private int trafficClass;
    private boolean oobInline;
    private boolean keepAlive;
    private boolean tcpNoDelay;
    private boolean reuseAddress = true;


    public DULProtocolHandler(Executor threadPool, AssociationHandler listener,
            boolean requestor)
    {
        this.executor = threadPool;
        this.listener = listener;
        this.requestor = requestor;
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

    public final int getPDVPipeBufferSize()
    {
        return pdvPipeBufferSize;
    }

    public final void setPDVPipeBufferSize(int bufferSize)
    {
        this.pdvPipeBufferSize = bufferSize;
    }

    public final boolean isPackPDV()
    {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV)
    {
        this.packPDV = packPDV;
    }

    public final long getReleaseResponseTimeout()
    {
        return releaseResponseTimeout;
    }

    public final void setReleaseResponseTimeout(long timeout)
    {
        this.releaseResponseTimeout = timeout;
    }

    public final long getSocketCloseDelay()
    {
        return socketCloseDelay;
    }

    public final void setSocketCloseDelay(long socketCloseDelay)
    {
        this.socketCloseDelay = socketCloseDelay;
    }

    public final int getIdleTime()
    {
        return idleTime;
    }

    public final void setIdleTime(int timeout)
    {
        this.idleTime = timeout;
    }

    public final boolean getKeepAlive()
    {
        return keepAlive;
    }

    public final void setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
    }

    public final boolean getOOBInline()
    {
        return oobInline;
    }

    public final void setOOBInline(boolean oobInline)
    {
        this.oobInline = oobInline;
    }

    public final int getReceiveBufferSize()
    {
        return receiveBufferSize;
    }

    public final void setReceiveBufferSize(int receiveBufferSize)
    {
        this.receiveBufferSize = receiveBufferSize;
    }

    public final boolean getReuseAddress()
    {
        return reuseAddress;
    }

    public final void setReuseAddress(boolean reuseAddress)
    {
        this.reuseAddress = reuseAddress;
    }

    public final int getSendBufferSize()
    {
        return sendBufferSize;
    }

    public final void setSendBufferSize(int sendBufferSize)
    {
        this.sendBufferSize = sendBufferSize;
    }

    public final int getSessionReceiveBufferSize()
    {
        return sessionReceiveBufferSize;
    }

    public final void setSessionReceiveBufferSize(int sessionReceiveBufferSize)
    {
        this.sessionReceiveBufferSize = sessionReceiveBufferSize;
    }

    public final int getSoLinger()
    {
        return soLinger;
    }

    public final void setSoLinger(int soLinger)
    {
        this.soLinger = soLinger;
    }

    public final boolean getTcpNoDelay()
    {
        return tcpNoDelay;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay)
    {
        this.tcpNoDelay = tcpNoDelay;
    }

    public final int getTrafficClass()
    {
        return trafficClass;
    }

    public final void setTrafficClass(int trafficClass)
    {
        this.trafficClass = trafficClass;
    }

    public final int getWriteTimeout()
    {
        return writeTimeout;
    }

    public final void setWriteTimeout(int writeTimeout)
    {
        this.writeTimeout = writeTimeout;
    }

    public void sessionCreated(ProtocolSession session) throws Exception
    {
        Association service = new Association(executor, listener, requestor,
                session);
        session.setAttachment(service);
        configureAssociation(service);
        configureSession(session);
    }

    private void configureSession(ProtocolSession session)
    {
        SessionConfig config = session.getConfig();
        if (idleTime > 0)
            config.setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
        if (writeTimeout > 0)
            config.setWriteTimeout(writeTimeout);
        if (config instanceof SocketSessionConfig)
            configSocketSession((SocketSessionConfig) config);            
    }

    private void configSocketSession(SocketSessionConfig config)
    {
        if (sessionReceiveBufferSize > 0)
            config.setSessionReceiveBufferSize(sessionReceiveBufferSize);
        if (receiveBufferSize > 0)
        {
            try
            {
                config.setReceiveBufferSize(receiveBufferSize);
            } catch (SocketException e)
            {
                log.info("Failed to set ReceivedBufferSize="
                        + receiveBufferSize, e);
            }
        }
        if (sendBufferSize > 0)
        {
            try
            {
                config.setSendBufferSize(sendBufferSize);
            } catch (SocketException e)
            {
                log.info("Failed to set SendBufferSize=" + sendBufferSize, e);
            }
        }
        if (soLinger > 0)
        {
            try
            {
                config.setSoLinger(true, soLinger);
            } catch (SocketException e)
            {
                log.info("Failed to enable SoLinger=" + soLinger, e);
            }
        }
        if (trafficClass > 0)
        {
            try
            {
                config.setTrafficClass(trafficClass);
            } catch (SocketException e)
            {
                log.info("Failed to set TrafficClass=" + trafficClass, e);
            }
        }
        try
        {
            config.setTcpNoDelay(tcpNoDelay);
        } catch (SocketException e)
        {
            log.info("Failed to set TcpNoDelay=" + tcpNoDelay, e);
        }
        try
        {
            config.setReuseAddress(reuseAddress);
        } catch (SocketException e)
        {
            log.info("Failed to set ReuseAddress=" + reuseAddress, e);
        }
        try
        {
            config.setKeepAlive(keepAlive);
        } catch (SocketException e)
        {
            log.info("Failed to set KeepAlive=" + keepAlive, e);
        }
        try
        {
            config.setOOBInline(oobInline);
        } catch (SocketException e)
        {
            log.info("Failed to set OOBInline=" + oobInline, e);
        }
    }

    private void configureAssociation(Association a)
    {
        a.setAssociationRequestTimeout(associationRequestTimeout);
        a.setAssociationAcceptTimeout(associationAcceptTimeout);
        a.setReleaseResponseTimeout(releaseResponseTimeout);
        a.setPDVPipeBufferSize(pdvPipeBufferSize);
        a.setPackPDV(packPDV);
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
}
