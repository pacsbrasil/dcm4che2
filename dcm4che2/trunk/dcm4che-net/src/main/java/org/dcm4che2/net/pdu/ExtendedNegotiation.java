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
 */
public class ExtendedNegotiation
{

    private String cuid;
    private byte[] info;

    public final String getSOPClassUID()
    {
        return cuid;
    }

    public final void setSOPClassUID(String cuid)
    {
        if (cuid == null)
            throw new NullPointerException();

        this.cuid = cuid;
    }

    public final byte[] getInformation()
    {
        return info.clone();
    }

    public final void setInformation(byte[] info)
    {
        this.info = info.clone();
    }

    public int length()
    {
        if (cuid == null)
            throw new IllegalStateException();

        return cuid.length() + info.length;
    }

}
