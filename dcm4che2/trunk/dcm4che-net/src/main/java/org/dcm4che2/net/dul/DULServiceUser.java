/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.dul;

import org.dcm4che2.net.pdu.AAbort;
import org.dcm4che2.net.pdu.AAssociateAC;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.net.pdu.AAssociateRQ;
import org.dcm4che2.net.pdu.AReleaseRP;
import org.dcm4che2.net.pdu.AReleaseRQ;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 20, 2005
 *
 */
public interface DULServiceUser {

    void onOpened(DULServiceProvider provider);

    void onAAssociateRQ(DULServiceProvider provider, AAssociateRQ associateRQ);

    void onAAssociateAC(DULServiceProvider provider, AAssociateAC associateAC);

    void onAAssociateRJ(DULServiceProvider provider, AAssociateRJ associateRJ);

    void onAReleaseRQ(DULServiceProvider provider, AReleaseRQ releaseRQ);

    void onAReleaseRP(DULServiceProvider provider, AReleaseRP releaseRP);

    void onAbort(AAbort abort);

}
