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
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.PDU;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
class AAssociateRJEncoder extends PDUEncoder
{

    public AAssociateRJEncoder()
    {
        super(PDUType.A_ASSOCIATE_RJ);
    }

    @Override
    protected void encodePDUBody(ProtocolSession session, PDU pdu,
            ByteBuffer out)
    {
        AAssociateRJ rj = (AAssociateRJ) pdu;
        out.put((byte) 0);
        out.put((byte) rj.getReason());
        out.put((byte) rj.getSource());
        out.put((byte) rj.getReason());
    }

}
