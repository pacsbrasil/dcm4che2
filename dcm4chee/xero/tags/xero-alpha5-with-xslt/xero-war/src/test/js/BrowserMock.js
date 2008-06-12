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
 * Contains mock examples of different types of browser based objects. 
 * Some functionality is implemented, returning additional mock objects.
 */
 
function Image(src) {
	this.src = src;
};
Image.prototype.width=0;
Image.prototype.height=0;
Image.prototype.complete=false;
Image.prototype.src=null;
Image.prototype.onload=null;

/**
 * Delivers the image, calling the onload and setting complete.
 */
function Image_deliver() {
	this.complete = true;
	if( this.onload!==null ) {
		info("Calling image onload event.");
		this.onload();
	};
};
Image.glue();


/** Create a TimeIdObj for intervals and timeouts */
function TimeIdObj(func, delay, repeats, args)
{
	this.func = func;
	this.delay = delay;
	this.args = args;	
	this.repeats = repeats;
	this.nextTime = window._time+this.delay;
};

function TimeIdObj_fire(windw) {
	this.func.apply(windw,this.args);
};
TimeIdObj.glue();

/** Some window methods */
Window.prototype.setInterval = function Window_setInterval(func,delay) {
	var args = new Array();
	if( func===undefined || func===null ) {
		error("Function for interval is null or undefined.");
		return null;
	}
	for(var i=2; i<arguments.length; i++) args.push(arguments[i]);
	var timeId = new TimeIdObj(func, delay, true, args);
	this._timers.push(timeId);
	return timeId;
};

window._timers = new Array();
window._time = 0;

function Window_clearInterval(interval) {
	var n=this._timers.length;
	for(var i=0; i<n; i++) {
		var timer = this._timers[i];
		if( timer ===  interval ) {
			info("Clearing interval "+interval);
			this._timers.splice(i,1);
			return;
		};
	};
	info("**** FAILED to clear interval "+interval);
};

function Window_deliverInterval(addTime) {
	info("Delivering time.");
	this._time = this._time+addTime;
	var n=this._timers.length;
	var timer;
	info("There are "+n+" time listeners.");
	for(var i=0; i<n; i++) {
		timer = this._timers[i];
		if( timer.nextTime <= this._time ) {
			info("About to fire timer "+i);
			timer.fire(this);
			if( ! timer.repeats ) {
				this._timers.splice(i,1);
				i--;
				n--;
			} else {
				timer.nextTime = this._time+timer.delay;
			};
		};
	};
};
Window.glue();


/**
 * Define an XML "HTTP" Request that can have objects delivered to it
 */
function XMLHttpRequest()
{
};

XMLHttpRequest.prototype.UNSENT = 0;
XMLHttpRequest.prototype.OPENED = 1;
XMLHttpRequest.prototype.LOADING = 3;
XMLHttpRequest.prototype.DONE = 4;
XMLHttpRequest.prototype.responseText = null;
XMLHttpRequest.prototype.responseXML = null;
XMLHttpRequest.prototype.status = 200;
XMLHttpRequest.prototype.statusText = null;
XMLHttpRequest.prototype.delivered = new Object();
XMLHttpRequest.prototype.readyState = 0;

/**
 * Open the response
 */
function XMLHttpRequest_open(method, url, async, user, password) {
	this.method = method;
	this.url = url;
	this.async = async;
	this.user = user;
	this.password = password;
	this.readyState = this.OPENED;
};

/**
 * Send the request method - in this version, synchronous request responses must be delivered BEFORE
 * the request is available, while asynchronous ones can be delivered before or after.  If they are
 * already delivered, then it will be immediately available and the response
 */
 function XMLHttpRequest_send(data) {
 	 var item = this.delivered[this.url];
 	 if( item!==undefined ) {
 	 	item.addRequest(this);
 	 	item.deliver();
 	 	return;
 	 }
 	 if( this.async ) {
 	 	item = new XMLHttpRequestItem(this.url);
 	 	item.addRequest(this);
 	 	this.delivered[this.url] = item;
 	 }
 	 else {
 	 	throw new Error("Try to request "+this.url+" synchronously, but not delivered/available yet.");
 	 };
 	 this.readyState = this.LOADING;
 };
 
 /**
 * Deliver the given data 
  */
function XMLHttpRequest_deliverXml(url, xml) {
	if( xml===undefined || xml===null ) throw new Error("Cannot deliver null or undefined xml "+xml);
	var item = this.delivered[url];
	if( item==null ) {
		item = new XMLHttpRequestItem(url,null,xml);
		this.delivered[url] = item;
	};
	item.responseXML = xml;
	item.deliver();
};

/**
 * Deliver an XMLHttpRequestItem
 */
function XMLHttpRequest_deliver(item) {
	this.readyState = this.DONE;
	this.responseXML = item.responseXML;
	this.responseText = item.responseText;
	this.status = item.status;
	this.statusText = item.statusText;
	info("XMLHttpRequest delivering "+this.responseXML);
	this.onreadystatechange()
};

XMLHttpRequest.glue();

function XMLHttpRequestItem(url, responseText, responseXML) {
	this.url = url;
	this.responseText = responseText;
	this.responseXML = responseXML;
	this.listeners = new Array();
};

XMLHttpRequestItem.prototype.status = 200;
XMLHttpRequestItem.prototype.statusText = null;
XMLHttpRequestItem.prototype.responseXML = null;
XMLHttpRequestItem.prototype.responseText = null;

/** Delivers the XML data to any waiting listeners, and then removes them from the delivery list. */
function XMLHttpRequestItem_deliver() {
	if( this.responseText===null && this.responseXML===null ) return;
	var listener;
	while( this.listeners.length > 0 ) {
		listener = this.listeners.pop();
		info("XMLHttoRequestImpl Delivering ",this.responseXML);
		listener.deliver(this);
	};
};

/** Adds an XMLHttpRequest to listen for the given URL */
function XMLHttpRequestItem_addRequest(req) {
	this.listeners.push(req);
};

XMLHttpRequestItem.glue();