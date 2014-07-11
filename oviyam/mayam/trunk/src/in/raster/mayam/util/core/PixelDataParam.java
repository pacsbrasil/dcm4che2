package in.raster.mayam.util.core;
import java.awt.Dimension;
import java.awt.color.ColorSpace;
import java.awt.image.*;
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