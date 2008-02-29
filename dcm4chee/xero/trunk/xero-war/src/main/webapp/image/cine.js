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
Cine.prototype.started=false;
// How many images to read ahead
Cine.prototype.readAhead=25;
// How many items are required to have read before starting and before starting to slow down
Cine.prototype.requireAhead = 12;
// The concurrent number of loads.
Cine.prototype.concurrent = 3;
Cine.prototype.debug=debug;
// Start with no images until the look ahead is defined.
Cine.prototype.imgs = 0;
Cine.prototype.playingCine = new Array();
Cine.prototype.showRate = 75;
Cine.prototype.lastShowRate = 0;
Cine.prototype.minTime = 1000;
Cine.prototype.maxTime = 0;
Cine.prototype.avgTime = 0;
Cine.prototype.degradeRate = 1;
Cine.prototype.complete = 0;
Cine.prototype.degradeCount = 0;
/** Corrects for slowness in the interval so that the actual frame rate is closer to the
 * set frame rate.
 * TODO Compute this dynamically.
 */
Cine.prototype.correctInterval = 1;
 
/** Sets the number of frames per second to display at. */
Cine.prototype.setRate=function Cine_setRate(rate) {
	// Use 1.25 to allow for the fact that the actual interval calling rate is slower
	// than the specified one...
 	this.rate = rate;
 	if( this.readAhead < rate*2 ) this.readAhead = rate*2;
 	var intvl = (this.correctInterval * 1000) / (this.rate * this.degradeRate);
 	info("Interval = "+intvl+" for rate="+this.rate);
 	if( this.interval!==undefined ) {
 		window.clearInterval(this.interval);
		this.interval = window.setInterval(this.callDisplayInterval, intvl);
 	}
};

/** Sets the degrade rate - the amount below 1 that the CINE is being played at in order to
 * be able to play smoothly.
 */
Cine.prototype.setDegradeRate=function Cine_setDegradeRate(dr) {
	if( dr > 1 ) dr = 1;
	if( dr < 0.1 ) dr = 0.1;
	this.degradeRate = dr;
	this.setRate(this.rate);
}

/** Starts the CINE playing - needs to pre-load some amount of information */
Cine.prototype.start=function Cine_start() {
	if( this.interval!==undefined ) return;
	// Just use the display interval code to start the fetching.
	this.stopped = false;
	this.degradeRate = 1;
	this.lastShowTime = new Date().getTime();
	this.displayInterval();
	this.interval = window.setInterval(this.callDisplayInterval, 1000/this.rate);
	this.playingCine.push(this);
};


Cine.prototype.initReadAhead = function Cine_initReadAhead() {
	var i;
	this.alreadyRead = 0;
	var ir;
	var p = this.lookAhead.viewPosition;
	this.imgs = this.lookAhead.getImageCount();
	if( this.imgs==0 ) {
		info("There should be some images defined.");
		return;
	}
	info("Using read ahead to "+this.readAhead+" on "+this.imgs+" images");
};

/** Gets the fetch position based on the look ahead index. */
Cine.prototype.fetchPosn = function Cine_fetchPosn(i) {
   return (1+i+this.lookAhead.viewPosition) % this.imgs;	
};


/** Returns whether or not the CINE is started yet.  Just because start is called doesn't mean the CINE is actually running & started yet */
Cine.prototype.isStarted=function Cine_isStarted() {
	return this.started;
};

Cine.prototype.stop=function Cine_stop() {
   if( this.stopped ) return;
   this.stopped = true;
   window.clearInterval(this.interval);
   this.interval = undefined;
};

