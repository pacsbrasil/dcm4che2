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
PatientLayout.prototype.gridX = 2;
PatientLayout.prototype.gridY = 1;
// Add some initial size to handle the border.
PatientLayout.prototype.heightEdge = 0;
PatientLayout.prototype.widthEdge = 0;

/** Initialize the patient layout object - lazy, and only succeed if the navigate object is available.
 */
PatientLayout.prototype.init = function PatientLayout_init() {
	if( this.navigate==null ) return false;
	if( this.layouts && this.layouts.length>0 ) return true;
	if( model.IS_IE ) this.widthEdge = 12;
	var patientMenu = new PatientMenu();
	this.add(patientMenu, TabsLayout.TabMenu);
	
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			var nth = j*this.gridX+i;
			var studyArea = new StudyAreaLayout(nth);
			this.add(studyArea);
		}
	}
	
	this.navigate.hook(this);

	return true;
};

/** Handles the compare study layout/event notifications */
function CompareStudyLay() {
	this.baseConstructor(this.i18n);
};

CompareStudyLay.prototype = new Layout("CompareStudyPrototype", "html/toolItem");
CompareStudyLay.prototype.i18n="SingleStudy";
CompareStudyLay.prototype.icon="SingleStudyIcon";
CompareStudyLay.prototype.action="controller.compareStudyMode";
CompareStudyLay.prototype.events=["/patient:0", "/patient:0/conversation:compareStudy"];

CompareStudyLay.prototype.namedListener = function(nav,vname,val) {
	if( val=="SingleStudy" ) this.i18n = "CompareStudy";
	else this.i18n = "SingleStudy";
	this.icon = this.i18n+"Icon";	
};

/** Listen at the patient level to determine updates to the layout */
CompareStudyLay.prototype.navigateListener = function(nav) {
	if(!nav) return;
	if(!nav.external) return;
	if(!nav.external.study) return;
	this.used = nav.external.study.length>1;
};

/** The menu for patient tab control */
function PatientMenu() {
	// The suffix is empty as there should only be 1 instance of this menu.
	var suffix = "";
	this.baseConstructor("PatientMenu");
	
$layout/addChildren(children=patientMenu.children)$
};

PatientMenu.prototype = new Layout("PatientMenuPrototype","image/patientMenu");
/** The listener for the patient information */
PatientMenu.prototype.navigateListener = function (navigate) {
	this.navigate = navigate;
	if( ! navigate ) return;
	this.patient = navigate.external;
};
PatientMenu.prototype.events=".";
PatientMenu.prototype.navigate="/patient:0";

/** Create the study layouts containing the study tray and the image area.  Not lazy. */
function StudyAreaLayout(nth) {
	var suffix = "S"+nth;
	this.baseConstructor("StudyArea"+suffix);
	this.nth = nth;
	this.navigate = "/patient:0/study:"+nth;
	var studyTray = new StudyTrayLayout(suffix);
	var even = ((nth %2)==0);
	if( studyTray.absWidth ) {
		this.add(studyTray,(even?Layout.WEST:Layout.EAST));
	} else {
		this.add(studyTray,Layout.NORTH);
	}
	
	this.add(new ReportArea(suffix));
	this.add(new SeriesAreas(suffix));
};

StudyAreaLayout.prototype = new Layout();
StudyAreaLayout.prototype.gridX = 1;
StudyAreaLayout.prototype.gridY = 2;
StudyAreaLayout.prototype.displayClass="studyArea";
StudyAreaLayout.prototype.events= ["/patient:0/conversation:compareStudy","."];
StudyAreaLayout.prototype.compareUsed = true;

/**
 * This defines the listeners for navigate changes to the layout.  Computes
 * quite a few fields relevant to display menus etc.
 * These computations really belong in the individual menu items, but right now
 * it isn't yet clear how to add those individually. 
 */
