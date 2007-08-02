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

/** Handles the mouse down functionality */
MarkupEdit.prototype.mouseDown = function(evt) {
  if( evt==null ) evt = window.event;
  if( !isLeftMouse(evt) ) return;
  var evtTarget = target(evt);
  if( isTag(evtTarget,"use") || isTag(evtTarget,"shape") ) return true;
  this.debug("Clicked on "+evtTarget.tagName+" id="+evtTarget.id);
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
  	}
  }
  return true;
};

/** Returns an array of the handle points for dragging. */
MarkupEdit.prototype.getHandlePoints = function(targ) {
	var dstr = targ.getAttribute("d");
	this.debug("Handle sources are "+dstr);
};

/** Adds targ to the selected set, displaying appropriate edit handles. */
MarkupEdit.prototype.addSelected = function(targ) {
	this.debug("Selecting "+targ.id);
	this.selected[targ.id] = targ;
	this.getHandlePoints(targ);
};

/** Removes all selected items, also clearing all edit handles */
MarkupEdit.prototype.clearSelected = function() {
	this.debug("Clearing all selected.");
	this.selected = new Object();	
};

/** Tells if targ is currently in the selection set. */
MarkupEdit.prototype.isSelected = function(targ) {
	this.debug(targ.id+" is selected? "+(this.selected[targ.id]==targ));
	return this.selected[targ.id] == targ;
};

/** Removes targ from the selected items, also clearing any edit handles. */
MarkupEdit.prototype.removeSelected = function(targ) {
	this.debug("Removing selection from "+targ.id);
	this.selected[targ.id] = null;
};

var markupEdit = new MarkupEdit();

