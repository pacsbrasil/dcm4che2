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
 *
 */
public final class PDUType {

    public static int A_ASSOCIATE_RQ = 0x01;
    public static int A_ASSOCIATE_AC = 0x02;
    public static int A_ASSOCIATE_RJ = 0x03;
    public static int P_DATA_TF      = 0x04;
    public static int A_RELEASE_RQ   = 0x05;
    public static int A_RELEASE_RP   = 0x06;
    public static int A_ABORT        = 0x07;

}
