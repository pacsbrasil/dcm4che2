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
  * This class handles markup editing for various types of objects, including polylines, smoothed
  * polylines, circles, ellipses, points and text position (bounding box and anchor points.)
  * This class works on both VML and SVG shapes, and creates edit handles on the currently selected
  * object by parsing the path element that defines the shape.  Only path and text elements are handled, as
  * everything can be represented as a path or text.
  * Circle/Ellipse - both represented by elliptical arc curve commands in SVG or by quadratic beziers in VML (or quadratics in both)
  * 	One control point at center, and one at the major and minor axis points.  Could also allow arbitrary
  *     resize drag elsewhere (TBD)
  * Polylines - just a set of lines.  One control point at each lineto/moveto point, except only one for start/end.
  * Text - anchor point and bounding box either/both depending on what is available.  Needs to be further
  *   defined (maybe which are available could be in a pop-up that allows editing the text.)
  * Interpolated polylines - just edits the points that are pass through, with side affects to modify the
  * control points.  Cubic bezier in both VML and SVG.
  * Shutters - require an extra set of controls to define how the shutter is handled.  Still, this will be
  * one of hte standard controls, except ellipses, text and interpolated areas aren't allowed. 
  * 
  * On editing an object, the object ID, path information, layer information is stored to the server.  This
  * will affect whatever layers this particular GSPS applies to.
  * On creation of a new object, the new object will be posted - this will add information to the series or
  * study level for that object, but until publishing will not affect multiple GSPS objects.  Post publishing,
  * editing this GSPS object may or may not affect all the images that it was originally applied to.
  * Deletion of an object works like editing - it removes it from the current GSPS only.
  * 
  * What happens with co-dependent values, eg computed region of interest or caliper values?  Some sort of
  * meta-information that links objects to one another needs to be defined.
  * What happens with grouping of objects - this should define a layer.  The colour of a layer can be
  * changed, as can the name and visibility of layers.  
  * 
  * This class handles selection, display of the drag points, unselection and various types of updates.
  */

/** Define a markup edit instance. */
function MarkupEdit() {
	this.selected = new Object();
};

MarkupEdit.prototype.debug = debug;
MarkupEdit.prototype.info = info;
MarkupEdit.prototype.warn = warn;
MarkupEdit.prototype.handleSize = 25;

var svgns = 'http://www.w3.org/2000/svg';
var xlinkns = 'http://www.w3.org/1999/xlink';

/** Handles the mouse down functionality */
MarkupEdit.prototype.mouseDown = function(evt) {
  if( evt==null ) evt = window.event;
  if( !isLeftMouse(evt) ) return;
  var evtTarget = target(evt);
  // Get the parent before any dereferencing takes places.
  if( isTag(evtTarget,"shape") ) {
  	this.group = evtTarget.parentNode;
  	evtTarget = findByRef(evtTarget.type);
  }
  else {
  	// SVG case - need to find the svg object that contains the use that references the group we are in.
  	// TODO - get the real group (how? - iterate over all the nodes and see which one contains the clicked item?)
  	this.group = document.getElementsByTagNameNS(svgns,"image").item(0).parentNode;
  }
  this.debug("Clicked on "+evtTarget.tagName+" id="+evtTarget.id+" isTag shape="+isTag(evtTarget,"shape"));
  if(!evt.ctrlKey ) {
  	// This event adds/remove just this object
  	if( this.isSelected(evtTarget) ) {
  		this.removeSelected(evtTarget);
  	}
  	else {
  		this.addSelected(evtTarget);
  	}
  }
  else {
  	if( this.isSelected(evtTarget) ) {
  		this.clearSelected();
  	}
  	else {
  		this.clearSelected();
  		this.addSelected(evtTarget);
  	};
  };
  return true;
};

/** Adds targ to the selected set, displaying appropriate edit handles.
 * @param {Element} targ
 */
