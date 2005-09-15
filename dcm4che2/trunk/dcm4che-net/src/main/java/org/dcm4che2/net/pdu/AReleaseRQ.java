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
public class AReleaseRQ implements PDU {
    
    private static AReleaseRQ instance = new AReleaseRQ();

    public static PDU getAReleaseRQ() {
        return instance;
    }
    
    private AReleaseRQ() {}

    public int length() {
        return 4;
    }

}
