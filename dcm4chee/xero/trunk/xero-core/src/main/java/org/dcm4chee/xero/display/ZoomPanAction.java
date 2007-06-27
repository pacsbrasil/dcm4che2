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
package org.dcm4chee.xero.display;

import org.dcm4chee.xero.search.study.DicomObjectType;
import org.dcm4chee.xero.search.study.ImageBean;
import org.dcm4chee.xero.search.study.ImageType;
import org.dcm4chee.xero.search.study.SeriesBean;
import org.dcm4chee.xero.wado.WadoImage;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("ZoomPan")
@Scope(ScopeType.EVENT)
public class ZoomPanAction {
	private static final double ZOOM_EXPONENT = 0.9;
	private static final double LOG_ZOOM_EXPONENT = Math.log(ZOOM_EXPONENT);

	public static final Logger log = LoggerFactory.getLogger(ZoomPanAction.class); 

	@In(value="LocalStudyModel", create=true)
	LocalStudyModel localStudyModel;
	
	@In(value="DisplayMode", create=true)
	DisplayMode mode;

	/** Amount to change zoom by */
	int relZoom = 0;
	float panX = 0, panY = 0;
	
	/** Center X,Y and half height values */
	double centerX=0.5, centerY=0.5, hWidth=0.5, hHeight=0.5;
	String region;

	public void setRelZoom(int rzoom) {
		this.relZoom = rzoom;
		log.info("Relative zoomPan set to "+rzoom+" region="+getRegion());
	}
	
	public int getRelZoom() {
		return this.relZoom;
	}
	
	/** Sets the size of the window to be that specified by the given zoom level.
	 */
	protected void setZoom(int izoom) {
		if( izoom<0 || izoom > 250 ) {
			throw new UnsupportedOperationException("Illegal zoom value "+izoom);
		}
		double zoom = 0.5 * Math.pow(ZOOM_EXPONENT, (double) izoom); 
		hWidth = zoom;
		hHeight = zoom;
		fixRegion();
		log.info("Zoom set to "+izoom);
	}
	
	/**
	 * Gets the zoom level, as computed from the region.
	 */
	protected int getZoom() {
		if( hHeight < 0.0001 ) throw new IllegalArgumentException("Region height must be larger than 0.0001.");
		double zoom = Math.log( hHeight * 2 ) / LOG_ZOOM_EXPONENT;
		log.info("zoomPan getZoom hHeight="+hHeight+" dbl zoom="+zoom);
		int izoom = (int) Math.round(zoom);
		if( izoom < 0 ) return 0;
		if( izoom >= 250 ) return 250;
		return izoom;
	}
		
	/** Sets the region to a specific value */
	public void setRegion(String region) {
		if( region==null ) throw new IllegalArgumentException("Region must not be null.");
		region = region.trim();
		log.info("Setting region to "+region + " on "+this);
		double[] dRegion = WadoImage.splitRegion(region);
		centerX = (dRegion[0]+dRegion[2])/2;
		centerY = (dRegion[1]+dRegion[3])/2;
		hWidth = (dRegion[2]-dRegion[0])/2;
		hHeight = (dRegion[3]-dRegion[1])/2;
		// Regenerate the region string from components.
		this.region = null;
		log.info("Region after set is "+getRegion());
	}
	
	/** Checks the regions to ensure that the zoom/pan doesn't go too far and that the image is visible */
	protected void fixRegion() {
		if( centerX < hHeight ) centerX = hHeight;
		else if( 1-centerX < hHeight ) centerX = 1-hHeight;
		if( centerY < hWidth ) centerY = hWidth;
		else if( 1-centerY < hWidth ) centerY = 1-hWidth;
		region = null;
	} 
	
	/**
	 * Pans in the X direction.  Affects the region value.
	 * @param pixels amount to zoom by relative to the current displayed size (+1 will zoom an entire page width to the right)
	 */
	public void setPanX(float pixels) {
		this.panX = pixels;
	}
	
	/**
	 * Pans in the Y direction.  Affects teh region value.
	 * @param pixels amount to zoom by relative to the current displayed size (+1 will zoom an entire page width downwards.)
	 */
	public void setPanY(float pixels) {
		this.panY = pixels;
	}
	
	/**
	 * Gets the region string for the sub-region of the image to display
	 * @return region left,top,right,bottom string
	 */
	public String getRegion() {
		if( region==null ) {
			region = (centerX-hWidth)+","+(centerY-hHeight)+","+(centerX+hWidth)+","+(centerY+hHeight);
		}
		return region;
	}
	
	/** Applies the pan/zoom to the given study, series or image. */
	public String action() {
		log.info("Region="+getRegion()+" relZoom="+relZoom+" pan X,y="+panX+","+panY + " on "+this);
		if( relZoom!=0 ) {
			int izoom = getZoom();
			log.info("Computed zoom is "+izoom);
			setZoom(izoom + relZoom);
		}
		if( panX!=0.0f || panY!=0.0 ) {
			centerX = centerX + hWidth * panX;
			centerY = centerY + hHeight * panY;
			fixRegion();
		}
		if( hWidth <= 0.0 || hHeight <= 0.0 ) return "failure";
		
	    DisplayMode.ApplyLevel applyLevel;
	    if( mode!=null ) applyLevel = mode.getApplyLevel();
	    else applyLevel = DisplayMode.ApplyLevel.SERIES;
		if( applyLevel == DisplayMode.ApplyLevel.IMAGE ) {
			ImageBean image = localStudyModel.getImage();
			image.setRegion(getRegion());
		}
		else {
			SeriesBean series = localStudyModel.getSeries();
			series.setRegion(getRegion());
			for(DicomObjectType dot : series.getDicomObject()) {
				if( dot instanceof ImageType ) {
					ImageType image = (ImageType) dot;
					image.setRegion(null);
				}
			}
		}

		return "success";
	}
}
