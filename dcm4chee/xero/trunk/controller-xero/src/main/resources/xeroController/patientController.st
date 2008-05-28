/**
 * This controller extends the based XeroController with 
 * methods dealing with laying out the overall patient display.
 */

 /** Displays all studies for the selected patient */
XeroController.prototype.displayPatient = function XC_displayPatient(src,patientId,uids) {
	console.info("Display patient %s",patientId);
	
	console.time("Load Patient Data");
	var model = this.model;
	
	if(! model.searchBox.search ) {
		this.performSearch();
	};
	
	var study = model.searchBox.search.find("patient","PatientIdentifier", patientId).study;
	var i,n=study.length;
	model.studies = new Object();
	var xmlSeries;
	this.debug("Found %d studies.",n);
	
	if(!uids) {
		uids = new Array();
		// Gets the series level information for all studies.
		for(i=0; i<n; i++) {
			uids.push(study[i].StudyInstanceUID);
		};
	};

	if( ! model.patientLayout ) {
		model.merged = new Merged();
		model.patientLayout = this.createPatientLayout(model.merged,2,2,1,2,4,2);
	}
	var merged = model.merged;
	merged.setNavigation(merged.KEY, patientId);
	merged.getChild(merged.KEY, 0).setNavigation(merged.KEY_SET,uids);
	
	var external = merged.external;
	if( (!external) || (!external.children[patientId]) ) {
		external = new StudyData(uids);
		external.request();
	}

	this.mergeExternal(external);

	model.layouts = model.patientLayout.layouts;
	model.layout = model.patientLayout;
	console.timeEnd("Load Patient Data");

	var size = getViewportSize();
	this.debug("Viewport size %d,%d",size[0],size[1]);
	model.patientLayout.resize(size[0],size[1], "width:100%; height:100%;");

	this.updateView("displayPatient");
};

/** Merge the external data into the merged display data so that layouts can be seen. */
XeroController.prototype.mergeExternal = function(external) {
	var merged = this.model.merged;
	if( !external ) external = merged.external;
	var queries = merged.mergeExternal(external);
	i = 2;
	while(queries && i>0) {
		this.debug("Merging queries %s", queries);
		external.addQueries(queries);
		queries = merged.mergeExternal(external);
		i--;
	}
	if( i==0 && queries ) {
		console.warn("Too many queries without getting all data...");
	}

};

/** Displays the specified (backslashes) set of study UIDs */
XeroController.prototype.displayStudy = function XC_displayStudy(src,patientId, studyUid) {
	this.debug("Display study UID %s",studyUid);
	var uids = [ studyUid ];
	this.displayPatient(src, patientId,uids);
};

