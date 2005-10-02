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
import java.io.OutputStream;

import org.dcm4che2.data.TransferSyntax;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Oct 1, 2005
 *
 */
public interface DataWriter
{
    
    void writeTo(OutputStream out, TransferSyntax ts)
    throws IOException;

}
