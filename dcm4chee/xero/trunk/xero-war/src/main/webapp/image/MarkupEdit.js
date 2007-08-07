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

MarkupEdit.prototype.debug = info;
MarkupEdit.prototype.info = info;
MarkupEdit.prototype.warn = info;

MarkupEdit.prototype.svgns = 'http://www.w3.org/2000/svg';
MarkupEdit.prototype.xlinkns = 'http://www.w3.org/1999/xlink';

/** Handles the mouse down functionality */
MarkupEdit.prototype.mouseDown = function(evt) {
  if( evt==null ) evt = window.event;
  if( !isLeftMouse(evt) ) return;
  var evtTarget = target(evt);
  // Get the parent before any dereferencing takes places.
  var group;
  if( isTag(evtTarget,"shape") ) {
  	group = evtTarget.parentNode;
  	evtTarget = findByRef(evtTarget.type);
  }
  else {
  	// SVG case - need to find the svg object that contains the use that references the group we are in.
  	// TODO - get the real group (how? - iterate over all the nodes and see which one contains the clicked item?)
  	group = document.getElementsByTagNameNS(this.svgns,"image").item(0).parentNode;
  }
  this.debug("Clicked on "+evtTarget.tagName+" id="+evtTarget.id+" isTag shape="+isTag(evtTarget,"shape"));
  if(!evt.ctrlKey ) {
  	// This event adds/remove just this object
  	if( this.isSelected(evtTarget) ) {
  		this.removeSelected(evtTarget);
  	}
  	else {
  		this.addSelected(group,evtTarget);
  	}
  }
  else {
  	if( this.isSelected(evtTarget) ) {
  		this.clearSelected();
  	}
  	else {
  		this.clearSelected();
  		this.addSelected(group,evtTarget);
  	};
  };
  return true;
};

/** Adds tar to the selected set, displaying appropriate edit handles.
 * @param {Element} group
 * @param {Element} targ
 */
MarkupEdit.prototype.addSelected = function(group,targ) {
	this.debug("Selecting "+targ.id);
	this.selected[targ.id] = targ;
	// TODO Compute this based on scale, not as an absolute value.
	var handleSize = 25;
	var handles = new EditHandle(targ, handleSize);
	handles.createHandleSvg(group, handleSize);
};

/** Removes all selected items, also clearing all edit handles */
MarkupEdit.prototype.clearSelected = function() {
	this.debug("Clearing all selected.");
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
	this.selected[targ.id] = null;
};

var markupEdit = new MarkupEdit();



/** This is an inner class used by MarkupEdit to represent handles of various types. 
 * @param {Element} node
 * @param {Number} size
 */
function EditHandle(node, size) {
	this.node = node;
	this.handleSize = size;
	this.handles = this.getHandles(node);
};

EditHandle.prototype.debug = info;
EditHandle.prototype.info = info;
EditHandle.prototype.warn = info;

EditHandle.prototype.svgns = 'http://www.w3.org/2000/svg';
EditHandle.prototype.xlinkns = 'http://www.w3.org/1999/xlink';

/** Returns the updated path d */
EditHandle.prototype.getD = function() {
};

/** Creates the appropriate types of objects for the handles
 * @param {Node} group
 * @param {Array} handles
 * @param {Number} handleSize
 * @return {Array} of SVG objects that comprise the handles (or vml for IE)
* @r
 */
EditHandle.prototype.createHandleSvg = function(group) {
	var ret = new Array(this.handles.length);
	var i;
	var handle;
	var rect;
	var useThis = this;
	var mdown = function (evt) { useThis.mouseDown(evt); };
	var mup = function (evt) { useThis.mouseUp(evt); };
	var mmove = function (evt) { useThis.mouseMove(evt); };
	this.handleDragging = false;
	for(i=0; i<this.handles.length; i++ ) {
		handle = this.handles[i];
		rect = document.createElementNS(this.svgns,"svg:rect");
		rect.setAttributeNS(null,"x",handle.x-this.handleSize/2);
		rect.setAttributeNS(null,"y", handle.y-this.handleSize/2);
		rect.setAttributeNS(null,"width", this.handleSize);
		rect.setAttributeNS(null,"height", this.handleSize);
		rect.setAttributeNS(null,"style", "fill: black; stroke: white;");
		rect.addEventListener("mousedown",mdown,true);
		rect.addEventListener("mouseup",mup,true);
		rect.addEventListener("mousemove",mmove,true);
		this.debug("Created rect "+rect+" about to append.");
		group.appendChild(rect);
		ret[i] = rect;
	}
	return ret;
};

/**
 * Handles mouse down events on the handle - starts to drag, can set updates etc.
 * @param {Event} event
 */
EditHandle.prototype.mouseDown = function(event) {
  if( this.handleDragging ) return;
  this.debug("Mouse down on handle.");
  this.handleDragging = true;
  this.startX = event.clientX;
  this.startY = event.clientY;
};

/**
 * Handles mouse up events on the handle - stops dragging, and posts the update to the server
 */
EditHandle.prototype.mouseUp = function(event) {
  if( !this.handleDragging ) return;
  info("Done dragging a handle.");
  this.handleDragging = false;
};

/**
 * Handles mouse movement on the handle.  Updates the handle position in the source data.
 * @param {Event} event
 */
EditHandle.prototype.mouseMove = function(event) {
	if( ! this.handleDragging ) return;
	var dx = event.clientX-this.startX;
	var dy = event.clientY-this.startY;
	this.debug("Mouse move delta "+dx+","+dy);
};

/** Returns the index of the next letter position, -1 if past the end of the string, OR the end string length
 * if not yet past the end of the string.
 */
EditHandle.prototype.nextLetterIndex = function (str, pos) {
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
EditHandle.prototype.drawCommand = function(str) {
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
EditHandle.prototype.addHandle = function(handles,handle) {
	var len = handles.length;
	var i;
	for(i=0; i<len; i++ ) {
		if( handles[i].x==handle.x && handles[i].y==handle.y ) return false;
	}
	this.debug("Adding a handle at "+i+"="+handle);
	handles[i] = handle;
	return true;
};

/** Returns an array of the handle points for dragging. */
EditHandle.prototype.getHandles = function(targ) {
	var dstr = targ.getAttribute("d");
	var handles = new Array();
	var i = 0;
	var next = this.nextLetterIndex(dstr,i);
	var cur;
	var cmd;
	var handle;
	while( next>0 ) {
		cur = dstr.substr(i,next-i);
		i = next;
		next = this.nextLetterIndex(dstr,i);
		cmd = this.drawCommand(cur);		
		args = cur.substr(cmd.length);
		if( cmd === "M" || cmd==="L" ) {
			handle = new PointHandle(args);
  		    this.debug("Handle at "+handle);
  		    this.addHandle(handles,handle);
		}
		else {
			this.warn("Unknown handle type "+cmd + " with args "+args);
		}		
	}
	return handles;
};


/** A point handle is a standard handle that adjusts one or more values all in exactly the same way
 * when it is moved.
 */
function PointHandle(point) {
	var xy = point.match(/\d+/g);
	this.startX = parseInt(xy[0]);
	this.startY = parseInt(xy[1]);
	this.x = this.startX;
	this.y = this.startY;
};
