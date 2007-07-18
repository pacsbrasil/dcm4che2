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
   var deltaX = (x-this.startX) / this.scale;
   var deltaY = (y-this.startY) / this.scale;
   if( isNaN(deltaX) || isNaN(deltaY) ) {
   	  alert("DeltaX/Y isNaN");
   	  return false;
   }
   this.orx = this.origOrx - deltaX;
   this.ory = this.origOry - deltaY;
   
   if( isNaN(this.orx) || isNaN(this.ory) ) {
   	  error("Origin x,y isNaN");
   	  this.orx = this.orx;
   	  this.ory = this.ory;
   	  return false;
   }
   
   var origin = ""+this.orx+","+this.ory;
   var viewBox = ""+this.orx+" "+this.ory+" "+this.szx+" "+this.szy;
   this.svgNode.setAttribute("coordorigin",origin);
   this.svgNode.setAttribute("viewBox", viewBox);
   this.debug("Setting viewBox="+viewBox);
   this.svgNode.setAttribute("viewBox",viewBox);
   // Never update the image any longer, as it is a direct update of the underlying data.
   return false;
};

/**
 * Starts the window levelling
 */
 function zpStartLeft(x,y) {
 	this.debug("Starting zoom/pan left button drag.");
 	this.url = this.getImageUrl();
 	var origin = (this.svgNode.getAttribute("coordorigin")+"").split(',',2);;
 	var carea = (this.svgNode.getAttribute("coordsize")+"").split(',',2);
 	
 	this.orx = Number(origin[0]);
 	this.ory = Number(origin[1]);
 	this.szx = Number(carea[0]);
 	this.szy = Number(carea[1]);
 	this.scale = Number(this.svgNode.getAttribute("scl"));
 	
 	this.debug("Origin x,y="+this.orx+","+this.ory+" size "+this.szx+","+this.szy +" scaling:"+this.scale);
 	
    this.origOrx = this.orx;
    this.origOry = this.ory;
    this.origSzx = this.szx;
    this.origSzy = this.szy;
    
    this.rows = Number(this.imageNode.getAttribute("Rows"));
    this.columns = Number(this.imageNode.getAttribute("Columns"));
    this.width = getWidthFromNode(this.svgNode);
    this.height = getHeightFromNode(this.svgNode);
    this.debug("rows,columns="+this.rows+","+this.columns);
    this.debug("width,height="+this.width+","+this.height);

    return true;
 };
 
/**
 * The image is never updated, so don't worry about the updated URL.
 */
function zpGetUpdatedUrlQuery(isDone)
{
}

function zpEndAction() {
	var cx = this.orx + this.width / (2*this.scale);
	var cy = this.ory + this.height / (2*this.scale);
    displayXslt.action(this.actionId,"&region="+this.width+","+this.height+","+cx+","+cy+","+this.scale);
};

/**
 * Creates a window level object
 */
function ZoomPan(minValue, maxValue) {
  this.image = new Image();
};

/** Updates the centerX/Y positions in the debug window */
function zpUpdateOtherDisplay(isDone) {
	this.debugValue("Zoom origin:"+this.orx+","+this.ory);
}

var zoomPanHandler;

/** Initialize the window level */
function initZoomPan() {
	ZoomPan.prototype = new ImageEvent();
	ZoomPan.prototype.startLeft = zpStartLeft;
	ZoomPan.prototype.getUpdatedUrlQuery = zpGetUpdatedUrlQuery;
	ZoomPan.prototype.updateOtherDisplay = zpUpdateOtherDisplay;
	ZoomPan.prototype.endAction = zpEndAction;
	ZoomPan.prototype.updateModel = zpUpdateModel;
	ZoomPan.prototype.noImageUpdates = true;
	zoomPanHandler=new ZoomPan();
};

addLoadEvent(initZoomPan);
