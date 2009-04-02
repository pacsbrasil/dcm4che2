/**
 * This controller extends the based XeroController with 
 * methods dealing with laying out the overall patient display.
 */

 /** Displays all studies for the selected patient */
XeroController.prototype.displayPatient = function XC_displayPatient(id,patientId,uid) {
	console.info("Display patient id",patientId);
	
	console.time("Load Patient Data");
	var model = this.model;
	var searchLayout = model.idMap.SearchLayout;
	if(! searchLayout.search ) {
		this.performSearch();
	};
	
	var patient = searchLayout.search.find("patient","PatientIdentifier", patientId);
	if(!patient) {
		console.warn("Unable to find patient.");
		// TODO - use the actual selected objects to read the patient IDs
		return;
	}
	var study = patient.study;
	var i,n=study.length;
	model.studies = new Object();
	var xmlSeries;
	console.info("Found %d studies.",n);
	
	var uids = this.selectManyStudies(patient,uid);

	if( ! model.navigate ) {
		model.navigate = new Navigation();
		model.navigate.conversation.mode="default";
		model.patientLayout.navigate = model.navigate;
		model.patientLayout.init();
	}
	
	var navigate = model.navigate;
	navigate.setNavigation(patientId);
	var navPat = navigate.getChild(0);
	navPat.setNavigation(uids,"study");
	navPat.setNavigation(uids[0],"nav0");
    navPat.conversation.nav1Show = (uids.length>1);
    
	if( uids.length>1 ) {
		navPat.setNavigation(uids[1],"nav1");
	} else {
		navPat.setNavigation(uids[0]+"&c=1","nav1"); 
	}
	var keyObjects = model.layout.findLayout("KeyObjects");
	navigate.conversation.keyObjectDefault = (keyObjects && keyObjects.isActive);
	
	var external = navigate.external;
	if( (!external) || (!external.children[patientId]) ) {
		external = new StudyData(uids, model.ae);
		console.info("Setup external study data root on url %s", external.url);
		external.request();
		console.info("Loaded study data.");
	} else {
		// Need to ensure all the studies are available for the base information
		external.queryForStudies(uids, external.children[patientId].children, model.ae);
	}
	
	this.mergeExternal(external);
	
	if( uids.length==1 ) {
		var navS0 = navPat.getChild(0,"nav0");
		if(! navS0.conversation.seriesLayout ) {
			if( navS0.getExtChildren().length>4 ) {
				navS0.setConversation("seriesLayout", "Layout4x2");
			}
		}  
	}

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
	var wasMode = this.getModeImage();
	if( lay.isActive ) toMode = "default";
	console.info("Changing display mode to",toMode,"from",wasMode);
	
	this.model.navigate.setConversation("mode", toMode);
	this.waitUpdate("displayMode");
};

/** Sets the current display mode - that is, the operation that controls click etc */
XeroController.prototype.seriesLayout = function(id, event, arg) {
	var lay = this.model.layout.findLayout(id);
	console.info("Changing series layout to",lay.i18n);
	var nav = lay.navigate;
	if( nav==null ) nav = lay.parent.navigate;
	if( !arg ) arg = lay.i18n;
	nav.setConversation("seriesLayout", arg);
	if( nav.conversation.studyLayout === "ShowReport" ) {
		nav.setConversation("studyLayout", "ShowReportAndImages");
	}

	// Navigate backwards to ensure all positions are shown.
	var cnt = this.getSeriesLayoutCount(arg);
	if( cnt>1 ) {
		var posn;
		var navTo = 0;
		var c0 = nav.getChild(0);
		while(posn==null && cnt>0) {
			if( c0.lookupId(--cnt)!=null ) break;
			if( c0.lookupId(navTo)==null ) break;
			navTo--;
		}
		console.info("Trying to back-navigate to", navTo);
		if( navTo<0 ) nav.setNavigation(c0.lookupId(navTo));
	}
	this.displayResize("seriesLayout");
};

/** From the layout name, figure out how many objects could be shown. */
XeroController.prototype.getSeriesLayoutCount = function(layout) {
	if( layout==="Layout1x1" ) return 1;
	if( layout==="Layout2x2" ) return 4;
	if( layout==="Layout4x2" ) return 8;
	return 2;
};

