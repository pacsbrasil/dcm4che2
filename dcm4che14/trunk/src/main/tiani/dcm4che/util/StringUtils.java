/*$Id$*/
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG <gunter.zeilinger@tiani.com>     *
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

package tiani.dcm4che.util;

import org.dcm4che.dict.VRs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class StringUtils {

    /** Prevent instances of Utility class */
    private StringUtils() {
    }

    public static StringBuffer promptHex(StringBuffer sb, int v, int l) {
        String hex = Integer.toHexString(v);
        for (int i = hex.length(); i < l; ++i)
            sb.append('0');
        sb.append(hex);
        return sb;
    }

    public static String promptHex(int v, int l) {
        return promptHex(new StringBuffer(l),v,l).toString();
    }

    public static StringBuffer promptTag(StringBuffer sb, int tag) {
        sb.append('(');
        promptHex(sb, tag >>> 16, 4).append(',');
        promptHex(sb, tag & 0xffff, 4).append(')');
        return sb;        
    }

    public static String promptTag(int tag) {
        return promptTag(new StringBuffer(11),tag).toString();
    }    

    public static String promptVR(int vr) {
        return (vr == VRs.NONE
                ? "NONE"
                : new String(new byte[]{(byte)(vr>>8), (byte)(vr)}));
    }

    public static int parseVR(String str) {
        if ("NONE".equals(str))
            return VRs.NONE;
        
        if (str.length() != 2)
            throw new IllegalArgumentException(str);
        
        return ((str.charAt(0) & 0xff) << 8) | (str.charAt(1) & 0xff);
    }
    
    public static StringBuffer promptBytes(StringBuffer sb, byte[] data,
            int start, int length) {
        if (length == 0)
            return sb;
        promptHex(sb, data[start] & 0xff, 2);
        for (int i = start+1, remain = length; --remain > 0; ++i)
            promptHex(sb.append('\\'), data[i] & 0xff, 2);
        return sb;
    }

    public static String promptBytes(byte[] data, int start, int length) {
        if (length == 0)
            return "";
        return promptBytes(new StringBuffer(length * 3 - 1), data, start,
                length).toString();
    }

    public static String promptValue(int vr, ByteBuffer bb, Charset cs) {
        if (bb.limit() == 0)
            return "";
        
        if (VRs.isStringValue(vr)) {
            if (bb.get(bb.limit()-1) == 0)
                bb.limit(bb.limit()-1);
            return cs.decode(bb).toString();
        }
        
        switch (vr) {
            case VRs.AT:
                return promptAT(bb);
            case VRs.FD:
                return promptFD(bb);
            case VRs.FL:
                return promptFL(bb);
            case VRs.OB: case VRs.UN:
                return promptBytes(bb.array(), bb.arrayOffset(), bb.limit());
            case VRs.OW:
                return promptOW(bb);
            case VRs.SL:
                return promptSL(bb);
            case VRs.SS:
                return promptSS(bb);
            case VRs.UL:
                return promptUL(bb);
            case VRs.US:
                return promptUS(bb);
        }
        throw new IllegalArgumentException("VR:" + promptVR(vr));
    }

    public static String promptAT(ByteBuffer bb) {
        int l = bb.limit() / 4 * 9 - 1;
        if (l < 0)
            return "";

        StringBuffer sb = new StringBuffer(l);
        bb.rewind();
        promptHex(sb, bb.getShort() & 0xffff, 4);
        promptHex(sb, bb.getShort() & 0xffff, 4);
        while (bb.remaining() >= 4) {
            promptHex(sb.append('\\'), bb.getShort() & 0xffff, 4);
            promptHex(sb, bb.getShort() & 0xffff, 4);
        }                
        return sb.toString();        
    }
    
    public static String promptFD(ByteBuffer bb) {
        if (bb.limit() < 8)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getDouble());
        while (bb.remaining() >= 8)
            sb.append('\\').append(bb.getDouble());

        return sb.toString();        
    }
    
    public static String promptFL(ByteBuffer bb) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getFloat());
        while (bb.remaining() >= 4)
            sb.append('\\').append(bb.getFloat());

        return sb.toString();        
    }
    
    public static String promptOW(ByteBuffer bb) {
        int l = bb.limit() / 2 * 5 - 1;
        if (l < 0)
            return "";
        
        StringBuffer sb = new StringBuffer(l);
        bb.rewind();
        promptHex(sb, bb.getShort() & 0xffff, 4);
        while (bb.remaining() >= 2)
            promptHex(sb.append('\\'), bb.getShort() & 0xffff, 4);
                
        return sb.toString();        
    }

    public static String promptSL(ByteBuffer bb) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getInt());
        while (bb.remaining() >= 4)
            sb.append('\\').append(bb.getInt());

        return sb.toString();        
    }
    
    public static String promptSS(ByteBuffer bb) {
        if (bb.limit() < 2)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getShort());
        while (bb.remaining() >= 2)
            sb.append('\\').append(bb.getShort());

        return sb.toString();        
    }
    
    public static String promptUL(ByteBuffer bb) {
        if (bb.limit() < 4)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getInt() & 0xffffffffL);
        while (bb.remaining() >= 4)
            sb.append('\\').append(bb.getInt() & 0xffffffffL);

        return sb.toString();        
    }
    
    public static String promptUS(ByteBuffer bb) {
        if (bb.limit() < 2)
            return "";

        StringBuffer sb = new StringBuffer(bb.limit());
        bb.rewind();
        sb.append(bb.getShort() & 0xffff);
        while (bb.remaining() >= 2)
            sb.append('\\').append(bb.getShort() & 0xffff);

        return sb.toString();        
    }

    static final byte[] b0 = {};
    public static byte[] parseValue(int vr, String str, Charset cs) {
        if (str.length() == 0)
            return b0;
        
        if (VRs.isStringValue(vr)) {
            return cs.encode(CharBuffer.wrap(str)).array();
        }
        switch (vr) {
            case VRs.AT:
                return parseAT(str);
            case VRs.FD:
                return parseFD(str);
            case VRs.FL:
                return parseFL(str);
            case VRs.OB: case VRs.UN:
                return parseBytes(str);
            case VRs.OW:
                return parseOW(str);
            case VRs.SS: case VRs.US:
                return parseSS_US(str);
            case VRs.SL: case VRs.UL:
                return parseSL_UL(str);
        }
        throw new IllegalArgumentException("VR:" + promptVR(vr));
    }

    static final String[] NULL_STRINGS = {};
    public static String[] toStringArray(String s, char delim) {
        if (s == null || s.length() == 0) {
            return NULL_STRINGS;
        }
        char[] c = s.toCharArray();
        int n = 1;
        for (int i = 0; i < c.length; ++i) {
            if (c[i] == delim) {
                ++n;
            };
        }
        String[] array = new String[n];
        int offset = 0;
        int index = 0;
        for (int i = 0; i < c.length; ++i) {
            if (c[i] == delim) {
                array[index++] = new String(c,offset,i-offset);
                offset = i+1;
            };
        }
        array[index] = new String(c,offset,c.length-offset);
        return array;
    }
    
    public static byte[] parseAT(String str) {
        String[] a = toStringArray(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i) {
            int tag = Integer.parseInt(a[i],16);
            bb.putShort((short)(tag >>> 16));
            bb.putShort((short)tag);
        }
        return b;
    }
    
    public static byte[] parseFD(String str) {
        String[] a = toStringArray(str,'\\');
        byte[] b = new byte[a.length * 8];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putDouble(Double.parseDouble(a[i]));
        return b;
    }
    
    public static byte[] parseFL(String str) {
        String[] a = toStringArray(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putFloat(Float.parseFloat(a[i]));
        return b;
    }
    
    public static byte[] parseBytes(String str) {
        byte[] b = new byte[(str.length()+1)/3];
        for (int i = 0, l = 0; i < b.length; ++i, l+=3)
            b[i++] = Byte.parseByte(str.substring(l, l+2), 16);
        return b;
    }
    
    public static byte[] parseOW(String str) {
        byte[] b = new byte[(str.length()+1)/5];
        for (int i = 0, l = 0; i < b.length; l+=5) {
            short s = Short.parseShort(str.substring(l, l+4), 16);
            b[i++] = (byte)(s);
            b[i++] = (byte)(s >>> 8);
        }
        return b;
    }
    public static byte[] parseSS_US(String str) {
        String[] a = toStringArray(str,'\\');
        byte[] b = new byte[a.length * 2];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putShort((short)Integer.parseInt(a[i]));
        return b;
    }
    
    public static byte[] parseSL_UL(String str) {
        String[] a = toStringArray(str,'\\');
        byte[] b = new byte[a.length * 4];
        ByteBuffer bb = ByteBuffer.wrap(b, 0, b.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putInt((int)Long.parseLong(a[i]));
        return b;
    }
}
