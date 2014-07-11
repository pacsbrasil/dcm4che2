var wadoURL;
var mouseLocX;
var mouseLocY;

var wcenter;
var wwidth;
var rescale_Slope;
var rescale_Intercept;
var lookupTable;
var huLookupTable;
var pixelBuffer = new Array();

var row;
var column;
var lookupObj;
var zoomPercent;

var canvas;
var ctx;
var myImageData;
var winEnabled = false;
var tmpCanvas;

String.prototype.replaceAll = function(pcFrom, pcTo){
    var i = this.indexOf(pcFrom);
    var c = this;
    while (i > -1){
        c = c.replace(pcFrom, pcTo);
        i = c.indexOf(pcFrom);
    }
    return c;
}

function mouseDownHandler(evt)
{
    mousePressed=1;
    jQuery('.contextMenu').hide();//To hide the popup if showing

    zoomPercent = jQuery(jcanvas).parent().parent().find('#zoomPercent').html();
    zoomPercent = zoomPercent.substring(zoomPercent.indexOf(":")+1, zoomPercent.indexOf("%"));
    zoomPercent = zoomPercent / 100;
    
    //zoomPercent = parent.scale;

    if(imageLoaded==1)
    {
       /* mouseLocX = evt.pageX - parent.jcanvas.offsetLeft;
        mouseLocX = parseInt(mouseLocX / zoomPercent);
        mouseLocY = evt.pageY - parent.jcanvas.offsetTop;
        mouseLocY = parseInt(mouseLocY / zoomPercent);*/
        
        mouseLocX = evt.pageX;
        mouseLocY = evt.pageY;
    }

    evt.preventDefault();
    evt.stopPropagation();
    evt.target.style.cursor = "url(images/wincursor.png), auto";
}

function mouseupHandler(evt)
{
    mousePressed=0;
    evt.target.style.cursor = "default";
    wlApplied = false;
//applyWindowing();
}

function mousemoveHandler(evt)
{

    try
    {
        if(parent.imageLoaded==1)
        {
           /* mouseLocX1 = evt.pageX - jcanvas.offsetLeft;
            mouseLocX1 = parseInt(mouseLocX1 / zoomPercent);
            mouseLocY1 = evt.pageY - jcanvas.offsetTop;
            mouseLocY1 = parseInt(mouseLocY1 / zoomPercent);*/            

            //if(mouseLocX1>=0&&mouseLocY1>=0&&mouseLocX1<column&&mouseLocY1<row)
           // {
                showHUvalue(parseInt(evt.pageX/zoomPercent),parseInt(evt.pageY/zoomPercent));

                if(mousePressed==1)
                {                   
                    var diffX=parseInt((evt.pageX-mouseLocX)/zoomPercent);
                    var diffY=parseInt((mouseLocY-evt.pageY)/zoomPercent);      
                    
                    parent.wc=parseInt(parent.wc)+diffY;
                    parent.ww=parseInt(parent.ww)+diffX;                    

                    if(parent.ww < 1) {
                        parent.ww = 1;
                    }
                    showWindowingValue(parent.wc,parent.ww);
                    lookupObj.setWindowingdata(parent.wc,parent.ww);
                    //genImage();
                    renderImage();
                    mouseLocX=evt.pageX;
                    mouseLocY=evt.pageY;
                    jQuery('.selected').removeClass('selected');

                }
           // }
        }
    }
    catch(err)
    {
    	console.log(err);
    }

}

function changePreset(presetValue)
{
    if(winEnabled) {
        //applyPreset(parseInt(document.getElementById("preset").options[document.getElementById("preset").selectedIndex].value));
        applyPreset(parseInt(presetValue));
    }
}

function changePreset(presetDiv,presetValue)
{
    if(winEnabled) {
    	jQuery('.selected').removeClass('selected');
		jQuery(presetDiv).addClass("selected");
        //applyPreset(parseInt(document.getElementById("preset").options[document.getElementById("preset").selectedIndex].value));
        applyPreset(parseInt(presetValue));
    }
}

function applyPreset(preset)
{
    switch (preset)
    {
        case 1:
            parent.wc=wcenter;
            parent.ww=wwidth;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;

        case 2:
            parent.wc=350;
            parent.ww=40;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;

        case 3:
            parent.wc=-600;
            parent.ww=1500;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;

        case 4:
            parent.wc=40;
            parent.ww=80;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;

        case 5:
            parent.wc=480;
            parent.ww=2500;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;

        case 6:
            parent.wc=90;
            parent.ww=350;
            lookupObj.setWindowingdata(parent.wc,parent.ww);
            renderImage();
            break;
    }
    showWindowingValue(parent.wc,parent.ww);
    jQuery('#winContext').hide();	
}

