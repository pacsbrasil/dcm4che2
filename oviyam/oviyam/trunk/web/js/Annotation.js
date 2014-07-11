var lines = [],ovals = [],rects = [],handles = [],handleSize = 6;
var scaleFac = 1.0, xPixelSpacing = 1,yPixelSpacing = 1; 

function init() {
	for(var i=0;i<8;i++) {
		handles[i] = new ovm.annotation.Handle();
		handles[i].setPosition(new ovm.annotation.Point(0, 0));
	}		
	constructWadoUrl();	
	//scaleFac = jQuery(jcanvas).parent().parent().find('#zoomPercent').html();
	//scaleFac = scaleFac.substring(scaleFac.indexOf(":")+1, scaleFac.indexOf("%"));
	//scaleFac = scaleFac / 100;	
	scaleFac = Math.min(canvasWidth/row,canvasHeight/column).toFixed(2);
	var pixelSpacing = jQuery(jcanvas).parent().parent().find("#pixelSpacing").html().split('\\');
	xPixelSpacing = pixelSpacing[0];
	yPixelSpacing = pixelSpacing[1];
}

function drawRuler(begin,end,length) {
	canvasCtx.fillStyle = "red";
	canvasCtx.beginPath();
	canvasCtx.moveTo(begin.getX(),begin.getY());
	canvasCtx.lineTo(end.getX(),end.getY());
	canvasCtx.stroke();
	canvasCtx.closePath();
	canvasCtx.fillStyle = "maroon";
	canvasCtx.globalAlpha = 0.7;
	canvasCtx.fillRect(begin.getX(),begin.getY()-14,60,14);
	canvasCtx.globalAlpha = 0.9;
	canvasCtx.fillStyle = "white";
	canvasCtx.fillText(length,begin.getX()+2,begin.getY()-3);
};

function drawRect(begin,endPt,area,mean,stdDev) {
	canvasCtx.fillStyle = "red";
	canvasCtx.strokeRect(begin.getX(),begin.getY(),endPt.getX(),endPt.getY());
	canvasCtx.globalAlpha = 0.5;
	canvasCtx.fillStyle = 'maroon';
	canvasCtx.fillRect(begin.getX(),begin.getY()-50,120,40);
	canvasCtx.globalAlpha = 0.9;
	canvasCtx.fillStyle = 'white';
	canvasCtx.fillText("Area : "+area+" cm"+String.fromCharCode(178),begin.getX()+2,begin.getY()-38);
	canvasCtx.fillText("Mean : "+mean,begin.getX()+2,begin.getY()-26);
	canvasCtx.fillText("StdDev : "+stdDev,begin.getX()+2,begin.getY()-14);
};

function drawOval(center,radius,area,mean,stdDev) {
	canvasCtx.fillStyle = "red";
	canvasCtx.save();
	canvasCtx.beginPath();
	canvasCtx.translate(center.getX()-radius.getX(),center.getY()-radius.getY());
	canvasCtx.scale(radius.getX(),radius.getY());
	canvasCtx.arc(1,1,1,0,2*Math.PI,false);
	canvasCtx.restore();
	canvasCtx.stroke();
	canvasCtx.closePath();
	canvasCtx.globalAlpha = 0.7;
	canvasCtx.fillStyle = 'maroon';
	canvasCtx.fillRect(center.getX(),center.getY()-radius.getY()-50,120,40);
	canvasCtx.globalAlpha = 0.9;
	canvasCtx.fillStyle = 'white';
	canvasCtx.fillText("Area : "+area+" cm"+String.fromCharCode(178),center.getX()+2,center.getY()-radius.getY()-38);
	canvasCtx.fillText("Mean : "+mean,center.getX()+2,center.getY()-radius.getY()-26);
	canvasCtx.fillText("StdDev : "+stdDev,center.getX()+2,center.getY()-radius.getY()-14);
}

function drawHandles() {
	canvasCtx.fillStyle = "blue";
	for(var i in handles) {
		canvasCtx.fillRect(handles[i].getPosition().getX(),handles[i].getPosition().getY(),handleSize,handleSize);
	}
}

