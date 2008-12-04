$unittest()$
$merged()$
$navConversation()$
$studyData()$
$xmlModel()$
$navigation()$

navigationTests = new Object();

navigationTests.createNavigationTest = function() {
	var nav = new Navigation();
};

function CreateTestStudyData(patId, studyIds) {
	var sd = new StudyData();
};

var twoStudy = "$twoStudyXml();format="js"$";
var prStudy = "$prStudyXml();format="js"$";
var images0 = "$study0Images();format="js"$";
var images1 = "$study1Images();format="js"$";

var studyData = new StudyData();
studyData.parseString(twoStudy);
var patient = studyData.patient[0];
var patid = ""+patient.PatientIdentifier;
var study0 = patient.study[0];
var study1 = patient.study[1];
var stuid0 = ""+study0.studyUID;
var stuid1 = ""+study1.studyUID;
console.info("Study id 0 %s", stuid0);
console.info("Study id 1 %s", stuid1);

/** Tests that the navigation object will deliver the correct study query URLs
 */
navigationTests.testStudyQuery = function() {
	var nav = new Navigation();
	nav.setNavigation(patid);
	var patNav = nav.getChild(0);
	var uids = [stuid0, stuid1];	
	patNav.setNavigation(uids);
	console.info("Set navigation to %s", uids);
	var c0 = patNav.getChild(0);
	var c1 = patNav.getChild(1);
	$assert(test="c0.id==stuid0")$;
	$assert(test="c1.id==stuid1")$;
	studyData = new StudyData();
	studyData.parseString(twoStudy);
	var urls = nav.mergeExternal(studyData);
	$assert(test="urls==null",msg="URLs must be null initially without anyone being interested in studies")$
	var se00 = c0.getChild(0);
	var se10 = c1.getChild(0);
	var im000 = se00.getChild(0);
	var im100 = se10.getChild(0);
	var im10n = se10.getChild(10000);
	urls = nav.mergeExternal(studyData);
	$assert(test="urls!=null",msg="Must require more URLs after adding studies to view.")$;
	var cnt = 0;
	for(key in urls) {
		console.info("Looking for URL %s", key);
		cnt++;
	}
	$assert(test="cnt==2",msg="Must have 2 studies to retrieve information for.")$;
	studyData.parseString(images0);
	studyData.parseString(images1);
	urls = nav.mergeExternal(studyData);
	$assert(test="urls==null",msg="Urls must be null after supplying all data.")$
	$assert(test="se00.id!=null")$
	$assert(test="se00.external!=null")$
	$assert(test="im000.id!=null")$
	$assert(test="im000.external!=null")$
};
 
/**
 * Tests that the modified query is used when there is a gspsLabel to apply. 
 */
navigationTests.testModifiedQuery = function() {
	var nav = new Navigation();
	nav.setNavigation(patid);
	var patNav = nav.getChild(0);
	var uids = [stuid0];	
	patNav.setNavigation(uids);
	console.info("Set navigation to %s", uids);
	var c0 = patNav.getChild(0);
	assertEquals(c0.id,stuid0,"c0.id");
	studyData = new StudyData();
	studyData.parseString(prStudy);
	var urls = nav.mergeExternal(studyData);
	assertNull(urls,"URLS must be null since no request has been made below that level");
	var se00 = c0.getChild(0);
	var im000 = se00.getChild(0);
	urls = nav.mergeExternal(studyData);
	assertNotNull(urls,"URLS must not be null now.");
	var cnt = 0;
	var modifier;
	for(key in urls) {
		console.info("Looking for URL %s", key);
		modifier = urls[key];
		assert(modifier!==key,"Modifier and key must not be the same object.");
		cnt++;
	}
	assertEquals(cnt,1,"Must have 1 url requested for pr test.");

	studyData.parseString(images0,modifier);
	urls = nav.mergeExternal(studyData);
	assertNull(urls,"Urls must be null after supplying all data.");
	assertNotNull(se00.id,"se00.id");
	assertNotNull(se00.external,"se00.external");
};

/** All of the event setup uses a set of listeners that just records the responses */
function TestEventListener(events) {
	this.events = events;
	this.navigateEvent = new Array();
	this.namedEvent = new Array();
	this.children = new Array();
};
TestEventListener.prototype.navigateListener = function (navigate) {
	this.navigateEvent.push(navigate);
};
TestEventListener.prototype.namedListener = function (navigate, varname, newVal, oldVal) {
	this.namedEvent.push({"navigate":navigate,"varname":varname, "oldVal":oldVal,"newVal":newVal});
};
TestEventListener.prototype.clear = function() {
	this.navigateEvent = new Array();
	this.namedEvent = new Array();
	var i,n=this.children.length;
	for(i=0; i<n;i++) {
		this.children[i].clear();
	};
};

var rootEvent = new TestEventListener([".", "/conversation:mode"]);
rootEvent.navigate = "patient:0";
var studyEvent = new TestEventListener([".", "../conversation:showReport", "/patient:0/merged:windowLevel"]);
studyEvent.navigate = "study:0";
rootEvent.children.push(studyEvent);

/** Tests navigation level changes */
navigationTests.test_setNavigation_expectEvent = function () {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	var urls = nav.mergeExternal(studyData);
	$assert(test="rootEvent.navigateEvent.length>0")$;
	$assert(test="studyEvent.navigateEvent.length>0")$;
	rootEvent.clear();
};

/** Tests conversational (non-nav) events/changes */
navigationTests.test_setConversation_expectEvent = function test_setConversation_expectEvent() {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	nav.mergeExternal(studyData);
	rootEvent.clear();
	
	nav.setConversation("mode", "wl");
	$assert(test="rootEvent.namedEvent.length>0")$;
	rootEvent.clear();
	console.info("About to set showReport to true.");
	nav.getChild(0).setConversation("showReport", "true");
	$assert(test="studyEvent.namedEvent.length>0")$;
	rootEvent.clear();
};

/** Tests merged events/changes */
navigationTests.test_setMerged_expectEvent = function () {
	var nav = new Navigation();
	nav.hook(rootEvent);
	nav.setNavigation(patid);
	studyData = new StudyData();
	studyData.parseString(prStudy);
	nav.mergeExternal(studyData);
	rootEvent.navigateEvent = new Array();
	studyEvent.navigateEvent = new Array();
	rootEvent.clear();
	
	nav.getChild(0).setMerged("windowLevel", 129);
	$assert(test="studyEvent.namedEvent.length>0")$;
	rootEvent.clear();
};

runUnitTests(navigationTests);