function showHUvalue(x,y)
{
    var t=(y*column)+x;
    //var hupanel=document.getElementById("huDisplayPanel");
    //hupanel.innerHTML="X :"+x+" Y :"+y+" HU :"+huLookupTable[pixelBuffer[t]];
    var huValue = "X :"+x+" Y :"+y+" HU :"+huLookupTable[pixelBuffer[t]];
    selectedFrame.find('#huDisplayPanel').html(huValue);
}

function showWindowingValue(wcenter,wwidth)
{
    var winValue = "WL: "+wcenter+" / WW: "+wwidth;
    selectedFrame.find('#windowLevel').html(winValue);
}

function loadDicom() {
    //stop zoom if zoom enabled
    if(zoomEnabled) {
        var zDiv = jQuery('#zoomIn').get(0);
        stopZoom(zDiv);
    }

    // stop move if move enabled
    if(moveEnabled) {
        var mvDiv = jQuery('#move').get(0);
        stopMove(mvDiv);
    }
    
    //Stop stack navigation if enabled
    if(scrollImages) {
    	var mvDiv = jQuery('#stackImage').get(0);
    	doStack(mvDiv);
    }
    
    if(measureEnabled) {
    	doMeasurement(jQuery('#ruler').get(0));
    }
    
    var imgSize = jQuery(jcanvas).parent().parent().find('#imageSize').html().substring(11).split("x");
    row = parseInt(imgSize[1]);
    column = parseInt(imgSize[0]);

    //var viewSize = jQuery(jcanvas).parent().parent().find('#viewSize').html().substring(10).split("x");
    /*if(parseInt(imgSize[0]) > parseInt(viewSize[0])) {
        return;
    } else if(parseInt(imgSize[1]) > parseInt(viewSize[1])) {
        return;
    } */

    var curr = jQuery('#containerBox').find('.current');

    var queryString = jQuery(jcanvas).parent().parent().find("#frameSrc").html();

    var seriesUID = getParameter(queryString, 'seriesUID');   
    
    var objectUID = getParameter(queryString, 'objectUID');

    var layerCanvas = jQuery(jcanvas).parent().children().get(2);

    if(!winEnabled) {
        winEnabled = true;
        doMouseWheel = false;
        //jQuery('#preset').removeAttr('disabled');
       // jQuery("#presetDiv input").removeAttr('disabled');
        //jQuery("#presetDiv a").css('visibility', 'visible');
        if(parent.pat.serverURL.indexOf("wado")>0) {
        	jQuery(jcanvas).parent().parent().find('#applyWLDiv').show();
        }
        jQuery(jcanvas).parent().parent().find('#huDisplayPanel').show();
        jQuery(jcanvas).parent().parent().find('#thickLocationPanel').hide();
        //jQuery('#containerBox .toolbarButton').unbind('mouseenter').unbind('mouseleave');
        //jQuery(curr).attr('class','toolbarButton current');   
        jQuery('#windowing').addClass('toggleOff');
		jQuery('#lblWindowing').removeClass('imgOff').addClass('imgOn');  		

		if(objectUID=='null') {
			var instanceNo = parseInt(jQuery(jcanvas).parent().parent().find('#totalImages').html().split(':')[1].split('/')[0])-1;		
			var instData = JSON.parse(sessionStorage[seriesUID])[instanceNo];
			objectUID = instData['SopUID'];
			var windowCenter = instData['windowCenter'].indexOf('|')>=0 ? instData['windowCenter'].substring(0,instData['windowCenter'].indexOf('|')) : instData['windowCenter'];
			var windowWidth = instData['windowWidth'].indexOf('|')>=0 ? instData['windowWidth'].substring(0,instData['windowWidth'].indexOf('|')) : instData['windowWidth'];
			jQuery(jcanvas).parent().parent().find('#windowLevel').html('WL:' + windowCenter + " / WW: " + windowWidth);
		}  

        wadoURL = parent.pat.serverURL + "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + parent.pat.studyUID + "&seriesUID=" + seriesUID + "&objectUID=" + objectUID; 
		parseAndLoadDicom();
		
       jQuery(layerCanvas).mouseup(function(evt) {
            mouseupHandler(evt);
        }).mousedown(function(evt) {
            mouseDownHandler(evt);
        }).mousemove(function(evt) {
            mousemoveHandler(evt);
        });
    } else {
        stopWLAdjustment();
    }
}

