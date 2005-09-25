/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.dul;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 22, 2005
 *
 */
public class DULProtocolHandler implements ProtocolHandler {
    
    private final boolean acceptor;
    private final DULServiceUser user;
    private long associationRequestTimeout = 10000L;
    
    public DULProtocolHandler(DULServiceUser user, boolean acceptor) {
        this.user = user;
        this.acceptor = acceptor;
    }

    public final long getAssociationRequestTimeout() {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(long associationRequestTimeout) {
        this.associationRequestTimeout = associationRequestTimeout;
    }

    public void sessionCreated(ProtocolSession session) throws Exception {
        DULServiceProvider service = 
            new DULServiceProvider(user, session, acceptor);
        service.setAssociationRequestTimeout(associationRequestTimeout);
        session.setAttachment(service);
     }

    public void sessionOpened(ProtocolSession session) throws Exception {
        DULServiceProvider provider = (DULServiceProvider) session.getAttachment();
        provider.opened();      
    }

    public void sessionClosed(ProtocolSession session) throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void sessionIdle(ProtocolSession session, IdleStatus status) throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void exceptionCaught(ProtocolSession session, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void messageReceived(ProtocolSession session, Object message) throws Exception {
        DULServiceProvider provider = (DULServiceProvider) session.getAttachment();
        provider.received((PDU) message);        
    }

    public void messageSent(ProtocolSession session, Object message) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
