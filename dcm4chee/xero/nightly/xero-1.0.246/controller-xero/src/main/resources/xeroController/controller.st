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
	var ua = navigator.userAgent;
	if(_SARISSA_IS_IE) {
		this.model.IS_IE6 = (ua.indexOf("MSIE 6")>=0);
		this.model.HAS_SVG = false;
		this.model.HAS_VML = true;
	} else {
		this.model.HAS_SVG = true;
		this.model.HAS_VML = false;
	}
	this.model.IS_FIREFOX = _SARISSA_IS_MOZ;
	this.model.IS_MOBILE = (ua.indexOf("Mobile")>=0 || ua.indexOf("BlackBerry")>=0);
	this.view = view;
	
	if( this.isDebug ) this.debug = console.debug;
};

XeroController.convertToBase = function(val,length, scale) {
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

$xeroControllers:{ $(it)()$ }$

var xeroController = new XeroController(xeroView);

XeroController.prototype.isDebug = false;
XeroController.prototype.debug = function() {};

/** Finds the layout specified by the given id, optionally looks for the parent layout of the
 * given patient through image level (1->4)
 */
XeroController.prototype.findLayout = function(id,lvl) {
	if( typeof(id)==="object" ) return id;
	
	if(!model.layout)
	{
	    return null;
	}
	
	var lay = this.model.layout.findLayout(id);
	if( !lvl ) return lay;
	while(lay) {
		if( lay && lay.navigate && lay.navigate.level===lvl ) return lay;
		lay = lay.parent;
	}
};

/** Updates the innerHtml of the element provided (el) with the provided HTML to update the
 * display area in IE.
 */
XeroController.prototype.innerHtml = function XC_innerHtml(el, html) {
	var oldEl = typeof el === "string" ? document.getElementById(el) : el;
	oldEl.outerHTML = html;
	return oldEl;
};

/** Non-IE browsers don't have a mouse wheel event to hook up to, so
 * use the DOM events to hookup the mouse events. */
XeroController.prototype.fixWheelMouse = function XC_fixWheelMouse(lay) {
	if( this.model.IS_IE ) return;
	
	if( lay.mousewheel ) {
	   hookEvent(lay.id, "mousewheel", lay.mousewheel);
	};
};


/** Gets the scroll node, or null if not found */
XeroController.prototype.getScrollNode = function(id) {
   var node;
   if( this.model.IS_FIREFOX ) {
      node = document.getElementById(id+"Body");
   } 
   if(!node ) {
      node = document.getElementById(id);
   }
   return node;
};

/**
 * Restore the scroll position - has special handling for Firefox to restore
 * the tbody scroll position instead of the top-level scroll position.
 */
XeroController.prototype.fixScrollPosition = function(id) {
   var lay = this.findLayout(id);
   if( lay.scrollTop==undefined ) return;
   var node = this.getScrollNode(lay.id);
   console.info("fixScrollPosition",id,"on",node,"to",lay.scrollTop);
   if( node ) node.scrollTop = lay.scrollTop;
};

/** Store the scroll position, so that it can be restored later */
XeroController.prototype.storeScrollPosition = function(id) {
   var lay = this.findLayout(id);
   var node = this.getScrollNode(lay.id);
   lay.scrollTop = node.scrollTop;
};

/** Replaces the node specified by the node id, with the given replacement text.
 * If isDiffUpdate is true, then this should be updated by a difference comparison
 * rather than a replacement.
 */
XeroController.prototype.replaceHtml = function XC_replaceHtml(node,text,isDiffUpdate) {
   var ret;
   if( this.model.IS_IE ) {
      if( isDiffUpdate ) {
   	     ret = DOMUtils.updateHtml(node,text);
      } else {
         ret = this.innerHtml(node,text);
       }
   }
   else
   {
      this.debug("fixMouseWheel");
      var ret = DOMUtils.updateHtml(node, text);
   }
   this.fireViewRendered(this.findLayout(node));
   return ret;
};

/** Fires the view rendered event recursively on this and child elements */
XeroController.prototype.fireViewRendered = function(lay) {
	if( !lay || !lay.used ) return;
	// Fire event
	var i,n;
	if( lay.viewRenderedListeners ) {
		n = lay.viewRenderedListeners.length;
		for(i=0;i<n; i++) {
			this[lay.viewRenderedListeners[i]](lay);
		};
	};
	
	// Recurse over the children
	if( !lay.children ) return;
	n=lay.children.length;
	for(i=0;i<n;i++) {
		this.fireViewRendered(lay.children[i]);
	};
};

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
	this.replaceHtml(this.model.layout.id, render);
	this.model.layout.clearRelayout();
	console.timeEnd(actionName);
};

/** Performs a delayed update */
XeroController.prototype.waitUpdate = function XC_waitUpdate(actionName, cnt) {
	if( cnt===undefined ) cnt = 7;
	var missing = this.renderComplete([this.model.layout]);
	if( missing===0 ) return 0;
	if(cnt===0 || missing<0) {
		this.debug("Rendering with cnt=%d with %d missing", cnt, missing);
		this.updateLayouts(actionName);
		return;
	}
	this.debug("Waiting till everything is complete - cnt=%d", cnt);
	var usethis = this;
	window.setTimeout( function() {
		usethis.waitUpdate(actionName,cnt-1);
	}, 200);
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
				var result = this.replaceHtml(lay.id,render, lay.isDiffUpdate);
				lay.clearRelayout();
				if( result===false ) {
					return -100000;
				} 
			}
			else {
				ret++;
			} 
		}
		else {
			ret += this.renderComplete(lay.children,incomplete);
		}
		if( ret<0 ) return ret;
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
	var lay = this.findLayout(layId);
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
	var lay = this.findLayout(layId);
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
