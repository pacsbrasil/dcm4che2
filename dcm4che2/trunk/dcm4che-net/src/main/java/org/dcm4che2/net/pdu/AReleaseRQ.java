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
 */
public class AReleaseRQ implements PDU
{

    public int length()
    {
        return 4;
    }

    public String toString()
    {
        return "A-RELEASE-RQ";
    }
}
