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

import static org.dcm4chee.xero.metadata.filter.FilterUtil.getFloats;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;
import static org.dcm4chee.xero.metadata.servlet.MetaDataServlet.nanoTimeToString;
import static org.dcm4chee.xero.wado.WadoParams.FRAME_NUMBER;
import static org.dcm4chee.xero.wado.WadoParams.OBJECT_UID;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageReadParam;
import javax.imageio.metadata.IIOMetadata;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.image.ColorModelFactory;
import org.dcm4che2.image.OverlayUtils;
import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * This class takes a URL to either a file location or another WADO service, and
 * uses it to read an image, providing the raw, buffered image data (without
 * modalty LUT/rescale slope & intercept, and without window levelling). It DOES
 * handle the region encoding and resolution (rows/cols) fields, as this needs
 * to be done as part of the initial read.
 * 
 * @author bwallace
 */
public class DicomImageFilter implements Filter<WadoImage> {
   public static final String COLOR_MODEL_PARAM = "ColorModelParam";
   public static final String RET = "ret";
   public static final String SUBSAMPLE_INDEX = "subSampleIndex";

   private static Logger log = LoggerFactory.getLogger(DicomImageFilter.class);

   /**
     * Read the raw DICOM image object
     * 
     * @return WADO image.
     */
   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  WadoImage ret = null;
	  DicomImageReader reader = dicomImageReaderFilter.filter(null, params);
	  
	  if (reader == null) {
		 log.warn("Couldn't find reader for DICOM object.");
		 return null;
	  }
	  // In case a size has been added, remove it as the size is changing.
	  params.remove(MemoryCacheFilter.CACHE_SIZE);
	  ImageReadParam param = reader.getDefaultReadParam();
	  DicomImageReadParam dParam = (DicomImageReadParam) param;
	  dParam.setOverlayRGB((String) params.get("rgb"));

	  int frame = FilterUtil.getInt(params,FRAME_NUMBER,1)-1;
	  // Overlays are specified as the actual overlay number, not as an
	  // offset value.
	  if ((frame & 0x60000000) != 0)	frame++;

