var locator = null;
var scoutModel = null;
var zoomPer = null;
var currCanvas = null;
var isLevelLine = false;
var imgPlane = "";
var originX = 0;
var originY = 0;

function Localizer() {
    //this.drawScoutLineWithBorder = drawScoutLine;
}

//Static method

Localizer.drawScoutLineWithBorder = function() {
    var imgType = jQuery(parent.jcanvas).parent().parent().find('#imgType').html();
    var frames = jQuery(parent.document).find('iframe');
    var sRefSopUid = jQuery(parent.jcanvas).parent().parent().find('#refSOPInsUID').html();
    var sFORUid = jQuery(parent.jcanvas).parent().parent().find('#forUIDPanel').html();
    var i;
    var destCanvas;

    if(frames.length <= 1) {
        return;
    }

    if(imgType == 'AXIAL') {
        for(i=0; i<frames.length; i++) {
            if(jQuery(frames[i]).contents().find('#imgType').html() == 'LOCALIZER') {
                var cSopInsUid = jQuery(frames[i]).contents().find('#frameSrc').html();
                cSopInsUid = cSopInsUid.substring(cSopInsUid.indexOf('objectUID=')+10);
                destCanvas = jQuery(frames[i]).contents().find('#canvasLayer1');
                var cFORUid = jQuery(frames[i]).contents().find('#forUIDPanel').html();
                if( (cSopInsUid == sRefSopUid) || (sFORUid == cFORUid)) {
    	            //projectSlice(destCanvas.get(0));
                    currCanvas = destCanvas.get(0);
                    //clearCanvas(currCanvas);
                    projectSlice('AXIAL');
    	        }
            }
        }
    } else if(imgType == 'LOCALIZER') {
        for(i=0; i<frames.length; i++) {
            if(jQuery(frames[i]).contents().find('#imgType').html() == 'AXIAL') {
                destCanvas = jQuery(frames[i]).contents().find('canvas');
                currCanvas = destCanvas.get(0);
                projectSlice('LOCALIZER');
            }
        }
    }
}

Localizer.hideScoutLine = function() {
    var frames = jQuery(parent.document).find('iframe');
    for(var i=0; i<frames.length; i++) {
        //if(jQuery(frames[i]).contents().find('#imgType').html() == 'LOCALIZER') {
            var localCanvas = jQuery(frames[i]).contents().find('#canvasLayer1').get(0);
            if(localCanvas != null) {
                clearCanvas(localCanvas);
            }
            localCanvas = jQuery(frames[i]).contents().find('#canvasLayer2').get(0);
            if( localCanvas != null) {
                clearCanvas(localCanvas);
            }
	//}
    }
    isLevelLine = false;
}

Localizer.clearCanvasContent = function() {
    var localCanvas = jQuery(parent.jcanvas).siblings().get(0);
    clearCanvas(localCanvas);
    localCanvas = jQuery(parent.jcanvas).siblings().get(1);
    clearCanvas(localCanvas);
    isLevelLine = false;
}

