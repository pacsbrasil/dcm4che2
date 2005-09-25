/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.common.ByteBuffer;
import org.dcm4che2.net.pdu.PresentationContext;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
class AAssociateRQEncoder extends AAssociateRQACEncoder {

    public AAssociateRQEncoder() {
        super(PDUType.A_ASSOCIATE_RQ);
    }

    @Override
    protected void encodePCs(Collection pcs, ByteBuffer out) {
        int pcid = 1;
        for (Iterator it = pcs.iterator(); it.hasNext(); ++pcid, ++pcid) {
            PresentationContext pc = (PresentationContext) it.next();
            pc.setPCID(pcid);
            pc.setResult(0);
            encodePC(ItemType.RQ_PRES_CONTEXT, pc, out);
        }
    }
}