/** Causes the next image to be displayed, assuming it is ready. */
function Cine_displayInterval() {
	if( this.stopped ) return;
	if( this.lookAhead.images===undefined ) {
		info("Images not yet defined - waiting till fetch is fully called.");
		return;
	}
	if( this.imgs===0 ) {
		this.initReadAhead();
		if( this.imgs==0 ) {
			info("Images should not be zero after reading some meta-data.");
			return;
		}
	}
	var ir,fp,i;
	var n=this.readAhead;
	if( n > this.imgs ) n = this.imgs;
	if( this.requireAhead > this.readAhead ) this.requireAhead = this.readAhead;
	var loaded = 0;
	this.debug("Trying to read ahead ",n," images.");
	var someUnloaded = 0;
	for(i=this.complete; i<n; i++) {
		fp = this.fetchPosn(i);
		ir = this.lookAhead.getImageRef(fp);
		if( ir.isLoaded() ) {
			loaded++;
			if(someUnloaded===0 ) {
				this.complete++;
			}
			continue;
		}
		someUnloaded++;
		if( (!ir.isFetching())  ) {
			ir.fetch();
		}
		if( someUnloaded >= this.concurrent ) break;
	}
	fp = this.fetchPosn(0);
	ir = this.lookAhead.getImageRef(fp);
	this.debug("read ahead completed "+this.complete+" total "+loaded);
	if( this.complete>= this.requireAhead ) {
		this.degradeCount = 0;
		this.started = true;
		if( this.degradeRate < 1 && this.complete > this.requireAhead+this.concurrent/2) {
			this.setDegradeRate(this.degradeRate * 1.025);
			info("Increasing frame rate - all images loaded: "+this.degradeRate);
		}
	}
	else if( this.started ) {
		this.degradeCount++;
		if( this.degradeCount < this.requireAhead/2 ) {
			this.setDegradeRate(this.degradeRate / 1.01);
		}
		else {
			this.setDegradeRate(this.degradeRate / 1.025);
		}
		warn("Missing "+(this.lookAhead)+ " - decrease frame rate:"+this.degradeRate);
	}
	if( this.started ) {
		info("fp="+fp+" complete="+this.complete+" someUnloaded="+someUnloaded);
		if( ir.isLoaded() ) {
			this.lookAhead.setViewPosition(fp);
			this.complete = 0;
			this.showRateInfo();
		}
	}; 
};
Cine.prototype.displayInterval = Cine_displayInterval;

/** Shows information about the actual rate that the CINE is being played at. */
Cine.prototype.showRateInfo = function Cine_showRateInfo() {
	this.lastShowRate = this.lastShowRate + 1;
	var now = new Date().getTime();
	if( this.lastShowRate > 1 ) {
		var tm = now - this.lastTime;
		if( tm < this.minTime ) this.minTime = tm;
		if( tm > this.maxTime ) this.maxTime = tm;
		var intvl = 1000/this.rate;
		var error = intvl - tm;
		if( error < 0 ) error = -error;
		this.avgTime = this.avgTime +error;
	}
	this.lastTime = now;
	if( this.lastShowRate < this.showRate ) return;
	var now = new Date().getTime();
	var delta = now - this.lastShowTime;
	this.lastShowTime = now;
	if( delta==0 ) {
		warn("Time doesn't appear to be changing.");
		return;
	};
	var rate = 1000*this.lastShowRate/delta;
	this.avgTime = this.avgTime / delta;
	info("Average display rate over "+this.lastShowRate+" items is "+rate+" in "+delta+" ms"+" min="+this.minTime+" max="+this.maxTime+" avg error="+this.avgTime);	
	this.minTime = 10000;
	this.maxTime = 0;
	this.avgTime = 0;
	this.lastShowRate = 0;
};

/** Returns how many read-ahead items are available */
function Cine_getReadAhead() {
	return this.readAhead;
};
Cine.prototype.getReadAhead=Cine_getReadAhead;

/**
 * Starts playing the CINE loop.
 */
function playCine(e) {
	info(1);
	var cmdButton = target(e);
	info("Getting node for series layout:");
	var lay = getNodeForSeriesLayout(cmdButton);
	info("Got layout node "+lay);
	if( lay.lookAhead===undefined ) {
		lay.lookAhead = new LookAheadImage(cmdButton);
		info( 2.3 );
		lay.lookAhead.init(lay);
	}
	info(3);
	if( lay.cine===undefined ) {
		lay.cine = new Cine(lay.lookAhead);
	};
	var speedEl = cmdButton.ownerDocument.getElementById("cineSpeed");
	var speed = 4;
	if( speedEl!==undefined && speedEl!==null ) {
		speed = speedEl.getAttribute("value");
	}
	info("Initialized to play CINE at ",speed," fps.");
	lay.cine.setRate(speed);
	lay.cine.start();
	info("Started CINE.");
	return evtPreventDefault(e);
};

/** Discontinues playing CINE */
function stopCine(cmdButton) {
	var lay = getNodeForSeriesLayout(cmdButton);
	if( lay.cine === undefined ) return;
	lay.cine.stop();	
};

function cineRate(cmdButton,rel) {
	var speedEl = cmdButton.ownerDocument.getElementById("cineSpeed");
	if( speedEl!==undefined && speedEl!==null ) {
		speed = speedEl.getAttribute("value");
		speed = speed * rel;
		if( speed < 0.1 ) speed = 0.1;
		if( speed > 60 ) speed = 60;
		speedEl.setAttribute("value", ""+speed);
	}
	for( var i in Cine.prototype.playingCine) {
		Cine.prototype.playingCine[i].setRate (speed);
	};
};
