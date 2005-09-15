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
public class RoleSelection {
    
    private final String cuid;
    private final boolean scu;
    private final boolean scp;

    public RoleSelection(String cuid, boolean scu, boolean scp) {
        if (cuid == null)
            throw new NullPointerException();
        
        this.cuid = cuid;
        this.scu = scu;
        this.scp = scp;
    }

    public int itemLength() {
        return cuid.length() + 4;
    }
    
    public final String getSOPClassUID() {
        return cuid;
    }

    public final boolean scu() {
        return scu;
    }

    public final boolean scp() {
        return scp;
    }

}
