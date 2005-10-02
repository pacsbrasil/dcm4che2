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
import org.apache.mina.protocol.ProtocolViolationException;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PDataTF;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public class PDataTFDecoder implements PDUDecoder
{

    public PDU decodePDU(ProtocolSession session, ByteBuffer in, int length)
            throws ProtocolViolationException
    {
        ByteBuffer buf = ByteBuffer.allocate(in.remaining(), false);
        buf.put(in);
        buf.flip();
        return new PDataTF(buf);
    }
}
