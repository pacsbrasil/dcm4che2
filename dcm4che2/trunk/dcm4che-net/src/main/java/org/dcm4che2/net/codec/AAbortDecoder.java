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
 *
 */
public class AAbortDecoder extends PDUDecoder {

    public AAbortDecoder() {
        super(PDUType.A_ABORT);
    }

    @Override
    protected PDU decodePDU(ProtocolSession session, ByteBuffer in) {
        in.getShort(); // skip reserved bytes 7,8
        final int source = in.getUnsigned();
        final int reason = in.getUnsigned();
        return new AAbort(source, reason);
    }

}
