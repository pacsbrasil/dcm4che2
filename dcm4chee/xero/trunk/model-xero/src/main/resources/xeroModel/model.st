$urlFactory()$
$xmlModel()$
$layout()$
$merged()$
$navConversation()$
$studyData()$
$navigation()$
$tabs()$

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

/** The pieces from here to the end of this file are very ugly as they need to be replicated
 * server and client side - it would be far preferable to have a single definition that works
 * for both sides, and can generate computed/linked/nested data, as well as embed plugin information
 * from the server side and information about what to call to setup various bits of data.  The
 * actual setup should be lazy if at all possible.
 */
var XeroStudyColumns = {
	"template":"xeroStudyColumns",
	"header":[
		{"name":"Patient Name", "var":"PatientName", "template":"image/patientNameSel" },
		{"name":"Patient ID", "var":"PatientID", "template":"html/displayFirst" },
		{"name":"Study Description", "var":"StudyDescription", "template":"image/studyDescriptionSel" },
		{"name":"Modality", "var":"ModalitiesInStudy", "template":"html/displayIt" },
		{"name":"Acc#", "var":"AccessionNumber", "template":"html/displayIt" },
		{"name":"Study Date", "var":"StudyDateF", "sort":"StudyDateTime", "template":"html/displayIt" },
		{"name":"# Series", "var":"NumberOfStudyRelatedSeries", "template":"html/displayIt" },
		{"name":"# Objects", "var":"NumberOfStudyRelatedInstances", "template":"html/displayIt" }
	]
};

xeroModel.queryBox = new Layout("query","query/query");
xeroModel.queryBox.query = new UrlFactory("/wado2/study.xml","query"); 
xeroModel.searchLayout = new Layout("results","search/studyTable");
xeroModel.searchLayout.columns = XeroStudyColumns;
xeroModel.searchLayout.query = xeroModel.queryBox.query;
xeroModel.findLayout = new Layout();
xeroModel.findLayout.add(xeroModel.queryBox);
xeroModel.findLayout.add(xeroModel.searchLayout);

xeroModel.layout = new TabsLayout();
xeroModel.layout.tabMenu = new Layout("userMenu","userMenu"); 
xeroModel.layout.addTab("Find", xeroModel.findLayout);
xeroModel.layout.addTab("Display");

var STUDY_QUERY="/wado2/study.xml";
var IMAGE_QUERY="/wado2/image.xml";
var WADO_QUERY="/wado2/wado?requestType=XERO";