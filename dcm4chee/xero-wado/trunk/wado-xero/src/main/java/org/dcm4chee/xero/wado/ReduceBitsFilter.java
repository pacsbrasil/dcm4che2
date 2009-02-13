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
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
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
 * having been applied.
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

   /**
    * Reduces the number of bits from 13-16 to 12, only for contentType=image/jp12
    */
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  int bits = FilterUtil.getInt(params,EncodeImage.MAX_BITS);
	  if( bits==0 || bits>15 ) {
		  log.debug("Not applying reduce bits - either default of 16 bits is allowed: {}", bits);
		  return filterItem.callNextFilter(params);
	  }

	  if( params.containsKey(WadoImage.IMG_AS_BYTES) ) {
		 // Check up-front to see if the image can be returned as bytes so that no
		 // decoding is necessary.  This only works if the correct number of bits is set.
		 DicomObject ds = dicomImageHeader.filter(null,params);
		 if( !needsRescale(null,bits,ds) ) {
			 log.debug("Image doesn't need to be rescaled and can be returned as raw bytes.");
			 return filterItem.callNextFilter(params);
		 }
		 // Can't get it as bytes....
		 FilterUtil.removeFromQuery(params,WadoImage.IMG_AS_BYTES);
		 params.remove(WadoImage.IMG_AS_BYTES);
	  }

	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if( wi==null ) return wi;
	  DicomObject ds = wi.getDicomObject();
	  if( !needsRescale(wi,bits,ds) )  {
		  log.debug("Image doesn't need to be rescaled, and can be returned as a regular image.");
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
	  
	  WritableRaster r = wi.getValue().getRaster();

	  if ( (minMaxResults.min >= 0) && (minMaxResults.max <= maxAllowed) ) {
		 // This is actually just a bits sized raw image - don't need to
         // transcode it - could compute actual bits, but this is good enough.
		 // This case is VERY common for CT images so it is worth including.  As well it doesn't
		 // require any actual changes to the results - however, it should be fixed to work
		 // with 8 bit actual images as well...
	     // TODO - fix thread safety of this code.
		 // FIXME: is changing BitsStored necessary?  We don't do it when we DO need to
		 // reduce the bits, so why do it when we DON'T need to reduce the bits?  By only
		 // changing the BitsStored, and not the HighBit and PixelRepresentation, we can be
		 // making a corrupt header.
		 ds.putInt(Tag.BitsStored, VR.IS, bits);
		 BufferedImage bi = new BufferedImage(cm12, r, false, null);
		 wi.setValue(bi);
		 log.info("Within reduced bit range already - no image change " + nanoTimeToString(System.nanoTime() - start));

		 return wi;
	  }
	  
	  addSmallestLargestToWadoImage(wiRet, originalSmallestPixelValue, originalLargestPixelValue, bits);

	  LookupTable lut = createLookupTable(minMaxResults, bits, maxAllowed, ds);
	  BufferedImage bi = applyLUT(bits, wi, lut, r);
	  
	  wiRet.setValue(bi);
	  
	  log.info(""+bits+" bit scaled " + nanoTimeToString(System.nanoTime() - start)+" small/large="+minMaxResults.min+","+minMaxResults.max);

	  return wiRet;
   }

   
   protected BufferedImage applyLUT(int bits, WadoImage wi, LookupTable lut, WritableRaster r) {

	   BufferedImage bi;

	   if( bits>8 ) {
		   
		   ColorModel cm = new ComponentColorModel(gray, new int[]{bits}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_USHORT);
		   bi = new BufferedImage(cm, cm.createCompatibleWritableRaster(r.getWidth(), r.getHeight()), false, null);
		   lut.lookup(r.getDataBuffer(), bi.getRaster().getDataBuffer());
		   
	   } else {
		   
		   if( bits!=8 ) throw new IllegalArgumentException("Only 8...15 bits supported for reduce bits filter.");
		   bi = new BufferedImage( wi.getValue().getWidth(), wi.getValue().getHeight(), BufferedImage.TYPE_BYTE_GRAY); 
		   lut.lookup(r.getDataBuffer(), bi.getRaster().getDataBuffer());
	   }
	   
	   return bi;
   }
   
   
   protected LookupTable createLookupTable(MinMaxResults minMaxResults, int bits, int maxAllowed, DicomObject ds) {
	   
	   int range = minMaxResults.max - minMaxResults.min;
	   int entries = range + 1;
	   
	   int stored = ds.getInt(Tag.BitsStored);
	   boolean signed = ds.getInt(Tag.PixelRepresentation) == 1;
	   
	   LookupTable lut = null;
	   
	   if( bits > 8)
	   {
		   short[] slut = new short[entries];
		   for (int i = 0; i < entries; i++) {
			   slut[i] = (short) ((maxAllowed * i) / range);
		   }
		   lut = new ShortLookupTable(stored, signed, minMaxResults.min, bits, slut);
	   }
	   else {

		   if( bits!=8 ) throw new IllegalArgumentException("Only 8...15 bits supported for reduce bits filter.");
		   byte[] blut = new byte[entries];
		   for (int i = 0; i < entries; i++) {
			   blut[i] = (byte) ((maxAllowed * i) / range);
		   }
		   lut = new ByteLookupTable(stored, signed, minMaxResults.min, bits, blut);
	   }
	   
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


   public static boolean needsRescale(WadoImage wi, int bits, DicomObject ds) {
	  boolean isGray = (ds.getInt(Tag.SamplesPerPixel)==1);
	  if( !isGray ) {
		 log.info("Not grayscale - not rescaling.");
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
