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

import org.dcm4chee.xero.search.study.RegionMacro;
import org.dcm4chee.xero.search.study.PresentationSizeMode;
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
	private static final double ZOOM_EXPONENT = 1.25;
	private static final double LOG_ZOOM_EXPONENT = Math.log(ZOOM_EXPONENT);

	public static final Logger log = LoggerFactory.getLogger(ZoomPanAction.class); 

	@In(value="LocalStudyModel", create=true)
	LocalStudyModel localStudyModel;
	
	@In(value="DisplayMode", create=true)
	DisplayMode mode;

	/** Amount to change zoom by */
	int relZoom = 0;
	int panX = 0, panY = 0;
	
	/** The presentation mode */
	private PresentationSizeMode presentationSizeMode;
	
	private String region;
	/** These are the internal display size information from the client, retrieved from region. */
	protected int width, height, cx, cy;
	/** Scale is a factor to convert image pixels into display pixels, converts image pixels to display pixels. */
	float magnify, updatedMagnify;

	public void setRelZoom(int rzoom) {
		this.relZoom = rzoom;
		log.info("Relative zoomPan set to "+getRelZoom());
	}
	
	public int getRelZoom() {
		return this.relZoom;
	}
	
	/**
	 * Computes the updated manification amount.
	 */
	protected float updateMagnify() {
		if( getRelZoom()==0 ) {
			log.info("Relative zoom is zero, so using original magnify.");
			return magnify;
		}
		if( magnify < 0.0001 ) {
			log.error("Display scale must be larger than 0.0001.");
			magnify = 1.0f;
		}
		double zoom = Math.log( magnify ) / LOG_ZOOM_EXPONENT;
		int izoom = getRelZoom() + (int) Math.round(zoom);
		log.info("Updated izoom is "+izoom);
		if( izoom < -250 ) izoom = -250;
		else if( izoom >= 250 ) izoom = 250;		
		updatedMagnify = (float) Math.pow(ZOOM_EXPONENT,izoom);
		log.info("zoomPan getZoom scale="+magnify+" dbl zoom="+zoom+" updatedMagnify="+updatedMagnify);
		return updatedMagnify;
	}
		
	/** Sets the region to a specific value */
	public void setRegion(String region) {
		if( region==null ) throw new IllegalArgumentException("Region must not be null.");
		this.region = region.trim();
		log.info("Setting region to "+region + " on "+this);
		double[] dRegion = WadoImage.splitDouble(this.region,5);
		width = (int) dRegion[0];
		height = (int) dRegion[1];
		cx = (int) dRegion[2];
		cy = (int) dRegion[3];
		magnify = (float) dRegion[4];
		updatedMagnify = magnify;
		if( magnify < 0.0001 ) {
			log.error("Mangification provided is too small.");
			magnify = 1.0f;
		}
	}
	
	/**
	 * Returns the top left corner of the area to display.
	 * @return top left corner as a comma-separated value.
	 */
	public String getTopLeft() {
	  int left = (int) (cx - width / (2 * updatedMagnify));
	  int top = (int) (cy - height / (2 * updatedMagnify));
	  return Integer.toString(left)+","+Integer.toString(top);
	}
	
	/**
	 * Returns the bottom right corner of the area to display.
	 * @return bottom right corner as a comma-separated value.
	 */
	public String getBottomRight() {
		int right = (int) (cx + width / (2 * updatedMagnify));
		int bottom = (int) (cy + height / (2 * updatedMagnify));
		return Integer.toString(right)+","+Integer.toString(bottom);
	}
	
	/**
	 * Pans in the X direction.  Affects the region value.
	 * @param pixels amount to zoom by relative to the current displayed size (+1 will zoom an entire page width to the right)
	 */
	public void setPanX(int pixels) {
		this.panX = pixels;
	}
	
	/**
	 * Pans in the Y direction.  Affects teh region value.
	 * @param pixels amount to zoom by relative to the current displayed size (+1 will zoom an entire page width downwards.)
	 */
	public void setPanY(int pixels) {
		this.panY = pixels;
	}
	
	/**
	 * Gets the region string for the sub-region of the image to display
	 * @return region width,height,cx,cy,scale
	 */
	public String getRegion() {
		return region;
	}
	
	/** Applies the pan/zoom to the given study, series or image. This maybe used by GSPS or as a temporary change.*/
	public String action() {
	    updateMagnify();
		log.info("Zoom pan action size="+getPresentationSizeMode()+" Region="+getRegion()+" relZoom="+relZoom+" pan X,y="+panX+","+panY + " on "+this);
		
	    DisplayMode.ApplyLevel applyLevel;
	    if( mode!=null ) applyLevel = mode.getApplyLevel();
	    else applyLevel = DisplayMode.ApplyLevel.SERIES;
	    RegionMacro macro = new RegionMacro(getPresentationSizeMode(), getTopLeft(), getBottomRight(), updatedMagnify);
	    localStudyModel.apply(applyLevel,macro);
	    log.info("Done zoom/pan action.");
		return "success";
	}

	public PresentationSizeMode getPresentationSizeMode() {
		return presentationSizeMode;
	}

	public void setPresentationSizeMode(PresentationSizeMode presentationSizeMode) {
		this.presentationSizeMode = presentationSizeMode;
	}
}
