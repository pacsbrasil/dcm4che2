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
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
class AReleaseRPEncoder extends PDUEncoder {

    public AReleaseRPEncoder() {
        super(PDUType.A_RELEASE_RP);
    }

    @Override
    protected void encodePDUBody(ProtocolSession session, PDU pdu, ByteBuffer out) {
        out.put((byte) 0);
        out.put((byte) 0);
        out.put((byte) 0);
        out.put((byte) 0);
    }

}
