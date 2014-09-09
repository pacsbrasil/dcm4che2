/**
 *  LookupTable.js
 *  Version 0.5
 *  Author: BabuHussain<babuhussain.a@raster.in>
 */
function LookupTable() {
	this.huLookup;
	this.ylookup;
	this.rescaleSlope;
	this.rescaleIntercept;
	this.windowCenter;
	this.windowWidth;
	this.lutSize;
	this.calculateHULookup = calculateHULookup;
	this.calculateLookup = calculateLookup;
	this.setWindowingdata = setWindowingdata;
}

LookupTable.prototype.setData = function(wc, ww, rs, ri,bitsStored, invert) {
	this.windowCenter = wc;
	this.windowWidth = ww;
	this.rescaleSlope = rs;
	this.rescaleIntercept = ri;	
	this.lutSize = Math.pow(2, bitsStored);	
	this.invert = invert;
}

var setWindowingdata = function(wc, ww) {
	this.windowCenter = wc;
	this.windowWidth = ww;
}

function calculateHULookup() {
	this.huLookup = new Array(this.lutSize);
	for ( var inputValue = 0; inputValue <= parseInt(this.lutSize)-1; inputValue++) {
		if (this.rescaleSlope == undefined
				&& this.rescaleIntercept == undefined) {
			this.huLookup[inputValue] = inputValue;
		} else {
			this.huLookup[inputValue] = inputValue * this.rescaleSlope
					+ this.rescaleIntercept;
		}
	}
}

function calculateLookup() {
	this.ylookup=new Array(this.lutSize);
	for(var inputValue=0;inputValue<=parseInt(this.lutSize)-1;inputValue++) {
		var lutVal = (((this.huLookup[inputValue] - (this.windowCenter)) / (this.windowWidth) + 0.5) * 255.0);
         var newVal = Math.min(Math.max(lutVal, 0), 255);
         if(this.invert === true) {
             this.ylookup[inputValue] = Math.round(255 - newVal);
         } else {
             this.ylookup[inputValue] = Math.round(newVal);
       	 
         }
	}

}
