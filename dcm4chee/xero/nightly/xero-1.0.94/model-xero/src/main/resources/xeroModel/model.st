/** Contains the model for Xero.
  * The model has a few parts:
  * 1. Static layout/menu/prefs information - this can be hardcoded or read as XML or JSON from the server.
  * 2. Query - contains the information about what the current query parameters are.  Updated whenever the 
  *        user performs a new find operation.
  * 3. search results - the information from the XML file that defines the results for the search.
  * 4. Image display information - TODO still.
  */
function XeroModel(is_ie) {
	this.queryBox = new Layout("query","query/query");
	this.queryBox.query = new UrlFactory("/wado2/study.xml","query"); 
	this.searchLayout = new Layout("results","query/studyTable");
	this.searchLayout.columns = XeroStudyColumns;
	this.searchLayout.query = this.queryBox.query;
	this.searchLayout.sortPatient = new SortPatient(this.searchLayout)

	this.findLayout = new Layout("Find");
	this.findLayout.add(this.queryBox);
	this.findLayout.add(this.searchLayout);

	this.layout = new TabsLayout("XeroRootTab");
	this.layout.tabMenu = new Layout("userMenu","userMenu"); 
	this.layout.addTab("Find", this.findLayout);
	this.patientLayout = new PatientLayout();
	this.layout.addTab("Display", this.patientLayout);
	
	this.layout.computeId();

	this.IS_IE = is_ie;
};

if( !console.info ) {
	console.info = console.log;
	console.debug = console.log;
	console.warn = console.log;
    console.time = function(name)
        {
            this.timeMap[name] = (new Date()).getTime();
        };
    console.timeEnd = function(name) {
            if (name in this.timeMap)
            {
                var delta = (new Date()).getTime() - this.timeMap[name];
                this.log([name+ ":"+ delta+"ms"]);
                delete this.timeMap[name];
            }
        };
    console.timeMap = new Object();
};

/** Shared data is the static data that is shared between server and client. */
XeroModel.prototype.shared = new Object();

$xeroModels:{ $(it)()$ }$

/** The pieces from here to the end of this file are very ugly as they need to be replicated
 * server and client side - it would be far preferable to have a single definition that works
 * for both sides, and can generate computed/linked/nested data, as well as embed plugin information
 * from the server side and information about what to call to setup various bits of data.  The
 * actual setup should be lazy if at all possible.
 * See studyColumns.xml in view-xero for the server side instance.
 */
var XeroStudyColumns = {
	"template":"xeroStudyColumns",
	"header":[
		{"name":"Patient Name", "var":"PatientName",  "sort":"PatientName", "template":"image/patientNameSel","action":"controller.sortQuery" },
		{"name":"Patient ID", "var":"PatientID", "sort":"PatientID", "template":"html/displayFirst","action":"controller.sortQuery" },
		{"name":"Study Description", "var":"StudyDescription", "sort":"StudyDescription", "template":"image/studyDescriptionSel","action":"controller.sortQuery" },
		{"name":"Modality", "var":"ModalitiesInStudy", "sort":"ModalitiesInStudy", "template":"html/displayIt","action":"controller.sortQuery" },
		{"name":"Acc#", "var":"AccessionNumber", "sort":"AccessionNumber", "template":"html/displayIt","action":"controller.sortQuery" },
		{"name":"Study Date", "var":"StudyDateF", "sort":"StudyDateTime", "template":"html/displayIt","action":"controller.sortQuery" },
		{"name":"# Series", "var":"NumberOfStudyRelatedSeries", "sort":"NumberOfStudyRelatedSeries", "template":"html/displayIt","action":"controller.sortQuery" },
		{"name":"# Objects", "var":"NumberOfStudyRelatedInstances", "sort":"NumberOfStudyRelatedInstances", "template":"html/displayIt","action":"controller.sortQuery" }
	]
};

// TODO - remove this - it is a bit of test code being used temporarily.
var i=3;

var STUDY_QUERY="/wado2/study.xml";
var IMAGE_QUERY="/wado2/image.xml";
var WADO_QUERY="/wado2/wado?requestType=XERO";