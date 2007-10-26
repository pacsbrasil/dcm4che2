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
 * This class handles CINE display control - start/stop end points, rate, and loading of images.
 */ 
 
 function Cine() {
 	this.imageCount = 0;
 };
 
 
/** Initializes the Cine object to play CINE on the given SVG Object.
 * Only a single SVG frame is updated at a time, so playing CINE is done on one particular frame.  Once
 * the CINE is stopped, then the "current" frame becomes the one being displayed, +/- the offset to allow it
 * to display in the given location.
 * Not all data is changed on every image update - the items that are changed are:
 * 	1. Window level - changed to be the current user-selected or system selected window level
 * 	2. Rows/columns/quality - this is changed based on the download speed
 * 	3. Image Markup - this will be changed on every image
 *  4. Displayed image - this will often, but not always change - the same CINE code can theoretically be used to drive
 *     overall display location or a combination time/spatial location.
 * 
 * The things that are held constant are:
 * 	1. Region - the same sub-region/viewport area/magnification is used for every image displayed.
 * 		Should different images have different resolutions, then the same % region will be used - eg, the bottom right quadrant might be the visible region.
 * 		This may cause the image to "jump" when stop is pressed if it has a different view type in the GSPS
 * 		etc than the original image that was started with.
 * 	2. Display markup - this will not likely be updated - it isn't part of the "viewport", but rather is above
 * 		that level.
 */
Cine.prototype.initForViewport = function() {
};