MarkupEdit.prototype.addSelected = function(targ) {
	this.debug("Selecting "+targ.id);
	this.selected[targ.id] = targ;
	// TODO Compute this based on scale, not as an absolute value.
	targ.nodeHandles = new NodeHandles(targ, this.handleSize);
	targ.nodeHandles.createSvg(this.group, this.handleSize);
};

/** Removes all selected items, also clearing all edit handles */
MarkupEdit.prototype.clearSelected = function() {
	this.debug("Clearing all selected.");
	for(var i in this.selected) {
		this.debug("Found markup edit selected item "+i);
		if( this.selected[i] && this.selected[i].nodeHandles )	this.selected[i].nodeHandles.clearSelected();
	}
	this.selected = new Object();	
};

/** Tells if targ is currently in the selection set. */
MarkupEdit.prototype.isSelected = function(targ) {
	var id = targ.id;
	this.debug(id+" is selected? "+(this.selected[id]==targ));
	return this.selected[id] == targ;
};

/** Removes targ from the selected items, also clearing any edit handles. */
MarkupEdit.prototype.removeSelected = function(targ) {
	this.debug("Removing selection from "+targ.id);
	if( this.selected[targ.id] && this.selected[targ.id].nodeHandles )	this.selected[targ.id].nodeHandles.clearSelected();
	this.selected[targ.id] = null;
};

var markupEdit = new MarkupEdit();



/** This is an inner class used by MarkupEdit to represent handles of various types. 
 * @param {Element} node
 * @param {Number} size
 */
function NodeHandles(node, size) {
	this.node = node;
	this.handleSize = size;
	this.createHandles(node);
};

NodeHandles.prototype.debug = debug;
NodeHandles.prototype.info = info;
NodeHandles.prototype.warn = warn;

/** Updates the path d (and also potentially the path element path for VML). */
NodeHandles.prototype.updateD = function() {
	var d = "";
	var i=0;
	for(i=0; i<this.cmds.length; i++ ) {
		d = d + this.cmds[i].value;
	}
	this.node.setAttributeNS(null,"d",d);
};

/** Creates the appropriate types of objects for the handles
 * @param {Node} group
 * @param {Array} handles
 * @param {Number} handleSize
 * @return {Array} of SVG objects that comprise the handles (or vml for IE)
* @r
 */
NodeHandles.prototype.createSvg = function(group) {
	var i;
	var handle;
	var rect;
	var useThis = this;
	var mdown = function (evt) { useThis.mouseDown(evt); };
	var mup = function (evt) { useThis.mouseUp(evt); };
	var mmove = function (evt) { useThis.mouseMove(evt); };
	this.handleDragging = false;
	this.group = group;
	for(i=0; i<this.handles.length; i++ ) {
		handle = this.handles[i];
		if( handle.rect ) continue;
		rect = handle.createSvg()
		group.appendChild(rect);
		addEvent(rect,"mousedown",mdown,true);
		addEvent(rect,"mouseup",mup,true);
		addEvent(rect,"mousemove",mmove,true);	
		addEvent(rect,"mouseout", mup, true);
	}
};

NodeHandles.prototype.clearSelected = function() {
	var i;
	if( this.handles ) {
		for(i=0; i<this.handles.length; i++) {
			this.handles[i].clearSelected(this.group);
		}
	}	
};

/**
 * Handles mouse down events on the handle - starts to drag, can set updates etc.
 * @param {Event} event
 */
NodeHandles.prototype.mouseDown = function(event) {
  if( this.handleDragging ) return;
  if( !event) event = window.event;
  this.debug("Mouse down on handle.");
  this.handleDragging = true;
  this.startX = event.clientX;
  this.startY = event.clientY;
};

/**
 * Handles mouse up events on the handle - stops dragging, and posts the update to the server
 */