	  try {
		 long start = System.nanoTime();
		 String op = "decompress";
		 synchronized (reader) {
			 int width = reader.getWidth(0);
			 int height = reader.getHeight(0);

			 String filenameExtra = updateParamFromRegion(param, params, width, height);
			 
			 BufferedImage bi;
			 DicomStreamMetaData streamData = (DicomStreamMetaData) reader.getStreamMetadata();
			 DicomObject ds = streamData.getDicomObject();
			 
			 boolean readRawBytes = getReadAsRawBytes(params, reader, param, frame, ds);			 
			 
			 ret = new WadoImage(streamData.getDicomObject(), ds.getInt(Tag.BitsStored), null);
			 ret.setFilename((String) params.get(OBJECT_UID)+"-f"+(frame+1)+filenameExtra);
			 if (readRawBytes) {
				 byte[] img = reader.readBytes(frame, param);
				 ret.setParameter(WadoImage.IMG_AS_BYTES, img);
				 setAsBytesTransferSyntax(ret, ds, reader, frame);
				 bi = null;
				 op="read raw";
			 } else if (OverlayUtils.isOverlay(frame) || !ColorModelFactory.isMonochrome(ds)) {
				 // This object can't be window leveled, so just get it as
				 // a buffered image directly.
				 bi = reader.read(frame, param);
				 log.debug("ColorSpace of returned buffered image is " + bi.getColorModel().getColorSpace());
				 op="read overlay/colour";
			 }  else {
				 log.debug("Requested source {}", param.getSourceRegion());
				 log.debug("Requested subsampling {},{}", param.getSourceXSubsampling(), param.getSourceYSubsampling());
				 WritableRaster r = (WritableRaster) reader.readRaster(frame, param);
				 log.debug("Size of returned raster {}", r.getBounds());
				 ColorModel cm = ColorModelFactory.createColorModel(ds);
				 log.debug("Color model for image is " + cm);
				 bi = new BufferedImage(cm, r, false, null);
			 }
			 Object input = reader.getInput();
			 if( ds.getInt(Tag.NumberOfFrames,1)==1 && (input instanceof ReopenableImageInputStream) ) {
				 log.info("Closing re-openable stream for {}",ret.getFilename());
				 ((ReopenableImageInputStream) input).close();
			 }
			 else log.info("Not closing re-openable multi-frame stream {}",ret.getFilename());

			 ret = setPixelRangeInformation(reader, frame, ret);
			 ret.setValue(bi);
			 log.info("Time to "+op+" image "+ret.getFilename()+" ts=" + ds.getString(Tag.TransferSyntaxUID) + " only is "
					 + nanoTimeToString(System.nanoTime() - start));
		 }
	  } catch (Exception e) {
		 log.error("Caught Exception reading image "+params.get(OBJECT_UID)+" frame "+(frame+1)+" exception ",e);
		 ret.setError(e);
	  }
	  // This might happen if the instance UID under request comes from
	  // another
	  // system and needs to be read in another way.
	  if (ret == null)
		 return (WadoImage) filterItem.callNextFilter(params);
	  return ret;
   }

   @SuppressWarnings("unchecked")
   protected boolean getReadAsRawBytes( Map<String, Object> params,
		   							    DicomImageReader reader, 
		   							    ImageReadParam param, 
		   							    int frame, 
		   							    DicomObject ds) {
	   boolean readRawBytes = params.containsKey(WadoImage.IMG_AS_BYTES);
	   List<String> rawBytesForTransferSyntaxes = (List<String>)params.get(WadoImage.IMG_AS_BYTES_ONLY_FOR_TRANSFER_SYNTAXES);
	   String tsUID = getReaderRawTransferSyntax( ds, reader, frame );
	   if ( (rawBytesForTransferSyntaxes != null) && rawBytesForTransferSyntaxes.contains(tsUID) ) {
		   readRawBytes = true;
	   }
	   if ( readRawBytes ) {
		   Point rawSubsamping = getReaderRawSubsampleIndices( reader, frame);
		   if ( ( rawSubsamping.x != param.getSourceXSubsampling() ) ||
				( rawSubsamping.y != param.getSourceYSubsampling() ) ) {
			   log.debug("Not reading raw bytes because raw subsample factor is not the same as the required value.");
			   readRawBytes = false;
		   }
	   }
	   return readRawBytes;
   }
   
   protected Point getReaderRawSubsampleIndices( DicomImageReader reader,
		   int frame) {
	   int subX = 1;
	   int subY = 1;
	   try {
		   IIOMetadata metadata = reader.getImageMetadata(frame);
		   if ( metadata != null ) {
			   final String nodeName = "transferSyntax";
			   Element node = (Element) metadata.getAsTree(nodeName);
			   String subsampleX = node.getAttribute("subsampleX");
			   String subsampleY = node.getAttribute("subsampleY");
			   if ( subsampleX != null ) {
				   subX = Integer.parseInt(subsampleX);
				   if ( subX < 1 ) {
					   subX = 1;
				   }
			   }
			   if ( subsampleY != null ) {
				   subY = Integer.parseInt(subsampleY);
				   if ( subY < 1 ) {
					   subY = 1;
				   }
			   }
		   }
	   } catch (Exception e) {
	   }
	   return new Point(subX, subY);
	   
   }
   
   protected String getReaderRawTransferSyntax( 
		   DicomObject ds, 
		   DicomImageReader reader,
		   int frame) {
	   String tsUID = null;
	   try {
		   IIOMetadata metadata = reader.getImageMetadata(frame);
		   if ( metadata != null ) {
			   final String nodeName = "transferSyntax";
			   Element node = (Element) metadata.getAsTree(nodeName);
			   tsUID = node.getAttribute("transferSyntax");
		   }
	   } catch (Exception e) {
	   }
	   if ( tsUID == null ) {
		   tsUID = ds.getString(Tag.TransferSyntaxUID);
	   }
	   return tsUID;
   }

   protected void setAsBytesTransferSyntax(
		   WadoImage ret, 
		   DicomObject ds, 
		   DicomImageReader reader,
		   int frame) {
	   String tsUID = getReaderRawTransferSyntax( ds, reader, frame );
	   ret.setParameter(WadoImage.AS_BYTES_RETURNED_TRANSFER_SYNTAX, tsUID);
	   
   }
   
   /**
    * Gets the IIOMetadata of the specific frame.
    * <p>
    * In this specific case the pixelRange node contains some information about previously done
    * pixel-range reduction in the ReduceBits filter.  The ReduceBitsFilter adds this information
    * to the filename.  When bit-reduced data is retrieved from cache, via a subclass of
    * DicomImageReader, that reader must provide an IIOMetatdata object with the node:
    * <p>
    * "pixelRange", with attributes.
    * <LU>
    * <LI>ReduceBitsFilter.SMALLEST_IMAGE_PIXEL_VALUE</LI>
    * <LI>ReduceBitsFilter.LARGEST_IMAGE_PIXEL_VALUE</LI>
    * <LI>ReduceBitsFilter.REDUCED_BITS</LI>
    * </LU>
    * <p>
    * Look in ReduceBitsFilter for the actual string attribute names.
    * <p>
    * 
    * @param reader
    * @param frame
    * @param wi
    * @return
    */
   protected WadoImage setPixelRangeInformation(DicomImageReader reader, int frame, WadoImage wi){
	   IIOMetadata metadata;
	   try {
		   metadata = reader.getImageMetadata(frame);
	   } catch (IOException e) {
		   log.error("Could not read image metadata, assume no previous bit reduction.");
		   e.printStackTrace();
		   return wi;
	   }
	   if ( metadata == null ) {
		   return wi;
	   }
	   final String nodeName = "pixelRange";
	   Element pixelRangeNode = (Element)metadata.getAsTree(nodeName);
	   if ( pixelRangeNode == null ) {
		   log.debug("Image metadata available, but no pixelRange available?");
		   return wi;
	   }
	   int smallestPixelValue = Integer.valueOf(pixelRangeNode.getAttribute(ReduceBitsFilter.SMALLEST_IMAGE_PIXEL_VALUE));
	   int largestPixelValue = Integer.valueOf(pixelRangeNode.getAttribute(ReduceBitsFilter.LARGEST_IMAGE_PIXEL_VALUE));
	   int bitsAfterResampling = Integer.valueOf(pixelRangeNode.getAttribute(ReduceBitsFilter.REDUCED_BITS));

	   ReduceBitsFilter.addSmallestLargestToWadoImage(wi, smallestPixelValue, largestPixelValue, bitsAfterResampling);
	   return wi;
   }

   /**
    * Given an image dimension and a subsample factor, calculate the final dimension.  This is
    * integer division with upwards rounding.
    * <p>
    * <code><pre>
    * return ( startSize + subsampleFactor - 1 ) / subsampleFactor;
    * We choose subsampleFactor such that 0<=( outputSize - desiredSize ) is a minimum.
    * </pre></code>
    * @param startSize the initial size before subsampling.
    * @param subsampleFactor an subsample factor >0
    * @return the calculated final size.
    */
   public static int calculateFinalSizeFromSubsampling( int startSize, int subsampleFactor ) {
	   if ( subsampleFactor <= 0 ) {
		   log.error("bad subsample factor of " + subsampleFactor);
		   return startSize;
	   }
	   return ( startSize + subsampleFactor - 1 ) / subsampleFactor;
   }
   
   /**
    * Based on one dimension only, calculate the needed power-of-2 subsample factor.
    * <p>
    * <code><pre>
    * int outputSize = ( startSize + subsampleFactor - 1 ) / subsampleFactor;
    * We choose subsampleFactor such that 0<=( outputSize - desiredSize ) is a minimum, and subsampleFactor
    * is an positive-integer power-of-2.
    * </pre></code>
    * @param startSize the start dimension of the image before subsampling.
    * @param desiredSize the final output scaling size desired.  If zero, we simply return 1 (no subsampling).
    * @return the positive-integer power-of-2 subsampling factor that will get us the closest output size larger than, or equal to the
    *         desiredSize.
    */
   public static int calculateDesiredSubsamplingFactorForOneDimension( int startSize, int desiredSize ) {
	   
	   return (1 << calculateDesiredPowerOfTwoResolutionLevelForOneDimension(startSize, desiredSize));
   }
   
   /**
    * Based on one dimension only, calculate the needed power-of-2 resolution level.  Resolution
    * Level is the value 'level' such that 2^level is the scale-down ratio.
    * <p>
    * <code><pre>
    * int outputSize = ( startSize + 2^level - 1 ) / 2^level;
    * We choose level such that 0<=( outputSize - desiredSize ) is a minimum, and level>=0.
    * </pre></code>
    * @param startSize the start dimension of the image before subsampling.
    * @param desiredSize the final output scaling size desired.  If zero, we simply return 0.
    * @return the level that will get us the closest output size larger than, or equal to the
    *         desiredSize.
    */
   public static int calculateDesiredPowerOfTwoResolutionLevelForOneDimension( int startSize, int desiredSize ) {
	   if ( ( desiredSize <= 0 ) || ( desiredSize >= startSize ) ) {
		   log.debug("startSize "+startSize+" or desiredSize "+desiredSize+" are outside normal range. Returning resolution level of 0 (original)");
		   return 0;
	   }
	   double rawFactor = (double)(startSize)/(double)(desiredSize);
	   double logOfFactor = Math.log(rawFactor)/Math.log(2.0);
	   int roundedLogOfFactor = (int)Math.round(logOfFactor);
	   int startTry = Math.max(0, roundedLogOfFactor-1);
	   int endTry = roundedLogOfFactor+1;

	   int levelToUse = 0;
	   int bestDiff = Integer.MAX_VALUE;
	   for ( int level = startTry; level <= endTry; level++ ) {
		   int subsampleFactor = 1 << level;
		   int subsampleSize = calculateFinalSizeFromSubsampling( startSize, subsampleFactor );
		   int diff = subsampleSize - desiredSize;
		   if ( ( diff <= bestDiff ) && ( diff >= 0 ) ) {
			   bestDiff = diff;
			   levelToUse = level;
		   }
	   }
	   return levelToUse;
   }
   
   /**
    * Check if an integer is equal to 2^n, where n is in {0,1,2,3,...}.  
    * Therefore value must be {1,2,4,8,16,...}.
    * @param value a value to test.
    * @return true if the integer is a power of 2, else false.
    */
   protected static boolean isPowerOfTwo( int value ) {
	   if ( value <= 0 ) {
		   return false;
	   }
	   int testValue = value;
	   int i = 0;
	   while ( testValue > 1 ) {
		   i++;
		   testValue /= 2;
	   }
	   if ( ( 1L << i ) == value ) {
		   return true;
	   }
	   return false;
   }

   /**
    * Given the "region" parameter, and the source image dimensions, calculate the desired image region in pixels.
    * @param region the desired fractional region, in format {x0,y0,x1,y1} where the elements are between 0.0 and 1.0
    * @param fullWidth the source width to apply the region to.
    * @param fullHeight the source height to apply the region to.
    * @return the rectangle on the source image
    */
   public static Rectangle calculateSubregionFromRegionFloatArray( float[] region, int fullWidth, int fullHeight ) {
	   int xOffset = (int)Math.round(region[0] * fullWidth);
	   int yOffset = (int)Math.round(region[1] * fullHeight);
	   int sWidth = (int)Math.round((region[2] - region[0]) * fullWidth);
	   int sHeight = (int)Math.round((region[3] - region[1]) * fullHeight);
	   Rectangle desiredRect = new Rectangle(xOffset, yOffset, sWidth, sHeight);
	   return desiredRect.intersection( new Rectangle(0,0,fullWidth,fullHeight));
   }

   /**
     * Compute the source, sub-sampling and destination regions appropriately
     * from the region, rows and cols in params.
     * Also returns a human readable version of the same information, typically to be used as part of a filename.
     * -wX-hY-rTL,BR, but null if full defaults;
     * 
     * @param read
     * @param params
     * @param width source image width (not subsampled)
     * @param height source image height (not subsampled)
     */
   public static String updateParamFromRegion(ImageReadParam read, Map<String, Object> params, int width, int height) {
	   String ret = "";
	   float[] region = getFloats(params, "region", null);
	   int rows = getInt(params, "rows");
	   int cols = getInt(params, "cols");
	   log.debug("DicomImageFilter rows=" + rows + " cols=" + cols);
	   Rectangle fullRegion = new Rectangle(0, 0, width, height);
	   Rectangle subRegion = new Rectangle( fullRegion );

	   if (region != null) {
		   // Figure out the sub-region to use
		   subRegion = calculateSubregionFromRegionFloatArray(region,width,height);
		   if( ! subRegion.equals( fullRegion ) ) {
			   read.setSourceRegion(subRegion);
			   ret="-r"+subRegion.x+","+subRegion.y+","+subRegion.width+","+subRegion.height;
			   log.debug("Source region {} region {}",subRegion,region);
		   }
	   }

	   if (rows == 0 && cols == 0)
		   return ret;

	   // Now figure out the size of the final region
	   int subsampleX = 1;
	   int subsampleY = 1;
	   if (cols != 0) {
		   subsampleX = calculateDesiredSubsamplingFactorForOneDimension( subRegion.width, cols );
		   subsampleY = subsampleX;
	   }
	   if (rows != 0) {
		   subsampleY = calculateDesiredSubsamplingFactorForOneDimension( subRegion.height, rows );
		   if (cols == 0)
			   subsampleX = subsampleY;
	   }
	   // Can't over-sample the data...
	   if (subsampleX < 1)
		   subsampleX = 1;
	   if (subsampleY < 1)
		   subsampleY = 1;
	   if (subsampleX == 1 && subsampleY == 1)
		   return ret;
	   log.debug("Sub-sampling " + subsampleX + "," + subsampleY + " sWidth,Height=" + subRegion.width + "," + subRegion.height );
	   read.setSourceSubsampling(subsampleX, subsampleY, 0, 0);
	   ret = "-s"+subsampleX+","+subsampleY+ret;	  
	   params.put(RET, ret);
	   return ret;
   }

   /**
    * 
    * @param regionAndSubsampleDescription of the form: -sSx,Sy-rX0,Y0,SubWidth,SubHeight 
    *         ( i.e. -s3,3-r0,16,256,512 )
    * @return A rectangle containing the region information, or null if the region was not specified in the 
    *         input String, meaning the region is equal to the full image.
    */
   public static Rectangle getRegionInformationFromFilenameString(String regionAndSubsampleDescription){
	   Pattern pattern = Pattern.compile("-r([0-9]+),([0-9]+),([0-9]+),([0-9]+)");
	   Matcher matcher = pattern.matcher(regionAndSubsampleDescription);
	   if (!matcher.find()){
		   log.debug("The region indicator -r was not found in the filename, return null.");
		   return null;
	   }
	   return new Rectangle(Integer.valueOf(matcher.group(1)), 
			   Integer.valueOf(matcher.group(2)),
			   Integer.valueOf(matcher.group(3)),
			   Integer.valueOf(matcher.group(4)));
   }

   /**
    * 
    * @param regionAndSubsampleDescription of the form: -sSx,Sy-rX0,Y0,SubWidth,SubHeight 
    *         ( i.e. -s3,3-r0,16,256,512 )
    * @return A Point contaning the subSample factor in the X and Y directions. If the subSample factor
    *         was not specified in the input String (i.e. -s was not found), then a Point containing (1,1)
    *         is returned. 
    */
   public static Point getSubSampleInformationFromFilenameString(String regionAndSubsampleDescription){
	   Pattern pattern = Pattern.compile("-s([0-9]+),([0-9]+)");
	   Matcher matcher = pattern.matcher(regionAndSubsampleDescription);
	   if (!matcher.find()){
		   log.debug("The size indicator -s was not found in the filename, returning 1,1 as size value.");
		   return new Point(1,1);
	   }
	   return new Point(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
   }
   
   /** Get the default priority for this filter. */
   @MetaData
   public int getPriority() {
	  return 0;
   }

   private Filter<DicomImageReader> dicomImageReaderFilter;

   public Filter<DicomImageReader> getDicomImageReaderFilter() {
      return dicomImageReaderFilter;
   }

   /**
    * Set the filter that reads the dicom image reader objects for a given SOP UID
    * @param dicomFilter
    */
   @MetaData(out="${ref:dicomImageReader}")
   public void setDicomImageReaderFilter(Filter<DicomImageReader> dicomImageReaderFilter) {
      this.dicomImageReaderFilter = dicomImageReaderFilter;
   }

}
