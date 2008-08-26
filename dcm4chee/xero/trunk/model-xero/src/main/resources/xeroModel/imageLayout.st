/**
 * This section defines some special image layout handling
 */
function ImageLayout(id,template) {
	this.baseConstructor(id,template);
};

ImageLayout.prototype = new Layout("baseImage","image/image");
ImageLayout.prototype.baseConstructor = Layout;

// Scale no smaller than fetchSize when fetching,
// and fetch on fetchSize index boundaries 
ImageLayout.prototype.fetchSize = 256;
ImageLayout.prototype.isDebug = false;
ImageLayout.prototype.MAGNIFY = "MAGNIFY";
ImageLayout.prototype.SCALE_TO_FIT = "SCALE_TO_FIT";
ImageLayout.prototype.TRUE_SIZE = "TRUE_SIZE";
ImageLayout.prototype.urlCount = 0;

/** Updates the image position information
 */
ImageLayout.prototype.updatePosition = function ImageLayout_updatePosition() {
	if(!this.merged ) {
		return;
	}
	
	// Need to add layout & query information to make the image fit the displayed area.
	this.layoutType = this.merged.get("size");
	console.info("Found layout type %s", this.layoutType);
	if(!this.layoutType ) this.layoutType = this.SCALE_TO_FIT;
	// Reset the positional information...
	this.scale = 1;
	this.scaleY = 1;
	this.translateX = 0;
	this.translateY = 0;
	this.rotation = undefined;
	this.flip = false;
	
	var tl = this.merged.get("topLeft");
	if( tl ) {
		var br = this.merged.get("bottomRight");
		this.region = parsePoint(tl+","+br);
		console.info("Using specified region %s", this.region);
	} else {
		this.region = [0,0,this.image.Columns, this.image.Rows];
	}
	
	this.debug("Region to display %s in %d,%d", this.region, this.width,this.height);
	if( !this.width ) return;
	
	this.computeSpatialTransform();
	this.computeScale();
	this.computeTranslate();
	this.computeIE();
};

/* Computes the IE specific translation/positioning values */
ImageLayout.prototype.computeIE = function ImageLayout_computeIE() {
	this.dblCols = this.image.Columns*2;
	this.dblRows = this.image.Rows*2; 
	this.dblColsDisp = this.dblCols * this.scale;
	this.dblRowsDisp = this.dblRows * this.scaleY;
	this.translateXIE = this.translateX - this.dblColsDisp/2;
	this.translateYIE = this.translateY - this.dblRowsDisp/2;
	console.info("IE settings: dbl cols/rows %f,%f  dbl cols/rows disp %f,%f translate IE %f,%f",this.dblCols,this.dblRows,
	this.dblColsDisp, this.dblRowsDisp, this.translateXIE, this.translateYIE); 
};

/** Indicate if the image load is complete */
ImageLayout.prototype.isComplete = function ImageLayout_isComplete() {
	if( this.preloadImage===undefined ) return true;
	if( this.preloadImage.complete ) {
		this.debug("Image %s is complete.", this.preloadImage.src);
		this.preloadImage = undefined;
		return true;
	};
	this.debug("Waiting for %s", this.preloadImage.src);
	return false;
};

/** This generates a URL object that includes positioning/offset information 
 * as well as the actual URL string to display the image.
 * This is technically controller code, but it is so intimately connected with this model
 * details that it is better to keep it part of the model.  
 */
