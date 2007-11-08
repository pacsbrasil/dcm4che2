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

import org.dcm4chee.xero.metadata.filter.FilterUtil;
import org.dcm4chee.xero.search.macro.RegionMacro;
import org.dcm4chee.xero.search.study.PresentationSizeMode;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("ZoomPan")
@Scope(ScopeType.EVENT)
public class ZoomPanAction {
   private static final Logger log = LoggerFactory.getLogger(ZoomPanAction.class);

   @In(value = "SessionStudyModel", create = true)
   StudyModel studyModel;

   @In(value = "DisplayMode", create = true)
   DisplayMode mode;

   /** Amount to change zoom by */
   float relZoom;

   int panX = 0, panY = 0;

   /** The overall image size, and the area being displayed */
   String topLeft, bottomRight;

   int rows = 512, columns = 512;

   /** The presentation mode */
   private PresentationSizeMode presentationSizeMode;

   /**
     * These are the internal display size information from the client,
     * retrieved from region.
     */
   protected int width, height, cx, cy;

   /**
     * Scale is a factor to convert image pixels into display pixels, converts
     * image pixels to display pixels.
     */
   float magnify=1f;

   public void setRelZoom(float rzoom) {
	  this.relZoom = rzoom;
	  log.info("Relative zoomPan set to " + getRelZoom());
   }

   public float getRelZoom() {
	  return this.relZoom;
   }

   /** Updates the width, height, cx, cy and computes the magnify amount */
   void updateRegion() {
	  float[] topLeftF = FilterUtil.splitFloat(getTopLeft(), 2);
	  float[] botRightF = FilterUtil.splitFloat(getBottomRight(), 2);
	  width = (int) Math.abs(botRightF[0] - topLeftF[0]);
	  height = (int) Math.abs(botRightF[1] - topLeftF[1]);
	  cx = panX + (int) Math.abs(botRightF[0] + topLeftF[0]) / 2;
	  cy = panY + (int) Math.abs(botRightF[1] + topLeftF[1]) / 2;
	  // Trim to some minimum values
	  if (width < 16)
		 width = 16;
	  if (height < 16)
		 height = 16;
	  if (cx < 1)
		 cx = 1;
	  if (cy < 1)
		 cy = 1;
	  if (cx > columns)
		 cx = columns;
	  if (cy > rows)
		 cy = rows;
	  magnify = columns / (float) width;
	  if (magnify < 0.0001f)
		 magnify = 0.0001f;
	  if( relZoom ==0 ) relZoom = magnify;
	  topLeft = null;
	  bottomRight =null;
   }

   /**
     * Returns the top left corner of the area to display.
     * 
     * @return top left corner as a comma-separated value.
     */
   public String getTopLeft() {
	  if( topLeft!=null ) return topLeft;
	  validateRegion();
	  int left = (int) (cx - columns/ (2 * relZoom));
	  int top = (int) (cy - rows / (2 * relZoom));
	  return Integer.toString(left) + "," + Integer.toString(top);
   }

   /** Checks to see that the various values are reasonable and valid posisble values */
   void validateRegion() {
	  // Guess 512x512 if nothing provided
	  if( rows < 16 ) rows = 512;
	  if( columns < 16 ) columns = 512;
	  if( cx < 1 ) cx = 1;
	  if( cy < 1 ) cy = 1;
	  if( cx > columns) cx = columns;
	  if( cy > rows ) cy = rows;
	  if( relZoom<0.0001f ) {
		 if( magnify<0.0001f ) magnify = 0.0001f;
		 relZoom = magnify;
	  }
   }
   /**
     * Returns the bottom right corner of the area to display.
     * 
     * @return bottom right corner as a comma-separated value.
     */
   public String getBottomRight() {
	  if (bottomRight != null)
		 return bottomRight;
	  validateRegion();
	  int right = (int) (cx + columns / (2 * relZoom));
	  int bottom = (int) (cy + rows / (2 * relZoom));
	  bottomRight = Integer.toString(right) + "," + Integer.toString(bottom);
	  return bottomRight;
   }

   /**
     * Pans in the X direction. Affects the region value.
     * 
     * @param pixels
     *            amount to zoom by relative to the current displayed size (+1
     *            will zoom an entire page width to the right)
     */
   public void setPanX(int pixels) {
	  this.panX = pixels;
   }

   /**
     * Pans in the Y direction. Affects teh region value.
     * 
     * @param pixels
     *            amount to zoom by relative to the current displayed size (+1
     *            will zoom an entire page width downwards.)
     */
   public void setPanY(int pixels) {
	  this.panY = pixels;
   }

   /**
     * Applies the pan/zoom to the given study, series or image. This maybe used
     * by GSPS or as a temporary change.
     */
   public String action() {
	  String startTL = getTopLeft();
	  String startBR = getBottomRight();
	  updateRegion();
	  log.info("Zoom pan action size=" + getPresentationSizeMode() + " cols,rows="+columns+","+rows+" tl,br=" + getTopLeft()
			+","+getBottomRight()+ " relZoom=" + relZoom + " pan X,y="
			+ panX + "," + panY + " on " + this + " original tl,br="+startTL+","+startBR);

	  DisplayMode.ApplyLevel applyLevel;
	  if (mode != null)
		 applyLevel = mode.getApplyLevel();
	  else
		 applyLevel = DisplayMode.ApplyLevel.SERIES;
	  RegionMacro macro = new RegionMacro(getPresentationSizeMode(), getTopLeft(), getBottomRight(), 1.0f/relZoom);
	  studyModel.apply(applyLevel, macro);
	  log.info("Done zoom/pan action.");
	  return "success";
   }

   public PresentationSizeMode getPresentationSizeMode() {
	  return presentationSizeMode;
   }

   public void setPresentationSizeMode(PresentationSizeMode presentationSizeMode) {
	  this.presentationSizeMode = presentationSizeMode;
   }

   public int getColumns() {
	  return columns;
   }

   public void setColumns(int columns) {
	  this.columns = columns;
   }

   public int getRows() {
	  return rows;
   }

   public void setRows(int rows) {
	  this.rows = rows;
   }

   public void setBottomRight(String bottomRight) {
	  this.bottomRight = bottomRight;
   }

   public void setTopLeft(String topLeft) {
	  this.topLeft = topLeft;
   }
}
