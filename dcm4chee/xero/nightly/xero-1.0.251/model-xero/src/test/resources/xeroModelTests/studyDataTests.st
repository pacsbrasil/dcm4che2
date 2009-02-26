/**
 * Tests the study data/XML object tests
 */
$unittest()$
$xmlModel()$
$studyData()$
$layout/layout()$
$sortPatient()$

var studyDataTests = new UnitTest();
console.info("testGsps"); 

var vlutTxt = "$vlutImageXml();format="js"$";
var sortTxt = "$sortStudyXml();format="js"$";

studyDataTests.testGsps = function StudyDataTests_gspsTest() { 
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

studyDataTests.testSort_parent = function StudyDataTests_sortTest_parent()  {
    var sd = new StudyData();
    sd.parseString(sortTxt);
    sd.searchLayout = new Layout();
    sd.searchLayout.search = sd;
    
    var sortPat = new SortPatient(sd.searchLayout);
    
    var pat = sd.searchLayout.search.patient;
    
    assert(pat[0].PatientName == "Modality^LUT^PState^Test^A","PatientName[0] should match");
    assert(pat[1].PatientName == "Modality^LUT^PState^Test^C","PatientName[1] should match");
    assert(pat[2].PatientName == "Modality^LUT^PState^Test^B","PatientName[2] should match");
    
    // Ascending
    sortPat.sortColumn ("PatientName");
    
    assert(pat[0].PatientName == "Modality^LUT^PState^Test^A","PatientName[0] should match");
    assert(pat[1].PatientName == "Modality^LUT^PState^Test^B","PatientName[1] should match");
    assert(pat[2].PatientName == "Modality^LUT^PState^Test^C","PatientName[2] should match");

    // Ascending
    sortPat.sortColumn ("PatientID");
    
    assert(pat[0].PatientID == "MLUT_Pnn_2", "PatientID[0] should match");
    assert(pat[1].PatientID == "MLUT_Pnn_3", "PatientID[1] should match");
    assert(pat[2].PatientID == "MLUT_Pnn_5", "PatientID[2] should match");

    // Descending
    sortPat.sortColumn ("PatientName");
    
    assert(pat[0].PatientName == "Modality^LUT^PState^Test^C","PatientName[0] should match");
    assert(pat[1].PatientName == "Modality^LUT^PState^Test^B","PatientName[1] should match");
    assert(pat[2].PatientName == "Modality^LUT^PState^Test^A","PatientName[2] should match");
    
    
    // Descending
    sortPat.sortColumn ("PatientID");
    
    assert(pat[0].PatientID == "MLUT_Pnn_5", "PatientID[0] should match");
    assert(pat[1].PatientID == "MLUT_Pnn_3", "PatientID[1] should match");
    assert(pat[2].PatientID == "MLUT_Pnn_2", "PatientID[2] should match");
};



studyDataTests.testSort_child = function StudyDataTests_sortTest_child()  {
    var sd = new StudyData();
    sd.parseString(sortTxt);
    sd.searchLayout = new Layout();
    sd.searchLayout.search = sd;
    
    var sortPat = new SortPatient(sd.searchLayout);
    
    var pat = sd.searchLayout.search.patient;
    
    assert(pat[0].study[0].StudyDescription == "Modality LUT PState Test_Z", "StudyDescription [0,0] should match");
    assert(pat[0].study[1].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,1] should match");
    assert(pat[0].study[2].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,2] should match");

    assert(pat[1].study[0].StudyDescription == "Modality LUT PState Test_T", "StudyDescription [1,0] should match");
    assert(pat[1].study[1].StudyDescription == "Modality LUT PState Test_S", "StudyDescription [1,1] should match");
    assert(pat[1].study[2].StudyDescription == "Modality LUT PState Test_L", "StudyDescription [1,2] should match");
    
    assert(pat[2].study[0].StudyDescription == "Modality LUT PState Test_K", "StudyDescription [2,0] should match");
    assert(pat[2].study[1].StudyDescription == "Modality LUT PState Test_N", "StudyDescription [2,1] should match");
    assert(pat[2].study[2].StudyDescription == "Modality LUT PState Test_M", "StudyDescription [2,2] should match");

    // Ascending    
    sortPat.sortColumn ("StudyDescription");
    
    assert(pat[0].study[0].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,0] should match");
    assert(pat[0].study[1].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,1] should match");
    assert(pat[0].study[2].StudyDescription == "Modality LUT PState Test_Z", "StudyDescription [0,2] should match");

    assert(pat[1].study[0].StudyDescription == "Modality LUT PState Test_K", "StudyDescription [1,0] should match");
    assert(pat[1].study[1].StudyDescription == "Modality LUT PState Test_M", "StudyDescription [1,1] should match");
    assert(pat[1].study[2].StudyDescription == "Modality LUT PState Test_N", "StudyDescription [1,2] should match");

    assert(pat[2].study[0].StudyDescription == "Modality LUT PState Test_L", "StudyDescription [2,0] should match");
    assert(pat[2].study[1].StudyDescription == "Modality LUT PState Test_S", "StudyDescription [2,1] should match");
    assert(pat[2].study[2].StudyDescription == "Modality LUT PState Test_T", "StudyDescription [2,2] should match");
    
    // Ascending
    sortPat.sortColumn ("StudyDateTime");

    assert(pat[0].study[0].StudyDateTime == "1999-10-13T08:30:08.000-05:00", "StudyDateTime [0,0] should match");
    assert(pat[0].study[1].StudyDateTime == "1999-11-17T08:30:08.000-05:00", "StudyDateTime [0,1] should match");
    assert(pat[0].study[2].StudyDateTime == "1999-11-19T08:30:08.000-05:00", "StudyDateTime [0,2] should match");

    assert(pat[1].study[0].StudyDateTime == "1999-10-15T08:30:08.000-05:00", "StudyDateTime [1,0] should match");
    assert(pat[1].study[1].StudyDateTime == "1999-10-17T08:30:08.000-05:00", "StudyDateTime [1,1] should match");
    assert(pat[1].study[2].StudyDateTime == "1999-11-18T08:30:08.000-05:00", "StudyDateTime [1,2] should match");
    
    assert(pat[2].study[0].StudyDateTime == "1999-12-17T08:30:08.000-05:00", "StudyDateTime [2,0] should match");
    assert(pat[2].study[1].StudyDateTime == "1999-12-18T08:30:08.000-05:00", "StudyDateTime [2,1] should match");
    assert(pat[2].study[2].StudyDateTime == "1999-12-19T08:30:08.000-05:00", "StudyDateTime [2,2] should match");
        
    // Descending    
    sortPat.sortColumn ("StudyDescription");
    
    assert(pat[0].study[0].StudyDescription == "Modality LUT PState Test_Z", "StudyDescription [0,0] should match");
    assert(pat[0].study[1].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,1] should match");
    assert(pat[0].study[2].StudyDescription == "Modality LUT PState Test_A", "StudyDescription [0,2] should match");

    assert(pat[1].study[0].StudyDescription == "Modality LUT PState Test_T", "StudyDescription [1,0] should match");    
    assert(pat[1].study[1].StudyDescription == "Modality LUT PState Test_S", "StudyDescription [1,1] should match");
    assert(pat[1].study[2].StudyDescription == "Modality LUT PState Test_L", "StudyDescription [1,2] should match");
    
    assert(pat[2].study[0].StudyDescription == "Modality LUT PState Test_N", "StudyDescription [2,0] should match");
    assert(pat[2].study[1].StudyDescription == "Modality LUT PState Test_M", "StudyDescription [2,1] should match");
    assert(pat[2].study[2].StudyDescription == "Modality LUT PState Test_K", "StudyDescription [2,2] should match");
    
    
    // Descending
    sortPat.sortColumn ("StudyDateTime");

    assert(pat[0].study[0].StudyDateTime == "1999-12-19T08:30:08.000-05:00", "StudyDateTime [0,0] should match");
    assert(pat[0].study[1].StudyDateTime == "1999-12-18T08:30:08.000-05:00", "StudyDateTime [0,1] should match");
    assert(pat[0].study[2].StudyDateTime == "1999-12-17T08:30:08.000-05:00", "StudyDateTime [0,2] should match");

    assert(pat[1].study[0].StudyDateTime == "1999-11-19T08:30:08.000-05:00", "StudyDateTime [1,0] should match");
    assert(pat[1].study[1].StudyDateTime == "1999-11-17T08:30:08.000-05:00", "StudyDateTime [1,1] should match");
    assert(pat[1].study[2].StudyDateTime == "1999-10-13T08:30:08.000-05:00", "StudyDateTime [1,2] should match");

    assert(pat[2].study[0].StudyDateTime == "1999-11-18T08:30:08.000-05:00", "StudyDateTime [2,0] should match");
    assert(pat[2].study[1].StudyDateTime == "1999-10-17T08:30:08.000-05:00", "StudyDateTime [2,1] should match");
    assert(pat[2].study[2].StudyDateTime == "1999-10-15T08:30:08.000-05:00", "StudyDateTime [2,2] should match");
};




studyDataTests.runUnitTests();

 