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
import java.net.SocketAddress;

import org.apache.log4j.Logger;
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
public class AssociationAcceptor {
    
    static final Logger log = Logger.getLogger(AssociationAcceptor.class);

    private final Executor executor;
    private final SocketAcceptor socketIoAcceptor = new SocketAcceptor();
    private final IoProtocolAcceptor acceptor = 
            new IoProtocolAcceptor(socketIoAcceptor );
    private long associationRequestTimeout = 1000;

    public AssociationAcceptor(Executor executor)
    {
        if (executor == null)
            throw new NullPointerException();

        this.executor = executor;
    }

    public AssociationAcceptor()
    {
        this(new NewThreadExecutor("Association"));
    }

    public final long getAssociationRequestTimeout() {
        return associationRequestTimeout;
    }

    public final void setAssociationRequestTimeout(long timeout) {
        this.associationRequestTimeout = timeout;
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
    
    public void bind(AssociationHandler listener, SocketAddress address)
    throws IOException {
        DULProtocolProvider provider =
                new DULProtocolProvider(executor, listener, false);
        provider.setAssociationRequestTimeout(associationRequestTimeout);
        log.debug("Start Acceptor listening on " + address);
        acceptor.bind(address, provider);
    }

    public void unbind(SocketAddress address) {
        log.debug("Stop Acceptor listening on " + address);
        acceptor.unbind(address);
    }

    
}
