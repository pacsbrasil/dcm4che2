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

package org.dcm4che.image;

import java.nio.ByteOrder;

import javax.imageio.stream.ImageInputStream;
import org.dcm4che.data.Dataset;
import org.dcm4cheri.image.PixelDataFactoryImpl;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @author <a href="mailto:joseph@tiani.com">joseph foraci</a>
 * @since July 2003
 * @version $Revision$ $Date$
 * @see "DICOM Part 5: Data Structures and Encoding, Section 8. 'Encoding of Pixel,
 *      Overlay and Waveform Data', Annex D"
 */
public abstract class PixelDataFactory
{
    public PixelDataFactory()
    {
    }

    public static PixelDataFactory newInstance()
    {
        return new PixelDataFactoryImpl();
    }

    /**
     * Creates a new <code>PixelData</code> instance, initialized by the
     * <code>Dataset</code> and backed by the <code>ImageInputStream</code>.
     * Any changes to the <code>ImageInputStream</code> will be seen by the
     * <code>PixelData</code> instance and will have undefined effects upon
     * the next read.
     */
    public abstract PixelData newPixelData(Dataset dataset, ImageInputStream iis, ByteOrder byteOrder, int pixelDataVr);
}
