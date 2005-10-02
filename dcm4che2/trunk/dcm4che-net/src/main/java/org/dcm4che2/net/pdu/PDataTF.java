/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

import org.apache.mina.common.ByteBuffer;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public class PDataTF implements PDU
{

    private final ByteBuffer buf;

    public PDataTF(ByteBuffer buf)
    {
        this.buf = buf;
    }

    public final ByteBuffer getByteBuffer()
    {
        return buf;
    }

    public int length()
    {
        return buf.remaining();
    }

    public String toString()
    {
        return "P-DATA_TF[len = " + length() + "]";
    }

}
