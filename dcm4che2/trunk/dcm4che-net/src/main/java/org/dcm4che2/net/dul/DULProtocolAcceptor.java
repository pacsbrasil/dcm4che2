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
import java.net.SocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.io.socket.SocketAcceptor;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.apache.mina.protocol.io.IoProtocolAcceptor;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 24, 2005
 *
 */
public class DULProtocolAcceptor {

    private final SocketAcceptor socketIoAcceptor = new SocketAcceptor();
    private final IoProtocolAcceptor acceptor = 
            new IoProtocolAcceptor(socketIoAcceptor );
    private int associationRequestTimer = 10;

    public final int getAssociationRequestTimer() {
        return associationRequestTimer;
    }

    public final void setAssociationRequestTimer(int associationRequestTimer) {
        this.associationRequestTimer = associationRequestTimer;
    }
    
    public void setIoThreadPoolFilter(IoThreadPoolFilter ioThreadPoolFilter) {
        if (ioThreadPoolFilter != null) {
            socketIoAcceptor.getFilterChain().addFirst("threadPool",
                    ioThreadPoolFilter );
        } else {
            socketIoAcceptor.getFilterChain().remove("threadPool");
        }
    }
    
    public void setProtocolThreadPoolFilter(
            ProtocolThreadPoolFilter protocolThreadPoolFilter) {
        if (protocolThreadPoolFilter != null) {
            acceptor.getFilterChain().addFirst("threadPool",
                    protocolThreadPoolFilter );
        } else {
            acceptor.getFilterChain().remove("threadPool");
        }
    }

    public int getBacklog() {
        return socketIoAcceptor.getBacklog();
    }

    public void setBacklog(int defaultBacklog) {
        socketIoAcceptor.setBacklog(defaultBacklog);
    }
    
    public void bind(DULServiceUser user, SocketAddress address)
    throws IOException {
        DULProtocolProvider provider = new DULProtocolProvider(user, true);
        provider.setAssociationRequestTimeout(associationRequestTimer);
        acceptor.bind(address, provider);
    }

    public void unbind(SocketAddress address) {
        acceptor.unbind(address);
    }

    
}
