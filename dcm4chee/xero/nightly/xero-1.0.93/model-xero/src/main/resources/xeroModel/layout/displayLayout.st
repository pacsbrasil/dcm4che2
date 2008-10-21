/**
 * Contains the layout object definitions for the display page.
 * Two types of over-rides can be added to the display layout area - things that are defined in the
 * metadata can be directly added to for the specific version being rendered.  For things not in the
 * metadata, some or all methods can be over-ridden in another javascript file included at higher priority
 * than this one.
 */

function PatientLayout() {
	this.baseConstructor("PatientLayout");
};

PatientLayout.prototype = new Layout("patientLayout");
PatientLayout.prototype.baseConstructor = Layout;
PatientLayout.prototype.gridX = 2;
PatientLayout.prototype.gridY = 1;
// Add some initial size to handle the border.
PatientLayout.prototype.heightEdge = 8;
PatientLayout.prototype.widthEdge = 6;

/** Initialize the patient layout object - lazy, and only succeed if the navigate object is available.
 */
PatientLayout.prototype.init = function PatientLayout_init() {
	if( this.navigate==null ) return false;
	if( this.layouts && this.layouts.length>0 ) return true;
	if( model.IS_IE ) this.widthEdge = 12;
	var patientMenu = new Layout("patientMenu", "image/patientMenu");
	var childMerged = this.navigate.addChildListener(patientMenu,PatientMergedListener,0); 
	this.tabMenu = patientMenu;
	
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			var nth = j*this.gridX+i;
			var studyArea = new StudyAreaLayout(childMerged, nth);
			this.add(studyArea);
		}
	}

	return true;
};

/** Create the study layouts containing the study tray and the image area.  Not lazy. */
function StudyAreaLayout(navigate,nth) {
	this.baseConstructor("studyArea"+nth);

	var studyTray = new Layout("studyTray-"+nth,"image/studyTray" );
	var childMerged = navigate.addChildListener(studyTray,StudyMergedListener,nth);
	navigate.addChildListener(this,StudyMergedListener,nth);
	studyTray.absWidth = 135;
	var seriesAreas = new SeriesAreasLayout(childMerged,nth);
	studyTray.nth=nth;
	
	// TODO - compute east/west addition point.
	var even = ((nth %2)==0);
	if( even ) {
		this.add(studyTray);
	}
	this.add(seriesAreas);
	if( !even ) {
		this.add(studyTray);
	}
};

StudyAreaLayout.prototype = new Layout();
StudyAreaLayout.prototype.baseConstructor = Layout;
StudyAreaLayout.prototype.gridX = 2;
StudyAreaLayout.prototype.gridY = 2;
StudyAreaLayout.prototype.displayClass="studyArea";

function SeriesAreasLayout(navigate,nth) {
	this.baseConstructor("seriesAreas"+nth);
	navigate.addListener(this,StudyMergedListener);
	this.nth = nth;
	
	var i,j,toolbar, imageArea, seriesArea;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			nth = j*this.gridX+i;
			seriesArea = new Layout("seriesArea","html/layout");
			this.add(seriesArea);
			seriesArea.setGrid(1,2);
			toolbar = new Layout("seriesToolbar-"+nth,"image/seriesToolbar");
			toolbar.parent = seriesArea;
			navigate.addChildListener(toolbar,SeriesMergedListener,nth);
			toolbar.absHeight = 25;
			imageAreas = new ImageAreasLayout(toolbar.navigate);
			toolbar.imageAreas = imageAreas;
			seriesArea.add(toolbar);
			seriesArea.add(imageAreas);
		};
	};
};

SeriesAreasLayout.prototype = new Layout();
SeriesAreasLayout.prototype.baseConstructor = Layout;
SeriesAreasLayout.prototype.gridX = 2;
SeriesAreasLayout.prototype.gridY = 2;


/** Create an images areas object */
function ImageAreasLayout(navigate) {
	this.baseConstructor("imagesAreas");
	navigate.addListener(this,ImageAreaMergedListener);
	this.used = false;
	this.origGridX = this.gridX;
	this.origGridY = this.gridY;
	var useThis = this;

	this.mousewheel = function(evt) {
		useThis.debug("Mouse wheel image area called.");
		return controller.navWheelImg(useThis,evt);
	};
	
	var i,j,layout;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			layout = new ImageLayout("imageLay-"+i+","+j);
			navigate.addChildListener(layout,ImageMergedListener,j*this.gridX+i);
			layout.resizeListener = ImageResizeListener;
			layout.x = i;
			layout.y = j;
			this.add(layout);
		};
	};
};

