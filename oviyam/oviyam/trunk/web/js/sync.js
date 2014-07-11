function syncSeries() {
    var frames = jQuery(parent.document).find('iframe');
    var mCanvas = null;
    var selectedFrame = -1;

    if(frames.length <= 1) {
        return;
    }

    for(var k=0; k<frames.length; k++) {
        if(jQuery(frames[k]).contents().find('html').css('border') == '1px solid rgb(255, 138, 0)') {
            mCanvas = jQuery(frames[k]).contents().find('#imageCanvas');
            selectedFrame = k;
            break;
        }
    }

    var cForUid = jQuery(mCanvas).parent().parent().find('#forUIDPanel').html();
    var cThickLoc = jQuery(mCanvas).parent().parent().find('#thickLocationPanel').html();

    if(cThickLoc==null || cThickLoc.length == 0) {
   // console.log("return");
        return;
    }
        
    for(var i=0;i<frames.length;i++) {
    	//if(jQuery(frames[i]).contents().find('html').css('border') != '2px solid rgb(0,255,0)') {
    	if(i!=selectedFrame) {
    		var forUid = jQuery(frames[i]).contents().find('#forUIDPanel').html();
    		var currCanvas = jQuery(frames[i]).contents().find('#imageCanvas').get(0);
    		if(cForUid == forUid) {
    			var serUid = getParameter(jQuery(frames[i]).contents().find('#frameSrc').html(),'seriesUID');
    			
    			var cThick = cThickLoc.match("Thick:(.*)mm Loc")[1];
                var cLocation = cThickLoc.match("Loc:(.*)mm")[1];
                var fromLoc = parseFloat(cLocation) - parseFloat(cThick);
                var toLoc = parseFloat(cLocation) + parseFloat(cThick);

                var sliceInfo = '';
                var totalImgs = 'Images: ';
                var thickLocPanel = jQuery(frames[i]).contents().find('#thickLocationPanel');
                var totalImgsPanel = jQuery(frames[i]).contents().find('#totalImages');
                jQuery(frames[i]).contents().find('#imageSize');
                
                var imgSize = jQuery(currCanvas).parent().parent().find('#imageSize').html().substring(11).split("x");
			    row = parseInt(imgSize[1]);
			    column = parseInt(imgSize[0]);
                
                var scaleFac = Math.min(currCanvas.width/column, currCanvas.height/row);
                
                var dw = (scaleFac * column);
				var dh = (scaleFac*row); 
                
				var instanceData = JSON.parse(sessionStorage[serUid]);
				for(var j=0;j<instanceData.length;j++) {
					var sliceLoc = (instanceData[j])['sliceLocation'];
					var sliceThickness = (instanceData[j])['sliceThickness'];
					if(sliceLoc>=fromLoc && sliceLoc<=toLoc && (parseFloat(sliceLoc)-parseFloat(cLocation))<128) {
						var instanceNo = (instanceData[j])['InstanceNo'];
						var img1 = jQuery('#' + serUid.replace(/\./g, '_') + '_' + instanceNo,window.parent.document).get(0);
						var currCanvas = jQuery(jQuery(frames[i]).contents().find('#imageCanvas')).get(0);
						currCanvas.getContext("2d").drawImage(img1,(currCanvas.width-dw)/2,(currCanvas.height-dh)/2,dw,dh);
						showHiddenValues(jQuery(jQuery(frames[i]).contents().find('#imageCanvas')),instanceData[i]);
						var thickLocPanel = jQuery(frames[i]).contents().find('#thickLocationPanel');
				        var totalImgsPanel = jQuery(frames[i]).contents().find('#totalImages');
				        var totalImages = totalImgsPanel.html().split("/")[1];              
				        totalImgs += instanceNo + " / " + totalImages;
				        totalImgsPanel.html(totalImgs); 
				        sliceInfo += 'Thick: ' + parseFloat(sliceThickness).toFixed(2) + ' mm ';
                        sliceInfo += 'Loc: ' + parseFloat(sliceLoc).toFixed(2) + ' mm';
                        thickLocPanel.html(sliceInfo);             
						break;
					}
				}
    		}
    	}
    }

   /* var myDb = initDB();
    for(var i=0; i<frames.length; i++) {
        (function(value){
            if(jQuery(frames[i]).contents().find('html').css('border') != '2px solid rgb(0, 255, 0)') {
                var forUid = jQuery(frames[i]).contents().find('#forUIDPanel').html();
                var currCanvas = jQuery(frames[i]).contents().find('#imageCanvas').get(0);
                if(cForUid == forUid) {
                    var qstr = jQuery(frames[i]).contents().find('#frameSrc').html();
                    var serUid = getParameter(qstr, 'seriesUID');

                    var cThick = cThickLoc.match("Thick:(.*)mm Loc")[1];
                    var cLocation = cThickLoc.match("Loc:(.*)mm")[1];
                    var fromLoc = parseFloat(cLocation) - parseFloat(cThick);
                    var toLoc = parseFloat(cLocation) + parseFloat(cThick);

                    var sliceInfo = '';
                    var totalImgs = 'Images: ';
                    var thickLocPanel = jQuery(frames[i]).contents().find('#thickLocationPanel');
                    var totalImgsPanel = jQuery(frames[i]).contents().find('#totalImages');

                    myDb.transaction(function(tx) {
                        var sql = "select SopUID, InstanceNo, SliceLocation, SliceThickness from instance where SliceLocation>=" + fromLoc + " and SliceLocation<=" + toLoc + " and SeriesInstanceUID='" + serUid + "'";
                        //console.log(sql);
                        tx.executeSql(sql, [], function(trans, results) {
                            if(results.rows.length > 0) {
                                var minDiff = 128;
                                var sopUid = '';
                                var sliceThickness = '';
                                var insNumber = '';
                                var sliceLocation = '';

                                for(var j=0; j<results.rows.length; j++) {
                                    var row = results.rows.item(j);
                                    var diff = parseFloat(row['SliceLocation']) - parseFloat(cLocation);
                                    diff = Math.abs(diff);
                                    if(minDiff > diff) {
                                        minDiff = diff;
                                        sopUid = row['SopUID'];
                                        sliceThickness = row['SliceThickness'];
                                        sliceLocation = row['SliceLocation'];
                                        insNumber = row['InstanceNo'];
                                    }
                                } // for j

                                if(isCompatible()) {
                                    var fName = sopUid + '.jpg';
                                    readImage(fName, currCanvas);
                                } else {
                                    //var queryString = window.location.href;
                                    //var studyUID = getParameter(queryString, 'studyUID');
                                    //var seriesUID = getParameter(queryString, 'seriesUID');
                                    //var serverURL = getParameter(queryString, 'serverURL');
                                    //var imgSrc = 'Image.do?serverURL=' + serverURL + '&study=' + studyUID + '&series=' + seriesUID + '&object=' + sopUid;
                                    //showImage(imgSrc, currCanvas);

                                    showImage(serUid + "_" + insNumber, currCanvas);
                                }
                                sliceInfo += 'Thick: ' + parseFloat(sliceThickness).toFixed(2) + ' mm ';
                                sliceInfo += 'Loc: ' + parseFloat(sliceLocation).toFixed(2) + ' mm';
                                thickLocPanel.html(sliceInfo);
                                totalImgs += insNumber + " / " + totalImgsPanel.html().match(" / (.*)")[1];
                                totalImgsPanel.html(totalImgs);
                                showHiddenValues(currCanvas);
                                changeImgPosInSeries(parseInt(insNumber-1), currCanvas);
                            }
                        }, errorHandler);
                    });
                }
            }
        })(i);
    } // for i*/
}

