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
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;

import java.awt.image.ColorModel;
import java.util.WeakHashMap;

/**
 *
 * @author  gunter.zeilinger@tiani.com
 * @version 1.0.0
 */
public class ColorModelFactoryImpl extends ColorModelFactory {    
    private static final WeakHashMap cache = new WeakHashMap();

    /** Creates a new instance of ColorModelFactoryImpl */
    public ColorModelFactoryImpl() {
    }

    public ColorModel getColorModel(ColorModelParam param) {
        if (!param.isCacheable()) {
            return param.newColorModel();
        }
        ColorModel cm = (ColorModel)cache.get(param);
        if (cm == null) {
            cache.put(param, cm = param.newColorModel());
        }
        return cm;
    }

    public ColorModelParam makeParam(Dataset ds) throws DcmValueException {
        String pmi = ds.getString(Tags.PhotometricInterpretation, null);
        if (pmi == null) {
            throw new DcmValueException("Missing Photometric Interpretation");
        }
        if ("PALETTE COLOR".equals(pmi)) {
            return new PaletteColorParam(ds);
        }
        if ("MONOCHROME1".equals(pmi)) {
            return new MonochromeParam(ds, "INVERSE");
        }
        if ("MONOCHROME2".equals(pmi)) {
            return new MonochromeParam(ds, "IDENTITY");
        }
        throw new UnsupportedOperationException("Photometric Interpretation "
                + pmi + " not supported!");
    }
    
}
