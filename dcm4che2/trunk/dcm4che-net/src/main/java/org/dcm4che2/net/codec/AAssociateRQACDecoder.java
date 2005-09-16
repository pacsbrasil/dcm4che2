/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.AAssociateRQAC;
import org.dcm4che2.net.pdu.CommonExtendedNegotiation;
import org.dcm4che2.net.pdu.ExtendedNegotiation;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
import org.dcm4che2.net.pdu.UserIdentity;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 *
 */
public abstract class AAssociateRQACDecoder extends PDUDecoder {
    
    private CharsetDecoder asciiDecoder = Charset.forName("US_ASCII").newDecoder();

    protected AAssociateRQACDecoder(int type) {
        super(type);
    }

    private String decodeASCIIString(ByteBuffer in)  {
        return decodeASCIIString(in, in.getUnsignedShort());
    }
    
    private String decodeASCIIString(ByteBuffer in, int fieldSize)  {
        try {
            return in.getString(fieldSize, asciiDecoder);
        } catch (CharacterCodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void decodePDU(ProtocolSession session, ByteBuffer in, AAssociateRQAC rqac) {
        rqac.setProtocolVersion(in.getUnsignedShort());
        in.getShort(); // skip Reserved Bytes 9-10
        rqac.setCalledAET(decodeASCIIString(in, 16));
        rqac.setCallingAET(decodeASCIIString(in, 16));
        byte[] b32 = new byte[32];
        in.get(b32);
        rqac.setReservedBytes(b32);
        int remaining = length - 68;
        while (remaining > 0)
            remaining =- decodeItem(in, rqac);
    }

    private int decodeItem(ByteBuffer in, AAssociateRQAC rqac) {
        int itemType = in.getUnsigned();
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType) {
        case ItemType.APP_CONTEXT:
            rqac.setApplicationContext(decodeASCIIString(in, itemLength));
            break;
        case ItemType.RQ_PRES_CONTEXT:
        case ItemType.AC_PRES_CONTEXT:
            rqac.addPresentationContext(decodePC(in, itemLength));
            break;
        case ItemType.USER_INFO:
            decodeUserInfo(in, itemLength, rqac);
            break;
        default:
            skipItem(in, itemLength);
        }
        return 4 + itemLength;
    }

    private PresentationContext decodePC(ByteBuffer in, int itemLength) {
        PresentationContext pc = new PresentationContext();
        pc.setPCID(in.getUnsigned());
        in.get(); // skip reserved byte
        pc.setResult(in.getUnsigned());
        in.get(); // skip reserved byte
        int remaining = itemLength - 4;
        while (remaining > 0)
            remaining =- decodePCSubItem(in, pc);
        return pc;
    }

    private int decodePCSubItem(ByteBuffer in, PresentationContext pc) {
        int itemType = in.getUnsigned();
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType) {
        case ItemType.ABSTRACT_SYNTAX:
            pc.setAbstractSyntax(decodeASCIIString(in, itemLength));
            break;
        case ItemType.TRANSFER_SYNTAX:
            pc.addTransferSyntax(decodeASCIIString(in, itemLength));
            break;
        default:
            skipItem(in, itemLength);
        }
        return 4 + itemLength;
    }

    private void decodeUserInfo(ByteBuffer in, int itemLength, AAssociateRQAC rqac) {
        int remaining = itemLength;
        while (remaining > 0)
            remaining =- decodeUserInfoSubItem(in, rqac);
    }

    private int decodeUserInfoSubItem(ByteBuffer in, AAssociateRQAC rqac) {
        int itemType = in.getUnsigned();
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType) {
        case ItemType.MAX_PDU_LENGTH:
            rqac.setMaxPDULength(in.getInt());
            break;
        case ItemType.IMPL_CLASS_UID:
            rqac.setImplClassUID(decodeASCIIString(in, itemLength));
            break;
        case ItemType.ASYNC_OPS_WINDOW:
            rqac.setMaxOpsInvoked(in.getUnsignedShort());
            rqac.setMaxOpsPerformed(in.getUnsignedShort());
            break;
        case ItemType.ROLE_SELECTION:
            rqac.addRoleSelection(decodeRoleSelection(in, itemLength));
            break;
        case ItemType.IMPL_VERSION_NAME:
            rqac.setImplVersionName(decodeASCIIString(in, itemLength));
            break;
        case ItemType.EXT_NEG:
            rqac.addExtendedNegotiation(
                    decodeExtendedNegotiation(in, itemLength));
            break;
        case ItemType.COMMON_EXT_NEG:
            rqac.addCommonExtendedNegotiation(
                    decodeCommonExtendedNegotiation(in, itemLength));
            break;
        case ItemType.USER_IDENTITY:
            rqac.setUserIdentity(decodeUserIdentity(in, itemLength));
            break;
        default:
            skipItem(in, itemLength);
        }
        return 4 + itemLength;
    }

    private RoleSelection decodeRoleSelection(ByteBuffer in, int itemLength) {
        RoleSelection rs = new RoleSelection();
        rs.setSOPClassUID(decodeASCIIString(in));
        rs.setSCU(in.get() != 0);
        rs.setSCP(in.get() != 0);
        return rs;
    }

    private ExtendedNegotiation decodeExtendedNegotiation(ByteBuffer in,
            int itemLength) {
        ExtendedNegotiation extNeg = new ExtendedNegotiation();
        int uidLength = in.getUnsignedShort();
        extNeg.setSOPClassUID(decodeASCIIString(in, uidLength));
        extNeg.setInformation(decodeBytes(in, itemLength - uidLength - 2));
        return extNeg;
    }

    private byte[] decodeBytes(ByteBuffer in) {
        return decodeBytes(in, in.getUnsignedShort());
    }
    
    private byte[] decodeBytes(ByteBuffer in, int len) {
        byte[] bs = new byte[len];
        in.get(bs);
        return bs;
    }

    private CommonExtendedNegotiation decodeCommonExtendedNegotiation(
            ByteBuffer in, int itemLength) {
        int endPos = in.position() + itemLength;
        CommonExtendedNegotiation extNeg = new CommonExtendedNegotiation();        
        extNeg.setSOPClassUID(decodeASCIIString(in, in.getUnsignedShort()));
        extNeg.setServiceClassUID(decodeASCIIString(in, in.getUnsignedShort()));
        decodeRelatedGeneralSOPClassUIDs(in, in.getUnsignedShort(), extNeg);
        in.position(endPos);
        return extNeg;
    }

    private void decodeRelatedGeneralSOPClassUIDs(ByteBuffer in, int totlen,
            CommonExtendedNegotiation extNeg) {
        int endPos = in.position() + totlen;
        while (in.position() < endPos) {
            extNeg.addRelatedGeneralSOPClassUID(decodeASCIIString(in));
        }
    }

    private UserIdentity decodeUserIdentity(ByteBuffer in, int itemLength) {
        UserIdentity user = new UserIdentity();
        user.setUserIdentityType(in.getUnsigned());
        user.setPositiveResponseRequested(in.get() != 0);
        user.setPrimaryField(decodeBytes(in));
        user.setSecondaryField(decodeBytes(in));
        return user;
    }

    private void skipItem(ByteBuffer in, int itemLength) {
        in.position(in.position() + itemLength);
    }

}