NodeHandles.prototype.mouseUp = function(event) {
  if( !this.handleDragging ) return;
  if( !event) event = window.event;
  this.mouseMove(event);
  this.debug("Done dragging a handle.");
  this.handleDragging = false;
  var i;
  for(i=0; i<this.handles.length; i++) {
  	this.handles[i].finalizePosition();
  }
};

/**
 * Handles mouse movement on the handle.  Updates the handle position in the source data.
 * @param {Event} event
 */
NodeHandles.prototype.mouseMove = function(event) {
  if( ! this.handleDragging ) return;
  if( !event) event = window.event;
  var dx = event.clientX-this.startX;
  var dy = event.clientY-this.startY;
  var pnt;
  var scl = this.group.getAttribute("scl");
  dx = dx / scl;
  dy = dy / scl;
  this.debug("Scaled dx/dy is "+dx+","+dy+" for scaling "+scl);
  this.debug("Mouse move delta "+dx+","+dy);
  var evtTarg = target(event);
  var handle = evtTarg.handleObj;
  if( handle ) {
  	handle.invalidate(dx,dy);
  	this.updateD();
  }
};

/** Returns the index of the next letter position, -1 if past the end of the string, OR the end string length
 * if not yet past the end of the string.
 */
NodeHandles.prototype.nextLetterIndex = function (str, pos) {
	if( pos>=str.length) return -1;
	var i = pos+1;
	var ch;
	while( i<str.length ) {
		ch = str.charAt(i);
		if( (ch>='a' && ch <='z') || (ch>='A' && ch<='Z') ) return i;
		i = i+1;
	};
	return i;
};

/**
 * Gets the draw command as a string - returns the initial substring containing the upper and lower
 * characters, eg M128,256 would return M, while at128,256 would return at.  Assume at least one character.
 */
NodeHandles.prototype.drawCommand = function(str) {
	var i = 1;
	var ch;
	while( i<str.length ) {
		ch = str.charAt(i);
		if( (ch>='a' && ch <='z') || (ch>='A' && ch<='Z') ) continue;
		return str.substr(0,i);
	};
	return str;
};



/** Checkes to see if handle exists, and if it doesn't, then add it as a handle */
NodeHandles.prototype.addHandle = function(handle,cmd) {
	var handles = this.handles;
	var len = handles.length;
	var i;
	for(i=0; i<len; i++ ) {
		if( handles[i].x==handle.x && handles[i].y==handle.y ) {
			handles[i].addCmd(cmd);
			return handles[i];
		}
	}
	this.debug("Adding a handle at "+i+"="+handle);
	handles.push(handle);
	handle.addCmd(cmd);
	return handle;
};

/** Adds a new handle to an existing, fully shown object
 * @param {Array} this.cmds
 */
NodeHandles.prototype.addNewHandle = function(handle,cmd,posn) {
	if( posn<0 ) posn += this.cmds.length+1;
	if( posn<0 ) return;
	if( posn>this.cmds.length ) return;
	this.cmds.splice(posn,0,cmd);
	var useHandle = this.addHandle(handle,cmd);
	return useHandle;
};

/** Creates an array of the handle points for dragging, as well as creating the cmds list
 * associated with the handles.
 */
NodeHandles.prototype.createHandles = function(targ) {
	var dstr = targ.getAttribute("d");
	var prType = targ.getAttribute("prType");
	if( !prType ) prType = this.guessPrType(targ,dstr);
	this.debug("Looks like prType to edit is "+prType);
	if( !prType ) {
		log.info("No prType for "+targ.getAttribute("id"));
		return false;
	}
	if( ! this[prType] ) {
		this.info("o handler for prType "+prType);
		return false;
	}
	return this[prType](targ,dstr);
};

/** Guesses the type of an object.  
 * @param {Element} targ
 * @param {String} dstr
 */
NodeHandles.prototype.guessPrType = function(targ,dstr) {
	var matches = dstr.match(/[^ \t,.0-9lmLM]/);
	if( !matches ) return "Polyline";
	return null;
};

