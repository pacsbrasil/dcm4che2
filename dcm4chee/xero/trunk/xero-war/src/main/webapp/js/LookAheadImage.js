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
 * Portions created by the Initial Developer are Copyright (C) 2008
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
  * ImageRef is an image reference/loading class.
  */
function ImageRef(lookAhead, imageXml) {
	this.position = parseInt(imageXml.getAttribute("Position"));
	debug("Creating an image ref for position="+this.position);
	this.objectUID = imageXml.getAttribute("SOPInstanceUID");
	this.frame = imageXml.getAttribute("FrameNumber");
	this.lookAhead = lookAhead;
	this.updateSrc(lookAhead);
	var usethis = this;
	this.imageOnload = function() {
		debug("Delivering image.");
		usethis.deliver(this.src);
		this.onload = null;
		this.src = null;
	};
};
ImageRef.prototype.src = null;
ImageRef.prototype.fetchView = null;
ImageRef.prototype.delivered = false;

/** Updates the src attribute of this object, based on any changed parameters. 
 * There are a few parameters that are handled specially - window level in specific that
 * can be added postfacto
 */
function ImageRef_updateSrc(lookAhead) {
	var src = lookAhead.src;
	if( src===null || src===undefined ) {
		return;
	};
	var i=src.indexOf("?");
	var n=src.length;
	var eql, amp,key,val, srcVal;
	while(i<n) {
		eql = src.indexOf("=",i+1);
		amp = src.indexOf("&",i+1);
		if( amp===-1 ) amp = n;
		if( eql===-1 ) eql = amp;
		key = src.substring(i,eql);
		val = this[key];
		if( val!==null && val!==undefined ) {
			srcVal = src.substring(eql+1,amp);
			if( val!==null && val!==undefined && srcVal != val ) {
				src = src.substring(0,eql)+"="+val + src.substring(amp,n);
				amp = src.indexOf("&",i+1);
				n = src.length;
				if( amp<0 ) amp = n;
			};
		};
		i = amp+1;
	};
	this.src = src;
};
ImageRef.prototype.updateSrc=ImageRef_updateSrc;

/** Returns the fetch view, that is the image object used to load an image.  Maybe null if no
 * image is being fetched.
 */
function ImageRef_getFetchView() {
	return this.fetchView;	
};
ImageRef.prototype.getFetchView = ImageRef_getFetchView;

/** Sets the src in the image view.  Does not require the URL, only the image */
function ImageRef_setSrc(image) {
	if( image===undefined || image===null ) return;
	info("setSrc src="+this.src);
	setImageSrc(image,this.src);
	info("Done setting the source on "+this.position);
	if( this.fetchView!==null && this.fetchView.complete ) {
		info("Clearing fetch on "+this.position +" because image is loaded.");
		this.clearFetch();
	} else {
		info("Not clearing the fetch "+this.fetchView);
	}
};
ImageRef.prototype.setSrc=ImageRef_setSrc;

/** Sets the window level for the given image.
 */
function ImageRef_setWindowLevel(center,width) {
	info("Setting the window level to "+center+","+width);
	this.windowCenter = center;
	this.windowWidth = width;
	if( this.src===null ) {
		warn("src should already be set in ImageRef before a new window level is set.");
	};
	var amp;
	if( this.src.indexOf("windowCenter")==-1 ) {
		// Put it in the middle because that is what the XSLT does - yeah, I know that is ugly but we want the exact URL.
		amp = this.src.indexOf("&",this.src.indexOf("objectUID")+10);
		if( amp==-1 ) amp = this.src.length;		
		this.src = this.src.substring(0,amp) + "&windowCenter="+center+"&windowWidth="+width + this.src.substring(amp);
	}
	this.updateSrc(this);
};
ImageRef.prototype.setWindowLevel=ImageRef_setWindowLevel;

/**
 * Clears any pending fetches.
 */
function ImageRef_clearFetch() {
	var fetchView = this.fetchView;
	if( fetchView!==null ) {		
		this.fetchView = null;
		fetchView.onload = null;
		fetchView.src = null;
	};
};
ImageRef.prototype.clearFetch=ImageRef_clearFetch;

/**
 * Starts a fetch operation.  Causes the image to be loaded, and when loaded, updates the view image
 * if this image is in view.
 */
