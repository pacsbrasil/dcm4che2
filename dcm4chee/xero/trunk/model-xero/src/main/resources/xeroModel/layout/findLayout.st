/** Defines the Find page layout information */
function FindLayout(suffix) {
	this.baseConstructor("Find");
	var queryBox = new Layout("QueryLayout","query/query");
	queryBox.query = new UrlFactory("/wado2/study.xml?maxResults=500",queryBox.id);
	 
	var searchLayout = new Layout("SearchLayout","query/studyTable");
	searchLayout.columns = XeroStudyColumns;
	searchLayout.query = queryBox.query;
	searchLayout.sortPatient = new SortPatient(searchLayout);
	searchLayout.viewRenderedListeners = ["fixScrollPosition"];
	this.add(queryBox);
	this.add(searchLayout);
	
	var findMenu = new FindMenuLayout("");
	this.add(findMenu,TabsLayout.TabMenu);
};

FindLayout.prototype = new Layout();

/** The menu for the find display tab */
function FindMenuLayout(suffix) {
	this.baseConstructor("FindMenu");
$layout/addChildren(children=findMenuLayout.children)$
};

FindMenuLayout.prototype = new Layout("FindMenuPrototype", "html/menu");

function ReportSelect(suffix) {
	this.baseConstructor("ReportSelect");
	this.add(new ReportSelectItem("ShowReportAndImages"));
	this.add(new ReportSelectItem("ShowReport"));
	this.add(new ReportSelectItem("ShowImages"));
	this.copyFrom(this.layouts[0]);
	this.arg="ShowReport";
};

ReportSelect.prototype = new Layout("ReportSelect","html/toolItem");
ReportSelect.prototype.action="controller.rotateButton";

function ReportSelectItem(i18n) {
	this.baseConstructor(i18n);
	this.i18n = i18n;
	this.icon = i18n+"Icon";
};
ReportSelectItem.prototype = new Layout("ReportSelectItem", "html/toolItem");
ReportSelectItem.prototype.action = "controller.rotateButton";
ReportSelectItem.prototype.showLabel = true;