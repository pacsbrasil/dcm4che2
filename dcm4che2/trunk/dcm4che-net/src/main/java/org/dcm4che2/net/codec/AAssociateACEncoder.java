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
class AAssociateACEncoder extends AAssociateRQACEncoder {

    public AAssociateACEncoder() {
        super(PDUType.A_ASSOCIATE_AC);
    }

    @Override
    protected void encodePCs(Collection pcs, ByteBuffer out) {
        for (Iterator it = pcs.iterator(); it.hasNext();) {
            PresentationContext pc = (PresentationContext) it.next();
            encodePC(ItemType.AC_PRES_CONTEXT, pc, out);
        }
    }

}