function stopWLAdjustment() {
    winEnabled = false;
    doMouseWheel = true;
    var layerCanvas = jQuery(jcanvas).parent().children().get(2);    
    jQuery(jcanvas).parent().parent().find('#applyWLDiv').hide();
    jQuery(jcanvas).parent().parent().find('#thickLocationPanel').show();
    jQuery(jcanvas).parent().parent().find('#huDisplayPanel').hide();
    //jQuery(curr).attr('class', 'toolbarButton');
    //jQuery(curr).children().attr('class', 'imgOff');
    jQuery(layerCanvas).unbind('mousedown').unbind('mouseup');

    //doContainerBoxHOver();
    jQuery('#windowing').removeClass('toggleOff');
    jQuery('#lblWindowing').removeClass('imgOn').addClass('imgOff');
}

function unBindWindowing() {
	if (jQuery(jcanvas).parent().parent().parent().parent().css('border') != '1px solid rgb(255, 138, 0)') {
		jQuery(jQuery(jcanvas).parent().children().get(2)).unbind('mousedown')
				.unbind('mouseup').unbind('mousemove');

		jQuery(jcanvas).parent().parent().find('#applyWLDiv').hide();
		jQuery(jcanvas).parent().parent().find('#thickLocationPanel').show();
		jQuery(jcanvas).parent().parent().find('#huDisplayPanel').hide();
	}
}

function bindWindowing() {
	if (jQuery(jQuery(jcanvas).parent().children().get(2)).data('events') == null) {
		var imgSize = jQuery(jcanvas).parent().parent().find('#imageSize')
				.html().substring(11).split("x");
		row = parseInt(imgSize[1]);
		column = parseInt(imgSize[0]);
		var queryString = jQuery(jcanvas).parent().parent().find("#frameSrc")
				.html();
		wadoURL = parent.pat.serverURL
				+ "/wado?requestType=WADO&contentType=application/dicom&studyUID="
				+ parent.pat.studyUID + "&seriesUID="
				+ getParameter(queryString, 'seriesUID') + "&objectUID="
				+ getParameter(queryString, 'objectUID');
		parseAndLoadDicom();

		if(parent.pat.serverURL.indexOf("wado")>0) {
			jQuery(jcanvas).parent().parent().find('#applyWLDiv').show();
		}
		jQuery(jcanvas).parent().parent().find('#huDisplayPanel').show();
		jQuery(jcanvas).parent().parent().find('#thickLocationPanel').hide();

		jQuery(jQuery(jcanvas).parent().children().get(2)).mouseup(
				function(evt) {
					mouseupHandler(evt);
				}).mousedown(function(evt) {
			mouseDownHandler(evt);
		}).mousemove(function(evt) {
			mousemoveHandler(evt);
		});
	}
}

function getContextPath()
{
    var path = top.location.pathname;
    if (document.all) {
        path = path.replace(/\\/g,"/");
    }
    path = path.substr(0,path.lastIndexOf("/")+1);

    return path;
}

