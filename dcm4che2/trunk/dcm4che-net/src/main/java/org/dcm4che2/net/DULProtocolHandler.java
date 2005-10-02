/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

import org.apache.mina.common.IdleStatus;
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

    private final Executor executor;
    private final AssociationHandler listener;
    private final boolean requestor;
    private long associationRequestTimeout = 10000L;

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

    public final void setAssociationRequestTimeout(
            long associationRequestTimeout)
    {
        this.associationRequestTimeout = associationRequestTimeout;
    }

    public void sessionCreated(ProtocolSession session) throws Exception
    {
        Association service = new Association(executor, listener, requestor,
                session);
        service.setAssociationRequestTimeout(associationRequestTimeout);
        session.setAttachment(service);
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
