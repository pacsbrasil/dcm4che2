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

import static org.dcm4chee.xero.metadata.filter.FilterUtil.getBoolean;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getFloats;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.MetaData;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scales the image as required. Assumes the image can be read in decimated
 * appropriately.
 * 
 * @author bwallace
 * 
 */
public class ScaleFilter implements Filter<WadoImage> {
   public static final String SVG_TRANSFORM = "svgTransform";

   private static final Logger log = LoggerFactory.getLogger(ScaleFilter.class);

   private static final float[] DEFAULT_REGION = new float[] { 0f, 0f, 1f, 1f };

   public WadoImage filter(FilterItem<WadoImage> filterItem, Map<String, Object> params) {
	  float[] region = getFloats(params, "region", DEFAULT_REGION);
	  int rows = getInt(params, "rows");
	  int cols = getInt(params, "cols");
	  int rot = getInt(params,"rotation");
	  boolean flip = getBoolean(params,"flip");
	  if (rows == 0 && cols == 0 && rot==0 && !flip) {
		 log.debug("Just calling next filter directly as no size, rotation or flip parameters.");
		 return (WadoImage) filterItem.callNextFilter(params);
	  }
	  params.remove(WadoImage.IMG_AS_BYTES_ONLY_FOR_TRANSFER_SYNTAXES);
	  DicomImageReader dir = dicomImageReaderFilter.filter(null, params);
	  if( dir==null ) return null;
	  // Size can't change per-image, so just get the overall sizes.
	  int width, height;
	  try {
		  synchronized (dir) {
			  width = dir.getWidth(0);
			  height = dir.getHeight(0);
		  }

		 if( width<=0 || height<=0 ) {
			log.error("Image width/height is zero.");
			 return (WadoImage) filterItem.callNextFilter(params);
		 }
	  } catch (IOException e) {
		 log.warn("Couldnt't read DICOM image.");
		 return null;
	  }
	  Rectangle regionOnSource = DicomImageFilter.calculateSubregionFromRegionFloatArray(region,width,height);
	  if ( regionOnSource.isEmpty()) {
		  //FIXME: What is the desired behavior?  This bad region will still be passed to the next filter.
		  // Should we remove the region from the parameters, and continue instead of just calling the next filter now?
		  log.error("Source subimage is described with zero size.");
		  return (WadoImage) filterItem.callNextFilter(params);
	  }
	  Dimension desiredSize = new Dimension(cols,rows);
	  Point2D scale = new Point2D.Double(1.0,1.0);
	  if( rows==0 && cols==0 ) {
		  desiredSize = regionOnSource.getSize();
	  } else if (rows == 0) {
		  double aspectHeightToWidth = (double)height / (double)width; 
		  desiredSize.height = (int)Math.round( cols * aspectHeightToWidth );
		  double scaleFactor = calculateScaleDownFactor( regionOnSource.width, desiredSize.width );
		  scale.setLocation(scaleFactor,scaleFactor);
	  } else if (cols == 0) {
		  double aspectWidthToHeight = (double)width / (double)height;
		  desiredSize.width = (int)Math.round( rows * aspectWidthToHeight );
		  double scaleFactor = calculateScaleDownFactor( regionOnSource.height, desiredSize.height );
		  scale.setLocation(scaleFactor,scaleFactor);
	  } else {
		  scale.setLocation( calculateScaleDownFactor( regionOnSource.width, desiredSize.width ),
				             calculateScaleDownFactor( regionOnSource.height, desiredSize.height ) );
	  }
	  // don't scale up.
	  boolean isEqualAspectScale = (Math.abs(( scale.getX() - scale.getY() )* Math.min(regionOnSource.width,regionOnSource.height) ) <= 2 );
	  if ( ( scale.getX() >= 1.0 ) || ( scale.getY() >= 1.0 ) ) {
		  if ( isEqualAspectScale ) {
			  scale.setLocation(1.0, 1.0);
		  }
		  else {
			  double ratioXY = scale.getX() / scale.getY();
			  if ( ratioXY >= 1.0 ) {
				  scale.setLocation(1.0, 1.0/ratioXY);
			  } else {
				  scale.setLocation(ratioXY, 1.0);
			  }
		  }
	  }
	  if ( isEqualAspectScale && ( scale.getX() >= 1.0 ) && ( rot == 0 ) && ( !flip ) ) {
		  log.debug("Just calling next filter - no scaling being applied.");
		  return (WadoImage) filterItem.callNextFilter(params);
	  }
	  
	  Dimension neededSize = new Dimension(
			  (int)(scale.getX()*regionOnSource.width + 0.99), 
			  (int)(scale.getY()*regionOnSource.height + 0.99) );
	  
	  // Remove scaling, rotation, and flip information from the parameters to 
	  // downstream filters.
	  FilterUtil.removeFromQuery(params, "rows", "cols", "rotation", "flip");
	  params.put("rows", neededSize.height);
	  params.put("cols", neededSize.width);
	  String queryStr = (String) params.get(MemoryCacheFilter.KEY_NAME);
	  // Need to include the rows/cols in the new queryStr for caching.
	  queryStr = queryStr + "&rows=" + neededSize.height + "&cols=" + neededSize.width;
	  params.put(MemoryCacheFilter.KEY_NAME, queryStr);
	  log.debug("Calling the next filter with queryStr=" + queryStr);
	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if (wi == null || wi.hasError())
		 return null;
	  log.debug("Got wado image from next filter.");
	  Dimension bufferDimension = wi.getBufferDimension();
	  if ( bufferDimension == null ) {
	  	 log.error("Can't get buffer dimension.  Image errors out.");
	  	 return null;
	  }
	  int nWidth = bufferDimension.width;
	  int nHeight = bufferDimension.height;
	  if ((Math.abs(nWidth - neededSize.width) <= 3) && (Math.abs(nHeight - neededSize.height) <= 3) && rot==0 && !flip) {
		 log.debug("Within 3 pixels of desired size, returning directly.");
		 return wi;
	  }
	  int sourceDiffX = Math.abs(nWidth - regionOnSource.width );
	  int sourceDiffY = Math.abs(nHeight - regionOnSource.height);
	  if ( ( sourceDiffX > 0 ) || (sourceDiffY > 0 ) ) {
		  String message = "";
		  if ( log.isDebugEnabled() ) {
			  message = "Didn't get expected source size " + regionOnSource.width + "x" + 
			  	regionOnSource.height + ", but got source size " + nWidth + "x" + nHeight;
		  }
		  if ( ( sourceDiffX > 1 ) || (sourceDiffY > 1 ) ) {
			  log.debug( message + ".  Recalculating scale factor." );
			  scale.setLocation((double)neededSize.width / (double) nWidth, (double)neededSize.height / (double) nHeight);
		  } else {
			  log.debug( message + ".  Close enough - using previously calculated scale factor.");
		  }
	  } else {
		  log.debug( "Received correct unscaled source size.  Will scale.");
	  }

	  log.debug("Returning a scaled instance of the image sx=" + scale.getX() + " sy=" + scale.getY() );
	  AffineTransform affine = new AffineTransform();
	  
	  BufferedImage biScale = null;
	  String transform = "";
	  if( regionOnSource.x!=0 || regionOnSource.y!=0 ) {
		 transform = " translate("+(-regionOnSource.width)+","+(-regionOnSource.height)+")" + transform;
	  }
	  String scaleInFilename = "";
	  if( scale.getX()!=1 || scale.getY() !=1 ) {
		 transform =" scale("+scale.getX()+","+scale.getY()+") "+transform;
		 scaleInFilename = "-scaletoXY" + neededSize.width + "," + neededSize.height;
	  }
	  String flipInFilename = "";
	  if( flip ) {
		 affine.scale(-1,1);
		 transform = "scale(-1,1) "+transform;
		 flipInFilename = "-flipLR";
	  }
	  //
	  // The AffineTransformOp is going to figure out the bounds of the image
	  // so we don't need to worry about translations.
	  //
	  String rotInFilename = "";
	  if( rot!=0 ) {
		 affine.rotate(rot*Math.PI/180);
		 transform = "rotate("+(360-rot)+") "+transform;
		 rotInFilename = "-rot" + (360-rot);
	  }
	  if( cols!=nWidth || rows!=nHeight ) {
		 affine.scale(scale.getX(), scale.getY());
	  }

	  Rectangle bounds = affine.createTransformedShape(new Rectangle(nWidth, nHeight)).getBounds();
	  if( bounds.getMinX()!=0 || bounds.getMinY()!=0 ) {
		 transform = "translate("+(bounds.getMinX())+","+(bounds.getMinY())+") "+transform;
	  }
	  log.debug("Transform is "+transform);
	  AffineTransform firstAffine = AffineTransform.getTranslateInstance(-bounds.getMinX(), -bounds.getMinY());
	  firstAffine.concatenate(affine);
	  log.debug("Overall affine transform="+firstAffine);
	  
	  AffineTransformOp scaleOp = new AffineTransformOp(firstAffine, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	  BufferedImage bi = wi.getValue();
	  if ( bi == null ) {
		  log.error("BufferedImage is null in WadoImage.  Can't scale image.");
		  return null;
	  }
	  biScale = scaleOp.filter(bi, biScale);
	  WadoImage ret = wi.clone();
	  ret.setValue(biScale);
	  if( !transform.equals("") ) ret.setParameter(SVG_TRANSFORM, transform);
	  String filename = ret.getFilename();
	  	 
	  filename += scaleInFilename + flipInFilename + rotInFilename;
	  ret.setFilename(filename);
	  return ret;
   }
   
   protected static double calculateScaleDownFactor( int startSize, int destinationSize )
   {
	   int subsampleFactor = DicomImageFilter.calculateDesiredSubsamplingFactorForOneDimension( startSize, destinationSize );
	   int subsampledSize = DicomImageFilter.calculateFinalSizeFromSubsampling( startSize, subsampleFactor );
	   if ( subsampledSize == destinationSize ) {
		   return 1.0 / (double)subsampleFactor;
	   }
	   return (double)destinationSize / (double)startSize;
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