function parseAndLoadDicom()
{
    //alert(wadoURL);
    var reader=new DicomInputStreamReader();

    if( !(!(wadoURL.indexOf('C-GET') >= 0) && !(wadoURL.indexOf('C-MOVE') >= 0))) {
        //var urlTmp = "DcmFile.do?study=" + getParameter(wadoURL, "studyUID") + "&object=" + getParameter(wadoURL, "objectUID");
        var urlTmp = "Wado.do?study=" + getParameter(wadoURL, "studyUID") + "&object=" + getParameter(wadoURL, "objectUID") + "&contentType=application/dicom";
    	reader.readDicom(urlTmp);
    } else {
    	 reader.readDicom("DcmStream.do?wadourl="+wadoURL.replaceAll("&","_"));
    }

    /*var urlTmp = "Wado.do?dicomURL=DICOM://ASGARDCM:OVIYAM2@localhost:11112&study=" + getParameter(wadoURL, "studyUID") + "&series=" + getParameter(wadoURL, "seriesUID");
    urlTmp += "&object=" + getParameter(wadoURL, "objectUID");
    reader.readDicom(urlTmp);*/

    /*jQuery.post(urlTmp, function(data) {
    	alert(data); 
    }); */

    var dicomBuffer=reader.getInputBuffer();
    var dicomReader=reader.getReader();
    var dicomParser=new DicomParser(dicomBuffer,dicomReader);
    dicomParser.parseAll();
    var elementindex=0;
    for(;elementindex<dicomParser.dicomElement.length;elementindex++)
    {
        var dicomElement=dicomParser.dicomElement[elementindex];
        if(dicomElement.name=="windowWidth")
        {
            wwidth=ww=dicomElement.value[0];
        }
        else if(dicomElement.name=="windowCenter")
        {
            wcenter=wc=dicomElement.value[0];
        }
        else if(dicomElement.name=="rescaleIntercept")
        {
            rescale_Intercept=parseInt(dicomElement.value);
        }
        else if(dicomElement.name=="rescaleSlope")
        {
            rescale_Slope=parseInt(dicomElement.value);
        }
    }
    pixelBuffer=dicomParser.pixelBuffer;
    lookupObj=new LookupTable();
    lookupObj.setData(wc,ww,rescale_Slope,rescale_Intercept);
    lookupObj.calculateHULookup();
    huLookupTable=lookupObj.huLookup;

    ctx = jcanvas.getContext("2d");
    var iNewWidth = jcanvas.width;
    var iNewHeight = jcanvas.height;

    jcanvas.width = column;
    jcanvas.height = row;

	ctx.fillStyle="black";
    ctx.fillRect(0, 0, column, row);
    //myImageData = ctx.getImageData(0,0,column,row);

    getWindowingValue();
    lookupObj.setWindowingdata(wc,ww);

    jcanvas.width = iNewWidth;
    jcanvas.height = iNewHeight;

    //genImage();
    initialize();
    parent.imageLoaded=1;
}

function initialize() {    
	tmpCanvas = document.createElement('canvas');
	var tmpCxt = tmpCanvas.getContext('2d');
	
	tmpCanvas.width = column;
    tmpCanvas.height = row;

    tmpCanvas.style.width = column;
    tmpCanvas.style.height = row;

    tmpCxt.fillStyle = "white";
    tmpCxt.fillRect(0,0,column,row);
    
    myImageData = tmpCxt.getImageData(0,0,column,row);
    
    //genImage();
    renderImage();
}


function getRenderCanvas() {    
    var canvasImageDataIndex = 3;
     var storedPixelDataIndex = 4;
     var numPixels = column * row;
     
     lookupObj.calculateLookup();
    lookupTable=lookupObj.ylookup;
    var localData = myImageData.data;
    
     while(storedPixelDataIndex<numPixels) {
	     localData[canvasImageDataIndex] = lookupTable[pixelBuffer[storedPixelDataIndex++]];
     	canvasImageDataIndex+=4;
     }   
     
    var tmpCxt = tmpCanvas.getContext('2d'); 
    tmpCxt.putImageData(myImageData,0,0);
    //return tmpCanvas;
}

function renderImage() {
	ctx.setTransform(1,0,0,1,0,0);
	ctx.fillStyle = 'black';
	ctx.fillRect(0,0,jcanvas.width, jcanvas.height);
	
	//var renderCanvas = getRenderCanvas();
	
	getRenderCanvas();
	
	var sw = jcanvas.width;
    var sh = jcanvas.height;
    var xScale = sw / column;
    var yScale = sh / row;
    
    var scaleFac = Math.min(xScale,yScale);	
    /*zoomPercent = jQuery(jcanvas).parent().parent().find('#zoomPercent').html();
    zoomPercent = zoomPercent.substring(zoomPercent.indexOf(":")+1, zoomPercent.indexOf("%"));
    zoomPercent = zoomPercent / 100;
    
	var dw = (zoomPercent * column);
	var dh = (zoomPercent*row); */
	
	var dw = (scaleFac * column);
	var dh = (scaleFac * row);
	
	/*var dw = (parent.scale * column);
	var dh = (parent.scale * row);*/
	
	var sx = (sw-dw)/2;
	var sy = (sh-dh)/2;
	
	//ctx.drawImage(renderCanvas, 0,0,column,row,0,0,column,row);
	ctx.drawImage(tmpCanvas, 0, 0, column, row, sx, sy, dw, dh);
}
function getWindowingValue() {
    var divVal = selectedFrame.find('#windowLevel').html();
    var values = divVal.split("/");
    wc = values[0].substring(values[0].indexOf(':')+1).trim();
    ww = values[1].substring(values[1].indexOf(':')+1).trim();
}

