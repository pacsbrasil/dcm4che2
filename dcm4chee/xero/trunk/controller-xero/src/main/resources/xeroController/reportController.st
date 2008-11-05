/**
 * This controller manages the report viewing and navigation.
 */
 
/** Causes the given report to be viewed - a null uid causes the report to be hidden. */
XeroController.prototype.viewReport = function RC_viewReport(layId,uid) {
	var lay = this.model.patientLayout.findLayout(layId);
	console.info("Trying to view report", uid,"in",lay.id);

	var nav = lay.navigate;
	if(!nav) nav = lay.parent.navigate;
	if( !nav ) {
		console.warn("No navigation defined - can't navigate study/report.");
	 	return;
	};
	nav.setConversation("displayReport", uid);

	var size = getViewportSize();
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");
	console.info("Resized report view - now updating the view.");
	this.updateView("viewReport");
};