StudyAreaLayout.prototype.navigateListener = function (navigate) {
	this.navigate = navigate;  
	this.used = (navigate!=null && navigate.external!=null);
	if( ! navigate ) return;
	this.study = navigate.external;
	if( !this.study ) return;
	this.patient = navigate.parent.external;
	this.multiStudy = (this.patient.study.length>1);
	if( this.nth>0 && navigate.parent.conversation.compareStudy=="SingleStudy" ) this.used = false;
};


/** Listen for changes in the compare study type */
StudyAreaLayout.prototype.namedListener = function StudyNavigateListener(nav,vname,val) {
	console.info("compare study changed",val,"study area listener.");
	if( this.nth==0 ) return;
	this.compareUsed = (val==="CompareStudy");
	var nUsed = (this.compareUsed && nav.external!=null);
	console.info("Set compare used to",this.compareUsed,"nUsed",nUsed);
	if( nUsed!=this.used ) {
		this.used = nUsed;
		this.relayout = true;
	} 
};

/** The report area displays a report study */
function ReportArea(suffix) {
	this.baseConstructor("ReportArea"+suffix);
};

ReportArea.prototype = new Layout("ReportArea","report/report");
ReportArea.prototype.events="conversation:displayReport";

ReportArea.prototype.namedListener=function(nav,vname,val) {
	if( !(nav && nav.external) ) return;
	if( val===undefined ) {
		this.used = false; 
		return;
	};
	if( val===null || val==="" ) {
		console.info("Display no report.");
		this.used = false;
		return;
	}
	console.info("Displaying report",val);
	this.used = true;
	this.src = "studyUID="+nav.external.studyUID+"&amp;objectUID="+val;
}; 

/**
 * The series areas contains the individual series are display objects
 */
function SeriesAreas(suffix, nth) {
	this.baseConstructor("SeriesArea"+suffix);
	this.nth = nth;
	var i,j, seriesSuffix, imageAreas;
	for(j=0; j<this.gridY; j++) {
		for(i=0; i<this.gridX; i++) {
			nth = j*this.gridX+i;
			seriesSuffix = suffix+"Se"+nth;
			imageAreas = new ImageAreasLayout(seriesSuffix, nth);
			this.add(imageAreas);
		};
	};
};

SeriesAreas.prototype = new Layout("SeriesAreas");
SeriesAreas.prototype.gridX = 2;
SeriesAreas.prototype.gridY = 2;
SeriesAreas.prototype.events="merged:external";

/** Just disable the series area if there are no available series */
SeriesAreas.prototype.namedListener = function(nav,key,external) {
	this.used = false;
	if( !(nav && external && external.series) ) return;
	var i,n=external.series.length;
	for(i=0; i<n; i++) {
		if( external.series[i].image ) {
			this.used = true;
			break;
		}
	}
};

/** Handle the study tray layout */
function StudyTrayLayout(suffix) {
	this.baseConstructor("StudyTray"+suffix);

$layout/addChildren(children=studyTrayLayout.children)$
	
};

StudyTrayLayout.prototype = new Layout("StudyTray", "image/studyTray");
StudyTrayLayout.prototype.absWidth = 135;
StudyTrayLayout.prototype.events=".";
StudyTrayLayout.prototype.navigateListener = StudyAreaLayout.prototype.navigateListener;

/** Defines the X of N series layout information */
function XofNSeries() {
	this.baseConstructor("XofN");
};

XofNSeries.prototype = new Layout("XofN","image/seriesNumber");

XofNSeries.prototype.events="series:0";

XofNSeries.prototype.navigateListener = function(nav) {
	console.info("Setting nav information for x of n series","nav level",nav.level);
	if( !nav ) return;
	if( ! nav.external ) return;
	this.series = nav.parent.getExtChildren();
	if( ! this.series ) return;
	console.info("Series UID navigated to", nav.external.seriesUID);
	this.n = this.series.length;
	for(this.x = 1;this.x<=this.series.length; this.x++) {
		if( nav.external.seriesUID == this.series[this.x-1].seriesUID ) break;
	}
};

/**
 * Generic display mode 
 */
