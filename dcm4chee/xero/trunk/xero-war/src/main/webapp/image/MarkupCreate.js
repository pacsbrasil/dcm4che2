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
  * MarkupCreate is a class that allows creation of various types of markups. 
  * The user selects the create markup mode, and then starts clicking on the image.
  * The length of the caliper and the caliper itself should be drawn.  
  */
function MarkupCreate() {
};

MarkupCreate.prototype = new MarkupEdit();
MarkupCreate.prototype.debug = debug;
MarkupCreate.prototype.prObj = new Polyline();

/**
 * Convert from event to SVG/VML image based coordinates (NOT display coordinates)
 * Must have group (SVG element or VML group element) defined.
 * @return [ x, y ]
 */
MarkupCreate.prototype.findCoords = function(event) {
	var scl = this.group.getAttribute("scl");
	// Non-standard attribute, but always present for VML compatibility. 
	var origin = parsePoint(this.group.getAttribute("coordorigin"));
	var x,y;
	if( event.layerX ) {
	  x = Math.floor(event.layerX / scl + origin[0]);
	  y = Math.floor(event.layerY / scl + origin[1]);
	} else {
	  x = Math.floor(event.offsetX / scl);
	  y = Math.floor(event.offsetY / scl);
	}
	this.debug("findCoords layer x,y / scl = "+x+","+y+" scl="+scl+" origin="+origin);
	return [x , y];
};

/**
 * Add a point to an existing poly-line being created, or add a new starting point.
 */
MarkupCreate.prototype.mouseDown = function(event) {
	if( !event) event = window.event;
	if( !isLeftMouse(event) ) return;
	if( this.creating ) {
		this.extendObject(event);
	}
	else {
		this.createObject(event);
	}
};


NodeHandles.prototype.createPolyline = NodeHandles.prototype.Polyline;

/**
 * Extends a previously created object by the clicked/being clicked on point.
 * @param {Event} event
 */
MarkupCreate.prototype.extendObject = function(event) {
	var evtTarget = target(event);
	var clickGroup = evtTarget.parentNode;
	// TODO -finalize the previous item, and create a new one rather than ignoring.
	if( clickGroup != this.group ) {
		this.info("Should finalize old item and start a new item.");
		return;
	}
	var coords = this.findCoords(event);
	this.debug("Mouse down on markup extend at "+coords);
	this.prObj.extendObject(this,event,coords);
};
	
Polyline.prototype.extendObject = function(create, event, coords)  {
	var point = ""+coords;
	var cmd = new PointCmd("L",point);
	var handle = new PointHandle(point,create.handleSize);
	var posn = -1;
	if( this.closed ) posn = -2;
	var newHandle = create.creating.nodeHandles.addNewHandle(handle,cmd,posn);
	create.creating.nodeHandles.createSvg(create.group);
	create.creating.nodeHandles.mouseDown(event);
	create.creating.nodeHandles.updateD();
};

/**
 * Creates an object of the appropriate current type (caliper in this case.)
 */
MarkupCreate.prototype.createObject = function(event) {
	var evtTarget = target(event);
	this.group = evtTarget.parentNode;
	var scl = this.group.getAttribute("scl");
	this.handleSize = Math.floor(this.defaultHandleSize/scl+1);
	var coords = this.findCoords(event);
	this.debug("Mouse down on markup create at "+coords);
	this.prObj.createObject(this,event,coords);
};

/** This handles the creation code specific to Polylines. */
Polyline.prototype.createObject = function(create,event,coords) {
	if( browserName==="IE") {
		create.creating = document.createElement("v:shape");	
	}
	else {
  		create.creating = document.createElementNS(svgns,"svg:path");
	}
	var d = "M"+coords+" L"+(coords[0]+1)+","+(coords[1]+1);
	this.debug("Initial d="+d);
	if( this.closed ) {
 		d = d+" L"+coords;
  		create.creating.setAttribute("style", "fill: green; stroke: white; stroke-width: 1;");
 		create.creating.setAttribute("filled", "t");
	}
	else {
		create.creating.setAttribute("filled", "f");
  		create.creating.setAttribute("style", "fill: none; stroke: white; stroke-width: 1;");
	}
	var csize, corg;
	if( browserName==="IE") {
		create.creating.setAttribute("strokecolor", "white");
		create.creating.setAttribute("fillcolor", "green");
		create.creating.setAttribute("path",d);
		csize = parsePoint(create.group.getAttribute("coordsize"));
		corg = parsePoint(create.group.getAttribute("coordorigin"));
		// This value will be fine for the displayed area, but if the user pans to add more 
		// markup, then it won't be big enough - that doesn't seem to make any difference, however.
		csize[0] = csize[0] + corg[0];
		csize[1] = csize[1] + corg[1];
		create.creating.setAttribute("coordsize", csize);
		create.creating.style.width  = csize[0];
		create.creating.style.height = csize[1];
	}
	create.creating.setAttribute("d", d);
	create.creating.setAttribute("prType",this.prType);
	create.creating.setAttribute("id",this.prType+Math.random());
	create.group.appendChild(create.creating);
	create.addSelected(create.creating);
	create.creating.nodeHandles.mouseDown(event);
};

var markupCreate = new MarkupCreate(); 