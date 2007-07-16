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
// An empty method to prevent not defined responses.
function imageEventEmpty() { }

/**
 * Create an ImageEvent base object
 */
function ImageEvent() {
  this.body = document.getElementById("body");
}

/**
 * Handles the mouse down event by calling this.start with the x,y coordinates, and then
 * capturing the events if start returns true.
 * @param evt is the mouse down event
 */
ImageEvent.prototype.mouseDown = function (evt) {
  if( this.mousing ) return;
  this.debugMouse("Mouse down.");
  if( evt==null ) evt = window.event;
  if( !isLeftMouse(evt) ) return;
  this.imageNode = target(evt);
  this.svgNode = this.imageNode.parentNode;
  this.debugUrl = document.getElementById("debugUrl");
  this.debugImage = document.getElementById("debugImage");
  if( !this.imageNode ) {
  	error("No image node.");
  	return true;
  }
  if( !this.svgNode ) {
  	error("No SVG node.");
  	return true;
  }
  this.actionId = this.imageNode.id;
  this.startX = evt.clientX;
  this.startY = evt.clientY;
  this.loadingImage = null;
  this.quality = 1.0;
  this.startCount = 0;
  this.totalTime = 0;
  this.avgTime = 2000;
  this.mousing = this.startLeft(evt.clientX, evt.clientY);
  if( this.mousing ) {
  	imageHandler = this;
    if( evt.preventDefault ) {
       evt.preventDefault();
    }
    else if( this.body.setCapture ) {
      this.debug("Set capture mode.");
      this.body.setCapture();
    }
    // Get the initial update loading - it may not be identical to the original image.
    this.updateScreen(false);
  }
  this.debugMouse("mouseDown mousing "+this.mousing);
  return false;
};

/** Empty event - just return false */
ImageEvent.prototype.startLeft = function(x,y) {
	return false;
};


/**
 * Handles the mouse up event capture/ignore non-relevant items.
 * It is possible to get multiple mouse up events because of the mouse being over or not 
 * over the image.
 * Calls updateModel and endAction to finish the last mouse movement and to update the
 * screen by calling the end action.
 */
ImageEvent.prototype.mouseUp = function (evt) {  
  if( !this.mousing ) return;
  if( !isLeftMouse(evt) ) return;
  this.mousing = false;
  this.updateModel(evt.clientX, evt.clientY);
  this.startX = 0;
  this.startY = 0;
  if( this.body && this.body.releaseCapture ) {
    this.body.releaseCapture();
  }
  this.endAction();
  this.debugMouse("mouseUp");
}


/**
 * Handles the move move event by calling updateModel and updateScreen
 */
ImageEvent.prototype.mouseMove = function wlMouseMove(evt) {
  if(this.mousing ) {
    this.debugMouse("Mouse move:"+evt.clientX+","+evt.clientY);
    if( this.updateModel(evt.clientX, evt.clientY) ) {
      this.updateScreen(false);
    }
  }
};


/**
 * Handle mouse-out events by pretending it is a mouse up.
 */
ImageEvent.prototype.mouseOut = function (evt) {
	if(!this.mousing ) return;
	// Would like to check if this is a real mouse out event - but how to do so is unclear.
	this.mouseUp(evt);
	this.debugMouse("mouseOut");
};


/**
 * A base class for image events, where it is expected that the user of this class will
 * be listening to mouse drag events, middle click and mouse wheel events.
 * This class encapsulates the differences between IE and standard browsers such as 
 * Firefox.
 * Also, up to 3 different event handlers can be active provided they don't capture
 * the same events (left mouse, context menu or mouse wheel events).
 */
 
ImageEvent.prototype.updateImage = function (url) 
{
  if( this.imageNode.src ) {
  	this.imageNode.src = url;
  	if( this.debugUrl ) this.debugUrl.innerHTML = "SRC URL:"+encodeURL(url);
  }
  else {
  	this.imageNode.setAttribute("xlink:href",url);
  	if( this.debugUrl ) this.debugUrl.innerHTML = "Xlink URL:"+encodeURL(url);
  }
};

/** Allows changing the debug output to another level or otherwise updating it. */
ImageEvent.prototype.debug = debug;
ImageEvent.prototype.debugMouse = debug;
ImageEvent.prototype.debugValue = info;
ImageEvent.prototype.debugLoad = debug;
ImageEvent.prototype.info = info;

/**
 * Removes the items in rem from the given URL.
 */
