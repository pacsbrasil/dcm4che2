/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import org.apache.mina.protocol.codec.DemuxingProtocolCodecFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class DULProtocolCodecFactory extends DemuxingProtocolCodecFactory {
    
    public DULProtocolCodecFactory() {
        super.register(AAssociateRQDecoder.class);
        super.register(AAssociateRQEncoder.class);
        super.register(AAssociateACDecoder.class);
        super.register(AAssociateACEncoder.class);
        super.register(AAssociateRJDecoder.class);
        super.register(AAssociateRJEncoder.class);
        super.register(PDataTFDecoder.class);
        super.register(PDataTFEncoder.class);
        super.register(AReleaseRQDecoder.class);
        super.register(AReleaseRQEncoder.class);
        super.register(AReleaseRPDecoder.class);
        super.register(AReleaseRPEncoder.class);
        super.register(AAbortDecoder.class);
        super.register(AAbortEncoder.class);
    }

}
