/**
 * The dynamic controller components are the parts that respond with interactive image presentation state
 * changes.
 * The dynamic events are:
 *    Window Level - while actually interacting
 *	  Panning
 *
 * The non-dynamic events include things like rotation, flip, reset that 
 * get applied on image mouse down, but don't add any mouse listening controls.  These
 * types of events use the plug-in design for handling mouse down, but do not otherwise
 * handle ongoing mouse events.
 *
 * Use of shift/control/alt can modify the base events or cause an alternative event to
 * be used.
 * shift is used to modify the base action - making it faster or otherwise modifying it.
 * alt, ctrl are both used to choose different actions
 *
 * 
 */

/** Given the current mode, this looks for the mode handler to use for that mode/type.
 * The name is an _ separated name list, starting from the most general handler to the
 * most specific.  Each name is looked for as a function in this object, and if none is
 * found, then undefined is returned.
 * Typically, this will be something like:
 * ctrl_a_ImageArea
 * which is an image area key handler, for the a key with ctrl.
 * Or
 * WindowLevel_ctrl_left_ImageArea
 * meaning that it is an image area left item, with ctrl down, in window level mode.
 *
 * The mode handler name is returned as it could be used either as a function call or
 * as an object depending on the context.
 * This should NOT be used for interactive move/up events, but only for the initial,
 * triggerring event.  Also, if there is at text box or something equivalent, it is better
 * to have a dedicated handler that does not go through this logic, as it maybe a bit slow
 * for faster events.
 */
XeroController.prototype.findModeHandler = function(name) {
	if( this[name] ) return name;
	var under = name.indexOf("_");
	if( under>=0 ) return this.findModeHandler(name.substring(under+1));
};

/** Handles mouse wheel events in the image area */
XeroController.prototype.wheelMouse = function (id,e) {
	if(!e) e = window.event;
	var lay = this.model.layout.findLayout(id);
	var area = lay.area; 
	var handler = this.getShiftInfo(e);
	handler = handler+"wheel";
	if( area ) handler = this["getMode"+area](lay,e)+"_"+handler+area; 
	console.debug("Looking for handler for",handler);
	handler = this.findModeHandler(handler);
	console.info("Found mouse click handler",handler);
	if( handler ) return this[handler](lay,e);
};

/** Gets the shift information */
XeroController.prototype.getShiftInfo = function(e) {
	var ret = "";
	if( e.ctrlKey ) ret = "ctrl_"+ret;
	if( e.shiftKey ) ret = "shift_"+ret;
	if( e.altKey ) ret = "alt_"+ret;
	if( e.metaKey ) ret = "meta_"+ret;
	return ret;
};

/** Handles the key press event - 
 * @TODO see if there is a reasonable way to hook up specific event objects instead of
 * the generalized ones.
 */
XeroController.prototype.keyPress = function(e) {
	if(!e) e = window.event;
	var shift = this.getShiftInfo(e);	
	var handler= shift+"key"+e.keyCode;
	console.debug("Key press handler initial",handler);
	handler = this.findModeHandler(handler);
	console.debug("Found handler",handler);
	if(!handler) return;
	return this[handler](e);
};

XeroController.prototype.dblClickTime=350;
XeroController.prototype.dblMoveDistance=2;

/**
 * Determine if the click was a double click event
 */
XeroController.prototype.isDoubleClick = function(e) {
	var lastMouse = this.lastMouse;
	e.timems = (new Date()).getTime();
	this.lastMouse = e;
	if( !lastMouse ) return false;
	var delta = e.timems - lastMouse.timems; 
	if( delta > this.dblClickTime ) return false;
	delta = Math.abs(e.clientX-lastMouse.clientX)+Math.abs(e.clientY-lastMouse.clientY);
	if( delta>this.dblMoveDistance ) return false;
	// Clear the last mouse as it can't be used for a triple click.
	this.lastMouse = null;
	return true;
};

/**
 * Determine if the mouse-move was long enough to be valid - don't actually
 * start handling any child events until this one returns true.
 */
