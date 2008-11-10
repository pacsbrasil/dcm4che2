/**
 * Contains the general controller for Xero.
 * This is the non-interactive controller for Xero - it handles the actions such as series
 * navigation that do not result in continuous, interactive updates to images etc.
 * There is an interactive controller as well that handles things like window levelling,
 * zoom/pan, and faster navigation while the user has the button down, or until some timeout
 * has completed and the user isn't navigating any longer.  At that point, the interactive
 * controller switches to this controller to complete the overall update.
 *
 * There are some common parts to the controller in a vertical slice:
 * 	Setup - configures the initial model for display
 *  Submit - stores information to the server
 * 	Restore Data - takes information from the server and sets up a local setting
 *  Restore View - takes stored data and sets up the model again for displaying that page/data
 *	Read Input - takes information from a form on the display page and sets up a model to
 *		correspond with it
 *
 * Horizontally, there are several components as well:
 *	Query Page - including search results, query form
 *	Display Page - including series tray, report view, series view, image view etc
 */

/** Creates a XeroController object, typically called xeroController */
function XeroController(view) {
	this.model = new XeroModel(_SARISSA_IS_IE);
	this.view = view;
	
	if( this.isDebug ) this.debug = console.debug;
};

$xeroControllers:{ $(it)()$ }$

var xeroController = new XeroController(xeroView);

XeroController.prototype.isDebug = false;
XeroController.prototype.debug = function() {};

XeroController.prototype.getBody = function XC_getBody() {
	if( this.body===undefined ) {
		this.body = document.getElementById("body");
	};
	return this.body;
};

XeroController.prototype.innerHtml = function XC_innerHtml(el, html) {
	var oldEl = typeof el === "string" ? document.getElementById(el) : el;
	oldEl.outerHTML = html;
	return oldEl;
};

XeroController.prototype.fixWheelMouse = function XC_fixWheelMouse(layouts) {
	if( _SARISSA_IS_IE ) return;
	if(!layouts ) layouts = this.model.layout.layouts;
	var lay, i,n=layouts.length;
	for(i=0; i<n; i++) {
		lay = layouts[i];
		if( ! lay.used ) continue;
		if( lay.mousewheel ) {
			hookEvent(lay.id, "mousewheel", lay.mousewheel);
		};
		if( lay.layouts ) this.fixWheelMouse(lay.layouts);
	}
};

XeroController.prototype.importHtml = function XC_importHtml(el, html) {
	this.debug("importHTML.");
	var oldEl = typeof el === "string" ? document.getElementById(el) : el;

	var dp = new DOMParser();
	var parsed = dp.parseFromString('<div xmlns="http://www.w3.org/1999/xhtml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:svg="http://www.w3.org/2000/svg" xmlns:v="urn:schemas-microsoft-com:vml">'+html+"</div>","text/xml");
	this.debug("Parsed=%s",parsed);
	if( !parsed ) {
		console.warn("No parsed value found - not changing display, source text %s", html);
		return;
	}
	parsed = parsed.documentElement;
	if( !parsed ) {
		console.warn("No parsed document element value found - not changing display, source text %s", html);
		return;
	}
	this.debug("Finished parsing node.");
	var newEl = document.importNode(parsed,true);
	newEl = newEl.firstChild;
	if(!newEl ) console.warn("newEl not defined.");
	if(!oldEl ) console.warn("oldEl '%s' not defined.", el);
	if( !oldEl.parentNode ) console.warn("parentNode of oldEl not defined.");
	this.debug("Finished importing node.");
	oldEl.parentNode.replaceChild(newEl, oldEl);
	this.debug("Replaced body node directly.");
	this.fixWheelMouse();
	this.debug("fixMouseWheel");
	/* Since we just removed the old element from the DOM, return a reference
	to the new element, which can be used to restore variable references. */
	return newEl;
};

XeroController.prototype.replaceHtml = function XC_replaceHtml(node,text) {
	//console.info("Replacing %s with %s", node, text);
	if( _SARISSA_IS_IE ) {
		this.innerHtml(node,text);
	} else {
		this.importHtml(node, text);
	}
}

/** Updates the view in its entirety.
 */
