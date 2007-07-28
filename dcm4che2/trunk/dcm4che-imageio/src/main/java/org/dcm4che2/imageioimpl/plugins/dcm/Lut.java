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
 
package org.dcm4che2.imageioimpl.plugins.dcm;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
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
public abstract class Lut {
    
    protected final int inBits;
    protected final int andmask;
    protected final int ormask;
    protected final int signbit;
    protected final boolean preserve;
    protected int outBits;
    protected int off;

    protected Lut(int inBits, boolean signed, int off, int outBits,
            boolean preserve) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.andmask = (1 << inBits) - 1;
        this.ormask = ~andmask;
        this.signbit = signed ? 1 << (inBits-1) : 0;
        this.off = (off & signbit) != 0 ? (off | ormask) : off;
        this.preserve = preserve;
    }
    
    public final int offset() {
        return off;
    }
    
    public abstract int length();
    
    public abstract byte lookupByte(int in);
    
    public abstract short lookupShort(int in); 

    public abstract void lookup(byte[] in, byte[] out);

    public abstract void lookup(byte[] in, short[] out);

    public abstract void lookup(short[] in, byte[] out);

    public abstract void lookup(short[] in, short[] out);
    
    protected abstract Lut scale(int outBits);
    protected abstract Lut inverse();   
    protected abstract Lut combine(Lut other);

    public void lookup(DataBuffer in, DataBuffer out) {
        switch (in.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(((DataBufferByte) in).getData(), out);
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(((DataBufferUShort) in).getData(), out);
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(((DataBufferShort) in).getData(), out);
            break;
        default:
            throw new IllegalArgumentException("in: " + in);
        }
    }
    
    private void lookup(short[] in, DataBuffer out) {
        switch (out.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(in, ((DataBufferByte) out).getData());
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(in, ((DataBufferUShort) out).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(in, ((DataBufferShort) out).getData());
            break;
        default:
            throw new IllegalArgumentException("out: " + out);
        }
    }

    private void lookup(byte[] in, DataBuffer out) {
        switch (out.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            lookup(in, ((DataBufferByte) out).getData());
            break;
        case DataBuffer.TYPE_USHORT:
            lookup(in, ((DataBufferUShort) out).getData());
            break;
        case DataBuffer.TYPE_SHORT:
            lookup(in, ((DataBufferShort) out).getData());
            break;
        default:
            throw new IllegalArgumentException("out: " + out);
        }
    }

    protected final int toIndex(int in) {
        return ((in & signbit) != 0 ? (in | ormask) : (in & andmask)) - off;
    }
    
    /**
     * Create ramp LUT for given i/o range, Rescale Slope/Intercept and
     * Window Center/Width. Create linear LUT if Window Width = 0.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param slope Rescale Slope (0028,1053)
     * @param intercept Rescale Intercept (0028,1052)
     * @param center Window Center (0028,1050)
     * @param width Window Width (0028,1051) or 0 (= no Window specified)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            float slope, float intercept, float center, float width,
            boolean inverse) {
        if (slope < 0) {
            slope = -slope;
            intercept = -intercept;
            center = 1 - center;
            inverse = !inverse;
        }
        int inRange = 1 << inBits;
        int inMin = signed ? -inRange/2 : 0;
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
        int outRange = 1 << outBits;
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
            byte[] ramp = new byte[size];
            for (int i = 0; i < size; i++) {
                ramp[i] = (byte) (m * i + b);
            }
            if (iMax + off == in2) {
                ramp[iMax] = (byte) out2;
            }
            return new Byte(inBits, signed, off, outBits, ramp);           
        } else {
            short[] ramp = new short[size];
            for (int i = 0; i < size; i++) {
                ramp[i] = (short) (m * i + b);
            }
            if (iMax + off == in2) {
                ramp[iMax] = (short) out2;
            }
            return new Short(inBits, signed, off, outBits, ramp);
        }
    }

    /**
     * Create LUT for given i/o range, non-linear Modality LUT and
     * Window Center/Width. Do not apply any Window if Window Width = 0.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param mLut item of Modality LUT Sequence (0028,3000)
     * @param center Window Center (0028,1050)
     * @param width Window Width (0028,1051) or 0 (= no Window specified)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            DicomObject mLut, float center, float width, boolean inverse) {
        Lut mlut = createLut(inBits, signed, mLut);
        if (width == 0) {
            Lut lut = mlut.scale(outBits);
            return inverse ? lut.inverse() : lut;
        }
        Lut vlut = createLut(mlut.outBits, false, outBits, 1, 0, 
                center, width, inverse);
        Lut lut = mlut.combine(vlut);
        return lut;
    }

    private static Lut createLut(int inBits, boolean signed, DicomObject ds) {
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
            for (int i = len-1; i != 0; i >>>= 1) {
                ++inBits;
            }
        }
        if (data.length == len) {
            return new Byte(inBits, signed, off, bits, data, true);
        } else if (data.length == len << 1) {
            return new Short(inBits, signed, off, bits,
                    ds.bigEndian() ? ByteUtils.bytesBE2shorts(data)
                                   : ByteUtils.bytesLE2shorts(data), true);
        }
        throw new IllegalArgumentException("LUT Data length: " + data.length
            +  " mismatch entry value: " + len + " in LUT Descriptor");            
    }
    
    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept and
     * non-linear VOI LUT.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param slope Rescale Slope (0028,1053)
     * @param intercept Rescale Intercept (0028,1052)
     * @param voiLut item of VOI LUT Sequence (0028,3010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            float slope, float intercept, DicomObject voiLut, boolean inverse) {
        Lut lut = createLut(inBits, signed, slope, intercept, voiLut)
                .scale(outBits);
        return inverse ? lut.inverse() : lut;
    }

    private static Lut createLut(int inBits, boolean signed,
            float slope, float intercept, DicomObject voiLut) {
        if (slope == 1) {
            Lut lut = createLut(inBits, signed, voiLut);
            lut.off -= intercept;
            return lut;
        } else {
            Lut vlut = createLut(32, true, voiLut);
            float in1 = (vlut.offset() - intercept) / slope;                
            float in2 = in1 + vlut.length() / slope;              
            int off = (int) Math.floor(Math.min(in1, in2));            
            int len = ((int) Math.ceil(Math.max(in1, in2))) - off;
            short[] data = new short[len];
            for (int i = 0; i < data.length; i++) {
                data[i] = vlut.lookupShort(Math.round(i * slope + intercept));
            }
            return new Short(inBits, signed, off, vlut.outBits, data);
        }
    }

    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept,
     * Window Center/Width and non-linear Presentation LUT.
     * Apply no Window if Window Width = 0.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param slope Rescale Slope (0028,1053)
     * @param intercept Rescale Intercept (0028,1052)
     * @param center Window Center (0028,1050)
     * @param width Window Width (0028,1051) or 0 (= no Window specified)
     * @param pLut item of Presentation LUT Sequence (2050,0010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            float slope, float intercept, float center, float width,
            DicomObject pLut, boolean inverse) {
        Lut plut = createLut(0, false, pLut);
        Lut vlut = createLut(inBits, signed, plut.inBits,
                slope, intercept, center, width, false);
        final Lut lut = vlut.combine(plut).scale(outBits);
        return inverse ? lut.inverse() : lut;
    }
    
    /**
     * Create LUT for given i/o range, non-linear Modality LUT and
     * non-linear VOI LUT.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param mLut item of Modality LUT Sequence (0028,3000)
     * @param voiLut item of VOI LUT Sequence (0028,3010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            DicomObject mLut, DicomObject voiLut, boolean inverse) {
        Lut mlut = createLut(inBits, signed, voiLut);
        Lut vlut = createLut(mlut.outBits, false, voiLut);
        Lut lut = mlut.combine(vlut).scale(outBits);
        return inverse ? lut.inverse() : lut;
    }
   
    /**
     * Create LUT for given i/o range, Rescale Slope/Intercept,
     * non-linear VOI LUT and non-linear Presentation LUT.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param slope Rescale Slope (0028,1053)
     * @param intercept Rescale Intercept (0028,1052)
     * @param voiLut item of VOI LUT Sequence (0028,3010)
     * @param pLut item of Presentation LUT Sequence (2050,0010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            float slope, float intercept, DicomObject voiLut,
            DicomObject pLut, boolean inverse) {
        Lut vlut = createLut(inBits, signed, slope, intercept, voiLut);
        Lut plut = createLut(0, false, pLut);
        Lut lut = vlut.scale(plut.inBits).combine(plut).scale(outBits);
        return inverse ? lut.inverse() : lut;
    }
   
    /**
     * Create LUT for given i/o range, non-linear Modality LUT,
     * Window Center/Width and non-linear Presentation LUT.
     * Apply no Window if Window Width = 0.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param mLut item of Modality LUT Sequence (0028,3000)
     * @param center Window Center (0028,1050)
     * @param width Window Width (0028,1051) or 0 (= no Window specified)
     * @param pLut item of Presentation LUT Sequence (2050,0010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            DicomObject mLut, float center, float width, DicomObject pLut,
            boolean inverse) {
        Lut mlut = createLut(inBits, signed, mLut);
        Lut plut = createLut(0, false, pLut);
        Lut lut;
        if (width == 0) {
            lut = mlut.scale(plut.inBits);
        } else {
            Lut vlut = createLut(mlut.outBits, false, plut.inBits, 1, 0, 
                    center, width, false);
            lut = mlut.combine(vlut);
        }
        lut = lut.combine(plut).scale(outBits);
        return inverse ? lut.inverse() : lut;
    }

    /**
     * Create LUT for given i/o range, non-linear Modality LUT,
     * non-linear VOI LUT and non-linear Presentation LUT.
     * 
     * @param inBits number of significant bits within input values
     * @param signed specifies if input values are signed or unsigned
     * @param outBits bit depth of output range
     * @param mLut item of Modality LUT Sequence (0028,3000)
     * @param voiLut item of VOI LUT Sequence (0028,3010)
     * @param pLut item of Presentation LUT Sequence (2050,0010)
     * @param inverse specifies if output shall be inversed
     * @return created LUT
     */
    public static Lut createLut(int inBits, boolean signed, int outBits, 
            DicomObject mLut, DicomObject voiLut, DicomObject pLut,
            boolean inverse) {
        Lut mlut = createLut(inBits, signed, mLut);
        Lut vlut = createLut(mlut.outBits, false, voiLut);
        Lut plut = createLut(0, false, pLut);
        Lut lut = mlut.combine(vlut).scale(plut.inBits).combine(plut)
                        .scale(outBits);
        return inverse ? lut.inverse() : lut;
    }

    /**
     * Create LUT for given DICOM image and output range.
     * If the image specifies multiple non-linear VOI LUTs, the VOI LUT specified 
     * by the first item of the VOI LUT Sequence (0028,3010) will be applied.
     * If the image does not specify any non-linear VOI LUT, but multiple
     * values for Window Center/Width, the first Window Center/Width value
     * will be applied.
     * 
     * @param img DICOM image
     * @param outBits bit depth of output range
     * @return created LUT
     */
    public static Lut createLutForImage(DicomObject img, int bitsOut) {
        DicomObject voiLut = img.getNestedDicomObject(Tag.VOILUTSequence);
        if (voiLut != null) {
            return createLutForImage(img, voiLut, bitsOut);
        }
        float c = img.getFloat(Tag.WindowCenter);
        float w = img.getFloat(Tag.WindowWidth);
        return createLutForImage(img, c, w, bitsOut);
    }

    private static boolean isModalityLUTcontainsPixelIntensityRelationshipLUT(
            DicomObject img) {
        return isModalityLUTcontainsPixelIntensityRelationshipLUT(
                img.getString(Tag.SOPClassUID));
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
     * 
     * @param img DICOM image
     * @param center Window Center (0028,1050)
     * @param width Window Width (0028,1051) or 0 (= no Window specified)
     * @param outBits bit depth of output range
     * @return created LUT
     */
    public static Lut createLutForImage(DicomObject img,
            float center, float width, int outBits) {
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
            return createLut(stored, signed, outBits, 
                    mLut, center, width, inverse);
        } else {
            return createLut(stored, signed, outBits, 
                    slope, intercept, center, width, inverse);
        }
    }

    private static boolean isInverse(DicomObject img) {
        String shape = img.getString(Tag.PresentationLUTShape);
        return shape != null ? "INVERSE".equals(shape) 
                : "MONOCHROME1".equals(
                        img.getString(Tag.PhotometricInterpretation));
    }

    /**
     * Create LUT for given DICOM image, non-linear VOI LUT and output range.
     * Apply no Window if Window Width = 0.
     * 
     * @param img DICOM image
     * @param voiLut item of VOI LUT Sequence (0028,3010)
     * @param outBits bit depth of output range
     * @return created LUT
     */
    public static Lut createLutForImage(DicomObject img,
            DicomObject voiLut, int outBits) {
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
            return createLut(stored, signed, outBits, 
                    mLut, voiLut, inverse);
        } else {
            return createLut(stored, signed, outBits, 
                    slope, intercept, voiLut, inverse);
        }
    }
    
    /**
     * Create LUT for given DICOM image with DICOM Presentation State and 
     * output range.
     * Apply no Window if Window Width = 0.
     * 
     * @param img DICOM image
     * @param pr DICOM Presentation State
     * @param outBits bit depth of output range
     * @return created LUT
     */
    public static Lut createLutForImageWithPR(DicomObject img,
            DicomObject pr, int outBits) {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        float slope = pr.getFloat(Tag.RescaleSlope, 1.f);
        float intercept = pr.getFloat(Tag.RescaleIntercept, 0.f);
        DicomObject mLut = pr.getNestedDicomObject(Tag.ModalityLUTSequence);
        float center = 0.f;
        float width = 0.f;
        DicomObject voiLut = null;
        DicomObject voi = selectVoiItem(img, pr);
        if (voi != null) {
            center = voi.getFloat(Tag.WindowCenter);
            width = voi.getFloat(Tag.WindowWidth);
            voiLut = voi.getNestedDicomObject(Tag.VOILUTSequence);
        }
        boolean inverse = "INVERSE".equals(
                pr.getString(Tag.PresentationLUTShape));
        DicomObject pLut = pr.getNestedDicomObject(Tag.PresentationLUTSequence);
        if (mLut == null) {
            if (voiLut == null) {
                if (pLut == null) {
                    return Lut.createLut(stored, signed, outBits, 
                            slope, intercept, center, width, inverse);
                } else {
                    return Lut.createLut(stored, signed, outBits, 
                            slope, intercept, center, width, pLut, false);                    
                }
            } else {
                if (pLut == null) {
                    return Lut.createLut(stored, signed, outBits,
                            slope, intercept, voiLut, inverse);
                } else {
                    return Lut.createLut(stored, signed, outBits,
                            slope, intercept, voiLut, pLut, false);                    
                }                
            }
        } else {
            if (voiLut == null) {
                if (pLut == null) {
                    return Lut.createLut(stored, signed, outBits,
                            mLut, center, width, inverse);
                } else {
                    return Lut.createLut(stored, signed, outBits,
                            mLut, center, width, pLut, false);                    
                }
            } else {
                if (pLut == null) {
                    return Lut.createLut(stored, signed, outBits,
                            mLut, voiLut, inverse);
                } else {
                    return Lut.createLut(stored, signed, outBits,
                            mLut, voiLut, pLut, false);                    
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
                if (iuid.equals(
                        refImage.getString(Tag.ReferencedSOPInstanceUID))) {
                    return item;
                }
            }
        }
        return null;       
    }

    public static class Byte extends Lut {
        
        private byte[] data;

        public Byte(int inBits, boolean signed, int off, int outBits,
                byte[] data) {
            this(inBits, signed, off, outBits, data, false);
        }
        
        public Byte(int inBits, boolean signed, int off, int outBits,
                byte[] data, boolean preserve) {
            super(inBits, signed, off, outBits, preserve);
            this.data = data;
        }

        public final int length() {
            return data.length;
        }
        
        public final byte lookupByte(int in) {
            int i = toIndex(in);
            return i <= 0 ? data[0] : i >= data.length ? data[data.length - 1]
                                                              : data[i];
        }
        
        public final short lookupShort(int in) {
            return (short) (lookupByte(in) & 0xff);
        }
        
        public final void lookup(byte[] in, byte[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupByte(in[i]);
            }
        }

        public final void lookup(byte[] in, short[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupShort(in[i]);
            }
        }

        public final void lookup(short[] in, byte[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupByte(in[i]);
            }
        }

        public final void lookup(short[] in, short[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupShort(in[i]);
            }
        }

        protected Lut inverse() {
            int outMax = (1 << outBits) - 1;
            byte[] newData = preserve ? new byte[data.length] : data;
            for (int i = 0; i < newData.length; i++) {
                newData[i] = (byte) (outMax - data[i]);
            }
            return preserve 
                    ? new Byte(inBits, signbit != 0, off, outBits, newData)
                    : this;
        }

        protected Lut scale(int outBits) {
            int shift = outBits - this.outBits;
            if (shift == 0) {
                return this;
            }
            if (outBits <= 8) {
                byte[] newData = preserve ? new byte[data.length] : data;
                if (shift < 0) {
                    shift = -shift;                
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (byte) ((data[i] & 0xff) >>> shift);
                    }              
                } else {
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (byte) (data[i] << shift);
                    }              
                }
                if (preserve) {
                    return new Byte(inBits, signbit != 0, off, outBits, newData);
                } else {
                    this.outBits = outBits;
                    return this;
                }
            } else {
                short[] newData = new short[data.length];
                if (shift < 0) {
                    shift = -shift;                
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (short) ((data[i] & 0xff) >>> shift);
                    }              
                } else {
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (short) ((data[i] & 0xff) << shift);
                    }
                }              
                return new Short(inBits, signbit != 0, off, outBits, newData);           
            }
        }

        protected Lut combine(Lut other) {
            if (other.outBits <= 8) {
                byte[] newData = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    newData[i] = other.lookupByte(data[i] & 0xff);
                }
                return new Byte(inBits, signbit != 0, off, other.outBits,
                        newData);
            } else {
                short[] newData = new short[data.length];
                for (int i = 0; i < data.length; i++) {
                    newData[i] = other.lookupShort(data[i] & 0xff);
                }
                return new Short(inBits, signbit != 0, off, other.outBits,
                        newData);
            }
        }

    }
    
    public static class Short extends Lut {
        
        private short[] data;

        public Short(int inBits, boolean signed, int off, int outBits,
                short[] data) {
            this(inBits, signed, off, outBits, data, false);
        }
        
        public Short(int inBits, boolean signed, int off, int outBits,
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
            int tmp = ((in & signbit) != 0 ? (in | ormask) : (in & andmask))
                    - off;
            return tmp <= 0 ? data[0]
                    : tmp >= data.length ? data[data.length - 1] : data[tmp];
        }
        
        public final void lookup(byte[] in, byte[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupByte(in[i]);
            }
        }
        
        public final void lookup(byte[] in, short[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupShort(in[i]);
            }
        }        
        public final void lookup(short[] in, byte[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupByte(in[i]);
            }
        }
        
        public final void lookup(short[] in, short[] out) {
            for (int i = 0; i < in.length; i++) {
                out[i] = lookupShort(in[i]);
            }
        }

        protected Lut inverse() {
            int outMax = (1 << outBits) - 1;
            short[] newData = preserve ? new short[data.length] : data;
            for (int i = 0; i < newData.length; i++) {
                newData[i] = (short) (outMax - data[i]);
            }
            return preserve 
                    ? new Short(inBits, signbit != 0, off, outBits, newData)
                    : this;
        }

        protected Lut scale(int outBits) {
            int shift = outBits - this.outBits;
            if (shift == 0) {
                return this;
            }
            if (preserve && outBits <= 8) {
                byte[] newData = new byte[data.length];
                if (shift < 0) {
                    shift = -shift;                
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (byte) ((data[i] & 0xffff) >>> shift);
                    }              
                } else {
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (byte) (data[i] << shift);
                    }              
                }
                return new Byte(inBits, signbit != 0, off, outBits, newData);
            } else {
                short[] newData = preserve ? new short[data.length] : data;
                if (shift < 0) {
                    shift = -shift;                
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (short) ((data[i] & 0xffff) >>> shift);
                    }              
                } else {
                    for (int i = 0; i < newData.length; i++) {
                        newData[i] = (short) (data[i] << shift);
                    }
                }
                if (preserve) {
                    return new Short(inBits, signbit != 0, off, outBits, newData);
                } else {
                    this.outBits = outBits;
                    return this;
                }
            }
        }

        protected Lut combine(Lut other) {
            if (other.outBits <= 8) {
                byte[] newData = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    newData[i] = other.lookupByte(data[i] & 0xffff);
                }
                return new Byte(inBits, signbit != 0, off, other.outBits,
                        newData);
            } else { 
                short[] newData = new short[data.length];
                for (int i = 0; i < data.length; i++) {
                    newData[i] = other.lookupShort(data[i] & 0xffff);
                }
                return new Short(inBits, signbit != 0, off, other.outBits,
                        newData);
            }
        }
    }    
 }
