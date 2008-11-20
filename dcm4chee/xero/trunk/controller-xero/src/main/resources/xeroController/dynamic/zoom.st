/** Handles the panning/zooming controls */

XeroController.prototype.TrueSize_clickImage = function (lay,e) {
};

/**
 * Sets the zoom mode, and sets the region back to the original region if the zoom mode was
 * already scale to fit.
 */
XeroController.prototype.ZoomReset_clickImage = function (lay,e) {
	this.zoomMode(lay,e,"SCALE_TO_FIT");
};
XeroController.prototype.Zoom_clickImage = XeroController.prototype.ZoomReset_clickImage;

XeroController.prototype.Magnify_clickImage = function(lay,e) {
	this.zoomMode(lay,e,"MAGNIFY");
};

XeroController.prototype.TrueSize_clickImage = function(lay,e) {
	this.zoomMode(lay,e,"TRUE_SIZE");
};

/** Handles setting a given zoom mode, and re-setting the region */
XeroController.prototype.zoomMode = function(lay,e,mode) {
	var nav = this.getNavigate(lay);
	nav.setMerged("smallImage",true);
	console.info("Resetting region on",lay.id);
	// Reset the topLeft/bottomRight instead of setting the mode
	nav.parent.setMerged("topLeft");
	nav.parent.setMerged("bottomRight");
	nav.parent.setMerged("magnify");
	
	this.waitUpdate("zoomMode",5);
};

/** 
 * Handles zooming in/out.  Delta is a +/- integer increment.  For Magnify mode, it means
 * change by that many powers of two, ignored for true size (which can't change magnification),
 * and scale to fit it changes the magnification by 10%
 */
XeroController.prototype.zoomChange = function(lay,e,delta) {
	var nav = this.getNavigate(lay);
	nav.setMerged("smallImage",true);
	var size = nav.merged.get("size");
	if( size==="TRUE_SIZE" ) {
		console.warn("Can't change magnification in true size mode.");
		return;
	}
	if( size==="MAGNIFY" ) {
		var mag = nav.merged.get("magnify");
		if( !mag ) mag = 1.0;
		while(delta>0 && mag < 16) {
			mag = mag * 2;
			delta--;
		}
		var rows = nav.external.Rows;
		while(delta<0 && rows*mag>64) {
			mag = mag/2;
			delta++;
		}
		nav.parent.setMerge("magnify",mag);
	}
	else {
		// Make sure we have an image level instance.
		if( !nav.external.Rows ) nav = nav.getChild(0);
		var tl = nav.merged.get("topLeft");
		var br = nav.merged.get("bottomRight");
		var rows = nav.external.Rows;
		var cols = nav.external.Columns;
		if(!tl) tl = [0,0];
		if(!br) br = [cols,rows];
		var dx = br[0]-tl[0];
		var dy = br[1]-tl[1];
		dx = Math.floor(dx*0.05*delta);
		dy = Math.floor(dy*0.05*delta);
		if( (br[0]-tl[0]-2*dx<16) || (br[1]-tl[1]-2*dy<16) ) {
			console.warn("Too small a zoom.");
			return;
		}
		if( (br[0]-tl[0]-2*dx>cols*16) || (br[1]-tl[1]-2*dy>rows*16) ) {
			console.warn("Too large a zoom."); 
			return;
		}
		tl[0] = tl[0]+dx;
		tl[1] = tl[1]+dy;
		br[0] = br[0]-dx;
		br[1] = br[1]-dy;
		nav.parent.merged.topLeft = tl;
		nav.parent.setMerged("bottomRight", br,true);
	}
	this.waitUpdate("zoomMode",5);
};

XeroController.prototype.Zoom_wheelSeries = function(id,e) {
	var lay = this.model.layout.findLayout(id);
	
	var wheelData = e.detail ? (-e.detail) : e.wheelDelta/40;
	if(! e.shiftKey ) wheelData = wheelData/3;
	else wheelData = wheelData*3;
	console.info("Wheeling zoom",wheelData);
	
	this.zoomChange(lay,e,wheelData);
};
XeroController.prototype.Zoom_shift_wheelSeries = XeroController.prototype.Zoom_wheelSeries;
XeroController.prototype.ZoomIn_wheelSeries = XeroController.prototype.Zoom_wheelSeries;
XeroController.prototype.ZoomOut_wheelSeries = XeroController.prototype.Zoom_wheelSeries;
XeroController.prototype.ZoomIn_shift_wheelSeries = XeroController.prototype.Zoom_wheelSeries;
XeroController.prototype.ZoomOut_shift_wheelSeries = XeroController.prototype.Zoom_wheelSeries;

XeroController.prototype.ZoomIn_clickImage = function(id,e) {
	var lay = this.model.layout.findLayout(id);
	this.zoomChange(lay,e,1);
};
XeroController.prototype.ZoomOut_clickImage = function(id,e) {
	var lay = this.model.layout.findLayout(id);
	this.zoomChange(lay,e,-1);
};

/** Handles panning the image - causes the image to be moved around on screen.  
 * At the end of the pan, updates the region of the image 
 */
function PanController() {
};
PanController.prototype = new ImageMouseHandler();

XeroController.prototype.Zoom_dragImage = new PanController();
XeroController.prototype.ZoomIn_dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.ZoomOut_dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.dblDragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.Magnify_dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.TrueSize_dragImage = XeroController.prototype.Zoom_dragImage;
XeroController.prototype.ZoomReset_dragImage = XeroController.prototype.Zoom_dragImage;

