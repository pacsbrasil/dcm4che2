/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.pdu;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public class AAbort implements PDU {
    
    private final int source;
    private final int reason;

    public AAbort(int source, int reason) {
        this.source = source;
        this.reason = reason;
    }
    
    public final int length() {
        return 4;
    }
    
    public final int source() {
        return source;
    }

    public final int reason() {
        return reason;
    }

}
