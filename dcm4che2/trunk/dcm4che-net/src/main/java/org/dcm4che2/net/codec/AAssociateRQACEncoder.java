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
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.Iterator;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolSession;
import org.dcm4che2.net.pdu.AAssociateRQAC;
import org.dcm4che2.net.pdu.CommonExtendedNegotiation;
import org.dcm4che2.net.pdu.ExtendedNegotiation;
import org.dcm4che2.net.pdu.PDU;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.RoleSelection;
import org.dcm4che2.net.pdu.UserIdentity;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Reversion$ $Date$
 * @since Sep 15, 2005
 */
abstract class AAssociateRQACEncoder extends PDUEncoder
{

    private CharsetEncoder asciiEncoder = Charset.forName("US-ASCII")
            .newEncoder();

    private final int pcItemType;

    protected AAssociateRQACEncoder(int type, int pcItemType)
    {
        super(type);
        this.pcItemType = pcItemType;
    }

    @Override
    protected void encodePDUBody(ProtocolSession session, PDU pdu,
            ByteBuffer out)
    {
        AAssociateRQAC rqac = (AAssociateRQAC) pdu;
        encodeProtocolVersion(rqac.getProtocolVersion(), out);
        encodeAET(rqac.getCalledAET(), out);
        encodeAET(rqac.getCallingAET(), out);
        encodeReservedBytes(rqac, out);
        encodeItem(ItemType.APP_CONTEXT, rqac.getApplicationContext(), out);
        encodePCs(rqac.getPresentationContexts(), out);
        encodeUserInfo(rqac, out);
    }

    private void encodeProtocolVersion(int version, ByteBuffer out)
    {
        out.putShort((short) version);
        out.putShort((short) 0); // reserved bytes 9-10
    }

    private void encodeReservedBytes(AAssociateRQAC rqac, ByteBuffer out)
    {
        out.put(rqac.getReservedBytes());
    }

    private void putASCIIString(String s, ByteBuffer out)
    {
        try
        {
            out.putString(s, asciiEncoder);
        } catch (CharacterCodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void encodeAET(String aet, ByteBuffer out)
    {
        int endpos = out.position() + 16;
        putASCIIString(aet, out);
        while (out.position() < endpos)
            out.put((byte) 0x20);
    }

    private void encodeASCIIString(String s, ByteBuffer out)
    {
        out.putShort((short) s.length());
        putASCIIString(s, out);
    }

    private void encodeItem(int type, String s, ByteBuffer out)
    {
        if (s == null)
            return;

        out.put((byte) type);
        out.put((byte) 0);
        encodeASCIIString(s, out);
    }

    protected void encodePCs(Collection pcs, ByteBuffer out)
    {
        for (Iterator it = pcs.iterator(); it.hasNext();)
        {
            PresentationContext pc = (PresentationContext) it.next();
            out.put((byte) pcItemType);
            out.put((byte) 0);
            out.putShort((short) pc.length());
            out.put((byte) pc.getPCID());
            out.put((byte) 0);
            out.put((byte) pc.getResult());
            out.put((byte) 0);
            encodeItem(ItemType.ABSTRACT_SYNTAX, pc.getAbstractSyntax(), out);
            for (Iterator it2 = pc.getTransferSyntaxes().iterator(); it2
                    .hasNext();)
                encodeItem(ItemType.TRANSFER_SYNTAX, (String) it2.next(), out);
        }
    }

    private void encodeUserInfo(AAssociateRQAC rqac, ByteBuffer out)
    {
        out.put((byte) ItemType.USER_INFO);
        out.put((byte) 0);
        out.putShort((short) rqac.userInfoLength());
        encodeMaxPDULength(rqac.getMaxPDULength(), out);
        encodeItem(ItemType.IMPL_CLASS_UID, rqac.getImplClassUID(), out);
        if (rqac.isAsyncOps())
            encodeAsyncOpsWindow(rqac, out);
        for (Iterator it = rqac.getRoleSelections().iterator(); it.hasNext();)
            encodeRoleSelection((RoleSelection) it.next(), out);
        encodeItem(ItemType.IMPL_VERSION_NAME, rqac.getImplVersionName(), out);
        for (Iterator it = rqac.getExtendedNegotiations().iterator(); it
                .hasNext();)
            encodeExtendedNegotiation((ExtendedNegotiation) it.next(), out);
        for (Iterator it = rqac.getCommonExtendedNegotiations().iterator(); it
                .hasNext();)
            encodeCommonExtendedNegotiation((CommonExtendedNegotiation) it
                    .next(), out);
        encodeUserIdentity(rqac.getUserIdentity(), out);
    }

    private void encodeRoleSelection(RoleSelection selection, ByteBuffer out)
    {
        out.put((byte) ItemType.ROLE_SELECTION);
        out.put((byte) 0);
        out.putShort((short) selection.length());
        encodeASCIIString(selection.getSOPClassUID(), out);
        out.put((byte) (selection.isSCU() ? 1 : 0));
        out.put((byte) (selection.isSCP() ? 1 : 0));
    }

    private void encodeExtendedNegotiation(ExtendedNegotiation extNeg,
            ByteBuffer out)
    {
        out.put((byte) ItemType.EXT_NEG);
        out.put((byte) 0);
        out.putShort((short) extNeg.length());
        encodeASCIIString(extNeg.getSOPClassUID(), out);
        out.put(extNeg.getInformation());
    }

    private void encodeCommonExtendedNegotiation(
            CommonExtendedNegotiation extNeg, ByteBuffer out)
    {
        out.put((byte) ItemType.COMMON_EXT_NEG);
        out.put((byte) 0);
        out.putShort((short) extNeg.length());
        encodeASCIIString(extNeg.getSOPClassUID(), out);
        encodeASCIIString(extNeg.getServiceClassUID(), out);
        for (Iterator it = extNeg.getRelatedGeneralSOPClassUIDs().iterator(); it
                .hasNext();)
            encodeASCIIString((String) it.next(), out);
    }

    private void encodeAsyncOpsWindow(AAssociateRQAC rqac, ByteBuffer out)
    {
        out.put((byte) ItemType.ASYNC_OPS_WINDOW);
        out.put((byte) 0);
        out.putShort((short) 4);
        out.putShort((short) rqac.getMaxOpsInvoked());
        out.putShort((short) rqac.getMaxOpsPerformed());
    }

    private void encodeMaxPDULength(int maxPDULength, ByteBuffer out)
    {
        out.put((byte) ItemType.MAX_PDU_LENGTH);
        out.put((byte) 0);
        out.putShort((short) 4);
        out.putInt(maxPDULength);
    }

    private void encodeUserIdentity(UserIdentity userIdentity, ByteBuffer out)
    {
        if (userIdentity == null)
            return;

        out.put((byte) ItemType.USER_IDENTITY);
        out.put((byte) 0);
        out.putShort((short) userIdentity.length());
        out.put((byte) userIdentity.getUserIdentityType());
        out.put((byte) (userIdentity.isPositiveResponseRequested() ? 1 : 0));
        encodeBytes(userIdentity.getPrimaryField(), out);
        encodeBytes(userIdentity.getSecondaryField(), out);
    }

    private void encodeBytes(byte[] bs, ByteBuffer out)
    {
        out.putShort((short) bs.length);
        out.put(bs);
    }

}
