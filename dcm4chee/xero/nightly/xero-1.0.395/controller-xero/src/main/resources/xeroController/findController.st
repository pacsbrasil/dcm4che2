/**
 * This controller manages the search form and results
 */
 
 
/** 
 * Perform a search based on the indicated queryStr.  If queryStr is undefined then the
 * current query is refreshed. 
 */
XeroController.prototype.performSearch = function XC_performSearch() {
    var model = this.model;
	var queryLayout = model.idMap.QueryLayout;
	
	var url = queryLayout.query.url;
	console.info("Performing search with URL:" + url);
	console.time("SearchQuery");
	var searchLayout = model.idMap.SearchLayout;
	searchLayout.search = new StudyData();

	if( model.ae ) url = url+model.ae;
	searchLayout.search.requestUrl(this.service,url);
	if( searchLayout.search.tooManyResults ) {
		console.warn("Too many results found from query",url);
	}
	console.timeEnd("SearchQuery");
};

/** Causes the study query to be made immediately on loading - so that everything is available.
 * Also hooks up the de-activate listener for QueryLayout
 */
function onLoadStudySearch() {
	if( model.idMap.SearchLayout.search ) return;

	// Update the client based on the URL that has been passed.
	var url = document.location.search.substring(1);
	if(!url) return;
	
	
	controller.updateClient(url);

	controller.findLayout("QueryLayout").hideListener = function() {
		controller.storeScrollPosition("SearchLayout");
	};
};

// Client side load event to update the query data
addLoadEvent(onLoadStudySearch);


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
	var queryLayout = this.model.idMap.QueryLayout;
	var queryUrl = queryLayout.query.updateUrlFromForm();
	this.updateFindTab();
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
	this.findAction();
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

// Update the client based on the passed URL
XeroController.prototype.updateClient = function(url) {
	// Populate the 'Find' tab with search results
	var queryLayout = this.findLayout("QueryLayout");
	queryLayout.query.updateQueryStr(url);
	queryLayout.query.updateFormFromURL(url);
	this.performSearch();

	if(this.model.i18n.InitialTab == "Display")
	{
		// Show the 'Display' tab first.
		var patients = this.findLayout("SearchLayout").search.patient;
		if(patients && patients.length > 0)
		{
			var pid = patients[0].PatientIdentifier;
			
			console.info("Displaying studies for PatientIdentifer="+pid);
			this.displayPatient('SearchLayout',pid);
		}
	}
};

XeroController.prototype.updateFindTab = function() {
	// Invoke a query based on the updated URL
	this.performSearch();
	
	var sl = this.findLayout("SearchLayout");
	if( sl ) {
	   sl.scrollTop = 0;
	}
	this.updateView("findAction");	
};

