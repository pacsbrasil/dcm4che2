/**
 * This controller manages the report viewing and navigation.
 */
 
/** Causes the given report to be viewed - sets the layout to one of the viewable layouts
 *  if it isn't already in that mode, and changes the UID to view.
 */
XeroController.prototype.viewReport = function RC_viewReport(layId,uid) {
	var lay = this.findLayout(layId);
	console.info("Trying to view report", uid,"in",lay.id);

	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate;
	if( !nav ) {
		console.warn("No navigation defined - can't navigate study/report.");
	 	return;
	};
	nav.setConversation("displayReport", uid);
	if( nav.conversation.studyLayout === "ShowImages" ) {
		nav.setConversation("studyLayout", "ShowReportAndImages");
	}

	this.displayResize("viewReport");
};

XeroController.prototype.viewOther = function (id,uid) {
	var lay = this.findLayout(id);
	var nav = lay.navigate;
	var other = nav.external.findInstance(uid);
	if( !other ) {
	   console.warn("Unable to find other view object",uid);
	   return;
	}
	var studyUID = other.get("studyUID");
	var seriesUID = other.get("seriesUID");
	var ae = other.get("ae");
	var url = WADO_QUERY + "&studyUID="+studyUID+"&seriesUID="+seriesUID+"&objectUID="+uid;
	if( ae ) url = url+"&ae="+ae;
	console.info("Opening window",url);
	window.open(url);
};

/** Changes the report/study layout to the one specified in layId */
XeroController.prototype.reportLayout = function RC_viewReport(id, newLay) {
	var lay = this.model.layout.findLayout(id);
	if( !newLay ) newLay = lay.arg;
	console.info("Trying to change report layout to",lay.i18n,"id",newLay);

	var nav = this.getNavigate(lay);
	nav.setConversation("studyLayout", newLay);

	this.displayResize(newLay);
};