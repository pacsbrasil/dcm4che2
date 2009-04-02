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
	var url = queryLayout.query.url;
	if( model.ae ) url = url+model.ae;
	searchLayout.search.requestUrl(url);
	if( searchLayout.search.tooManyResults ) {
		console.warn("Too many results found from query",url);
	}
	console.timeEnd("SearchQuery");
};

/** Causes the study query to be made immediately on loading - so that everything is available.
 * Also hooks up the de-activate listener for QueryLayout
 */
function onloadStudySearch() {
	if( model.idMap.SearchLayout.search ) return;
	controller.performSearch();
	controller.findLayout("QueryLayout").hideListener = function() {
		controller.storeScrollPosition("SearchLayout");
	};
};

addLoadEvent(onloadStudySearch);


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
	var sl = this.findLayout("SearchLayout");
	if( sl ) {
	   sl.scrollTop = 0;
	}
	this.updateView("findAction");	
};

/** Sort the selected columns */
XeroController.prototype.sortQuery = function SC_sortQuery(src,event,col) {
  var lay = model.idMap.SearchLayout
  console.info("Found sort layout %s",lay);
  if(! lay.search ) {
	this.performSearch();
  };
  console.info("Trying to sort %s on column %s", src,col);
  lay.sortPatient.sortColumn(col);
  this.updateView("findAction");	
};

/** Submit the find form on enter press */
XeroController.prototype.find_key13 = function(e) {
	this.findAction('QueryLayout');
};

/** Clear the query criteria */
XeroController.prototype.clearQuery = function(id) {
	var lay = this.findLayout(id);
	var q = lay.query;
	// TODO - figure out a better way to list all the query fields.
	q.AccessionNumber="";
	q.ModalitiesInStudy=null;
	q.PatientID="";
	q.StudyDateTime=null;
	q.PatientName="";
	lay.relayout = true;
	this.updateLayouts("clearQuery");
};