function addNewLine(begin,end) {
	var len = (calculateLength(begin,end)/10).toFixed(3);
	if(Math.round(len)>0) {
		var newLine = new ovm.annotation.Line();
		newLine.setLineInfo(begin,end,len+" cm");
		lines.push(newLine);
	}
};

function addNewRect(beginPt,endPt) {
	var x = Math.round((beginPt.getX()-originX)/scaleFac);
	var y = Math.round((beginPt.getY()-originY)/scaleFac);
	var w = Math.round(endPt.getX()/scaleFac);
	var h = Math.round(endPt.getY()/scaleFac);	
	var mean = meanOfRect(x,y,w,h);
	var area = areaOfRect(beginPt,endPt);
	if(area>0) {
		var newRect = new ovm.annotation.Rectangle();
		newRect.setRectInfo(beginPt,endPt,area,mean,stdDevOfRect(mean,x,y,w,h));
		rects.push(newRect);
	}
};

function addNewOval(beginPt,endPt) {
	var x = Math.round((beginPt.getX()-originX)/scaleFac);
	var y = Math.round((beginPt.getY()-originY)/scaleFac);
	var w = Math.round(endPt.getX()/scaleFac);
	var h = Math.round(endPt.getY()/scaleFac);
	var mean = meanOfOval(x,y,w,h);
	var area = areaOfOval(beginPt,endPt);
	if(area>0) {
		var newOval = new ovm.annotation.Oval();
		newOval.setOvalInfo(beginPt,endPt,area,mean,stdDevOfRect(mean,x,y,w,h));
		ovals.push(newOval);
	}
};

function detectSelectedShape(point) {
	for(var i in lines) {
		if(isLineIntersects(lines[i].getBegin().getX(),lines[i].getBegin().getY(),lines[i].getEnd().getX(),lines[i].getEnd().getY(),point.getX(),point.getY())) {			
			return lines[i];
		}
	}
	for(var i in rects) {
		if(isRectIntersects(rects[i].getBegin(),rects[i].getSize(),point)) {
			return rects[i];
		}
	}
	for(var i in ovals) {
		if(isOvalIntersects(ovals[i].getCenter(),ovals[i].getRadius(),point)) {
			return ovals[i];
		}
	}
};

function detectSelectedHandle(point) {
	for(var i in handles) {
		if(point.getX()>=handles[i].getPosition().getX() && point.getX()<=handles[i].getPosition().getX()+handleSize && point.getY()>=handles[i].getPosition().getY() && point.getY()<=handles[i].getPosition().getY()+handleSize) {
			return i;
		}
	}
	return -1;
}

function drawAllAnnotations() {
	canvasCtx.clearRect(0,0,canvasWidth,canvasHeight);
	for(var i in lines) {
		drawRuler(lines[i].getBegin(),lines[i].getEnd(),lines[i].getLength());
	}
	for(var i in rects) {
		drawRect(rects[i].getBegin(),rects[i].getSize(),rects[i].getArea(),rects[i].getMean(),rects[i].getStdDev());
	}
	for(var i in ovals) {
		drawOval(ovals[i].getCenter(),ovals[i].getRadius(),ovals[i].getArea(),ovals[i].getMean(),ovals[i].getStdDev());
	}
}

function drawSelectionHandles() {
	switch(selectedShape.getType()) {
		case "ruler":
				drawLineHandles();
				break;
		case "rect":
				drawRectHandles();
				break;
		case "oval":
				drawOvalHandles();
				break;
	}
}

function drawLineHandles() {
	canvasCtx.fillStyle = "blue";
	handles[0].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()-handleSize/2,selectedShape.getBegin().getY()-handleSize/2));
	handles[1].setPosition(new ovm.annotation.Point(selectedShape.getEnd().getX()-handleSize/2,selectedShape.getEnd().getY()-handleSize/2));
	handles[2].setPosition(new ovm.annotation.Point(((selectedShape.getBegin().getX()+selectedShape.getEnd().getX())/2) - handleSize/2,((selectedShape.getBegin().getY()+selectedShape.getEnd().getY())/2) - handleSize/2));

	canvasCtx.fillRect(handles[0].getPosition().getX(),handles[0].getPosition().getY(),handleSize,handleSize);
	canvasCtx.fillRect(handles[1].getPosition().getX(),handles[1].getPosition().getY(),handleSize,handleSize);
	canvasCtx.fillRect(handles[2].getPosition().getX(),handles[2].getPosition().getY(),handleSize,handleSize);
}

