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
public abstract class PDUEncoder {

    private final int type;
    
    public PDUEncoder(int type) {
        this.type = type;
    }
    
    public ByteBuffer encodePDU(ProtocolSession session, PDU pdu) {
        int pdulen = pdu.length();
        ByteBuffer buf = ByteBuffer.allocate(pdulen + 6);
        buf.put((byte) type);
        buf.put((byte) 0);
        buf.putInt(pdulen);
        encodePDUBody(session, pdu, buf);
        return buf;
    }

    protected abstract void encodePDUBody(ProtocolSession session, PDU pdu,
            ByteBuffer buf);
}