function projectSlice(_imgType) {
    var scoutPos = jQuery(currCanvas).parent().parent().find('#imgPosition').html();
    var scoutOrientation = jQuery(currCanvas).parent().parent().find('#imgOrientation').html();
    var scoutPixelSpacing = jQuery(currCanvas).parent().parent().find('#pixelSpacing').html();
    var imgSize = jQuery(currCanvas).parent().parent().find('#imageSize').html().substring(11).split("x");
    var viewSize = jQuery(currCanvas).parent().parent().find('#viewSize').html().substring(11).split("x");
    var scoutRow = imgSize[1];
    var scoutColumn = imgSize[0];

    scoutModel = new ScoutLineModel('');
    scoutModel.imgPosition = scoutPos;
    scoutModel.imgOrientation = scoutOrientation;
    scoutModel.pixelSpacing = scoutPixelSpacing;
    scoutModel.rows = scoutRow;
    scoutModel.columns = scoutColumn;

    var zoomLabel = jQuery(currCanvas).parent().parent().find('#zoomPercent').html();
    zoomLabel = zoomLabel.substring(6, zoomLabel.indexOf("%"));
    zoomPer = parseFloat(zoomLabel / 100);
    
    var dw = (zoomPer * scoutColumn);
    var dh = (zoomPer * scoutRow);    
         
    originX = (currCanvas.width-dw)/2;
    originY = (currCanvas.height-dh)/2;
    
    locator = new SliceLocator();

    if(!isLevelLine) {
        isLevelLine = true;

        var oImgOrient = new ImageOrientation();
        var imgOri = jQuery(currCanvas).parent().parent().find('#imgOrientation').html();
        imgPlane = oImgOrient.getOrientation(imgOri);

        //First  and last slice
        var serUid = jQuery(parent.jcanvas).parent().parent().find('#frameSrc').html();
        serUid = serUid.substring(serUid.indexOf("seriesUID=")+10);
        serUid = serUid.substring(0, serUid.indexOf('&'));               

        drawBorderCT(serUid,_imgType);
    }

    // current slice

    imgPos = jQuery(parent.jcanvas).parent().parent().find('#imgPosition').html();
    imgOrientation = jQuery(parent.jcanvas).parent().parent().find('#imgOrientation').html();
    imgPixelSpacing = jQuery(parent.jcanvas).parent().parent().find('#pixelSpacing').html();
    imgSize = jQuery(parent.jcanvas).parent().parent().find('#imageSize').html().substring(11).split("x");
    imgRow = imgSize[1];
    imgColumn = imgSize[0];

    imgSize = null;
    var cCanvas = jQuery(currCanvas).siblings().get(1);
    clearCanvas(cCanvas);  

    var ps = null;
    var cThickLoc = null;
    var cThick = null;

    if(jQuery(parent.jcanvas).parent().parent().find('#imgType').html() != 'LOCALIZER') {
        ps = jQuery(parent.jcanvas).parent().parent().find('#pixelSpacing').html();

        var psArr = ps.split("\\");
    	ps = parseFloat(psArr[0]) / parseFloat(psArr[1]);

    	cThickLoc = jQuery(parent.jcanvas).parent().parent().find('#thickLocationPanel').html();
    	cThick = parseFloat(cThickLoc.match("Thick:(.*)mm Loc")[1]);
    } else {
	ps = jQuery(parent.jcanvas).parent().parent().find('#pixelSpacing').html();

        var psArr = ps.split("\\");
    	ps = parseFloat(psArr[0]) / parseFloat(psArr[1]);

    	cThickLoc = jQuery(currCanvas).parent().parent().find('#thickLocationPanel').html();
    	cThick = parseFloat(cThickLoc.match("Thick:(.*)mm Loc")[1]);
    }

    var thick = cThick * ps * zoomPer;
    thick = thick / 2;
    //thick = Math.floor(thick * 3.779527559);

    if(imgPlane == "SAGITTAL") {
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
        drawLine(parseInt(locator.getBoxUlx()*zoomPer+originX), parseInt(locator.getBoxUly()*zoomPer+originY), parseInt(locator.getBoxLlx()*zoomPer+originX), parseInt(locator.getBoxLly()*zoomPer+originY), cCanvas, "GREEN", null);

        drawLine(parseInt(locator.getBoxUlx()*zoomPer+originX), parseInt(locator.getBoxUly()*zoomPer+originY)+thick, parseInt(locator.getBoxLlx()*zoomPer+originX), parseInt(locator.getBoxLly()*zoomPer+originY)+thick, cCanvas, "GREEN", null);
        drawLine(parseInt(locator.getBoxUlx()*zoomPer+originX), parseInt(locator.getBoxUly()*zoomPer+originY)-thick, parseInt(locator.getBoxLlx()*zoomPer+originX), parseInt(locator.getBoxLly()*zoomPer+originY)-thick, cCanvas, "GREEN", null);

    } else if(imgPlane == "CORONAL") {
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
        drawLine(parseInt(locator.getmAxisLeftx()*zoomPer+originX), parseInt(locator.getmAxisLefty()*zoomPer+originY), parseInt(locator.getmAxisRightx()*zoomPer+originX), parseInt(locator.getmAxisRighty()*zoomPer+originY), cCanvas, "GREEN", null);

        drawLine(parseInt(locator.getmAxisLeftx()*zoomPer+originX), parseInt(locator.getmAxisLefty()*zoomPer+originY)+thick, parseInt(locator.getmAxisRightx()*zoomPer+originX), parseInt(locator.getmAxisRighty()*zoomPer+originY)+thick, cCanvas, "GREEN", null);
        drawLine(parseInt(locator.getmAxisLeftx()*zoomPer+originX), parseInt(locator.getmAxisLefty()*zoomPer+originY)-thick, parseInt(locator.getmAxisRightx()*zoomPer+originX), parseInt(locator.getmAxisRighty()*zoomPer+originY)-thick, cCanvas, "GREEN", null);

    } else {
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
        drawLine(parseInt(locator.getmAxisLeftx()*zoomPer+originX), parseInt(locator.getmAxisLefty()*zoomPer+originY), parseInt(locator.getmAxisRightx()*zoomPer+originX), parseInt(locator.getmAxisRighty()*zoomPer+originY), cCanvas, "GREEN", parseFloat(cThick * ps * zoomPer));

    }

    return true;
}