/**
 * Creates handles for polylines
 */
NodeHandles.prototype.Polyline = function(targ,dstr) {
	this.cmds = new Array();
	this.handles = new Array();
	var i = 0;
	var next = this.nextLetterIndex(dstr,i);
	var cur;
	var cmd;
	while( next>0 ) {
		cur = dstr.substr(i,next-i);
		i = next;
		next = this.nextLetterIndex(dstr,i);
		cmd = this.drawCommand(cur);
		args = cur.substr(cmd.length);
		if( cmd === "M" || cmd==="L" ) {
			cmd = new PointCmd(cmd,args);
			handle = new PointHandle(args, this.handleSize);
			this.cmds.push(cmd);
  		    this.debug("Cmd at "+cmd);
  		    this.addHandle(handle,cmd);
		}
		else {
			this.warn("Unknown handle type "+cmd + " with args "+args);
		}	
	}
	return true;
};


/** A point handle is a standard handle that adjusts one or more values all in exactly the same way
 * when it is moved.  Point handles can have types that define how some other objects move relative to 
 * them (eg center, major axis, minor axis, etc).  This must be defined by customized PointCmd objects.
 */
function PointHandle(point, handleSize, htype) {
	this.htype = htype;
	var xy = parsePoint(point);
	this.startX = xy[0];
	this.startY = xy[1];
	this.x = this.startX;
	this.y = this.startY;
	this.hHandleSize = handleSize/2;
	this.handleSize = handleSize;
	this.cmds = new Array();
};

/**
 * Finalizes point values, setting the starting point to the current one.
 */
PointHandle.prototype.finalizePosition = function()
{
	this.startX = this.x;
	this.startY = this.y;
}
/**
 * Updates the point by moving it dx,dy.  Maybe constrained in some way, eg orthogonal movement.
 * @param {Number} dx
 * @param {Number} dy
 */
PointHandle.prototype.invalidate = function(dx,dy) {
  this.x = Math.floor(this.startX + dx);
  this.y = Math.floor(this.startY + dy);
  var i;
  for(i=0; i<this.cmds.length; i++ ) {
  	this.cmds[i].invalidate(this);
  }
  this.rect.setAttributeNS(null,"x",this.x-this.hHandleSize);
  this.rect.setAttributeNS(null,"y", this.y-this.hHandleSize);
};

/**
 * Adds a command to the set of commands to update when this object changes.
 */
PointHandle.prototype.addCmd = function(cmd) {
	this.cmds.push(cmd);
};

/** Creates the SVG that can be dragged and used as a handle by the user. */
PointHandle.prototype.createSvg = function() {
	this.rect = document.createElementNS(svgns,"svg:rect");
	var rect = this.rect;
	rect.setAttributeNS(null,"x",this.x-this.hHandleSize);
	rect.setAttributeNS(null,"y", this.y-this.hHandleSize);
	rect.setAttributeNS(null,"width", this.handleSize);
	rect.setAttributeNS(null,"height", this.handleSize);
	rect.setAttributeNS(null,"style", "fill: black; stroke: white;");
	rect.handleObj = this;
	return rect;
};

/**
 * Clears the created SVG from the given group.  After this, any svg would need to be re-created.
 * @param {Element} group
 */
PointHandle.prototype.clearSelected = function(group) {
	if( this.rect ) {
		group.removeChild(this.rect);
		this.rect = null;
	};
};

/**
 * Defines a point region (either an L or an M command) to update.
 */
function PointCmd(cmd, args) {
	this.cmd = cmd;
	this.value = this.cmd + args;
};

PointCmd.prototype.debug = debug;
/**
 * Invalidates the previous value and causes a new value to be computed.
 * @param {PointHandle} handle
 */
PointCmd.prototype.invalidate = function(handle) {
	this.value = this.cmd+handle.x+","+handle.y+" ";
	this.debug("Updated point cmd value to "+this.value);
};