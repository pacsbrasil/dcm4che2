/** Contains the model for Xero.
  * The model has a few parts:
  * 1. Static layout/menu/prefs information - this can be hardcoded or read as XML or JSON from the server.
  * 2. Query - contains the information about what the current query parameters are.  Updated whenever the 
  *        user performs a new find operation.
  * 3. search results - the information from the XML file that defines the results for the search.
  * 4. Image display information - TODO still.
  */
function XeroModel() {
};

var xeroModel = new XeroModel();
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

xeroModel.queryBox = new Layout("query","query/query");
xeroModel.queryBox.query = new UrlFactory("/wado2/study.xml","query"); 
xeroModel.searchLayout = new Layout("results","query/studyTable");
xeroModel.searchLayout.columns = XeroStudyColumns;
xeroModel.searchLayout.query = xeroModel.queryBox.query;
xeroModel.findLayout = new Layout();
xeroModel.findLayout.add(xeroModel.queryBox);
xeroModel.findLayout.add(xeroModel.searchLayout);
xeroModel.sortPatient = new SortPatient(xeroModel.searchLayout);

xeroModel.layout = new TabsLayout();
xeroModel.layout.tabMenu = new Layout("userMenu","userMenu"); 
xeroModel.layout.addTab("Find", xeroModel.findLayout);
xeroModel.layout.addTab("Display");

xeroModel.IS_IE = _SARISSA_IS_IE;

var STUDY_QUERY="/wado2/study.xml";
var IMAGE_QUERY="/wado2/image.xml";
var WADO_QUERY="/wado2/wado?requestType=XERO";