function drawRectHandles() {
	handles[0].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX() - handleSize/2,selectedShape.getBegin().getY() - handleSize/2));
	handles[1].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()+(selectedShape.getSize().getX()/2) - handleSize/2,selectedShape.getBegin().getY() - handleSize/2));
	handles[2].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()+selectedShape.getSize().getX() - handleSize/2,selectedShape.getBegin().getY() - handleSize/2));
	handles[3].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX() - handleSize/2,selectedShape.getBegin().getY()+(selectedShape.getSize().getY()/2) - handleSize/2));
	handles[4].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()+selectedShape.getSize().getX() - handleSize/2,selectedShape.getBegin().getY()+(selectedShape.getSize().getY()/2) - handleSize/2));
	handles[5].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX() - handleSize/2,selectedShape.getBegin().getY()+selectedShape.getSize().getY() - handleSize/2));
	handles[6].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()+(selectedShape.getSize().getX()/2) - handleSize/2,selectedShape.getBegin().getY()+selectedShape.getSize().getY() - handleSize/2));
	handles[7].setPosition(new ovm.annotation.Point(selectedShape.getBegin().getX()+selectedShape.getSize().getX() - handleSize/2,selectedShape.getBegin().getY()+selectedShape.getSize().getY() - handleSize/2));
	drawHandles();
}

function drawOvalHandles() {
	handles[0].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()-selectedShape.getRadius().getX(),selectedShape.getCenter().getY()-selectedShape.getRadius().getY()-handleSize/2));
	handles[1].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX(),selectedShape.getCenter().getY()-selectedShape.getRadius().getY()-handleSize/2));
	handles[2].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()+selectedShape.getRadius().getX(),selectedShape.getCenter().getY()-selectedShape.getRadius().getY()-handleSize/2));
	handles[3].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()-selectedShape.getRadius().getX()-handleSize/2,selectedShape.getCenter().getY()));
	handles[4].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()+selectedShape.getRadius().getX()-handleSize/2,selectedShape.getCenter().getY()));
	handles[5].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()-selectedShape.getRadius().getX()-handleSize/2,selectedShape.getCenter().getY()+selectedShape.getRadius().getY()));
	handles[6].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX(),selectedShape.getCenter().getY()+selectedShape.getRadius().getY()-handleSize/2));
	handles[7].setPosition(new ovm.annotation.Point(selectedShape.getCenter().getX()+selectedShape.getRadius().getX()-handleSize/2,selectedShape.getCenter().getY()+selectedShape.getRadius().getY()));
	drawHandles();
}

function getFloatShift(floatNum) {
	var decimalLen = 0;
	var floatElements = floatNum.toString().split('\.');
	if(floatElements.length==2) {
		decimalLen = floatElements[1].length;
	}
	mult = Math.pow(10,decimalLen);
	return mult;
}

function isLineIntersects(x1,y1,x2,y2,mouseX,mouseY) { //Distance between a point and a line
	var a = Math.round(Math.sqrt(Math.pow((x2-x1),2)+Math.pow((y2-y1),2)));
	var b = Math.round(Math.sqrt(Math.pow(mouseX-x1,2)+Math.pow(mouseY-y1,2)));
	var c = Math.round(Math.sqrt(Math.pow(x2-mouseX,2)+Math.pow(y2-mouseY,2)));	
	if(a==b+c) {
		return true;
	}
	return false;
}

