/**
 * Handles a layout object 
 */
function Layout(format) {
	if( !format ) format = "html/layout";
	this.format = format;
};

Layout.prototype.debug = function() {};
// Layout.prototype.debug = console.debug;

Layout.prototype.setGrid = function Layout_setGrid(x,y) {
	this.gridX = x;
	this.gridY = y;
	if(! this.layouts) this.layouts = new Array();
};

Layout.prototype.resize = function Layout_resize(width,height,style) {
	this.debug("resize to %d,%d",width,height);
	this.width = Math.floor(width);
	this.height = Math.floor(height);
	this.style = style;
	if( ! this.gridX ) return;
	if( ! this.gridY ) return; 	
	var absWidth = this.sumAbsWidth();
	var absHeight = this.sumAbsHeight();
	var numRelWidth = this.countRelWidth();
	var numRelHeight = this.countRelHeight();
	this.debug("numRel = %d,%d",numRelWidth, numRelHeight);
	width = width-absWidth;
	height = height-absHeight;
	
	var i,j;
	var subWidth, subHeight;
	var layout;
	for(i=0; i<this.gridX; i++) {
		for(j=0; j<this.gridY; j++) {
			layout = this.layouts[i+j*this.gridX];
			if( !layout ) break;
			subWidth = layout.absWidth;
			if(!subWidth ) {
				subWidth = layout.relWidth;
				if( !subWidth ) subWidth = 100/numRelWidth;
				subWidth = subWidth*width/100;
			}
			
			subHeight = layout.absHeight;
			if(! subHeight ) {
				subHeight = layout.relHeight;
				if( !subHeight ) subHeight = 100/numRelHeight;
				subHeight = subHeight * height/100;
			}
			style = "width:"+Math.floor(subWidth*100/this.width)+"%; height:"+Math.floor(subHeight*100/this.height)+"%;";
			layout.resize(subWidth, subHeight, style);
		};
	};
};

/** Sum all of the widths of the absolute width items */
Layout.prototype.sumAbsWidth = function Layout_sumAbsWidth() {
	var ret = 0;
	var layout,x;
	for(x=0; x<this.gridX; x++) {
		layout = this.layouts[x];
		if( layout.absWidth ) ret = ret+layout.absWidth;
	}
	return ret;
};
Layout.prototype.sumAbsHeight = function Layout_sumAbsHeight() {
	var ret = 0;
	var layout,y;
	for(y=0; y<this.gridY; y++) {
		layout = this.layouts[y*this.gridX];
		if( layout.absHeight ) ret = ret+layout.absHeight;
	}
	return ret;
};

/** Count the number of relative width items (those that are not absolute width) */
Layout.prototype.countRelWidth = function Layout_countRelWidth() {
	var ret = 0;
	var layout,i;
	for(i=0; i<this.gridX; i++) {
		if( ! this.layouts[i].absWidth ) ret++;
	};
	return ret;
};
Layout.prototype.countRelHeight = function Layout_countRelHeight() {
	var ret = 0;
	var layout,i;
	for(i=0; i<this.gridY; i++) {
		if( ! this.layouts[i*this.gridX].absHeight ) ret++;
	};
	if( ret===0 ) ret = 1;
	return ret;
};
