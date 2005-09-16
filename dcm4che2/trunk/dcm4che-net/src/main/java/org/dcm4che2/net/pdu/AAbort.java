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
    
    private int source;
    private int reason;

    public final int length() {
        return 4;
    }

    public final int getReason() {
        return reason;
    }

    public final void setReason(int reason) {
        this.reason = reason;
    }

    public final int getSource() {
        return source;
    }

    public final void setSource(int source) {
        this.source = source;
    }

}
