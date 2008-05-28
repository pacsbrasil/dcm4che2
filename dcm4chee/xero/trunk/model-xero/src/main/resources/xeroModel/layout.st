/**
 * Handles a layout object 
 */
function Layout(id, format) {
	if( !format ) format = "html/layout";
	if( !id ) id = Layout.prototype.idCnt++;
	this.id = id;
	this.format = format;
	if( this.isDebug ) this.debug = console.debug;
};

Layout.prototype.isDebug = false;
Layout.prototype.debug = function() {};
// Some things don't quite get setup right without an initial size, even though it is wrong.
Layout.prototype.idCnt = 0;
Layout.prototype.setGrid = function Layout_setGrid(x,y) {
	this.gridX = x;
	this.gridY = y;
	if(! this.layouts) this.layouts = new Array();
};

Layout.prototype.used = true;

/**
 * Resizes the objects to display in the correct area
 */
Layout.prototype.resize = function Layout_resize(width,height,style) {
	this.debug("resize %s to %d,%d",this.id,width,height);
	var changed = this.resizeListener && ((this.width!=Math.floor(width)) || (this.height!=Math.floor(height)));
	this.width = Math.floor(width);
	this.height = Math.floor(height);
	this.style = style;
	if( changed ) {
		this.resizeListener();
	}
	if( ! this.gridX ) return;
	if( ! this.gridY ) return;
	
	var usedLay = this.getUsedLayouts();
	var absWidth = this.sumAbsWidth(usedLay);
	var absHeight = this.sumAbsHeight(usedLay);
	var sumRelWidth = this.sumRelWidth(usedLay);
	var sumRelHeight = this.sumRelHeight(usedLay);
	this.debug("sum rel x,y=%f,%f on %s,%d,%d", sumRelWidth, sumRelHeight, this.format,this.gridX, this.gridY);
	width = width-absWidth;
	height = height-absHeight;
	
	var i,j;
	var subWidth, subHeight;
	var layout;
	for(i=0; i<this.gridX; i++) {
		for(j=0; j<this.gridY; j++) {
			if( i+j*this.gridX>=usedLay.length ) break;
			layout = usedLay[i+j*this.gridX];
			subWidth = layout.absWidth;
			if(!subWidth ) {
				subWidth = layout.relWidth;
				if( !subWidth ) subWidth = 100;
				subWidth = subWidth*width/sumRelWidth;
				if( sumRelWidth===0 ) {
					console.warn("Setting sub-width to infinity!");
				}
			}
			
			subHeight = layout.absHeight;
			if(! subHeight ) {
				subHeight = layout.relHeight;
				if( !subHeight ) subHeight = 100;
				subHeight = subHeight * height/sumRelHeight;
			}
			// Would really prefer to be able to use percentage widths, but they aren't consistent
			// enough to not get extremely different sizes in various browsers.
			//style = "width:"+Math.floor(subWidth*100/this.width)+"%; height:"+Math.floor(subHeight*100/this.height)+"%;";
			style = "width:"+Math.floor(subWidth)+"px; height:"+Math.floor(subHeight)+"px;";
			layout.resize(subWidth, subHeight, style);
		};
	};
};

/** Checks to see if the layout information is complete */
Layout.prototype.isComplete = function Layout_isComplete() {
	if( !this.layouts ) return true;
	var lay,i,n=this.layouts.length;
	for(i=0;i<n;i++){
		lay = this.layouts[i];
		if( !lay.used ) continue;
		if( !lay.isComplete() ) return false;
	};
	return true;
};

/** Clears an relayout flags that have been set for this object and all child objects */
Layout.prototype.clearRelayout = function Layout_clearRelayout() {
	this.relayout = false;
	if(! this.layouts ) return;
	var i,n=this.layouts.length;
	for(i=0; i<n; i++) {
		this.layouts[i].clearRelayout();
	}
};

/** Return only layouts that are being used - allows them to disappear or otherwise
 * not be shown.
 */
Layout.prototype.getUsedLayouts = function Layout_getUsedLayouts() {
	var i,n=this.layouts.length;
	var ret = new Array();
	for(i=0; i<n; i++) {
		if( this.layouts[i].used ) ret.push(this.layouts[i]);
	}
	return ret;
};

/** Finds the given layout object.  Can be passed a layout object directly or the id of one. */
Layout.prototype.findLayout = function findLayout(id) {
	if( typeof(id)!=="string" ) return id;
	if( this.id===id ) return this;
	if( !this.layouts ) return;
	var ret,i,n=this.layouts.length;
	for(i=0; i<n;i++) {
		ret = this.layouts[i].findLayout(id);
		if( ret ) return ret;
	}
}; 

