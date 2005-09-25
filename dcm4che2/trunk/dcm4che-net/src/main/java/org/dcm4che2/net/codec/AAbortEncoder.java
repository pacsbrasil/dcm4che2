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
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
class AAbortEncoder extends PDUEncoder {

    public AAbortEncoder() {
        super(PDUType.A_ABORT);
    }

    @Override
    protected void encodePDUBody(ProtocolSession session, PDU pdu, ByteBuffer out) {
        AAbort aabort = (AAbort) pdu;
        out.put((byte) 0);
        out.put((byte) 0);
        out.put((byte) aabort.getSource());
        out.put((byte) aabort.getReason());
    }

}
