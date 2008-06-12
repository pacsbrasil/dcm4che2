/**
 * This controller manages the report viewing and navigation.
 */
 
/** Creates the report area and returns it. */
XeroController.prototype.createReportArea = function RC_createReportArea() {
	var lay = new Layout("report", "report/report");
	lay.used = false;
	return lay;
};

/** Causes the given report to be viewed. */
XeroController.prototype.viewReport = function RC_viewReport(uid) {
	console.info("Trying to view report %s", uid);
	this.model.studyArea.tabs.selectTab("reportArea");
	var reportArea = this.model.studyArea.tabs.findTab("reportArea");
	reportArea.src = "/wado2/wado?objectUID="+uid;
	console.info("Set src to %s used is %s",reportArea.src, reportArea.used); 

	var size = getViewportSize();
	this.model.patientLayout.resize(size[0]-8,size[1]-32, "width:100%; height:100%;");
	console.info("Resized report view - now updating the view.");
	this.updateView("viewReport");
};