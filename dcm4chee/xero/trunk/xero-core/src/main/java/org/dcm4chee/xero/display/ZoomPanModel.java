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

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("ZoomPanModel")
@Scope(ScopeType.CONVERSATION)
public class ZoomPanModel {
	private static final Logger log = LoggerFactory.getLogger(ZoomPanModel.class); 

	/** Current zoom level */
	int izoom = 0;
	
	/** Center X,Y and half height values */
	double centerX=0.5, centerY=0.5, hWidth=0.5, hHeight=0.5;
	String region;

	/** Sets the zoom value, and as a side-affect modifies the region to re-center it with the
	 * given limits.  Cannot be read back out, as this is a relative operation.
	 * @param izoom level, 0 being the whole image, 1 being 90%, 2 being 81% 0.9<sup>izoom</sup>
	 */
	public void setZoom(int izoom) {
		if( izoom<0 || izoom > 250 ) {
			throw new UnsupportedOperationException("Illegal zoom value "+izoom);
		}
		this.izoom = izoom;
		double zoom = 0.5 * Math.pow(0.9, (double) izoom); 
		hWidth = zoom;
		hHeight = zoom;
		fixRegion();
	}
	
	/** Gets the zoom level 
	 * @return zoom level 0 to 250 
	 */
	public int getZoom() {
		return izoom;
	}
	
	/** Sets the region to a specific value */
	public void setRegion(String region) {
		log.info("Setting region to "+region);
		double[] dRegion = splitRegion(region);
		centerX = (dRegion[0]+dRegion[2])/2;
		centerY = (dRegion[1]+dRegion[3])/2;
		hWidth = (dRegion[2]-dRegion[0])/2;
		hHeight = (dRegion[3]-dRegion[1])/2;
		// Regenerate the region string from components.
		this.region = null;
	}
	
	/** Splits region into sub-parts */
	public static double[] splitRegion(String region) {
		double ret[] = new double[4];
		int start = 0;		
		region = region.trim();
		for(int i=0; i<ret.length; i++ ) {
			if( start>=region.length() ) throw new IllegalArgumentException("Too few arguments in "+region);
			int end = region.indexOf(',',start);
			if( end<0 ) end = region.length();
			ret[i] = Double.parseDouble(region.substring(start,end));
			start = end+1;
		}
		if( start<region.length() ) throw new IllegalArgumentException("Too many arguments in "+region);
		return ret;
	}
	
	/** Checks the regions to ensure that the zoom/pan doesn't go too far and that the image is visible */
	void fixRegion() {
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
	public void setPanX(double pixels) {
		centerX = centerX + hWidth * pixels;
		fixRegion();
	}
	
	/**
	 * Pans in the Y direction.  Affects teh region value.
	 * @param pixels amount to zoom by relative to the current displayed size (+1 will zoom an entire page width downwards.)
	 */
	public void setPanY(double pixels) {
		centerY = centerY + hHeight * pixels;
		fixRegion();
	}
	
	/**
	 * Gets the region string for the sub-region of the image to display
	 * @return region left,top,right,bottom string
	 */
	public String getRegion() {
		if( region==null ) {
			region = (centerX-hWidth)+","+(centerY-hHeight)+","+(centerX+hWidth)+","+(centerY+hHeight);
		}
		log.info("Get region "+region);
		return region;
	}
}
