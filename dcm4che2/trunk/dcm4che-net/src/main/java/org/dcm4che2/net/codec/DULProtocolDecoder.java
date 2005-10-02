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
import org.apache.mina.protocol.codec.CumulativeProtocolDecoder;
import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateRQAC;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 19, 2005
 */
public class DULProtocolDecoder extends CumulativeProtocolDecoder
{

    private static final int HEADER_LEN = 6;

    private final PDUDecoder[] decoder =
    { new AAssociateRQDecoder(), new AAssociateACDecoder(),
            new AAssociateRJDecoder(), new PDataTFDecoder(),
            new AReleaseRQDecoder(), new AReleaseRPDecoder(),
            new AAbortDecoder() };

    private boolean readHeader;

    private int type;

    private int length;

    public DULProtocolDecoder()
    {
        super(AAssociateRQAC.DEF_MAX_PDU_LENGTH + 6);
    }

    @Override
    protected boolean doDecode(ProtocolSession session, ByteBuffer in,
            ProtocolDecoderOutput out) throws ProtocolViolationException
    {

        if (in.remaining() < HEADER_LEN)
            return false;

        if (!readHeader)
        {
            type = in.get() & 0xff;
            if (type == 0 || type > PDUType.A_ABORT)
                throw new DULProtocolViolationException(
                        AAbort.UNRECOGNIZED_PDU, "Unkown PDU type: " + type);

            in.get(); // reserved byte
            length = in.getInt();
            readHeader = true;
        }

        if (in.remaining() < length)
            return false;

        out.write(decoder[type - 1].decodePDU(session, in, length));
        readHeader = false;
        return true;
    }

}
