/**
 * Handles the window levelling.
 */
function WLController() {
};
WLController.prototype = new MouseHandler();
WLController.prototype.isWLController = true;

XeroController.prototype.WindowLevel_clickImage = new WLController();
XeroController.prototype.ctrl_clickImage = XeroController.prototype.WindowLevel_clickImage;

WLController.prototype.prepare = function(e) {
	this.prepareBase();
	this.image = this.getImage();
	if( !this.image ) {
		console.warn("No image found, not window levelling.");
		return false;
	}
	this.layout = this.findLayout();
	if( !this.layout ) {
		console.warn("No layout found, not window levelling.");
		return false;
	}
	this.baseUrl = this.getImageUrl(["rows", "columns", "windowWidth", "windowCenter","imageQuality"]);
	console.info("Window levelling layout %s url %s",this.layout.id, this.baseUrl);

	this.minPixel = this.layout.merged.get("minPixel");
	if( this.minPixel==undefined ) {
		var pos = this.layout.navigate.external.Position;
		var query = this.layout.navigate.parent.generateQuery(parseInt(pos));
		var squery;
		for(var i in query) squery = i;
		var mods = query[squery];
		query[squery] = null;
		
		var count = squery.indexOf("&Count");
		squery = squery.substring(0,count)+"&Count=1&Position="+pos+"&pixelInfo=true";
		console.warn("Need to compute min/max pixel value for window levelling using query",squery,"at position",pos);
		query = new Object();
		query[squery] = mods;
		this.controller.model.navigate.external.addQueries(query,true);
	};

	return true;
};

/** 
 * On the start, find the related image object (should be the start target),
 * and grab the source URL and figure out the range of the window levelling and the 
 * initial window level.
 */
WLController.prototype.start = function WLC_start(e) {
	console.info("Starting wl controller.");
	// TODO - add a server call to fetch windowing information if not already available
	this.minPixel = this.layout.merged.get("minPixel");
	if( this.minPixel==null ) {
		console.info("Waiting for min/max pixel info.");
		return false;
	}
	this.origCenter = this.layout.merged.get("windowCenter");
	this.origWidth = this.layout.merged.get("windowWidth");
	this.maxPixel = this.layout.merged.get("maxPixel");
	this.minPixel = Number(this.minPixel);
	this.maxPixel = Number(this.maxPixel);
	this.pixelRange = this.maxPixel - this.minPixel;
	if( this.origCenter==undefined ) {
		this.origCenter = (this.minPixel + this.maxPixel)/2;
		this.origWidth = this.pixelRange;
	} else {
		this.origCenter = Number(this.origCenter);
		this.origWidth = Number(this.origWidth);
	};
	this.center = this.origCenter;
	this.width = this.origWidth;
	console.info("Initial c/w=",this.origCenter,",",this.origWidth,"min/max=", this.minPixel, this.maxPixel);
	return true;
}; 

/** Update the displayed window level, at least if the image isn't currently fetching */
WLController.prototype.mousemove = function WLC_mousemove(event) {
	var posn = docCoords(event);
	var dx = posn[0]-this.startPosn[0];
	var dy = posn[1]-this.startPosn[1];
	var center = Math.floor(this.origCenter - (dx*this.pixelRange/512));
	var width = Math.floor(this.origWidth - (dy*this.pixelRange/512));
	if( center >= this.minPixel && center < this.maxPixel ) {
		this.center = center;
	}
	if( width>10 && width < 2*this.pixelRange ) {
		this.width = width;
	}
	//console.info("dx,dy=%d,%d c/w=%d,%d", dx,dy,center,width);
	this.updateImage();
};

WLController.prototype.qualityOpts = "&rows=256";

/** This causes the image to be updated if it is complete, otherwise it just continues */
WLController.prototype.updateImage = function WLC_updateImage() {
	if( !this.loadImage ) {
		if( this.width == this.loadWidth && this.center==this.loadCenter ) return;
		this.loadWidth = this.width;
		this.loadCenter = this.center;
		this.tries = 0;
		var url = this.baseUrl + this.qualityOpts + "&windowWidth="+this.width+"&windowCenter="+this.center;
		//console.info("Loading %s", url);
		this.loadImage = new Image();
		this.loadImage.src = url;
	}

	if( this.loadImage.complete || this.tries > 10 ) {
		this.setImageUrl(this.loadImage.src);
		this.loadImage = null;
	} else {
		this.tries++;
	}
};

/** Update the window level in the merged context and re-display the screen once the new image is fetched */
WLController.prototype.end = function WLC_end(event) {
	if( this.loadWidth && (this.loadWidth!=this.origWidth || this.loadCenter!=this.origCenter) ) {
		console.debug("Setting WL c/w=%d,%d", this.loadCenter, this.loadWidth);
		this.layout.navigate.parent.setMerged("windowCenter", this.loadCenter);
		this.layout.navigate.parent.setMerged("windowWidth", this.loadWidth);
	}

	// Clear the values.
	this.loadImage = null;
	this.image = undefined;
	this.layout = undefined;
	this.baseUrl = undefined;
	return true;
};

/** Sets the default mode to window levelling */
XeroController.prototype.imageMouseHandler = new WLController();

