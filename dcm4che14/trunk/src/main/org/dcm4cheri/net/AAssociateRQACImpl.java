/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package org.dcm4cheri.net;

import org.dcm4che.net.*;
import org.dcm4che.dict.UIDs;
import org.dcm4cheri.util.Impl;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class AAssociateRQACImpl implements AAssociateRQAC {
    
    private String appCtxUID = UIDs.DICOMApplicationContextName;
    private int version = 1;
    private int maxLength = DEFAULT_MAX_LENGTH;
    private String callingAET = "ANONYMOUS";
    private String calledAET = "ANONYMOUS";
    private String implClassUID = Impl.CLASS_UID;
    private String implVers = Impl.VERSION_NAME;
    private AsyncOpsWindow asyncOpsWindow = null;
    private final LinkedHashMap presCtxs = new LinkedHashMap();
    private final LinkedHashMap roleSels = new LinkedHashMap();
    private final LinkedHashMap extNegs = new LinkedHashMap();

    protected AAssociateRQACImpl init(UnparsedPDUImpl raw) throws PDUException {
        if (raw.buffer() == null) {
            throw new PDUException(
                    "PDU length exceeds supported maximum " + raw,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.REASON_NOT_SPECIFIED));
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(
                raw.buffer(), 6, raw.length());
        DataInputStream din = new DataInputStream(bin);
        try {
            version = din.readShort();
            din.readUnsignedByte();
            din.readUnsignedByte();
            calledAET = readASCII(din, 16);
            callingAET = readASCII(din, 16);
            if (din.skip(32) != 32) {
                throw new EOFException();
            }
            while (din.available() > 0) {
                int itemType = din.readUnsignedByte();
                din.readUnsignedByte();
                int itemLen = din.readUnsignedShort();
                switch (itemType) {
                    case 0x10:
                        appCtxUID = readASCII(din, itemLen);
                        break;
                    case 0x20:
                    case 0x21:
                        if (itemType != pctype()) {
                            throw new PDUException(
                                    "unexpected item type "
                                          + Integer.toHexString(itemType) + 'H',
                                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                            AAbort.UNEXPECTED_PDU_PARAMETER));
                        }
                        addPresContext(
                                new PresContextImpl(itemType, din, itemLen));
                        break;
                    case 0x50:
                        readUserInfo(din, itemLen);
                        break;
                   default:
                        throw new PDUException(
                                "unrecognized item type "
                                        + Integer.toHexString(itemType) + 'H',
                                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                        AAbort.UNRECOGNIZED_PDU_PARAMETER));
                }
            }
        } catch (PDUException e) {
            throw e;
        } catch (Exception e) {            
            throw new PDUException("Failed to parse " + raw, e,
                    new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                   AAbort.REASON_NOT_SPECIFIED));
        }
        return this;
    }
    
    public final int getProtocolVersion() {
        return version;
    }

    public final void setProtocolVersion(int version) {
        this.version = version;
    }
    
    public String getCalledAET() {        
        return calledAET;
    }

    public String getCallingAET() {        
        return callingAET;
    }

    public void setCalledAET(String aet) {        
        this.calledAET = checkAE(aet);
    }

    public void setCallingAET(String aet) {
        this.callingAET = checkAE(aet);
    }

    public final String getApplicationContextUID() {
        return appCtxUID;
    }

    public final void setApplicationContextUID(String appCtxUID) {
        appCtxUID = checkUID(appCtxUID);
    }

    private String checkAE(String aet) {
        String retval = aet.trim();
        int len = retval.length();
        if (len == 0 || len > 16) {
            throw new IllegalArgumentException(aet);
        }
        return retval;
    }

    private String checkUID(String uid) {
        int len = uid.length();
        if (len == 0 || len > 64) {
            throw new IllegalArgumentException(uid);
        }
        return uid;
    }
    
    public final int nextPCID() {
        int c = presCtxs.size();
        if (c == 128) {
            return -1;
        }
        int retval = ((c << 1) | 1);
        while (presCtxs.containsKey(new Integer(retval))) {
            retval = (retval + 2) % 256;
        }
        return retval;
    }
    
    public final PresContext addPresContext(PresContext presCtx) {
        if (((PresContextImpl)presCtx).type() != pctype()) {
            throw new IllegalArgumentException("wrong type of " + presCtx);
        }
        return (PresContext)presCtxs.put(
                new Integer(presCtx.pcid()), presCtx);
    }

    public final PresContext removePresContext(int pcid) {
        return (PresContext)presCtxs.remove(new Integer(pcid));
    }
    
    public final PresContext getPresContext(int pcid) {
        return (PresContext)presCtxs.get(new Integer(pcid));
    }

    public final Iterator iteratePresContext() {
        return presCtxs.values().iterator();
    }

    public final void clearPresContext() {
        presCtxs.clear();
    }
    
    public final int countPresContext() {
        return presCtxs.size();
    }
    
    public final String getImplClassUID() {
        return implClassUID;        
    }

    public final void setImplClassUID(String uid) {
        this.implClassUID = checkUID(uid);
    }

    public final String getImplVersionName() {
        return implVers;
    }
    
    public final void setImplVersionName(String name) {
        this.implVers = name != null ? checkAE(name) : null;
    }

    public final int getMaxLength() {
        return maxLength;
    }

    public final void setMaxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength:" + maxLength);
        }
        this.maxLength = maxLength;
    }
    
    public final AsyncOpsWindow getAsyncOpsWindow() {
        return asyncOpsWindow;
    }
    
    public final void setAsyncOpsWindow(AsyncOpsWindow aow) {
        this.asyncOpsWindow = aow;
    }
    
    public final RoleSelection removeRoleSelection(String uid) {
        return (RoleSelection)roleSels.remove(uid);
    }
    
    public final RoleSelection getRoleSelection(String uid) {
        return (RoleSelection)roleSels.get(uid);
    }

    public Iterator iterateRoleSelections() {
        return roleSels.values().iterator();
    }
    
    public int countRoleSelections() {
        return roleSels.size();
    }
    
    public void clearRoleSelections() {
        roleSels.clear();
    }
    
    public final ExtNegotiation removeExtNegotiation(String uid) {
        return (ExtNegotiation)extNegs.remove(uid);
    }
    
    public final ExtNegotiation getExtNegotiation(String uid) {
        return (ExtNegotiation)extNegs.get(uid);
    }

    public Iterator iterateExtNegotiations() {
        return extNegs.values().iterator();
    }
    
    public int countExtNegotiations() {
        return extNegs.size();
    }
    
    public void clearExtNegotiations() {
        extNegs.clear();
    }
    
    static String readASCII(DataInputStream in, int len)
            throws IOException, UnsupportedEncodingException {
        byte[] b = new byte[len];
        in.readFully(b);
        while (len > 0 && b[len-1] == 0) --len;
        return new String(b, 0, len, "US-ASCII");
    }

    public RoleSelection addRoleSelection (RoleSelection roleSel) {
        return (RoleSelection)roleSels.put(
                roleSel.getSOPClassUID(), roleSel);
    }

    public ExtNegotiation addExtNegotiation(ExtNegotiation extNeg) {
        return (ExtNegotiation)extNegs.put(
                extNeg.getSOPClassUID(), extNeg);
    }

    private void readUserInfo(DataInputStream din, int len)
            throws IOException, PDUException {
        int diff = len - din.available();
        if (diff != 0) {
            throw new PDUException("User info item length=" + len
                + " mismatch PDU length (diff=" + diff + ")",
                new AAbortImpl(AAbort.SERVICE_PROVIDER,
                               AAbort.INVALID_PDU_PARAMETER_VALUE));
        }
        while (din.available() > 0) {
            int subItemType = din.readUnsignedByte();
            din.readUnsignedByte();
            int itemLen = din.readUnsignedShort();
            switch (subItemType) {
                case 0x51:
                    if (itemLen != 4) {
                        throw new PDUException(
                                "Illegal length of Maximum length sub-item: "
                                + itemLen,
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                           AAbort.INVALID_PDU_PARAMETER_VALUE));
                    }                                
                    maxLength = din.readInt();
                    break;
                case 0x52:
                    implClassUID = readASCII(din, itemLen);
                    break;
                case 0x53:
                    asyncOpsWindow = new AsyncOpsWindowImpl(din, itemLen);
                    break;
                case 0x54:
                    addRoleSelection(new RoleSelectionImpl(din, itemLen));
                    break;
                case 0x55:
                    implVers = readASCII(din, itemLen);
                    break;
                case 0x56:
                    addExtNegotiation(new ExtNegotiationImpl(din, itemLen));
                    break;
                default:
                    throw new PDUException(
                            "unrecognized user sub-item type "
                                    + Integer.toHexString(subItemType) + 'H',
                            new AAbortImpl(AAbort.SERVICE_PROVIDER,
                                    AAbort.UNRECOGNIZED_PDU_PARAMETER));
           }
        }
    }
    
    protected abstract int type();
    protected abstract int pctype();

    private static final byte[] ZERO32 = { 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    
    private void writeAE(DataOutputStream dout, String aet) throws IOException {
        dout.writeBytes(aet);
        for (int n = aet.length(); n < 16; ++n) {
            dout.write(0);
        }
    }
    
    private static final class MyByteArrayOutputStream
            extends ByteArrayOutputStream {
        MyByteArrayOutputStream() {
            super(4096);
            write(0);
            write(0);
            write(0);
            write(0);
            write(0);
            write(0);
        }
        void writeTo(int type, OutputStream out) throws IOException {
            int len = count - 6;
            buf[0] = (byte)type;
            buf[1] = (byte)0;
            buf[2] = (byte)(len >> 24);
            buf[3] = (byte)(len >> 16);
            buf[4] = (byte)(len >> 8);
            buf[5] = (byte)(len >> 0);
            out.write(buf, 0, count);
        }
    }
    
    public final void writeTo(OutputStream out) throws IOException {
        MyByteArrayOutputStream bout = new MyByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeShort(version);
        dout.write(0);
        dout.write(0);
        writeAE(dout, calledAET);
        writeAE(dout, callingAET);
        dout.write(ZERO32);
        dout.write(0x10);
        dout.write(0);
        dout.writeShort(appCtxUID.length());
        dout.writeBytes(appCtxUID);
        for (Iterator it = presCtxs.values().iterator(); it.hasNext();) {
            ((PresContextImpl)it.next()).writeTo(dout);
        }
        writeUserInfo(dout);
        bout.writeTo(type(), out);
    }
    
    private void writeUserInfo(DataOutputStream dout) throws IOException {
        dout.write(0x50);
        dout.write(0);
        dout.writeShort(getUserInfoLength());
        dout.write(0x51);
        dout.write(0);
        dout.writeShort(4);
        dout.writeInt(maxLength);
        dout.write(0x52);
        dout.write(0);
        dout.writeShort(implClassUID.length());
        dout.writeBytes(implClassUID);
        if (asyncOpsWindow != null) {
            ((AsyncOpsWindowImpl)asyncOpsWindow).writeTo(dout);
        }
        for (Iterator it = roleSels.values().iterator(); it.hasNext();) {
            ((RoleSelectionImpl)it.next()).writeTo(dout);
        }
        if (implVers != null) {
            dout.write(0x55);
            dout.write(0);
            dout.writeShort(implVers.length());
            dout.writeBytes(implVers);
        }
        for (Iterator it = extNegs.values().iterator(); it.hasNext();) {
            ((ExtNegotiationImpl)it.next()).writeTo(dout);
        }             
    }
    
    private int getUserInfoLength() {
        int retval = 12 + implClassUID.length();
        if (asyncOpsWindow != null) {
            retval += 8;
        }
        for (Iterator it = roleSels.values().iterator(); it.hasNext();) {
            RoleSelectionImpl rs = (RoleSelectionImpl)it.next();
            retval += 4 + rs.length();
        }
        if (implVers != null) {
            retval += 4 + implVers.length();
        }
        for (Iterator it = extNegs.values().iterator(); it.hasNext();) {
            ExtNegotiationImpl en = (ExtNegotiationImpl)it.next();
            retval += 4 + en.length();
        }
        return retval;
    }
}