function showHiddenValues(currCanvas,instanceDetails) {

    if(typeof instances == 'undefined') {
        var actFrame = getActiveFrame();
        instances = actFrame.contentWindow.instances;
    }   

    if(instanceDetails['frameOfReferenceUID'] != 'undefined') {
        currCanvas.parent().parent().find('#forUIDPanel').html(instanceDetails['frameOfReferenceUID']);
    }

    if(instanceDetails['refSOPInsUID'] != 'undefined') {
        currCanvas.parent().parent().find('#refSOPInsUID').html(instanceDetails['refSOPInsUID']);
    }
    
    currCanvas.parent().parent().find('#imgPosition').html(instanceDetails['imagePositionPatient']);
    currCanvas.parent().parent().find('#imgOrientation').html(instanceDetails['imageOrientPatient']);
    currCanvas.parent().parent().find('#imgType').html(instanceDetails['imageType']);
    currCanvas.parent().parent().find('#pixelSpacing').html(instanceDetails['pixelSpacing']);
    
   /* var sql = "select FrameOfReferenceUID, ImagePosition, ImageOrientPatient, PixelSpacing, ImageType, ReferencedSOPInsUID from instance where SopUID='" + instances[imgInc] + "';";
    var myDb = initDB();
    myDb.transaction(function(tx) {
        tx.executeSql(sql, [], function(transaction, results) {
            var row = results.rows.item(0);
            if(row['FrameOfReferenceUID'] != 'undefined') {
                jQuery(currCanvas).parent().parent().find('#forUIDPanel').html(row['FrameOfReferenceUID']);
            }

            if(row['ReferencedSOPInsUID'] != 'undefined') {
                jQuery(currCanvas).parent().parent().find('#refSOPInsUID').html(row['ReferencedSOPInsUID']);
            }

            jQuery(currCanvas).parent().parent().find('#imgPosition').html(row['ImagePosition']);
            jQuery(currCanvas).parent().parent().find('#imgOrientation').html(row['ImageOrientPatient']);
            jQuery(currCanvas).parent().parent().find('#imgType').html(row['ImageType']);
            jQuery(currCanvas).parent().parent().find('#pixelSpacing').html(row['PixelSpacing']);
        }, errorHandler);

    });*/
}