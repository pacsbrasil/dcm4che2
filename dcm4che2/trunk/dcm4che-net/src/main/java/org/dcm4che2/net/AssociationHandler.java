/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

import java.io.InputStream;

import org.dcm4che2.data.DicomObject;
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
public interface AssociationHandler {

    void onOpened(Association as);

    void onAAssociateRQ(Association as, AAssociateRQ rq);

    void onAAssociateAC(Association as, AAssociateAC ac);

    void onAAssociateRJ(Association as, AAssociateRJ rj);

    void onAReleaseRQ(Association as, AReleaseRQ rq);

    void onAReleaseRP(Association as, AReleaseRP rp);

    void onAbort(Association as, AAbort abort);

    void onDIMSE(Association as, int pcid, DicomObject command,
            InputStream dataStream);

    void onClosed(Association association);

}
