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
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.image.PixelDataFactory;
import org.dcm4che.image.PixelDataReader;
import org.dcm4che.image.PixelDataWriter;
import org.dcm4che.net.DataSource;

/**
 * @author <a href="mailto:gunter@tia

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
		try {
			DcmParser parser = parserFact.newDcmParser(in);
			parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDcmFile(null, Tags.PixelData);
			if (parser.getReadTag() != Tags.PixelData) {
				throw new IOException("No Pixel Data in file - " + file);
			}
			String pmi = ds.getString(Tags.PhotometricInterpretation);			
			if (printSCU.isColorPrint()) {
				if (!"RGB".equals(pmi) && !"MONOCHROME1".equals(pmi)) {
					throw new IOException("Conversion from " + pmi + " to RGB not yet supported");
				}
				if (ds.getInt(Tags.PlanarConfiguration, 0) != 0) {
					throw new IOException("Conversion from Planar Configuration color-by-pixel to color-by-plane not yet supported");
				}
			} else {
				if (!"MONOCHROME2".equals(pmi) && !"MONOCHROME1".equals(pmi)) {
					throw new IOException("Conversion from " + pmi + " to MONOCHROME not yet supported");
				}
				int bitsStored = ds.getInt(Tags.BitsStored, -1);
				if (bitsStored != 8 && bitsStored != 12) {
					throw new IOException("Conversion from " + bitsStored + " bits stored to 8 or 12 bits not yet supported");			
				}
				if (ds.getInt(Tags.PixelPresentation, 0) != 0) {
					throw new IOException("Conversion from unsigned to signed pixel values not yet supported");						
				}
			}
			DcmEncodeParam encodeParam = DcmDecodeParam.valueOf(tsUID);
			imageBox.writeDataset(out, encodeParam);
			ds.writeHeader(out, encodeParam,
				printSCU.isColorPrint() 
				? Tags.BasicColorImageSeq
				: Tags.BasicGrayscaleImageSeq,
				VRs.SQ, -1);
			ds.writeHeader(out, encodeParam,
				Tags.Item, VRs.NONE, -1);
			ds.subSet(IMAGE_MODULE).writeDataset(out, encodeParam);
			// TODO change this (length) if pixel data is rescaled to a different bit depth
			ds.writeHeader(out, encodeParam,
				parser.getReadTag(),
				parser.getReadVR(),
				parser.getReadLength());
                
            final int bitsAlloc = ds.getInt(Tags.BitsAllocated, 8);
            final int bitsStored = ds.getInt(Tags.BitsStored, 8);
            final int highBit = ds.getInt(Tags.HighBit, bitsStored - 1);
            final int cols = ds.getInt(Tags.Columns, 0);
            final int rows = ds.getInt(Tags.Rows, 0);
            final int spp = ds.getInt(Tags.SamplesPerPixel, 1);
            final boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
            final float rs = ds.getFloat(Tags.RescaleSlope, 1F);
			final float ri = ds.getFloat(Tags.RescaleIntercept, 0F);
			int[][] buff = null;
            ImageInputStream iis = null;
			PixelDataReader pd = null;
			
            //if a window exists, scale pixel data to window range
            if (ds.contains(Tags.WindowCenter) && ds.contains(Tags.WindowWidth)) {
            	final int winTop = (int)Math.ceil(((ds.getFloat(Tags.WindowCenter, 0F)
            		+ ds.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
				final int winBot = (int)Math.floor(((ds.getFloat(Tags.WindowCenter, 0F)
                    - ds.getFloat(Tags.WindowWidth, 0F) / 2) - ri) / rs);
				if (buff == null) {
					iis = ImageIO.createImageInputStream(in);
					pd = pdFact.newReader(ds,
						iis, parser.getDcmDecodeParam().byteOrder,
						parser.getReadVR());
					pd.readPixelData(false);
					buff = pd.getPixelDataArray(0);
				}
				scaleToRange(buff, signed, bitsStored, bitsStored, winBot, winTop);
            }
            
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
            
            //check if any overlays exist and if they are to be burned into the image
            if (burnInOverlays && overlays.length > 0) {
                final int burnValue = (signed) ? (1 << highBit) - 1 : -1;
                if (buff == null) {
	                iis = ImageIO.createImageInputStream(in);
	                pd = pdFact.newReader(ds,
	                	iis, parser.getDcmDecodeParam().byteOrder,
	                	parser.getReadVR());
					pd.readPixelData(false);
	                buff = pd.getPixelDataArray(0);
                }
                Overlay ovl;
                
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
            
            if (pd != null) {
                PixelDataWriter pdWriter = pdFact.newWriter(pd.getPixelDataArray(), false,
                    pd.getPixelDataDescription(), new DataSourceImageOutputStream(out));
				pdWriter.writePixelData(false);
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
    
    private void scaleToRange(int[][] pixelData, boolean signed, int fromBitDepth,
        int toBitDepth, int start, int end)
    {
        final int min = (signed) ? -(1 << (fromBitDepth - 1)) : 0;
        final int max = (signed) ? (1 << (fromBitDepth - 1)) - 1
                                 : (1 << fromBitDepth) - 1;
        if (start == end) {
            start = min;
            end = max;
        }
        final int rngOrig = end - start;
        final int newMin = (signed) ? -(1 << (toBitDepth - 1)) : 0;
        final int newMax = (signed) ? (1 << (toBitDepth - 1)) - 1
                                    : (1 << toBitDepth) - 1;
        final int rngNew = newMax - newMin;
        final float f = rngNew / rngOrig;
        float tmp;
        
        for (int s = 0; s < pixelData.length; s++)
            for (int i = 0; i < pixelData[s].length; i++) {
                tmp = (pixelData[s][i] - start) * f;
                if (tmp < 0)
                    pixelData[s][i] = newMin;
                else if (tmp > rngNew)
                    pixelData[s][i] = newMax;
                else
                    pixelData[s][i] = (int)(tmp + 0.5F) - newMin;
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
