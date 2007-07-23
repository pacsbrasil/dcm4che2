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
public abstract class LUT {
    
    protected final int andmask;
    protected final int srcoff;
    protected final int dstoff;

    protected LUT(int srcbits, int srcoff, int dstoff) {
        this.andmask = (1 << srcbits) - 1;
        this.srcoff = srcoff;
        this.dstoff = dstoff;
    }
    
    public static LUT createLUT(DicomObject ds) {
        int allocated = ds.getInt(Tag.BitsAllocated, 8);
        int stored = ds.getInt(Tag.BitsStored, allocated);
        int range = 1 << stored;
        int max = range-1;
        boolean signed = ds.getInt(Tag.PixelRepresentation) != 0;
        float m = ds.getFloat(Tag.RescaleSlope, 1.f);
        float b = ds.getFloat(Tag.RescaleIntercept, 0.f);
        float c = ds.getFloat(Tag.WindowCenter, 
                ((signed ? 0 : range/2) * m) + b);
        float w = ds.getFloat(Tag.WindowWidth, range * m);
        int iMin = (int) (((c - w/2) - b) / m);
        int iMax = iMin + (int) (w / m);
        int srcoff;
        int iMin1;
        int iMax1;
        if (signed) {
            srcoff = range/2;
            iMin1 = Math.max(iMin, -range/2);
            iMax1 = Math.min(iMax, range/2);
        } else {
            srcoff = 0;
            iMin1 = Math.max(iMin, 0);
            iMax1 = Math.min(iMax, range);
        }
        int off = iMin1 - iMin;
        
        String plutShape = ds.getString(Tag.PresentationLUTShape);
        boolean inverse = plutShape != null 
                ? "INVERSE".equals(plutShape)
                : "MONOCHROME1".equals(
                        ds.getString(Tag.PhotometricInterpretation));

        if (allocated <= 8) {
            byte[] data = new byte[iMax1-iMin1];
            if (inverse) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (max - (i + off) * m / w * range);
                }
            } else {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) ((i + off) * m / w * range);
                }            
            }
            return new ByteLUT(stored, srcoff, iMin1 + srcoff, data);         
        } else {
            short[] data = new short[iMax1-iMin1];
            if (inverse) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (short) (max - (i + off) * m / w * range);
                }
            } else {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (short) ((i + off) * m / w * range);
                }            
            }
            return new ShortLUT(stored, srcoff, iMin1 + srcoff, data);         
        }
    }
    
    public abstract void lookup(DataBuffer src, DataBuffer dst);

}
