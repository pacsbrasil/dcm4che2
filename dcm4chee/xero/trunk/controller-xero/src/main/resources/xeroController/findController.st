/**
 * This controller manages the search form and results
 */
XeroController.prototype.performSearch = function XC_performSearch(src) {
	console.info("Passed src %s", src);
    var model = this.model;
	console.info("Starting to perform search");
	var queryLayout = model.idMap.QueryLayout;
	queryLayout.query.updateUrl();
	console.info("Updated URL to %s", queryLayout.query.url);

	console.time("SearchQuery");
	var searchLayout = model.idMap.SearchLayout;
	searchLayout.search = new StudyData();
	searchLayout.search.requestUrl(queryLayout.query.url);
	console.timeEnd("SearchQuery");
};

/** Adds a multiple study selection to allow viewing a sub-set of the available studies. */
XeroController.prototype.manyStudy = function(id, pid, studyUID) {
	var search = this.model.idMap.SearchLayout.search;
	if( !(search && search.children) ) {
		this.performSearch();
		search = this.model.idMap.SearchLayout.search;
	}
	var pat = search.children[pid];
	if(!pat) {
		log.warn("Didn't find patient.");
		return;
	}
	var study = pat.children[studyUID];
	if( !study ) {
		log.warn("Didn't find study UID.",studyUID);
		return;
	};
	study.isSelected = !pat.children[studyUID].isSelected;
	console.info("Selected study",studyUID,study.isSelected); 
};

/** Finds the search results and causes them to be displayed */
XeroController.prototype.findAction = function XC_findAction(src) {
	this.performSearch(src);	
	this.updateView("findAction");	
};

/** Sort the selected columns */
XeroController.prototype.sortQuery = function SC_sortQuery(src,col) {
  var lay = model.idMap.SearchLayout
  console.info("Found sort layout %s",lay);
  if(! lay.search ) {
	this.performSearch();
  };
  console.info("Trying to sort %s on column %s", src,col);
  lay.sortPatient.sortColumn(col);
  this.updateView("findAction");	
};