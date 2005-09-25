package org.dcm4che2.net.dul;

import java.io.IOException;
import java.net.SocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.io.socket.SocketConnector;
import org.apache.mina.protocol.ProtocolProvider;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.apache.mina.protocol.io.IoProtocolConnector;

public class DULProtocolConnector {
    
    private final  SocketConnector socketIoConnector = new SocketConnector();
    private final IoProtocolConnector connector = 
            new IoProtocolConnector(socketIoConnector );
    
    public final void setIoThreadPoolFilter(IoThreadPoolFilter ioThreadPoolFilter) {
        if (ioThreadPoolFilter != null) {
            socketIoConnector.getFilterChain().addFirst("threadPool",
                    ioThreadPoolFilter );
        } else {
            socketIoConnector.getFilterChain().remove("threadPool");
        }
    }
    
    public final void setProtocolThreadPoolFilter(
            ProtocolThreadPoolFilter protocolThreadPoolFilter) {
        if (protocolThreadPoolFilter != null) {
            connector.getFilterChain().addFirst("threadPool",
                    protocolThreadPoolFilter );
        } else {
            connector.getFilterChain().remove("threadPool");
        }
    }

    public DULServiceProvider connect(DULServiceUser user,
            SocketAddress address)
    throws IOException {
        return connect(user, address, null, Integer.MAX_VALUE);
    }

    public DULServiceProvider connect(DULServiceUser user,
            SocketAddress address, SocketAddress localAddress)
    throws IOException {
        return connect(user, address, localAddress, Integer.MAX_VALUE);
    }

    public DULServiceProvider connect(DULServiceUser user,
            SocketAddress address, int timeout)
    throws IOException {
        return connect(user, address, null, timeout);
    }
    
    public DULServiceProvider connect(DULServiceUser user,
            SocketAddress address, SocketAddress localAddress, int timeout)
    throws IOException {
        ProtocolProvider provider = new DULProtocolProvider(user, false);
        ProtocolSession session = connector.connect(address, localAddress,
                timeout, provider );
        return (DULServiceProvider) session.getAttachment();
    }


}
