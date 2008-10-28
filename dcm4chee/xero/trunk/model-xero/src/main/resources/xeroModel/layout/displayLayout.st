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
	var patientMenu = new PatientMenu();
	var patientNav = this.navigate.addChildListener(patientMenu,PatientMergedListener,0); 
	this.add(patientMenu, TabsLayout.TabMenu);
	
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			var nth = j*this.gridX+i;
			var studyArea = new StudyAreaLayout(patientNav, nth);
			this.add(studyArea);
		}
	}

	return true;
};

/** The menu for patient tab control */
function PatientMenu() {
	// The suffix is empty as there should only be 1 instance of this menu.
	var suffix = "";
	this.baseConstructor("PatientMenu");
	
$layout/addChildren(children=patientMenu.children)$
};

PatientMenu.prototype = new Layout("PatientMenuPrototype","image/patientMenu");
PatientMenu.prototype.baseConstructor=Layout;


/** Create the study layouts containing the study tray and the image area.  Not lazy. */
function StudyAreaLayout(navigate,nth) {
	var suffix = "S"+nth;
	this.baseConstructor("StudyArea"+suffix);

	var studyNav = navigate.addChildListener(this,StudyMergedListener,nth);
	var studyTray = new StudyTrayLayout(suffix, studyNav);
	var even = ((nth %2)==0);
	if( studyTray.absWidth ) {
		this.add(studyTray,(even?Layout.WEST:Layout.EAST));
	} else {
		this.add(studyTray,Layout.NORTH);
	}

	var i,j, imageArea, seriesArea, seriesSuffix;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			nth = j*this.gridX+i;
			seriesSuffix = suffix+"Se"+nth;
			imageAreas = new ImageAreasLayout(studyNav, seriesSuffix, nth);
			this.add(imageAreas);
		};
	};
};

StudyAreaLayout.prototype = new Layout();
StudyAreaLayout.prototype.baseConstructor = Layout;
StudyAreaLayout.prototype.gridX = 2;
StudyAreaLayout.prototype.gridY = 2;
StudyAreaLayout.prototype.displayClass="studyArea";

/** Handle the study tray layout */
function StudyTrayLayout(suffix,studyNav) {
	this.baseConstructor("StudyTray"+suffix);
	studyNav.addListener(this,StudyMergedListener);

$layout/addChildren(children=studyTrayLayout.children)$
	
};

StudyTrayLayout.prototype = new Layout("StudyTray", "image/studyTray");
StudyTrayLayout.prototype.baseConstructor = Layout;
StudyTrayLayout.prototype.absWidth = 135;

/** Handle the window level menu layout */
function WLMenu(suffix) {
	this.baseConstructor("WLMenu"+suffix);
$layout/addChildren(children=wlMenu.children)$	
};

WLMenu.prototype = new Layout("WLMenuPrototype", "html/labelItem");
WLMenu.prototype.baseConstructor = Layout;
WLMenu.prototype.icon="icon/wl.png";
WLMenu.prototype.label="Window Level";


/** Handle the zoom menu layout */
function ZoomMenu(suffix) {
	this.baseConstructor("ZoomMenu"+suffix);
$layout/addChildren(children=zoomMenu.children)$	
};

ZoomMenu.prototype = new Layout("ZoomMenuPrototype", "html/labelItem");
ZoomMenu.prototype.baseConstructor = Layout;
ZoomMenu.prototype.icon="icon/zoomPan.png";
ZoomMenu.prototype.label="Zoom";


/** Create the Spatial transforms menu layout */
function SpatMenu(suffix,studyNav) {
	this.baseConstructor("SpatMenu"+suffix);
$layout/addChildren(children=spatMenu.children)$	
};

SpatMenu.prototype = new Layout("SpatMenuPrototype", "html/labelItem");
SpatMenu.prototype.baseConstructor = Layout;
SpatMenu.prototype.icon="icon/rotateRight.png";
SpatMenu.prototype.label="Rotate Right";



/** Create an images areas object */
function ImageAreasLayout(navigate, suffix, nth) {
	this.baseConstructor("ImageAreas"+suffix);
	var seriesNav = navigate.addChildListener(this,ImageAreaMergedListener, nth);

	var toolbar = new ImageAreasMenu(suffix);
	seriesNav.addListener(toolbar,SeriesMergedListener);
	toolbar.imageAreas = this;
	this.add(toolbar,Layout.NORTH);

	this.used = false;
	this.origGridX = this.gridX;
	this.origGridY = this.gridY;
	var useThis = this;

	this.mousewheel = function(evt) {
		useThis.debug("Mouse wheel image area called.");
		return controller.navWheelImg(useThis,evt);
	};
	
	var i,j,layout, imageSuffix;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			var nth = j*this.gridX+i;
			imageSuffix = suffix+"I"+nth;
			layout = new ImageLayout("ImageLay"+imageSuffix);
			seriesNav.addChildListener(layout,ImageMergedListener,nth);
			layout.resizeListener = ImageResizeListener;
			layout.x = i;
			layout.y = j;
			this.add(layout);
		};
	};
};

ImageAreasLayout.prototype = new Layout();
ImageAreasLayout.prototype.baseConstructor = Layout;
ImageAreasLayout.prototype.gridX = 2;
ImageAreasLayout.prototype.gridY = 2;
ImageAreasLayout.prototype.onmousewheel="controller.navWheelImg";


/** The menu for image level control */
function ImageAreasMenu(suffix) {
	this.baseConstructor("ImageAreasMenu"+suffix);
	
$layout/addChildren(children=imageAreasMenu.children)$
};

ImageAreasMenu.prototype = new Layout("ImageAreasMenuPrototype","html/menu");
ImageAreasMenu.prototype.baseConstructor=Layout;
ImageAreasMenu.prototype.absHeight = 1;

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
	console.info("series merged listener called.");
	this.navigate = navigate;
	var changed = this.used;
	this.used = (navigate!=null && navigate.external!=null);
	changed = (changed!=this.used);
	if( navigate ) console.info("Changing series on %s to %d",this.id, navigate.id);
	if( changed && this.parent ) {
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
 