XeroController.prototype.isMouseMoveValid = function(e) {
	var lastMouse = this.lastMouse;
	var timems = (new Date()).getTime();
	if( !lastMouse ) return true;
	var delta = timems - lastMouse.timems; 
	if( delta > this.dblClickTime ) return true;
	delta = Math.abs(e.clientX-lastMouse.clientX)+Math.abs(e.clientY-lastMouse.clientY);
	if( delta>this.dblMoveDistance ) return true;
	// Clear the last mouse as it can't be used for a double click
	this.lastMouse = null;
	return false;
};

/**
 * Handles the mouse down events - this is typically called underneath a
 * wrapper function that computes the mode to try to use.
 */
XeroController.prototype.mouseDown = function(id,e) {
	if( this.mousing ) return;
	if(!e) e = window.event;
	var lay = this.model.layout.findLayout(id);
	var area = lay.area; 
	var handler = this.getShiftInfo(e);
	var clickHandler = handler;
	if( this.isDoubleClick(e) ) {
		handler = handler+"dblDrag";
		clickkHandler = clickHandler + "dbl";
	} else {
		handler = handler+"drag";
		clickHandler = clickHandler + "click";
	};
	if( area ) {
		var mode = this["getMode"+area](lay,e)+"_";
		handler = mode+handler+area;
		clickHandler = mode+clickHandler+area;
	} 
	console.info("Looking for handler for",handler);
	handler = this.findModeHandler(handler);
	clickHandler = this.findModeHandler(clickHandler);
	if(!(handler || clickHandler)) return;
	console.info("Found mouse handler",handler,"click",clickHandler);
	
	if( handler ) {
		var mousing;
		mousing = this[handler];
		mousing.clickHandler = clickHandler;

		mousing.lay = lay;
		mousing.controller = this;
		mousing.startPosn = docCoords(e);
		var mouseDom = mousing.getMouseDom(target(e));
		mousing.mouseDom = mouseDom;
		this.mousing = mousing;
		if( !mouseDom ) {
			console.warn("No mouse DOM area to hook mouse events to for",handler);
			return;
		}
		console.info("Found mouse DOM area",mouseDom.id);
		if( ! mousing.prepare(e) ) {
			console.warn("Mouse prepare for",handler,"returned false, not mousing.");
			return;
		}
		console.info("Area prepared - now about to hook listeners.");
	
		if(mousing.mousemove) hookEvent(mouseDom, "mousemove", XC_callMouseMove);
		hookEvent(mouseDom,"mouseout", XC_callMouseOut);
		hookEvent(mouseDom,"mouseup", XC_callMouseUp);
		console.info("Hooked events, returning and awaiting the next call.");
	} else {
		// Only a click handler - so we can execute it when the mouse comes up.
		this.mousing = {"clickHandler":clickHandler,"lay":lay};
		hookEvent(target(e),"mouseout", XC_callMouseOut);
		hookEvent(target(e),"mouseup", XC_callMouseUp);
	};
	return cancelEvent(e);
};

/** 
 * Finds the image display parent of the given object.  This is a top-level funciton that is
 * re-used.
 */
function XC_findImageTarget(src) {
	var clazz = src.className;
	var prevChild = src;
	if( clazz && clazz.baseVal ) clazz = clazz.baseVal;
	while( src && clazz!="imageDisplay" && src.nodeName!=="BODY" && src.nodeName!=="body") {
		console.debug("findImageTarget", src.id, src.nodeName, clazz);
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
		console.debug("findImageTarget Found", src.id, src.nodeName, clazz);
		return src;
	}
	return undefined;
}; 

/** Call the Xero controller mouse move. */
function XC_callMouseMove(evt) {
	return controller.mouseMove(evt);
};
/** Call the Xero controller mouse move. */
function XC_callMouseOut(evt) {
	return controller.mouseOut(evt);
};
/** Call the Xero controller mouse move. */
function XC_callMouseUp(evt) {
	return controller.mouseUp(evt);
};

