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
 * Bill Wallace, Agfa HealthCare Inc., 
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Bill Wallace <bill.wallace@agfa.com>
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

package org.dcm4chee.xero.wado;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.image.LookupTable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author lpeters
 *
 */
public class ReduceBitsFilterTest {
	
	int maxAllowed = 4095;
	MinMaxResults minMaxResults;
	private BufferedImage biBytesInUShort;
	private BufferedImage biUShortsInUShort;
	private int width;
	private int height;
	private final short [] dataBytesInUShorts = {1, 5, 13, 77, 114, 253 };
	private final short [] dataUShortsInUShorts = {101, 507, 1300, 7700, 11400, 25457 };
	

	@BeforeTest
	public void initBufferedImages() {
		width = 3;
		height = 5;
		ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[] {16}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		biBytesInUShort = new BufferedImage(cm, raster, false, null);
		short [] data = ((DataBufferUShort)(raster.getDataBuffer())).getData();
		for ( int i = 0; i < 6; i++ ) {
			data[i] = dataBytesInUShorts[i];
		}
		
		raster = cm.createCompatibleWritableRaster(width, height);
		biUShortsInUShort = new BufferedImage(cm, raster, false, null);
		data = ((DataBufferUShort)(raster.getDataBuffer())).getData();
		for ( int i = 0; i < 6; i++ ) {
			data[i] = dataUShortsInUShorts[i];
		}
		
	}
	

	@SuppressWarnings("unchecked")
	protected void checkSmallestLargest(WadoImage wi, int smallestPixelValue, int largestPixelValue, int bitsAfterResampling ) {
		String filename = wi.getFilename();
		assertTrue(filename.contains("-pixelRange"+smallestPixelValue+","+largestPixelValue+","+bitsAfterResampling));	
		assertEquals(smallestPixelValue, ReduceBitsFilter.getPreviousSmallestPixelValue(wi));
		assertEquals(largestPixelValue, ReduceBitsFilter.getPreviousLargestPixelValue(wi));
		assertEquals(bitsAfterResampling, ReduceBitsFilter.getPreviousReducedBits(wi));
		
		Collection<String> headers = (Collection<String>) wi.getParameter(ReduceBitsFilter.RESPONSE_HEADERS);
		assertTrue(headers.contains(ReduceBitsFilter.SMALLEST_IMAGE_PIXEL_VALUE));
		assertTrue(headers.contains(ReduceBitsFilter.LARGEST_IMAGE_PIXEL_VALUE));
		assertTrue(headers.contains(ReduceBitsFilter.REDUCED_BITS));
	}
		
	@BeforeMethod
	public void initializeMinMaxResults() {
		minMaxResults = new MinMaxResults();
		minMaxResults.min = -3;
		minMaxResults.max = 3000;
		minMaxResults.setPixelPadding(-1000,-100);
	}
	
	@Test
	public void addSmallestLargestToWadoImageTest() throws IOException, ParserConfigurationException {
		WadoImage wi = new WadoImage(null, 0, null);
		
		ReduceBitsFilter.addSmallestLargestToWadoImage(wi, -2000, 789932, 11 );
		checkSmallestLargest(wi,-2000,789932,11);
		
		ReduceBitsFilter.addSmallestLargestToWadoImage(wi, -4000, 2111, 10 );
		checkSmallestLargest(wi,-4000,2111,10);
		
		String filename = wi.getFilename();
		assertFalse(filename.contains("-pixelRange-2000,79889,11"));			
	}

