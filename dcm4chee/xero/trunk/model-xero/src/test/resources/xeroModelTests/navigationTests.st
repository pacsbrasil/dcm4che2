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
	assertEquals(c0.id,stuid0,"c0.id");
	assertEquals(c1.id,stuid1,"c1.id");
	studyData = new StudyData();
	studyData.parseString(twoStudy);
	var urls = nav.mergeExternal(studyData);
	assertNull(urls,"URLS must be null since no request has been made below that level");
	var se00 = c0.getChild(0);
	var se10 = c1.getChild(0);
	var im000 = se00.getChild(0);
	var im100 = se10.getChild(0);
	var im10n = se10.getChild(10000);
	urls = nav.mergeExternal(studyData);
	assertNotNull(urls,"URLS must not be null now.");
	var cnt = 0;
	for(key in urls) {
		console.info("Looking for URL %s", key);
		cnt++;
	}
	assertEquals(cnt,2,"Must have 2 urls requested.");
	studyData.parseString(images0);
	studyData.parseString(images1);
	urls = nav.mergeExternal(studyData);
	assertNull(urls,"Urls must be null after supplying all data.");
	assertNotNull(se00.id,"se00.id");
	assertNotNull(se00.external,"se00.external");
	assertNotNull(im000.id,"im000.id");
	assertNotNull(im000.external,"im000.external");
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

runUnitTests(navigationTests);