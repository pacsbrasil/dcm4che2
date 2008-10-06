/**
 * The dynamic controller components are the parts that respond with interactive image presentation state
 * changes.
 */

/** Handle the image area mouse down */
XeroController.prototype.imageMouseDown = function XC_imageMouseDown(layId,event) {
	if( this.mousing ) return;
	if( !event ) event = window.event;
	if(!this.imageMouseHandler ) return; 
	var mousing = this.imageMouseHandler;
	mousing.target = target(event);
	mousing.imageDisplay = this.findImageTarget(mousing.target);
	if( !mousing.imageDisplay ) {
		console.warn("Somehow you appeared to have clicked on a non-imageDisplay target %s", mousing.target.id);
		return;
	}
	mousing.startPosn = docCoords(event);
	hookEvent(mousing.imageDisplay,"mousemove", XC_callImageMouseMove);
	hookEvent(mousing.imageDisplay,"mouseout", XC_callImageMouseOut);
	hookEvent(mousing.imageDisplay,"mouseup", XC_callImageMouseUp);
	mousing.controller = this;
	if(!mousing.start(event)) return false;
	this.mousing = mousing;
	return evtPreventDefault(event);
};

/** Finds the image display parent of the given object */
XeroController.prototype.findImageTarget = function XC_findImageTarget(src) {
	var clazz = src.className;
	var prevChild = src;
	if( clazz && clazz.baseVal ) clazz = clazz.baseVal;
	while( src && clazz!="imageDisplay" && src.nodeName!=="BODY" && src.nodeName!=="body") {
		console.debug("findImageTarget %s %s %s", src.id, src.nodeName, clazz);
		prevChild = src;
		src = src.parentNode;
		clazz = src.className;
		if( clazz && clazz.baseVal ) clazz = clazz.baseVal;
		// Safari and Opera don't provide easy access to svg class.
		if( clazz==="image" ) {
			console.info("Returning previous child - not really a good test.");
			return prevChild;
		}
	} 
	if( clazz==="imageDisplay" ) {
		console.debug("findImageTarget Found - %s %s %s", src.id, src.nodeName, clazz);
		return src;
	}
	return undefined;
}; 

/** Call the Xero controller mouse move. */
function XC_callImageMouseMove(evt) {
	return controller.imageMouseMove(evt);
};
/** Call the Xero controller mouse move. */
function XC_callImageMouseOut(evt) {
	return controller.imageMouseOut(evt);
};
/** Call the Xero controller mouse move. */
function XC_callImageMouseUp(evt) {
	return controller.imageMouseUp(evt);
};

/** Handle the mouse movement events */
XeroController.prototype.imageMouseMove = function XC_imageMouseMove(event) {
	if( !this.mousing ) return;
	if( !event ) event = window.event;
	if( !isMouseDown(event) ) {
		return this.imageMouseUp(event);
	}
	if( this.mousing.mousemove!=null ) {
		this.mousing.mousemove(event);
	}
	return false;
};

/** Handle the mouse up events */
XeroController.prototype.imageMouseUp = function XC_imageMouseUp(event) {
	if( !this.mousing ) return true;
	if( !event ) event = window.event;
	this.mousing.end(event);
	unhookEvent(this.mousing.imageDisplay,"mousemove", XC_callImageMouseMove);
	unhookEvent(this.mousing.imageDisplay,"mouseout", XC_callImageMouseOut);
	unhookEvent(this.mousing.imageDisplay,"mouseup", XC_callImageMouseUp);
	this.mousing = null;
	return evtPreventDefault(event);
};

/** Handle mouse out events */
XeroController.prototype.imageMouseOut = function XC_imageMouseOut(event) {
	if( !this.mousing ) return;
	if( !event ) event = window.event;
	var targ = target(event);
	var reltg = (event.relatedTarget) ? event.relatedTarget : event.toElement;
	var src = this.findImageTarget(targ);
	var dest = this.findImageTarget(reltg);
	console.info("Mouse out test src id dest id",(src==null ? "null src" : src.id), (dest==null ? "null dest" : dest.id));
	if( dest===null || src!==dest ) return this.imageMouseUp(event);
	console.info("Same target - just ignoring this.");
	return evtPreventDefault(event);
};

/** Define the base functions for a mouse handler */
function MouseHandler() {
};

MouseHandler.prototype.start = function MouseHandler_start(event) {
	console.info("Hello, image mouse down target id %s imageDisplay %s button %d which %d at ", this.target, this.imageDisplay.id, event.button, event.which, this.startPosn);
	return true;
};

MouseHandler.prototype.mousemove = function MouseHandler_showMouseMove(event) {
	var posn = docCoords(event);
	var delta = [posn[0]-this.startPosn[0],posn[1]-this.startPosn[1]];
	console.debug("Mouse move button %d which %d at %s", event.button, event.which, delta);
};

MouseHandler.prototype.end = function MouseHandler_showMouseMoveEnd(event) {
	var posn = docCoords(event);
	var delta = [posn[0]-this.startPosn[0],posn[1]-this.startPosn[1]];
	console.debug("Show Mouse Move End button %d which %d at %s", event.button, event.which, delta);
};

/** Finds the (primary) image object */
MouseHandler.prototype.getImage = function MouseHandler_getImage() {
	var imageId = "im"+this.imageDisplay.id.substring(3);
	var ret = document.getElementById(imageId); 
	//console.info("Getting image id %s object %s", imageId, ret);
	return ret;
};

/** Finds the url for the image object */
MouseHandler.prototype.getImageUrl = function MouseHandler_getImageUrl(strip) {
	if(!this.image) this.image = this.getImage();
	var url = this.image.getAttribute("src");
	if( url ) return stripUrl(url,strip);
	url = this.image.getAttributeNS("http://www.w3.org/1999/xlink","href");
	if( url ) return stripUrl(url,strip);
	console.warn("Unable to find URL for %s",this.image.id);
};

/** Sets the url for the image object */
MouseHandler.prototype.setImageUrl = function MouseHandler_setImageUrl(url) {
	var origUrl = this.image.getAttribute("src");
	if( origUrl ) {
		this.image.setAttribute("src", url);
		return;
	}
	this.image.setAttributeNS("http://www.w3.org/1999/xlink","href",url);
};

MouseHandler.prototype.findLayout = function MouseHandler_findLayout() {
	var id = this.imageDisplay.parentNode.id;
	return this.controller.model.patientLayout.findLayout(id);
};

/** Defines the individual dynamic controllers */
$windowLevel()$