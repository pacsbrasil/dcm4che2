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
 * @since Sep 20, 2005
 *
 */
public interface DULServiceUser {

    void confirm(AAssociateAC associateAC, DULServiceProvider provider);

    void confirm(AAssociateRJ associateRJ, DULServiceProvider provider);

    void confirm(AReleaseRP releaseRP, DULServiceProvider provider);

    void indicate(AAssociateRQ associateRQ);

}