function isRectIntersects(beginPt,size,mousePt) {
	if(isLineIntersects(beginPt.getX(),beginPt.getY(),beginPt.getX()+size.getX(),beginPt.getY(),mousePt.getX(),mousePt.getY())) {
		return true;
	} else if(isLineIntersects(beginPt.getX(),beginPt.getY()+size.getY(),beginPt.getX()+size.getX(),beginPt.getY()+size.getY(),mousePt.getX(),mousePt.getY())) {
		return true;
	} else if(isLineIntersects(beginPt.getX(),beginPt.getY(),beginPt.getX(),beginPt.getY()+size.getY(),mousePt.getX(),mousePt.getY())) {
		return true;
	} else if(isLineIntersects(beginPt.getX()+size.getX(),beginPt.getY(),beginPt.getX()+size.getX(),beginPt.getY()+size.getY(),mousePt.getX(),mousePt.getY())) {
		return true;
	}
	return false;
}

function isOvalIntersects(center,radius,mousePt) {
	if(((Math.pow(mousePt.getX()-center.getX(),2)/Math.pow(radius.getX(),2))+(Math.pow(mousePt.getY()-center.getY(),2)/Math.pow(radius.getY(),2))).toFixed(0)==1) {
		return true;
	}
	return false;
}

function isInsideRect(beginPt,endPt,mousePt) {
	if((beginPt.getX()<=mousePt.getX() && beginPt.getX()+endPt.getX()>=mousePt.getX()) && (beginPt.getY()<=mousePt.getY() && beginPt.getY()+endPt.getY()>=mousePt.getY())) {
		return true;
	}
	return false;
}

function isInsideOval(x,y,r1,r2,mouseX,mouseY) {
	if(((Math.pow(mouseX-x,2)/Math.pow(r1,2))+(Math.pow(mouseY-y,2)/Math.pow(r2,2))).toFixed(0)<=1) {
		return true;
	}
	return false;
}

function calculateLength(begin,end) {
	var mult = 1;
	var xDiff = end.getX()-begin.getX();
	var yDiff = end.getY()-begin.getY();

	xDiff/=scaleFac;
	yDiff/=scaleFac;
	
	mult = Math.max(getFloatShift(xPixelSpacing),getFloatShift(yPixelSpacing));	
	var xDistance = mult*xPixelSpacing*xDiff;
	var yDistance = mult*yPixelSpacing*yDiff;
	return (Math.sqrt((Math.pow(xDistance,2)+Math.pow(yDistance,2))/Math.pow(mult,2)));
}

function areaOfRect(begin,end) {
	var width = calculateLength(begin,new ovm.annotation.Point(begin.getX()+end.getX(),begin.getY()))/10;
	var height = calculateLength(begin,new ovm.annotation.Point(begin.getX(),begin.getY()+end.getY()))/10;
	return (width*height).toFixed(3);
}

function meanOfRect(x,y,width,height) {
	var sum = 0, pixelCount = 0;
	for(var i = x;i<x+width;i++) {
		for(var j = y;j<y+height;j++) {
			++pixelCount;
			var pixel = getPixelAt(i,j);
			if(typeof pixel!="undefined") {
				sum+=pixel;
			}
		}
	}
	if(pixelCount==0) {
		return 0;
	}
	return (sum/pixelCount).toFixed(3);
}

function stdDevOfRect(mean,x,y,width,height) {	
	var sum = 0,pixelCount = 0;
	for(var i=x;i<x+width;i++) {
		for(var j=y;j<y+height;j++) {
			var value = getPixelAt(i,j);
			if(typeof value!="undefined") {
				var deviation = value - mean;
				sum+=deviation * deviation;
			}
			pixelCount++;
		}
	}
	if(pixelCount==0) {
		return 0;
	}
	return Math.sqrt(sum/pixelCount).toFixed(3);
}

function measureRect() {
	selectedShape.setArea(areaOfRect(selectedShape.getBegin(),selectedShape.getSize()));
	var x = Math.round((selectedShape.getBegin().getX()-originX)/scaleFac);
	var y = Math.round((selectedShape.getBegin().getY()-originY)/scaleFac);
	var w = Math.round(selectedShape.getSize().getX()/scaleFac);
	var h = Math.round(selectedShape.getSize().getY()/scaleFac);
	selectedShape.setMean(meanOfRect(x,y,w,h));
	selectedShape.setStdDev(stdDevOfRect(selectedShape.getMean(),x,y,w,h));
}

