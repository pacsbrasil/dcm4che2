$model/urlFactory()$
$model/xmlModel()$
$model/studyData()$
$model/layout()$
$model/merged()$

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
	"header":[
		{"name":"Patient Name", "var":"PatientName", "format":"image/patientNameSel" },
		{"name":"Patient ID", "var":"PatientID", "format":"html/displayFirst" },
		{"name":"Study Description", "var":"StudyDescription", "format":"image/studyDescriptionSel" },
		{"name":"Modality", "var":"ModalitiesInStudy", "format":"html/displayIt" },
		{"name":"Acc#", "var":"AccessionNumber", "format":"html/displayIt" },
		{"name":"Study Date", "var":"StudyDateF", "sort":"StudyDateTime", "format":"html/displayIt" },
		{"name":"# Series", "var":"NumberOfStudyRelatedSeries", "format":"html/displayIt" },
		{"name":"# Objects", "var":"NumberOfStudyRelatedInstances", "format":"html/displayIt" }
	]
};

xeroModel.queryBox = { "format":"query/query", "query":new UrlFactory("/wado2/study.xml","query") }; 
xeroModel.searchBox = {"format":"search/studyTable", "columns":XeroStudyColumns, "query":xeroModel.queryBox.query  };
xeroModel.queryLayouts = [ xeroModel.queryBox, xeroModel.searchBox ];

var STUDY_QUERY="/wado2/study.xml";
var IMAGE_QUERY="/wado2/image.xml";
var WADO_QUERY="/wado2/wado?requestType=WADO";