/**
 * This controller manages the search form and results
 */
XeroController.prototype.performSearch = function XC_performSearch() {
	var model = this.model;
	console.info("Starting to perform search");
	model.queryBox.query.updateUrl();
	console.info("Updated URL to %s", model.queryBox.query.url);

	console.time("SearchQuery");
	model.searchLayout.search = new XmlModel(model.searchLayout.query);
	model.searchLayout.search.request();
	console.timeEnd("SearchQuery");
};

/** Finds the search results and causes them to be displayed */
XeroController.prototype.findAction = function XC_findAction(src) {
	this.performSearch();	
	this.updateView("findAction");	
};

/** Sort the selected columns */
XeroController.prototype.sortQuery = function SC_sortQuery(src,col) {
  console.info("Trying to sort %s on column %s", src,col);
  model.sortPatient.sortColumn(col);
  this.updateView("findAction");	
};