ImageLayout.prototype.createFullUrl = function ImageLayout_createFullUrl(base) {
	var ret = { "imageUrl" : base+"&amp;objectUID="+this.image.objectUID, "top":0, "left":0, "imageId":"im"+this.id, "fullImageId": this.urlCount++};
	if( this.image.frame ) {
		ret.imageUrl = ret.imageUrl + "&amp;frameNumber="+this.image.frame;
	}
	var ww = this.merged.get("windowWidth");
	if( ww ) {
		var wc = this.merged.get("windowCenter");
		ret.imageUrl = ret.imageUrl + "&amp;windowWidth="+ww+"&amp;windowCenter="+wc;
	}
	var puid = this.merged.get("gspsUID");
	if( puid ) {
		ret.imageUrl = ret.imageUrl + "&amp;presentationUID="+puid;
	}
	
	var intScale = 1;
	// Only fetch sizes down to 256 -otherwise fetch full size.
	for(ret.fetchScale=1; ret.fetchScale < this.image.Rows/this.fetchSize; ret.fetchScale *=2 ) {
		if( this.scale > intScale ) break;
		intScale = intScale/2;
	}
	
	this.debug("ImageLayout scale %f, fetchScale %f",this.scale, ret.fetchScale);

	var coords = [0,0, 0,this.height, this.width,0, this.width,this.height];
	
	// Figure out the region that needs to be fetched, in image coordinates.
	var xmin = this.image.Columns;
	var ymin = this.image.Rows;
	var xmax = 0;
	var ymax = 0;
	var i,n=coords.length;
	var pnt;
	for(i=0; i<n; i+=2) {
		pnt = this.toImage(coords,i);
		xmin = Math.min(xmin,pnt[0]);
		xmax = Math.max(xmax,pnt[0]);
		ymin = Math.min(ymin,pnt[1]);
		ymax = Math.max(ymax,pnt[1]);
	};
	xmin = Math.max(0,xmin) / ret.fetchScale;
	ymin = Math.max(0,ymin) / ret.fetchScale;
	xmax = Math.min(this.image.Columns,xmax) / ret.fetchScale;
	ymax = Math.min(this.image.Rows,ymax) / ret.fetchScale;
	
	// Now, figure out the size of that region in display coordinates
	xmin = (xmin - (xmin % this.fetchSize))*ret.fetchScale;
	ymin = (ymin - (ymin % this.fetchSize))*ret.fetchScale;
	xmax = (xmax + this.fetchSize - (xmax % this.fetchSize))*ret.fetchScale;
	ymax = (ymax + this.fetchSize - (ymax % this.fetchSize))*ret.fetchScale;
	xmax = Math.min(this.image.Columns,xmax);
	ymax = Math.min(this.image.Rows,ymax);

	ret.cols = xmax-xmin;
	ret.rows = ymax-ymin;
	ret.top = ymin;
	ret.left = xmin;
	if( xmin!==0 || ymin!==0 || xmax!==this.image.Columns || ymax!==this.image.Rows ) {
		console.info("Fetching sub-region %d,%d,%d,%d", xmin,ymin,xmax,ymax);
		ret.imageUrl = ret.imageUrl + "&amp;region="+xmin/this.image.Columns+","+ymin/this.image.Rows+","+xmax/this.image.Columns+","+ymax/this.image.Rows;
	}
	if( ret.fetchScale!=1 ) {
		console.info("Fetching at scale %d",ret.fetchScale);
		ret.imageUrl = ret.imageUrl + "&amp;rows=" + Math.floor((ymax-ymin)/ret.fetchScale);
	}
	return ret;
};
 
/**
 * Compute the translation so that the center of the display area is co-incident with the overall area.
 */
ImageLayout.prototype.computeTranslate = function ImageLayout_computeTranslate() {
	// dispX,Y is the display point we want co-incident with the image point.
	// Remember that the image area is translated first, then scaled, then rotated
	var dispX = this.width/2;
	var dispY = this.height/2;
	this.debug("Half x,y=%f,%f", dispX, dispY);
	var imgTL = this.toDisplay(this.region,0);
	var imgBR = this.toDisplay(this.region,2);
	
	var imgX = (imgBR[0]-imgTL[0])/2+imgTL[0];
	var imgY = (imgBR[1]-imgTL[1])/2+imgTL[1];
	this.translateX = dispX-imgX;
	this.translateY = dispY-imgY;
	if( this.translateX > -1 && this.translateX < 1 ) this.translateX = 0;
	if( this.translateY > -1 && this.translateY < 1 ) this.translateY = 0;
	console.info("ImageLayout translation %d,%d", this.translateX, this.translateY);
};

/** 
 * Computes the spatial transformation.
 */
ImageLayout.prototype.computeSpatialTransform = function ImageLayout_computeSpatialTransform() {
	this.rotation = this.merged.get("rotation");
	var flip = this.merged.get("horizontalFlip");
	if( flip && flip=="TRUE" ) this.flip = true;
	console.info("Flip %s rotation %d", this.flip, this.rotation); 
};