/** Sum all of the widths of the absolute width items */
Layout.prototype.sumAbsWidth = function Layout_sumAbsWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.absWidth ) ret = ret+layout.absWidth;
	}
	return ret;
};
Layout.prototype.sumAbsHeight = function Layout_sumAbsHeight(usedLay) {
	var ret = 0;
	var layout,y;
	for(i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.absHeight ) ret = ret+layout.absHeight;
	}
	return ret;
};

/** Sum the relative width information, using 100 for any missing item values */
Layout.prototype.sumRelWidth = function Layout_sumRelWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.relWidth ) {
			ret = ret+layout.relWidth;
		} else if( ! layout.absWidth ) {
			ret = ret+100;
		}
	};
	return ret;
};
Layout.prototype.sumRelHeight = function Layout_sumRelHeight(usedLay) {
	var ret = 0;
	var layout,i;
	for(i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.relHeight ) {
			ret = ret+layout.relHeight;
		} else if( !layout.absHeight ) {
			ret = ret+100;
		}
	};
	return ret;
};

/** Add another child object */
Layout.prototype.add = function Layout_add(child) {
	this.layouts.push(child);
};

/**
 * This section defines some special image layout handling
 */
function ImageLayout(id,format) {
	if( format ) this.format = format;
	this.id = id;
	if( this.isDebug ) {
		this.debug = console.debug;
	}
};
ImageLayout.prototype = new Layout("baseImage","image/image");
// Scale no smaller than fetchSize when fetching,
// and fetch on fetchSize index boundaries 
ImageLayout.prototype.fetchSize = 256;
ImageLayout.prototype.isDebug = false;

/** Updates the image position information
 */
ImageLayout.prototype.updatePosition = function ImageLayout_updatePosition() {
	if(!this.image ) {
		return;
	}
	
	// Need to add layout & query information to make the image fit the displayed area.
	this.layoutType = "SCALE_TO_FIT";
	// Reset the positional information...
	this.scale = 1;
	this.translateX = 0;
	this.translateY = 0;
	this.rotate = 0;
	this.region = [0,0,this.image.Columns, this.image.Rows];
	this.debug("Region to display %s in %d,%d", this.region, this.width,this.height);
	if( !this.width ) return;
	
	this.computeScale();
	this.computeTranslate();
};

/** Indicate if the image load is complete */
ImageLayout.prototype.isComplete = function ImageLayout_isComplete() {
	if( !this.preloadImage ) return true;
	if( this.preloadImage.complete ) {
		this.preloadImage = undefined;
		return true;
	};
	return false;
};

/** This generates a URL object that includes positioning/offset information 
 * as well as the actual URL string to display the image.
 * This is technically controller code, but it is so intimately connected with this model
 * details that it is better to keep it part of the model.  
 */
ImageLayout.prototype.createFullUrl = function ImageLayout_computeSize(base) {
	var ret = { "imageUrl" : base+"&amp;objectUID="+this.image.SOPInstanceUID, "top":0, "left":0 };

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
	if( xmin!==0 && ymin!==0 && xmax!==this.image.Columns && ymax!==this.image.Rows ) {
		this.debug("Fetching sub-region %d,%d,%d,%d", xmin,ymin,xmax,ymax);
		ret.imageUrl = ret.imageUrl + "&amp;region="+xmin/this.image.Columns+","+ymin/this.image.Rows+","+xmax/this.image.Columns+","+ymax/this.image.Rows;
		ret.top = ymin;
		ret.left = xmin;
	}
	if( ret.fetchScale!=1 ) {
		this.debug("Fetching at scale %d",ret.fetchScale);
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
	// Needed for IE only to get the right position.
	this.translateXDisp = -this.translateX / this.scale;
	this.translateYDisp = -this.translateY / this.scale;
	this.debug("ImageLayout translation %d,%d", this.translateX, this.translateY);
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
	if( this.scale ) {
		xd = xd / this.scale;
		yd = yd / this.scale;
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
		yd = yd * this.scale;
	}
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
 * for ZOOM/True Size, whatever scale is appropriate.)
 */
ImageLayout.prototype.computeScale = function ImageLayout_computeScale() {
	var imgTL = this.toDisplay(this.region,0);
	var imgBR = this.toDisplay(this.region,2);
	
	var imageX = imgBR[0]-imgTL[0];
	var imageY = imgBR[1]-imgTL[1];

	var scaleX = this.width/imageX;
	var scaleY = this.height/imageY;
	if( scaleX > scaleY ) scaleX = scaleY;
	if( scaleX > 1 ) scaleX = 1;
	this.scale = scaleX;
	this.widthImg = this.width / this.scale;
	this.heightImg = this.height / this.scale;
	
	this.debug("Computed scale %f",scaleX);
	return this.scale;
};