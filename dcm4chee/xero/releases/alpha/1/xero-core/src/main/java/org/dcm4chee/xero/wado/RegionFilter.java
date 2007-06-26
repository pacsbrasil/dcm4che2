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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.dcm4chee.xero.display.ZoomPanAction;
import org.dcm4chee.xero.metadata.filter.Filter;
import org.dcm4chee.xero.metadata.filter.FilterItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A region filter supports getting a sub-region of a larger image.
 * There are 2 primary cases for how this is used:
 * Getting multiple sub-regions of an image, one at a time is expensive or not.
 * It tends to be inexpensive for Wavelet or JPEG2000 encoded files, while it is expensive
 * for WADO remote retrievals or for JPEG, raw etc encodinges for local files.
 * Regardless, it will be much slower reading the data than using an in-memory copy. 
 * Thus, the following strategy should be used:
 * <ol>
 * <li> Try to get some normalized positions at a given zoom level from cache only.  Use those to reconstruct
 * the specific sub-region. 
 * In the future, this could be extended to lookiing for higher resolution versions of the same data.
 * </li>
 * <li>If the sub-regions are not all available, then figure out a region to request.  Get it once,
 * and then make multiple requests for sub-regions from a custom secondary filter on the far side
 * of the primary filter. Supply that value as an argument, and cause that to not be otherwise cached.
 * </li>
 * <li>Request all the required sub-regions to fill the overall sub-region list needed.</li>
 * </ol> 
 *  
 * @author bwallace
 *
 */
public class RegionFilter implements Filter<WadoImage> {
	public static final Logger log = LoggerFactory.getLogger(RegionFilter.class);

	public WadoImage filter(FilterItem filterItem, Map<String, Object> params) {
		String region = (String) WadoImage.removeFromQuery(params,"region")[0];
		log.info("Region filter on region "+region);
		if( region==null ) return (WadoImage) filterItem.callNextFilter(params);
		double[] dregion = ZoomPanAction.splitRegion(region);
		if( dregion[0]==0.0 && dregion[1]==0.0 && dregion[2] == 1.0 && dregion[3]==1.0 )
			return (WadoImage) filterItem.callNextFilter(params);
		log.info("Non-default region supplied:"+region);
		Object[] values = WadoImage.removeFromQuery(params,"rows", "columns");
		String rows = (String) values[0];
		String columns = (String) values[1];
		WadoImage wiFull = (WadoImage) filterItem.callNextFilter(params);
		if( wiFull==null ) return null;
		BufferedImage biFull = wiFull.getValue();
		int width = biFull.getWidth();
		int height = biFull.getHeight();
		int x = (int) (dregion[0] * width);
		int y = (int) (dregion[1] * height);
		int w = (int) ((dregion[2]-dregion[0])*width);
		int h = (int) ((dregion[3]-dregion[1])*height);
		log.info("Region: x,y,w,h is "+x+","+y+","+w+","+h);
		BufferedImage biRegion = biFull.getSubimage(x, y, w, h);
		WadoImage ret = new WadoImage(wiFull);
		ret.setValue(biRegion);
		// Determine if we need to scale, and the width/height if we do.
		int iRows, iCols;
		if( rows!=null && columns!=null ) {
			iRows = Integer.parseInt(rows);
			iCols = Integer.parseInt(columns);
			if( iRows < 1 || iCols < 1 ) throw new IllegalArgumentException("Rows and columns must be positive integers:"+rows+","+columns);
			if( iRows >= w && iCols >= h && Math.abs(iRows/iCols-h/w)< 0.01 ) return ret;			
		}
		else if( rows!=null ) {
			iRows = Integer.parseInt(rows);
			iCols = (int) ((iRows * (long) w)/h);
			if( iRows >= h ) {
				return ret;
			}
		}
		else if( columns!=null ){
			iCols = Integer.parseInt(columns);
			iRows = (int) ((iCols * (long) h)/w);
			if( iCols >= w ) return ret;
		}
		else {
			return ret;
		}
		log.info("Scaling to "+iCols+","+iRows + " with scale "+(iCols/(double) w)+","+(iRows/(double) h));
		// We couldn't just use the sub-region, we also need to scale, so do so.
		AffineTransform at = AffineTransform.getScaleInstance(iCols/(double) w, iRows/(double) h);
		AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		BufferedImage biScale = new BufferedImage(iCols, iRows, biRegion.getType());
		ato.filter(biRegion,biScale);
		log.info("Size of final image is "+biScale.getWidth()+","+biScale.getHeight()+" and is of type "+biScale.getType());
		log.info("Bits before scaling:"+WLFilter.getBitsPerPixel(biRegion)+" bits after "+WLFilter.getBitsPerPixel(biScale));
		ret.setValue(biScale);
		return ret;
	}
	
	

}
