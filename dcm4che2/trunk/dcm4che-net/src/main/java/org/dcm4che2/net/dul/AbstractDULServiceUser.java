/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.dul;

import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 24, 2005
 *
 */
public abstract class AbstractDULServiceUser implements DULServiceUser {

    public void confirm(AAssociateAC associateAC, DULServiceProvider provider) {
        // TODO Auto-generated method stub
        
    }

    public void confirm(AAssociateRJ associateRJ, DULServiceProvider provider) {
        // TODO Auto-generated method stub
        
    }

    public void confirm(AReleaseRP releaseRP, DULServiceProvider provider) {
        // TODO Auto-generated method stub
        
    }

}
