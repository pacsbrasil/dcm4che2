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
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class AAssociateRQDecoder extends AAssociateRQACDecoder {

    public AAssociateRQDecoder() {
        super(PDUType.A_ASSOCIATE_RQ);
    }

    @Override
    protected PDU decodePDU(ProtocolSession session, ByteBuffer in) {
        AAssociateRQ rq = new AAssociateRQ();
        decodePDU(session, in, rq);
        return rq;
    }

}