/** Converts a display relative coordinate to an image relative one. */
ImageLayout.prototype.toImage = function ImageLayout_toImage(xy,offset) {
	if(offset===undefined) offset=0;
	var xd = xy[offset];
	var yd = xy[offset+1];
	if( this.translateX ) {
		xd = xd-this.translateX;
	}
	if( this.translateY ) {
		yd = yd-this.translateY;
	}
	if( this.flip )	xd = -xd;
	if( this.rotation ) {
		var xp,yp;
		if( this.rotation==90 ) {
			xp = yd;
			yp = -xd;
		} else if(this.rotation==180) {
			xp = -xd;
			yp = -yd;
		} else if(this.rotation==270) {
			xp = -yd
			yp = xd
		} else {
			var radians = 2*Math.PI-this.rotation*Math.PI/180;
			var cs = Math.cos(radians);
			var sn = Math.sin(radians);
			var xp = xd*cs-yd*sn;
			var yp = yd*cs+xd*sn;
		};
		xd = xp;
		yd = yp;
	};
	
	if( this.scale ) {
		xd = xd / this.scale;
		yd = yd / this.scaleY;
	}
	return [xd,yd];
}; 

/**
 * Computes the display relative coordinate from the image relative pair
 */
ImageLayout.prototype.toDisplay = function ImageLayout_toDisplay(xy,offset) {
	if(offset===undefined) offset=0;
	var xd = xy[offset];
	var yd = xy[offset+1];
	if( this.scale ) {
		xd = xd * this.scale;
		yd = yd * this.scaleY;
	}
	if( this.rotation ) {
		var xp,yp;
		if( this.rotation==90 ) {
			xp = -yd
			yp = xd
		} else if(this.rotation==180) {
			xp = -xd;
			yp = -yd;
		} else if(this.rotation==270) {
			xp = yd;
			yp = -xd;
		} else {
			var radians = this.rotation*Math.PI/180;
			var cs = Math.cos(radians);
			var sn = Math.sin(radians);
			var xp = xd*cs-yd*sn;
			var yp = yd*cs+xd*sn;
		};
		xd = xp;
		yd = yp;
	};
	if( this.flip )	xd = -xd;
	if( this.translateX ) {
		xd = xd+this.translateX;
	}
	if( this.translateY ) {
		yd = yd+this.translateY;
	}
	this.debug("toDisplay %f,%f to %f,%f", xy[offset],xy[offset+1],xd,yd);
	return [xd,yd];
}; 


/** Compute the scale to display the image at so that it all appears (or
 * for SCALE_TO_FIT, MAGNIFY or TRUE_SIZE, whatever scale is appropriate.)
 */
ImageLayout.prototype.computeScale = function ImageLayout_computeScale() {
	this.aspect = this.merged.get("aspect");
	if(!this.aspect) this.aspect = 1;
	if( this.layoutType=="TRUE_SIZE") {
		var spx = parseFloat(this.merged.get("spacingX"));
		var dpi = screen.logicalXDPI;
		if( !dpi ) dpi = screen.deviceXDPI;
		if( !dpi ) {
			dpi = 0.294;
			console.warn("Using %f mm/pixel screen size - may not correspond to true screen size.",dpi);
			console.dir(screen);
		}
		else {
			dpi = 25.4/dpi;
			console.info("Using %f mm/pixel screen size as provided by screen.", dpi);
		}
		this.scale = spx/dpi;
	} else if( this.layoutType=="MAGNIFY" ) {
		this.scale = parseFloat(this.merged.get("magnify"));
	} else {
		console.info("Setting scale to fit type %s", this.layoutType);
		var imgTL = this.toDisplay(this.region,0);
		var imgBR = this.toDisplay(this.region,2);
		console.info("imgTL = %s imgBR= %s region %s", imgTL, imgBR, this.region);
	
		var imageX = Math.abs(imgBR[0]-imgTL[0]);
		var imageY = Math.abs(imgBR[1]-imgTL[1]);

		var scaleX = this.width/imageX;
		var scaleY = this.height/(imageY*this.aspect);
		console.info("scale %f,%f", scaleX,scaleY);
		if( scaleX > scaleY ) scaleX = scaleY;
		if( scaleX > 1 ) scaleX = 1;
		this.scale = scaleX;
	}
	this.widthImg = this.width / this.scale;
	this.heightImg = this.height / (this.scale * this.aspect);
	this.scaleY = this.scale * this.aspect;
	console.info("Computed scale %f,%f w,h=%f,%f",this.scale,this.scaleY, this.widthImg, this.heightImg);
	return this.scale;	
};