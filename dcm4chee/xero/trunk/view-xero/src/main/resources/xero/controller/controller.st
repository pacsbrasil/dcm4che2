/**
 * Contains the general controller for Xero.
 * This is the non-interactive controller for Xero - it handles the actions such as series
 * navigation that do not result in continuous, interactive updates to images etc.
 * There is an interactive controller as well that handles things like window levelling,
 * zoom/pan, and faster navigation while the user has the button down, or until some timeout
 * has completed and the user isn't navigating any longer.  At that point, the interactive
 * controller switches to this controller to complete the overall update.
 *
 * There are some common parts to the controller in a vertical slice:
 * 	Setup - configures the initial model for display
 *  Submit - stores information to the server
 * 	Restore Data - takes information from the server and sets up a local setting
 *  Restore View - takes stored data and sets up the model again for displaying that page/data
 *	Read Input - takes information from a form on the display page and sets up a model to
 *		correspond with it
 *
 * Horizontally, there are several components as well:
 *	Query Page - including search results, query form
 *	Display Page - including series tray, report view, series view, image view etc
 */

/** Creates a XeroController object, typically called xeroController */
function XeroController(model, view) {
	this.model = model;
	this.view = view;
	
	// Start with the query layout as the default layout.
	console.info("Setting controller layouts to %s",model.queryLayouts);
	model.layouts = model.queryLayouts;
};

XeroController.prototype.getBody = function XC_getBody() {
	if( this.body===undefined ) {
		this.body = document.getElementById("body");
	};
	return this.body;
};

XeroController.prototype.updateView = function XC_action(actionName) {
	console.time("InitialRender "+actionName);
	var render = this.view.toString("xeroBody",this.model);
	console.timeEnd("InitialRender "+actionName);
	
	console.time("InnerHTML");
	this.getBody().innerHTML = render;
	console.timeEnd("InnerHTML");
};

$controller/patientController()$

XeroController.prototype.performSearch = function XC_performSearch() {
	var model = this.model;
	console.info("Starting to perform search");
	model.queryBox.query.updateUrl();
	console.info("Updated URL to %s", model.queryBox.query.url);

	console.time("SearchQuery");
	model.searchBox.search = new XmlModel(model.searchBox.query);
	model.searchBox.search.request();
	console.timeEnd("SearchQuery");
};

/** Finds the search results and causes them to be displayed */
XeroController.prototype.findAction = function XC_findAction(src) {
	this.performSearch();	
	this.updateView("findAction");	
};

/** Displays the query model */
XeroController.prototype.displayQuery = function XC_displayQuery(src) {
	var model = this.model;
	model.layouts = model.queryLayouts;
	this.updateView("displayQuery");
};

var xeroController = new XeroController(xeroModel, xeroView);