ImageAreasLayout.prototype = new Layout(null,"image/imageArea");
ImageAreasLayout.prototype.baseConstructor = Layout;
ImageAreasLayout.prototype.gridX = 2;
ImageAreasLayout.prototype.gridY = 2;


/** The listener for the patient information */
function PatientMergedListener(navigate) {
	this.navigate = navigate;
	if( navigate ) {
		this.patient = navigate.external;
	}
};

/** This defines the listeners for navigate changes to the layout */
function StudyMergedListener(navigate) {
	this.navigate = navigate;
	this.used = (navigate!=null && navigate.external!=null);
	if( navigate ) {
		this.study = navigate.external;
	}
};

/** This defines the listeners for navigate changes to the layout */
function SeriesMergedListener(navigate) {
	this.navigate = navigate;
	var changed = this.used;
	this.used = (navigate!=null && navigate.external!=null);
	changed = (changed!=this.used);
	if( navigate ) console.info("Changing series on %s to %d",this.id, navigate.id);
	if( changed && this.parent) {
		this.parent.relayout = true;
	}
	if( navigate ) {
		this.series = navigate.external;
	}
	this.relayout = true;
};

/** Defines the image area navigate listener to reset the grid size and
 * enable/disable child elements as appropriate.
 */
function ImageAreaMergedListener(navigate) {
	this.navigate = navigate;
	if(!navigate) return;
	if( navigate.external===this.series ) {
		return;
	}
	this.relayout = true;
	this.series = navigate.external;
	if( !this.series ) {
		this.used = false;
		return;
	}
	this.used = true;
	var n,imageCount = navigate.external.Viewable;
	if( imageCount<=1 ) {
		this.gridX = 1;
		this.gridY = 1;
	} else {
		this.gridX = this.origGridX;
		this.gridY = this.origGridY;
		n = this.gridX * this.gridY;
		while(n>imageCount) {
			if( this.gridX>1 && this.gridY>1 ) {
				n = (this.gridX-1)*(this.gridY-1);
				if( n < imageCount ) {
					// Try removing the bottom row as well
					n = this.gridX * (this.gridY-1);
					if( n>=imageCount ) this.gridY--;
					break;
				}
				this.gridX = this.gridX-1;
				this.gridY = this.gridY-1;
			} else if( this.gridX>1 ) {
				this.gridX = imageCount;
				break;
			} else if( this.gridY>1 ) {
				this.gridY = imageCount;
				break;
			} else break;
		}
	}
	n=this.layouts.length;
	var i;
	for(i=0; i<n; i++) {
		this.layouts[i].used = (i < this.gridX * this.gridY);
	}
}; 

/** This defines the listeners for navigate changes to the layout */
function ImageMergedListener(navigate) {
	this.navigate = navigate;
	if( navigate && this.image!==navigate.external ) {
		this.relayout = true;
		this.image = navigate.external;
		this.merged = navigate.getMerged();
		if( this.image ) {
			this.Position1 = 1+parseInt(this.image.Position);
		}
		this.updatePosition();
		AddImageUrl(this,true);
	}
};

function AddImageUrl(imageLay, clear) {
	if( clear ) {
	   imageLay.imageUrls = new Array();
	} else if(imageLay.imageUrls.length>0 ) {
	   imageLay.imageUrls[imageLay.imageUrls.length-1].imageId = imageLay.imageUrls[imageLay.imageUrls.length-1].fullImageId; 
	}
	if( (!imageLay.image) || !(imageLay.width) || !(imageLay.height) ) return;
	imageLay.imageUrls.push(imageLay.createFullUrl(WADO_QUERY));
	imageLay.preloadImage = new Image();
	var src = imageLay.imageUrls[imageLay.imageUrls.length-1].imageUrl;
	// This needs to be the unexpanded form as the src assignment does not undo this.
	src = src.replace(/&amp;/g,"&");
	imageLay.preloadImage.src = src;
	console.info("Created a pre-load image for '%s'", src);
};

/** Listens for changes in size and re-calculates the URLs to use - LEAVING the old one in place
 * so that it continues to show until the new one is ready. (Leaving the old one is temporarily disabled
 * while bugs are worked out around re-sizing - this leaving should occur only if the old image has 
 * been fetched and is immediately visible - this change may cause some hiccups to occur for a while.)
 */
function ImageResizeListener() {
	this.updatePosition();
	AddImageUrl(this,true);
};
 