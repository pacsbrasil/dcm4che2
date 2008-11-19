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
	if(!patient) {
		log.warn("Unable to find patient.");
		// TODO - use the actual selected objects to read the patient IDs
		return;
	}
	var study = patient.study;
	var i,n=study.length;
	model.studies = new Object();
	var xmlSeries;
	this.debug("Found %d studies.",n);
	
	uids = this.selectManyStudies(patient,uids);

	if( ! model.navigate ) {
		model.navigate = new Navigation();
		model.navigate.conversation.mode="WindowLevel";
		model.patientLayout.navigate = model.navigate;
		model.patientLayout.init();
	}
	
	var navigate = model.navigate;
	navigate.setNavigation(patientId);
	navigate.getChild(0).setNavigation(uids);
	var keyObjects = model.layout.findLayout("KeyObjects");
	navigate.conversation.keyObjectDefault = (keyObjects && keyObjects.isActive);
	
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

/** Selects the isSelected studies from patient */
XeroController.prototype.selectManyStudies = function(patient,uid) {
	var study,studies = patient.study;
	var i,n=studies.length;
	var uids = new Array();
	if( uid ) uids.push(uid);
	for(i=0; i<n;i++) {
		study = studies[i];
		if( study.isSelected && study.studyUID!=uid) uids.push(study.studyUID);
	}
	if( uids.length>0 ) {
		console.info("Found",uids.length,"seleted studies:",uids);
		return uids;
	}
	for(i=0; i<n; i++) {
		study = studies[i];
		uids.push(study.studyUID);	
	}
	return uids;
};

/** Returns the mode for the given layout */
XeroController.prototype.getModeImage = function(lay) {
	var mode = this.model.navigate.conversation.mode;
	return mode;
};
XeroController.prototype.getModeSeries = XeroController.prototype.getModeImage;

/** Gets the navigate object from this or a parent object */
XeroController.prototype.getNavigate = function(lay) {
	while(!lay.navigate) lay = lay.parent;
	if( !lay ) return;
	return lay.navigate;
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
	var toMode = lay.i18n;
	if( lay.isActive && lay.children ) {
		toMode = lay.findRotate(toMode,1).i18n;
	}
	console.info("Changing display mode to",toMode);
	this.model.navigate.setConversation("mode", toMode);
	// TODO - avoid a complete update, just do a lazy update of the changes
	this.waitUpdate("displayMode");
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.seriesLayout = function(layId, arg) {
	var lay = this.model.layout.findLayout(layId);
	console.info("Changing series layout to",lay.i18n);
	var nav = lay.navigate;
	if( nav==null ) nav = lay.parent.navigate;
	if( !arg ) arg = lay.i18n;
	nav.setConversation("seriesLayout", arg);
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

XeroController.prototype.selectGsps = function XC_selectGsps(id, gsps) {
	console.info("You selected to apply %s to %s", gsps, id);
	var lay = this.model.layout.findLayout(id);
	var nav = this.getNavigate(lay);
	if( gsps==null || gsps==="" ) nav.conversation.setQueryModifier("pr");
	else nav.conversation.setQueryModifier("pr","&gsps="+gsps,true);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getExternalId());
	this.updateDisplayTab(true);
	console.info("Completed applying gsps %s", gsps);	
};

XeroController.prototype.selectKo = function (id, koUID) {
	console.info("You selected to apply ko %s to %s", koUID, id);
	var lay = this.model.layout.findLayout(id);
	var nav = this.getNavigate(lay);
	if( koUID==null || koUID==="" ) {
		var qm = nav.conversation.setQueryModifier("ko","&koUID=*");
		qm.infoOnly = true;
	} else nav.conversation.setQueryModifier("ko","&koUID="+koUID);
	nav.setConversation("koUID", koUID);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getExternalId());
	// The merge external is required before the invalidate to match the next external data/children
	// up, while the invalidate is required to ensure that new children get the correct values from external.
	// This may require an extra external query AFTER the invalidate.
	this.mergeExternal();
	nav.invalidate(true);
	this.updateDisplayTab(true);
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
		this.waitUpdate("displayTabPartial");
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
	var uids = nav.conversation.nav;
	var uid2 = uids[lay.parent.nth];
	console.info("Swapping studies",uid,uid2);
	var p2, v, n=uids.length, uids2 = new Array();
	for(p2=0; p2<n; p2++) {
		v = uids[p2];
		if( v==uid ) uids2.push(uid2);
		else if(v==uid2 ) uids2.push(uid);
		else uids2.push(v);
	};
	nav.setNavigation(uids2);
	this.mergeExternal();
	this.displayResize();
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
	var lay = this.model.layout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate
	if( typeof(dir)==="string" ) dir = parseInt(dir);
	var dirSeries = nav.lookupId(dir);
	if(!dirSeries ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	console.info("You are trying to navigate to %s", dirSeries);
	nav.setNavigation(dirSeries);
	console.info("setNav completed.");
	this.updateDisplayTab();	
};

/** Navigates at the image level - can be called against either the imageToolbar or imageArea */
XeroController.prototype.navImg = function XC_navImg(id,dir) {
	var lay = this.model.layout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	var navigate = lay.navigate;
	if(!navigate) navigate = lay.parent.navigate;
	if( dir==="first" ) {
		var dirImg = 0;
	} else if(dir==="last") {
		var dirImg = navigate.external.Viewable-1; 
	} else {
		if( typeof(dir)==="string" ) dir = parseInt(dir);
		var dirImg = navigate.lookupId(dir);
	};
	if(dirImg==null ) {
		console.warn("Didn't find a next/previous series to navigate to dir",dir,dirImg);
		return;
	}
	console.info("You are trying to navigate to %s", dirImg);
	navigate.setNavigation(dirImg);
	
	this.updateDisplayTab();
};

/** Navigates the series using the mouse wheel */
XeroController.prototype.wheelSeries = function(id,e) {
	id = this.model.patientLayout.findLayout(id);
	console.info("Wheel mouse navigation called on %s", id.id);
	var wheelData = e.detail ? e.detail : e.wheelDelta/-40;
	if( e.shiftKey ) wheelData = (wheelData * id.gridX * id.gridY)*10;
	else wheelData = (wheelData * id.gridX * id.gridY)/3;
	wheelData = Math.floor(wheelData);
	console.info("Trying to navigate %d", wheelData);
	this.navImg(id,wheelData);
	console.info("Wheel isComplete=%s",id.isComplete());
	return cancelEvent(e);
};

/** Changes the series mode to 1x1 mode, or to 2x2 if in 1x1 */
XeroController.prototype.dblImage = function(id,e) {
	var lay =this.model.patientLayout.findLayout(id);
	var nav = this.getNavigate(lay);
	nav = nav.parent.parent;
	var mode = nav.conversation.seriesLayout;
	if( mode==="Layout1x1" ) {
	  if( nav.conversation.wasSeriesLayout ) {
	  	mode = nav.conversation.wasSeriesLayout;
	  	nav.conversation.wasSeriesLayout = undefined;
	  } else mode = "Layout2x2";
	} else {
	  nav.conversation.wasSeriesLayout = mode;
	  mode="Layout1x1";
	}
	nav.setConversation("seriesLayout", mode);
	this.displayResize("doubleImage");
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


