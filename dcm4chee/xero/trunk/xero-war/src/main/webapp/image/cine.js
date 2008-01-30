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

/** This class handles CINE display control - start/stop end points, rate, and loading of images.*/ 
function Cine(lookAhead) {
	this.lookAhead = lookAhead;
	var usethis = this;
 	this.callDisplayInterval = function() {
 		usethis.displayInterval.apply(usethis,arguments);
 	};
};
Cine.prototype.rate = 15;
Cine.prototype.interval=1000/Cine.prototype.rate;
Cine.prototype.started=false;
Cine.prototype.readAhead=4;
 
/** Sets the number of frames per second to display at. */
function Cine_setRate(rate) {
 	this.rate = rate;
};
Cine.prototype.setRate=Cine_setRate;

/** Starts the CINE playing - needs to pre-load some amount of information */
function Cine_start() {
	// TODO - ensure that the image meta-data is loaded for at least X images beyond now.  Wait till
	// at least the first readAhead entries are available before proceeding.
	// This section starts fetching the first readAhead entries. 
	var i,n;
	this.alreadyRead = 0;
	var ir;
	var p = this.lookAhead.viewPosition;
	this.imgs = this.lookAhead.getImageCount();
	var n = this.readAhead*2;
	if( n>this.imgs ) {
		this.readAhead = Math.floor(this.imgs/2);
		n = this.imgs;
	};
	info("Set read ahead to "+this.readAhead+" fetching n="+n+" on "+this.imgs+" images");
	// Just use the display interval code to start the fetching.
	this.displayInterval();
	this.interval = window.setInterval(this.callDisplayInterval, 1000/this.rate);
};
Cine.prototype.start=Cine_start;

/** Gets the fetch position based on the look ahead index. */
function Cine_fetchPosn(i) {
   return (1+i+this.lookAhead.viewPosition) % this.imgs;	
};
Cine.prototype.fetchPosn = Cine_fetchPosn;

/** Returns whether or not the CINE is started yet.  Just because start is called doesn't mean the CINE is actually running & started yet */
function Cine_isStarted() {
	return this.started;
};
Cine.prototype.isStarted=Cine_isStarted;

/** Causes the next image to be displayed, assuming it is ready. */
function Cine_displayInterval() {
	info("displayInterval called.");
	var ir,fp,i,complete;
	var n=this.readAhead*2;
	var loaded = 0;
	for(i=0; i<n; i++) {
		fp = this.fetchPosn(i);
		ir = this.lookAhead.getImageRef(fp);
		if( ir.isLoaded() ) loaded++;
		if( i==this.readAhead ) {
			complete = loaded;
		}
		if( !ir.isFetching() ) {
			ir.fetch();
		}
	}
	fp = this.fetchPosn(0);
	ir = this.lookAhead.getImageRef(fp);
	info("read ahead completed "+complete+" total "+loaded);
	if( complete==this.readAhead ) {
		this.started = true;
		if( loaded==complete ) {
			info("Could increase frame rate if it is down now...");
		}
	}
	else if( this.started ) {
		warn("Missing "+(this.lookAhead)+ " TODO - decrease frame rate")
	}
	// TODO - handle some way to skip an image when it fails or just takes forever
	if( ir.isLoaded() && this.started ) {
		info("Is loaded, and setting view position to "+fp);
		this.lookAhead.setViewPosition(fp);
	}; 
};
Cine.prototype.displayInterval = Cine_displayInterval;

/** Returns how many read-ahead items are available */
function Cine_getReadAhead() {
	return this.readAhead;
};
Cine.prototype.getReadAhead=Cine_getReadAhead;

/**
 * Starts playing the CINE loop.
 */
function playCine(cmdButton) {
	alert("Play cine.");
	return false;
};

