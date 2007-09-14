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
/**
 * Events.js contains methods to deal with registering for events, getting
 * coordinates etc.
 */
 
/**
 * Finds the coordinates for a given event, multi-browser supported.
 * Returns array with the DOCUMENT based coordinates
 * @param (Event) e is the event to use.  Will use window.event for IE
 * @return [x,y] document based coordinates.
 * @author tarquinwj (public code in posted as demo)
 */
function docCoords(e) {
   if( !e ) { e = window.event; } if( !e || ( typeof( e.pageX ) != 'number' && typeof( e.clientX ) != 'number' ) ) { return [ 0, 0 ]; }
   if( typeof( e.pageX ) == 'number' ) { return [e.pageX,e.pageY];} else {
      var posX = e.clientX; var posY = e.clientY;
      if( !( ( window.navigator.userAgent.indexOf( 'Opera' ) + 1 ) || ( window.ScriptEngine && ScriptEngine().indexOf( 'InScript' ) + 1 ) || window.navigator.vendor == 'KDE' ) ) {
         if( document.documentElement && ( document.documentElement.scrollTop || document.documentElement.scrollLeft ) ) {
            posX += document.documentElement.scrollLeft; posY += document.documentElement.scrollTop;
         } else if( document.body && ( document.body.scrollTop || document.body.scrollLeft ) ) {
            posX += document.body.scrollLeft; posY += document.body.scrollTop;
         }
      }
      return [ posX, posY ];
   }
};

/** Get the target of the event */
function target(e) {
	var targ;
	if( !e ) var e = window.event;
	if ( e.target ) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if( targ.nodeType == 3 ) // defeat Safari bug
	  targ = targ.parentNode;
	return targ;
}

/** Adds the given event to the given element.  Works in IE, Firefox, Konqueror, Opera, and Safari.
 * @param elm is the element node or id to add an event to.
 * @param evType is the type to add a listener for
 * @param fn is the function to call
 * @param useCapture indicates that this function will handle the event exclusively.
 */
function addEvent(elm, evType, fn, useCapture) {
	if( (typeof elm)==='string' ) {
		elm = document.getElementById(elm);
	}
	if (elm.addEventListener) {
		elm.addEventListener(evType, fn, useCapture);
		return true;
	}
	else if (elm.attachEvent) {
		var r = elm.attachEvent('on' + evType, fn);
		return r;
	}
	else {
		elm['on' + evType] = fn;
	}
};

/** Adds an initialization event 
 * @param func is the function to all.
 */
function addLoadEvent(func) {
	if( typeof(YAHOO)!=='undefined' ) {
		YAHOO.util.Event.onContentReady("body", func);
		return;
	}
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = func;
	}
	else {
		window.onload = function() {
			oldonload();
			func();
		}
	}
};

/** Tells if this is a left mouse button.  Works in IE, Firefox, Konq, Opera, Safari.
 * @param evt to detect the mouse button on.
 * @return true if left mouse button.
 */
function isLeftMouse(evt) {
	if( evt.which ) return (evt.which<2);
	return event.button < 2;
};

/** Encodes URL & objects correctly 
 * @param url (string) */
function encodeURL(url) {
  if( ! url ) return "";
  return url.replace(/&/g,'&amp;');
};

var level = 1;

/** Test to see if the trace library is available. */
function ttrace(msg) {
	if( typeof(trace)!='undefined') {
		trace(msg);
	}
};

/** A no-op for use as an included operation */
function notrace(msg) {
};

/** Dumps an object, complete with all attributes */
function dumpObj(msg,obj) {
	var e;
	try {
  		for(var v in obj) {
			info(msg+"."+v+"="+obj[v]);
		}	
	} catch(e) {
		warn("<b>Caught exception while dumping:"+e+"</b>");
	}
};

function debug(msg) {
	if( level<=0) {
		ttrace(msg);
	};
};

function info(msg) {
	if( level<=1) {
		ttrace(msg);
	};
};

function warn(msg) {
	if( level<=2) {
		ttrace(msg);
	};
};

function error(msg) {
	if( level <=3 ) {
		alert("Error:"+msg);
		ttrace(msg);
	};
};

/** Parse get width from a node */
function getWidthFromNode(node) {
	var wid = node.getAttribute("width");
	if( wid ) return Number(wid);
	var style = node.style;
	if(!style) error("Can't get width");
    wid = style.width;
    return Number(wid.substring(0,wid.length-2));
};

/** Parse get width from a node */
function getHeightFromNode(node) {
	var height = node.getAttribute("height");
	if( height ) return Number(height);
	var style = node.style;
	if(!style) error("Can't get height");
	height = style.height;
    return Number(height.substring(0,height.length-2));
};

/** Return true if the tag is of the given type - only compares after the : for namespaced elements.  Works 
 * in both XHTML and HTML browsers (Firefox and IE). */
function isTag(node, tag) {
	return node.localName===tag || node.tagName===tag;
};

/** Finds another element by a #ID ref */
function findByRef(numId) {
	var octothorpe = numId.indexOf('#');
	var search = numId;
	if( octothorpe >=0 ) {
		search = numId.substring(octothorpe+1);
	}
	return document.getElementById(search);
};

/** Parses a point into some number of numbers as an array.  
 * @param {String} point
 */
function parsePoint(point) {
	var spoint = ""+point;
	var xy = spoint.match(/[\-0-9.]+/g);
	for(var i=0; i<xy.length; i++) {
		xy[i] = parseInt(xy[i]);
	};	
	return xy;
};

/** Tests to see which browser this is. */
function browserNameFunc() {
	if( navigator.appName=="Microsoft Internet Explorer" ) {
		return "IE";
	}
	if( navigator.userAgent.indexOf("WebKit")>=0 ) {
		return "Safari";
	}
	if( navigator.userAgent.indexOf("Konqueror")>=0 ) {
		return "Konqueror";
	}
	if( navigator.userAgent.indexOf("Gecko")>=0 ) {
		return "Firefox";
	}
	if( window.opera ) return "Opera";
	var msg = "Unknown browser -update browserNameFunc with support for "+navigator.appName+" userAgent="+navigator.userAgent;
	warn(msg);
	return navigator.appName;
};
var browserName = browserNameFunc();
