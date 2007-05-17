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
 * Updates the window level according to the new client X,Y positions
 */
function zpUpdateModel(x,y) {
   var deltaX = this.hWidth * (x-this.startX)/256.0;
   var deltaY = this.hHeight * (y-this.startY)/256.0;
   if( isNaN(deltaX) || isNaN(deltaY) ) {
   	  alert("DeltaX/Y isNaN");
   	  return;
   }
   this.centerX = this.origCenterX - deltaX;
   this.centerY = this.origCenterY - deltaY;
   if( isNaN(this.centerX) || isNaN(this.centerY) ) {
   	  alert("centerX/Y isNaN");
   	  this.centerX = this.origCenterX;
   	  this.centerY = this.origCenterY;
   	  return;
   }
   if( this.centerX < this.hWidth ) this.centerX = this.hWidth;
   else if( (1-this.centerX) < this.hWidth ) this.centerX = 1-this.hWidth;
   if( this.centerY < this.hHeight ) this.centerY = this.hHeight;
   else if( (1-this.centerY) < this.hHeight ) this.centerY = 1-this.hHeight;
   var rLeft = this.centerX - this.hWidth;
   var rRight = this.centerX + this.hWidth;
   var rTop = this.centerY - this.hHeight;
   var rBottom = this.centerY + this.hHeight;
   if( isNaN(rLeft) || isNaN(rRight) || isNaN(rTop) || isNaN(rBottom) ) {
   	 alert("isNan rLeft, rTop, rRight or rBottom:"+rLeft+","+rRight+","+rTop+","+rBottom);
   	 return false;
   }
   this.region = "&region="+rLeft+","+rTop+","+rRight+","+rBottom;
   return true;
};

/**
 * Starts the window levelling
 */
 function zpStartLeft(x,y) {
 	this.url = this.getImageUrl();
    this.region = this.getUrlProperty(this.url,'region', '0.0,0.0,1.0,1.0');
    var sRegion = this.region.split(",",4);
    this.region = "&region="+this.region;
    var rLeft = Number(sRegion[0]);
    var rTop = Number(sRegion[1]);
    var rRight = Number(sRegion[2]);
    var rBottom = Number(sRegion[3]);
    this.centerX = (rLeft + rRight)/2;
    this.centerY = (rTop + rBottom)/2;
    this.hWidth = (rRight - rLeft)/2;
    this.hHeight = (rBottom - rTop)/2;
    this.origCenterX = this.centerX;
    this.origCenterY = this.centerY;
    this.origHWidth = this.hWidth;
    this.origHHeight = this.hHeight;
    
    this.baseUrl = this.getImageUrl(['region', 'imageQuality']);

    // Todo move this into ImageEvent generally...
    this.lastTime = new Date().getTime();
    this.sinceCompleteTime = -1;
    this.loadingImage = this.imageNode;
    this.quality = 0.8;
    
    return true;
 };

/**
 * Updates the window level value on the screen - that is, changes the source URL or otherwise
 * re-loads the image.
 */
function zpGetUpdatedUrlQuery(isDone)
{
  return this.region;
}

function zpEndAction() {
    displayXslt.action("zoomPan",this.region);
};

/**
 * Creates a window level object
 */
function ZoomPan(minValue, maxValue) {
  this.image = new Image();
  this.left = 0.0;
  this.right = 1.0;
  this.top = 0.0;
  this.bottom = 1.0;
  this.zoom = 1.0;
};

/** Updates the centerX/Y positions in the debug window */
function zpUpdateOtherDisplay(isDone) {
	if( this.debug ) {
		this.debug.innerHTML = ""+Math.round(1000*this.centerX) + ","+Math.round(1000*this.centerY);
	}
}

/** Initialize the window level */
function initZoomPan() {
	ZoomPan.prototype = new ImageEvent();
	ZoomPan.prototype.startLeft = zpStartLeft;
	ZoomPan.prototype.getUpdatedUrlQuery = zpGetUpdatedUrlQuery;
	ZoomPan.prototype.updateOtherDisplay = zpUpdateOtherDisplay;
	ZoomPan.prototype.endAction = zpEndAction;
	ZoomPan.prototype.updateModel = zpUpdateModel;
	var t = displayXslt.itemsToUpdate;
	t.zoomPlus=["image","zoomPanToolbar"];
	t.zoomMinus = t.zoomPlus;
	t.zoomLeft = t.zoomPlus;
	t.zoomRight = t.zoomPlus;
	t.zoomUp = t.zoomPlus;
	t.zoomDown = t.zoomPlus;
	t.zoomPan=t.zoomPlus;
	
	if( displayMode==="zoomPan") {
		imageHandler=new ZoomPan();
	}
};

addLoadEvent(initZoomPan);
