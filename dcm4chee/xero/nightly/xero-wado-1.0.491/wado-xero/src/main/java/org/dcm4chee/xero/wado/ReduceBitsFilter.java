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
 * Portions created by the Initial Developer are Copyright (C) 2008
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

import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.ByteLookupTable;
import org.dcm4che2.image.LookupTable;
import org.dcm4che2.image.ShortLookupTable;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter reduces the number of stored bits, down to 12 bits so that the
 * image can be encoded using 12 bit JPEG. Also adds response headers to
 * indicate the transformation.
 * TODO Add Modality LUT transformation - send the image complete with the Modality LUT
 * having been applied?  Actually, if we did this, it would have to be flagged so that
 * future W/L filters do not apply the modality LUT.
 * 
 * @author bwallace
 */
public class ReduceBitsFilter implements Filter<WadoImage> {
   public static final String RESPONSE_HEADERS = "responseHeaders";

   public static final String LARGEST_IMAGE_PIXEL_VALUE = "LargestImagePixelValue";

   public static final String SMALLEST_IMAGE_PIXEL_VALUE = "SmallestImagePixelValue";
   public static final String REDUCED_BITS = "ReducedBits";

   private static final Logger log = LoggerFactory.getLogger(ReduceBitsFilter.class);

   static ColorSpace gray = ColorSpace.getInstance(ColorSpace.CS_GRAY);

   static int[] bits12 = new int[] { 12 };
   static ColorModel cm12 = new ComponentColorModel(gray, bits12, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);

   protected static final int NEEDS_NO_PROCESSING = 0;
   protected static final int NEEDS_MASKING_FLAG = 0x01;
   protected static final int NEEDS_BIT_REDUCTION_FLAG = 0x02;
   protected static final int NEEDS_DATA_RANGE_SHIFT_FLAG = 0x04;
   protected static final int NEEDS_PIXEL_PADDING_REMOVED_FLAG = 0x08;
   
   /**
    * Reduces the number of bits from 13-16 to 12, only for contentType=image/jp12
    */
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  int bits = FilterUtil.getInt(params,EncodeImage.MAX_BITS);
	  if( bits==0 || bits>15 ) {
		  log.debug("Not applying reduce bits - either default of 16 bits is allowed: {}", bits);
		  return filterItem.callNextFilter(params);
	  }

	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if( wi==null || wi.hasError()) return wi;
	  if ( wi.getValue() == null ) {
		  if ( wi.getParameter(WadoImage.IMG_AS_BYTES) != null ) {
			  log.debug("Raw bytes returned.  We can assume no rescale is needed.");
		  } else {
			  log.info("Neither raw bytes, nor a BufferedImage is returned.");
		  }
		  return wi;
	  }
	  
	  DicomObject ds = wi.getDicomObject();
	  if( !mayRequireRescale(wi,bits,ds) )  {
		  log.debug("Image is 8-bit or color, doesn't need to be rescaled, and can be returned as a regular image.");
		  return wi;
	  }
	  log.debug("Rescaling image.");

	  long start = System.nanoTime();
	  int maxAllowed = (1 << bits)-1;

	  WadoImage wiRet = wi.clone();
	  
	  int previousBits = getPreviousReducedBits(wi);
	  
	  int originalSmallestPixelValue = getPreviousSmallestPixelValue(wi);
	  int originalLargestPixelValue = getPreviousLargestPixelValue(wi);
	  MinMaxResults minMaxResults = new MinMaxResults();
	  if ( previousBits == 0 ) {
		  minMaxResults = getSmallestLargest(wiRet);
		  originalSmallestPixelValue = minMaxResults.min;
		  originalLargestPixelValue = minMaxResults.max;
	  } else {
		  minMaxResults.min = 0;
		  minMaxResults.max = (1 << previousBits)-1;
	  }
	  
	  int needsProcessingFlags = calculateNeedsProcessing( maxAllowed, minMaxResults);
	  
	  WritableRaster originalRaster = wi.getValue().getRaster();

	  if ( needsProcessingFlags == NEEDS_NO_PROCESSING ) {
		  if ( bits <= 8 ) {
			  BufferedImage oldBI = wi.getValue();
			  if ( oldBI.getRaster().getDataBuffer().getDataType() != DataBuffer.TYPE_BYTE ) {
				  BufferedImage bi = castDownToByteBufferedImage(bits, oldBI);
				  wi.setValue(bi);
				  if ( bi == null ) {
					  return null;
				  }
				  log.debug("Within 8-bit reduced-bit unsigned range already - but needed to cast data down from 12-bit." + nanoTimeToString(System.nanoTime() - start));
			  } 
		  }
		  else {
			  BufferedImage bi = new BufferedImage(cm12, originalRaster, false, null);
			  wi.setValue(bi);
			  log.debug("Within 12-bit reduced-bit unsigned range already - no image change " + nanoTimeToString(System.nanoTime() - start));
		  }
		  return wi;
	  }
	  
