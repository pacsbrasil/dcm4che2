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
 * @since Sep 16, 2005
 *
 */
public class ExtendedNegotiation {

    private final String cuid;
    private final byte[] info;

    public ExtendedNegotiation(String cuid, byte[] info) {
        if (cuid == null)
            throw new NullPointerException();
        
        this.cuid = cuid;
        this.info = info.clone();
    }

    public int itemLength() {
        return cuid.length() + info.length;
    }
    
    public final String getSOPClassUID() {
        return cuid;
    }
    
    public final byte[] getInformation() {
        return info.clone();
    }
}
