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

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jul 23, 2007
 */
public abstract class LookupTable {

    public static final String LINEAR = "LINEAR";
    public static final String SIGMOID = "SIGMOID";

    private static final int OPAQUE = 255;

    protected final int inBits;

    protected final int andmask;

    protected final int ormask;

    protected final int signbit;

    protected final boolean preserve;

    protected int outBits;

    protected int off;

    protected LookupTable(int inBits, boolean signed, int off, int outBits,
            boolean preserve) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.andmask = (1 << inBits) - 1;
        this.ormask = ~andmask;
        this.signbit = signed ? 1 << (inBits - 1) : 0;
        this.off = (off & signbit) != 0 ? (off | ormask) : off;
        this.preserve = preserve;
    }

    public final int getOffset() {
        return off;
    }

    public abstract int length();

    public abstract byte lookupByte(int in);

    public abstract short lookupShort(int in);

    public abstract int lookup(int in);

    public abstract byte[] lookup(byte[] src, byte[] dst);

    public abstract short[] lookup(byte[] src, short[] dst);

    public abstract int[] lookup(byte[] src, int[] dst, int alpha);

    public abstract byte[] lookup(short[] src, byte[] dst);

    public abstract short[] lookup(short[] src, short[] dst);

    public abstract int[] lookup(short[] src, int[] dst, int alpha);

    public void lookup(DataBuffer src, DataBuffer dst) {
        lookup(src, dst, OPAQUE);
    }
    
    public void lookup(DataBuffer src, DataBuffer dst, int alpha) {
        switch (dst.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(src, ((DataBufferByte) dst).getData());
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(src, ((DataBufferUShort) dst).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(src, ((DataBufferShort) dst).getData());
            break;
        case DataBuffer.TYPE_INT:
            lookup(src, ((DataBufferInt) dst).getData(), alpha);
            break;
        default:
            throw new IllegalArgumentException(
                    "Illegal Type of Destination DataBuffer: " + dst);
        }       
    }

    public void lookup(DataBuffer src, byte[] dst) {
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(((DataBufferByte) src).getData(), dst);
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(((DataBufferUShort) src).getData(), dst);
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(((DataBufferShort) src).getData(), dst);
            break;
        default:
            throw new IllegalArgumentException(
                    "Illegal Type of Source DataBuffer: " + src);
        }            
    }
    
    public void lookup(DataBuffer src, short[] dst) {
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(((DataBufferByte) src).getData(), dst);
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(((DataBufferUShort) src).getData(), dst);
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(((DataBufferShort) src).getData(), dst);
            break;
        default:
            throw new IllegalArgumentException(
                    "Illegal Type of Source DataBuffer: " + src);
        }            
    }

    public void lookup(DataBuffer src, int[] dst, int alpha) {
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(((DataBufferByte) src).getData(), dst, alpha);
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(((DataBufferUShort) src).getData(), dst, alpha);
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(((DataBufferShort) src).getData(), dst, alpha);
            break;
        default:
            throw new IllegalArgumentException(
                    "Illegal Type of Source DataBuffer: " + src);
        }            
    }

    protected abstract LookupTable scale(int outBits, boolean inverse,
            short[] pval2out);

    protected abstract LookupTable combine(LookupTable other, int outBits,
            boolean inverse, short[] pval2out);

    protected abstract LookupTable combine(LookupTable vlut, LookupTable plut,
            int outBits, boolean inverse, short[] pval2out);

    protected final int toIndex(int in) {
        return ((in & signbit) != 0 ? (in | ormask) : (in & andmask)) - off;
    }


    static int inBits(short[] pval2out) {
        switch (pval2out.length) {
        case 0x100:
            return 8;
        case 0x200:
            return 9;
        case 0x400:
            return 10;
        case 0x800:
            return 11;
        case 0x1000:
            return 12;
        case 0x2000:
            return 13;
        case 0x4000:
            return 14;
        case 0x8000:
            return 15;
        case 0x10000:
            return 16;
        default:
            throw new IllegalArgumentException(
                    "pval2out.length: " + pval2out.length + " != 2^[8..16]");
        }
    }
    
    /**
     * Create ramp or sigmoid LUT for given i/o range, Rescale Slope/Intercept
     * and Window Center/Width. Create linear LUT if Window Width = 0.
     * <p>
     * If <code>vlutFct</code> is <code>null</code> or <code>"LINEAR"</code>,
     * a ramp LUT will be created. If <code>vlutFct</code> is
     * <code>"SIGMOID"</code>, a sigmoid LUT will be created.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param slope
     *            Rescale Slope (0028,1053)
     * @param intercept
     *            Rescale Intercept (0028,1052)
     * @param center
     *            Window Center (0028,1050)
     * @param width
     *            Window Width (0028,1051) or 0 (= no Window specified)
     * @param vlutFct
     *            VOI LUT Function (0028,1056)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, float slope, float intercept, float center,
            float width, String vlutFct, boolean inverse, short[] pval2out) {
        if (width == 0 || vlutFct == null || LINEAR.equals(vlutFct)) {
            return createRampLut(inBits, signed, outBits, slope, intercept,
                    center, width, inverse, pval2out);
        } else if (SIGMOID.equals(vlutFct)) {
            return createSigmoidLut(inBits, signed, outBits, slope, intercept,
                    center, width, inverse, pval2out);            
        } else {
            throw new UnsupportedOperationException(
                    "Unsupported VOI LUT function: " + vlutFct);
        }
    }
    
    private static LookupTable createRampLut(int inBits, boolean signed,
                int outBits, float slope, float intercept, float center,
                float width, boolean inverse, short[] pval2out) {
        if (slope < 0) {
            slope = -slope;
            intercept = -intercept;
            center = 1 - center;
            inverse = !inverse;
        }
        int inRange = 1 << inBits;
        int inMin = signed ? -inRange / 2 : 0;
        int inMax = inMin + inRange - 1;
        int in1;
        int in2;
        if (width == 0) {
            in1 = inMin;
            in2 = inMax;
        } else {
            float c_05 = center - .5f;
            float w_2 = (width - 1f) / 2;
            in1 = (int) (((c_05 - w_2) - intercept) / slope);
            in2 = (int) (((c_05 + w_2) - intercept) / slope) + 1;
        }
        int off = Math.max(in1, inMin);
        int iMax = Math.min(in2, inMax) - off;
        int size = iMax + 1;
        int outBits1 = pval2out == null ? outBits : inBits(pval2out);
        int outRange = 1 << outBits1;
        int pval2outShift = 16 - outBits;
        int out1;
        int out2;
        if (inverse) {
            out1 = outRange - 1;
            out2 = 0;
        } else {
            out1 = 0;
            out2 = outRange - 1;
        }
        float m = (float) (out2 - out1) / (in2 - in1);
        float b = out1 + m * (off - in1) + .5f;
        if (outBits <= 8) {
            byte[] data = new byte[size];
            if (pval2out == null) {
                for (int i = 0; i < size; i++) {
                    data[i] = (byte) (m * i + b);
                }
                if (iMax + off == in2) {
                    data[iMax] = (byte) out2;
                }
            } else {
                for (int i = 0; i < size; i++) {
                    data[i] = (byte) ((pval2out[(int) (m * i + b)] & 0xffff)
                            >>> pval2outShift);
                }
                if (iMax + off == in2) {
                    data[iMax] = (byte) ((pval2out[out2] & 0xffff)
                            >>> pval2outShift);
                }               
            }
            return new ByteLookupTable(inBits, signed, off, outBits, data);
        } else {
            short[] data = new short[size];
            if (pval2out == null) {
                for (int i = 0; i < size; i++) {
                    data[i] = (short) (m * i + b);
                }
                if (iMax + off == in2) {
                    data[iMax] = (short) out2;
                }
            } else {
                for (int i = 0; i < size; i++) {
                    data[i] = (short) ((pval2out[(int) (m * i + b)] & 0xffff)
                            >>> pval2outShift);
                }
                if (iMax + off == in2) {
                    data[iMax] = (short) ((pval2out[out2] & 0xffff)
                            >>> pval2outShift);
                }               
            }
            return new ShortLookupTable(inBits, signed, off, outBits, data);
        }
    }

    private static LookupTable createSigmoidLut(int inBits, boolean signed,
            int outBits, float slope, float intercept, float center,
            float width, boolean inverse, short[] pval2out) {
        int size = 1 << inBits;
        int off = signed ? -size / 2 : 0;
        int outBits1 = pval2out == null ? outBits : inBits(pval2out);
        int outRange = 1 << outBits1;
        int outMax = outRange - 1;
        int pval2outShift = 16 - outBits;
        float ic = (center - intercept) / slope - off;
        float k = -4 * slope / width;
        if (outBits <= 8) {
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                int tmp = (int) (outRange / (1 + Math.exp((i - ic) * k)));
                if (inverse) {
                    tmp = outMax - tmp;
                }
                if (pval2out != null) {
                    tmp = (pval2out[tmp] & 0xffff) >>> pval2outShift;
                }
                data[i] = (byte) tmp;
            }
            return new ByteLookupTable(inBits, signed, off, outBits, data);
        } else {
            short[] data = new short[size];
            for (int i = 0; i < size; i++) {
                int tmp = (int) (outRange / (1 + Math.exp((i - ic) * k)));
                if (inverse) {
                    tmp = outMax - tmp;
                }
                if (pval2out != null) {
                    tmp = (pval2out[tmp] & 0xffff) >>> pval2outShift;
                }
                data[i] = (short) tmp;
            }
            return new ShortLookupTable(inBits, signed, off, outBits, data);
        }
    }
    
    /**
     * Create LUT for given i/o range, non-linear Modality LUT and Window
     * Center/Width. Do not apply any Window if Window Width = 0.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param mLut
     *            item of Modality LUT Sequence (0028,3000)
     * @param center
     *            Window Center (0028,1050)
     * @param width
     *            Window Width (0028,1051) or 0 (= no Window specified)
     * @param vlutFct
     *            VOI LUT Function (0028,1056)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, DicomObject mLut, float center, float width,
            String vlutFct, boolean inverse, short[] pval2out) {
        LookupTable mlut = createLut(inBits, signed, mLut);
        if (width == 0) {
            return mlut.scale(outBits, inverse, pval2out);
        }
        LookupTable vlut = createLut(mlut.outBits, false, outBits, 1, 0,
                center, width, vlutFct, inverse, pval2out);
        return mlut.combine(vlut, outBits, false, null);
    }

    private static LookupTable createLut(int inBits, boolean signed,
            DicomObject ds) {
        int[] desc = ds.getInts(Tag.LUTDescriptor);
        byte[] data = ds.getBytes(Tag.LUTData);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException(
                    "Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (data == null) {
            throw new IllegalArgumentException("Missing LUT Data!");
        }
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int off = desc[1];
        int bits = desc[2];
        if (inBits == 0) {
            // ignore offset for P-LUT
            off = 0;
            for (int i = len - 1; i != 0; i >>>= 1) {
                ++inBits;
            }
        }
        if (data.length == len) {
            return new ByteLookupTable(inBits, signed, off, bits, data, true);
        } else if (data.length == len << 1) {
            return new ShortLookupTable(inBits, signed, off, bits, ds
                    .bigEndian() ? ByteUtils.bytesBE2shorts(data) : ByteUtils
                    .bytesLE2shorts(data), true);
        }
        throw new IllegalArgumentException("LUT Data length: " + data.length
                + " mismatch entry value: " + len + " in LUT Descriptor");
    }

    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept and non-linear
     * VOI LUT.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param slope
     *            Rescale Slope (0028,1053)
     * @param intercept
     *            Rescale Intercept (0028,1052)
     * @param voiLut
     *            item of VOI LUT Sequence (0028,3010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, float slope, float intercept, DicomObject voiLut,
            boolean inverse, short[] pval2out) {
        return createLut(inBits, signed, slope, intercept, voiLut).scale(
                outBits, inverse, pval2out);
    }

    private static LookupTable createLut(int inBits, boolean signed,
            float slope, float intercept, DicomObject voiLut) {
        if (slope == 1) {
            LookupTable lut = createLut(inBits, signed, voiLut);
            lut.off -= intercept;
            return lut;
        } else {
            LookupTable vlut = createLut(32, true, voiLut);
            float in1 = (vlut.off - intercept) / slope;
            float in2 = in1 + vlut.length() / slope;
            int off = (int) Math.floor(Math.min(in1, in2));
            int len = ((int) Math.ceil(Math.max(in1, in2))) - off;
            short[] data = new short[len];
            for (int i = 0; i < data.length; i++) {
                data[i] = vlut.lookupShort(Math.round(i * slope + intercept));
            }
            return new ShortLookupTable(inBits, signed, off, vlut.outBits, data);
        }
    }

    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept, Window
     * Center/Width and non-linear Presentation LUT. Apply no Window if Window
     * Width = 0.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param slope
     *            Rescale Slope (0028,1053)
     * @param intercept
     *            Rescale Intercept (0028,1052)
     * @param center
     *            Window Center (0028,1050)
     * @param width
     *            Window Width (0028,1051) or 0 (= no Window specified)
     * @param vlutFct
     *            VOI LUT Function (0028,1056)
     * @param pLut
     *            item of Presentation LUT Sequence (2050,0010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, float slope, float intercept, float center,
            float width, String vlutFct, DicomObject pLut, boolean inverse,
            short[] pval2out) {
        LookupTable plut = createLut(0, false, pLut);
        LookupTable vlut = createLut(inBits, signed, plut.inBits, slope,
                intercept, center, width, vlutFct, false, null);
        return vlut.combine(plut, outBits, inverse, pval2out);
    }

    /**
     * Create LUT for given i/o range, non-linear Modality LUT and non-linear
     * VOI LUT.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param mLut
     *            item of Modality LUT Sequence (0028,3000)
     * @param voiLut
     *            item of VOI LUT Sequence (0028,3010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, DicomObject mLut, DicomObject voiLut, boolean inverse,
            short[] pval2out) {
        LookupTable mlut = createLut(inBits, signed, voiLut);
        LookupTable vlut = createLut(mlut.outBits, false, voiLut);
        return mlut.combine(vlut, outBits, inverse, pval2out);
    }

    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept, non-linear VOI
     * LUT and non-linear Presentation LUT.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param slope
     *            Rescale Slope (0028,1053)
     * @param intercept
     *            Rescale Intercept (0028,1052)
     * @param voiLut
     *            item of VOI LUT Sequence (0028,3010)
     * @param pLut
     *            item of Presentation LUT Sequence (2050,0010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, float slope, float intercept, DicomObject voiLut,
            DicomObject pLut, boolean inverse, short[] pval2out) {
        LookupTable vlut = createLut(inBits, signed, slope, intercept, voiLut);
        LookupTable plut = createLut(0, false, pLut);
        return vlut.combine(plut, outBits, inverse, pval2out);
    }

    /**
     * Create LUT for given i/o range, non-linear Modality LUT, Window
     * Center/Width and non-linear Presentation LUT. Apply no Window if Window
     * Width = 0.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param mLut
     *            item of Modality LUT Sequence (0028,3000)
     * @param center
     *            Window Center (0028,1050)
     * @param width
     *            Window Width (0028,1051) or 0 (= no Window specified)
     * @param vlutFct
     *            VOI LUT Function (0028,1056)
     * @param pLut
     *            item of Presentation LUT Sequence (2050,0010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, DicomObject mLut, float center, float width,
            String vlutFct, DicomObject pLut, boolean inverse,
            short[] pval2out) {
        LookupTable mlut = createLut(inBits, signed, mLut);
        LookupTable plut = createLut(0, false, pLut);
        if (width == 0) {
            return mlut.combine(plut, outBits, inverse, pval2out);
        } else {
            LookupTable vlut = createLut(mlut.outBits, false, plut.inBits, 1,
                    0, center, width, vlutFct, false, null);
            return mlut.combine(vlut, plut, outBits, inverse, pval2out);
        }
    }

    /**
     * Create LUT for given i/o range, non-linear Modality LUT, non-linear VOI
     * LUT and non-linear Presentation LUT.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param inBits
     *            number of significant bits within input values
     * @param signed
     *            specifies if input values are signed or unsigned
     * @param outBits
     *            bit depth of output range
     * @param mLut
     *            item of Modality LUT Sequence (0028,3000)
     * @param voiLut
     *            item of VOI LUT Sequence (0028,3010)
     * @param pLut
     *            item of Presentation LUT Sequence (2050,0010)
     * @param inverse
     *            specifies if output shall be inverted
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLut(int inBits, boolean signed,
            int outBits, DicomObject mLut, DicomObject voiLut,
            DicomObject pLut, boolean inverse, short[] pval2out) {
        LookupTable mlut = createLut(inBits, signed, mLut);
        LookupTable vlut = createLut(mlut.outBits, false, voiLut);
        LookupTable plut = createLut(0, false, pLut);
        return mlut.combine(vlut, plut, outBits, inverse, pval2out);
    }

    /**
     * Create LUT for given DICOM image and output range. If the image specifies
     * multiple non-linear VOI LUTs, the VOI LUT specified by the first item of
     * the VOI LUT Sequence (0028,3010) will be applied. If the image does not
     * specify any non-linear VOI LUT, but multiple values for Window
     * Center/Width, the first Window Center/Width value will be applied.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param img
     *            DICOM image
     * @param outBits
     *            bit depth of output range
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLutForImage(DicomObject img, int bitsOut,
            short[] pval2out) {
        DicomObject voiLut = img.getNestedDicomObject(Tag.VOILUTSequence);
        if (voiLut != null) {
            return createLutForImage(img, voiLut, bitsOut, pval2out);
        }
        float c = img.getFloat(Tag.WindowCenter);
        float w = img.getFloat(Tag.WindowWidth);
        String vlutFct = img.getString(Tag.VOILUTFunction);
        return createLutForImage(img, c, w, vlutFct, bitsOut, pval2out);
    }

    static boolean isModalityLUTcontainsPixelIntensityRelationshipLUT(
            DicomObject img) {
        return isModalityLUTcontainsPixelIntensityRelationshipLUT(img
                .getString(Tag.SOPClassUID));
    }

    private static boolean isModalityLUTcontainsPixelIntensityRelationshipLUT(
            String uid) {
        return UID.XRayAngiographicImageStorage.equals(uid)
                || UID.XRayAngiographicBiPlaneImageStorageRetired.equals(uid)
                || UID.XRayRadiofluoroscopicImageStorage.equals(uid);
    }

    /**
     * Create LUT for given DICOM image, Window Center/Width and output range.
     * Apply no Window if Window Width = 0.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param img
     *            DICOM image
     * @param center
     *            Window Center (0028,1050)
     * @param width
     *            Window Width (0028,1051) or 0 (= no Window specified)
     * @param vlutFct
     *            VOI LUT Function (0028,1056)
     * @param outBits
     *            bit depth of output range
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLutForImage(DicomObject img, float center,
            float width, String vlutFct, int outBits, short[] pval2out) {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        float slope = img.getFloat(Tag.RescaleSlope, 1.f);
        float intercept = img.getFloat(Tag.RescaleIntercept, 0.f);
        DicomObject mLut = isModalityLUTcontainsPixelIntensityRelationshipLUT(img) ? null
                : img.getNestedDicomObject(Tag.ModalityLUTSequence);
        boolean inverse = isInverse(img);
        if (mLut != null) {
            return createLut(stored, signed, outBits, mLut, center, width,
                    vlutFct, inverse, pval2out);
        } else {
            return createLut(stored, signed, outBits, slope, intercept,
                    center, width, vlutFct, inverse, pval2out);
        }
    }

    private static boolean isInverse(DicomObject img) {
        String shape = img.getString(Tag.PresentationLUTShape);
        return shape != null ? "INVERSE".equals(shape) : "MONOCHROME1"
                .equals(img.getString(Tag.PhotometricInterpretation));
    }

    /**
     * Create LUT for given DICOM image, non-linear VOI LUT and output range.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param img
     *            DICOM image
     * @param voiLut
     *            item of VOI LUT Sequence (0028,3010)
     * @param outBits
     *            bit depth of output range
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLutForImage(DicomObject img,
            DicomObject voiLut, int outBits, short[] pval2out) {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        float slope = img.getFloat(Tag.RescaleSlope, 1.f);
        float intercept = img.getFloat(Tag.RescaleIntercept, 0.f);
        DicomObject mLut = 
                isModalityLUTcontainsPixelIntensityRelationshipLUT(img) ? null
                        : img.getNestedDicomObject(Tag.ModalityLUTSequence);
        boolean inverse = isInverse(img);
        if (mLut != null) {
            return createLut(stored, signed, outBits, mLut, voiLut, inverse,
                    pval2out);
        } else {
            return createLut(stored, signed, outBits, slope, intercept, voiLut,
                    inverse, pval2out);
        }
    }

    /**
     * Create LUT for given DICOM image with DICOM Presentation State and output
     * range.
     * <p>
     * If <code>pval2out</code> is not <code>null</code>, the output will
     * be weighted according this function, where the highest input value 
     * (p-value) maps to array index length-1 and the highest output value
     * (2^outBits-1) is represented by 0xFFFF. Length of <code>pval2out</code>
     * must be equal to 2^inBits, with inBits in the range [8, 16].
     * 
     * @param img
     *            DICOM image
     * @param pr
     *            DICOM Presentation State
     * @param outBits
     *            bit depth of output range
     * @param pval2out
     *            p-value to output map or <code>null</code>
     * @return created LUT
     */
    public static LookupTable createLutForImageWithPR(DicomObject img,
            DicomObject pr, int outBits, short[] pval2out) {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        float slope = pr.getFloat(Tag.RescaleSlope, 1.f);
        float intercept = pr.getFloat(Tag.RescaleIntercept, 0.f);
        DicomObject mLut = pr.getNestedDicomObject(Tag.ModalityLUTSequence);
        float center = 0.f;
        float width = 0.f;
        String vlutFct = null;
        DicomObject voiLut = null;
        DicomObject voi = selectVoiItem(img, pr);
        if (voi != null) {
            center = voi.getFloat(Tag.WindowCenter);
            width = voi.getFloat(Tag.WindowWidth);
            vlutFct = voi.getString(Tag.VOILUTFunction);
            voiLut = voi.getNestedDicomObject(Tag.VOILUTSequence);
        }
        boolean inverse = "INVERSE".equals(pr
                .getString(Tag.PresentationLUTShape));
        DicomObject pLut = pr.getNestedDicomObject(Tag.PresentationLUTSequence);
        if (mLut == null) {
            if (voiLut == null) {
                if (pLut == null) {
                    return LookupTable.createLut(stored, signed, outBits,
                            slope, intercept, center, width, vlutFct, inverse,
                            pval2out);
                } else {
                    return LookupTable.createLut(stored, signed, outBits,
                            slope, intercept, center, width, vlutFct, pLut,
                            false, pval2out);
                }
            } else {
                if (pLut == null) {
                    return LookupTable.createLut(stored, signed, outBits,
                            slope, intercept, voiLut, inverse, pval2out);
                } else {
                    return LookupTable.createLut(stored, signed, outBits,
                            slope, intercept, voiLut, pLut, false, pval2out);
                }
            }
        } else {
            if (voiLut == null) {
                if (pLut == null) {
                    return LookupTable.createLut(stored, signed, outBits, mLut,
                            center, width, vlutFct, inverse, pval2out);
                } else {
                    return LookupTable.createLut(stored, signed, outBits, mLut,
                            center, width, vlutFct, pLut, false, pval2out);
                }
            } else {
                if (pLut == null) {
                    return LookupTable.createLut(stored, signed, outBits, mLut,
                            voiLut, inverse, pval2out);
                } else {
                    return LookupTable.createLut(stored, signed, outBits, mLut,
                            voiLut, pLut, false, pval2out);
                }
            }
        }
    }

    private static DicomObject selectVoiItem(DicomObject img, DicomObject pr) {
        DicomElement voisq = pr.get(Tag.SoftcopyVOILUTSequence);
        if (voisq == null) {
            return null;
        }
        String iuid = img.getString(Tag.SOPInstanceUID);
        for (int i = 0, n = voisq.countItems(); i < n; i++) {
            DicomObject item = voisq.getDicomObject(i);
            DicomElement refImgs = item.get(Tag.ReferencedImageSequence);
            if (refImgs == null) {
                return item;
            }
            for (int j = 0, m = refImgs.countItems(); j < m; j++) {
                DicomObject refImage = refImgs.getDicomObject(j);
                if (iuid.equals(refImage
                        .getString(Tag.ReferencedSOPInstanceUID))) {
                    return item;
                }
            }
        }
        return null;
    }

}