	  int[] smallestLargestOutput = new int[2];
	  LookupTable lut = createLookupTable(minMaxResults, previousBits, bits, ds, needsProcessingFlags, smallestLargestOutput);
	  if ( previousBits == 0 ) {
		  //TODO: because we are not expanding smaller data ranges "up" to the desired bits,
		  //it is possible that a 9-bit image is described as 12-bit here.  This is fine, but
		  //if the data goes through the filter a second time with a desired bits = 8, we will
		  //reduce what we think is 12 bits down to 8, rather than 9-bits down to 8.  This will
		  //greatly reduce gray levels.  This can be fixed if needed, but we do not want to do the old
		  //strategy of increasing the data range to match the desired bits.
		  //
		  originalSmallestPixelValue = smallestLargestOutput[0];
		  originalLargestPixelValue = smallestLargestOutput[1];
	  }
	  addSmallestLargestToWadoImage(wiRet, originalSmallestPixelValue, originalLargestPixelValue, bits);
	  BufferedImage bi = applyLUT(bits, lut, originalRaster);
	  
	  wiRet.setValue(bi);
	  
	  log.info(""+bits+" bit scaled " + nanoTimeToString(System.nanoTime() - start)+" small/large="+minMaxResults.min+","+minMaxResults.max);

	  return wiRet;
   }


   protected static BufferedImage castDownToByteBufferedImage(int bits, BufferedImage oldBI) {
	   ColorModel cm8 = new ComponentColorModel(gray, new int[] {bits}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
	   WritableRaster raster = cm8.createCompatibleWritableRaster(oldBI.getWidth(), oldBI.getHeight());
	   DataBuffer oldData = oldBI.getRaster().getDataBuffer();
	   short [] source = null;
	   int sourceOffset = oldData.getOffset();
	   int sourceSize = oldData.getSize();
	   if ( oldData instanceof DataBufferShort ) {
		   source = ((DataBufferShort)oldData).getData();
	   } else if ( oldData instanceof DataBufferUShort ) {
		   source = ((DataBufferUShort)oldData).getData();
	   }
	   if ( source == null ) {
		   log.error("Source data is not a short data type.  Cannot convert to 8-bits");
		   return null;
	   }
	   DataBufferByte newData = (DataBufferByte)( raster.getDataBuffer() );
	   byte [] destin = newData.getData();
	   if ( destin.length < sourceSize ) {
		   log.error("Source size is "+sourceSize+" but we generated output image of only "+destin.length+" pixels.");
		   return null;
	   }
	   for ( int i = 0; i < destin.length; i++ ) {
		   destin[i] = (byte)(source[i+sourceOffset] & 0xff);
	   }
	   BufferedImage bi = new BufferedImage(cm8, raster, false, null);
	   return bi;
   }


   protected static int calculateNeedsProcessing(int maxAllowed, MinMaxResults minMaxResults) {
	   boolean needsMasking = minMaxResults.bitsNeedMasking;
	   boolean needsBitReduction = ( ( minMaxResults.max - minMaxResults.min) > maxAllowed );
	   boolean needsDataRangeShift = ( (minMaxResults.min < 0) || (minMaxResults.max > maxAllowed ) );
	   if ( (minMaxResults.min == 0) && needsBitReduction ) {
		   needsDataRangeShift = false;
	   }
	   boolean needsPaddingRemoved = false;
	   int minIncludingPadding = minMaxResults.min;
	   int maxIncludingPadding = minMaxResults.max;
	   if ( minMaxResults.pixelPaddingFound ) {
		   minIncludingPadding = Math.min(minMaxResults.min, minMaxResults.minPixelPadding);
		   maxIncludingPadding = Math.max(minMaxResults.max, minMaxResults.maxPixelPadding);
		   needsPaddingRemoved = ( ( maxIncludingPadding - minIncludingPadding) > maxAllowed ) ;
		   if ( ( ! needsDataRangeShift ) && ( ! needsPaddingRemoved ) ) {
			   if ( ( (minIncludingPadding < 0) || (maxIncludingPadding > maxAllowed ) ) ) {
				   needsDataRangeShift = true;
			   }
		   }
	   }
	   int needsProcessingFlags = NEEDS_NO_PROCESSING;
	   if ( needsMasking ) { needsProcessingFlags |= NEEDS_MASKING_FLAG; }
	   if ( needsBitReduction ) { needsProcessingFlags |= NEEDS_BIT_REDUCTION_FLAG; }
	   if ( needsDataRangeShift ) { needsProcessingFlags |= NEEDS_DATA_RANGE_SHIFT_FLAG; }
	   if ( needsPaddingRemoved ) { needsProcessingFlags |= NEEDS_PIXEL_PADDING_REMOVED_FLAG; }
	   return needsProcessingFlags;
   }

   
   protected static BufferedImage applyLUT(int bits, LookupTable lut, WritableRaster r) {

	   BufferedImage bi;

	   if( bits>8 ) {
		   
		   ColorModel cm = new ComponentColorModel(gray, new int[]{bits}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
		   bi = new BufferedImage(cm, cm.createCompatibleWritableRaster(r.getWidth(), r.getHeight()), false, null);
		   
	   } else {
		   
		   if( bits!=8 ) throw new IllegalArgumentException("Only 8...15 bits supported for reduce bits filter.");
		   bi = new BufferedImage( r.getWidth(), r.getHeight(), BufferedImage.TYPE_BYTE_GRAY); 
	   }
	   lut.lookup(r.getDataBuffer(), bi.getRaster().getDataBuffer() );
	   
	   return bi;
   }
   
   
   protected static LookupTable createLookupTable(MinMaxResults minMaxResults, int previousBitsIfNotZero, int outBits, DicomObject ds, int needsProcessingFlags, int[] smallestLargestOutput ) {
	   
	   int maxAllowed = (1 << outBits )-1;
	   int range = maxAllowed;
	   int lowRampStart = 0;
	   if ( (needsProcessingFlags & NEEDS_BIT_REDUCTION_FLAG) != 0 ) {
		   range = minMaxResults.max;
		   if ( (needsProcessingFlags & NEEDS_DATA_RANGE_SHIFT_FLAG) != 0 ) {
			   lowRampStart = minMaxResults.min;
			   range = minMaxResults.max - minMaxResults.min;
		   }
	   }
	   else if ( (needsProcessingFlags & NEEDS_DATA_RANGE_SHIFT_FLAG) != 0 ) {
		   lowRampStart = minMaxResults.min;
		   // We could do special CT code here, but as long as we are going to apply
		   // the pixel-padding as a separate overlay, I don't think it is necessary.
	   }
	   int entries = range + 1;
	   
	   // We either have the results of a previous ReduceBitsFilter, or else we need to check
	   // the DICOM header.
	   int stored = previousBitsIfNotZero;
	   boolean signed = false;
	   if ( stored <= 0 ) {
		   stored = ds.getInt(Tag.BitsStored, 16);
		   signed = ds.getInt(Tag.PixelRepresentation) == 1;
	   }
	   
	   LookupTable lut = null;
	   
	   if( outBits > 8)
	   {
		   short[] slut = new short[entries];
		   for (int i = 0; i < entries; i++) {
			   slut[i] = (short) ((maxAllowed * i) / range);
		   }
		   lut = new ShortLookupTable(stored, signed, lowRampStart, outBits, slut);
	   }
	   else {

		   if( outBits!=8 ) throw new IllegalArgumentException("Only 8...15 bits supported for reduce bits filter.");
		   byte[] blut = new byte[entries];
		   for (int i = 0; i < entries; i++) {
			   blut[i] = (byte) ((maxAllowed * i) / range);
		   }
		   lut = new ByteLookupTable(stored, signed, lowRampStart, outBits, blut);
	   }
	   smallestLargestOutput[0] = lowRampStart;
	   smallestLargestOutput[1] = lowRampStart + range;
	   
	   return lut;
   }
   
  

   /**
    * Adds the smallest and largest pixel value before data resampling, and the number
    * of bits we resample to, into the WadoImage response headers and filename.
    * @param wi
    * @param smallestPixelValue
    * @param largestPixelValue
    * @param bitsAfterResampling
    */
   @SuppressWarnings("unchecked")
   public static void addSmallestLargestToWadoImage(WadoImage wi, int smallestPixelValue, int largestPixelValue, int bitsAfterResampling ) {
	   // Add the header now so it is always available.
	   Collection<String> headers = (Collection<String>) wi.getParameter(RESPONSE_HEADERS);

	   if( headers==null ) {
		   headers = new HashSet<String>(3);
		   wi.setParameter(RESPONSE_HEADERS, headers);
	   }	  

	   headers.add(SMALLEST_IMAGE_PIXEL_VALUE);
	   headers.add(LARGEST_IMAGE_PIXEL_VALUE);
	   headers.add(REDUCED_BITS);

	   String filename = getStrippedFilename(wi);

	   wi.setParameter(SMALLEST_IMAGE_PIXEL_VALUE, ""+smallestPixelValue);
	   wi.setParameter(LARGEST_IMAGE_PIXEL_VALUE, ""+largestPixelValue);
	   wi.setParameter(REDUCED_BITS, ""+bitsAfterResampling);

	   String filenameAddition = getFilenameAddition(smallestPixelValue, largestPixelValue, bitsAfterResampling );
	   wi.setFilename( filename + filenameAddition);	   
   }

   protected static String getStrippedFilename(WadoImage wi) {
	   String filename = wi.getFilename();
	   int previousReducedBits = getPreviousReducedBits(wi);
	   if (previousReducedBits != 0){
		   int previousSmallestPixelValue = getPreviousSmallestPixelValue(wi);
		   int previousLargestPixelValue = getPreviousLargestPixelValue(wi);		  

		   String oldFilenameAddition = getFilenameAddition( previousSmallestPixelValue, 
				   previousLargestPixelValue, previousReducedBits);

		   filename = filename.replace(oldFilenameAddition, "");			  
	   }
	   return filename;
   }

   protected static String getFilenameAddition(int smallestPixelValue, int largestPixelValue, int bitsAfterResampling ){
	   return "-pixelRange"+smallestPixelValue+","+largestPixelValue+","+bitsAfterResampling;
   }
   
   protected static int getPreviousReducedBits(WadoImage wi) {
		  return (int)wi.getParameter(REDUCED_BITS, 0.0f);
   }
   protected static int getPreviousSmallestPixelValue(WadoImage wi) {
	   return (int)wi.getParameter(SMALLEST_IMAGE_PIXEL_VALUE, 0.0f);
   }
   protected static int getPreviousLargestPixelValue(WadoImage wi) {
	   return (int)wi.getParameter(LARGEST_IMAGE_PIXEL_VALUE, 0.0f);
   }
   
   /** 
    * Determine the smallest and largest pixel-value, either from already existing
    * DICOM tags, or by scanning the pixel data.
    * @returns an array of the smallest/largest values.
    * @param wi
    */
   public static MinMaxResults getSmallestLargest(WadoImage wi) {
	  WritableRaster r = wi.getValue().getRaster();
	  DicomObject ds = wi.getDicomObject();
	  
	  return ExtractOverlaysAndPixelPadding.calcMinMax(ds, r.getDataBuffer());
   }


   public static boolean mayRequireRescale(WadoImage wi, int bits, DicomObject ds) {
	   boolean isGray = (ds.getInt(Tag.SamplesPerPixel)==1);
	   if( !isGray ) {
		   log.info("Color - not rescaling.");
		   return false;
	   }
	   boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
	   if ( signed ) {
		   return true;
	   }
	   if ( ds.getInt(Tag.BitsAllocated,8) <= 8 ) {
		   log.debug("8-bits allocated - not rescaling.");
		   return false;
	   }
	   if ( wi != null ) {
		   if ( wi.getValue().getRaster().getDataBuffer().getDataType() == DataBuffer.TYPE_BYTE ) {
			   log.info("byte DataBuffer.  We cannot rescale bits, but why isn't it labelled as 8 bits allocated?");
			   return false; 
		   }
	   }
	  return true;
   }
   
   public static boolean needsRescaleWhenRequestIsForRawBytes( WadoImage wi, int bits, DicomObject ds) {
	   if ( ! mayRequireRescale( wi, bits, ds) ) {
		   return false;
	   }
	   int previouslyResampledBits = 0;
	   if ( wi != null ) { 
		   previouslyResampledBits = getPreviousReducedBits(wi);
	   }
	   int stored = ds.getInt(Tag.BitsStored);
	   boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
	   if ( previouslyResampledBits != 0 ) {
		   stored = previouslyResampledBits;
		   signed = false;
	   }
	   // This will ensure that in current versions, colour images won't pass here,
	   // and that regular images that don't need any translation will go directly through this filter.
	   if( stored > bits || signed ) {
		   log.info("Needs rescale - stored bits "+stored+" allowed "+bits+" signed "+signed);
		   return true;
	   }
	   log.info("Does not need rescale - {} of {} unsigned",stored,bits);
	   return false;

   }
   
   private Filter<DicomObject> dicomImageHeader;

   /** Gets the filter that returns the dicom object image header */
	public Filter<DicomObject> getDicomImageHeader() {
   	return dicomImageHeader;
   }

	@MetaData(out="${ref:dicomImageHeader}")
	public void setDicomImageHeader(Filter<DicomObject> dicomImageHeader) {
   	this.dicomImageHeader = dicomImageHeader;
   }
}
