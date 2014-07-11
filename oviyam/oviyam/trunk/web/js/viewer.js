var pat = null;
var firstSeries;
var imgLoaded = false;

function loadViewerPage() {
	myLayout = $('#optional-container').layout({
		west: {
	    	size: 205
	    }
	});
	initPage();

	$("#toolbarContainer").load("viewer_tools.html");
}

function getStudyDetails() {
	pat = $.cookies.get('patient');
	var queryString = window.top.location.search.substring(1);
	var patId = getParameter(queryString, "patientID");
	var studyId = getParameter(queryString, "studyUID");
	var serverName = getParameter(queryString, "serverName");
	if (serverName == 'null') {
		serverName = '';
	}
	if (patId == 'null') {
		patId = '';
	}
	if (studyId == 'null') {
		studyId = '';
	}

	if (patId != null && studyId != null && serverName != null) {
		$.post("StudyInfo.do", {
			"patientID" : patId,
			"studyUID" : studyId,
			"serverName" : serverName
		}, function(data) {
			if (data['error'] != null) {
				if (data['error'].trim() == 'Server not found') {
					alert("Server not found!!!");
					return;
				}
			}
			pat = data;
			// $('#westPane').css('background-color', pat.bgColor);
			loadStudy();
		}, "json");
	} else {
		// $('#westPane').css('background-color', pat.bgColor);
		loadStudy();
	}
}

function isCompatible() {
	return !!(window.requestFileSystem || window.webkitRequestFileSystem);
}

function saveJpgImages() {
	if(isCompatible()) {
		window.requestFileSystem = window.requestFileSystem
			|| window.webkitRequestFileSystem;
		var secondTR = $('.seriesTable');
		secondTR.find('img').each(function() {
			if (this.complete) {
				saveLocally(this);
			} else {
				this.onload = function() {
					saveLocally(this);
				};
			}

		});
	}
}

function saveLocally(image) {
	var cvs = document.createElement('canvas');
	var ctx = cvs.getContext("2d");

	var fn = '';
	if (image.src.indexOf('images/pdf.png') >= 0) {
		fn = getParameter($(image).attr('imgSrc'), 'object') + '.pdf';
	} else {
		fn = getParameter(image.src, 'object') + '.jpg';
	}
	cvs.width = image.naturalWidth;
	cvs.height = image.naturalHeight;
	ctx.drawImage(image, 0, 0);

	if (image.src.indexOf('images/pdf.png') >= 0) {
		var imd = cvs.toDataURL('image/pdf');
		var ui8a = convertDataURIToBinary(imd);
		var bb = new Blob([ ui8a ], {
			type : 'image/pdf'
		});
	} else {
		var imd = cvs.toDataURL('image/jpeg');
		var ui8a = convertDataURIToBinary(imd);
		var bb = new Blob([ ui8a ], {
			type : 'image/jpeg'
		});
	}

	window.requestFileSystem(window.TEMPORARY, 1024 * 1024, function(fs) {
		fs.root.getFile(fn, {
			create : true
		}, function(fileEntry) {
			// Create a FileWriter object for our FileEntry.
			fileEntry.createWriter(function(fileWriter) {
				fileWriter.onwriteend = function(e) {
					//console.log(fileEntry.fullPath + ' Write completed.');
				};

				fileWriter.onerror = function(e) {
					console.log('Write failed: ' + e.toString());
				};

				fileWriter.write(bb); //.getBlob(contentType[extname]));
			}, fileErrorHandler);
		}, fileErrorHandler);
	}, fileErrorHandler);
}

function convertDataURIToBinary(dataURI) {
	var BASE64_MARKER = ';base64,';
	var base64Index = dataURI.indexOf(BASE64_MARKER) + BASE64_MARKER.length;
	var base64 = dataURI.substring(base64Index);
	var raw = window.atob(base64);
	var rawLength = raw.length;
	var array = new Uint8Array(new ArrayBuffer(rawLength));

	for (i = 0; i < rawLength; i++) {
		array[i] = raw.charCodeAt(i);
	}
	return array;
}

function loadStudy() {
	// load WestPane content
	var tmpUrl = "westContainer.jsp?patient=" + pat.pat_ID + "&study="
			+ pat.studyUID + "&patientName=" + pat.pat_Name;
	tmpUrl += "&studyDesc=" + pat.studyDesc + "&studyDate=" + pat.studyDate
			+ "&totalSeries=" + pat.totalSeries + "&dcmURL=" + pat.dicomURL;
	tmpUrl += "&wadoUrl=" + pat.serverURL;
	$('#westPane').load(encodeURI(tmpUrl));

	document.title = pat.pat_Name;
	getSeries(pat.pat_ID, pat.studyUID);
}

function getSeries(patId, studyUID) {
	$.post("Series.do", {
		"patientID" : patId,
		"studyUID" : studyUID,
		"dcmURL" : pat.dicomURL
	}, function(data) {
		sessionStorage[studyUID] = JSON.stringify(data);
		firstSeries = data[0]['seriesUID'];
		if(pat.serverURL != 'C-MOVE' && pat.serverURL != 'C-GET') {
			$.each(data, function(i, series) {
				getInstances(patId, studyUID, series['seriesUID']);
			});
		}
	}, "json");
}

function getInstances(patId, studyUID, seriesUID) {
	$.post("Instance.do", {
		"patientId" : patId,
		"studyUID" : studyUID,
		"seriesUID" : seriesUID,
		"dcmURL" : pat.dicomURL,
		"serverURL" : pat.serverURL
	}, function(data) {
		sessionStorage[seriesUID] = JSON.stringify(data);
		if (!imgLoaded && firstSeries === seriesUID) {
			loadFirstImage(seriesUID);
		}
	}, "json");
}

function loadFirstImage(serUID) {
	imgLoaded = true;
	var link = '';
	if (isCompatible()) {
		link = "frameContent.html?seriesUID=" + serUID;
	} else {
		link = 'frameContent.html?studyUID=' + pat.studyUID + '&seriesUID='
				+ serUID + '&serverURL=' + pat.serverURL;
	}
	jQuery("iframe").attr('src', link);
	jQuery('#loadingView').hide();
}