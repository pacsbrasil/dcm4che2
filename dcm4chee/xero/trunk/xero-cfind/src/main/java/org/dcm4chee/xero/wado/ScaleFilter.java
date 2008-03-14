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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.dcm4chee.xero.metadata.filter.MemoryCacheFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.dcm4chee.xero.metadata.filter.MemoryCacheFilter.removeFromQuery;

import static org.dcm4chee.xero.metadata.filter.FilterUtil.getFloats;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getInt;
import static org.dcm4chee.xero.metadata.filter.FilterUtil.getBoolean;

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
	  DicomImageReader dir = DicomFilter.filterDicomImageReader(filterItem, params, null);
	  if( dir==null ) return null;
	  // Size can't change per-image, so just get the overall sizes.
	  int width, height;
	  try {
		 // If no image has been read yet, the size will be zero, so read the metadata to get this.
		 dir.getStreamMetadata();
		 width = dir.getWidth(0);
		 height = dir.getHeight(0);
		 if( width<=0 || height<=0 ) {
			log.error("Image width/height is zero.");
			 return (WadoImage) filterItem.callNextFilter(params);
		 }
	  } catch (IOException e) {
		 log.warn("Couldnt't read DICOM image.");
		 return null;
	  }
	  if( rows==0 && cols==0 ) {
		 cols = (int) (width * (region[2] - region[0]));
		 rows = (int) (height * (region[3]-region[1]));
	  }
	  else if (rows == 0)
		 rows = (cols * height) / width;
	  else if (cols == 0)
		 cols = (rows * width) / height;
	  int fRows = (int) (rows / (region[3] - region[1]));
	  int fCols = (int) (cols / (region[2] - region[0]));

	  float scaleX = width / (float) fCols;
	  float scaleY = height / (float) fRows;
	  if ((scaleX <= 1 && scaleY <= 1 && rot==0 && !flip) || scaleX<=0 || scaleY<=0) {
		 log.debug("Just calling next filter - no scaling being applied.");
		 return (WadoImage) filterItem.callNextFilter(params);
	  }
	  // Don't request anything bigger than the current image size.
	  if( scaleX<1 ) scaleX = 1;
	  if( scaleY<1 ) scaleY = 1;
	  int nCols = (int) ((width / (int) scaleX) * (region[2] - region[0]));
	  int nRows = (int) ((height / (int) scaleY) * (region[3] - region[1]));
	  removeFromQuery(params, "rows", "cols", "rotation", "flip");
	  params.put("rows", nRows);
	  params.put("cols", nCols);
	  String queryStr = (String) params.get(MemoryCacheFilter.KEY_NAME);
	  // Need to include the rows/cols in the new queryStr for caching.
	  queryStr = queryStr + "&rows=" + nRows + "&cols=" + nCols;
	  params.put(MemoryCacheFilter.KEY_NAME, queryStr);
	  log.debug("Calling the next filter with queryStr=" + queryStr);
	  WadoImage wi = (WadoImage) filterItem.callNextFilter(params);
	  if (wi == null)
		 return null;
	  log.debug("Got wado image from next filter.");
	  BufferedImage bi = wi.getValue();
	  int nWidth = bi.getWidth();
	  int nHeight = bi.getHeight();
	  if ((Math.abs(nWidth - cols) <= 3) && (Math.abs(nHeight - rows) <= 3) && rot==0 && !flip) {
		 log.debug("Within 3 pixels of desired size, returning directly.");
		 return wi;
	  }

	  log.debug("Returning a scaled instance of the image sx=" + (cols / (double) nWidth) + " sy=" + (rows / (double) nHeight));
	  AffineTransform affine = new AffineTransform();
	  
	  BufferedImage biScale = null;
	  String transform = "";
	  if( region[0]!=0 || region[1]!=0 ) {
		 transform = " translate("+(-region[0]*width)+","+(-region[1]*height)+")" + transform;
	  }
	  if( scaleX!=1 || scaleY!=1 ) {
		 transform =" scale("+(1/scaleX)+","+(1/scaleY)+") "+transform;
	  }
	  if( flip ) {
		 affine.scale(-1,1);
		 transform = "scale(-1,1) "+transform;
	  }
	  if( rot!=0 ) {
		 affine.rotate(rot*Math.PI/180);
		 transform = "rotate("+(360-rot)+") "+transform;
	  }
	  if( cols!=nWidth || rows!=nHeight ) {
		 affine.scale(cols / (double) nWidth, rows / (double) nHeight);
	  }

	  Rectangle bounds = affine.createTransformedShape(new Rectangle(nWidth, nHeight)).getBounds();
	  if( bounds.getMinX()!=0 || bounds.getMinY()!=0 ) {
		 transform = "translate("+(bounds.getMinX())+","+(bounds.getMinY())+") "+transform;
	  }
	  log.debug("Transform is "+transform);
	  AffineTransform firstAffine = AffineTransform.getTranslateInstance(-bounds.getMinX(), -bounds.getMinY());
	  firstAffine.concatenate(affine);
	  log.debug("Overall affine transform="+firstAffine);
	  
	  AffineTransformOp scale = new AffineTransformOp(firstAffine, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	  biScale = scale.filter(bi, biScale);
	  WadoImage ret = wi.clone();
	  ret.setValue(biScale);
	  if( !transform.equals("") ) ret.setParameter(SVG_TRANSFORM, transform);
	  return ret;
   }

}
