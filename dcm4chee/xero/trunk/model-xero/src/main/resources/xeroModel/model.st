$urlFactory()$
$xmlModel()$
$studyData()$
$layout()$
$merged()$
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

xeroModel.queryBox = new Layout(null,"query/query");
xeroModel.queryBox.query = new UrlFactory("/wado2/study.xml","query"); 
xeroModel.searchLayout = new Layout(null,"search/studyTable");
xeroModel.searchLayout.columns = XeroStudyColumns;
xeroModel.searchLayout.query = xeroModel.queryBox.query;
xeroModel.findLayout = new Layout();
xeroModel.findLayout.add(xeroModel.queryBox);
xeroModel.findLayout.add(xeroModel.searchLayout);

xeroModel.layout = new TabsLayout();
xeroModel.layout.addTab("Find", xeroModel.findLayout);
xeroModel.layout.addTab("Display");

var STUDY_QUERY="/wado2/study.xml";
var IMAGE_QUERY="/wado2/image.xml";
var WADO_QUERY="/wado2/wado?requestType=WADO";