function ImageRef_fetch() {
	info("Starting to fetch "+this.src+" p="+this.position);
	this.clearFetch();
	var fetchView;
	fetchView = new Image();
	fetchView.onload = this.imageOnload;
	this.fetchView = fetchView;
	fetchView.src = this.src;
};
ImageRef.prototype.fetch=ImageRef_fetch;

/** Deliver the image src to this object.  Sets delivered to true and updates the underlying view. */
function ImageRef_deliver(src) {
	info("Being delivered image "+src);
	if( src!=this.src ) return;
	this.delivered = true;
	var view = this.lookAhead.getViewAtPosition(this.position);
	if( view!=null ) {
		this.setSrc(view,src);
	};
};
ImageRef.prototype.deliver=ImageRef_deliver;


/** Return true if the image has been loaded. */
function ImageRef_isLoaded() {
	return this.delivered;
};
ImageRef.prototype.isLoaded=ImageRef_isLoaded;

/** Return true if the image has been loaded. */
function ImageRef_isFetching() {
	return this.fetchView!==null;
};
ImageRef.prototype.isFetching=ImageRef_isFetching;

 /**
  * LookAhead is a class that handles loading images in the background, usually with one or more
  * parameters changed.  It can send notification when the image loading is done, and can update an SVG, VML
  * or HTML image object on completion.  
  */
function LookAheadImage() {
	this.view = new Array();
};
LookAheadImage.prototype.viewCount = 0;
LookAheadImage.prototype.viewPosition = 0;
LookAheadImage.prototype.imageCount = 0;
LookAheadImage.prototype.src=null;
LookAheadImage.prototype.fetchSize=64;
LookAheadImage.prototype.fetchStart=0;
LookAheadImage.prototype.fetchEnd=-1;

/**
 * Given a VML or SVG image element, initializes the look ahead image object with all the views
 * found in the given area, and with the correct series UID.  
 */
function LookAheadImage_init(xmlEl) {
	this.tagName = xmlEl.getTagName();
	// Figure out the URL to use for fetching meta-data about the images.
	var src = getImageSrc(xmlEl);
	var objIndex = src.indexOf("&objectUID");
	// TODO - consider whether this is fixed, or exactly how this might vary.  It might not even belong
	// at the look ahead image level...
	this.imgUrl = src.substring(0,objIndex);
	debug("Found src='",this.imgUrl,"'");
	
	// TODO - figure out if there are any series merges etc to be aware of and incorporate.
	this.seriesUID = getUrlAttribute(src,"seriesUID");
	this.dataUrl = getUrlModule(src) + "/image/image.xml?seriesUID=" + this.seriesUID;
	debug("Found dataUrl=",this.dataUrl);
	
	// Figure out all the image viewports, and their relative offsets to the current display position.
	// The current heuristic for this is to find all elements of the same type as this object, with an offset attribute
	// and the src seriesUID being the same as this series UID.  
	var imgs = xmlEl.getOwnerDocument().getElementsByTagName(this.tagName);
	var n = imgs.getLength();
	var img, tstSrc, tstUid, offset;
	info("Found ",n," image nodes of type ",this.tagName," to search for offset and series UID=",this.seriesUID);
	for(var i=0; i<n; i++ ) {
		img = imgs.item(i);
		tstSrc = getImageSrc(img);
		tstUid = getUrlAttribute(tstSrc,"seriesUID");
		if( ! tstUid.equals(this.seriesUID) ) {
			debug("Skipping node because UID isn't right, uid='",tstUid,"' looking for '",this.seriesUID,"'");
			continue;
		}
		offset = img.getAttribute("offset");
		if( offset===null || offset===undefined ) continue;
		offset = parseInt(offset);
		this.setView(offset,img);		
	}
	
	// Fetch the first set of objects
	this.fetch();
};
LookAheadImage.prototype.init = LookAheadImage_init;

