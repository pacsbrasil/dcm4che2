/**
 * This controller extends the based XeroController with 
 * methods dealing with laying out the overall patient display.
 */

 /** Displays all studies for the selected patient */
XeroController.prototype.displayPatient = function XC_displayPatient(src,patientId,uids) {
	console.info("Display patient %s",patientId);
	
	console.time("Load Patient Data");
	var model = this.model;
	var searchLayout = model.idMap.SearchLayout;
	if(! searchLayout.search ) {
		this.performSearch();
	};
	
	var patient = searchLayout.search.find("patient","PatientIdentifier", patientId);
	var study = patient.study;
	var i,n=study.length;
	model.studies = new Object();
	var xmlSeries;
	this.debug("Found %d studies.",n);
	
	if(!uids) {
		uids = new Array();
		// Gets the study level ko/sr/pr applicability for all studies.
		for(i=0; i<n; i++) {
			uids.push(study[i].studyUID);
		};
	}; 

	if( ! model.navigate ) {
		model.navigate = new Navigation();
		model.patientLayout.navigate = model.navigate;
		model.patientLayout.init();
	}
	
	var navigate = model.navigate;
	navigate.setNavigation(patientId);
	navigate.getChild(0).setNavigation(uids);
	
	var external = navigate.external;
	if( (!external) || (!external.children[patientId]) ) {
		external = new StudyData(uids);
		console.info("Setup external study data root on url %s", external.url);
		external.request();
		console.info("Loaded study data.");
	}
	this.mergeExternal(external);

	model.layout.selectTab("Display");
	console.timeEnd("Load Patient Data");

	this.displayResize("displayPatient");
};

/** Resizes and redisplays the entire display area */
XeroController.prototype.displayResize = function(op) {
	var size = getViewportSize();
	this.debug("Viewport size %d,%d",size[0],size[1]);
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");
	this.updateView(op);
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.displayMode = function(layId) {
	var lay = this.model.layout.findLayout(layId);
	console.info("Changing display mode to",lay.i18n);
	this.model.navigate.setConversation("mode", lay.i18n);
	// TODO - avoid a complete update, just do a lazy update of the changs
	this.updateView("displayMode");
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.seriesLayout = function(layId) {
	var lay = this.model.layout.findLayout(layId);
	console.info("Changing series layout to",lay.i18n);
	var nav = lay.navigate;
	if( nav==null ) nav = lay.parent.navigate;
	nav.setConversation("seriesLayout", lay.i18n);
	this.displayResize("seriesLayout");
};

/** Chooses between the compare study mode and single study mode */
XeroController.prototype.compareStudyMode = function(layId) {
	var lay = this.model.patientLayout.findLayout(layId);
	console.info("Changing study layout to",lay.i18n);
	this.model.navigate.getChild(0).setConversation("compareStudy", lay.i18n);
	this.displayResize("compareStudyMode");
};

/** Merge the external data into the navigate display data so that layouts can be seen. */
XeroController.prototype.mergeExternal = function(external) {
	var navigate = this.model.navigate;
	if( !external ) external = navigate.external;
	// Have to allow 3 queries, top level, series and image.
	var i=3, queries;
	do { 
		if( queries ) external.addQueries(queries);
		console.warn("Delivering queries to mergeExternal.");
		queries = navigate.mergeExternal(external);
		i--;
	} while(queries && i>0);
	if( i==0 && queries ) {
		console.warn("Too many queries without getting all data...");
	}

};

XeroController.prototype.selectGsps = function XC_selectGsps(navId, gsps) {
	console.info("You selected to apply %s to %s", gsps, navId);
	var lay = this.model.patientLayout.findLayout(navId);
	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate;
	if( gsps==null || gsps==="" ) nav.conversation.setPresentation(undefined);
	else nav.conversation.setPresentation("&gsps="+gsps);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getUrlId());
	this.updateDisplayTab(true);
	console.info("Completed applying gsps %s", gsps);	
};

/** Redisplays the display tab, resizing and merging external information etc */
XeroController.prototype.updateDisplayTab = function XC_updateDisplayTab(forceComplete) {
	var sameTab = true;
	
	this.mergeExternal();
	console.info("mergeExternal completed.");

	var size = getViewportSize();
	console.info("Viewport size %d,%d",size[0],size[1]);
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");

	if( sameTab && forceComplete!==true ) {
		this.waitUpdate("displayTab");
	}
	else {
		this.updateView("displayTab");
	}
};

/** Displays the specified (backslashes) set of study UIDs */
XeroController.prototype.displayStudy = function XC_displayStudy(src,patientId, studyUid) {
	this.debug("Display study UID %s",studyUid);
	var uids = [ studyUid ];
	this.displayPatient(src, patientId,uids);
};

/** Navigates to a given study for a given study slot */
XeroController.prototype.navStudy = function(layId,uid) {
	var lay = this.model.layout.findLayout(layId);
	var nav = lay.parent.navigate;
	if( !(nav && nav.external) ) return;
	if(nav.external.studyUID==uid) return;
	console.info("Navigating layout",layId,"to study",uid);
};

/** Navigates at the series level to a specific series */
XeroController.prototype.navToSer = function XC_NavToSer(id,uid) {
	console.info("Trying %s to navigate to %s", id, uid);
	var lay = this.model.patientLayout.findLayout(id);
	var navigate = lay.navigate;
	navigate.setNavigation(uid);
	console.info("setNavigation completed.");
	this.updateDisplayTab();	
};

/** Navigates at the series level by dir +/- forward/backwards */
XeroController.prototype.navSer = function XC_navSer(id,dir) {
	var lay = this.model.patientLayout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	if( (!lay) || ! lay.navigate ) {
		console.warn("Didn't find a study to navigate on?");
		return;
	}
	var navigate = lay.navigate;
	var dirSeries = navigate.lookupId(dir);
	if(!dirSeries ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	console.info("You are trying to navigate to %s", dirSeries);
	navigate.setNavigation(dirSeries);
	console.info("setNav completed.");
	this.updateDisplayTab();	
};

/** Navigates at the image level - can be called against either the imageToolbar or imageArea */
XeroController.prototype.navImg = function XC_navImg(id,dir) {
	var lay = this.model.patientLayout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	if( (!lay) || ! lay.navigate ) {
		console.warn("Didn't find an image to navigate on?");
		return;
	}
	var navigate = lay.navigate;
	var dirImg = navigate.lookupId(dir);
	if(dirImg==null ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	console.info("You are trying to navigate to %s", dirImg);
	navigate.setNavigation(dirImg);
	
	this.updateDisplayTab();
};

/** Navigates the series using the mouse wheel */
XeroController.prototype.navWheelImg = function XC_navWheelImg(id,e) {
	id = this.model.patientLayout.findLayout(id);
	console.info("Wheel mouse navigation called on %s", id.id);
	var wheelData = e.detail ? e.detail : e.wheelDelta/-40;
	wheelData = (wheelData * id.gridX * id.gridY)/3;
	console.info("Trying to navigate %d", wheelData);
	this.navImg(id,wheelData);
	console.info("Wheel isComplete=%s",id.isComplete());
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
	id.navigateListener(id.navigate);
	id.resize(id.width, id.height, id.style);
	this.updateView("displayPatient");
};

