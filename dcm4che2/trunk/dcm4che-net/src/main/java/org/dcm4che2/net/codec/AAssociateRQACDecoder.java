/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.net.codec;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.AAbort;
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
 */
abstract class AAssociateRQACDecoder implements PDUDecoder
{

    CharsetDecoder asciiDecoder = Charset.forName("US-ASCII").newDecoder();

    private String decodeASCIIString(ByteBuffer in)
    {
        return decodeASCIIString(in, in.getUnsignedShort());
    }

    private String decodeASCIIString(ByteBuffer in, int fieldSize)
    {
        try
        {
            return in.getString(fieldSize, asciiDecoder);
        } catch (CharacterCodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void decodePDU(ProtocolSession session, ByteBuffer in,
            AAssociateRQAC rqac, String prompt)
            throws DULProtocolViolationException
    {
        if (in.remaining() < 68)
            throw new DULProtocolViolationException(
                    AAbort.INVALID_PDU_PARAMETER_VALUE,
                    "Insufficient PDU-length of " + prompt + ": " + in.remaining());

        rqac.setProtocolVersion(in.getUnsignedShort());
        in.get(); // skip reserved byte 9
        in.get(); // skip reserved byte 10
        rqac.setCalledAET(decodeASCIIString(in, 16));
        rqac.setCallingAET(decodeASCIIString(in, 16));
        byte[] b32 = new byte[32];
        in.get(b32);
        rqac.setReservedBytes(b32);
        while (in.remaining() > 0)
            decodeItem(in, rqac);
    }

    private void decodeItem(ByteBuffer in, AAssociateRQAC rqac)
    {
        int itemType = in.get() & 0xff;
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType)
        {
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
    }

    private PresentationContext decodePC(ByteBuffer in, int itemLength)
    {
        PresentationContext pc = new PresentationContext();
        pc.setPCID(in.get() & 0xff);
        in.get(); // skip reserved byte
        pc.setResult(in.get() & 0xff);
        in.get(); // skip reserved byte
        int remaining = itemLength - 4;
        while (remaining > 0)
            remaining -= decodePCSubItem(in, pc);
        return pc;
    }

    private int decodePCSubItem(ByteBuffer in, PresentationContext pc)
    {
        int itemType = in.get() & 0xff;
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType)
        {
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

    private void decodeUserInfo(ByteBuffer in, int itemLength,
            AAssociateRQAC rqac)
    {
        int remaining = itemLength;
        while (remaining > 0)
            remaining -= decodeUserInfoSubItem(in, rqac);
    }

    private int decodeUserInfoSubItem(ByteBuffer in, AAssociateRQAC rqac)
    {
        int itemType = in.get() & 0xff;
        in.get(); // skip reserved byte
        int itemLength = in.getUnsignedShort();
        switch (itemType)
        {
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
            rqac.addExtendedNegotiation(decodeExtendedNegotiation(in,
                    itemLength));
            break;
        case ItemType.COMMON_EXT_NEG:
            rqac.addCommonExtendedNegotiation(decodeCommonExtendedNegotiation(
                    in, itemLength));
            break;
        case ItemType.USER_IDENTITY:
            rqac.setUserIdentity(decodeUserIdentity(in, itemLength));
            break;
        default:
            skipItem(in, itemLength);
        }
        return 4 + itemLength;
    }

    private RoleSelection decodeRoleSelection(ByteBuffer in, int itemLength)
    {
        RoleSelection rs = new RoleSelection();
        rs.setSOPClassUID(decodeASCIIString(in));
        rs.setSCU(in.get() != 0);
        rs.setSCP(in.get() != 0);
        return rs;
    }

    private ExtendedNegotiation decodeExtendedNegotiation(ByteBuffer in,
            int itemLength)
    {
        ExtendedNegotiation extNeg = new ExtendedNegotiation();
        int uidLength = in.getUnsignedShort();
        extNeg.setSOPClassUID(decodeASCIIString(in, uidLength));
        extNeg.setInformation(decodeBytes(in, itemLength - uidLength - 2));
        return extNeg;
    }

    private byte[] decodeBytes(ByteBuffer in)
    {
        return decodeBytes(in, in.getUnsignedShort());
    }

    private byte[] decodeBytes(ByteBuffer in, int len)
    {
        byte[] bs = new byte[len];
        in.get(bs);
        return bs;
    }

    private CommonExtendedNegotiation decodeCommonExtendedNegotiation(
            ByteBuffer in, int itemLength)
    {
        int endPos = in.position() + itemLength;
        CommonExtendedNegotiation extNeg = new CommonExtendedNegotiation();
        extNeg.setSOPClassUID(decodeASCIIString(in, in.getUnsignedShort()));
        extNeg.setServiceClassUID(decodeASCIIString(in, in.getUnsignedShort()));
        decodeRelatedGeneralSOPClassUIDs(in, in.getUnsignedShort(), extNeg);
        in.position(endPos);
        return extNeg;
    }

    private void decodeRelatedGeneralSOPClassUIDs(ByteBuffer in, int totlen,
            CommonExtendedNegotiation extNeg)
    {
        int endPos = in.position() + totlen;
        while (in.position() < endPos)
        {
            extNeg.addRelatedGeneralSOPClassUID(decodeASCIIString(in));
        }
    }

    private UserIdentity decodeUserIdentity(ByteBuffer in, int itemLength)
    {
        UserIdentity user = new UserIdentity();
        user.setUserIdentityType(in.get() & 0xff);
        user.setPositiveResponseRequested(in.get() != 0);
        user.setPrimaryField(decodeBytes(in));
        user.setSecondaryField(decodeBytes(in));
        return user;
    }

    private void skipItem(ByteBuffer in, int itemLength)
    {
        in.position(in.position() + itemLength);
    }

}
