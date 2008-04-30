/**
 * This controller extends the based XeroController with 
 * methods dealing with laying out the overall patient display.
 */

 /** Displays all studies for the selected patient */
XeroController.prototype.displayPatient = function XC_displayPatient(src,patientId) {
	console.info("Display patient %s",patientId);
	var model = this.model;
	
	if(! model.searchBox.search ) {
		this.performSearch();
	};
	
	var study = model.searchBox.search.find("patient","PatientIdentifier", patientId).study;
	var i,n=study.length;
	model.studies = new Object();
	var uids = new Array();
	var xmlSeries;
	console.info("Found %d studies.",n);
	
	// Gets the series level information for all studies.
	for(i=0; i<n; i++) {
		uids.push(study[i].StudyInstanceUID);
	};

	if( ! model.patientLayouts ) {
		model.merged = new Merged();
		model.patientLayouts = this.createPatientLayouts(model.merged,2,1,1,2,3,3);
	}
	
	var merged = model.merged;		
	merged.setNavigation(merged.KEY, patientId);
	merged.getChild(merged.KEY, 0).setNavigation(merged.KEY_SET,uids);

	model.layouts = model.patientLayouts;
	this.updateView("displayPatient");
};

/**
 * Create a patient layout with the given sizing/design
 */
XeroController.prototype.createPatientLayouts = function PC_createPatientLayouts(merged, stx,sty,sex,sey,imx,imy) {
	var layout = new Layout();
	layout.setGrid(1,2);
	var layouts = layout.layouts;
	var menuLayout = new Layout("image/menu");
	merged.addListener(menuLayout,merged.KEY,0); 
	layouts.push( menuLayout );
	menuLayout.absHeight=25;
	var patientArea = this.createStudyArea(menuLayout.merged, stx,sty,sex,sey,imx,imy);	
	layouts.push( patientArea );
	var size = getViewportSize();
	console.info("Viewport size %d,%d",size[0],size[1]);
	layout.resize(size[0],size[1], "width:100%; height:100%;");
	return layouts;
};

/** Create the study layouts containing the study tray and the image area */
XeroController.prototype.createStudyArea = function PC_createStudyArea(merged, stx,sty,sex,sey,imx,imy) {
	var studyArea = new Layout();
	studyArea.setGrid(stx*2,sty);
	var layouts = studyArea.layouts;
	var i,j
	for(j=0; j<sty; j++) {
		for(i=0; i<stx; i++) {
			var studyTray = new Layout("image/studyTray" );
			merged.addListener(studyTray,merged.KEY_SET,j*stx+i);
			studyTray.absWidth = 135;
			var seriesArea = this.createSeriesArea(studyTray.merged,sex,sey,imx,imy);
			studyTray.x = i;
			studyTray.y = j;
			
			var even = ((i %2)==0);
			if( even ) {
				layouts.push(studyTray);
			}
			console.info("Adding a series area %d,%d",i,j);
			layouts.push(seriesArea);
			if( !even ) {
				layouts.push(studyTray);
			}
		}
	}
	return studyArea;
};

XeroController.prototype.createSeriesArea = function PC_createSeriesArea(merged,sex, sey, imx, imy) {
	var seriesArea = new Layout("html/layout");
	seriesArea.setGrid(sex,sey*2);
	var layouts = seriesArea.layouts;
	var i,j,toolbar;
	var toolbars = new Array();
	for(j=0; j<sey; j++) {
		for(i=0; i<sex; i++) {
			toolbar = new Layout("image/seriesToolbar");
			toolbars[i] = toolbar;
			merged.addListener(toolbar,merged.KEY,j*sex+i);
			toolbar.absHeight = 25;
			toolbar.x = i;
			toolbar.y = j;
			layouts.push(toolbar);
		};
		for(i=0; i<sex; i++) {
			imageArea = this.createImageArea(toolbars[i].merged,imx,imy);
			layouts.push(imageArea);
		};
	};
	return seriesArea;
};

XeroController.prototype.createImageArea = function PC_createImageArea(merged,imx, imy) {
	var imageArea = new Layout("html/layout");
	imageArea.setGrid(imx,imy);
	var layouts = imageArea.layouts;
	var i,j,layout;
	for(j=0; j<imy; j++) {
		for(i=0; i<imx; i++) {
			layout = new Layout("image/image");
			merged.addListener(layout,merged.position,j*imx+i);
			layout.x = i;
			layout.y = j;
			layouts.push(layout);
		};
	};
	return imageArea;
};

/** Displays the specified (backslashes) set of study UIDs */
XeroController.prototype.displayStudy = function XC_displayStudy(src,studyUids) {
	console.info("Display studyUID %s",studyUids);
	var model = this.model;
	model.layouts = model.patientLayouts;
	
	
	this.updateView("displayStudy");
};

 