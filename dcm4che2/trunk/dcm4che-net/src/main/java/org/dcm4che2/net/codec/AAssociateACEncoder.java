/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
class AAssociateACEncoder extends AAssociateRQACEncoder
{

    public AAssociateACEncoder()
    {
        super(PDUType.A_ASSOCIATE_AC, ItemType.AC_PRES_CONTEXT);
    }
}
