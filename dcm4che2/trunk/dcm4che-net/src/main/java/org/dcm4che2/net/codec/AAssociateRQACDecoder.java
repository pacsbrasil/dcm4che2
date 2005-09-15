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
import org.dcm4che2.net.pdu.AAssociateRQAC;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public abstract class AAssociateRQACDecoder extends PDUDecoder {

    protected AAssociateRQACDecoder(int type) {
        super(type);
    }

    protected void decodePDU(ProtocolSession session, ByteBuffer in, AAssociateRQAC rqac) {
        // TODO Auto-generated method stub
        
    }

}