	@Test
	public void testCalculateNeedsProcessing_givenDefaultMinMaxResults_returnNeedsNoProcessing() {
		minMaxResults = new MinMaxResults();
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_NO_PROCESSING );
	}	
	
	@Test
	public void testCalculateNeedsProcessing_givenBitsNeedMasking_returnNeedsMasking() {
		minMaxResults = new MinMaxResults();
		minMaxResults.bitsNeedMasking = true;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_MASKING_FLAG );
	}	
	
	@Test
	public void testCalculateNeedsProcessing_givenDataAndPaddingWithin12BitsButGoesNegative_returnNeedsDataRangeShift() {
		minMaxResults.pixelPaddingFound = true;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG );
	}	
	
	
	@Test
	public void testCalculateNeedsProcessing_givenDataWithin12BitsButGoesNegative_WithPaddingThatIncreasesRangeBeyond12Bits__returnNeedsPixelPaddingRemoved_And_DataRangeShift() {
		minMaxResults.setPixelPadding(-3000,-100);
		minMaxResults.pixelPaddingFound = true;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_PIXEL_PADDING_REMOVED_FLAG | ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG );
	}
	
	@Test
	public void testCalculateNeedsProcessing_givenDataWithin12Bits_WithPaddingThatIncreasesRangeBeyond12Bits__returnNeedsPixelPaddingRemoved() {
		minMaxResults.setPixelPadding(-3000,-100);
		minMaxResults.pixelPaddingFound = true;
		minMaxResults.min = 3;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_PIXEL_PADDING_REMOVED_FLAG  );
	}
	
	@Test
	public void testCalculateNeedsProcessing_givenDataGreaterThan12Bits_WithNegativePadding__returnNeedsPixelPaddingRemoved_AndBiReduction_AndDataRangeShift() {
		minMaxResults.setPixelPadding(-3000,-100);
		minMaxResults.pixelPaddingFound = true;
		minMaxResults.min = 3;
		minMaxResults.max = 9000;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG | ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG | ReduceBitsFilter.NEEDS_PIXEL_PADDING_REMOVED_FLAG );
	}
	
	@Test
	public void testCalculateNeedsProcessing_givenDataGreaterThan12BitsButStartsAtZero_WithNegativePadding__returnNeedsPixelPaddingRemoved_AndBiReduction() {
		minMaxResults.setPixelPadding(-3000,-100);
		minMaxResults.pixelPaddingFound = true;
		minMaxResults.min = 0;
		minMaxResults.max = 9000;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG | ReduceBitsFilter.NEEDS_PIXEL_PADDING_REMOVED_FLAG );
	}
	
	@Test
	public void testCalculateNeedsProcessing_givenDataGreaterThan12BitsButStartsAtZero__returnNeedsPixelPaddingRemoved_AndBiReduction_AndDataRangeShift() {
		minMaxResults.setPixelPadding(-3000,-100);
		minMaxResults.pixelPaddingFound = false;
		minMaxResults.min = 0;
		minMaxResults.max = 9000;
		int flags = ReduceBitsFilter.calculateNeedsProcessing(maxAllowed, minMaxResults);
		assertEquals( flags, ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG );
	}

	@Test
	public void testCastDownToByteBufferedImage() {
		initBufferedImages();

		BufferedImage resultBi = ReduceBitsFilter.castDownToByteBufferedImage(8, biBytesInUShort);
		byte [] byteData = ((DataBufferByte)(resultBi.getRaster().getDataBuffer())).getData();
		for ( int i = 0; i < 6; i++) {
			assertEquals( byteData[i]&0xff, dataBytesInUShorts[i]&0xffff );
		}
	}

	@Test
	public void testCreateLookupTable() {
		int outBits = 12;
		int previousBitsIfNotZero = 0;
		DicomObject ds = new BasicDicomObject();
		ds.putInt(Tag.BitsStored, VR.US, 16);
		int[] smallestLargestOutput = new int[2];
		
		//
		minMaxResults.min = 0;
		minMaxResults.max = 25457;
		int needsProcessingFlags = ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG;
		
		LookupTable lookup = ReduceBitsFilter.createLookupTable(minMaxResults, previousBitsIfNotZero, outBits, ds, needsProcessingFlags, smallestLargestOutput );
		BufferedImage bi = ReduceBitsFilter.applyLUT(outBits, lookup, biUShortsInUShort.getRaster());
		
		short[] out = ((DataBufferUShort)(bi.getRaster().getDataBuffer())).getData();
		int i;
		for ( i = 0; i < 6; i++ ) {
			
			int expected = ( dataUShortsInUShorts[i] * 4095 ) / 25457;
			assertEquals(out[i], expected);
		}
		
		//
		minMaxResults.min = 101;
		minMaxResults.max = 25457;
		needsProcessingFlags = ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG | ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG;
		
		lookup = ReduceBitsFilter.createLookupTable(minMaxResults, previousBitsIfNotZero, outBits, ds, needsProcessingFlags, smallestLargestOutput );
		bi = ReduceBitsFilter.applyLUT(outBits, lookup, biUShortsInUShort.getRaster());
		
		out = ((DataBufferUShort)(bi.getRaster().getDataBuffer())).getData();
		for ( i = 0; i < 6; i++ ) {
			
			int expected = ( (dataUShortsInUShorts[i] - 101) * 4095 ) / (25457-101);
			assertEquals(out[i], expected);
		}
		
		//
		outBits = 15;		
		minMaxResults.min = 101;
		minMaxResults.max = 25457;
		needsProcessingFlags = ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG;
		
		lookup = ReduceBitsFilter.createLookupTable(minMaxResults, previousBitsIfNotZero, outBits, ds, needsProcessingFlags, smallestLargestOutput );
		bi = ReduceBitsFilter.applyLUT(outBits, lookup, biUShortsInUShort.getRaster());
		
		out = ((DataBufferUShort)(bi.getRaster().getDataBuffer())).getData();
		for ( i = 0; i < 6; i++ ) {
			
			int expected = (dataUShortsInUShorts[i] - 101);
			assertEquals(out[i], expected);
		}
		
		//
		outBits = 8;		
		minMaxResults.min = 101;
		minMaxResults.max = 25457;
		needsProcessingFlags = ReduceBitsFilter.NEEDS_BIT_REDUCTION_FLAG | ReduceBitsFilter.NEEDS_DATA_RANGE_SHIFT_FLAG;
		
		lookup = ReduceBitsFilter.createLookupTable(minMaxResults, previousBitsIfNotZero, outBits, ds, needsProcessingFlags, smallestLargestOutput );
		bi = ReduceBitsFilter.applyLUT(outBits, lookup, biUShortsInUShort.getRaster());
		
		byte [] outByte = ((DataBufferByte)(bi.getRaster().getDataBuffer())).getData();
		for ( i = 0; i < 6; i++ ) {
			
			int expected = ( (dataUShortsInUShorts[i] - 101) * 255 ) / (25457-101);
			assertEquals(outByte[i]&0xff, expected);
		}
		
		
	}
}
