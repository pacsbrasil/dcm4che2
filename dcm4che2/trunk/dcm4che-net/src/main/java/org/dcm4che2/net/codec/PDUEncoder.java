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
import org.apache.mina.protocol.ProtocolEncoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.codec.MessageEncoder;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public abstract class PDUEncoder implements MessageEncoder {
    private final int type;

    protected PDUEncoder(int type) {
        this.type = type;
    }

    public void encode(ProtocolSession session, Object message,
            ProtocolEncoderOutput out) {
        PDU pdu = (PDU) message;
        final int pdulen = pdu.length();
        ByteBuffer buf = ByteBuffer.allocate(pdulen + 6);
        buf.putShort((short) type);
        buf.putInt(pdu.length());
        encodePDU(session, pdu, buf);
        
        buf.flip();
        out.write(buf);
    }

    protected abstract void encodePDU(ProtocolSession session, PDU pdu,
            ByteBuffer out);

}