function DisplayModeLay(i18n) {
	this.baseConstructor(i18n);
	this.i18n = i18n;
	this.icon = i18n+"Icon";
};
DisplayModeLay.prototype = new Layout("DisplayModeLay","html/toolItem");
DisplayModeLay.prototype.events="/conversation:mode";
DisplayModeLay.prototype.action="controller.displayMode";
DisplayModeLay.prototype.showLabel=true;

/** A general mode listener - activates/de-actives given modes, plus promoting child to parent mode */
DisplayModeLay.prototype.namedListener = function (navigate, varName, newVal) {
	if( this.children && this.i18n!=newVal ) {
		// This is a top-level menu item that contains child modes, so make it reflect the
		// child mode correctly.
		var i,n=this.children.length;
		for(i=0; i<n; i++) {
			if( this.children[i].i18n==newVal ) {
				this.i18n = this.children[i].i18n;
				this.icon = this.children[i].icon;
				this.isActive = true;
				return;
			}
		}
		this.isActive = false;
	}
	else {
		var newActive = (this.i18n == newVal);
		if( newActive==this.isActive ) return;
		console.info("Setting mode change on",this.i18n,"to",newActive,"mode event",newVal);
		this.isActive = newActive;
		this.relayout = true;
	}
};




/** Handle the window level menu layout */
function WLMenu() {
	this.baseConstructor(this.i18n);
	var suffix="";
$layout/addChildren(children=wlMenu.children)$	
};

WLMenu.prototype = new DisplayModeLay("WindowLevel");
WLMenu.prototype.isActive = true;
WLMenu.prototype.showLabel = false;

/** Handle the zoom menu layout */
function ZoomMenu() {
	this.baseConstructor(this.i18n);
	var suffix="";
$layout/addChildren(children=zoomMenu.children)$	
};
ZoomMenu.prototype = new DisplayModeLay("Zoom");
ZoomMenu.prototype.showLabel = false;

/** Create the Spatial transforms menu layout */
function SpatMenu(suffix,studyNav) {
	this.baseConstructor(this.i18n);
	var suffix="";
$layout/addChildren(children=spatMenu.children)$	
};
SpatMenu.prototype = new DisplayModeLay("RotateRight");
SpatMenu.prototype.showLabel = false;

/** Create an images areas object */
function ImageAreasLayout(suffix, nth) {
	this.baseConstructor("ImageAreas"+suffix);
	this.navigate = "series:"+nth;
	this.nth = nth;

	var toolbar = new ImageAreasMenu(suffix);
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
			layout = new ImageLayout("ImageLay"+imageSuffix,nth);
			layout.x = i;
			layout.y = j;
			this.add(layout);
		};
	};
};

ImageAreasLayout.prototype = new Layout();
ImageAreasLayout.prototype.gridX = 2;
ImageAreasLayout.prototype.gridY = 2;
ImageAreasLayout.prototype.onmousewheel="controller.navWheelImg";
ImageAreasLayout.prototype.events=".";

/** Defines the image area navigate listener to reset the grid size and
 * enable/disable child elements as appropriate.
 */
ImageAreasLayout.prototype.navigateListener = function(navigate) {
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

/** The menu for image level control */
function ImageAreasMenu(suffix) {
	this.baseConstructor("ImageAreasMenu"+suffix);
	
$layout/addChildren(children=imageAreasMenu.children)$
};

ImageAreasMenu.prototype = new Layout("ImageAreasMenuPrototype","html/menu");
ImageAreasMenu.prototype.absHeight = 22;

/** This defines the listeners for navigate changes to the layout */
ImageAreasMenu.prototype.navigateListener = function SeriesMergedListener(navigate) {
	this.debug("series merged listener called.");
	this.navigate = navigate;
	var changed = this.used;
	this.used = (navigate!=null && navigate.external!=null);
	changed = (changed!=this.used);
	if( navigate ) this.debug("Changing series on",this.id,"to", navigate.id);
	if( changed && this.parent ) {
		this.parent.relayout = true;
	}
	if( navigate && navigate.external!==this.series ) {
		this.series = navigate.external;
	}
	this.relayout = true;
};

ImageAreasMenu.prototype.events=".";

