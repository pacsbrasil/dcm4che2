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

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;
import org.dcm4che.net.DataSource;

/**
 * @author <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since Jun 22, 2003
 * @version $Revision$ $Date$
 *
 */
class PrintSCUDataSource implements DataSource {

	static final DcmParserFactory parserFact =
		DcmParserFactory.getInstance();
	private final  PrintSCU printSCU;
	private final  Dataset imageBox;
	private final  File file;
	
	/**
	 * @param printSCU
	 * @param imageBox
	 * @param file
	 */
	public PrintSCUDataSource(PrintSCU printSCU, Dataset imageBox, File file) {
		this.printSCU = printSCU;
		this.imageBox = imageBox;
		this.file = file;
	}

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

	/* (non-Javadoc)
	 * @see org.dcm4che.net.DataSource#writeTo(java.io.OutputStream, java.lang.String)
	 */
	public void writeTo(OutputStream out, String tsUID) throws IOException {
		InputStream in = new BufferedInputStream(
			new FileInputStream(file));
		Dataset ds = PrintSCU.dcmFact.newDataset();
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
					throw new IOException("Conversion from " + pmi + " to MONCHROME not yet supported");
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
			ds.writeHeader(out, encodeParam,
				parser.getReadTag(),
				parser.getReadVR(),
				parser.getReadLength());
			copy(in, out, parser.getReadLength());
			ds.writeHeader(out, encodeParam,
				Tags.ItemDelimitationItem, VRs.NONE, 0);
			ds.writeHeader(out, encodeParam,
				Tags.SeqDelimitationItem, VRs.NONE, 0);
		} finally {
			try {
				in.close();
			} catch (IOException ignore) {
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
