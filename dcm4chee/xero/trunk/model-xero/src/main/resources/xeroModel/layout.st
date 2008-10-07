/**
 * Handles a layout object.  A layout object is a templatable object that specifies what template to display it with
 * plus information on what/how to display it.
 */
function Layout(id, template) {
	if( !id ) id = "unspecified";
	var idcnt = this.idCount[id];
	if(idcnt===undefined) {
		idcnt = 1;
	}
	this.idCount[id] = idcnt+1;
	this.id = id +"-"+ idcnt;
	if( template ) this.template = template;
	if( this.isDebug ) this.debug = console.debug;
};

Layout.prototype.idCount = new Object();
Layout.prototype.isDebug = false;
Layout.prototype.debug = function() {};
Layout.prototype.template = "html/layout";
// Some things don't quite get setup right without an initial size, even though it is wrong.
Layout.prototype.setGrid = function Layout_setGrid(x,y) {
	this.gridX = x;
	this.gridY = y;
	if(! this.layouts) this.layouts = new Array();
};

Layout.prototype.used = true;

/**
 * Resizes the objects to display in the correct area
 */
Layout.prototype.resize = function Layout_resize(width,height,style) {
	this.debug("resize %s to %d,%d",this.id,width,height);
	var changed = this.resizeListener && ((this.width!=Math.floor(width)) || (this.height!=Math.floor(height)));
	this.width = Math.floor(width);
	this.height = Math.floor(height);
	this.style = style;
	if( changed ) {
		this.resizeListener();
	}
	if( ! this.gridX ) return;
	if( ! this.gridY ) return;
	
	var usedLay = this.getUsedLayouts();
	var absWidth = this.sumAbsWidth(usedLay);
	var absHeight = this.sumAbsHeight(usedLay);
	var sumRelWidth = this.sumRelWidth(usedLay);
	var sumRelHeight = this.sumRelHeight(usedLay);
	this.debug("sum rel x,y=%f,%f on %s,%d,%d", sumRelWidth, sumRelHeight, this.template,this.gridX, this.gridY);
	width = width-absWidth;
	height = height-absHeight;
	
	var i,j;
	var subWidth, subHeight;
	var layout;
	for(i=0; i<this.gridX; i++) {
		for(j=0; j<this.gridY; j++) {
			if( i+j*this.gridX>=usedLay.length ) break;
			layout = usedLay[i+j*this.gridX];
			subWidth = layout.absWidth;
			if(!subWidth ) {
				subWidth = layout.relWidth;
				if( !subWidth ) subWidth = 100;
				subWidth = subWidth*width/sumRelWidth;
				if( sumRelWidth===0 ) {
					console.warn("Setting sub-width to infinity!");
				}
			}
			// Always do width relative - it messes up the display otherwise.
			style = "width:"+Math.floor(subWidth*100/this.width)+"%;";
			
			subHeight = layout.absHeight;
			if(! subHeight ) {
				subHeight = layout.relHeight;
				if( !subHeight ) subHeight = 100;
				subHeight = subHeight * height/sumRelHeight;
				style = style+"height:"+Math.floor(subHeight*100/this.height)+"%;";
			} else {
				style = style+"height:"+subHeight+"px;";
			}
			layout.resize(subWidth, subHeight, style);
		};
	};
};

/** Checks to see if the layout information is complete */
Layout.prototype.isComplete = function Layout_isComplete() {
	if( !this.layouts ) return true;
	var lay,i,n=this.layouts.length;
	for(i=0;i<n;i++){
		lay = this.layouts[i];
		if( !lay.used ) continue;
		if( !lay.isComplete() ) return false;
	};
	return true;
};

/** Clears an relayout flags that have been set for this object and all child objects */
Layout.prototype.clearRelayout = function Layout_clearRelayout() {
	this.relayout = false;
	if(! this.layouts ) return;
	var i,n=this.layouts.length;
	for(i=0; i<n; i++) {
		this.layouts[i].clearRelayout();
	}
};

/** Return only layouts that are being used - allows them to disappear or otherwise
 * not be shown.
 */
Layout.prototype.getUsedLayouts = function Layout_getUsedLayouts() {
	var i,n=this.layouts.length;
	var ret = new Array();
	for(i=0; i<n; i++) {
		if( this.layouts[i].used ) ret.push(this.layouts[i]);
	}
	return ret;
};

/** Finds the given layout object.  Can be passed a layout object directly or the id of one. */
Layout.prototype.findLayout = function findLayout(id) {
	if( typeof(id)!=="string" ) return id;
	if( this.id===id ) return this;
	if( !this.layouts ) return;
	var ret,i,n=this.layouts.length;
	for(i=0; i<n;i++) {
		ret = this.layouts[i].findLayout(id);
		if( ret ) return ret;
	}
}; 

/** Sum all of the widths of the absolute width items */
Layout.prototype.sumAbsWidth = function Layout_sumAbsWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.absWidth ) ret = ret+layout.absWidth;
	}
	return ret;
};
Layout.prototype.sumAbsHeight = function Layout_sumAbsHeight(usedLay) {
	var ret = 0;
	var layout,y;
	for(i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.absHeight ) ret = ret+layout.absHeight;
	}
	return ret;
};

/** Sum the relative width information, using 100 for any missing item values */
Layout.prototype.sumRelWidth = function Layout_sumRelWidth(usedLay) {
	var ret = 0;
	var layout,i,n=Math.min(this.gridX,usedLay.length);
	for(i=0; i<n; i++) {
		layout = usedLay[i];
		if( layout.relWidth ) {
			ret = ret+layout.relWidth;
		} else if( ! layout.absWidth ) {
			ret = ret+100;
		}
	};
	return ret;
};
Layout.prototype.sumRelHeight = function Layout_sumRelHeight(usedLay) {
	var ret = 0;
	var layout,i;
	for(i=0; i<usedLay.length; i+=this.gridX) {
		layout = usedLay[i];
		if( layout.relHeight ) {
			ret = ret+layout.relHeight;
		} else if( !layout.absHeight ) {
			ret = ret+100;
		}
	};
	return ret;
};

/** Add another child object */
Layout.prototype.add = function Layout_add(child) {
	if( !this.layouts ) this.layouts =new Array();
	this.layouts.push(child);
};