function areaOfOval(begin,end) {
	var radius1 = calculateLength(begin,new ovm.annotation.Point(begin.getX()+end.getX(),begin.getY()))/10;
	var radius2 = calculateLength(begin,new ovm.annotation.Point(begin.getX(),begin.getY()+end.getY()))/10;
	return (Math.PI * radius1 * radius2).toFixed(3);
}

function meanOfOval(x,y,width,height) {
	var sum = 0, pixelCount = 0;
	for(var i = x;i<x+width;i++) {
		for(var j = y;j<y+height;j++) {
			if(isInsideOval(x,y,width,height,i,j)) {
				++pixelCount;
				var pixel = getPixelAt(i,j);
				if(typeof pixel != "undefined") {
					sum+=pixel;
				}
			}
		}
	}
	if(pixelCount==0) {
		return 0;
	}
	return (sum/pixelCount).toFixed(3);
}

function stdDevOfOval(mean,x,y,width,height) {
	var sum = 0,pixelCount = 0;
	for(var i=x;i<x+width;i++) {
		for(var j=y;j<y+height;j++) {
			if(isInsideOval(x,y,width,height,i,j)) {
				var value = getPixelAt(i,j);
				if(typeof value != "undefined") {
					var deviation = value - mean;
					sum+=deviation * deviation;
				}
				pixelCount++;
			}
		}
	}
	if(pixelCount==0) {
		return 0;
	}
	return Math.sqrt(sum/pixelCount).toFixed(3);
}

function measureOval() {
	selectedShape.setArea(areaOfOval(selectedShape.getCenter(),selectedShape.getRadius()));
	var x = Math.round((selectedShape.getCenter().getX()-originX)/scaleFac);
	var y = Math.round((selectedShape.getCenter().getY()-originY)/scaleFac);
	var w = Math.round(selectedShape.getRadius().getX()/scaleFac);
	var h = Math.round(selectedShape.getRadius().getY()/scaleFac);
	selectedShape.setMean(meanOfOval(x,y,w,h));
	selectedShape.setStdDev(stdDevOfOval(selectedShape.getMean(),x,y,w,h));
}

function clearAnnotations() {
	lines = [];
	ovals = [];
	rects = [];
}

function keyEventHandler(e) {
	if(e.which==46) {
		switch(selectedShape.getType()) {
			case 'ruler':
				lines.splice(lines.indexOf(selectedShape),1);
				break;
			case 'rect':
				rects.splice(rects.indexOf(selectedShape),1);
				break;
			case 'oval':
				ovals.splice(ovals.indexOf(selectedShape),1);
				break;
		}
		drawAllAnnotations();	
	}
}

function createNewRuler(e) {
	var endPt = new ovm.annotation.Point(e.pageX-jcanvas.offsetLeft,e.pageY-jcanvas.offsetTop);					
	drawRuler(beginPt, endPt, (calculateLength(beginPt, endPt)/10).toFixed(3)+" cm");
}

function createNewRect(e) {		
	var endPt = new ovm.annotation.Point((e.pageX-jcanvas.offsetLeft)-beginPt.getX(),(e.pageY-jcanvas.offsetTop)-beginPt.getY());					
	if(beginPt.getX()<beginPt.getX()+endPt.getX() && beginPt.getY()<beginPt.getY()+endPt.getY()) {
		drawRect(beginPt,endPt,areaOfRect(beginPt,endPt),"","");
	} else {
		if(beginPt.getX()<beginPt.getX()+endPt.getX()) {
			endPt = new ovm.annotation.Point(endPt.getX(),2);
			drawRect(beginPt,endPt,areaOfRect(beginPt,endPt),"","");
		} else {
			endPt = new ovm.annotation.Point(2,endPt.getY());
			drawRect(beginPt,endPt,areaOfRect(beginPt,endPt),"","");
		}
	}
}

