package org.dcm4che2.net;

import java.io.IOException;
import java.net.SocketAddress;

import org.apache.mina.io.filter.IoThreadPoolFilter;
import org.apache.mina.io.socket.SocketConnector;
import org.apache.mina.protocol.ProtocolProvider;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.filter.ProtocolThreadPoolFilter;
import org.apache.mina.protocol.io.IoProtocolConnector;

public class AssociationRequestor
{

    private final SocketConnector socketIoConnector = new SocketConnector();

    private final IoProtocolConnector connector = new IoProtocolConnector(
            socketIoConnector);

    private final Executor executor;

    public AssociationRequestor(Executor executor)
    {
        if (executor == null)
            throw new NullPointerException();

        this.executor = executor;
    }

    public AssociationRequestor()
    {
        this(new NewThreadExecutor("Association"));
    }

    public final void setIoThreadPoolFilter(
            IoThreadPoolFilter ioThreadPoolFilter)
    {
        if (ioThreadPoolFilter != null)
        {
            socketIoConnector.getFilterChain().addFirst("threadPool",
                    ioThreadPoolFilter);
        } else
        {
            socketIoConnector.getFilterChain().remove("threadPool");
        }
    }

    public final void setProtocolThreadPoolFilter(
            ProtocolThreadPoolFilter protocolThreadPoolFilter)
    {
        if (protocolThreadPoolFilter != null)
        {
            connector.getFilterChain().addFirst("threadPool",
                    protocolThreadPoolFilter);
        } else
        {
            connector.getFilterChain().remove("threadPool");
        }
    }

    public Association connect(AssociationHandler listener,
            SocketAddress address) throws IOException
    {
        return connect(listener, address, null, Integer.MAX_VALUE);
    }

    public Association connect(AssociationHandler listener,
            SocketAddress address, SocketAddress localAddress)
            throws IOException
    {
        return connect(listener, address, localAddress, Integer.MAX_VALUE);
    }

    public Association connect(AssociationHandler listener,
            SocketAddress address, int timeout) throws IOException
    {
        return connect(listener, address, null, timeout);
    }

    public Association connect(AssociationHandler listener,
            SocketAddress address, SocketAddress localAddress, int timeout)
            throws IOException
    {
        ProtocolProvider provider = new DULProtocolProvider(executor,
                listener, true);
        ProtocolSession session = connector.connect(address, localAddress,
                timeout, provider);
        return (Association) session.getAttachment();
    }

}