/** Handle the mouse movement events */
XeroController.prototype.mouseMove = function(e) {
	if( !e ) e = window.event;
	if( !this.mousing ) {
		unhookEvent(target(e),"mousemove", XC_callMouseMove);
		return cancelEvent(e);
	};
	if( !isMouseDown(e) ) {
		return this.mouseUp(e);
	}
	// Only start sending events to the child element once the double click
	// time/distance is exceeded so that no updates are done before that.
	// The return value will be true on started once it is ready.
	if(this.isMouseMoveValid(e) ) {
		if( !this.mousing.started ) {
			this.mousing.started = this.mousing.start();
			if( !this.mousing.started ) return false;
		} 
		this.mousing.mousemove(e);
	} else {
		console.info("Still waiting for the mouse move to become valid.");
	};
	return false;
};

/** Handle the mouse up events */
XeroController.prototype.mouseUp = function(e) {
	if( !this.mousing ) return true;
	if( !e ) e = window.event;
	this.mousing.end(e);
	unhookEvent(this.mousing.mouseDom,"mousemove", XC_callMouseMove);
	unhookEvent(this.mousing.mouseDom,"mouseout", XC_callMouseOut);
	unhookEvent(this.mousing.mouseDom,"mouseup", XC_callMouseUp);
	this.mousing = null;
	this.waitUpdate("mouseUp",2);
	return cancelEvent(e);
};

/** Handle mouse out events - currently only handles image area events 
 * @TODO fix this to be more general
 */
XeroController.prototype.mouseOut = function(e) {
	if( !this.mousing ) return;
	if( !e ) e = window.event;
	var targ = target(e);
	var reltg = (e.relatedTarget) ? e.relatedTarget : e.toElement;
	var src = this.findImageTarget(targ);
	var dest = this.findImageTarget(reltg);
	console.info("Mouse out test src id dest id",(src==null ? "null src" : src.id), (dest==null ? "null dest" : dest.id));
	if( dest===null || src!==dest ) return this.mouseUp(e);
	console.info("Same target - just ignoring this.");
	return cancelEvent(e);
};


/** 
 * Define the base functions for a mouse handler.  Mouse handlers
 * those those that handle on-going mouse move listening.  These are NOT the handlers
 * that handle simple click type events, or mouse wheel events.
 * The expected handling of a mouse event is that 
 * 1. The start event is called (if no start is present, then this isn't a mouse handler)
 *     This can update the screen, or cause fetches to be started, but MUST not re-draw
 *     existing objects.
 */
function MouseHandler() {
};

/** Called before start is called to start loadings information/background images etc
 * @return true to indicate to handle this click.
 */
MouseHandler.prototype.prepare = function (lay,e) {
	this.imageDisplay = null;
	this.image = null;
	this.started = false;
	return true;
};

/** Have an extra copy of this to allow calling the parent method */ 
MouseHandler.prototype.prepareBase = MouseHandler.prototype.prepare;


/** Gets the object to register the general events on - mostly this will be the image area
 * so that is setup to be the default area.
 */
MouseHandler.prototype.getMouseDom = XC_findImageTarget;

MouseHandler.prototype.start = function MouseHandler_start(event) {
	console.info("Hello, image mouse down target id %s imageDisplay %s button %d which %d at ", this.target, this.imageDisplay.id, event.button, event.which, this.startPosn);
	return true;
};

MouseHandler.prototype.end = function MouseHandler_showMouseMoveEnd(event) {
	var posn = docCoords(event);
	var delta = [posn[0]-this.startPosn[0],posn[1]-this.startPosn[1]];
	console.debug("Show Mouse Move End button %d which %d at %s", event.button, event.which, delta);
};

/** Finds the (primary) image object */
MouseHandler.prototype.getImage = function() {
	if(!this.imageDisplay) this.imageDisplay = this.mouseDom;
	var imageId = "im"+this.imageDisplay.id.substring(3);
	var ret = document.getElementById(imageId); 
	console.info("Getting image id %s object %s", imageId, ret);
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
	var id = this.mouseDom.parentNode.id;
	return this.controller.model.layout.findLayout(id);
};


/** Defines the individual dynamic controllers */
$dynamic/windowLevel()$