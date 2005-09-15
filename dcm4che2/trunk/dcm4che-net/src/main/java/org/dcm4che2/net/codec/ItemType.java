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
public final class ItemType {

    public static final int APP_CTX = 0x10;
    public static final int RQ_PRES_CTX = 0x20;
    public static final int AC_PRES_CTX = 0x21;
    public static final int ABSTRACT_SYNTAX = 0x30;
    public static final int TRANSFER_SYNTAX = 0x40;
    public static final int USER_INFO = 0x50;
    public static final int MAX_LENGTH = 0x51;
    public static final int IMPL_CLASS_UID = 0x52;
    public static final int ASYNC_OPS_WINDOW = 0x53;
    public static final int ROLE_SELECTION = 0x54;
    public static final int IMPL_VERSION_NAME = 0x55;
    public static final int EXT_NEG = 0x56;
    public static final int COMMON_EXT_NEG = 0x57;
    public static final int USER_IDENTITY = 0x58;

}