PanController.prototype.prepare= function(e) {
	this.prepareBase(e);
	
	this.gtr = document.getElementById("tr"+this.lay.id);
	if(this.gtr) {
		console.info("Found an SVG g transform object", this.gtr.id);
		this.baseTransform = this.gtr.attributes.getNamedItem("transform").value;
		console.info("Found base transform", this.baseTransform);
		var ctm = this.gtr.parentNode.getScreenCTM();
		// This object only has scale factors, not rotation.
		this.scaleX = 1/ctm.a;
		this.scaleY = 1/ctm.d;
		console.info("Scale x,y=",this.scaleX, this.scaleY);
		return true;
	}
	this.vml = document.getElementById("vml"+this.lay.id);
	if( this.vml ) {
		console.info("Found a VML element",this.vml.id,"to zoom/pan");
		console.info("Left,Top is", this.vml.style.left,this.vml.style.top);
		var width = this.vml.parentNode.offsetWidth;
		var height = this.vml.parentNode.offsetHeight;
		if( this.lay.sameSize ) {
			this.scaleX = 1;
			this.scaleY = 1;
		} else {
			this.scaleX = 0.5;
			this.scaleY = 0.5;
		}
		this.baseX = this.convertToBase(this.vml.style.left, width, this.scaleX);
		this.baseY = this.convertToBase(this.vml.style.top, height, this.scaleY);
		
		this.finalScaleX = this.vml.coordsize.x/this.convertToBase(this.vml.style.width, width, 1);
		this.finalScaleY = this.vml.coordsize.y/this.convertToBase(this.vml.style.height,height,1);
		
		this.vml.style.left = this.baseX+"px";
		this.vml.style.top = this.baseY+"px";
		return true;
	}
	console.warn("Can't find any object to pan?");
	return false;
}; 

/** Start active panning */
PanController.prototype.start = function(e) {
	return true;
};

PanController.prototype.convertToBase = function(val,length, scale) {
	if( val=="" ) return 0;
	var last = val.charAt(val.length-1);
	if( last=='%' ) {
		return Math.floor(parseInt(val.substring(0,val.length-1))*length*scale/100);
	};
	if( last=="x" ) return parseInt(val.substring(0,val.length-2))*scale;
	// TODO - compute the 3.X from pixel spacing/sizes instead of a raw value. 
	if( last=="m" ) return parseInt(val.substring(0,val.length-2))*3.75*scale
	console.warn("Unknown length type left,top coordinate:",val,length,"last character", last);
};

/** Handle the pan movement - update the transform for SVG, and the style for VML */
PanController.prototype.mousemove = function(e) {
	var posn = docCoords(e);
	if( this.gtr ) {
		this.dx = Math.floor((posn[0]-this.startPosn[0])*this.scaleX);
		this.dy = Math.floor((posn[1]-this.startPosn[1])*this.scaleY);
		var transform = "translate("+this.dx+","+this.dy+") "+this.baseTransform;
		if( this.transform==transform ) return;
		this.e = e;
		this.transform = transform;
		//console.info("Pan delta",this.dx,",",this.dy,"transform",this.transform);
		this.gtr.attributes.getNamedItem("transform").value = this.transform;
		return;
	}
	if( this.vml ) {
		var dx = posn[0]-this.startPosn[0] ;
		var dy = posn[1]-this.startPosn[1] ;
		this.dx = dx;
		this.dy = dy;
		dx = dx*this.scaleX+this.baseX;
		dy = dy*this.scaleY+this.baseY;
		//console.info("dx,dy=",dx,",",dy);
		this.vml.style.left = dx+"px";
		this.vml.style.top = dy+"px";
		return;
	}
};


/** Return the sum of two points, the first point as a comma-separate string, the
 * second directly specified as two values.
 */
PanController.prototype.sumPoint = function(p1,dx,dy) {
	p1 = parsePoint(p1);
	return [p1[0]+dx,p1[1]+dy];
};

/** Handle the end of the pan movement - compute the topLeft, bottomRight and rerender */
PanController.prototype.end = function(e) {
	console.info("Final dx,dy=",this.dx,",",this.dy);
	var tl = this.lay.navigate.merged.get("topLeft");
	var br =this.lay.navigate.merged.get("bottomRight");
	if( !tl ) tl = "0,0";
	if( !br ) br = this.lay.navigate.external.Columns+","+this.lay.navigate.external.Rows;
	var dx = this.dx;
	var dy = this.dy;
	if( this.vml ) {
		dx = Math.floor(dx*this.finalScaleX);
		dy = Math.floor(dy*this.finalScaleY);
		console.info("VML dx,dy=",dx,dy);
	}
	// Convert dx,dy into IMAGE coordinate changes
	// Apply dx,dy to tl, br
	tl = this.sumPoint(tl,-dx,-dy);
	var rows = this.lay.navigate.external.Rows;
	var cols = this.lay.navigate.external.Columns;
	if( tl[0] < -cols/2 ) tl[0] = -cols/2;
	if( tl[1] < -rows/2 ) tl[1] = -rows/2;
	br = this.sumPoint(br,-dx,-dy);
	if( br[0] > cols*1.5 ) br[0] = cols*1.5;
	if( br[1] > rows*1.5 ) br[1] = rows*1.5;
	// The pre-load image should not be created for this, so just call the notify on the br
	this.lay.navigate.parent.merged.topLeft=tl;
	this.lay.navigate.parent.setMerged("bottomRight", br, true);

	this.controller.waitUpdate("pan",5);
};
