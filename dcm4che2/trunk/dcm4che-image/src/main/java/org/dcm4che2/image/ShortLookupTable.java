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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2003-2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
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

package org.dcm4che2.image;

public class ShortLookupTable extends LookupTable {

    private short[] data;

    public ShortLookupTable(int inBits, boolean signed, int off, int outBits,
            short[] data) {
        this(inBits, signed, off, outBits, data, false);
    }

    public ShortLookupTable(int inBits, boolean signed, int off, int outBits,
            short[] data, boolean preserve) {
        super(inBits, signed, off, outBits, preserve);
        this.data = data;
    }

    public final int length() {
        return data.length;
    }

    public final byte lookupByte(int in) {
        return (byte) lookupShort(in);
    }

    public final short lookupShort(int in) {
        int tmp = ((in & signbit) != 0 ? (in | ormask) : (in & andmask)) - off;
        return tmp <= 0 ? data[0] : tmp >= data.length ? data[data.length - 1]
                : data[tmp];
    }

    public final int lookup(int in) {
        return lookupShort(in) & 0xffff;
    }

    public final byte[] lookup(byte[] src, byte[] dst) {
        if (dst == null) {
            dst = new byte[src.length];
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = lookupByte(src[i]);
        }
        return dst;
    }

    public final short[] lookup(byte[] src, short[] dst) {
        if (dst == null) {
            dst = new short[src.length];
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = lookupShort(src[i]);
        }
        return dst;
    }

    public final byte[] lookup(short[] src, byte[] dst) {
        if (dst == null) {
            dst = new byte[src.length];
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = lookupByte(src[i]);
        }
        return dst;
    }

    public final short[] lookup(short[] src, short[] dst) {
        if (dst == null) {
            dst = new short[src.length];
        }
        for (int i = 0; i < src.length; i++) {
            dst[i] = lookupShort(src[i]);
        }
        return dst;
    }

    protected LookupTable inverse() {
        int outMax = (1 << outBits) - 1;
        short[] newData = preserve ? new short[data.length] : data;
        for (int i = 0; i < newData.length; i++) {
            newData[i] = (short) (outMax - data[i]);
        }
        return preserve ? new ShortLookupTable(inBits, signbit != 0, off,
                outBits, newData) : this;
    }

    protected LookupTable scale(int outBits, boolean inverse) {
        int shift = outBits - this.outBits;
        if (shift == 0 && !inverse) {
            return this;
        }
        int outMax = (1 << outBits) - 1;
        if (preserve && outBits <= 8) {
            byte[] newData = new byte[data.length];
            for (int i = 0; i < newData.length; i++) {
                int tmp = shift < 0 ? (data[i] & 0xffff) >>> -shift
                        : data[i] << shift;
                newData[i] = (byte) (inverse ? outMax - tmp : tmp);
            }
            return new ByteLookupTable(inBits, signbit != 0, off, outBits, newData);
        } else {
            short[] newData = preserve ? new short[data.length] : data;
            for (int i = 0; i < newData.length; i++) {
                int tmp = shift < 0 ? (data[i] & 0xffff) >>> -shift
                        : data[i] << shift;
                newData[i] = (short) (inverse ? outMax - tmp : tmp);
            }
            if (preserve) {
                return new ShortLookupTable(inBits, signbit != 0, off, outBits,
                        newData);
            } else {
                this.outBits = outBits;
                return this;
            }
        }
    }

    protected LookupTable combine(LookupTable other, int outBits,
            boolean inverse) {
        int shift1 = other.inBits - this.outBits;
        int shift2 = outBits - other.outBits;
        int outMax = (1 << outBits) - 1;
        if (outBits <= 8) {
            byte[] newData = new byte[data.length];
            for (int i = 0; i < newData.length; i++) {
                int tmp = other
                        .lookup(shift1 < 0 ? (data[i] & 0xffff) >>> -shift1
                                : data[i] << shift1);
                if (shift2 < 0) {
                    tmp >>>= -shift2;
                } else {
                    tmp <<= shift2;
                }
                newData[i] = (byte) (inverse ? outMax - tmp : tmp);
            }
            return new ByteLookupTable(inBits, signbit != 0, off, outBits, newData);
        } else {
            short[] newData = new short[data.length];
            for (int i = 0; i < newData.length; i++) {
                int tmp = other
                        .lookup(shift1 < 0 ? (data[i] & 0xffff) >>> -shift1
                                : data[i] << shift1);
                if (shift2 < 0) {
                    tmp >>>= -shift2;
                } else {
                    tmp <<= shift2;
                }
                newData[i] = (short) (inverse ? outMax - tmp : tmp);
            }
            return new ShortLookupTable(inBits, signbit != 0, off, outBits,
                    newData);
        }
    }

    protected LookupTable combine(LookupTable vlut, LookupTable plut,
            int outBits, boolean inverse) {
        int shift1 = plut.inBits - vlut.outBits;
        int shift2 = outBits - plut.outBits;
        int outMax = (1 << outBits) - 1;
        if (outBits <= 8) {
            byte[] newData = new byte[data.length];
            for (int i = 0; i < newData.length; i++) {
                int tmp = vlut.lookup(data[i] & 0xffff);
                tmp = plut.lookup(shift1 < 0 ? tmp >>> -shift1 : tmp << shift1);
                if (shift2 < 0) {
                    tmp >>>= -shift2;
                } else {
                    tmp <<= shift2;
                }
                newData[i] = (byte) (inverse ? outMax - tmp : tmp);
            }
            return new ByteLookupTable(inBits, signbit != 0, off, outBits, newData);
        } else {
            short[] newData = new short[data.length];
            for (int i = 0; i < newData.length; i++) {
                int tmp = vlut.lookup(data[i] & 0xffff);
                tmp = plut.lookup(shift1 < 0 ? tmp >>> -shift1 : tmp << shift1);
                if (shift2 < 0) {
                    tmp >>>= -shift2;
                } else {
                    tmp <<= shift2;
                }
                newData[i] = (short) (inverse ? outMax - tmp : tmp);
            }
            return new ShortLookupTable(inBits, signbit != 0, off, outBits,
                    newData);
        }
    }

    protected int maxOut() {
        int max = 0;
        for (int i = 0; i < data.length; i++) {
            max = Math.max(max, data[i] & 0xffff);
        }
        return max;
    }

    protected int minOut() {
        int min = 0xffff;
        for (int i = 0; i < data.length; i++) {
            min = Math.min(min, data[i] & 0xffff);
        }
        return min;
    }
}