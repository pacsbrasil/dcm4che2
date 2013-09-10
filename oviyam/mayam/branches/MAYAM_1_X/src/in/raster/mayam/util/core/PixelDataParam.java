package in.raster.mayam.util.core;
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
 * Gunter Zeilinger <gunterze@gmail.com>
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

import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 1.1 $
 * @since 29.10.2003
 */
class PixelDataParam {

    private static final int[] GRAY_BAND_OFFSETS = { 0};

    private static final int[] RGB_BAND_OFFSETS = { 0, 1, 2};

    private final String pmi;

    private final int samplesPerPixel;

    private final int columns;

    private final int rows;

    private final int bitsAllocated;

    private final int bitsStored;

    private final int pixelRepresentation;

    private final int planarConfiguration;

    private final int numberOfFrames;

    private final int frameLength;

    private final int dataType;

    public PixelDataParam(Dataset ds, boolean typeShortSupported) {
        pmi = ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2");
        samplesPerPixel = ds.getInt(Tags.SamplesPerPixel, 1);
        columns = ds.getInt(Tags.Columns, -1);
        rows = ds.getInt(Tags.Rows, -1);
        numberOfFrames = ds.getInt(Tags.NumberOfFrames, 1);
        pixelRepresentation = ds.getInt(Tags.PixelRepresentation, 0);
        planarConfiguration = ds.getInt(Tags.PlanarConfiguration, 0);

        switch (bitsAllocated = ds.getInt(Tags.BitsAllocated, 8)) {
        case 8:
            dataType = DataBuffer.TYPE_BYTE;
            frameLength = rows * columns * samplesPerPixel;
            break;
        case 16:
            dataType = pixelRepresentation != 0 && typeShortSupported ? DataBuffer.TYPE_SHORT
                    : DataBuffer.TYPE_USHORT;
            frameLength = rows * columns * samplesPerPixel * 2;
            break;
        default:
            throw new IllegalArgumentException("bits allocated:"
                    + bitsAllocated);
        }
        bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
    }

    public final int getBitsAllocated() {
        return bitsAllocated;
    }

    public final int getBitsStored() {
        return bitsStored;
    }

    public final int getColumns() {
        return columns;
    }

    public final int getPixelRepresentation() {
        return pixelRepresentation;
    }

    public final int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public final String getPhotoMetricInterpretation() {
        return pmi;
    }

    public final int getRows() {
        return rows;
    }

    public final int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public final int getNumberOfFrames() {
        return numberOfFrames;
    }

    public final int getFrameLength() {
        return frameLength;
    }

    public int getPixelDataLength() {
        return frameLength * numberOfFrames;
    }

    public final Dimension[] createImageDimensionArray() {
        Dimension[] a = new Dimension[numberOfFrames];
        Arrays.fill(a, new Dimension(columns, rows));
        return a;
    }

    private static int[] createOffsetArray(int numBands) {
        int[] bandOffsets = new int[numBands];
        for (int i = 0; i < numBands; i++) {
            bandOffsets[i] = i;
        }
        return bandOffsets;
    }

    private SampleModel getSampleModel() {
        if (planarConfiguration == 0) {
            return new PixelInterleavedSampleModel(dataType, columns, rows,
                    samplesPerPixel, columns * samplesPerPixel,
                    samplesPerPixel == 1 ? GRAY_BAND_OFFSETS : RGB_BAND_OFFSETS);
        } else {
            return new BandedSampleModel(dataType, columns, rows,
                    samplesPerPixel);
        }
    }

    private ColorModel getColorModel(int bits) {
        if (samplesPerPixel == 3) {
            return new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_sRGB), new int[] { bits, bits,
                    bits}, false, false, ColorModel.OPAQUE, dataType);
        } else {
            return new ComponentColorModel(ColorSpace
                    .getInstance(ColorSpace.CS_GRAY), new int[] { bitsStored},
                    false, false, ColorModel.OPAQUE, dataType);
        }
    }

    public WritableRaster createRaster() {
        return Raster.createWritableRaster(getSampleModel(), null);
    }

    public BufferedImage createBufferedImage(int maxBits) {
        return new BufferedImage(getColorModel(Math.min(maxBits, bitsStored)),
                createRaster(), false, null);
    }

    public String toString() {
        return "PixelData[pmi=" + pmi + ", samples=" + samplesPerPixel
                + ", rows=" + rows + ", columns=" + columns + ", alloc="
                + bitsAllocated + ", bits=" + bitsStored + ", signed="
                + pixelRepresentation + ", planar=" + planarConfiguration
                + ", frames=" + numberOfFrames;
    }

}
