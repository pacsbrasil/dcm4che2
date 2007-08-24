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

/**
 * Handles window levelling in the display area, with a server round-trip used
 * on every image display (instead of an SVG image display).
 */

/**
 * Updates the window level according to the new client X,Y positions
 */
function wlUpdateModel(x,y) {
   var deltaX = (x-this.startX)/512.0;
   var deltaY = (y-this.startY)/512.0;
   this.windowWidth = deltaX*this.range + this.startWidth;
   this.windowCenter = deltaY*this.range + this.startCenter;
   if( this.windowWidth <=0.001 ) this.windowWidth = 0.001;
   if( this.windowWidth >= this.range ) this.windowWidth = this.range;
   if( this.windowCenter <= this.minValue ) this.windowCenter = this.minValue;
   if( this.windowCenter >= this.maxValue ) this.windowCenter = this.maxValue;
   if( isNaN(this.windowWidth) || isNaN(this.windowCenter) ) {
      alert("Resetting window width and center due to isNan");
      this.windowWidth = this.originalWidth;
      this.windowCenter = this.originalCenter;
   }
   return true;
}

/**
 * Starts the window levelling
 */
 function wlStartLeft(x,y) {
  this.baseUrl = this.getImageUrl(['windowWidth', 'windowCenter', 'imageQuality']);
  
  this.minValue = this.imageNode.getAttribute("minPixel");
  this.maxValue = this.imageNode.getAttribute("maxPixel");
  this.debug("Min,max pixel="+this.minValue+","+this.maxValue);
  
  if( (!this.minValue) || (!this.maxValue) ) {
  	this.info("No pixel range - can't window level.");
  	return false;
  }  
  this.minValue = Number(this.minValue);
  this.maxValue = Number(this.maxValue);
  this.range = this.maxValue - this.minValue;
  if( this.imageNode.getAttribute("windowWidth") ) {
	 this.windowWidth = Number(this.imageNode.getAttribute("windowWidth"));
	 this.windowCenter = Number(this.imageNode.getAttribute("windowCenter"));
     this.debug("Starting window level: w,c="+this.windowWidth+","+this.windowCenter);
  }
  else {
	 this.windowWidth = this.range;
	 this.windowCenter = this.minValue + this.range/2;
     this.info("Using min/max values to generate window level: w,c="+this.windowWidth+","+this.windowCenter+" range="+this.range);
  }
  
  this.originalWidth = this.windowWidth;
  this.originalCenter = this.windowCenter;
  
  this.startWidth = this.windowWidth;
  this.startCenter = this.windowCenter;
  this.sinceCompleteTime = -1;
  this.quality = 0.8;
  return true;
 }

/**
 * Updates the window level value on the screen - that is, changes the source URL or otherwise
 * re-loads the image.
 */
function wlGetUpdatedUrlQuery(isDone)
{
  return "&windowCenter="+this.windowCenter+"&windowWidth="+this.windowWidth;
};

/**
 * Creates a window level object
 */
function WindowLevelBase() {
  this.image = new Image();
};

function wlEndAction() {
  displayXslt.action(this.actionId,"windowWidth="+this.windowWidth+"&windowCenter="+this.windowCenter);
}

/** Updates the width/center information.  Called when an actual update occurs.
 */
function wlUpdateOtherDisplay(isDone) {
	this.debugValue("WL W:"+Math.round(this.windowWidth)+" C:"+Math.round(this.windowCenter));
};

var windowLevelHandler = new ImageEvent();

/** Initialize the window level */
function initWindowLevel() {
	WindowLevelBase.prototype = new ImageEvent();
	WindowLevelBase.prototype.updateModel = wlUpdateModel;
	WindowLevelBase.prototype.getUpdatedUrlQuery = wlGetUpdatedUrlQuery;
	WindowLevelBase.prototype.updateOtherDisplay = wlUpdateOtherDisplay;
	WindowLevelBase.prototype.startLeft = wlStartLeft;
	WindowLevelBase.prototype.endAction = wlEndAction;
	
	var t = displayXslt.itemsToUpdate;
	t.windowLevel=["image","imageToolbar"];
	
    windowLevelHandler = new WindowLevelBase();
};

addLoadEvent(initWindowLevel);
 