XeroController.prototype.updateView = function XC_action(actionName) {
	console.time(actionName);
	var render = this.view.toString("xeroBody",this.model);
	if( render==null || render==="" ) {
		alert("Couldn't render view.");
		return;
	}
	window._render = render;
	
	//console.info("Render: %s",render);
	this.replaceHtml("body", render);
	console.timeEnd(actionName);
};

/** Performs a delayed update */
XeroController.prototype.waitUpdate = function XC_waitUpdate(actionName, cnt) {
	if( cnt===undefined ) cnt = 5;
	var missing = this.renderComplete([this.model.layout]);
	if( missing===0 ) return 0;
	if( cnt===0 ) {
		this.debug("Rendering with cnt=%d with %d missing", cnt, missing);
		this.updateLayouts(actionName);
		return;
	}
	this.debug("Waiting till everything is complete - cnt=%d", cnt);
	var usethis = this;
	window.setTimeout( function() {
		usethis.waitUpdate(actionName,cnt-1);
	}, 150);
};

/** Renders all components that have relayout set and are complete.  Returns a count of required items. */
XeroController.prototype.renderComplete = function XC_renderComplete(layouts,incomplete) {
	if(! layouts ) return 0;
	var render, origLay,lay,i,n=layouts.length;
	var ret = 0;
	for(i=0; i<n; i++) {
		lay = layouts[i];
		if( !lay.used ) continue;
		if( lay.relayout ) {
			if( incomplete || lay.isComplete() ) {
				this.debug("Re-rendering %s template %s",lay.id, lay.template);
				origLay = this.model.layout;
				this.model.layout = lay;
				console.time("rerender");
				render = this.view.toString(lay.template,this.model);
				console.timeEnd("rerender");
				this.model.layout = origLay;
				this.replaceHtml(lay.id,render);
				lay.clearRelayout();
			}
			else {
				ret++;
			} 
		}
		else {
			ret += this.renderComplete(lay.children,incomplete);
		}
	}
	return ret;
};


/** Updates the layout components individually, only as required. */
XeroController.prototype.updateLayouts = function XC_updateLayouts(actionName,layouts) {
	if( layouts===undefined ) {
		console.time(actionName);
		if(! this.model.layout.layouts ) {
			console.warn("Layout is null - can't update layout.");
			return;
		}
		this.updateLayouts(actionName,this.model.layout.layouts);
		console.timeEnd(actionName);
		return;
	}

	// Rendering everything, even stuff not yet complete.
	this.renderComplete(this.model.layout.layouts,true);	 
};

/** Switch to the given tab */
XeroController.prototype.switchTab = function XC_switchTab(src,tabName) {
	if( this.model.layout.selectTab(tabName) ) {
		this.updateView("switchTab");
	}
};

/** Switch a button */
XeroController.prototype.pushButton = function XC_pushButton(layId) {
	var lay = this.model.layout.findLayout(layId);
	if(!lay ) {
		console.warn("No layout named",layId,"found.");
		return;
	};
	lay.isActive = ! lay.isActive;
	lay.relayout = true;
	this.waitUpdate("pushButton");
};

/** Rotates to the given child item */
XeroController.prototype.rotateButton = function XC_rotateButton(layId,arg) {
	var lay = this.model.layout.findLayout(layId);
	if(!lay ) {
		console.warn("No layout named",layId,"found.");
		return;
	};
	var par, chd;
	console.info("Rotating button",lay.id,"arg",arg,"in",lay.i18n);
	if( lay.children ) {
		par = lay;
		chd = lay.findRotate(arg,0);
	} else {
		par = lay.parent;
		chd = lay;
	};
	var lst = par.findRotate(par.i18n);
	if( lst ) lst.isActive = false;
	chd.isActive = true;
	par.copyFrom(chd);
	// Choose the next child type
	var rot = par.findRotate(par.i18n,1);
	console.info("Found rotate",rot.i18n);
	par.arg = rot.i18n;
	par.relayout = true;
	this.waitUpdate("rotateButton");
};

// Define short versions of the MVC components.
var controller = xeroController;
var model = controller.model;
var view = xeroView;