/** Navigates at the series level */
XeroController.prototype.navSer = function XC_navSer(id,dir) {
	var lay = this.model.patientLayout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	if( (!lay) || ! lay.merged ) {
		console.warn("Didn't find a study to navigate on?");
		return;
	}
	var merged = lay.merged;
	var dirSeries = merged.lookupId(merged.KEY,dir);
	if(!dirSeries ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	console.info("You are trying to navigate to %s", dirSeries);
	merged.setNavigation(merged.KEY,dirSeries);
	this.mergeExternal();

	var size = getViewportSize();
	this.debug("Viewport size %d,%d",size[0],size[1]);
	this.model.patientLayout.resize(size[0],size[1], "width:100%; height:100%;");

	this.updateView("navSer");
	
};

/** Navigates at the image level - can be called against either the imageToolbar or imageArea */
XeroController.prototype.navImg = function XC_navImg(id,dir) {
	var lay = this.model.patientLayout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	if( (!lay) || ! lay.merged ) {
		console.warn("Didn't find an image to navigate on?");
		return;
	}
	var merged = lay.merged;
	var dirImg = merged.lookupId(merged.POSITION,dir);
	if(dirImg==null ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	console.info("You are trying to navigate to %s", dirImg);
	merged.setNavigation(merged.POSITION,dirImg);
	this.mergeExternal();
	
	this.waitUpdate("navImg");
};

/** Navigates the series using the mouse wheel */
XeroController.prototype.navWheelImg = function XC_navWheelImg(id,e) {
	this.debug("Wheel mouse navigation called on %s.", id);
	id = this.model.patientLayout.findLayout(id);
	var wheelData = e.detail ? e.detail : e.wheelDelta/-40;
	wheelData = (wheelData * id.gridX * id.gridY)/3;
	this.debug("Trying to navigate %d", wheelData);
	this.navImg(id,wheelData);
	console.info("isComplete=%s",id.isComplete());
	return cancelEvent(e);
};

/** Changes the image layout */
XeroController.prototype.setImgLay = function XC_setImgLay(id,w,h) {
	id = this.model.patientLayout.findLayout(id);
	if( id.imageArea ) id = id.imageArea;
	id.origGridX = w;
	id.origGridY = h;
	// Make an artificial change to re-layout the image area.
	id.series = null;
	id.mergedListener(id.merged);
	id.resize(id.width, id.height, id.style);
	this.updateView("displayPatient");
};

XeroController.prototype.setMode = function XC_setMode(md) {
	alert("Set mode to "+md);
};

/**
 * Create a patient layout with the given sizing/design
 */
XeroController.prototype.createPatientLayout = function PC_createPatientLayout(merged, stx,sty,sex,sey,imx,imy) {
	var layout = new Layout("patientLayout");
	layout.setGrid(1,2);
	var layouts = layout.layouts;
	var patientMenu = new Layout("patientMenu", "image/patientMenu");
	patientMenu.absHeight=25;
	var childMerged = merged.addChildListener(patientMenu,PatientMergedListener,merged.KEY,0); 
	layouts.push( patientMenu );
	var studyArea = this.createStudyArea(childMerged, stx,sty,sex,sey,imx,imy);	
	layouts.push( studyArea );

	return layout;
};

/** Create the study layouts containing the study tray and the image area */
XeroController.prototype.createStudyArea = function PC_createStudyArea(merged, stx,sty,sex,sey,imx,imy) {
	var cnt = Layout.prototype.idCnt++;
	var studyArea = new Layout("studyArea"+cnt);
	studyArea.setGrid(stx*2,sty);
	var layouts = studyArea.layouts;
	var i,j
	for(j=0; j<sty; j++) {
		for(i=0; i<stx; i++) {
			var studyTray = new Layout("studyTray"+cnt+","+i+","+j,"image/studyTray" );
			var childMerged = merged.addChildListener(studyTray,StudyMergedListener,merged.KEY_SET,j*stx+i);
			studyTray.absWidth = 135;
			var seriesArea
			var seriesArea = this.createSeriesArea(childMerged,sex,sey,imx,imy);
			studyTray.x = i;
			studyTray.y = j;
			
			var even = ((i %2)==0);
			if( even ) {
				layouts.push(studyTray);
			}
			this.debug("Adding a series area %d,%d",i,j);
			layouts.push(seriesArea);
			if( !even ) {
				layouts.push(studyTray);
			}
		}
	}
	return studyArea;
};

XeroController.prototype.createSeriesArea = function PC_createSeriesArea(merged,sex, sey, imx, imy) {
	var cnt = Layout.prototype.idCnt++;
	var seriesArea = new Layout("seriesArea"+cnt,"html/layout");
	seriesArea.setGrid(sex,sey*2);
	merged.addListener(seriesArea,StudyMergedListener);
	var layouts = seriesArea.layouts;
	var i,j,toolbar, imageArea;
	var toolbars = new Array();
	for(j=0; j<sey; j++) {
		for(i=0; i<sex; i++) {
			toolbar = new Layout("seriesToolbar"+cnt+","+i+","+j,"image/seriesToolbar");
			toolbars[i] = toolbar;
			merged.addChildListener(toolbar,SeriesMergedListener,merged.KEY,j*sex+i);
			toolbar.absHeight = 25;
			toolbar.x = i;
			toolbar.y = j;
			layouts.push(toolbar);
		};
		for(i=0; i<sex; i++) {
			imageArea = this.createImageArea(toolbars[i].merged,imx,imy);
			toolbars[i].imageArea = imageArea;
			layouts.push(imageArea);
		};
	};
	return seriesArea;
};

XeroController.prototype.createImageArea = function PC_createImageArea(merged,imx, imy) {
	var cnt = Layout.prototype.idCnt++;
	var imageArea = new Layout("imageArea"+cnt,"image/imageArea");
	var xcThis = this;
	imageArea.mousewheel = function(evt) {
		console.info("Mouse wheel image area called.");
		return xcThis.navWheelImg(imageArea,evt);
	};
	merged.addListener(imageArea,ImageAreaMergedListener);
	imageArea.used = false;
	imageArea.setGrid(imx,imy);
	imageArea.origGridX = imx;
	imageArea.origGridY = imy;
	var layouts = imageArea.layouts;
	var i,j,layout;
	for(j=0; j<imy; j++) {
		for(i=0; i<imx; i++) {
			layout = new ImageLayout("imageLay"+cnt+","+i+","+j);
			merged.addChildListener(layout,ImageMergedListener,merged.POSITION,j*imx+i);
			layout.resizeListener = ImageResizeListener;
			layout.x = i;
			layout.y = j;
			layouts.push(layout);
		};
	};
	return imageArea;
};

/** The listener for the patient information */
function PatientMergedListener(merged) {
	this.merged = merged;
	if( merged ) {
		this.patient = merged.external;
	}
};

/** This defines the listeners for merged changes to the layout */
function StudyMergedListener(merged) {
	this.merged = merged;
	this.used = (merged!=null && merged.external!=null);
	if( merged ) {
		this.study = merged.external;
	}
};

/** This defines the listeners for merged changes to the layout */
function SeriesMergedListener(merged) {
	this.merged = merged;
	this.used = (merged!=null && merged.external!=null);
	if( merged ) {
		this.series = merged.external;
	}
	this.relayout = true;
};

/** Defines the image area merged listener to reset the grid size and
 * enable/disable child elements as appropriate.
 */
function ImageAreaMergedListener(merged) {
	this.merged = merged;
	if(!merged) return;
	if( merged.external===this.series ) {
		return;
	}
	this.relayout = true;
	this.series = merged.external;
	if( !this.series ) {
		this.used = false;
		return;
	}
	this.used = true;
	var n,imageCount = merged.external.Viewable;
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

/** This defines the listeners for merged changes to the layout */
function ImageMergedListener(merged) {
	this.merged = merged;
	if( merged && this.image!==merged.external ) {
		this.relayout = true;
		this.image = merged.external;
		if( this.image ) {
			this.Position1 = 1+parseInt(this.image.Position);
		}
		this.updatePosition();
		AddImageUrl(this,true);
	}
};

function AddImageUrl(imageLay, clear) {
	if( clear ) imageLay.imageUrls = new Array();
	if( (!imageLay.image) || !(imageLay.width) || !(imageLay.height) ) return;
	imageLay.imageUrls.push(imageLay.createFullUrl(WADO_QUERY));
	imageLay.preloadImage = new Image();
	var src = imageLay.imageUrls[imageLay.imageUrls.length-1].imageUrl;
	imageLay.preloadImage.src = src;
};

/** Listens for changes in size and re-calculates the URLs to use - LEAVING the old one in place. */
function ImageResizeListener() {
	this.updatePosition();
	AddImageUrl(this,false);
};
