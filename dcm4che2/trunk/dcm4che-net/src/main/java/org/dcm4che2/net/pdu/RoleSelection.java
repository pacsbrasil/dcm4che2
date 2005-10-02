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
public class RoleSelection
{

    private String cuid;
    private boolean scu;
    private boolean scp;

    public final String getSOPClassUID()
    {
        return cuid;
    }

    public final void setSOPClassUID(String cuid)
    {
        this.cuid = cuid;
    }

    public final boolean isSCU()
    {
        return scu;
    }

    public final void setSCU(boolean scu)
    {
        this.scu = scu;
    }

    public final boolean isSCP()
    {
        return scp;
    }

    public final void setSCP(boolean scp)
    {
        this.scp = scp;
    }

    public int length()
    {
        if (cuid == null)
            throw new IllegalStateException();

        return cuid.length() + 4;
    }
}