/** 
 * Cause image meta-data to be fetched at the next position.
 */
 function LookAheadImage_fetch() {
 	if( this.fetchEnd>0 && this.fetchStart >= this.fetchEnd ) return;
 	var req = new XMLHttpRequest();
 	var usethis = this;
 	var url = this.dataUrl + "&Position="+this.fetchStart + "&Count="+this.fetchSize;
 	info("Fetching ",url);
 	req.onreadystatechange = function() {
 		info("LookAheadImage.fetch received ",req.url);
 		if( req.readyState!=4 ) return;
 		if( req.status!=200 ) {
 			error("Couldn't receive image meta-data for url="+url);
 			// TODO Handle this better - retries etc, ideally automatically with new login etc.
 			return;
 		}
 		info("Calling read image xml on "+url);
 		// May need to parse this for IE
 		usethis.readImageXml(req.responseXML);
 		info("Read image xml, updating counts.");
 		usethis.fetchEnd = usethis.getImageCount();
 		usethis.fetchStart = usethis.fetchStart + usethis.fetchSize;
 		usethis.fetch();
 		info("Done delivering image meta-data for ",url);
 	};
 	req.open("GET", url, true);
 	info("request send.");
 	req.send("");
 	info("Done fetch initialize.");
 };
 LookAheadImage.prototype.fetch=LookAheadImage_fetch;

/**
 * Parses the XML, setting information such as the fetch URL etc.
 */
function LookAheadImage_readImageXml(xml) {
	info("Reading xml into look ahead image.");
	if( xml===null || xml===undefined ) throw new Error("XML provided to LookAheadImage.readImageXml is null/undefined:",xml);
	var results = xml.getDocumentElement();
	var series = results.getElementsByTagName("series").item(0);
	if( this.images===undefined ) {
		this.imageCount = parseInt(series.getAttribute("Viewable"));
		this.images = new Array();
	};
	info("Reading image data...");
	var images = series.getElementsByTagName("image");
	var n = images.getLength();
	var i, image;
	for(i=0; i<n; i++) {
		image = images.item(i);
		this.images[image.getAttribute("Position")] = new ImageRef(this,image);
	};
};
LookAheadImage.prototype.readImageXml=LookAheadImage_readImageXml;

function LookAheadImage_setViewPosition(posn) {
	posn = parseInt(posn);
	this.viewPosition = posn;
	for(var i in this.view ) {
		var vw = this.view[i];
		var ip = 0+parseInt(i)+posn;
		if( vw===null || vw===undefined ) continue;
		var ir = this.getImageRef(ip);
		if( ir===undefined || ir===null ) {
			info("Setting source to null as no ir is defined at "+ip);
			this.setSrc(vw,null);
		}
		else {
			info("Setting src on view.");
			ir.setSrc(vw);
		}
	};
};
LookAheadImage.prototype.setViewPosition=LookAheadImage_setViewPosition;

/** Deals with setting the src attribute in various types of image objects */
function setImageSrc(img,url) {
  if( img.src!==undefined ) {
  	img.src = url;
  }
  else {
  	img.setAttribute("xlink:href",url);
  }
};
LookAheadImage.prototype.setSrc = setImageSrc;


/**
 * Gets the image src attribute, or xlink attribute as appropriate 
 */
function getImageSrc(img) {
	if( img.src!==undefined ) {
		debug("Returning image source ",img.src);
		return img.src;
	}
	else {
		debug("Returning attribute xlink:href=",img.getAttribute("xlink:href"));
		return img.getAttribute("xlink:href");
	}
};

/** Sets a view for a given object.  Also grabs the source attribute from the object. */
function LookAheadImage_setView(index,view) {
	this.view[index] = view;
	this.viewCount = this.viewCount+1;
	info("Setting the src for the look ahead to "+view.src);
	this.src = view.src;
};
LookAheadImage.prototype.setView=LookAheadImage_setView;

/** Gets the view associated with a given IMAGE position, not a view position */
function LookAheadImage_getViewAtPosition(p) {
	p = p-this.viewPosition;
	if( p<0 ) return null;
	if( p>= this.viewCount ) return null;
	info("Getting view index "+p+" value is "+this.view[p]);
	return this.view[p];
};
LookAheadImage.prototype.getViewAtPosition = LookAheadImage_getViewAtPosition;

/** Returns the number of images in the entire series */
function LookAheadImage_getImageCount() {
	return this.imageCount;
};
LookAheadImage.prototype.getImageCount=LookAheadImage_getImageCount;

/** Gets the image reference at the given position. */
function LookAheadImage_getImageRef(i) {
	if( i<0 || i >= this.imageCount ) return undefined;
	var ret = this.images[i];
	if( ret===undefined ) return null;
	return ret;
};
LookAheadImage.prototype.getImageRef=LookAheadImage_getImageRef;
