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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.PresentationContext;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
public class AAssociateACEncoder extends AAssociateRQACEncoder {

    public AAssociateACEncoder() {
        super(PDUType.A_ASSOCIATE_AC);
    }

    private static final Set TYPES;

    static {
        Set types = new HashSet();
        types.add(AAssociateAC.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    public Set getMessageTypes() {
        return TYPES;
    }

    @Override
    protected void encodePCs(Collection pcs, ByteBuffer out) {
        for (Iterator it = pcs.iterator(); it.hasNext();) {
            PresentationContext pc = (PresentationContext) it.next();
            encodePC(ItemType.AC_PRES_CTX, pc, out);
        }
    }

}