ImageEvent.prototype.getStrippedUrl = function (url, rem) {
	if( !url ) {
		alert("No URL found.");
		return undefined;
	}
	var i,j;
    var search;
	if( (typeof rem)==='string') {
        search = '&'+rem+'=';
		i = url.indexOf(search);
		if( i<0 ) return url;
		j = url.indexOf('&',i+2);
		if( j<0 ) j = url.length;
		url = url.substring(0,i) + url.substring(j,url.length);
		return url;
	}
	else if( rem ) {
	   for(i=0; i<rem.length; i++) {
	   	  url = this.getStrippedUrl(url,rem[i]);
	   }
	}
	return url;
};

/**
 * Updates the screen with updated display values from this.getUpdatedUrlQuery(isDone)
 */
ImageEvent.prototype.updateScreen = function (isDone)
{
  if( this.noImageUpdates ) return;
  var url=this.baseUrl + this.getUpdatedUrlQuery(isDone) + "&imageQuality="+this.quality;  
  var isTime = false;
  var currentTime = new Date().getTime();
  var reason = "";
  var delay = currentTime - this.lastTime;
  if( isNaN(delay) ) {
  	this.lastTime = currentTime;
  	delay = 0;
  }
  if( ! this.loadingImage ) {
  	this.loadingImage = new Image();
  	this.loadingImage.src = url;
  	this.debugLoad("Starting to load:"+currentTime);
  	this.debugLoad("Initial URL:"+encodeURL(url));
  }
  var origUrl = this.loadingImage.src;
  if( delay>3500 || delay > 3*this.avgTime) {
  	reason = "Timeout ";
    isTime = true;
  }
  else if( this.loadingImage.complete ) {
  	reason = "Loaded image: ";
    if( this.sinceCompleteTime == -1 ) {
      this.sinceCompleteTime = currentTime;
    }
    else if( this.sinceCompleteTime + 50 < currentTime || delay > 150) {
      isTime = true;
    }
  }
  if( isDone || isTime) {
    this.loadingImage = new Image();
    this.loadingImage.src = url;
    this.startCount = this.startCount + 1;
    this.totalTime = this.totalTime + currentTime - this.lastTime;
    this.lastTime = currentTime;
    this.sinceCompleteTime = -1;
    this.avgTime = this.totalTime/this.startCount;
    this.debugLoad(reason+"C:"+this.startCount+ " A:"+Math.round(this.avgTime) + " Q:"+Math.round(this.quality*100)/100);
    this.updateImage(origUrl);
    if(delay > 200 && this.quality > 0.25 ) this.quality = this.quality * 0.98;
    if( delay > 500 && this.quality > 0.25 ) this.quality = this.quality * 0.9;
    if( delay < 120 && this.quality < 0.90 ) this.quality = this.quality * 1.02;
    if( this.updateOtherDisplay ) {
    	this.updateOtherDisplay(isDone);
    }
  }
}

/** Gets from the given location to the end of the assignment in the string */
function getToAmp(url, index) {
	var end = url.indexOf('&',index);
	if( end<0 ) {
		end = url.length;
	}
	return url.substring(index,end);
}

/** Gets a given url property value, or returns default.
 * @param url is a string URL, after the ? portion
 * @param tag is the tag to look for
 * @param def is the default value to use.
 */
ImageEvent.prototype.getUrlProperty = function(url,tag,def) {
	var tagEq = tag + "=";
	var startInd = url.indexOf(tagEq);
	if( startInd<0 ) return def;
	if( startInd==0 ) {
		return getToAmp(url, startInd+tagEq.length);
	}
	tagEq = "&"+tagEq;
	startInd = url.indexOf(tagEq);
	if( startInd<0 ) return def;
	return getToAmp(url,startInd+tagEq.length);	
};

/** 
 * Gets the stripped image url, and updates the image node
 */
ImageEvent.prototype.getImageUrl = function(rem) {
	if( !this.imageNode) {
		error("No image node found.");
		return undefined;
	}
    var url = this.imageNode.getAttribute("src");
    if( ! url ) url = this.imageNode.getAttribute("xlink:href");
    var origUrl = url;
	if( rem ) {
		url = this.getStrippedUrl(origUrl,rem);
	}
	this.debug("Stripped URL:"+encodeURL(url));
	return url;
};

// Sometimes the page likes seeing this well defined early on - so create them, even though they
// might be invalid. 
var imageEvent = new ImageEvent();
var imageHandler = imageEvent;