function createNewOval(e) {
	var endPt = new ovm.annotation.Point((e.pageX-jcanvas.offsetLeft)-beginPt.getX(),(e.pageY-jcanvas.offsetTop)-beginPt.getY());
	if(beginPt.getX()<beginPt.getX()+endPt.getX() && beginPt.getY()<beginPt.getY()+endPt.getY()) {
		drawOval(beginPt,endPt,areaOfOval(beginPt,endPt)," "," ");
	} else {
		if(beginPt.getX()<beginPt.getX()+endPt.getX()) {
			endPt = new ovm.annotation.Point(endPt.getX(),2);					
		} else {
			endPt = new ovm.annotation.Point(2,endPt.getY());
		}
		drawOval(beginPt,endPt,areaOfOval(beginPt,endPt),"","");
	}
}

function moveRuler(e) {
	var dist1 = selectedShape.getEnd().getX()-selectedShape.getBegin().getX();
	var dist2 = selectedShape.getEnd().getY()-selectedShape.getBegin().getY();
	selectedShape.setBegin(new ovm.annotation.Point((e.pageX-jcanvas.offsetLeft)-(dist1/2),(e.pageY-jcanvas.offsetTop)-(dist2/2)));
	selectedShape.setEnd(new ovm.annotation.Point(selectedShape.getBegin().getX()+dist1,selectedShape.getBegin().getY()+dist2));								
	drawLineHandles();
}

function moveRect(e) {
	selectedShape.setBegin(new ovm.annotation.Point(e.pageX-jcanvas.offsetLeft,e.pageY-jcanvas.offsetTop));
	drawRect(selectedShape.getBegin(),selectedShape.getSize(),areaOfRect(selectedShape.getBegin(),selectedShape.getSize()),"","");
	drawRectHandles();
	selectedShape.setMean(" ");
	selectedShape.setStdDev(" ");
}

function moveOval(e) {
	selectedShape.setCenter(new ovm.annotation.Point(e.pageX-jcanvas.offsetLeft,e.pageY-jcanvas.offsetTop));
	drawOval(selectedShape.getCenter(),selectedShape.getRadius(),selectedShape.getArea()," ", " ");
	selectedShape.setMean(" ");
	selectedShape.setStdDev(" ");
	drawOvalHandles();
}

function resizeRuler(e) {
	switch(parseInt(selectedHandle)) {
		case 0:
			selectedShape.setBegin(new ovm.annotation.Point(e.pageX-jcanvas.offsetLeft,e.pageY-jcanvas.offsetTop));
			break;
		case 1:
			selectedShape.setEnd(new ovm.annotation.Point(e.pageX-jcanvas.offsetLeft,e.pageY-jcanvas.offsetTop));
			break;
	}
	selectedShape.setLength((calculateLength(selectedShape.getBegin(), selectedShape.getEnd())/10).toFixed(3)+" cm");
	drawLineHandles();
}

