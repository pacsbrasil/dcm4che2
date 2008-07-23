/**
 * Tests the study data/XML object tests
 */
$unittest()$
$xmlModel()$
$studyData()$
var studyDataTests = new Object();

var vlutTxt = "$vlutImageXml();format="js"$";

studyDataTests.gspsTest = function StudyDataTests_gspsTest() {
	var sd = new StudyData();
	sd.parseString(vlutTxt);
	assertNotNull(sd,"vlut study data");
	var pat = sd.patient[0];
	var patId = sd.children["VLUT_Pnn"];
	assertNotNull(pat,"VLUT Patient");
	assert(pat===patId,"Patient child and patient by id must be the same.");
	var study = pat.study[0];
	assertEquals(pat.study.length,1,"Only 1 study.");
	assertEquals(study.series.length,14,"Should have 12 image series and 1 gsps series, and 1 empty");
	var gspsSeries = study.children["PR:1.2.276.0.7230010.3.200.4"];
	assertNotNull(gspsSeries,"GSPS series must exist.");
	assertEquals(gspsSeries.gsps.length,12,"Must have 12 GSPS VLUT children.");
};

runUnitTests(studyDataTests);

 