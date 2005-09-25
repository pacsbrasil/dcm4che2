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
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
class AAbortDecoder implements PDUDecoder {

    public PDU decodePDU(ProtocolSession session, ByteBuffer in, int length)
    throws ProtocolViolationException {
        AAbort pdu = new AAbort();
        if (length != 4)
            throw new DULProtocolViolationException(
                    AAbort.INVALID_PDU_PARAMETER_VALUE, 
                    "Invalid PDU-length of A-ABORT: " + length);
        
        in.get(); // skip reserved byte 7
        in.get(); // skip reserved byte 8
        pdu.setSource(in.get() & 0xff);
        pdu.setReason(in.get() & 0xff);
        return pdu;
    }

}
