/*
 *  Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 *  This file is part of dcm4che.
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published
 *  by the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4che.client;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataDescription;
import org.dcm4che.image.PixelDataFactory;
import org.dcm4che.image.PixelDataReader;
import org.dcm4che.image.PixelDataWriter;
import org.dcm4che.net.DataSource;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since Jun 22, 2003
 * @version $Revision$ $Date$
 *
 */
class PrintSCUDataSource implements DataSource {

	private static final DcmParserFactory parserFact =
		DcmParserFactory.getInstance();
	private final PrintSCU printSCU;
	private final Dataset imageBox;
	private final File file;
    private final boolean burnInOverlays;
	
	/**
	 * @param printSCU the calling <code>PrintSCU</code>
	 * @param imageBox the <code>Dataset</code> containing the Image Box header
     *        (minus any image pixel module-specific attributes)
	 * @param file the path to the local file containing the image to be sent
     * @param burnInOverlays specifies whether to "burn-in" and Overlay Planes
     *        found within the DICOM image
	 */
	public PrintSCUDataSource(PrintSCU printSCU, Dataset imageBox, File file,
                              boolean burnInOverlays) {
		this.printSCU = printSCU;
		this.imageBox = imageBox;
		this.file = file;
        this.burnInOverlays = burnInOverlays;
	}

    private static final PixelDataFactory pdFact = PixelDataFactory.getInstance();
	private static final Dataset IMAGE_MODULE =
		PrintSCU.dcmFact.newDataset();
	static {
		IMAGE_MODULE.putUS(Tags.SamplesPerPixel);
		IMAGE_MODULE.putCS(Tags.PhotometricInterpretation);
		IMAGE_MODULE.putUS(Tags.PlanarConfiguration);
		IMAGE_MODULE.putUS(Tags.Rows);
		IMAGE_MODULE.putUS(Tags.Columns);
		IMAGE_MODULE.putIS(Tags.PixelAspectRatio);
		IMAGE_MODULE.putUS(Tags.BitsAllocated);
		IMAGE_MODULE.putUS(Tags.BitsStored);
		IMAGE_MODULE.putUS(Tags.HighBit);
		IMAGE_MODULE.putUS(Tags.PixelRepresentation);
	}

