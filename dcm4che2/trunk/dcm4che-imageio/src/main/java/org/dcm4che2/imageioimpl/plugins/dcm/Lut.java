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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Jul 23, 2007
 */
public abstract class Lut {
    
    protected final int andmask;
    protected final int ormask;
    protected final int signbit;
    protected final int off;

    protected Lut(int srcbits, boolean signed, int off) {
        this.andmask = (1 << srcbits) - 1;
        this.ormask = ~andmask;
        this.signbit = signed ? 1 << (srcbits-1) : 0;
        this.off = off;
    }
    
    protected final int toIndex(int src) {
        return ((src & signbit) != 0 ? (src | ormask) : (src & andmask)) - off;
    }
    
    public static Lut createLut(DicomObject ds) {
        int allocated = ds.getInt(Tag.BitsAllocated, 8);
        int stored = ds.getInt(Tag.BitsStored, allocated);
        int range = 1 << stored;
        int max = range-1;
        boolean signed = ds.getInt(Tag.PixelRepresentation) != 0;
        float m = ds.getFloat(Tag.RescaleSlope, 1.f);
        boolean m_neg = m < 0;
        float m_abs = m_neg ? -m : m;
        float b = ds.getFloat(Tag.RescaleIntercept, 0.f);
        float c = ds.getFloat(Tag.WindowCenter, 
                ((signed ? 0 : range/2) * m) + b);
        float w = ds.getFloat(Tag.WindowWidth, range * m_abs);
        float c_05 = c - .5f;
        float w_2 = (w-1f)/2;
        int iMin = (int) (((c_05 - w_2) - b) / m_abs);
        int iMax = (int) (((c_05 + w_2) - b) / m_abs) + 1;
        float k = ((float) range) / (iMax - iMin);
        int iMin1;
        int iMax1;
        if (signed) {
            iMin1 = Math.max(iMin, -range/2);
            iMax1 = Math.min(iMax, range/2);
        } else {
            iMin1 = Math.max(iMin, 0);
            iMax1 = Math.min(iMax, range);
        }
        int off = iMin1 - iMin;
        int len_1 = iMax1 - iMin1;
        int len = len_1 + 1;
        
        String plutShape = ds.getString(Tag.PresentationLUTShape);
        boolean inverse = plutShape != null 
                ? "INVERSE".equals(plutShape)
                : "MONOCHROME1".equals(
                        ds.getString(Tag.PhotometricInterpretation));
        if (m_neg) {
            inverse = !inverse;
        }

        if (allocated <= 8) {
            byte[] data = new byte[len];
            if (inverse) {
                for (int i = 0; i < len_1; i++) {
                    data[i] = (byte) (max - (i + off) * k);
                }
                data[len_1] = (byte) Math.max(max - (len_1 + off) * k, 0);
            } else {
                for (int i = 0; i < len_1; i++) {
                    data[i] = (byte) ((i + off) * k);
                }            
                data[len_1] = (byte) Math.min((len_1 + off) * k, max);
            }
            return new ByteLut(stored, signed, iMin1, data);         
        } else {
            short[] data = new short[len];
            if (inverse) {
                for (int i = 0; i < len_1; i++) {
                    data[i] = (short) (max - (i + off) * k);
                }
                data[len_1] = (short) Math.max(max - (len_1 + off) * k, 0);
            } else {
                for (int i = 0; i < len_1; i++) {
                    data[i] = (short) ((i + off) * k);
                }            
                data[len_1] = (short) Math.min((len_1 + off) * k, max);
            }
            return new ShortLut(stored, signed, iMin1, data);         
        }
    }
    
    public abstract void lookup(DataBuffer src, DataBuffer dst);

}