function drawLine(x1, y1, x2, y2, canvas, color, lineWidth) {
    var oCtx = canvas.getContext("2d");

    if(lineWidth != null) {
        oCtx.lineWidth = lineWidth;
    }

    oCtx.beginPath();
    oCtx.moveTo(x1, y1);
    oCtx.lineTo(x2, y2);
    oCtx.closePath();
    oCtx.strokeStyle = color;
    oCtx.stroke();
}

function projectScoutLine(sliceModel) {	
    var scoutPos = scoutModel.imgPosition;
    var scoutOrientation = scoutModel.imgOrientation;
    var scoutPixelSpacing = scoutModel.pixelSpacing;
    var scoutRow = scoutModel.rows;
    var scoutColumn = scoutModel.columns;    

    var imgPos = sliceModel.getImgPosition();
    var imgOrientation = sliceModel.getImgOrientation();
    var imgPixelSpacing = sliceModel.getPixelSpacing();
    var imgRow = sliceModel.getRows();
    var imgColumn = sliceModel.getColumns();
    
    if(imgPlane == "SAGITTAL") {
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn); 
        drawLine(parseInt(locator.getBoxUlx()*zoomPer+originX), parseInt(locator.getBoxUly()*zoomPer+originY), parseInt(locator.getBoxLlx()*zoomPer+originX), parseInt(locator.getBoxLly()*zoomPer+originY), currCanvas, "YELLOW", null);
    } else if(imgPlane == "CORONAL") {
        locator.projectSlice(scoutPos, scoutOrientation, scoutPixelSpacing, scoutRow, scoutColumn, imgPos, imgOrientation, imgPixelSpacing, imgRow, imgColumn);
        drawLine(parseInt(locator.getmAxisLeftx()*zoomPer+originX), parseInt(locator.getmAxisLefty()*zoomPer+originY), parseInt(locator.getmAxisRightx()*zoomPer+originX), parseInt(locator.getmAxisRighty()*zoomPer+originY), currCanvas, "YELLOW", null);
    }
}

function clearCanvas(canvas) {
    var oCtx = canvas.getContext("2d");

    // Store the current transformation matrix
    oCtx.save();

    // Use the identity matrix while clearing the canvas
    oCtx.setTransform(1, 0, 0, 1, 0, 0);
    oCtx.clearRect(0, 0, canvas.width, canvas.height);

    // Restore the transform
    oCtx.restore();
}

function drawBorderCT(seriesUid,imgType) {
	var instanceData = JSON.parse(sessionStorage[seriesUid]);
	
	var firstInstance = null;
	var lastInstance = null;
	
	for(var i=0;i<instanceData.length;i++) {
		if((instanceData[i])['imageType']==imgType) {
			firstInstance = instanceData[i];
			break;
		}
	}
	
	for(var i=instanceData.length-1;i>0;i--) {
		if((instanceData[i])['imageType']==imgType) {
			lastInstance = instanceData[i];
			break;
		}
	}
	
	if(firstInstance!=null) {
		var sliceModel = new ScoutLineModel('');
		sliceModel.imgPosition = firstInstance['imagePositionPatient'];
		sliceModel.imgOrientation = firstInstance['imageOrientPatient'];
		sliceModel.pixelSpacing = firstInstance['pixelSpacing'];
		sliceModel.rows = firstInstance['nativeRows'];
		sliceModel.columns = firstInstance['nativeColumns'];
		sliceModel.instanceNo = firstInstance['InstanceNo'];
		projectScoutLine(sliceModel);
	}
	
	if(lastInstance!=null) {
		var sliceModel1 = new ScoutLineModel('');
		sliceModel1.imgPosition = lastInstance['imagePositionPatient'];
		sliceModel1.imgOrientation = lastInstance['imageOrientPatient'];
		sliceModel1.pixelSpacing = lastInstance['pixelSpacing'];
		sliceModel1.rows = lastInstance['nativeRows'];
		sliceModel1.columns = lastInstance['nativeColumns'];
		projectScoutLine(sliceModel1);	
	}
}