	/**
	 * @see org.dcm4che.net.DataSource#writeTo(java.io.OutputStream, java.lang.String)
	 */
	public void writeTo(OutputStream out, String tsUID) throws IOException {
		InputStream in = new BufferedInputStream(
			new FileInputStream(file));
		final Dataset ds = PrintSCU.dcmFact.newDataset();
        final int toBitsStored, toBitsAllocated;
        
		try {
			DcmParser parser = parserFact.newDcmParser(in);
			parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDcmFile(null, Tags.PixelData);
            //err check for PixelData
			if (parser.getReadTag() != Tags.PixelData) {
				throw new IOException("No Pixel Data in file - " + file);
			}
            DcmDecodeParam decodeParam = parser.getDcmDecodeParam();
            DcmEncodeParam encodeParam = DcmDecodeParam.valueOf(tsUID);
            PixelDataDescription toDesc = null;
            PixelDataDescription fromDesc = new PixelDataDescription(ds,
                decodeParam.byteOrder, parser.getReadVR());
            if (fromDesc.getNumberOfFrames() > 1)
                throw new IOException("multi-frame images are not currently supported");
            //grab some common pixel module attributes
            final int bitsAlloc = ds.getInt(Tags.BitsAllocated, 8);
            final int bitsStored = ds.getInt(Tags.BitsStored, 8);
            final int highBit = ds.getInt(Tags.HighBit, bitsStored - 1);
            final int cols = ds.getInt(Tags.Columns, 0);
            final int rows = ds.getInt(Tags.Rows, 0);
            final int spp = ds.getInt(Tags.SamplesPerPixel, 1);
            final boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
            final float rs = ds.getFloat(Tags.RescaleSlope, 1F);
            final float ri = ds.getFloat(Tags.RescaleIntercept, 0F);
            //err check on bit depth
            if (bitsStored == 32 && !signed) {
                throw new IOException("conversion from " + bitsStored
                    + " bits stored, unsigned is not currently supported");
            }
            //err check PMI
            String pmi = ds.getString(Tags.PhotometricInterpretation);          
			if (printSCU.isColorPrint()) {
				if (!"RGB".equals(pmi) && !"MONOCHROME1".equals(pmi)) {
					throw new IOException("Conversion from " + pmi + " to RGB not currently supported");
				}
			}
            else {
				if (!"MONOCHROME2".equals(pmi) && !"MONOCHROME1".equals(pmi)) {
					throw new IOException("Conversion from " + pmi + " to MONOCHROME not currently supported");
				}
			}
            //
            if (bitsStored > 8) {
                toBitsStored = 12;
                toBitsAllocated = 16;
            }
            else {
                toBitsStored = 8;
                toBitsAllocated = 8;
            }
            if (bitsStored != toBitsStored || highBit != bitsStored - 1 || signed
                || !fromDesc.isByPlane()) {
                toDesc = new PixelDataDescription(fromDesc, encodeParam,
                    toBitsAllocated, toBitsStored, false, true);
                ds.putUS(Tags.BitsStored, toBitsStored);
                ds.putUS(Tags.BitsAllocated, toBitsAllocated);
                ds.putUS(Tags.HighBit, toBitsStored - 1);
                ds.putUS(Tags.PixelRepresentation, 0);
                ds.putUS(Tags.PlanarConfiguration, 1);
            }
            else
                toDesc = fromDesc;
            //write image box
			imageBox.writeDataset(out, encodeParam);
			ds.writeHeader(out, encodeParam,
				printSCU.isColorPrint() ? Tags.BasicColorImageSeq
				                        : Tags.BasicGrayscaleImageSeq,
				VRs.SQ, -1);
			ds.writeHeader(out, encodeParam,
				Tags.Item, VRs.NONE, -1);
            //write image box pixel representation attributes (only header for PixelData)
			ds.subSet(IMAGE_MODULE).writeDataset(out, encodeParam);
			ds.writeHeader(out, encodeParam,
				parser.getReadTag(),
				toDesc.getPixelDataVr(),
				(int)toDesc.calcPixelDataLength());
                
			int[][] buff = null;
			PixelDataReader pd = null;
			
            //overlays
            final class Overlay
            {
                public int cols, rows;
                public int x, y;
                public byte[] data;
                public Overlay (int group)
                {
                    final int OvRows = 0x10, OvCols = 0x11;
                    final int OvOrigin = 0x50, OvData = 0x3000;
                    data = ds.getByteBuffer(group | OvData).array();
                    if (data == null)
                        throw new IllegalArgumentException("no overlay data");
                    int[] origin = ds.getInts(group | OvOrigin);
                    x = origin[0] - 1;
                    y = origin[1] - 1;
                    cols = ds.getInt(group | OvCols, 0);
                    rows = ds.getInt(group | OvRows, 0);
                    if (cols == 0 || rows == 0)
                        throw new IllegalArgumentException("bad/no cols/rows in overlay");
                }
            };
            
            //check if any overlays exist and if they are to be burned into the image
            if (burnInOverlays) {
                DcmElement el;
                int group = 0x60000000, cntr = 16;
                List list = new Vector(16);
                while (cntr > 0) {
                    if ((el = ds.get(group | 0x3000)) != null)
                        list.add(new Overlay(group));
                    cntr--;
                    group += 0x20000;
                }
                Overlay[] overlays = (Overlay[])list.toArray(new Overlay[0]);
                list = null;
                final int burnValue = (signed) ? (1 << highBit) - 1 : -1;
                
                if (overlays.length > 0) {
                    if (buff == null)
                        buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                    
                    Overlay ovl;
                    
                    //iterate over each overlay
                    for (int i = 0; i < overlays.length; i++) {
                        ovl = overlays[i];
                        final int colstart = Math.max(0, ovl.x);
                        final int colend = Math.min(ovl.x + ovl.cols, cols);
                        final int rowstart = Math.max(0, ovl.y);
                        final int rowend = Math.min(ovl.y + ovl.rows, rows);
                        final int x = (ovl.x >= 0) ? 0 : -ovl.x;
                        final int y = (ovl.y >= 0) ? 0 : -ovl.y;
                        int mask = 1;
                        int ind = x + y * ovl.cols;
                        for (int j = rowstart; j < rowend; j++) {
                            for (int k = colstart; k < colend; k++) {
                                if ((ovl.data[ind >> 3] & mask) > 0) {
                                    for (int s = 0; s < spp; s++)
                                    	buff[s][(j * cols) + k] = burnValue;
                                }
                                ind++;
                                if (mask == 0x80)
                                    mask = 1;
                                else
                                    mask = mask << 1;
                            }
                            ind += ovl.cols;
                        }
                    }
                }
                
                //iterate over pixel data for overlay data in pixel data itself
                if (bitsAlloc > bitsStored) {
                    if (buff == null)
                        buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                    
                    final int mask = ~(((1 << bitsStored) - 1) << (highBit - bitsStored - 1));
                    for (int s = 0; s < spp; s++)
                        for (int i = 0; i < buff[s].length; i++)
                            if ((buff[s][i] & mask) != 0)
                                buff[s][i] = burnValue;
                }
            }
            
            //if a window exists, scale pixel data to window range
            int winTop = 0, winBot = 0;
            final boolean windowPresent = ds.contains(Tags.WindowCenter) && ds.contains(Tags.WindowWidth);
            if (windowPresent) {
                winTop = (int)Math.ceil(((ds.getFloat(Tags.WindowCenter, 0F)
                    + ds.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
                winBot = (int)Math.floor(((ds.getFloat(Tags.WindowCenter, 0F)
                    - ds.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
            }
            
            //scale
            if (toDesc != fromDesc || windowPresent) {
                if (buff == null)
                    buff = (pd = readPixelData(fromDesc, in)).getPixelDataArray(0);
                scaleToRange(buff, fromDesc, bitsStored, false, winBot, winTop);
            }
            
            if (pd != null) {
                //for the writing we're going on the assumption that the print server
                // will ignore the overlay bits in pixel cell (in the case of bs=12)
                int[][][] tmp = new int[1][0][0];
                tmp[0] = buff;
                PixelDataWriter pdWriter = pdFact.newWriter(tmp, false, toDesc,
                    new DataSourceImageOutputStream(out));
				pdWriter.writePixelData();
            }
            else {
    			copy(in, out, parser.getReadLength());
            }
            
			ds.writeHeader(out, encodeParam,
				Tags.ItemDelimitationItem, VRs.NONE, 0);
			ds.writeHeader(out, encodeParam,
				Tags.SeqDelimitationItem, VRs.NONE, 0);
		}
        finally {
			try {
				in.close();
			}
            catch (IOException ignore) {}
		}
	}

    private PixelDataReader readPixelData(PixelDataDescription desc,
        InputStream in)
        throws IOException {
        PixelDataReader reader = pdFact.newReader(desc, ImageIO.createImageInputStream(in));
        reader.readPixelData(true);
        return reader;
    }

    /**
     * Scales the range [<code>start..end</code>] in pixelData described by
     * <code>from</code> to the specified bit depth and sign representation.
     * The pixel data is assumed to contain the <i>entire cell</i>, and
     * loses any overlay bits that may be present.
     */
    private void scaleToRange(int[][] pixelData, PixelDataDescription from,
        int toBitDepth, boolean toSigned, int start, int end) {
        final int fromBitDepth = from.getBitsStored();
        final int min = (from.isSigned()) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (from.isSigned()) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start >= end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (toSigned) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (toSigned) ? (1 << (toBitDepth - 1)) - 1
                                      : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final float f = rngNew / rngOrig;
        final int leftShift = 32 - from.getHighBit() - 1;
        final int rightShift = 32 - fromBitDepth;
        float tmp;

        if (from.isSigned()) {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >> rightShift) - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
        else {
            for (int s = 0; s < pixelData.length; s++)
                for (int i = 0; i < pixelData[s].length; i++) {
                    tmp = (((pixelData[s][i] << leftShift) >>> rightShift) - start) * f;
                    if (tmp < 0)
                        pixelData[s][i] = newMin;
                    else if (tmp > rngNew)
                        pixelData[s][i] = newMax;
                    else
                        pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
                }
        }
    }
    
	private void copy(InputStream in, OutputStream out, int len) throws IOException {
		byte tmp;
		int c, remain = len;
		byte[] buffer = printSCU.getBuffer();
		while (remain > 0) {
			c = in.read(buffer, 0, Math.min(buffer.length, remain));
			if (c == -1) {
				throw new EOFException("EOF during read of pixel data");
			}
			out.write(buffer, 0, c);
			remain -= c;
		}
	}

}
