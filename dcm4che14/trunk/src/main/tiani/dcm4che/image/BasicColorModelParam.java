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

package tiani.dcm4che.image;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmValueException;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.DataBuffer;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
abstract class BasicColorModelParam implements ColorModelParam {   
    protected final int size;
    protected final int bits;
    protected final int bits_8;
    protected final int min;
    protected final int max;
    protected final int shiftmask;

    /** Creates a new instance of PaletteColorParam */
    protected BasicColorModelParam(Dataset ds) throws DcmValueException {
        this.bits = ds.getInt(Tags.BitsStored, 8);
        if (bits < 8 || bits > 16) {
            throw new UnsupportedOperationException("Bits Stored: " + bits
                    + " not supported!");
        }
        this.bits_8 = bits - 8;
        int hBit = ds.getInt(Tags.HighBit, bits-1);
        if (hBit != bits-1) {
            throw new UnsupportedOperationException("High Bit: " + hBit
                    + " not supported!");
        }
        this.size = 1 << bits;
        if (ds.getInt(Tags.PixelRepresentation, 0) == 0) {
            this.min = 0;
            this.max = size;
        } else {
            this.min = -(size>>1);
            this.max = -min;
        }
        this.shiftmask = 32-bits;
    }

    protected BasicColorModelParam(BasicColorModelParam other) {
        this.size = other.size;
        this.bits = other.bits;
        this.bits_8 = other.bits_8;
        this.min = other.min;
        this.max = other.max;
        this.shiftmask = other.shiftmask;
    }
        
    protected final int mask(int pxValue) {
        return min == 0 ? ((pxValue<<shiftmask)>>>shiftmask)
                        : ((pxValue<<shiftmask)>>shiftmask);
    }
}
