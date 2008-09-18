/**
 * This controller manages the search form and results
 */
XeroController.prototype.performSearch = function XC_performSearch(src) {
	console.info("Passed src %s", src);
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
	this.performSearch(src);	
	this.updateView("findAction");	
};

/** Sort the selected columns */
XeroController.prototype.sortQuery = function SC_sortQuery(src,col) {
  var lay = model.layout.findLayout(src);
  console.info("Found sort layout %s",lay);
  if( !lay.sortPatient ) lay.sortPatient = new SortPatient(lay);  
  console.info("Trying to sort %s on column %s", src,col);
  lay.sortPatient.sortColumn(col);
  this.updateView("findAction");	
};