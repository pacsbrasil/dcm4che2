$model/urlFactory()$
$model/xmlModel()$

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

XeroModel.prototype.studyColumns = {
	"header":[
		{"name":"Patient Name", "var":"PatientName" },
		{"name":"Patient ID", "var":"PatientID" },
		{"name":"Study Description", "var":"StudyDescription" },
		{"name":"Modality", "var":"ModalitiesInStudy" },
		{"name":"Acc#", "var":"AccessionNumber" },
		{"name":"Study Date", "var":"StudyDate" },
		{"name":"# Series", "var":"NumberOfStudyRelatedSeries" },
		{"name":"# Objects", "var":"NumberOfStudyRelatedInstances" }
	]
};

xeroModel.query = new UrlFactory("/wado2/study.xml","query");

xeroModel.search = new XmlModel(xeroModel.query);