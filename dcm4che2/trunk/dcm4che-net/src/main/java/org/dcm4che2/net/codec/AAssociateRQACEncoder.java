/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

package org.dcm4che2.net.codec;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
 *
 */
public abstract class AAssociateRQACEncoder extends PDUEncoder {

    private static final int PROTOCOL_VERSION = 1;

    protected AAssociateRQACEncoder(int type) {
        super(type);
    }

    @Override
    protected void encodePDU(ProtocolSession session, PDU pdu, ByteBuffer out) {
        AAssociateRQAC rqac = (AAssociateRQAC) pdu;
        encodeProtocolVersion(out);
        encodeAET(rqac.getCalledAET(), out);
        encodeAET(rqac.getCallingAET(), out);
        encodeReservedBytes(rqac, out);
        encodeItem(ItemType.APP_CTX, rqac.getApplicationContext(), out);
        encodePCs(rqac.getPresentationContexts(), out);
        encodeUserInfo(rqac, out);
    }

    private void encodeProtocolVersion(ByteBuffer out) {
        out.putShort((short) PROTOCOL_VERSION);
        out.putShort((short) 0); // reserved bytes 9-10
    }

    private void encodeReservedBytes(AAssociateRQAC rqac, ByteBuffer out) {
        out.put(rqac.getReservedBytes());
    }

    private void encodeAET(String calledAET, ByteBuffer out) {
        byte[] b;
        try {
            b = calledAET.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
        out.put(b);
        for (int i = b.length; i < 16; ++i)
            out.put((byte) 0);
    }

    private void encodeItem(int type, String s, ByteBuffer out) {
        if (s == null)
            return;
        
        out.putShort((short) type);
        encodeString(s, out);
    }

    private void encodeString(String s, ByteBuffer out) {
        byte[] b;
        try {
            b = s.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
        out.putShort((short) b.length);
        out.put(b);
    }

    protected abstract void encodePCs(Collection pcs, ByteBuffer out);

    protected void encodePC(int type, PresentationContext pc, ByteBuffer out) {
        out.putShort((short) type);
        out.putShort((short) pc.length());
        out.putShort((short) pc.pcid());
        out.putShort((short) pc.result());
        encodeItem(ItemType.ABSTRACT_SYNTAX, pc.getAbstractSyntax(), out);
        List tsList = pc.getTransferSyntaxList();
        for (int i = 0, n = tsList.size(); i < n; i++) {
            encodeItem(ItemType.TRANSFER_SYNTAX, (String) tsList.get(i), out);
        }
    }

    private void encodeUserInfo(AAssociateRQAC rqac, ByteBuffer out) {
        out.putShort((short) ItemType.USER_INFO);
        out.putShort((short) rqac.userInfoLength());
        encodeMaxPDULength(rqac.getMaxPDULength(), out);
        encodeItem(ItemType.IMPL_CLASS_UID, rqac.getImplClassUID(), out);
        if (rqac.isAsyncOps())
            encodeAsyncOpsWindow(rqac, out);
        for (Iterator it = rqac.getRoleSelections().iterator(); it.hasNext();)
            encodeRoleSelection((RoleSelection) it.next(), out);
        encodeItem(ItemType.IMPL_VERSION_NAME, rqac.getImplVersionName(), out);        
        for (Iterator it = rqac.getExtendedNegotiations().iterator(); it.hasNext();)
            encodeExtendedNegotiation((ExtendedNegotiation) it.next(), out);
        for (Iterator it = rqac.getCommonExtendedNegotiations().iterator(); it.hasNext();)
            encodeCommonExtendedNegotiation((CommonExtendedNegotiation) it.next(), out);
        encodeUserIdentity(rqac.getUserIdentity(), out);
    }

    private void encodeRoleSelection(RoleSelection selection, ByteBuffer out) {
        out.putShort((short) ItemType.ROLE_SELECTION);
        out.putShort((short) selection.itemLength());
        encodeString(selection.getSOPClassUID(), out);
        out.put((byte) (selection.scu() ? 1 : 0));
        out.put((byte) (selection.scp() ? 1 : 0));
    }

    private void encodeExtendedNegotiation(ExtendedNegotiation extNeg, ByteBuffer out) {
        out.putShort((short) ItemType.EXT_NEG);
        out.putShort((short) extNeg.itemLength());
        encodeString(extNeg.getSOPClassUID(), out);
        out.put(extNeg.getInformation());
    }
    
    private void encodeCommonExtendedNegotiation(CommonExtendedNegotiation extNeg, ByteBuffer out) {
        out.putShort((short) ItemType.COMMON_EXT_NEG);
        out.putShort((short) extNeg.itemLength());
        encodeString(extNeg.getSOPClassUID(), out);
        encodeString(extNeg.getServiceClassUID(), out);
        for (Iterator it = extNeg.getRelatedSOPClassUIDs().iterator(); it.hasNext();)
            encodeString((String) it.next(), out);
    }
    
    private void encodeAsyncOpsWindow(AAssociateRQAC rqac, ByteBuffer out) {
        out.putShort((short) ItemType.ASYNC_OPS_WINDOW);
        out.putShort((short) 4);
        out.putShort((short) rqac.getMaxOpsInvoked());
        out.putShort((short) rqac.getMaxOpsPerformed());       
    }

    private void encodeMaxPDULength(int maxPDULength, ByteBuffer out) {
        out.putShort((short) ItemType.MAX_LENGTH);
        out.putShort((short) 4);
        out.putInt(maxPDULength);
    }

    private void encodeUserIdentity(UserIdentity userIdentity, ByteBuffer out) {
        if (userIdentity == null)
            return;
        
        out.putShort((short) ItemType.USER_IDENTITY);
        out.putShort((short) userIdentity.itemLength());
        out.put((byte) userIdentity.getUserIdentityType());
        out.put((byte) (userIdentity.isPositiveResponseRequested() ? 1 : 0));
        encodeBytes(userIdentity.getPrimaryField(), out);
        encodeBytes(userIdentity.getSecondaryField(), out);
    }

    private void encodeBytes(byte[] bs, ByteBuffer out) {
        out.putShort((short) bs.length);
        out.put(bs);        
    }

}