/** Chooses between the compare study mode and single study mode */
XeroController.prototype.studyCompare = function(id) {
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	
	var key = nav.KEY+"Show";
	var altKey = (key =="nav0Show" ? "nav1Show" : "nav0Show");
	var showAlt = nav.parent.conversation[altKey];
	//console.info("compare Show keys",key,altKey,"at level", nav.parent.level);
	//console.info("Values:", nav.parent.conversation[key],nav.parent.conversation[altKey]);
	
	if( showAlt===false ) {
		// Display the compare mode again
		console.debug("Showing alternate panel.");
		nav.parent.setConversation(altKey,true);
	} else {
		console.debug("Hiding this panel.");
		nav.parent.setConversation(key,false);
	}
	this.displayResize("studyCompare");
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

XeroController.prototype.selectGsps = function XC_selectGsps(id, event, gsps) {
	console.info("You selected to apply %s to %s", gsps, id);
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	var curGsps = nav.conversation.getQueryModifier("pr");
	if( curGsps!=null && curGsps.substring(6)==gsps ) gsps = null;
	if( gsps==null || gsps==="" ) nav.conversation.setQueryModifier("pr");
	else nav.conversation.setQueryModifier("pr","&gsps="+gsps,true);
	nav.setConversation("gspsLabel",gsps);
	console.info("Conversation info set on",nav.conversation.id,"at level",
		nav.level,"external id now", nav.conversation.getExternalId());
	this.updateDisplayTab(true);
	console.info("Completed applying gsps %s", gsps);	
};

XeroController.prototype.selectKo = function (id, event, koUID) {
	console.info("You selected to apply ko %s to %s", koUID, id);
	var lay = this.model.layout.findLayout(id);
	var nav = this.getNavigate(lay);
	var curKo = nav.conversation.getQueryModifier("ko");
	if( curKo!=null && curKo.substring(7)==koUID ) koUID = null;
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
	this.displayPatient(src, patientId,studyUid);
};

/** Navigates a given study next/previous - skips next/prev if they are
 * shown in the alt screen.
 */
XeroController.prototype.navStudy = function(id,event,dir) {
	console.info("Navigating study dir",dir,"in",id);
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	var uid = nav.lookupId(dir);
	
	var altKey = (nav.KEY=="nav0" ? "nav1" : "nav0");
	var altVal = nav.parent.conversation[altKey];
	var altShow = nav.parent.conversation[altKey+"Show"];
	if( altShow!==false && uid===altVal ) {
		console.info("Skipping alternate viewed image.");
		uid = nav.lookupId(dir*2);
	}
	
	if(uid==null ) {
		console.warn("Didn't find a study to navigate to.");
		return;
	}
	nav.parent.setNavigation(uid,nav.KEY);
	this.mergeExternal();
	this.displayResize();
};

/** Navigates to a given study for a given study slot */
XeroController.prototype.navToStudy = function(id,event,uid) {
	var lay = this.findLayout(id);
	var nav = this.getNavigate(lay);
	if( !(nav && nav.external) ) return;
	
	var altKey = (lay.key=="nav0" ? "nav1" : "nav0");
	var altVal = nav.conversation[altKey];
	if( altVal===uid ) {
		uid = uid + "&c=1";
	}
	nav.setNavigation(uid,lay.key);
	this.mergeExternal();
	this.displayResize();
};

/** Navigates at the series level to a specific series */
XeroController.prototype.navToSer = function XC_NavToSer(id,event,uid) {
	console.info("Trying %s to navigate to %s", id, uid);
	var lay = this.model.patientLayout.findLayout(id);
	var navigate = lay.navigate;
	// TODO - change to use a series level instance
	navigate.setNavigation(uid);
	console.info("setNavigation completed.");
	this.updateDisplayTab();	
};

/** Navigates at the series level by dir +/- forward/backwards */
XeroController.prototype.navSer = function XC_navSer(id,event,dir) {
	var lay = this.model.layout.findLayout(id);
	console.info("You are trying to navigate %s in dir %d found lay %s", id,dir,lay.id);
	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate
	if( typeof(dir)==="string" ) dir = parseInt(dir);
	if(!isFinite(dir)) {
		console.warn("Not a finite series direction:",dir);
		return;
	}
	dir = Math.floor(dir);
	// TODO - this should operate as a child of the series it belongs to,
	// then the getChild would not be required.
	var dirSeries = nav.getChild(0).lookupId(dir);
	if(!dirSeries ) {
		console.warn("Didn't find a next/previous series to navigate to.");
		return;
	}
	var sl = nav.conversation.seriesLayout;
	var cnt = this.getSeriesLayoutCount(sl);
	if( dir>0 && cnt>1) {
		var dirTest = nav.getChild(0).lookupId(cnt);
		if( !dirTest ) {
			console.info("Not able to navigate past series end points.");
			return;
		} 
	}
	console.info("You are trying to navigate to %s", dirSeries);
	nav.setNavigation(dirSeries);
	this.updateDisplayTab();	
};

/** Navigates at the image level - can be called against either the imageToolbar or imageArea */
XeroController.prototype.navImg = function XC_navImg(id,event,dir) {
	var lay = this.findLayout(id);
	console.info("You are trying to navigate",id,"in dir",dir,"found lay", id,dir,lay.id);
	var navigate = this.getNavigate(lay);
	if( navigate.level==Navigation.SERIES ) {
		console.warn("Using series level navigation to navigate image - should use image level nav instead.");
		navigate = navigate.getChild(0);
	}
	var navSer = navigate.parent;
	if( dir==="first" ) {
		var dirImg = 0;
	} else if(dir==="last") {
		var dirImg = navSer.external.Viewable-1; 
	} else {
		if( typeof(dir)==="string" ) dir = parseInt(dir);
		if( !isFinite(dir) ) {
			console.warn("Not a finite direction/number:",dir);
			return;
		}
		// Sometimes fractions can occur
		dir = Math.floor(dir);
		var dirImg = navigate.lookupId(dir);
	};
	if( dirImg==null ) {
		console.warn("Didn't find a next/previous image to navigate to dir",dir,dirImg,"level",navigate.level);
		return;
	}
	console.info("You are trying to image navigate", navigate.KEY,"to", dirImg);
	navSer.setNavigation(dirImg,navigate.KEY);
	
	this.updateDisplayTab();
};

/** Navigates the series using the mouse wheel */
XeroController.prototype.wheelSeries = function(id,e) {
	id = this.model.patientLayout.findLayout(id);
	console.info("Wheel mouse navigation called on %s", id.id);
	var wheelData = e.detail ? e.detail : e.wheelDelta/-40;
	if( e.shiftKey ) wheelData = (wheelData * id.gridX * id.gridY)*10;
	else wheelData = (wheelData * id.gridX * id.gridY)/3;
	wheelData = Math.floor(-wheelData);
	console.info("Trying to navigate %d", wheelData);
	this.navImg(id,e,wheelData);
	console.info("Wheel isComplete=%s",id.isComplete());
	return cancelEvent(e);
};

/** Changes the series mode to 1x1 mode, or to 2x2 if in 1x1, AND navigates to the current
 * series.
 */
XeroController.prototype.dblImage = function(id,e) {
	console.info("dbl image on",id);
	var lay =this.findLayout(id);
	var nav = this.getNavigate(lay);
	var nth = nav.parent.offset;
	nav = nav.parent.parent;
	var mode = nav.conversation.seriesLayout;
	var navTo;
	if( mode==="Layout1x1" ) {
	  if( nav.conversation.wasSeriesLayout ) {
	  	mode = nav.conversation.wasSeriesLayout;
	  	nav.conversation.wasSeriesLayout = undefined;
	  	navTo = nav.conversation.dblSeries;
	  	nav.setConversation("dblSeries");
	  } else mode = "Layout2x2";
	} else {
	  nav.conversation.wasSeriesLayout = mode;
	  mode="Layout1x1";
	  nav.setConversation("dblSeries", nav.getChild(0).lookupId(0));
	  navTo = nav.getChild(0).lookupId(nth);
	}
	nav.setConversation("seriesLayout", mode);
	if( navTo ) {
		console.debug("DBl click navigating series to",navTo);
		nav.setNavigation(navTo);		
	}
	this.updateDisplayTab(true);
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