function retrieveImage1(studyUID, seriesUID, instanceUID) {

    window.requestFileSystem  = window.requestFileSystem || window.webkitRequestFileSystem;

    /*var selServer = $("#availableServers input[type=checkbox]:checked").parent().parent();
    var host = selServer.find('td:nth-child(4)').html();
    var wadoPort = selServer.find('td:nth-child(6)').html(); */

    var xhr = new XMLHttpRequest();
    //var url = 'Image.do?serverURL=http://' + host + ':' + wadoPort + '&study=' + studyUID + '&series=' + seriesUID + '&object=' + instanceUID;
    var url = 'Image.do?serverURL=' + parent.wadoURL.substring(0, parent.wadoURL.indexOf('wado')-1) + '&study=' + studyUID + '&series=' + seriesUID + '&object=' + instanceUID;
    url = url + '&windowCenter=' + parent.wc + '&windowWidth=' + parent.ww;

    xhr.open('GET', url, true);
    xhr.responseType = 'arraybuffer';

    xhr.onload = function(e) {
        if(this.status == 200) {
            window.requestFileSystem(window.TEMPORARY, 1024*1024, function(fs) {
                var fn = '';
                /*if(sopClassUID == '1.2.840.10008.5.1.4.1.1.104.1') {
                fn = instanceUID+'.pdf';
            } else { */
                fn = instanceUID+'.jpg';
                //}

                fs.root.getFile(fn, {
                    create:true
                }, function(fileEntry) {
                    fileEntry.createWriter(function(writer) {
                        writer.onwriteend = function(e) {
                            console.log(fileEntry.fullPath + " created");
                        //updateProgress();
                        }
                        writer.onerror = function(e) {
                            console.log(e.toString());
                        }

                        var bb;
                        if(window.BlobBuilder) {
                            bb = new BlobBuilder();
                        } else if(window.WebKitBlobBuilder) {
                            bb = new WebKitBlobBuilder();
                        }
                        bb.append(xhr.response);

                        /*if(sopClassUID == '1.2.840.10008.5.1.4.1.1.104.1') {
                      	writer.write(bb.getBlob('application/pdf'));
                     } else { */
                        writer.write(bb.getBlob('image/jpeg'));
                    //}
                    }, fileErrorHandler);
                }, fileErrorHandler);
            }, fileErrorHandler);
        }
    };
    xhr.send();
}

function constructWadoUrl() { // Load the dicom file if wado url is null
    //stop zoom if zoom enabled
    if(zoomEnabled) {
        var zDiv = jQuery('#zoomIn').get(0);
        stopZoom(zDiv);
    }

    // stop move if move enabled
    if(moveEnabled) {
        var mvDiv = jQuery('#move').get(0);
        stopMove(mvDiv);
    }

    var imgSize = jQuery(jcanvas).parent().parent().find('#imageSize').html().substring(11).split("x");
    row = parseInt(imgSize[1]);
    column = parseInt(imgSize[0]);
    var queryString = jQuery(jcanvas).parent().parent().find("#frameSrc").html();
    var seriesUID = getParameter(queryString, 'seriesUID');
    var objectUID = getParameter(queryString, 'objectUID');
    
    if(objectUID=='null') {
			var instanceNo = parseInt(jQuery(jcanvas).parent().parent().find('#totalImages').html().split(':')[1].split('/')[0])-1;		
			var instData = JSON.parse(sessionStorage[seriesUID])[instanceNo];
			objectUID = instData['SopUID'];
			var windowCenter = instData['windowCenter'].indexOf('|')>=0 ? instData['windowCenter'].substring(0,instData['windowCenter'].indexOf('|')) : instData['windowCenter'];
			var windowWidth = instData['windowWidth'].indexOf('|')>=0 ? instData['windowWidth'].substring(0,instData['windowWidth'].indexOf('|')) : instData['windowWidth'];
			jQuery(jcanvas).parent().parent().find('#windowLevel').html('WL:' + windowCenter + " / WW: " + windowWidth);
			jQuery(jcanvas).parent().parent().find('#pixelSpacing').html(instData['pixelSpacing']);
		}  

    wadoURL = parent.pat.serverURL + "/wado?requestType=WADO&contentType=application/dicom&studyUID=" + parent.pat.studyUID + "&seriesUID=" + seriesUID + "&objectUID=" + objectUID;      
	parseAndLoadDicom();
}

function getPixelAt(x,y) {
	var t = (y*column)+x;
	return huLookupTable[pixelBuffer[t]];
}