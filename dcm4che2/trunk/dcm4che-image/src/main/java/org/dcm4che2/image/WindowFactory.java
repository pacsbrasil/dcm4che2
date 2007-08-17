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
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision$ $Date$
 * @since Aug 18, 2007
 */
public class WindowFactory {

    public static boolean containsVOIAttributes(DicomObject img) {
        return img.containsValue(Tag.WindowCenter)
                && img.containsValue(Tag.WindowWidth)
                || img.containsValue(Tag.VOILUTSequence);
    }

    public static float[] getMinMaxWindowCenterWidth(DicomObject img,
            DataBuffer db) {
        int[] minMax;
        float slope;
        float intercept;
        DicomObject mLut = LookupTable
                .isModalityLUTcontainsPixelIntensityRelationshipLUT(img)
                        ? null
                        : img.getNestedDicomObject(Tag.ModalityLUTSequence);
        if (mLut != null) {
            slope = 1;
            intercept = 0;
            minMax = WindowFactory.calcMinMax(mLut);
        } else {
            slope = img.getFloat(Tag.RescaleSlope, 1.f);
            intercept = img.getFloat(Tag.RescaleIntercept, 0.f);
            if (img.containsValue(Tag.SmallestImagePixelValue)
                    && img.containsValue(Tag.LargestImagePixelValue)) {
                minMax = new int[] { img.getInt(Tag.SmallestImagePixelValue),
                        img.getInt(Tag.LargestImagePixelValue) };
            } else {
                minMax = WindowFactory.calcMinMax(img, db);
            }
        }
        return new float[] {
                ((minMax[1] + minMax[0]) / 2.f) * slope + intercept,
                (minMax[1] - minMax[0]) * slope + 1 };
    }

    static int[] calcMinMax(DicomObject img, DataBuffer db) {
        int allocated = img.getInt(Tag.BitsAllocated, 8);
        int stored = img.getInt(Tag.BitsStored, allocated);
        boolean signed = img.getInt(Tag.PixelRepresentation) != 0;
        int range = 1 << stored;
        int mask = range - 1;
        int signbit = signed ? 1 << (stored - 1) : 0;
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            return WindowFactory.calcMinMax(signbit, mask, ((DataBufferByte) db).getData()); 
        case DataBuffer.TYPE_USHORT:
            return WindowFactory.calcMinMax(signbit, mask, ((DataBufferUShort) db).getData()); 
        case DataBuffer.TYPE_SHORT:
            return WindowFactory.calcMinMax(signbit, mask, ((DataBufferShort) db).getData());
        default:
            throw new IllegalArgumentException(
                    "Illegal Type of DataBuffer: " + db);
        }
    }

    static int[] calcMinMax(int signbit, int mask, short[] data) {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < data.length; i++) {
            int val = data[i] & mask;
            if ((val & signbit) != 0) {
                val |= ~mask;
            }
            if (minVal > val) {
                minVal = val;
            }
            if (maxVal < val) {
                maxVal = val;
            }
        }
        return new int[] { minVal, maxVal };
    }

    static int[] calcMinMax(int signbit, int mask, byte[] data) {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < data.length; i++) {
            int val = data[i] & mask;
            if ((val & signbit) != 0) {
                val |= ~mask;
            }
            if (minVal > val) {
                minVal = val;
            }
            if (maxVal < val) {
                maxVal = val;
            }
        }
        return new int[] { minVal, maxVal };
    }

    static int[] calcMinMax(DicomObject lut) {
        int[] desc = lut.getInts(Tag.LUTDescriptor);
        byte[] data = lut.getBytes(Tag.LUTData);
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
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        if (data.length == len) {
            for (int i = 0; i < len; i++) {
                int val = data[i] & 0xff;
                if (minVal > val) {
                    minVal = val;
                }
                if (maxVal < val) {
                    maxVal = val;
                }
            }
        } else if (data.length == len << 1) {
            int hibyte = lut.bigEndian() ? 0 : 1;
            int lobyte = 1 - hibyte;
            for (int i = 0, j = 0; i < len; i++, j++, j++) {
                int val = (data[j + hibyte] & 0xff) << 8
                        | (data[j + lobyte] & 0xff);
                if (minVal > val) {
                    minVal = val;
                }
                if (maxVal < val) {
                    maxVal = val;
                }
            }
        } else {
            throw new IllegalArgumentException("LUT Data length: "
                    + data.length + " mismatch entry value: " + len
                    + " in LUT Descriptor");
        }
        return new int[] { minVal, maxVal };
    }

}
