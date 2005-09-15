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
import org.apache.mina.protocol.ProtocolDecoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;
import org.apache.mina.protocol.codec.MessageDecoder;
import org.apache.mina.protocol.codec.MessageDecoderResult;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public abstract class PDUDecoder implements MessageDecoder {

    private final int type;

    private int length;

    private boolean readHeader;

    private static final int HEADER_LEN = 6;

    protected PDUDecoder(int type) {
        this.type = type;
    }

    public MessageDecoderResult decodable(ProtocolSession session, ByteBuffer in) {
        if (in.remaining() < HEADER_LEN)
            return MessageDecoderResult.NEED_DATA;

        if (type == in.getUnsigned())
            return MessageDecoderResult.OK;

        return MessageDecoderResult.NOT_OK;
    }

    public MessageDecoderResult decode(ProtocolSession session, ByteBuffer in,
            ProtocolDecoderOutput out) throws ProtocolViolationException {
        if (!readHeader) {
            in.getShort();
            length = in.getInt();
            readHeader = true;
        }
        if( in.remaining() < length)
            return MessageDecoderResult.NEED_DATA;

        out.write(decodePDU(session, in));
        readHeader = false;

        return MessageDecoderResult.OK;
    }

    protected abstract PDU decodePDU(ProtocolSession session, ByteBuffer in);
}
