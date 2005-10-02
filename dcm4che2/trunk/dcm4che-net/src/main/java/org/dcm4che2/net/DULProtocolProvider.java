/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolDecoder;
import org.apache.mina.protocol.ProtocolEncoder;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolProvider;
import org.dcm4che2.net.codec.DULProtocolDecoder;
import org.dcm4che2.net.codec.DULProtocolEncoder;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 22, 2005
 */
public class DULProtocolProvider implements ProtocolProvider
{

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

    private final DULProtocolHandler handler;

    public DULProtocolProvider(Executor executor, AssociationHandler listener,
            boolean requestor)
    {
        this.handler = new DULProtocolHandler(executor, listener, requestor);
    }

    public ProtocolCodecFactory getCodecFactory()
    {
        return CODEC_FACTORY;
    }

    public ProtocolHandler getHandler()
    {
        return handler;
    }

    public final long getAssociationRequestTimeout()
    {
        return handler.getAssociationRequestTimeout();
    }

    public final void setAssociationRequestTimeout(long timeout)
    {
        handler.setAssociationRequestTimeout(timeout);
    }

}
