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

/*$Id$*/

package tiani.dcm4che.data;

import org.dcm4che.data.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tiani.dcm4che.util.StringUtils;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class ValueElement extends DcmElementImpl {
    protected final ByteBuffer data;
    
    ValueElement(int tag, ByteBuffer data) {
        super(tag);
        this.data = data;
    }

    public final int length() {
        return ((data.limit()+1)&(~1));
    }    

    public final ByteBuffer getByteBuffer() {
        return data;
    }
    
    public final ByteBuffer getByteBuffer(ByteOrder byteOrder) {
        if (data.order() != byteOrder)
            swapOrder();
        return data;
    }

    public int vm() {
        return data.limit() == 0 ? 0 : 1;
    }
        
    protected void swapOrder() {
        data.order(swap(data.order()));
    }
        
    // SS, US -------------------------------------------------------------
    private static ByteBuffer setShort(int v) {
        return ByteBuffer.wrap(new byte[2]).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short)v);
    }
    
    private static ByteBuffer setShorts(int[] a) {
        if (a.length == 0)
            return EMPTY_VALUE;
        
        if (a.length == 1)
            return setShort(a[0]);
        
        ByteBuffer bb = ByteBuffer.wrap(new byte[a.length<<1])
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putShort((short)a[i]);
        return bb;
    }

    private static final class SS extends ValueElement {
        SS(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x5353;
        }
        public final int vm() {
            return data.limit()>>1;
        }
        public final int getInt(int index) {
            return data.getShort(index<<1);
        }
        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getInt(i);
            return a;
        }
        protected void swapOrder() {
            swapWords(data);
        }
    }
    static DcmElement createSS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " SS #" + data.limit());
            return new SS(tag, EMPTY_VALUE);
        }
        return new SS(tag, data);
    }
    static DcmElement createSS(int tag) {
        return new SS(tag, EMPTY_VALUE);
    }
    static DcmElement createSS(int tag, int v) {
        return new SS(tag, setShort(v));
    }
    static DcmElement createSS(int tag, int[] a) {
        return new SS(tag, setShorts(a));
    }
    
    private static final class US extends ValueElement {
        US(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x5553;
        }
        public final int vm() {
            return data.limit()>>1;
        }
        public final int getInt(int index) {
            return data.getShort(index<<1) & 0xffff;
        }
        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getInt(i);
            return a;
        }
        protected void swapOrder() {
            swapWords(data);
        }
    }
    static DcmElement createUS(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " US #" + data.limit());
            return new US(tag, EMPTY_VALUE);
        }
        return new US(tag, data);
    }
    static DcmElement createUS(int tag) {
        return new US(tag, EMPTY_VALUE);
    }
    static DcmElement createUS(int tag, int s) {
        return new US(tag, setShort(s));
    }
    static DcmElement createUS(int tag, int[] s) {
        return new US(tag, setShorts(s));
    }
    
    // SL, UL -------------------------------------------------------------
    private static ByteBuffer setInt(int v) {
        return ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(v);
    }
    
    private static ByteBuffer setInts(int[] a) {
        if (a.length == 0)
            return EMPTY_VALUE;

        if (a.length == 1)
            return setInt(a[0]);
        
        ByteBuffer bb = ByteBuffer.wrap(new byte[a.length<<2])
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putInt(a[i]);
        return bb;
    }

    private static abstract class Int extends ValueElement {
        Int(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int getInt(int index) {
            return data.getInt(index<<2);
        }
        
        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getInt(i);
            return a;
        }
                
        public final int vm() {
            return data.limit()>>2;
        }
        
        protected void swapOrder() {
            swapInts(data);
        }
    }
            
    private static class SL extends Int {
        SL(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x534C;
        }
    }
    static DcmElement createSL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " SL #" + data.limit());
            return new SL(tag, EMPTY_VALUE);
        }
        return new SL(tag, data);
    }
    static DcmElement createSL(int tag) {
        return new SL(tag, EMPTY_VALUE);
    }
    static DcmElement createSL(int tag, int v) {
        return new SL(tag, setInt(v));
    }
    static DcmElement createSL(int tag, int[] a) {
        return new SL(tag, setInts(a));
    }
    
    static class UL extends Int {
        UL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x554C;
        }
    }
    static DcmElement createUL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " UL #" + data.limit());
            return new UL(tag, EMPTY_VALUE);
        }
        return new UL(tag, data);
    }
    static DcmElement createUL(int tag) {
        return new UL(tag, EMPTY_VALUE);
    }
    static DcmElement createUL(int tag, int v) {
        return new UL(tag, setInt(v));
    }
    static DcmElement createUL(int tag, int[] a) {
        return new UL(tag, setInts(a));
    }
    
    // AT -------------------------------------------------------------
    private static ByteBuffer setTag(int v) {
        return ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short)(v>>8)).putShort((short)v);
    }
    
    private static ByteBuffer setTags(int[] a) {
        if (a.length == 0)
            return EMPTY_VALUE;

        if (a.length == 1)
            return setTag(a[0]);
        
        ByteBuffer bb = ByteBuffer.wrap(new byte[a.length<<2])
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putShort((short)(a[i]>>16)).putShort((short)a[i]);
        return bb;
    }

    private static final class AT extends ValueElement {
        AT(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vr() {
            return 0x4154;
        }

        public final int vm() {
            return data.limit()>>2;
        }

        public final int getTag(int index) {
            final int pos = index<<2;
            return ((data.getShort(pos)<<16) | (data.getShort(pos+2) & 0xffff));
        }
        
        public final int[] getTags() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getTag(i);
            return a;
        }

        protected void swapOrder() {
            swapWords(data);
        }
    }
    static DcmElement createAT(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " AT #" + data.limit());
            return new AT(tag, EMPTY_VALUE);
        }
        return new AT(tag, data);
    }
    static DcmElement createAT(int tag) {
        return new AT(tag, EMPTY_VALUE);
    }
    static DcmElement createAT(int tag, int v) {
        return new AT(tag, setTag(v));
    }
    static DcmElement createAT(int tag, int[] a) {
        return new AT(tag, setTags(a));
    }
    

    // FL -------------------------------------------------------------
    private static ByteBuffer setFloat(float v) {
        return ByteBuffer.wrap(new byte[4]).order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(v);
    }
    
    private static ByteBuffer setFloats(float[] a) {
        if (a.length == 0)
            return EMPTY_VALUE;

        if (a.length == 1)
            return setFloat(a[0]);
        
        ByteBuffer bb = ByteBuffer.wrap(new byte[a.length<<2])
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putFloat(a[i]);
        return bb;
    }

    private static final class FL extends ValueElement {
        FL(int tag, ByteBuffer data) {
            super(tag, data);
        }

        public final int vm() {
            return data.limit()>>2;
        }        

        public final int vr() {
            return 0x464C;
        }

        public final float getFloat(int index) {
            return data.getFloat(index<<2);
        }
        
        public final float[] getFloats() {
            float[] a = new float[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getFloat(i);
            return a;
        }
        protected void swapOrder() {
            swapInts(data);
        }
    }

    static DcmElement createFL(int tag, ByteBuffer data) {
        if ((data.limit() & 3) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " FL #" + data.limit());
            return new FL(tag, EMPTY_VALUE);
        }
                    
        return new FL(tag, data);
    }
    static DcmElement createFL(int tag) {
        return new FL(tag, EMPTY_VALUE);
    }
    static DcmElement createFL(int tag, float v) {
        return new FL(tag, setFloat(v));
    }
    static DcmElement createFL(int tag, float[] a) {
        return new FL(tag, setFloats(a));
    }
   
    // FD -------------------------------------------------------------
    private static ByteBuffer setDouble(double v) {
        return ByteBuffer.wrap(new byte[8]).order(ByteOrder.LITTLE_ENDIAN)
                .putDouble(v);
    }
    
    private static ByteBuffer setDoubles(double[] a) {
        if (a.length == 0)
            return EMPTY_VALUE;

        if (a.length == 1)
            return setDouble(a[0]);
        
        ByteBuffer bb = ByteBuffer.wrap(new byte[a.length<<3])
                .order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < a.length; ++i)
            bb.putDouble(a[i]);
        return bb;
    }    

    private static final class FD extends ValueElement {
        FD(int tag, ByteBuffer data) {
            super(tag, data);
        }
        
        public final int vm() {
            return data.limit()>>>3;
        }

        public final int vr() {
            return 0x4644;
        }

        public final double getDouble(int index) {
            return data.getDouble(index << 3);
        }
        
        public final double[] getDoubles() {
            double[] a = new double[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getDouble(i);
            return a;
        }
        
        protected void swapOrder() {
            swapLongs(data);
        }
    }
    static DcmElement createFD(int tag, ByteBuffer data) {
        if ((data.limit() & 7) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " FD #" + data.limit());
            return new FD(tag, EMPTY_VALUE);
        }
        return new FD(tag, data);
    }
    static DcmElement createFD(int tag) {
        return new FD(tag, EMPTY_VALUE);
    }
    static DcmElement createFD(int tag, double v) {
        return new FD(tag, setDouble(v));
    }
    static DcmElement createFD(int tag, double[] a) {
        return new FD(tag, setDoubles(a));
    }

    // OW, OB, UN -------------------------------------------------------------
            
    private static final class OW extends ValueElement {
       OW(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x4F57;
        }
        public final int getInt(int index) {
            return data.getInt(index<<2);
        }       
        public final int[] getInts() {
            int[] a = new int[vm()];
            for (int i = 0; i < a.length; ++i)
                a[i] = getInt(i);
            return a;
        }
        protected void swapOrder() {
            swapWords(data);
        }
    }
    static DcmElement createOW(int tag) {
        return new OW(tag, EMPTY_VALUE);
    }

    static DcmElement createOW(int tag, byte[] v, ByteOrder byteOrder) {
        if ((v.length & 1) != 0)
            throw new IllegalArgumentException("odd value length: " + v.length);
        return new OW(tag, ByteBuffer.wrap(v).order(byteOrder));
    }

    static DcmElement createOW(int tag, ByteBuffer data) {
        if ((data.limit() & 1) != 0) {
            log.warning("Ignore illegal value of " + StringUtils.promptTag(tag)
                + " OW #" + data.limit());
            return new OW(tag, EMPTY_VALUE);
        }
        return new OW(tag, data);
    }

    private static final class OB extends ValueElement {
        OB(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x4F42;
        }
    }
    static DcmElement createOB(int tag) {
        return new OB(tag, EMPTY_VALUE);
    }

    static DcmElement createOB(int tag, ByteBuffer v) {
        return new OB(tag, v);
    }

    static DcmElement createOB(int tag, byte[] v) {
        return new OB(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }

    private static final class UN extends ValueElement {
        UN(int tag, ByteBuffer data) {
            super(tag, data);
        }
        public final int vr() {
            return 0x554E;
        }
    }
    static DcmElement createUN(int tag) {
        return new UN(tag, EMPTY_VALUE);
    }

    static DcmElement createUN(int tag, ByteBuffer v) {
        return new UN(tag, v);
    }

    static DcmElement createUN(int tag, byte[] v) {
        return new UN(tag, ByteBuffer.wrap(v).order(ByteOrder.LITTLE_ENDIAN));
    }
}
