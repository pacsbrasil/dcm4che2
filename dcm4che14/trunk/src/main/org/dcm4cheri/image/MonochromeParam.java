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

package org.dcm4cheri.image;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Arrays;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
final class MonochromeParam extends BasicColorModelParam  {
    
    private final int inverse;
    private final float slope, intercept;
    private final float[] center, width;
    private final int hashcode;

    private final static float[] EMPTY = {};
    private final static float[] nullToEmpty(float[] a) {
        return a == null ? EMPTY : a;
    }
    private final static float correctZeroSlope(float f) {
        return f == 0.f ? 1.f : f;
    }

    /** Creates a new instance of MonochromeParam */
    public MonochromeParam(Dataset ds, String shape) throws DcmValueException {
        super(ds);
        this.inverse = "INVERSE".equals(
                ds.getString(Tags.PresentationLUTShape, shape))
                ? -1 : 0;
        this.slope = correctZeroSlope(ds.getFloat(Tags.RescaleSlope, 1.f));
        this.intercept = ds.getFloat(Tags.RescaleIntercept, 0.f);
        this.center = nullToEmpty(ds.getFloats(Tags.WindowCenter));
        this.width = nullToEmpty(ds.getFloats(Tags.WindowWidth));
        for (int i = 0; i < width.length; ++i) {
            if (width[i] <= 0.f) {
                width[i] = (max - min) / slope;
            }
        }
        this.hashcode = hashcode(dataType, inverse, min, max,
                slope, intercept, center, width);
    }
    
    private static int hashcode(int dataType, int inverse, int min, int max,
            float slope, float intercept, float[] center, float[] width) {
        StringBuffer sb = new StringBuffer();
        sb.append(dataType).append(inverse).append(min).append(max)
                .append(slope).append(intercept);
        if (Math.min(center.length, width.length) != 0) {
            sb.append(center[0]).append(width[0]);
        }
        return sb.toString().hashCode();
    }
    
    private MonochromeParam(MonochromeParam other, float center1, float width1,
            boolean inverse1) {
        super(other);
        this.inverse = inverse1 ? -1 : 0;
        this.slope = other.slope;
        this.intercept = other.intercept;
        this.center = new float[] { center1 };
        this.width = new float[] { width1 };
        this.hashcode = hashcode(dataType, inverse, min, max,
                slope, intercept, center, width);
    }
    
    public ColorModelParam update(float center, float width, boolean inverse) {
        if (width < 0) {
            throw new IllegalArgumentException("width: " + width);
        }
        return new MonochromeParam(this, center, width, inverse);
    }
    
    public int hashCode() {
        return hashcode;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }        
        if (!(o instanceof MonochromeParam)) {
            return false;
        }        
        MonochromeParam other = (MonochromeParam)o;
        if (this.getNumberOfWindows() == 0) {
            if (other.getNumberOfWindows() != 0) {
                return false;
            }
        } else {
            if (other.getNumberOfWindows() == 0
                    || this.center[0] != other.center[0] 
                    || this.width[0] != other.width[0]) {
                return false;
            }
        }
        return this.inverse == other.inverse
                && this.intercept == other.intercept
                && this.slope == other.slope
                && this.max == other.max
                && this.min == other.min;
    }
    
    public final float getWindowCenter(int index) {
        return center[index];
    }
    
    public final float getWindowWidth(int index) {
        return width[index];
    }
    
    public final int getNumberOfWindows() {
        return Math.min(center.length, width.length);
    }
    
    public final boolean isCacheable() {
        return true;
    }
    
    public final boolean isInverse() {
        return inverse != 0;
    }
    
    public final float toMeasureValue(int pxValue) {
        return mask(pxValue)*slope + intercept;
    }    
    
    private static int toARGB(int grey) {
        return 0xff000000 // alpha
            | (grey << 16)
            | (grey << 8)
            | (grey);
    }
    
    public ColorModel newColorModel() {
        int[] cmap = new int[size];
        if (getNumberOfWindows() == 0) {
            if (min == 0) {
                for (int i = 0; i < size; ++i) {
                    cmap[i] = toARGB(((i>>bits_8) ^ inverse) & 0xff);
                }
            } else {
                for (int i = 0, j = size>>1; j < size; ++j,++i) {
                    cmap[i] = toARGB(((j>>bits_8) ^ inverse) & 0xff);
                    cmap[j] = toARGB(((i>>bits_8) ^ inverse) & 0xff);
                }
            }
        } else {
            createCMAP(cmap, (int)((center[0] - intercept)/slope),
                    (int)(width[0]/slope));
        }
        return new IndexColorModel(bits, size, cmap, 0, false, -1, dataType);
    }
    
    private void createCMAP(int[] cmap, int c, int w) {
        int u = c - (w>>1);
        int o = u + w;
        if (u > 0 && inverse != 0) {
            Arrays.fill(cmap, 0, Math.min(u,max), 0xffffffff);
        }
        if (o < max && inverse == 0) {
            Arrays.fill(cmap, Math.max(0,o), max, 0xffffffff);
        }
        for (int i = Math.max(0,u), n = Math.min(o,max); i < n; ++i) {
            cmap[i] = toARGB((((i-u)<<8) / w ^ inverse) & 0xff);
        }
        if (min == 0) {
            return; // all done for unsigned px val
        }
        if (u > min && inverse != 0) {
            Arrays.fill(cmap, size>>1, Math.min(u+size, size), 0xffffffff);
        }
        if (o < 0 && inverse == 0) {
            Arrays.fill(cmap, Math.max(o+size,size>>1), size, 0xffffffff);
        }
        for (int i = Math.max(min,u), n = Math.min(o,0); i < n; ++i) {
            cmap[i+size] = toARGB((((i-u)<<8) / w ^ inverse) & 0xff);
        }
    }
}
