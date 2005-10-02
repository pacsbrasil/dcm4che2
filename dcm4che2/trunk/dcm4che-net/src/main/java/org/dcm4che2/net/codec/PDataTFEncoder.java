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
import org.dcm4che2.net.pdu.PDataTF;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public class PDataTFEncoder extends PDUEncoder
{

    public PDataTFEncoder()
    {
        super(PDUType.P_DATA_TF);
    }

    @Override
    protected void encodePDUBody(ProtocolSession session, PDU pdu,
            ByteBuffer out)
    {
        PDataTF tf = (PDataTF) pdu;
        out.put(tf.getByteBuffer());
    }

}