function resizeRect(e) {
	var oldX = selectedShape.getBegin().getX(), oldY = selectedShape.getBegin().getY();
	var evtX = e.pageX - jcanvas.offsetLeft;
	var evtY = e.pageY - jcanvas.offsetTop;
	switch(parseInt(selectedHandle)) {
		case 0:	
			if(evtX<=(oldX+selectedShape.getSize().getX()+(oldX-evtX)) && evtY<=(oldY+selectedShape.getSize().getY()+(oldY-evtY))) {
				selectedShape.setBegin(new ovm.annotation.Point(evtX,evtY));
				selectedShape.setSize(new ovm.annotation.Point(selectedShape.getSize().getX()+oldX-evtX,selectedShape.getSize().getY()+oldY-evtY));
			}					
			break;								
		case 1:
			if(evtY<=(oldY+selectedShape.getSize().getY()+(oldY-evtY))) {
				selectedShape.setBegin(new ovm.annotation.Point(selectedShape.getBegin().getX(),evtY));
				selectedShape.setSize(new ovm.annotation.Point(selectedShape.getSize().getX(),selectedShape.getSize().getY()+oldY-evtY));
			}
			break;
		case 2:
			if(oldX<=oldX+(evtX-oldX) && evtY<=oldY+(selectedShape.getSize().getY()+oldY-evtY)) {
				selectedShape.setBegin(new ovm.annotation.Point(selectedShape.getBegin().getX(),evtY));
				selectedShape.setSize(new ovm.annotation.Point(evtX-oldX,selectedShape.getSize().getY()+oldY-evtY));
			}
			break;
		case 3:
			if(evtX<=oldX+(selectedShape.getSize().getX()+oldX-evtX)) {
				selectedShape.setBegin(new ovm.annotation.Point(evtX,selectedShape.getBegin().getY()));
				selectedShape.setSize(new ovm.annotation.Point(selectedShape.getSize().getX()+oldX-evtX,selectedShape.getSize().getY()));									
			}
			break;
		case 4:
			if(oldX<=oldX+(evtX-oldX)) {
				selectedShape.setSize(new ovm.annotation.Point(evtX-oldX,selectedShape.getSize().getY()));	
			}
			break;
		case 5:
			if(evtX<=oldX+(selectedShape.getSize().getX()+(oldX-evtX)) && oldY<=oldY+(evtY-oldY)) {
				selectedShape.setBegin(new ovm.annotation.Point(evtX,selectedShape.getBegin().getY()));
				selectedShape.setSize(new ovm.annotation.Point(selectedShape.getSize().getX()+(oldX-evtX),evtY-oldY));
			}
			break;
		case 6:
			if(selectedShape.getBegin().getY()<=selectedShape.getBegin().getY()+evtY-oldY) {
				selectedShape.setSize(new ovm.annotation.Point(selectedShape.getSize().getX(),evtY-oldY));
			}
			break;
		case 7:
			if(oldX<=oldX+(evtX-oldX) && oldY<=oldY+(evtY-oldY)) {
				selectedShape.setSize(new ovm.annotation.Point(evtX-oldX,evtY-oldY));
			}
			break;
		}
		selectedShape.setArea(areaOfRect(selectedShape.getBegin(),selectedShape.getSize()));
		selectedShape.setMean(" ");
		selectedShape.setStdDev(" ");
		drawRectHandles();
}

function resizeOval(e) {
	var evtX = e.pageX - jcanvas.offsetLeft;
	var evtY = e.pageY - jcanvas.offsetTop;
	
	switch(parseInt(selectedHandle)) {
		case 0:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+selectedShape.getCenter().getX()-evtX)/2,(selectedShape.getRadius().getY()+selectedShape.getCenter().getY()-evtY)/2));
			break;
		case 1:
			selectedShape.setRadius(new ovm.annotation.Point(selectedShape.getRadius().getX(),(selectedShape.getRadius().getY()+selectedShape.getCenter().getY()-evtY)/2));
			break;
		case 2:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+evtX-selectedShape.getCenter().getX())/2,(selectedShape.getRadius().getY()+selectedShape.getCenter().getY()-evtY)/2));
			break;
		case 3:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+selectedShape.getCenter().getX()-evtX)/2,selectedShape.getRadius().getY()));
			break;
		case 4:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+evtX-selectedShape.getCenter().getX())/2,selectedShape.getRadius().getY()));
			break;
		case 5:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+selectedShape.getCenter().getX()-evtX)/2,(selectedShape.getRadius().getY()+evtY-selectedShape.getCenter().getY())/2));
			break;
		case 6:
			selectedShape.setRadius(new ovm.annotation.Point(selectedShape.getRadius().getX(),(selectedShape.getRadius().getY()+evtY-selectedShape.getCenter().getY())/2));
			break;
		case 7:
			selectedShape.setRadius(new ovm.annotation.Point((selectedShape.getRadius().getX()+evtX-selectedShape.getCenter().getX())/2,(selectedShape.getRadius().getY()+evtY-selectedShape.getCenter().getY())/2));
			break;
	}
	drawOval(selectedShape.getCenter(),selectedShape.getRadius(),areaOfOval(selectedShape.getCenter(),selectedShape.getRadius())," "," ");
	selectedShape.setMean(" ");
	selectedShape.setStdDev(" ");
	drawOvalHandles();
}

/*function clearAnnotations() {
		lines.length = 0;
		rects.length = 0;
		ovals.length = 0;			
}*/