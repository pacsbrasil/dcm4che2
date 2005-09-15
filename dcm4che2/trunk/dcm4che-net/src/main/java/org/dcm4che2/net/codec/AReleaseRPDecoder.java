/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class AReleaseRPDecoder extends PDUDecoder {

    public AReleaseRPDecoder() {
        super(PDUType.A_RELEASE_RP);
    }

    @Override
    protected PDU decodePDU(ProtocolSession session, ByteBuffer in) {
        return AReleaseRP.getAReleaseRP();
    }

}
