/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net;

import java.io.IOException;

import org.dcm4che2.net.pdu.AAbort;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 30, 2005
 *
 */
public class AbortException extends IOException
{
    private static final long serialVersionUID = 3258131375163781169L;

    private final AAbort aabort;

    public AbortException(AAbort aabort)
    {
        super(aabort.toString());
        this.aabort = aabort;
    }

    public final AAbort getAAbort()
    {
        return aabort;
    }

}
